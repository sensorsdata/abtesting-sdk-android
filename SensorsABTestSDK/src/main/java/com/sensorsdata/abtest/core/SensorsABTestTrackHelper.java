/*
 * Created by zhangxiangwei on 2020/09/12.
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

package com.sensorsdata.abtest.core;

import android.text.TextUtils;

import com.sensorsdata.abtest.BuildConfig;
import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class SensorsABTestTrackHelper {
    private static final String TAG = "SAB.SensorsABTestTrackHelper";
    private static volatile SensorsABTestTrackHelper mInstance;
    private JSONObject mABTestTriggerCache;
    private boolean mLibPluginVersionAdded;

    private SensorsABTestTrackHelper() {
        loadABTestTriggerCache();
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

    public void trackABTestTrigger(Experiment experiment, String distinctId, String loginId, String anonymousId, String customIDs) {
        if (experiment == null || TextUtils.isEmpty(experiment.experimentId) || TextUtils.isEmpty(experiment.experimentGroupId)) {
            SALog.i(TAG, "trackABTestTrigger param experiment is invalid");
            return;
        }

        if (TextUtils.isEmpty(distinctId)) {
            SALog.i(TAG, "trackABTestTrigger distinctId is null");
            return;

        }

        if (!isTrackABTestTrigger(distinctId + customIDs, experiment.experimentId, experiment.experimentGroupId)) {
            SALog.i(TAG, "trackABTestTrigger experimentId: " + experiment.experimentId + "，experimentGroupId：" + experiment.experimentGroupId + " has triggered and return");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$abtest_experiment_id", experiment.experimentId);
            jsonObject.put("$abtest_experiment_group_id", experiment.experimentGroupId);
            // 冷启动第一次追加 $lib_plugin_version 属性
            if (!mLibPluginVersionAdded) {
                JSONArray array = getSDKVersion();
                if (array != null) {
                    jsonObject.put("$lib_plugin_version", array);
                    mLibPluginVersionAdded = true;
                }
            }
            // 当前 id 和试验请求 id 不一致时，以试验请求 id 为准
            if (!TextUtils.equals(SensorsDataAPI.sharedInstance().getDistinctId(), distinctId)) {
                jsonObject.put("$abtest_distinct_id", distinctId);
                jsonObject.put("$abtest_login_id", loginId);
                jsonObject.put("$abtest_anonymous_id", anonymousId);
            }
            SensorsDataAPI.sharedInstance().track("$ABTestTrigger", jsonObject);
            saveABTestTrigger(distinctId + customIDs, experiment.experimentId, experiment.experimentGroupId);
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private boolean isTrackABTestTrigger(String triggerKey, String experimentId, String experimentGroupId) {
        try {
            if (mABTestTriggerCache != null && mABTestTriggerCache.has(triggerKey)) {
                SALog.i(TAG, "isTrackABTestTrigger mABTestTriggerCache is " + mABTestTriggerCache.toString());
                JSONObject obj = mABTestTriggerCache.optJSONObject(triggerKey);
                return obj == null || !obj.has(experimentId) || !TextUtils.equals(experimentGroupId, obj.optString(experimentId));
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return true;
    }

    private void loadABTestTriggerCache() {
        String abTestTrigger = StoreManagerFactory.getStoreManager().getString(AppConstants.Property.Key.ABTEST_TRIGGER, "");
        if (!TextUtils.isEmpty(abTestTrigger)) {
            try {
                SALog.i(TAG, "loadABTestTriggerCache abTestTrigger is " + abTestTrigger);
                mABTestTriggerCache = new JSONObject(abTestTrigger);
            } catch (JSONException e) {
                SALog.printStackTrace(e);
            }
        }
    }

    private void saveABTestTrigger(String triggerKey, String experimentId, String experimentGroupId) {
        if (TextUtils.isEmpty(triggerKey) || TextUtils.isEmpty(experimentId) || TextUtils.isEmpty(experimentGroupId)) {
            return;
        }
        if (mABTestTriggerCache == null) {
            mABTestTriggerCache = new JSONObject();
        }
        JSONObject obj = mABTestTriggerCache.optJSONObject(triggerKey);
        if (obj == null || !obj.has(experimentId) || !TextUtils.equals(experimentGroupId, obj.optString(experimentId))) {
            try {
                if (obj == null) {
                    obj = new JSONObject();
                }
                obj.put(experimentId, experimentGroupId);
                SALog.i(TAG, String.format("saveABTestTrigger triggerKey is %s,experimentId is %s,experimentGroupId is %s ", triggerKey, experimentId, experimentGroupId));
                mABTestTriggerCache.put(triggerKey, obj);
                StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.ABTEST_TRIGGER, mABTestTriggerCache.toString());
            } catch (JSONException e) {
                SALog.printStackTrace(e);
            }
        }
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
