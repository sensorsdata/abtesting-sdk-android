/*
 * Created by zhangxiangwei on 2020/09/09.
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

import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.util.SPUtils;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SensorsABTestCacheManager implements IExperimentCacheAPI {

    private static final String TAG = "SAB.SensorsABTestCacheManager";
    public ConcurrentHashMap<String, Experiment> mHashMap;
    private static final String KEY_EXPERIMENT = "key_experiment_with_distinct_id";

    private SensorsABTestCacheManager() {
        mHashMap = new ConcurrentHashMap<>();
    }

    private static class SensorsABTestCacheManagerStaticNestedClass {
        private static SensorsABTestCacheManager INSTANCE = new SensorsABTestCacheManager();
    }

    public static SensorsABTestCacheManager getInstance() {
        return SensorsABTestCacheManagerStaticNestedClass.INSTANCE;
    }

    /**
     * 更新文件缓存
     *
     * @param result 网络请求的试验结果
     */
    @Override
    public void saveExperiments2DiskCache(String result) {
        SALog.i(TAG, "更新试验数据成功:\n" + result);
        SPUtils.getInstance().put(KEY_EXPERIMENT, result);
    }

    /**
     * 启动时检查文件缓存，更新至内存中
     */
    @Override
    public void loadExperimentsFromDiskCache() {
        String experiments = SPUtils.getInstance().getString(KEY_EXPERIMENT);
        SALog.i(TAG, "loadExperimentsFromDiskCache | experiments:\n" + JSONUtils.formatJson(experiments));
        getExperimentsFromMemoryCache(experiments);
    }

    /**
     * 更新内存缓存
     *
     * @param result response 结果
     */
    @Override
    public ConcurrentHashMap<String, Experiment> getExperimentsFromMemoryCache(String result) {
        ConcurrentHashMap<String, Experiment> hashMap = new ConcurrentHashMap<>();
        if (TextUtils.isEmpty(result)) {
            mHashMap.clear();
            return hashMap;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String experiments = jsonObject.optString("experiments");
            if (TextUtils.isEmpty(experiments)) {
                mHashMap.clear();
                return hashMap;
            }
            parseCache(experiments, hashMap);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        mHashMap.clear();
        mHashMap.putAll(hashMap);
        return hashMap;
    }

    private void parseCache(String result, ConcurrentHashMap<String, Experiment> hashMap) {
        JSONArray array = null;
        try {
            array = new JSONArray(result);
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
                    }
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public ConcurrentHashMap<String, Experiment> loadExperimentsFromCache(String result) {
        ConcurrentHashMap<String, Experiment> hashMap = getExperimentsFromMemoryCache(result);
        saveExperiments2DiskCache(result);
        return hashMap;
    }

    /**
     * 清除文件缓存
     */
    void clearCache() {
        loadExperimentsFromCache("");
    }

    /**
     * 从内存缓存中获取命中的试验实体
     *
     * @param paramName 试验参数名
     * @return 命中的试验实体
     */
    private Experiment getExperimentByParamName(String paramName) {
        if (paramName != null && mHashMap.containsKey(paramName)) {
            return mHashMap.get(paramName);
        }
        return null;
    }

    /**
     * 获取缓存中的试验变量值
     *
     * @param paramName 试验参数名
     * @param defaultValue 默认值
     * @param <T> 类型
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
        Experiment experiment = getExperimentByParamName(paramName);
        if (experiment != null) {
            if (experiment.checkTypeIsValid(paramName, defaultValue)) {
                T t = experiment.getVariableValue(paramName, defaultValue);
                if (t != null) {
                    SALog.i(TAG, "getExperimentVariableValue success and type: " + t.getClass() + " ,value: " + t.toString());
                    if (!experiment.isWhiteList) {
                        SensorsABTestTrackHelper.getInstance().trackABTestTrigger(experiment, SensorsDataAPI.sharedInstance().getDistinctId(), SensorsDataAPI.sharedInstance().getLoginId(), SensorsDataAPI.sharedInstance().getAnonymousId());
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
        SALog.i(TAG, "getExperimentVariableValue return null");
        return null;
    }
}
