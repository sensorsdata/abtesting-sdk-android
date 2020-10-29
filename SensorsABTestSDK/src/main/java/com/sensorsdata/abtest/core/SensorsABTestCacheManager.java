/*
 * Created by zhangxiangwei on 2020/09/09.
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


import android.text.TextUtils;

import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.entity.SABErrorEnum;
import com.sensorsdata.abtest.util.SPUtils;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class SensorsABTestCacheManager implements IExperimentCacheAPI {

    private static final String TAG = "SAB.SensorsABTestCacheManager";
    public ConcurrentHashMap<String, Experiment> mHashMap;
    private static final String KEY_EXPERIMENT = "key_experiment";

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
                        JSONObject configObject = object.optJSONObject("config");
                        if (configObject != null) {
                            experiment.config = configObject;
                            experiment.variables = configObject.optString("variables");
                            experiment.type = configObject.optString("type");
                        }
                        hashMap.put(experiment.experimentId, experiment);
                    }
                }
                SALog.i(TAG, "saveExperiments2MemoryCache | experiments:\n" + JSONUtils.formatJson(hashMap.toString()));
            }
        } catch (JSONException e) {
            SALog.printStackTrace(e);
        }
        mHashMap.clear();
        mHashMap.putAll(hashMap);
        return hashMap;
    }

    @Override
    public ConcurrentHashMap<String, Experiment> loadExperimentsFromCache(String result) {
        ConcurrentHashMap<String, Experiment> hashMap = getExperimentsFromMemoryCache(result);
        saveExperiments2DiskCache(result);
        return hashMap;
    }

    /**
     * 从内存缓存中获取试验命中的变量值
     *
     * @param experimentId 试验 id
     * @return 试验命中的试验实体
     */
    private Experiment getExperimentById(String experimentId) {
        if (experimentId != null && mHashMap.containsKey(experimentId)) {
            return mHashMap.get(experimentId);
        }
        return null;
    }

    /**
     * 获取缓存中的实验变量值
     *
     * @param experimentId 试验 id
     * @param defaultValue 默认值
     * @param <T> 类型
     * @return 缓存中的实验变量
     */
    public <T> T getExperimentVariable(String experimentId, T defaultValue) {
        if (TextUtils.isEmpty(experimentId)) {
            SALog.i(TAG, String.format("experiment_id：%s， 试验 ID 不正确，试验 ID 必须为非空字符串！", experimentId));
            return null;
        }
        if (defaultValue != null) {
            SALog.i(TAG, "getExperimentVariable experimentId: " + experimentId + " ,params type: " + defaultValue.getClass());
        }
        Experiment experiment = getExperimentById(experimentId);
        if (experiment != null) {
            SALog.i(TAG, "getExperimentVariable experiment type: " + experiment.type);
            if (experiment.checkTypeIsValid(defaultValue)) {
                T t = experiment.getExperimentVariable(defaultValue);
                if (t != null) {
                    SALog.i(TAG, "getExperimentVariable success and type: " + t.getClass() + " ,value: " + t.toString());
                    if (!experiment.isWhiteList) {
                        SensorsABTestTrackHelper.trackABTestTrigger(experiment);
                    }
                }
                return t;
            } else if (defaultValue != null) {
                SABErrorDispatcher.dispatchSABException(SABErrorEnum.ASYNC_REQUEST_PARAMS_TYPE_NOT_VALID, experimentId, experiment.type, defaultValue.getClass().toString());
            }
        }
        SALog.i(TAG, "getExperimentVariable return null");
        return null;
    }

}
