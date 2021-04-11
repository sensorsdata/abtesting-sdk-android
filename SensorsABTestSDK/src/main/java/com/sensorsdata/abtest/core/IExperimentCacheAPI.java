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


import com.sensorsdata.abtest.entity.Experiment;

import java.util.concurrent.ConcurrentHashMap;

interface IExperimentCacheAPI {
    /**
     * 文件缓存
     *
     * @param result response
     */
    void saveExperiments2DiskCache(String result);

    /**
     * 内存缓存
     *
     * @param result response
     * @return 试验集合
     */
    ConcurrentHashMap<String, Experiment> getExperimentsFromMemoryCache(String result);

    /**
     * 文件缓存和内存缓存
     *
     * @param result response
     * @return 试验集合
     */
    ConcurrentHashMap<String, Experiment> loadExperimentsFromCache(String result);

    /**
     * 加载文件缓存
     */
    void loadExperimentsFromDiskCache();

    /**
     * 通过试验参数名 获取缓存试验实体
     *
     * @param paramName 试验参数名
     * @param defaultValue 默认值
     * @param <T> 默认值类型
     * @return 试验实体
     */
    <T> T getExperimentVariableValue(String paramName, T defaultValue);
}
