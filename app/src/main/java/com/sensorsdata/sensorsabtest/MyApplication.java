/*
 * Created by zhangxiangwei on 2020/09/11.
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

package com.sensorsdata.sensorsabtest;

import android.app.Application;

import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    //新版环境
    private static final String SA_SERVER_URL = "http://10.129.20.65:8106/sa?project=test_091";
    private static final String AB_DISPATCH_SERVER_URL = "http://10.129.20.96:8202/api/v2/abtest/online/results?project-key=6654CD0907B729D4DAE175BA30E11143E52822A9";
    //旧版环境
//    private static final String SA_SERVER_URL = "http://10.129.28.106:8106/sa?project=default";
//    private static final String AB_DISPATCH_SERVER_URL = "http://10.129.29.10:8202/api/v2/abtest/online/results?project-key=130EB9E0EE57A09D91AC167C6CE63F7723CE0B22";

    @Override
    public void onCreate() {
        super.onCreate();
        SAConfigOptions configOptions = new SAConfigOptions(SA_SERVER_URL);
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
                        SensorsAnalyticsAutoTrackEventType.APP_END | SensorsAnalyticsAutoTrackEventType.APP_CLICK |
                        SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN)
                .enableTrackAppCrash()
                .enableLog(true)
                .enableJavaScriptBridge(true)
                .enableVisualizedAutoTrack(true);
        configOptions.setAnonymousId("wx001");

        SensorsDataAPI.startWithConfigOptions(this, configOptions);

        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions(AB_DISPATCH_SERVER_URL);
        SensorsABTest.startWithConfigOptions(this, abTestConfigOptions);
        Map<String,String> map=new HashMap<>();
        map.put("custom_subject_id","c222");
        SensorsABTest.shareInstance().setCustomIDs(map);
    }
}
