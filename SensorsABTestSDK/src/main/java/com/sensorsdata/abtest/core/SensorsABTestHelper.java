/*
 * Created by zhangxiangwei on 2020/09/09.
 * Copyright 2015－2021 Sensors Data Inc.
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
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;

import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.util.AlarmManagerUtils;
import com.sensorsdata.abtest.util.SPUtils;
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
    private Context mContext;
    private CountDownTimer mCountDownTimer;

    public void init(Context context) {
        this.mContext = context;
        SPUtils.getInstance().init(context);
        SensorsABTestCacheManager.getInstance().loadExperimentsFromDiskCache();
        SensorsDataAPI.sharedInstance().addSAJSListener(this);
        SensorsDataAPI.sharedInstance().addEventListener(this);
        requestExperimentsAndUpdateCacheWithRetry();
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
        AlarmManagerUtils.getInstance(mContext).setUpAlarm();
    }

    @Override
    public void onEnterBackground() {
        SALog.i("AppStartupManager", "onEnterBackground");
        AlarmManagerUtils.getInstance(mContext).cancelAlarm();
    }

    private void requestExperimentsAndUpdateCacheWithRetry() {
        TaskRunner.getBackHandler().post(new Runnable() {
            @Override
            public void run() {
                cancelTimer();
                if (mCountDownTimer == null) {
                    mCountDownTimer = new CountDownTimer(120 * 1000, 30 * 1000) {
                        @Override
                        public void onTick(long l) {
                            new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache(null, null, new IApiCallback<Map<String, Experiment>>() {
                                @Override
                                public void onSuccess(Map<String, Experiment> stringExperimentMap) {
                                    cancelTimer();
                                }

                                @Override
                                public void onFailure(int errorCode, String message) {

                                }
                            });
                        }

                        @Override
                        public void onFinish() {
                        }
                    };
                }
                mCountDownTimer.start();
            }
        });
    }

    private void cancelTimer() {
        try {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        } finally {
            mCountDownTimer = null;
        }
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
        onDistinctIdChanged();
    }

    @Override
    public void logout() {
        SALog.i(TAG, "logout");
        onDistinctIdChanged();
    }

    @Override
    public void identify() {
        SALog.i(TAG, "identify");
        onDistinctIdChanged();
    }

    @Override
    public void resetAnonymousId() {
        SALog.i(TAG, "resetAnonymousId");
        onDistinctIdChanged();
    }

    private void onDistinctIdChanged() {
        try {
            SensorsABTestCacheManager.getInstance().clearCache();
            new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache();
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}
