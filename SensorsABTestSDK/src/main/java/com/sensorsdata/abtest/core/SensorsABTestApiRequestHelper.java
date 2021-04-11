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


import android.content.Context;
import android.text.TextUtils;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.entity.ExperimentRequest;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.util.TaskRunner;
import com.sensorsdata.abtest.util.UrlUtil;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.network.HttpCallback;
import com.sensorsdata.analytics.android.sdk.network.HttpMethod;
import com.sensorsdata.analytics.android.sdk.network.RequestHelper;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;
import com.sensorsdata.analytics.android.sdk.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorsABTestApiRequestHelper<T> {

    private static final String TAG = "SAB.SensorsABTestApiRequestHelper";
    private boolean mHasCallback = false;

    public void requestExperimentByParamName(final String paramName, final T defaultValue, final int timeoutMillSeconds, final OnABTestReceivedData<T> callBack) {
        // callback 为 null
        if (callBack == null) {
            SALog.i(TAG, "试验 callback 不正确，试验 callback 不能为空！");
            return;
        }

        // 传参非法
        if (TextUtils.isEmpty(paramName)) {
            SALog.i(TAG, String.format("experiment param name：%s，试验参数名不正确，试验参数名必须为非空字符串！", paramName));
            if (!mHasCallback) {
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_NULL_EXPERIMENT_PARAMETER_NAME, defaultValue);
                callBack.onResult(defaultValue);
                mHasCallback = true;
            }
            return;
        }

        // 网络状态不可用
        Context context = SensorsABTest.shareInstance().getContext();
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            if (!mHasCallback) {
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_NETWORK_UNAVAILABLE, defaultValue);
                callBack.onResult(defaultValue);
                mHasCallback = true;
            }
            return;
        }

        // 启动定时器
        final TimeoutRunnable runnable = new TimeoutRunnable(callBack, defaultValue);
        TaskRunner.getBackHandler().postDelayed(runnable, timeoutMillSeconds);

        requestExperimentsAndUpdateCache(new IApiCallback<Map<String, Experiment>>() {
            @Override
            public void onSuccess(Map<String, Experiment> experimentMap) {
                try {
                    TaskRunner.getBackHandler().removeCallbacks(runnable);
                    if (experimentMap == null) {
                        if (!mHasCallback) {
                            SALog.i(TAG, "onSuccess response is empty and return default value: " + defaultValue);
                            callBack.onResult(defaultValue);
                            mHasCallback = true;
                        }
                        return;
                    }

                    Experiment experiment = experimentMap.get(paramName);
                    if (experiment == null) {
                        if (!mHasCallback) {
                            SALog.i(TAG, "onSuccess experiment is empty and return default value: " + defaultValue);
                            callBack.onResult(defaultValue);
                            mHasCallback = true;
                        }
                        return;
                    }

                    if (!experiment.checkTypeIsValid(paramName, defaultValue)) {
                        if (!mHasCallback) {
                            if (defaultValue != null) {
                                String variableType = "";
                                Experiment.Variable variable = experiment.getVariableByParamName(paramName);
                                if (variable != null) {
                                    variableType = variable.type;
                                }
                                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_PARAMS_TYPE_NOT_VALID, paramName, variableType, defaultValue.getClass().toString());
                            }
                            callBack.onResult(defaultValue);
                            mHasCallback = true;
                        }
                        return;
                    }

                    T value = experiment.getVariableValue(paramName, defaultValue);
                    if (value != null) {
                        if (!mHasCallback) {
                            SALog.i(TAG, "onSuccess return value: " + value);
                            callBack.onResult(value);
                            mHasCallback = true;
                        } else {
                            SALog.i(TAG, "mOnABTestReceivedData is null ");
                        }
                        if (!experiment.isWhiteList) {
                            SensorsABTestTrackHelper.getInstance().trackABTestTrigger(experiment);
                        }
                    }
                } catch (Exception e) {
                    if (!mHasCallback) {
                        SALog.i(TAG, "onSuccess Exception and return default value: " + defaultValue);
                        callBack.onResult(defaultValue);
                        mHasCallback = true;
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String message) {
                TaskRunner.getBackHandler().removeCallbacks(runnable);
                if (!mHasCallback) {
                    SALog.i(TAG, "onFailure and return default value: " + defaultValue);
                    callBack.onResult(defaultValue);
                    mHasCallback = true;
                }
            }
        });
    }

    void requestExperiments(final IApiCallback<String> callBack) {
        String url = null, key = null;
        SensorsABTestConfigOptions configOptions = SensorsABTest.shareInstance().getConfigOptions();
        if (configOptions != null) {
            String serverUrl = configOptions.getUrl();
            url = UrlUtil.getApiBaseUrl(serverUrl);
            key = UrlUtil.getProjectKey(serverUrl);
        }
        if (TextUtils.isEmpty(url)) {
            SALog.i(TAG, "url is empty and request cancel");
            return;
        }
        if (TextUtils.isEmpty(key)) {
            SALog.i(TAG, "key is empty and request cancel");
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("project-key", key);
        new RequestHelper.Builder(HttpMethod.POST, url)
                .header(headers)
                .jsonData(new ExperimentRequest().createRequestBody().toString())
                .callback(new HttpCallback.StringCallback() {
                    @Override
                    public void onFailure(final int code, final String errorMessage) {
                        if (callBack != null) {
                            callBack.onFailure(code, errorMessage);
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        if (callBack != null) {
                            callBack.onSuccess(response);
                        }
                    }

                    @Override
                    public void onAfter() {

                    }
                }).execute();
    }

    public void requestExperimentsAndUpdateCache() {
        requestExperimentsAndUpdateCache(null);
    }

    void requestExperimentsAndUpdateCache(final IApiCallback<Map<String, Experiment>> callBack) {
        requestExperiments(new IApiCallback<String>() {
            @Override
            public void onSuccess(String s) {
                try {
                    SALog.i(TAG, String.format("试验返回：response：%s", s));
                    ConcurrentHashMap<String, Experiment> hashMap = null;
                    JSONObject response = new JSONObject(s);
                    String status = response.optString("status");
                    if (TextUtils.equals(AppConstants.AB_TEST_SUCCESS, status)) {
                        SALog.i(TAG, String.format("获取试验成功：results：%s", JSONUtils.formatJson(response.toString())));
                        JSONArray array = response.optJSONArray("results");
                        hashMap = SensorsABTestCacheManager.getInstance().loadExperimentsFromCache(array != null ? array.toString() : "");
                    } else if (TextUtils.equals(AppConstants.AB_TEST_FAILURE, status)) {
                        SALog.i(TAG, String.format("获取试验失败：error_type：%s，error：%s", response.optString("error_type"), response.optString("error")));
                    }
                    if (callBack != null) {
                        callBack.onSuccess(hashMap);
                    }
                } catch (Exception e) {
                    SALog.i(TAG, String.format("试验数据解析失败，response ：%s！", s));
                    if (callBack != null) {
                        callBack.onSuccess(null);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String message) {
                SALog.i(TAG, "onFailure error_code: " + errorCode + ",message: " + message);
                if (callBack != null) {
                    callBack.onFailure(errorCode, message);
                }
            }
        });
    }

    private class TimeoutRunnable implements Runnable {

        private T defaultValue;
        private OnABTestReceivedData<T> onABTestReceivedData;

        TimeoutRunnable(OnABTestReceivedData<T> onABTestReceivedData, T defaultValue) {
            this.onABTestReceivedData = onABTestReceivedData;
            this.defaultValue = defaultValue;
        }

        @Override
        public void run() {
            if (onABTestReceivedData != null && !mHasCallback) {
                SALog.i(TAG, "timeout return value: " + defaultValue);
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_TIMEOUT, defaultValue);
                onABTestReceivedData.onResult(defaultValue);
                mHasCallback = true;
            }
        }
    }
}
