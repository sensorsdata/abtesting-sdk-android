/*
 * Created by luweibin on 2021/12/28.
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

package com.sensorsdata.abtest.entity;

import com.sensorsdata.abtest.OnABTestReceivedData;

/**
 * 用于保存正在请求中试验的信息，用于试验请求结束过后的回调
 */
public class RequestingExperimentInfo {
    private final OnABTestReceivedData<?> onABTestReceivedData;
    private final String paramName;
    private final Object defaultValue;

    public RequestingExperimentInfo(OnABTestReceivedData<?> onABTestReceivedData, String paramName, Object defaultValue) {
        this.onABTestReceivedData = onABTestReceivedData;
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    public OnABTestReceivedData<?> getResultCallBack() {
        return onABTestReceivedData;
    }

    public String getParamName() {
        return paramName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
