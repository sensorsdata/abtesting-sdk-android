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
            "\t\"error\": \"xxxxxx\",\n" +
            "\t\"results\": [{\n" +
            "\t\t\t\"abtest_experiment_id\": \"100\",\n" +
            "\t\t\t\"abtest_experiment_group_id\": \"123\",\n" +
            "\t\t\t\"is_control_group\": false,\n" +
            "\t\t\t\"is_white_list\": true,\n" +
            "\t\t\t\"variables\": [{\n" +
            "\t\t\t\t\"name\": \"textColor\",\n" +
            "\t\t\t\t\"value\": \"1\",\n" +
            "\t\t\t\t\"type\": \"INTEGER\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"abtest_experiment_id\": \"101\",\n" +
            "\t\t\t\"abtest_experiment_group_id\": \"123\",\n" +
            "\t\t\t\"is_control_group\": false,\n" +
            "\t\t\t\"is_white_list\": true,\n" +
            "\t\t\t\"variables\": [{\n" +
            "\t\t\t\t\t\"name\": \"textSize\",\n" +
            "\t\t\t\t\t\"value\": \"1\",\n" +
            "\t\t\t\t\t\"type\": \"INTEGER\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"name\": \"textColor\",\n" +
            "\t\t\t\t\t\"value\": \"1\",\n" +
            "\t\t\t\t\t\"type\": \"INTEGER\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"name\": \"color3\",\n" +
            "\t\t\t\t\t\"value\": \"true\",\n" +
            "\t\t\t\t\t\"type\": \"BOOLEAN\"\n" +
            "\t\t\t\t}\n" +
            "\t\t\t]\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}";

    @Test
    public void testApi() {
        testFetchCacheABTest();
        testFastFetchABTest();
    }

    @Test
    public void testFetchCacheABTest() {
        // normal
        String value = SensorsABTest.shareInstance().fetchCacheABTest("color2", "default");
        assertEquals(value, "111");

        // type exception_int
        int valueNumber = SensorsABTest.shareInstance().fetchCacheABTest("color1", -22);
        assertEquals(valueNumber, 1);

        // type exception_boolean
        boolean valueBoolean = SensorsABTest.shareInstance().fetchCacheABTest("color3", false);
        assertEquals(valueBoolean, true);

        // type exception_json
        JSONObject jsonObject = new JSONObject();
        JSONObject valueJsonObject = SensorsABTest.shareInstance().fetchCacheABTest("color1", jsonObject);
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
        SensorsABTest.shareInstance().asyncFetchABTest("color1", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "default");
            }
        });
        // type exception_int
        SensorsABTest.shareInstance().asyncFetchABTest("color1", -1, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                int value = result;
                assertEquals(value, 1);
            }
        });
        // type exception_boolean
        SensorsABTest.shareInstance().asyncFetchABTest("color1", true, new OnABTestReceivedData<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                assertEquals(true, result);
            }
        });
        // type exception_jsonObject
        final JSONObject object = new JSONObject();
        SensorsABTest.shareInstance().asyncFetchABTest("color1", object, new OnABTestReceivedData<JSONObject>() {
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

        SensorsABTest.shareInstance().asyncFetchABTest("color1", null, new OnABTestReceivedData<Integer>() {
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
        SensorsABTest.shareInstance().asyncFetchABTest("color1", -1, 5, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                SALog.i("testAsyncFetchABTestWithTimeOut", " value: " + result);
            }
        });
    }

    @Test
    public void testFastFetchABTest() {
        // normal
        SensorsABTest.shareInstance().fastFetchABTest("color1", "default", new OnABTestReceivedData<String>() {
            @Override
            public void onResult(String result) {
                assertEquals(result, "1");
            }
        });
        // type exception_int
        SensorsABTest.shareInstance().fastFetchABTest("color1", -1, new OnABTestReceivedData<Integer>() {
            @Override
            public void onResult(Integer result) {
                int value = result;
                assertEquals(value, -1);
            }
        });
        // type exception_boolean
        SensorsABTest.shareInstance().fastFetchABTest("color1", true, new OnABTestReceivedData<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                assertEquals(true, result);
            }
        });
        // type exception_jsonObject
        final JSONObject object = new JSONObject();
        SensorsABTest.shareInstance().fastFetchABTest("color1", object, new OnABTestReceivedData<JSONObject>() {
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

        SensorsABTest.shareInstance().fastFetchABTest("color1", null, new OnABTestReceivedData<String>() {
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
                .enableVisualizedAutoTrack(true);
        SensorsDataAPI.startWithConfigOptions(appContext, configOptions);

        SensorsABTestConfigOptions abTestConfigOptions = new SensorsABTestConfigOptions("http://abtesting.saas.debugbox.sensorsdata.cn/api/v100/abtest/online/results?project-key=0a551836f92dc3292be545c748f3f462e2d43bc9");
        SensorsABTest.startWithConfigOptions(appContext, abTestConfigOptions);

        try {
            JSONObject jsonObject = new JSONObject(sMockData);
            JSONArray jsonArray = jsonObject.optJSONArray("results");
            SensorsABTestCacheManager.getInstance().getExperimentsFromMemoryCache(jsonArray.toString());
            SensorsABTestCacheManager.getInstance().saveExperiments2DiskCache(jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}