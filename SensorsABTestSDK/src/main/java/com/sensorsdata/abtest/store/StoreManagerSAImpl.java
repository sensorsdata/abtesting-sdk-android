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

import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.plugin.encrypt.AbstractStoreManager;
import com.sensorsdata.analytics.android.sdk.plugin.encrypt.DefaultStorePlugin;
import com.sensorsdata.analytics.android.sdk.plugin.encrypt.SAEncryptStorePlugin;
import com.sensorsdata.analytics.android.sdk.plugin.encrypt.StorePlugin;

import java.util.List;

class StoreManagerSAImpl implements IStoreManager {

    public StoreManagerSAImpl(Context context) {
        SABStoreManager.getInstance().init(context);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return SABStoreManager.getInstance().getString(key, defaultValue);
    }

    @Override
    public void putString(String key, String value) {
        SABStoreManager.getInstance().setString(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return SABStoreManager.getInstance().getLong(key, defaultValue);
    }

    @Override
    public void putLong(String key, long value) {
        SABStoreManager.getInstance().setLong(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return SABStoreManager.getInstance().getInteger(key, defaultValue);
    }

    @Override
    public void putInt(String key, int value) {
        SABStoreManager.getInstance().setInteger(key, value);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return SABStoreManager.getInstance().getFloat(key, defaultValue);
    }

    @Override
    public void putFloat(String key, float value) {
        SABStoreManager.getInstance().setFloat(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return SABStoreManager.getInstance().getBool(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        SABStoreManager.getInstance().setBool(key, value);
    }

    private static class SABStoreManager extends AbstractStoreManager {
        public static SABStoreManager getInstance() {
            return SingletonHolder.INSTANCE;
        }

        private SABStoreManager() {
        }

        public void init(Context context) {
            String oldSpFileName = "spUtils";
            List<StorePlugin> saRegisterPlugins = SensorsDataAPI.getConfigOptions().getStorePlugins();
            DefaultStorePlugin defaultStorePlugin = new DefaultStorePlugin(context, oldSpFileName) {
                @Override
                public List<String> storeKeys() {
                    return null;
                }
            };
            if (saRegisterPlugins == null || saRegisterPlugins.isEmpty()) {
                mDefaultState = true;
                registerPlugin(defaultStorePlugin);
            } else {
                mDefaultState = false;
                if (isRegisterPlugin(context, oldSpFileName)) {
                    registerPlugin(defaultStorePlugin);
                }
                for (StorePlugin plugin : saRegisterPlugins) {
                    if (plugin instanceof SAEncryptStorePlugin) {
                        registerPlugin(new SAEncryptStorePlugin(context, AppConstants.AB_TEST_CACHE_FILE_NAME));
                    } else {
                        registerPlugin(plugin);
                    }
                }
            }
            upgrade();
        }

        private static class SingletonHolder {
            private static final SABStoreManager INSTANCE = new SABStoreManager();
        }
    }
}