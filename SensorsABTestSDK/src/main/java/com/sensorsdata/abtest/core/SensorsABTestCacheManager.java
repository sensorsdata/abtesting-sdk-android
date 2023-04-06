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

package com.sensorsdata.abtest.core;


import android.text.TextUtils;

import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.abtest.util.CommonUtils;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SensorsABTestCacheManager {

    private static final String TAG = "SAB.SensorsABTestCacheManager";
    private final ConcurrentHashMap<String, Experiment> mHashMap;
    /** 与 mHashMap 类似，该 Map 用于保存 out list 中的结果，其 key 是 paramName, value 是 Experiment */
    private final ConcurrentHashMap<String, Experiment> mOutListHashMap;
    private final CopyOnWriteArraySet<String> mDispatchResultSet;
    private JSONArray mFuzzyExperiments = null;
    private String mIdentifier = "";

    private SensorsABTestCacheManager() {
        mHashMap = new ConcurrentHashMap<>();
        mOutListHashMap = new ConcurrentHashMap<>();
        mDispatchResultSet = new CopyOnWriteArraySet<>();
    }

    private static class SensorsABTestCacheManagerStaticNestedClass {
        private static final SensorsABTestCacheManager INSTANCE = new SensorsABTestCacheManager();
    }

    public static SensorsABTestCacheManager getInstance() {
        return SensorsABTestCacheManagerStaticNestedClass.INSTANCE;
    }

    /**
     * 更新文件缓存
     *
     * @param result 网络请求的试验结果
     */
    public void saveExperiments2DiskCache(String result) {
        SALog.i(TAG, "更新试验数据成功:\n" + result);
        StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.EXPERIMENT_CACHE_KEY, result);
    }

    /**
     * 启动时检查文件缓存，更新至内存中
     */
    public void loadExperimentsFromDiskCache() {
        String experiments = StoreManagerFactory.getStoreManager().getString(AppConstants.Property.Key.EXPERIMENT_CACHE_KEY, "");
        SALog.i(TAG, "loadExperimentsFromDiskCache | experiments:\n" + JSONUtils.formatJson(experiments));
        updateExperimentsMemoryCache(experiments);
        updateOutListExperimentMemoryCache(experiments);
    }

    /**
     * 更新 out list 试验缓存
     *
     * @param result 试验 json 数据
     */
    public void updateOutListExperimentMemoryCache(String result) {
        if (TextUtils.isEmpty(result)) {
            mOutListHashMap.clear();
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray outListArray = jsonObject.optJSONArray("outList");
            mOutListHashMap.clear();
            if (outListArray == null || outListArray.length() == 0) {
                return;
            }
            for (int i = 0; i < outListArray.length(); i++) {
                JSONObject experimentObj = outListArray.optJSONObject(i);
                if (experimentObj != null) {
                    Experiment experiment = new Experiment();

                    experiment.experimentId = experimentObj.optString("abtest_experiment_id");
                    experiment.experimentGroupId = experimentObj.optString("abtest_experiment_group_id");
                    experiment.isControlGroup = experimentObj.optBoolean("is_control_group");
                    experiment.isWhiteList = experimentObj.optBoolean("is_white_list");
                    experiment.experimentResultId = experimentObj.optString("abtest_experiment_result_id");
                    experiment.experimentType = experimentObj.optString("experiment_type");
                    experiment.subjectId = experimentObj.optString("subject_id");
                    experiment.subjectName = experimentObj.optString("subject_name");
                    experiment.experimentVersion = experimentObj.optString("abtest_experiment_version");
                    experiment.originalJsonStr = experimentObj.toString();

                    JSONArray variablesArray = experimentObj.optJSONArray("variables");
                    if (variablesArray != null && variablesArray.length() > 0) {
                        List<Experiment.Variable> list = new ArrayList<>();
                        for (int j = 0; j < variablesArray.length(); j++) {
                            JSONObject variableObject = variablesArray.optJSONObject(j);
                            Experiment.Variable variable = new Experiment.Variable();
                            variable.type = variableObject.optString("type");
                            variable.name = variableObject.optString("name");
                            variable.value = variableObject.optString("value");
                            list.add(variable);
                            // 服务端对试验列表排序，不同的试验相同 key 按照顺序优先取第一个
                            if (!mOutListHashMap.containsKey(variable.name)) {
                                mOutListHashMap.put(variable.name, experiment);
                            }
                        }
                        experiment.variables = list;
                    }
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 更新内存缓存，并返回缓存结果
     *
     * @param result response 结果
     */
    public ConcurrentHashMap<String, Experiment> updateExperimentsMemoryCache(String result) {
        ConcurrentHashMap<String, Experiment> hashMap = new ConcurrentHashMap<>();
        mHashMap.clear();
        mDispatchResultSet.clear();
        if (TextUtils.isEmpty(result)) {
            return hashMap;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String experiments = jsonObject.optString("experiments");
            if (TextUtils.isEmpty(experiments)) {
                return hashMap;
            }
            parseCache(experiments, hashMap);
            mIdentifier = jsonObject.optString("identifier", "");
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        mHashMap.putAll(hashMap);
        return hashMap;
    }

    private void parseCache(String result, ConcurrentHashMap<String, Experiment> hashMap) {
        try {
            JSONArray array = new JSONArray(result);
            if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    if (object != null) {
                        Experiment experiment = new Experiment();
                        experiment.experimentId = object.optString("abtest_experiment_id");
                        if (TextUtils.isEmpty(experiment.experimentId)) {
                            break;
                        }
                        experiment.experimentGroupId = object.optString("abtest_experiment_group_id");
                        experiment.isControlGroup = object.optBoolean("is_control_group");
                        experiment.isWhiteList = object.optBoolean("is_white_list");
                        experiment.experimentResultId = object.optString("abtest_experiment_result_id");
                        experiment.experimentType = object.optString("experiment_type");
                        experiment.subjectId = object.optString("subject_id");
                        experiment.subjectName = object.optString("subject_name");
                        experiment.experimentVersion = object.optString("abtest_experiment_version");
                        experiment.originalJsonStr = object.toString();

                        JSONArray variablesArray = object.optJSONArray("variables");
                        if (variablesArray != null && variablesArray.length() > 0) {
                            List<Experiment.Variable> list = new ArrayList<>();
                            for (int j = 0; j < variablesArray.length(); j++) {
                                JSONObject variableObject = variablesArray.optJSONObject(j);
                                Experiment.Variable variable = new Experiment.Variable();
                                variable.type = variableObject.optString("type");
                                variable.name = variableObject.optString("name");
                                variable.value = variableObject.optString("value");
                                list.add(variable);
                                // 服务端对试验列表排序，不同的试验相同 key 按照顺序优先取第一个
                                if (!hashMap.containsKey(variable.name)) {
                                    hashMap.put(variable.name, experiment);
                                }
                            }
                            experiment.variables = list;
                        }

                        if (!TextUtils.isEmpty(experiment.experimentResultId)) {
                            mDispatchResultSet.add(experiment.experimentResultId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 更新缓存，并返回 paraName 和试验的映射
     *
     * @param result 试验 paraName 和试验的映射
     * @return Map
     */
    public ConcurrentHashMap<String, Experiment> updateExperimentsCache(String result) {
        ConcurrentHashMap<String, Experiment> hashMap = updateExperimentsMemoryCache(result);
        updateOutListExperimentMemoryCache(result);
        saveExperiments2DiskCache(result);
        return hashMap;
    }

    /**
     * 清除文件缓存
     */
    void clearCache() {
        updateExperimentsCache("");
        mFuzzyExperiments = null;
    }

    /**
     * 从内存缓存中获取命中的试验实体
     *
     * @param paramName 试验参数名
     * @return 命中的试验实体
     */
    private Experiment getExperimentByParamName(String paramName) {
        if (paramName != null && CommonUtils.getCurrentUserIdentifier().equals(mIdentifier) && mHashMap.containsKey(paramName)) {
            return mHashMap.get(paramName);
        }
        return null;
    }

    public Experiment getExperimentByParamNameFromOutList(String paramName) {
        if (paramName != null && CommonUtils.getCurrentUserIdentifier().equals(mIdentifier) && mOutListHashMap.containsKey(paramName)) {
            return mOutListHashMap.get(paramName);
        }
        return null;
    }

    /**
     * 获取缓存中的试验变量值
     *
     * @param paramName    试验参数名
     * @param defaultValue 默认值
     * @param <T>          类型
     * @return 缓存中的试验变量值
     */
    public <T> T getExperimentVariableValue(String paramName, T defaultValue) {
        if (TextUtils.isEmpty(paramName)) {
            SALog.i(TAG, String.format("experiment param name：%s，试验参数名不正确，试验参数名必须为非空字符串！", paramName));
            return null;
        }
        if (defaultValue != null) {
            SALog.i(TAG, "getExperimentVariableValue param name: " + paramName + " , type: " + defaultValue.getClass());
        }
        T result = checkCachedExperiment(getExperimentByParamName(paramName), paramName, defaultValue);
        checkCachedExperiment(getExperimentByParamNameFromOutList(paramName), paramName, defaultValue);
        return result;
    }

    private <T> T checkCachedExperiment(Experiment experiment, String paramName, T defaultValue) {
        if (experiment != null) {
            if (TextUtils.equals(experiment.experimentResultId, "-1")) {
                SALog.i(TAG, "The experiment is from out list.");
            }
            if (experiment.checkTypeIsValid(paramName, defaultValue)) {
                T t = experiment.getVariableValue(paramName, defaultValue);
                if (t != null) {
                    SALog.i(TAG, "checkCachedExperiment success and type: " + t.getClass() + " ,value: " + t.toString());
                    if (!experiment.isWhiteList) {
                        SensorsABTestTrackHelper.getInstance().trackABTestTrigger(experiment,
                                SensorsDataAPI.sharedInstance().getDistinctId(),
                                CommonUtils.getLoginId(),
                                SensorsDataAPI.sharedInstance().getAnonymousId(),
                                SensorsABTestCustomIdsManager.getInstance().getCustomIdsString());
                    }
                }
                return t;
            } else if (defaultValue != null) {
                String variableType = "";
                Experiment.Variable variable = experiment.getVariableByParamName(paramName);
                if (variable != null) {
                    variableType = variable.type;
                }
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_PARAMS_TYPE_NOT_VALID, paramName, variableType, defaultValue.getClass().toString());
            }
        }
        SALog.i(TAG, "checkCachedExperiment return null");
        return null;
    }

    public void saveFuzzyExperiments(JSONArray fuzzyExperiments) {
        mFuzzyExperiments = fuzzyExperiments;
    }

    public boolean isFuzzyExperiments(String experimentName) {
        if (mFuzzyExperiments == null) {
            return true;
        }
        int fuzzyExperimentsSize = mFuzzyExperiments.length();
        for (int index = 0; index < fuzzyExperimentsSize; index++) {
            try {
                if (experimentName.equals(mFuzzyExperiments.getString(index))) {
                    return true;
                }
            } catch (Exception e) {
                SALog.printStackTrace(e);
            }
        }
        return false;
    }

    /**
     * 获取所有分流结果的 unique_id 值
     *
     * @return unique_id set
     */
    public Set<String> getDispatchUniqueIdResult() {
        return new HashSet<>(mDispatchResultSet);
    }

    /**
     * 获取所有出组的试验
     *
     * @return HashMap
     */
    public Map<String, Experiment> getOutListExperiment() {
        return new HashMap<>(mOutListHashMap);
    }
}
