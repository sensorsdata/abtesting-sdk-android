package com.sensorsdata.abtest;

public class SensorsDataVersionConfig {
    /**
     * 当前 SDK 的版本
     */
    public static final String SDK_VERSION = "0.2.3";

    /**
     * 当前 SDK 所要依赖的版本，可以为 JSONArray
     */
    public static final String DEPENDENT_SDK_VERSIONS = "[\n" +
            "    {\n" +
            "        \"DEPENDENT_MIN_SDK_VERSIONS\":\"6.3.4\",\n" +
            "        \"SDK_VERSION_PATH\":\"com.sensorsdata.analytics.android.sdk.SensorsDataAPI\",\n" +
            "        \"ERROR_MESSAGE\":\"当前 SA SDK 版本 %s 过低，请升级至 %s 及其以上版本后进行使用\"\n" +
            "    }\n" +
            "]";
}
