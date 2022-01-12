/*
 * Created by luweibin on 2022/01/06.
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

package com.sensorsdata.abtest.store;

/**
 * 该接口用于兼容 SP 存储和 SA 的合规存储方案
 */
public interface IStoreManager {
    String getString(String key, String defaultValue);

    void putString(String key, String value);

    long getLong(String key, long defaultValue);

    void putLong(String key, long value);

    int getInt(String key, int defaultValue);

    void putInt(String key, int value);

    float getFloat(String key, float defaultValue);

    void putFloat(String key, float value);

    boolean getBoolean(String key, boolean defaultValue);

    void putBoolean(String key, boolean value);
}