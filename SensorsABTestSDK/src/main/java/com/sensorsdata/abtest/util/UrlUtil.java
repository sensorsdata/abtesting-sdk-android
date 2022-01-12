/*
 * Created by zhangxiangwei on 2020/10/16.
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

import android.net.Uri;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SALog;

public class UrlUtil {

    private static final String TAG = "SAB.UrlUtil";

    public static String getApiBaseUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String baseUrl = null;
        try {
            baseUrl = url.split("\\?")[0];
            SALog.i(TAG, "baseUrl: " + baseUrl);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return baseUrl;
    }

    public static String getProjectKey(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String key = null;
        try {
            Uri uri = Uri.parse(url);
            key = uri.getQueryParameter("project-key");
            SALog.i(TAG, "key: " + key);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return key;
    }

}
