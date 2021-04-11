/*
 * Created by zhangxiangwei on 2020/09/12.
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

package com.sensorsdata.abtest.entity;

/**
 * 对外异常汇总
 */
public enum SABErrorEnum {

    // SDK 初始化异常，错误码以 0 开头
    SDK_NULL_CONTEXT("0001", "Context must be not empty!"),
    SDK_NULL_SENSORS_AB_TEST_CONFIG_OPTIONS("0002", "SensorsABTestConfigOptions can not be null"),
    SDK_NULL_BASE_URL_OF_SENSORS_AB_TEST_CONFIG_OPTIONS("0003", "A/B Testing SDK 初始化失败，请使用正确的 URL"),
    SDK_NULL_KEY_OF_SENSORS_AB_TEST_CONFIG_OPTIONS("0004", "A/B Testing SDK 初始化失败，请使用正确的 URL（必须包含 project-key)"),

    // SDK 接口传参、网络问题，错误码以 1 开头
    ASYNC_REQUEST_NULL_EXPERIMENT_PARAMETER_NAME("1001", "The experiment param name of async request is empty and return default value: %s"),
    ASYNC_REQUEST_NETWORK_UNAVAILABLE("1002", "Network is unavailable and return default value: %s"),
    ASYNC_REQUEST_TIMEOUT("1003", "AsyncRequest is timeout and return default value: %s"),
    ASYNC_REQUEST_PARAMS_TYPE_NOT_VALID("1004", "试验结果类型与代码期望类型不一致，试验参数名：%s，当前返回类型为：%s，代码期望类型为：%s");

    public String code;
    public String message;

    SABErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
