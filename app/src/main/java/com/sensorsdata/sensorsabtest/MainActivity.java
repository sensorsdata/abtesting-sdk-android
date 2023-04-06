package com.sensorsdata.sensorsabtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.core.SensorsABTestCacheManager;
import com.sensorsdata.abtest.core.SensorsABTestTrackConfigManager;
import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.abtest.util.TaskRunner;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextView;
    private Spinner mSpinner, mTypeSpinner, mDefaultValueSpinner;
    private String mParamName, mExperimentType;
    private ArrayAdapter<Object> mAdapter = null;
    private Object mExperimentDefaultValue;
    private AlertDialog mConfigDialog;
    private EditText mTrackConfigEditText;
    private AlertDialog mCustomParamNameDialog;
    private EditText mCustomParamNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        findViewById(R.id.bt_fetchCacheABTest).setOnClickListener(this);
        findViewById(R.id.bt_asyncFetchABTest).setOnClickListener(this);
        findViewById(R.id.bt_asyncFetchABTestWithTimeout).setOnClickListener(this);
        findViewById(R.id.bt_fastFetchABTest).setOnClickListener(this);
        findViewById(R.id.bt_fastFetchABTestWithTimeout).setOnClickListener(this);
        findViewById(R.id.bt_login).setOnClickListener(this);
        findViewById(R.id.bt_logout).setOnClickListener(this);
        findViewById(R.id.bt_h5).setOnClickListener(this);
        findViewById(R.id.bt_distinct_id).setOnClickListener(this);
        findViewById(R.id.clear_cache).setOnClickListener(this);
        findViewById(R.id.clear_screen).setOnClickListener(this);
        findViewById(R.id.bt_update_custom_ids).setOnClickListener(this);
        findViewById(R.id.bt_update_track_config).setOnClickListener(this);
        mSpinner = findViewById(R.id.spinner_experiment_id);
        mTypeSpinner = findViewById(R.id.spinner_type);
        mDefaultValueSpinner = findViewById(R.id.spinner_default_value);
        mTextView = findViewById(R.id.tv_content);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        TaskRunner.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initExperimentId();
                initType();
            }
        }, 3000);
    }

    private void initExperimentId() {
        String experiments = StoreManagerFactory.getStoreManager().getString("key_experiment_with_distinct_id", "");
        ConcurrentHashMap<String, Experiment> experimentConcurrentHashMap = SensorsABTestCacheManager.getInstance().updateExperimentsMemoryCache(experiments);
        Set<String> experimentIds = experimentConcurrentHashMap.keySet();
        Iterator<String> iterator = experimentIds.iterator();
        final List<String> items = new ArrayList<>();
        while (iterator.hasNext()) {
            items.add(iterator.next());
        }
        items.add("not_exists_param_name");
        items.add("自定义");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mParamName = items.get(position);
                if (mParamName.equals("自定义")) {
                    if (mCustomParamNameDialog == null) {
                        mCustomParamNameEditText = new EditText(MainActivity.this);
                        mCustomParamNameEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        mCustomParamNameEditText.setHint("请输入试验参数名");
                        mCustomParamNameDialog = new AlertDialog.Builder(MainActivity.this).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mParamName = mCustomParamNameEditText.getText().toString().trim();
                                mTextView.append("自定义试验参数名：" + mParamName);
                            }
                        }).setView(mCustomParamNameEditText).setTitle("修改试验参数名").create();
                    }
                    mCustomParamNameDialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initType() {
        final String[] types = {"int", "String", "boolean", "json"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExperimentType = types[position];
                initDefaultValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initDefaultValue() {
        switch (mExperimentType) {
            case "int":
                mAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, new Integer[]{-1, 0, 1});
                break;
            case "String":
                mAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, new String[]{"first", "second", "third"});
                break;
            case "boolean":
                mAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, new Boolean[]{false, true});
                break;
            case "json":
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("age", "1");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                mAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, new JSONObject[]{jsonObject});
                break;
        }
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDefaultValueSpinner.setAdapter(mAdapter);
        mDefaultValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExperimentDefaultValue = mAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_fetchCacheABTest:
                handleInvoke(InvokeEnum.FetchCacheABTest, -1);
                break;

            case R.id.bt_asyncFetchABTest:
                handleInvoke(InvokeEnum.AsyncFetchABTest, -1);
                break;

            case R.id.bt_asyncFetchABTestWithTimeout:
                handleInvoke(InvokeEnum.AsyncFetchABTest, 1);
                break;

            case R.id.bt_fastFetchABTest:
                handleInvoke(InvokeEnum.FastFetchABTest, -1);
                break;

            case R.id.bt_fastFetchABTestWithTimeout:
                handleInvoke(InvokeEnum.FastFetchABTest, 200);
                break;
            case R.id.bt_h5:
                startActivity(new Intent(this, H5VisualTestActivity.class));
                break;
            case R.id.bt_distinct_id:
                final String uriString = "safb057fa8://abtest?sensors_abtest_url=http://10.120.41.143:8107/api/v2/sa/abtest/experiments/distinct_id?feature_code=xxx";
                SensorsDataUtils.handleSchemeUrl(this, new Intent(Intent.ACTION_VIEW, Uri.parse(uriString)));
                break;
            case R.id.bt_login:
                SensorsDataAPI.sharedInstance().login("login_test");
                break;
            case R.id.bt_logout:
                SensorsDataAPI.sharedInstance().logout();
                break;
            case R.id.clear_screen:
                mTextView.setText("");
                mTextView.scrollTo(0, 0);
                lineIndex = 1;
                break;
            case R.id.clear_cache:
                SensorsABTestCacheManager.getInstance().updateExperimentsCache("");
                SensorsABTestCacheManager.getInstance().saveFuzzyExperiments(null);
                SensorsABTestTrackConfigManager.getInstance().saveTrackConfig(new JSONObject());
                StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.ABTEST_TRIGGER, "");
                break;
            case R.id.bt_update_custom_ids:
                Map<String, String> ids = new HashMap<>();
                Random random = new Random();
                ids.put("key_a_" + random.nextInt(100), "value_a_" + random.nextInt(100));
                ids.put("key_b_" + random.nextInt(100), "value_b_" + random.nextInt(100));
                SensorsABTest.shareInstance().setCustomIDs(ids);
                break;
            case R.id.bt_update_track_config:
                if (mConfigDialog == null) {

                    final String config = " {\n" +
                            "        \"item_switch\": false,\n" +
                            "        \"trigger_switch\": true,\n" +
                            "        \"property_set_switch\": true,\n" +
                            "        \"trigger_content_ext\":\n" +
                            "        [\n" +
                            "            \"abtest_experiment_version\",\n" +
                            "            \"abtest_experiment_result_id\"\n" +
                            "        ]\n" +
                            "    }";
                    mTrackConfigEditText = new EditText(this);
                    mTrackConfigEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    mTrackConfigEditText.setText(config);
                    mConfigDialog = new AlertDialog.Builder(this).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                JSONObject configJson = new JSONObject(mTrackConfigEditText.getText().toString());
                                SensorsABTestTrackConfigManager.getInstance().saveTrackConfig(configJson);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).setView(mTrackConfigEditText).setTitle("更改 TrackConfig 配置").create();
                }
                mConfigDialog.show();

                break;
        }
    }


    private void handleInvoke(InvokeEnum invokeEnum, int timeoutMillseconds) {
        if (mExperimentDefaultValue instanceof Integer) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mParamName, (Integer) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mParamName, (Integer) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                int result = (int) SensorsABTest.shareInstance().fetchCacheABTest(mParamName, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof String) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mParamName, (String) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<String>() {
                    @Override
                    public void onResult(String result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mParamName, (String) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<String>() {
                    @Override
                    public void onResult(String result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                String result = (String) SensorsABTest.shareInstance().fetchCacheABTest(mParamName, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof Boolean) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mParamName, (Boolean) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mParamName, (Boolean) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                boolean result = (Boolean) SensorsABTest.shareInstance().fetchCacheABTest(mParamName, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof JSONObject) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mParamName, (JSONObject) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<JSONObject>() {
                    @Override
                    public void onResult(JSONObject result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mParamName, (JSONObject) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<JSONObject>() {
                    @Override
                    public void onResult(JSONObject result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                JSONObject result = (JSONObject) SensorsABTest.shareInstance().fetchCacheABTest(mParamName, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        }
    }

    private int lineIndex = 1;

    private void refreshLogView(String msg) {
        String text = "";
        if (TextUtils.isEmpty(mTextView.getText())) {
            text += (lineIndex++) + "." + msg;
        } else {
            text += "\n" + (lineIndex++) + "." + msg;
        }
        mTextView.append(text);
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                if (mTextView.canScrollVertically(1)) {
                    mTextView.scrollBy(0, 1);
                    mTextView.post(this);
                }
            }
        });
    }

    enum InvokeEnum {
        AsyncFetchABTest,
        FastFetchABTest,
        FetchCacheABTest
    }

}