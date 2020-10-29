package com.sensorsdata.sensorsabtest;

import android.content.Context;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.SensorsABTestConfigOptions;
import com.sensorsdata.abtest.core.SensorsABTestCacheManager;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApiTest {

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
    public void testApi() {
        testFetchCacheABTest();
        testFastFetchABTest();
    }

    @Test
    public void testFetchCacheABTest() {
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

    @Test
    public void testAsyncFetchABTest() {
        // normal
        SensorsABTest.shareInstance().asyncFetchABTest("666", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });
        // type exception_int
        SensorsABTest.shareInstance().asyncFetchABTest("666", -1, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                int value = result;
                assertEquals(value, 1);
            }
        });
        // type exception_boolean
        SensorsABTest.shareInstance().asyncFetchABTest("666", true, new OnABTestReceivedData<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                assertEquals(true, result);
            }
        });
        // type exception_jsonObject
        final JSONObject object = new JSONObject();
        SensorsABTest.shareInstance().asyncFetchABTest("666", object, new OnABTestReceivedData<JSONObject>() {
            @Override
            public void onResult(JSONObject jsonObject) {
                assertEquals(object, jsonObject);
            }
        });
        //
        SensorsABTest.shareInstance().asyncFetchABTest("", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });

        SensorsABTest.shareInstance().asyncFetchABTest(null, "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });

        SensorsABTest.shareInstance().asyncFetchABTest("122", "", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "");
            }
        });

        SensorsABTest.shareInstance().asyncFetchABTest("666", null, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                int value = result;
                assertEquals(value, 1);
            }
        });

        SensorsABTest.shareInstance().asyncFetchABTest(null, null, new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, null);
            }
        });

        SensorsABTest.shareInstance().asyncFetchABTest(null, null, null);
    }

    /**
     * 含 timeout
     */
    @Test
    public void testAsyncFetchABTestWithTimeOut() {
        // normal
        SensorsABTest.shareInstance().asyncFetchABTest("666", -1, 5, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                SALog.i("testAsyncFetchABTestWithTimeOut", " value: " + result);
            }
        });
    }

    @Test
    public void testFastFetchABTest() {
        // normal
        SensorsABTest.shareInstance().fastFetchABTest("1", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "1");
            }
        });
        // type exception_int
        SensorsABTest.shareInstance().fastFetchABTest("1", -1, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                int value = result;
                assertEquals(value, -1);
            }
        });
        // type exception_boolean
        SensorsABTest.shareInstance().fastFetchABTest("1", true, new OnABTestReceivedData<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                assertEquals(true, result);
            }
        });
        // type exception_jsonObject
        final JSONObject object = new JSONObject();
        SensorsABTest.shareInstance().fastFetchABTest("1", object, new OnABTestReceivedData<JSONObject>() {
            @Override
            public void onResult(JSONObject jsonObject) {
                assertEquals(object, jsonObject);
            }
        });
        //
        SensorsABTest.shareInstance().fastFetchABTest("", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });

        SensorsABTest.shareInstance().fastFetchABTest(null, "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });

        SensorsABTest.shareInstance().fastFetchABTest("122", "", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "");
            }
        });

        SensorsABTest.shareInstance().fastFetchABTest("1", null, new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "1");
            }
        });

        SensorsABTest.shareInstance().fastFetchABTest(null, null, new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, null);
            }
        });
    }


    @Before
    public void init() {

        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SAConfigOptions configOptions = new SAConfigOptions("http://10.120.152.3:8106/sa?project=default");
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
                SensorsAnalyticsAutoTrackEventType.APP_END |
                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
                SensorsAnalyticsAutoTrackEventType.APP_CLICK)
                .enableTrackAppCrash()
                .enableLog(true)
                .enableJavaScriptBridge(false)
                .enableVisualizedAutoTrack(true)
                .enableVisualizedAutoTrackConfirmDialog(true);
        SensorsDataAPI.startWithConfigOptions(appContext, configOptions);

        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("http://abtesting-online.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=test");
        SensorsABTest.startWithConfigOptions(appContext, abTestConfigOptions);
    }
}