package com.sensorsdata.sensorsabtest;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.core.SensorsABTestCacheManager;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SDKInitTest {

    private static final String sMockData = "{\n" +
            "\t\"status\": \"SUCCESS\",\n" +
            "\t\"error_type\": \"XXX_ERROR\",\n" +
            "\t\"error_msg\": \"xxxxxx\",\n" +
            "\t\"results\": [{\n" +
            "\t\t\"experiment_id\": \"0\",\n" +
            "\t\t\"experiment_group_id\": \"123\",\n" +
            "\t\t\"is_control_group\": false,\n" +
            "\t\t\"is_white_list\": true,\n" +
            "\t\t\"config\": {\n" +
            "\t\t\t\"variables\": \"0\",\n" +
            "\t\t\t\"type\": \"INTEGER\"\n" +
            "\t\t}\n" +
            "\t}, {\n" +
            "\t\t\"experiment_id\": \"1\",\n" +
            "\t\t\"experiment_group_id\": \"123\",\n" +
            "\t\t\"is_control_group\": false,\n" +
            "\t\t\"is_white_list\": true,\n" +
            "\t\t\"config\": {\n" +
            "\t\t\t\"variables\": \"1\",\n" +
            "\t\t\t\"type\": \"STRING\"\n" +
            "\t\t}\n" +
            "\t}, {\n" +
            "\t\t\"experiment_id\": \"2\",\n" +
            "\t\t\"experiment_group_id\": \"123\",\n" +
            "\t\t\"is_control_group\": false,\n" +
            "\t\t\"is_white_list\": true,\n" +
            "\t\t\"config\": {\n" +
            "\t\t\t\"variables\": \"true\",\n" +
            "\t\t\t\"type\": \"BOOLEAN\"\n" +
            "\t\t}\n" +
            "\t}, {\n" +
            "\t\t\"experiment_id\": \"3\",\n" +
            "\t\t\"experiment_group_id\": \"123\",\n" +
            "\t\t\"is_control_group\": false,\n" +
            "\t\t\"is_white_list\": true,\n" +
            "\t\t\"config\": {\n" +
            "\t\t\t\"variables\": \"{\\\"a\\\":\\\"Hello\\\",\\\"b\\\":\\\"World\\\"}\",\n" +
            "\t\t\t\"type\": \"JSON\"\n" +
            "\t\t}\n" +
            "\t}]\n" +
            "}";

    @Test
    public void testInit() {
        // normal

        String value = SensorsABTest.shareInstance().fetchCacheABTest("1", "default");
        assertEquals(value, "1");

        // type exception_int
        int valueNumber = SensorsABTest.shareInstance().fetchCacheABTest("1", -22);
        assertEquals(valueNumber, -22);

        // type exception_boolean
        boolean valueBoolean = SensorsABTest.shareInstance().fetchCacheABTest("1", false);
        assertEquals(valueBoolean, false);

        // type exception_json
        JSONObject jsonObject = new JSONObject();
        JSONObject valueJsonObject = SensorsABTest.shareInstance().fetchCacheABTest("1", jsonObject);
        assertEquals(jsonObject, valueJsonObject);

        // experimentId  ""
        String valueEmpty = SensorsABTest.shareInstance().fetchCacheABTest("", "default");
        assertEquals(valueEmpty, "default");

        // experimentId null
        String valueNull = SensorsABTest.shareInstance().fetchCacheABTest(null, "default");
        assertEquals(valueNull, "default");

        // defaultValue null
        String defaultNull = SensorsABTest.shareInstance().fetchCacheABTest("", null);
        assertEquals(defaultNull, null);

        // defaultValue & experimentId null
        String result = SensorsABTest.shareInstance().fetchCacheABTest(null, null);
        assertEquals(result, null);
    }


    @Before
    public void SensorsABTestConfigOptionsNull() {
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

        SensorsABTest.startWithConfigOptions(appContext, null);

        try {
            JSONObject jsonObject = new JSONObject(sMockData);
            JSONArray jsonArray = jsonObject.optJSONArray("results");
            SensorsABTestCacheManager.getInstance().getExperimentsFromMemoryCache(jsonArray.toString());
            SensorsABTestCacheManager.getInstance().saveExperiments2DiskCache(jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    @Before
//    public void SensorsABTestConfigOptionsUrlEmpty() {
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        SAConfigOptions configOptions = new SAConfigOptions("https://abtest.sensorsdata.cn/api/v2/sab/results/project_key=123");
//        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
//        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
//                SensorsAnalyticsAutoTrackEventType.APP_END |
//                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
//                SensorsAnalyticsAutoTrackEventType.APP_CLICK)
//                .enableTrackAppCrash()
//                .enableLog(true)
//                .enableVisualizedAutoTrack(true)
//                .enableVisualizedAutoTrackConfirmDialog(true);
//        SensorsDataAPI.startWithConfigOptions(appContext, configOptions);
//
//        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("");
//        SensorsABTest.startWithConfigOptions(appContext, abTestConfigOptions);
//
//        try {
//            JSONObject jsonObject = new JSONObject(sMockData);
//            JSONArray jsonArray = jsonObject.optJSONArray("results");
//            SensorsABTestCacheManager.getInstance().saveExperiments2MemoryCache(jsonArray.toString());
//            SensorsABTestCacheManager.getInstance().saveExperiments2DiskCache(jsonArray.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

//    @Before
//    public void insideThread() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//                SAConfigOptions configOptions = new SAConfigOptions("https://abtest.sensorsdata.cn/api/v2/sab/results/project_key=123");
//                // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
//                configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
//                        SensorsAnalyticsAutoTrackEventType.APP_END |
//                        SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
//                        SensorsAnalyticsAutoTrackEventType.APP_CLICK)
//                        .enableTrackAppCrash()
//                        .enableLog(true)
//                        .enableVisualizedAutoTrack(true)
//                        .enableVisualizedAutoTrackConfirmDialog(true);
//                SensorsDataAPI.startWithConfigOptions(appContext, configOptions);
//
//                SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("https://abtest.sensorsdata.cn/api/v2/sab/results/project_key=123");
//                SensorsABTest.startWithConfigOptions(appContext, abTestConfigOptions);
//
//                try {
//                    JSONObject jsonObject = new JSONObject(sMockData);
//                    JSONArray jsonArray = jsonObject.optJSONArray("results");
//                    SensorsABTestCacheManager.getInstance().saveExperiments2MemoryCache(jsonArray.toString());
//                    SensorsABTestCacheManager.getInstance().saveExperiments2DiskCache(jsonArray.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//}