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
import com.sensorsdata.abtest.entity.TrackConfig;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


class SensorsABTestTrackHelper {
    private static final String TAG = "SAB.SensorsABTestTrackHelper";
    private static volatile SensorsABTestTrackHelper mInstance;
    private JSONObject mABTestTriggerCache;
    private boolean mLibPluginVersionAdded;
    private Boolean isDynamicConfigVersion;

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

        if (!isTrackABTestTrigger(distinctId + customIDs, experiment)) {
            SALog.i(TAG, "trackABTestTrigger experiment has triggered and return, the experiment is: " + experiment);
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
            saveABTestTrigger(distinctId + customIDs, experiment);
            if (!SensorsABTestTrackConfigManager.getInstance().getTrackConfig().triggerSwitch) {
                SALog.i(TAG, "trigger switch is disabled，$ABTestTrigger will not triggered.");
                return;
            }
            addTriggerContent(jsonObject, experiment);
            SensorsDataAPI.sharedInstance().track("$ABTestTrigger", jsonObject);
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 添加 trigger_switch 开关对应的扩展属性
     *
     * @param eventJson  $ABTestTrigger 事件对应的 json 信息
     * @param experiment 试验
     */
    private void addTriggerContent(JSONObject eventJson, Experiment experiment) {
        TrackConfig config = SensorsABTestTrackConfigManager.getInstance().getTrackConfig();
        if ((!config.triggerSwitch && !config.itemSwitch)
                || config.triggerContentExtension == null
                || config.triggerContentExtension.isEmpty()
                || TextUtils.isEmpty(experiment.originalJsonStr)
                || TextUtils.isEmpty(experiment.subjectId)) {
            return;
        }
        try {
            JSONObject experimentJson = new JSONObject(experiment.originalJsonStr);
            for (String extItem : config.triggerContentExtension) {
                String extResult = experimentJson.optString(extItem);
                if (!TextUtils.isEmpty(extResult)) {
                    eventJson.put("$" + extItem, extResult);
                }
            }
            eventJson.put("$abtest_experiment_result_id", experiment.experimentResultId);
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 判断缓存的试验是否命中，新版本的流程并且删除不匹配的试验
     *
     * @param triggerKey distinctid + customids
     * @param experiment 试验
     * @return true 表示已命中，false 表示未命中
     */
    private boolean isTrackABTestTrigger(String triggerKey, Experiment experiment) {
        try {
            if (mABTestTriggerCache != null) {
                if (isDynamicConfigVersion(experiment)) {
                    return isTrackABTestTriggerByDynamicConfig(experiment);
                } else if (mABTestTriggerCache.has(triggerKey)) {
                    SALog.i(TAG, "isTrackABTestTrigger mABTestTriggerCache is " + mABTestTriggerCache.toString());
                    JSONObject obj = mABTestTriggerCache.optJSONObject(triggerKey);
                    return obj == null
                            || !obj.has(experiment.experimentId)
                            || !TextUtils.equals(experiment.experimentGroupId, obj.optString(experiment.experimentId));
                }
            }

        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return true;
    }

    /**
     * 判断新版本的动态配置格式缓存的试验是否命中，并且删除不匹配的试验
     *
     * @param experiment 试验
     * @return true 没有命中，false 命中
     */
    private boolean isTrackABTestTriggerByDynamicConfig(Experiment experiment) {
        JSONArray subjectIdExperimentArray = mABTestTriggerCache.optJSONArray("subjectIdExperiment");
        if (subjectIdExperimentArray == null || subjectIdExperimentArray.length() == 0) {
            return true;
        }
        for (int i = 0; i < subjectIdExperimentArray.length(); i++) {
            JSONObject subjectObj = subjectIdExperimentArray.optJSONObject(i);
            if (subjectObj != null) {
                String subjectId = subjectObj.optString("subject_id");
                String subjectName = subjectObj.optString("subject_name");
                if (TextUtils.equals(experiment.subjectId, subjectId) && TextUtils.equals(experiment.subjectName, subjectName)) {
                    JSONObject resultsObj = subjectObj.optJSONObject("results");
                    if (resultsObj != null) {
                        Iterator<String> experimentKeys = resultsObj.keys();
                        List<String> shouldRemovedExperimentList = new ArrayList<>();
                        while (experimentKeys.hasNext()) {
                            String experimentId = experimentKeys.next();
                            if (TextUtils.equals(experimentId, experiment.experimentId)) {
                                JSONObject experimentObj = resultsObj.optJSONObject(experimentId);
                                //最新版本不需要判断试验组 id，值需要判断 unique id 即可。老版本则还是需要判断
                                if (experimentObj != null/*
                                        && TextUtils.equals(experiment.experimentGroupId, experimentObj.optString("abtest_experiment_group_id"))*/
                                        && TextUtils.equals(experiment.experimentResultId, experimentObj.optString("abtest_experiment_result_id"))) {
                                    return false;
                                } else {
                                    shouldRemovedExperimentList.add(experimentId);
                                }
                            }
                        }
                        //对于试验 id 相同，但是 uniqueId 和 groupId 不同的试验，需要从缓存中删除
                        for (String id : shouldRemovedExperimentList) {
                            resultsObj.remove(id);
                        }
                    }
                }
            }
        }
        return true;
    }

    public Set<String> getCachedABTestUniqueIdSet() {
        Set<String> resultSet = new HashSet<>();
        if (mABTestTriggerCache == null) {
            return resultSet;
        }
        JSONArray subjectIdExperimentArray = mABTestTriggerCache.optJSONArray("subjectIdExperiment");
        if (subjectIdExperimentArray == null || subjectIdExperimentArray.length() == 0) {
            return resultSet;
        }
        for (int i = 0; i < subjectIdExperimentArray.length(); i++) {
            JSONObject subjectObj = subjectIdExperimentArray.optJSONObject(i);
            if (subjectObj != null) {
                String subjectId = subjectObj.optString("subject_id", "");
                String subjectName = subjectObj.optString("subject_name", "");
                if (!checkSubjectInfoEqual(subjectId, subjectName)) {
                    continue;
                }
                JSONObject resultsObj = subjectObj.optJSONObject("results");
                if (resultsObj != null) {
                    Iterator<String> experimentKeys = resultsObj.keys();
                    while (experimentKeys.hasNext()) {
                        JSONObject itemObj = resultsObj.optJSONObject(experimentKeys.next());
                        if (itemObj != null) {
                            String resultID = itemObj.optString("abtest_experiment_result_id");
                            if (!TextUtils.equals(resultID, "-1")) {
                                resultSet.add(resultID);
                            }
                        }
                    }
                }
            }
        }
        return resultSet;
    }

    private boolean checkSubjectInfoEqual(String subjectId, String subjectName) {
        //1.check user
        if (TextUtils.equals(Experiment.SubjectType.USER.toString(), subjectName)) {
            return TextUtils.equals(SensorsDataAPI.sharedInstance().getDistinctId(), subjectId);
        }
        //2.check device
        if (TextUtils.equals(Experiment.SubjectType.DEVICE.toString(), subjectName)) {
            return TextUtils.equals(SensorsDataAPI.sharedInstance().getAnonymousId(), subjectId);
        }
        //3.check custom ids
        if (TextUtils.equals(Experiment.SubjectType.CUSTOM.toString(), subjectName)) {
            return SensorsABTestCustomIdsManager.getInstance().isContainTargetCustom(subjectId);
        }
        return false;
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

    private void saveABTestTrigger(String triggerKey, Experiment experiment) {
        if (TextUtils.isEmpty(triggerKey) || TextUtils.isEmpty(experiment.experimentId) || TextUtils.isEmpty(experiment.experimentGroupId)) {
            return;
        }
        if (mABTestTriggerCache == null) {
            mABTestTriggerCache = new JSONObject();
        }
        if (isDynamicConfigVersion(experiment)) {
            saveABTestDynamicConfigTrigger(experiment);
        } else {
            JSONObject obj = mABTestTriggerCache.optJSONObject(triggerKey);
            if (obj == null || !obj.has(experiment.experimentId) || !TextUtils.equals(experiment.experimentGroupId, obj.optString(experiment.experimentId))) {
                try {
                    if (obj == null) {
                        obj = new JSONObject();
                    }
                    obj.put(experiment.experimentId, experiment.experimentGroupId);
                    SALog.i(TAG, String.format("saveABTestTrigger triggerKey is %s,experimentId is %s,experimentGroupId is %s ", triggerKey, experiment.experimentId, experiment.experimentGroupId));
                    mABTestTriggerCache.put(triggerKey, obj);
                    StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.ABTEST_TRIGGER, mABTestTriggerCache.toString());
                } catch (JSONException e) {
                    SALog.printStackTrace(e);
                }
            }
        }
    }

    private void saveABTestDynamicConfigTrigger(Experiment experiment) {
        try {
            JSONArray subjectIdExperimentArray = mABTestTriggerCache.optJSONArray("subjectIdExperiment");
            if (subjectIdExperimentArray == null) {
                subjectIdExperimentArray = new JSONArray();
            }
            int index = -1;
            JSONObject subjectObj = null;
            JSONObject resultsObj = null;
            for (int i = 0; i < subjectIdExperimentArray.length(); i++) {
                JSONObject subjectObjTmp = subjectIdExperimentArray.optJSONObject(i);
                String subjectId = subjectObjTmp.optString("subject_id");
                String subjectName = subjectObjTmp.optString("subject_name");
                if (TextUtils.equals(experiment.subjectId, subjectId) && TextUtils.equals(experiment.subjectName, subjectName)) {
                    resultsObj = subjectObjTmp.optJSONObject("results");
                    subjectObj = subjectObjTmp;
                    index = i;
                    break;
                }
            }

            if (resultsObj == null) {
                resultsObj = new JSONObject();
            }
            JSONObject experimentOjb = new JSONObject();
            experimentOjb.put("abtest_experiment_id", experiment.experimentId);
            experimentOjb.put("abtest_experiment_group_id", experiment.experimentGroupId);
            experimentOjb.put("abtest_experiment_result_id", experiment.experimentResultId);
            resultsObj.put(experiment.experimentId, experimentOjb);

            if (subjectObj == null) {
                subjectObj = new JSONObject();
                subjectObj.put("subject_id", experiment.subjectId);
                subjectObj.put("subject_name", experiment.subjectName);
                subjectObj.put("results", resultsObj);
            }
            if (index == -1) {
                subjectIdExperimentArray.put(subjectObj);
            } else {
                subjectIdExperimentArray.put(index, subjectObj);
            }
            mABTestTriggerCache.put("subjectIdExperiment", subjectIdExperimentArray);
            StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.ABTEST_TRIGGER, mABTestTriggerCache.toString());
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        }
    }

    private boolean isDynamicConfigVersion(Experiment experiment) {
        if (isDynamicConfigVersion != null) {
            return isDynamicConfigVersion;
        }
        return isDynamicConfigVersion = !TextUtils.isEmpty(experiment.subjectId);
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
