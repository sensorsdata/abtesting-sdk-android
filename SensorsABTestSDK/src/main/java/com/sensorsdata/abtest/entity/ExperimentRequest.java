/*
 * Created by zhangxiangwei on 2020/09/12.
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

package com.sensorsdata.abtest.entity;

import android.text.TextUtils;

import com.sensorsdata.abtest.BuildConfig;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ExperimentRequest {
    private static final String TAG = "SAB.RequestParams";

    public JSONObject createRequestBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            String loginId = SensorsDataAPI.sharedInstance().getLoginId();
            if (!TextUtils.isEmpty(loginId)) {
                jsonObject.put("login_id", loginId);
            }
            jsonObject.put("anonymous_id", SensorsDataAPI.sharedInstance().getAnonymousId());
            jsonObject.put("platform", "Android");
            jsonObject.put("properties", getPresetProperties());
            jsonObject.put("abtest_lib_version", BuildConfig.SDK_VERSION);

            SALog.i(TAG, "getRequestParams | request:\n" + JSONUtils.formatJson(jsonObject.toString()));
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return jsonObject;
    }

    /**
     * 从埋点 SDK 中获取预置属性
     *
     * @return 预置属性
     */
    private JSONObject getPresetProperties() {
        JSONObject jsonObject = null;
        try {
            jsonObject = SensorsDataAPI.sharedInstance().getPresetProperties();
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return jsonObject;
    }
}
