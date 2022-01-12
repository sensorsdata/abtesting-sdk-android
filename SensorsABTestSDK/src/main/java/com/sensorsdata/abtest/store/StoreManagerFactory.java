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

import com.sensorsdata.abtest.util.AppInfoUtils;

public class StoreManagerFactory {
    private static volatile IStoreManager mStoreManager = null;

    /**
     * 初始化数据存储方案，根据当前 SA 的版本来判断是否需要合规存储
     *
     * @param context 初始化存储方案需要的上下文
     */
    public synchronized static void initStoreManager(Context context) {
        if (AppInfoUtils.checkSASecretVersionIsValid()) {
            mStoreManager = new StoreManagerSAImpl(context);
        } else {
            mStoreManager = new StoreManagerSPImpl(context);
        }
    }

    /**
     * 获取存储方式
     *
     * @return 存储方式接口
     */
    public static IStoreManager getStoreManager() {
        if (mStoreManager == null) {
            synchronized (StoreManagerFactory.class) {
                if (mStoreManager == null) {
                    return new StoreManagerEmptyImpl();
                } else {
                    return mStoreManager;
                }
            }
        } else {
            return mStoreManager;
        }
    }
}