/*
 * Created by zhangxiangwei on 2020/09/09.
 * Copyright 2015－2020 Sensors Data Inc.
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

package com.sensorsdata.abtest;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sensorsdata.abtest.core.SensorsABTestHelper;
import com.sensorsdata.abtest.core.SABErrorDispatcher;
import com.sensorsdata.abtest.core.SensorsABTestApiRequestHelper;
import com.sensorsdata.abtest.core.SensorsABTestCacheManager;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.util.AppInfoUtils;
import com.sensorsdata.abtest.util.UrlUtil;
import com.sensorsdata.analytics.android.sdk.SALog;

public class SensorsABTest implements ISensorsABTestApi {

    private static final String TAG = "SAB.SensorsABTest";
    private static SensorsABTest sInstance;
    // 默认请求超时时间
    private static final int TIMEOUT_REQUEST = 30 * 1000;
    private SensorsABTestConfigOptions mConfigOptions;
    private Context mContext;

    private SensorsABTest(Context context, SensorsABTestConfigOptions configOptions) {
        try {
            this.mContext = context.getApplicationContext();
            this.mConfigOptions = configOptions;
        } catch (Exception ex) {
            SALog.printStackTrace(ex);
        }
    }

    SensorsABTest() {
    }

    public SensorsABTestConfigOptions getConfigOptions() {
        return mConfigOptions;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 获取 SensorsABTestApi 实例
     *
     * @return SensorsABTestApi 实例
     */
    public static SensorsABTest shareInstance() {
        if (sInstance == null) {
            Log.i(TAG, "startWithConfigOptions(Context context, SensorsABTestConfigOptions configOptions) 接口调用失败，A/B Testing SDK 未初始化");
            return new SensorsABTestEmptyImplementation();
        }
        return sInstance;
    }

    /**
     * 初始化 A/B Testing SDK
     *
     * @param context 上下文
     * @param configOptions SDK 的配置项
     */
    public synchronized static void startWithConfigOptions(Context context, SensorsABTestConfigOptions configOptions) {
        if (sInstance != null) {
            SALog.i(TAG, "A/B Testing SDK 重复初始化！只有第一次初始化有效！");
            return;
        }

        if (context == null) {
            SABErrorDispatcher.dispatchSABException(SABErrorEnum.SDK_NULL_CONTEXT);
            return;
        }
        if (configOptions == null) {
            SABErrorDispatcher.dispatchSABException(SABErrorEnum.SDK_NULL_SENSORS_AB_TEST_CONFIG_OPTIONS);
            return;
        }

        String serverUrl = configOptions.getUrl();

        String baseUrl = UrlUtil.getApiBaseUrl(serverUrl);
        if (TextUtils.isEmpty(baseUrl) || baseUrl.trim().equals("")) {
            SABErrorDispatcher.dispatchSABException(SABErrorEnum.SDK_NULL_BASE_URL_OF_SENSORS_AB_TEST_CONFIG_OPTIONS);
            return;
        }

        String key = UrlUtil.getProjectKey(serverUrl);
        if (TextUtils.isEmpty(key) || key.trim().equals("")) {
            SABErrorDispatcher.dispatchSABException(SABErrorEnum.SDK_NULL_KEY_OF_SENSORS_AB_TEST_CONFIG_OPTIONS);
            return;
        }

        if (!AppInfoUtils.checkSASDKVersionIsValid()) {
            return;
        }

        try {
            SALog.i(TAG, String.format("A/B Testing SDK 初始化成功，试验 URL：%s", configOptions.getUrl()));
            sInstance = new SensorsABTest(context.getApplicationContext(), configOptions);
            new SensorsABTestHelper().init(context.getApplicationContext());
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public <T> T fetchCacheABTest(String paramName, T defaultValue) {
        try {
            SALog.i(TAG, "fetchCacheABTest param name: " + paramName + ",default value: " + defaultValue);
            T t = SensorsABTestCacheManager.getInstance().getExperimentVariableValue(paramName, defaultValue);
            return t != null ? t : defaultValue;
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return defaultValue;
    }

    @Override
    public <T> void asyncFetchABTest(String paramName, T defaultValue, OnABTestReceivedData<T> callBack) {
        try {
            asyncFetchABTest(paramName, defaultValue, TIMEOUT_REQUEST, callBack);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public <T> void asyncFetchABTest(String paramName, T defaultValue, int timeoutMillSeconds, OnABTestReceivedData<T> callBack) {
        try {
            if (timeoutMillSeconds > 0) {
                SALog.i(TAG, "timeoutMillSeconds minimum value is 1000ms");
                timeoutMillSeconds = Math.max(1000, timeoutMillSeconds);
            } else {
                SALog.i(TAG, "timeoutMillSeconds params is not valid: <= 0 and set default value: " + TIMEOUT_REQUEST);
                timeoutMillSeconds = TIMEOUT_REQUEST;
            }
            SALog.i(TAG, "asyncFetchABTest request param name: " + paramName + ",default value: " + defaultValue + ",timeoutMillSeconds: " + timeoutMillSeconds);
            new SensorsABTestApiRequestHelper<T>().requestExperimentByParamName(paramName, defaultValue, timeoutMillSeconds, callBack);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public <T> void fastFetchABTest(String paramName, T defaultValue, OnABTestReceivedData<T> callBack) {
        try {
            fastFetchABTest(paramName, defaultValue, TIMEOUT_REQUEST, callBack);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public <T> void fastFetchABTest(String paramName, T defaultValue, int timeoutMillSeconds, OnABTestReceivedData<T> callBack) {
        try {
            T t = SensorsABTestCacheManager.getInstance().getExperimentVariableValue(paramName, defaultValue);
            if (t != null && callBack != null) {
                callBack.onResult(t);
            } else {
                asyncFetchABTest(paramName, defaultValue, timeoutMillSeconds, callBack);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}
