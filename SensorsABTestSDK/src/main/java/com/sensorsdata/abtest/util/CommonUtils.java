/*
 * Created by luweibin on 2021/12/16.
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

package com.sensorsdata.abtest.util;

import android.text.TextUtils;

import com.sensorsdata.abtest.core.SensorsABTestCustomIdsManager;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

public class CommonUtils {
    /**
     * 获取用于 AB Testing SDK 区分用户的唯一标识
     * 标识用户使用 distinctId、loginId、anonymousId 三者拼接
     * 如果存在自定义主体 IDs，自定义主体 IDs 也会拼接到用户唯一标识
     *
     * @return 返回用于标识缓存内容对应的用户唯一标识
     */
    public static String getCurrentUserIdentifier() {
        return SensorsDataAPI.sharedInstance().getDistinctId()
                + getLoginId()
                + SensorsDataAPI.sharedInstance().getAnonymousId()
                + SensorsABTestCustomIdsManager.getInstance().getCustomIdsString();
    }

    /**
     * SensorsDataAPI.sharedInstance().getLoginId() 在不同线程下返回的值不同
     * 这里统一使用主线程下的默认值：null
     *
     * @return id
     */
    public static String getLoginId() {
        String id = SensorsDataAPI.sharedInstance().getLoginId();
        return TextUtils.isEmpty(id) ? null : id;
    }
}