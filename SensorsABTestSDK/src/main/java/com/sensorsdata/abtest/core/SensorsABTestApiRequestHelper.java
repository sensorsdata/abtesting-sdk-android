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
import android.os.Looper;
import android.text.TextUtils;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.entity.ExperimentRequest;
import com.sensorsdata.abtest.entity.RequestingExperimentInfo;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.exception.DataInvalidException;
import com.sensorsdata.abtest.util.SensorsDataHelper;
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorsABTestApiRequestHelper<T> {

    private static final String TAG = "SAB.SensorsABTestApiRequestHelper";
    private boolean mHasCallback = false;
    private String mDistinctId;

    public void requestExperimentByParamName(final String distinctId, final String loginId, final String anonymousId,
                                             final String paramName, final T defaultValue, Map<String, Object> properties,
                                             final int timeoutMillSeconds, final OnABTestReceivedData<T> callBack, boolean mergeRequest) {
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
                mHasCallback = true;
                doCallbackOnMainThread(callBack, defaultValue);
            }
            return;
        }

        // 网络状态不可用
        Context context = SensorsABTest.shareInstance().getContext();
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            if (!mHasCallback) {
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_NETWORK_UNAVAILABLE, defaultValue);
                mHasCallback = true;
                doCallbackOnMainThread(callBack, defaultValue);
            }
            return;
        }

        // 自定义参数校验
        Map<String, String> propertiesString = null;
        if (properties != null && properties.size() > 0) {
            try {
                propertiesString = SensorsDataHelper.checkPropertiesAndToString(properties);
            } catch (DataInvalidException e) {
                if (!mHasCallback) {
                    SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_PROPERTIES_NOT_VALID, e.getMessage());
                    mHasCallback = true;
                    doCallbackOnMainThread(callBack, defaultValue);
                }
                return;
            }
        }

        final RequestExperimentTaskRecorder currentTask;
        // step1.根据是否需要合并请求来创建请求
        if (mergeRequest) {
            // step2.如果是需要合并的需求，判断当前试验是否是不确定状态的试验，若不是则说明缓存中已存在
            if (!SensorsABTestCacheManager.getInstance().isFuzzyExperiments(paramName)) {
                if (!mHasCallback) {
                    SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_EXPERIMENT_NOT_IN_FUZZY, defaultValue);
                    mHasCallback = true;
                    doCallbackOnMainThread(callBack, defaultValue);
                }
                return;
            }
            // step3.如果是需要拉取请求的试验，检查当前请求是否已经正在进行，如果正在进行则合并请求并结束，如果没有则进行请求
            currentTask = RequestExperimentTaskRecorderManager.getInstance().mergeRequest(loginId, anonymousId, paramName, properties, timeoutMillSeconds, callBack, defaultValue);
        } else {
            // step2.如果不是需要合并的请求，直接创建任务
            currentTask = RequestExperimentTaskRecorderManager.getInstance().createRequest(loginId, anonymousId, paramName, properties, timeoutMillSeconds, callBack, defaultValue);
        }
        if (currentTask.isMergedTask()) {
            return;
        }

        // 启动定时器
        final TimeoutRunnable runnable = new TimeoutRunnable(currentTask);
        TaskRunner.getBackHandler().postDelayed(runnable, timeoutMillSeconds);

        mDistinctId = distinctId;
        requestExperimentsAndUpdateCache(propertiesString, paramName, new IApiCallback<Map<String, Experiment>>() {
            @Override
            public void onSuccess(Map<String, Experiment> experimentMap) {
                RequestExperimentTaskRecorderManager.getInstance().removeTask(currentTask);
                List<RequestingExperimentInfo> taskExperimentInfoList = currentTask.getRequestingExperimentList();
                TaskRunner.getBackHandler().removeCallbacks(runnable);
                if (mHasCallback) {
                    SALog.i(TAG, "Request success! but all callbacks has been returned with default value!");
                    return;
                }
                SALog.i(TAG, "Task merged request size is " + taskExperimentInfoList.size());
                for (RequestingExperimentInfo item : taskExperimentInfoList) {
                    OnABTestReceivedData<?> onABTestReceivedData = item.getResultCallBack();
                    String itemParamName = item.getParamName();
                    Object itemDefaultValue = item.getDefaultValue();
                    try {
                        if (experimentMap == null) {
                            SALog.i(TAG, "onSuccess response is empty and return default value: " + itemDefaultValue);
                            doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                            continue;
                        }
                        Experiment experiment = experimentMap.get(itemParamName);
                        if (experiment == null) {
                            SALog.i(TAG, "onSuccess experiment is empty and return default value: " + itemDefaultValue);
                            doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                            continue;
                        }
                        if (!experiment.checkTypeIsValid(itemParamName, itemDefaultValue)) {
                            if (itemDefaultValue != null) {
                                String variableType = "";
                                Experiment.Variable variable = experiment.getVariableByParamName(itemParamName);
                                if (variable != null) {
                                    variableType = variable.type;
                                }
                                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_PARAMS_TYPE_NOT_VALID, itemParamName, variableType, itemDefaultValue.getClass().toString());
                            }
                            doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                            continue;
                        }
                        Object value = experiment.getVariableValue(itemParamName, itemDefaultValue);
                        if (value != null) {
                            SALog.i(TAG, "onSuccess return value: " + value);
                            doCallbackOnMainThread(onABTestReceivedData, value);

                            if (!experiment.isWhiteList) {
                                SensorsABTestTrackHelper.getInstance().trackABTestTrigger(experiment, distinctId, loginId, anonymousId);
                            }
                        }
                    } catch (Exception e) {
                        SALog.i(TAG, "onSuccess Exception and return default value: " + itemDefaultValue);
                        doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                    }
                }
                mHasCallback = true;
            }

            @Override
            public void onFailure(int errorCode, String message) {
                TaskRunner.getBackHandler().removeCallbacks(runnable);
                RequestExperimentTaskRecorderManager.getInstance().removeTask(currentTask);
                if (!mHasCallback) {
                    List<RequestingExperimentInfo> taskExperimentInfoList = currentTask.getRequestingExperimentList();
                    SALog.i(TAG, "Task merged request size is " + taskExperimentInfoList.size());
                    for (RequestingExperimentInfo item : taskExperimentInfoList) {
                        OnABTestReceivedData<?> onABTestReceivedData = item.getResultCallBack();
                        Object itemDefaultValue = item.getDefaultValue();
                        doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                        SALog.i(TAG, "onFailure and return default value: " + itemDefaultValue);
                    }
                    mHasCallback = true;
                }
            }
        });
    }

    void requestExperiments(Map<String, String> properties, String paramName, final IApiCallback<String> callBack) {
        requestExperiments(properties, paramName, null, callBack);
    }

    void requestExperiments(Map<String, String> properties, String paramName, JSONObject object, final IApiCallback<String> callBack) {
        String url = null, key = null, requestBody = null;
        SensorsABTestConfigOptions configOptions = SensorsABTest.shareInstance().getConfigOptions();
        if (configOptions != null) {
            String serverUrl = configOptions.getUrl();
            url = UrlUtil.getApiBaseUrl(serverUrl);
            key = UrlUtil.getProjectKey(serverUrl);
        }
        requestBody = new ExperimentRequest(properties, paramName, object).createRequestBody().toString();
        if (TextUtils.isEmpty(url)) {
            SALog.i(TAG, "url is empty and request cancel");
            return;
        }
        if (TextUtils.isEmpty(key)) {
            SALog.i(TAG, "key is empty and request cancel");
            return;
        }
        if (TextUtils.isEmpty(requestBody)) {
            SALog.i(TAG, "request body is empty and request cancel");
            return;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("project-key", key);
        new RequestHelper.Builder(HttpMethod.POST, url)
                .header(headers)
                .jsonData(requestBody)
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
        requestExperimentsAndUpdateCache(null, null, null);
    }

    void requestExperimentsAndUpdateCache(Map<String, String> properties, String paramName, final IApiCallback<Map<String, Experiment>> callBack) {
        requestExperiments(properties, paramName, new IApiCallback<String>() {
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
                        JSONObject object = null;
                        if (array != null) {
                            object = new JSONObject();
                            object.put("experiments", array);
                            object.put("distinct_id", mDistinctId);
                        }
                        hashMap = SensorsABTestCacheManager.getInstance().loadExperimentsFromCache(object != null ? object.toString() : "");
                        SensorsABTestCacheManager.getInstance().saveFuzzyExperiments(response.optJSONArray("fuzzy_experiments"));
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

    private void doCallbackOnMainThread(final OnABTestReceivedData<?> callback, final Object value) {
        try {
            final Method method = callback.getClass().getMethod("onResult", Object.class);
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                method.invoke(callback, value);
            } else {
                TaskRunner.getUiThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(callback, value);
                        } catch (Exception e) {
                            SALog.printStackTrace(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private class TimeoutRunnable implements Runnable {

        private final RequestExperimentTaskRecorder currentTask;

        TimeoutRunnable(RequestExperimentTaskRecorder currentTask) {
            this.currentTask = currentTask;
        }

        @Override
        public void run() {
            RequestExperimentTaskRecorderManager.getInstance().removeTask(currentTask);
            if (currentTask != null && !mHasCallback) {
                List<RequestingExperimentInfo> taskExperimentInfoList = currentTask.getRequestingExperimentList();
                SALog.i(TAG, "Task merged request size is " + taskExperimentInfoList.size());
                for (RequestingExperimentInfo item : taskExperimentInfoList) {
                    OnABTestReceivedData<?> onABTestReceivedData = item.getResultCallBack();
                    Object itemDefaultValue = item.getDefaultValue();
                    doCallbackOnMainThread(onABTestReceivedData, itemDefaultValue);
                    SALog.i(TAG, "timeout return value: " + itemDefaultValue);
                    SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_TIMEOUT, itemDefaultValue);
                }
                mHasCallback = true;
            }
        }
    }
}
