/*
 * Created by luweibin on 2021/11/15.
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

import android.text.TextUtils;

import com.sensorsdata.abtest.OnABTestReceivedData;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestExperimentTaskRecorder {
    private final String mLoginId;
    private final String mAnonymousId;
    private final String mParamName;
    private final Map<String, Object> mProperties;
    private final int mTimeoutMillSeconds;
    private final Map<OnABTestReceivedData<Object>, Object> mCallbacksAndDefaultValueMap = new LinkedHashMap<>();
    private boolean mIsMergedTask = false;

    RequestExperimentTaskRecorder(String loginId, String anonymousId, String paramName, Map<String, Object> properties, int timeoutMillSeconds) {
        this.mLoginId = loginId;
        this.mAnonymousId = anonymousId;
        this.mParamName = paramName;
        this.mProperties = properties;
        this.mTimeoutMillSeconds = timeoutMillSeconds;
    }

    void putCallbackAndDefaultValue(OnABTestReceivedData<Object> onABTestReceivedData, Object defaultValue) {
        if (!this.mCallbacksAndDefaultValueMap.containsKey(onABTestReceivedData)) {
            this.mCallbacksAndDefaultValueMap.put(onABTestReceivedData, defaultValue);
        }
    }

    Map<OnABTestReceivedData<Object>, Object> getCallbacksAndDefaultValueMap() {
        return mCallbacksAndDefaultValueMap;
    }

    boolean isSameExperimentTask(String loginId, String anonymousId, String paramName, Map<String, Object> properties, int timeoutMillSeconds) {
        return TextUtils.equals(loginId, mLoginId)
                && TextUtils.equals(anonymousId, mAnonymousId)
                && (properties == null || TextUtils.equals(paramName, mParamName))
                && isSameProperties(properties)
                && timeoutMillSeconds == mTimeoutMillSeconds;
    }

    private boolean isSameProperties(Map<String, Object> properties) {
        if (properties == null && mProperties == null) {
            return true;
        } else if (properties != null && mProperties != null) {
            if (properties.size() == mProperties.size()) {
                for (Map.Entry<String, Object> entry : mProperties.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (!properties.containsKey(key) || !value.equals(properties.get(key))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    void setIsMergedTask(boolean isMergedTask) {
        this.mIsMergedTask = isMergedTask;
    }

    boolean isMergedTask() {
        return mIsMergedTask;
    }
}
