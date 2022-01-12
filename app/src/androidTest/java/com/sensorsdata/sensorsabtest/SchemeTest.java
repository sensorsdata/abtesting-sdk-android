/*
 * Created by zhangxiangwei on 2020/09/24.
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

package com.sensorsdata.sensorsabtest;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.abtest.core.SensorsABTestSchemeHandler;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SchemeTest {

    private static final String uriString = "sa83746610://abtest?sensors_abtest_url=http://abtesting.debugbox.sensorsdata.cn/api/v2/sa/abtest/experiments/distinct_id&feature_code=3aefsdazdf&account_id=001";

    @Test
    public void testScheme() {
        SensorsABTestSchemeHandler.handleSchemeUrl(uriString);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void init() {

        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SAConfigOptions configOptions = new SAConfigOptions("https://abtest.sensorsdata.cn/api/v2/sab/results/project_key=123");
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
                SensorsAnalyticsAutoTrackEventType.APP_END |
                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
                SensorsAnalyticsAutoTrackEventType.APP_CLICK)
                .enableTrackAppCrash()
                .enableLog(true)
                .enableVisualizedAutoTrack(true)
                .enableVisualizedAutoTrackConfirmDialog(true);
        SensorsDataAPI.startWithConfigOptions(appContext, configOptions);

        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("http://10.120.18.61:8212/api/v2/abtest/results?project-key=123");
        SensorsABTest.startWithConfigOptions(appContext, abTestConfigOptions);
    }

}




