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

import android.content.Context;

import com.sensorsdata.abtest.util.SPUtils;

class StoreManagerSPImpl implements IStoreManager {
    public StoreManagerSPImpl(Context context) {
        SPUtils.getInstance().init(context);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return SPUtils.getInstance().getString(key, defaultValue);
    }

    @Override
    public void putString(String key, String value) {
        SPUtils.getInstance().put(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return SPUtils.getInstance().getLong(key, defaultValue);
    }

    @Override
    public void putLong(String key, long value) {
        SPUtils.getInstance().put(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return SPUtils.getInstance().getInt(key, defaultValue);
    }

    @Override
    public void putInt(String key, int value) {
        SPUtils.getInstance().put(key, value);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return SPUtils.getInstance().getFloat(key, defaultValue);
    }

    @Override
    public void putFloat(String key, float value) {
        SPUtils.getInstance().put(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return SPUtils.getInstance().getBoolean(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        SPUtils.getInstance().put(key, value);
    }
}