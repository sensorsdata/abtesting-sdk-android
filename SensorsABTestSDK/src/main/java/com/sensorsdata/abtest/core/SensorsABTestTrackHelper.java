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

package com.sensorsdata.abtest.core;

import android.text.TextUtils;

import com.sensorsdata.abtest.BuildConfig;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

class SensorsABTestTrackHelper {
    private static final String TAG = "SAB.SensorsABTestTrackHelper";
    private static volatile SensorsABTestTrackHelper mInstance;
    private HashMap<String, HashSet<String>> mABTestTriggerEventHashMap = null;

    private SensorsABTestTrackHelper() {
    }

    public static SensorsABTestTrackHelper getInstance() {
        if (mInstance == null) {
            synchronized (SensorsABTestTrackHelper.class) {
                if (mInstance == null) {
                    mInstance = new SensorsABTestTrackHelper();
                }
            }
        }
        return mInstance;
    }

    public void trackABTestTrigger(Experiment experiment) {
        if (experiment == null || TextUtils.isEmpty(experiment.experimentId)) {
            SALog.i(TAG, "trackABTestTrigger param experiment is invalid");
            return;
        }

        String distinctId = SensorsDataAPI.sharedInstance().getDistinctId();
        if (TextUtils.isEmpty(distinctId)) {
            SALog.i(TAG, "trackABTestTrigger distinctId is null");
            return;
        }

        if (!isTrackABTestTrigger(experiment.experimentId, distinctId)) {
            SALog.i(TAG, "trackABTestTrigger experimentId: " + experiment.experimentId + " has triggered and return");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$abtest_experiment_id", experiment.experimentId);
            jsonObject.put("$abtest_experiment_group_id", experiment.experimentGroupId);
            if(mABTestTriggerEventHashMap == null){
                JSONArray array = getSDKVersion();
                if(array != null){
                    jsonObject.put("$lib_plugin_version", array);
                }
            }
            SensorsDataAPI.sharedInstance().track("$ABTestTrigger", jsonObject);
            addExperimentId2HashMap(experiment.experimentId, distinctId);
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private boolean isTrackABTestTrigger(String experimentId, String distinctId) {
        try {
            if (mABTestTriggerEventHashMap != null && mABTestTriggerEventHashMap.containsKey(distinctId)) {
                HashSet<String> stringHashSet = mABTestTriggerEventHashMap.get(distinctId);
                return stringHashSet == null || !stringHashSet.contains(experimentId);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return true;
    }

    private void addExperimentId2HashMap(String experimentId, String distinctId) {
        if (mABTestTriggerEventHashMap == null) {
            mABTestTriggerEventHashMap = new HashMap<>();
        }
        SALog.i(TAG, "addExperimentId2HashMap mABTestTriggerEventHashMap old: " + mABTestTriggerEventHashMap.toString());
        HashSet<String> hashSet = mABTestTriggerEventHashMap.get(distinctId);
        if (hashSet == null) {
            hashSet = new HashSet<>();
        }
        hashSet.add(experimentId);
        mABTestTriggerEventHashMap.put(distinctId, hashSet);
        SALog.i(TAG, "addExperimentId2HashMap mABTestTriggerEventHashMap last: " + mABTestTriggerEventHashMap.toString());
    }

    private JSONArray getSDKVersion() {
        try {
            if (!TextUtils.isEmpty(BuildConfig.SDK_VERSION)) {
                SALog.i(TAG, "android plugin version: " + BuildConfig.SDK_VERSION);
                JSONArray libPluginVersion = new JSONArray();
                libPluginVersion.put("android_abtesting:" + BuildConfig.SDK_VERSION);
                return libPluginVersion;
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return null;
    }
}
