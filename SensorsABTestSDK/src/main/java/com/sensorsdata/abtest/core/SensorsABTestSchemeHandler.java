/*
 * Created by zhangxiangwei on 2020/09/18.
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

import android.net.Uri;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.network.HttpCallback;
import com.sensorsdata.analytics.android.sdk.network.HttpMethod;
import com.sensorsdata.analytics.android.sdk.network.RequestHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 主 SDK 主动反射调用
 */
public class SensorsABTestSchemeHandler {
    private static final String TAG = "SAB.SensorsABTesSchemeHandler";

    public static void handleSchemeUrl(String uriString) {
        try {
            SALog.i(TAG, "handleSchemeUrl receive uri : " + uriString);
            if (TextUtils.isEmpty(uriString)) {
                SALog.i(TAG, "uriString is empty and return");
                return;
            }

            Uri uri = Uri.parse(uriString);
            String postUrl = uri.getQueryParameter("sensors_abtest_url");
            if (TextUtils.isEmpty(postUrl)) {
                SALog.i(TAG, "postUrl is empty and return");
                return;
            }

            String featureCode = uri.getQueryParameter("feature_code");
            if (TextUtils.isEmpty(featureCode)) {
                SALog.i(TAG, "featureCode is empty and return");
                return;
            }

            String accountIdString = uri.getQueryParameter("account_id");
            if (TextUtils.isEmpty(accountIdString)) {
                SALog.i(TAG, "accountId is empty and return");
                return;
            }
            int accountId;
            try {
                accountId = Integer.parseInt(accountIdString);
            } catch (NumberFormatException e) {
                SALog.printStackTrace(e);
                return;
            }

            String distinctId = SensorsDataAPI.sharedInstance().getDistinctId();
            if (TextUtils.isEmpty(distinctId)) {
                SALog.i(TAG, "distinctId is empty and return");
                return;
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("accept", "application/json");
            try {
                JSONObject body = new JSONObject();
                body.put("distinct_id", distinctId);
                body.put("feature_code", featureCode);
                body.put("account_id", accountId);
                new RequestHelper.Builder(HttpMethod.POST, postUrl)
                        .header(headers)
                        .jsonData(body.toString())
                        .callback(new HttpCallback.StringCallback() {
                            @Override
                            public void onFailure(final int code, final String errorMessage) {
                                SALog.i(TAG, "code: " + code + ",message: " + errorMessage);
                            }

                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    SALog.i(TAG, response);
                                }
                            }

                            @Override
                            public void onAfter() {

                            }
                        }).execute();
            } catch (JSONException e) {
                SALog.printStackTrace(e);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }
}
