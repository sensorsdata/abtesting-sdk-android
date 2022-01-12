/*
 * Created by zhangxiangwei on 2020/09/10.
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

package com.sensorsdata.abtest.service;

import android.app.IntentService;
import android.content.Intent;

import com.sensorsdata.abtest.core.SensorsABTestApiRequestHelper;
import com.sensorsdata.abtest.util.AlarmManagerUtils;
import com.sensorsdata.analytics.android.sdk.SALog;

public class GlobalLoopService extends IntentService {

    private static final String TAG = "SAB.GlobalLoopService";

    public GlobalLoopService(String name) {
        super(name);
    }

    public GlobalLoopService() {
        super("GlobalLoopService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SALog.i(TAG, "GlobalLoopService receive");
        new SensorsABTestApiRequestHelper<>().requestExperimentsAndUpdateCache();
        AlarmManagerUtils.getInstance(this).setUpAlarmOnReceiver();
    }
}
