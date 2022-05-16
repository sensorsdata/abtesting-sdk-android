/*
 * Created by zhangxiangwei on 2020/09/09.
 * Copyright 2020－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.abtest.core;


import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.util.SABAlarmManager;
import com.sensorsdata.abtest.util.TaskRunner;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.listener.SAEventListener;
import com.sensorsdata.analytics.android.sdk.listener.SAJSListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SensorsABTestHelper implements SAJSListener, SAEventListener, AppStateManager.AppStateChangedListener {

    private static final String TAG = "SensorsABTestHelper";
    /**
     * 初始化请求超时时间
     */
    private static final long REQUEST_TIMEOUT = 30 * 1000L;
    /**
     * 初始化请求失败的重试次数
     */
    private static final int REQUEST_RETRY_TIMES = 3;
    private Context mContext;

    public void init(Context context) {
        this.mContext = context;
        SensorsABTestCacheManager.getInstance().loadExperimentsFromDiskCache();
        SensorsABTestCustomIdsManager.getInstance().loadCustomIds();
        SensorsDataAPI.sharedInstance().addSAJSListener(this);
        SensorsDataAPI.sharedInstance().addEventListener(this);
        requestExperimentsAndUpdateCacheWithRetry(REQUEST_RETRY_TIMES, 0L);
        if (context instanceof Application) {
            Application application = (Application) context;
            AppStateManager appStateManager = new AppStateManager();
            appStateManager.addAppStateChangedListener(this);
            application.registerActivityLifecycleCallbacks(appStateManager);
        }
    }

    /**
     * 处理从 JS 发送过来的请求
     *
     * @param view WebView
     * @param content 消息实体
     */
    @Override
    public void onReceiveJSMessage(WeakReference<View> view, String content) {
        new SensorsABTestH5Helper(view, content).handlerJSMessage();
    }

    @Override
    public void onEnterForeground(boolean resumeFromBackground) {
        SALog.i("AppStartupManager", "onEnterForeground");
        SABAlarmManager.getInstance().refreshInterval();
    }

    @Override
    public void onEnterBackground() {
        SALog.i("AppStartupManager", "onEnterBackground");
        SABAlarmManager.getInstance().cancelAlarm();
    }

    private void requestExperimentsAndUpdateCacheWithRetry(final int retryTimes, long lastRequestTime) {
        if (retryTimes < 0) return;
        final long currentTime = SystemClock.elapsedRealtime();
        long delayTime = 0L, waitTime;
        if (lastRequestTime != 0L && (waitTime = currentTime - lastRequestTime) < REQUEST_TIMEOUT) {
            delayTime = REQUEST_TIMEOUT - waitTime;
        }
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache(null, null, new IApiCallback<Map<String, Experiment>>() {
                    @Override
                    public void onSuccess(Map<String, Experiment> stringExperimentMap) {
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        requestExperimentsAndUpdateCacheWithRetry(retryTimes - 1, currentTime);
                    }
                });
            }
        };
        TaskRunner.getBackHandler().postDelayed(requestTask, delayTime);
    }

    @Override
    public void trackEvent(JSONObject jsonObject) {
        try {
            String event = jsonObject.optString("event");
            if (TextUtils.equals("$ABTestTrigger", event)) {
                JSONObject properties = jsonObject.optJSONObject("properties");
                if (properties == null) {
                    return;
                }
                SALog.i(TAG, String.format("distinct_id is %s,login_id is %s,anonymous_id is %s", properties.optString("$abtest_distinct_id"), properties.optString("$abtest_login_id"), properties.optString("$abtest_anonymous_id")));
                if (properties.has("$abtest_distinct_id")) {
                    try {
                        jsonObject.put("distinct_id", properties.optString("$abtest_distinct_id"));
                        properties.remove("$abtest_distinct_id");
                    } catch (JSONException e) {
                        SALog.printStackTrace(e);
                    }
                }
                if (properties.has("$abtest_login_id")) {
                    try {
                        jsonObject.put("login_id", properties.optString("$abtest_login_id"));
                        properties.remove("$abtest_login_id");
                    } catch (JSONException e) {
                        SALog.printStackTrace(e);
                    }
                }
                if (properties.has("$abtest_anonymous_id")) {
                    try {
                        jsonObject.put("anonymous_id", properties.optString("$abtest_anonymous_id"));
                        properties.remove("$abtest_anonymous_id");
                    } catch (JSONException e) {
                        SALog.printStackTrace(e);
                    }
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public void login() {
        SALog.i(TAG, "login");
        onUserInfoChanged();
    }

    @Override
    public void logout() {
        SALog.i(TAG, "logout");
        onUserInfoChanged();
    }

    @Override
    public void identify() {
        SALog.i(TAG, "identify");
        if (TextUtils.isEmpty(SensorsDataAPI.sharedInstance().getLoginId())) {
            onUserInfoChanged();
        } else {
            SALog.i(TAG, "User has login, no need change!");
        }
    }

    @Override
    public void resetAnonymousId() {
        SALog.i(TAG, "resetAnonymousId");
        onUserInfoChanged();
    }

    /**
     * 用户信息被修改的时候调用
     */
    public static void onUserInfoChanged() {
        try {
            SensorsABTestCacheManager.getInstance().clearCache();
            new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache();
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}
