/*
 * Created by zhangxiangwei on 2020/09/11.
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

package com.sensorsdata.sensorsabtest;

import android.app.Application;

import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SAConfigOptions configOptions = new SAConfigOptions("https://newsdktest.datasink.sensorsdata.cn/sa?project=jiangjicheng&token=5a394d2405c147ca");
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
                SensorsAnalyticsAutoTrackEventType.APP_END |
                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
                SensorsAnalyticsAutoTrackEventType.APP_CLICK)
                .enableTrackAppCrash()
                .enableLog(true)
                .enableJavaScriptBridge(true)
                .enableVisualizedAutoTrack(true)
                .enableVisualizedAutoTrackConfirmDialog(true);
        SensorsDataAPI.startWithConfigOptions(this, configOptions);

        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("http://10.120.52.81:8222/api/v2/abtest/online/results?project-key=fake");
        SensorsABTest.startWithConfigOptions(this, abTestConfigOptions);
    }
}
