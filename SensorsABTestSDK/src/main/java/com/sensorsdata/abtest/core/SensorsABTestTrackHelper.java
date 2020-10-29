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

package com.sensorsdata.abtest.core;

import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONException;
import org.json.JSONObject;

class SensorsABTestTrackHelper {

    public static void trackABTestTrigger(Experiment experiment) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$abtest_experiment_id", experiment.experimentId);
            jsonObject.put("$abtest_experiment_group_id", experiment.experimentGroupId);
            SensorsDataAPI.sharedInstance().track("$ABTestTrigger", jsonObject);
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        }
    }
}
