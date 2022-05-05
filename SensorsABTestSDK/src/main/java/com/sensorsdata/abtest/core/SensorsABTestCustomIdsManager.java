/*
 * Created by luweibin on 2022/01/21.
 * Copyright 2015－2022 Sensors Data Inc.
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
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.abtest.util.SensorsDataHelper;
import com.sensorsdata.analytics.android.sdk.SALog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SensorsABTestCustomIdsManager {
    private static final String TAG = "SAB.SensorsABTestCustomIdsManager";
    private JSONObject mCustomIds = null;

    private SensorsABTestCustomIdsManager() {
    }

    private static class SingleHolder {
        private static final SensorsABTestCustomIdsManager INSTANCE = new SensorsABTestCustomIdsManager();
    }

    public static SensorsABTestCustomIdsManager getInstance() {
        return SingleHolder.INSTANCE;
    }

    /**
     * 加载自定义主体 ID customIds
     */
    public void loadCustomIds() {
        try {
            String cacheCustomIds = StoreManagerFactory.getStoreManager().getString(AppConstants.Property.Key.CUSTOM_IDS, "");
            if (!TextUtils.isEmpty(cacheCustomIds)) {
                mCustomIds = new JSONObject(cacheCustomIds);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 设置自定义主体 IDs
     *
     * @param customIds 新的自定义主体 IDs
     */
    public void setCustomIds(Map<String, String> customIds) {
        try {
            // step1.校验参数 key 和 value 的合法性
            customIds = checkCustomIdValid(customIds);
            // step2.判断 IDs 是否和缓存相同
            if (!isSameCustomIds(customIds)) {
                // step3.当 IDs 和缓存不同时，缓存新的 IDs 并通知更新数据
                if (customIds == null) {
                    mCustomIds = null;
                } else {
                    mCustomIds = new JSONObject(customIds);
                }
                try {
                    StoreManagerFactory.getStoreManager().putString(
                            AppConstants.Property.Key.CUSTOM_IDS,
                            mCustomIds == null ? "" : mCustomIds.toString());
                } catch (Exception e) {
                    SALog.printStackTrace(e);
                }
                SensorsABTestHelper.onUserInfoChanged();
            } else {
                SALog.i(TAG, "The new custom-IDs is the same as before");
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    /**
     * 获取当前自定义主体 IDs
     *
     * @return 当前自定义主体 IDs
     */
    public JSONObject getCustomIds() {
        return mCustomIds;
    }

    /**
     * 获取当前自定义主体字符串类型 IDs
     *
     * @return 当前自定义主体字符串类型 IDs
     */
    public String getCustomIdsString() {
        return mCustomIds == null ? "" : mCustomIds.toString();
    }


    /**
     * 检查最新的自定义主体是否与缓存的自定义主体相同
     *
     * @param customIds 待检查的自定义主体 IDs
     * @return 是否与缓存相同
     */
    private boolean isSameCustomIds(Map<String, String> customIds) {
        if ((mCustomIds == null || mCustomIds.length() == 0) && (customIds == null || customIds.isEmpty())) {
            return true;
        } else if (mCustomIds != null && customIds != null && mCustomIds.length() == customIds.size()) {
            for (Map.Entry<String, String> entry : customIds.entrySet()) {
                String customKey = entry.getKey();
                String customValue = entry.getValue();
                if (!mCustomIds.has(customKey) || !mCustomIds.optString(customKey).equals(customValue)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 校验自定义主体的合法性，校验规则：
     * 1.如果 key 不合法，不上报此 ID 并日志报错
     * 2.如果 value 不合法，正常上报此 ID 并日志报错
     *
     * @param customIds 需要校验的自定义主体
     * @return 通过校验的自定义主体
     */
    private Map<String, String> checkCustomIdValid(Map<String, String> customIds) {
        Map<String, String> validMap = null;
        if (customIds == null) return null;
        for (Map.Entry<String, String> entry : customIds.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TextUtils.isEmpty(key)) {
                printLog("自定义主体 ID key 为空，已移除该参数！");
                continue;
            } else if (key.length() > 100) {
                printLog("自定义主体 ID key [" + key + "] 长度超过 100，已移除该参数！");
                continue;
            } else if (!SensorsDataHelper.isKeyMatch(key)) {
                printLog("自定义主体 ID key [" + key + "] 不合法，已移除该参数！");
                continue;
            }
            if (TextUtils.isEmpty(value)) {
                printLog("自定义主体 ID value 为空！");
            } else if (value.length() > 1024) {
                printLog("自定义主体 ID value 的长度超过了 1024！");
            }
            if (validMap == null) {
                validMap = new HashMap<>();
            }
            validMap.put(key, value);
        }
        return validMap;
    }

    private void printLog(String msg) {
        SALog.i(TAG, msg);
    }
}