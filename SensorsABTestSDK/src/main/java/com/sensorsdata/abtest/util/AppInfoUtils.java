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

package com.sensorsdata.abtest.util;


import android.text.TextUtils;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPIEmptyImplementation;

import java.lang.reflect.Field;

public class AppInfoUtils {
    private static final String TAG = "AppInfoUtils";
    public static final String MIN_SA_SDK_VERSION = "4.3.6";
    public static final String MIN_SA_SECRET_SDK_VERSION = "6.2.0";

    public static boolean checkSASDKVersionIsValid() {
        return checkSASDKVersionIsValid(MIN_SA_SDK_VERSION);
    }

    public static boolean checkSASecretVersionIsValid() {
        return checkSASDKVersionIsValid(MIN_SA_SECRET_SDK_VERSION);
    }

    private static boolean checkSASDKVersionIsValid(String saVersion) {
        SensorsDataAPI sensorsDataAPI = SensorsDataAPI.sharedInstance();
        if (sensorsDataAPI instanceof SensorsDataAPIEmptyImplementation) {
            Log.e(TAG, "神策 Android 埋点 SDK 未集成或未初始化");
            return false;
        }
        Field field = null;
        try {
            field = sensorsDataAPI.getClass().getDeclaredField("VERSION");
            field.setAccessible(true);
            String version = (String) field.get(sensorsDataAPI);
            String compareVersion = version;
            if (!TextUtils.isEmpty(version)) {
                if (version.contains("-")) {
                    compareVersion = compareVersion.substring(0, compareVersion.indexOf("-"));
                }
                if (!AppInfoUtils.isVersionValid(compareVersion, saVersion)) {
                    if (saVersion.equals(MIN_SA_SDK_VERSION)) {
                        Log.e(TAG, String.format("当前神策 Android 埋点 SDK 版本 %s 过低，请升级至 %s 及其以上版本后进行使用", version, saVersion));
                    } else if (saVersion.equals(MIN_SA_SECRET_SDK_VERSION)) {
                        Log.e(TAG, String.format("Android A/B Testing 合规方案无法使用！当前埋点 SDK 版本为 %s，升级至 %s 后合规方案才能生效！", version, saVersion));
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
            return false;
        }
        return true;
    }

    public static boolean isVersionValid(String saVersion, String requiredVersion) {
        try {
            if (saVersion.equals(requiredVersion)) {
                return true;
            } else {
                String[] saVersions = saVersion.split("\\.");
                String[] requiredVersions = requiredVersion.split("\\.");
                for (int index = 0; index < requiredVersions.length; index++) {
                    int tagValue = Integer.parseInt(saVersions[index]);
                    int desValue = Integer.parseInt(requiredVersions[index]);
                    if (tagValue > desValue) {
                        return true;
                    } else if (tagValue < desValue) {
                        return false;
                    }
                }
                return false;
            }
        } catch (Exception ex) {
            // ignore
            return false;
        }
    }
}
