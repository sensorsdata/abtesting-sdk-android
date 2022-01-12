package com.sensorsdata.sensorsabtest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sensorsdata.abtest.OnABTestReceivedData;
import com.sensorsdata.abtest.SensorsABTest;
import com.sensorsdata.abtest.core.SensorsABTestCacheManager;
import com.sensorsdata.abtest.entity.Experiment;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.analytics.android.sdk.PropertyBuilder;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextView;
    private Spinner mSpinner, mTypeSpinner, mDefaultValueSpinner;
    private String mExperimentId, mExperimentType;
    private ArrayAdapter<Object> mAdapter = null;
    private Object mExperimentDefaultValue;

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
        findViewById(R.id.bt_h5).setOnClickListener(this);
        findViewById(R.id.bt_distinct_id).setOnClickListener(this);
        findViewById(R.id.clear_cache).setOnClickListener(this);
        findViewById(R.id.clear_screen).setOnClickListener(this);
        mSpinner = findViewById(R.id.spinner_experiment_id);
        mTypeSpinner = findViewById(R.id.spinner_type);
        mDefaultValueSpinner = findViewById(R.id.spinner_default_value);
        mTextView = findViewById(R.id.tv_content);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        initExperimentId();
        initType();
    }

    private void initExperimentId() {
        String experiments = StoreManagerFactory.getStoreManager().getString("key_experiment_with_distinct_id", "");
        ConcurrentHashMap<String, Experiment> experimentConcurrentHashMap = SensorsABTestCacheManager.getInstance().getExperimentsFromMemoryCache(experiments);
        Set<String> experimentIds = experimentConcurrentHashMap.keySet();
        Iterator<String> iterator = experimentIds.iterator();
        final List<String> items = new ArrayList<>();
        while (iterator.hasNext()) {
            items.add(iterator.next());
        }
        items.add("242");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExperimentId = items.get(position);
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
                mAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, new JSONObject[]{PropertyBuilder.newInstance().append("age", "1").toJSONObject()});
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
                handleInvoke(InvokeEnum.AsyncFetchABTest, 200);
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
            case R.id.clear_screen:
                mTextView.setText("");
                mTextView.scrollTo(0, 0);
                lineIndex = 1;
                break;
            case R.id.clear_cache:
                SensorsABTestCacheManager.getInstance().loadExperimentsFromCache("");
                SensorsABTestCacheManager.getInstance().saveFuzzyExperiments(null);
                break;
        }
    }


    private void handleInvoke(InvokeEnum invokeEnum, int timeoutMillseconds) {
        if (mExperimentDefaultValue instanceof Integer) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mExperimentId, (Integer) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mExperimentId, (Integer) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                int result = (int) SensorsABTest.shareInstance().fetchCacheABTest(mExperimentId, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof String) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mExperimentId, (String) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<String>() {
                    @Override
                    public void onResult(String result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mExperimentId, (String) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<String>() {
                    @Override
                    public void onResult(String result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                String result = (String) SensorsABTest.shareInstance().fetchCacheABTest(mExperimentId, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof Boolean) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mExperimentId, (Boolean) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mExperimentId, (Boolean) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                boolean result = (Boolean) SensorsABTest.shareInstance().fetchCacheABTest(mExperimentId, mExperimentDefaultValue);
                refreshLogView("FetchCacheABTest: " + result);
            }
        } else if (mExperimentDefaultValue instanceof JSONObject) {
            if (invokeEnum == InvokeEnum.AsyncFetchABTest) {
                SensorsABTest.shareInstance().asyncFetchABTest(mExperimentId, (JSONObject) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<JSONObject>() {
                    @Override
                    public void onResult(JSONObject result) {
                        refreshLogView("asyncFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FastFetchABTest) {
                SensorsABTest.shareInstance().fastFetchABTest(mExperimentId, (JSONObject) mExperimentDefaultValue, timeoutMillseconds, new OnABTestReceivedData<JSONObject>() {
                    @Override
                    public void onResult(JSONObject result) {
                        refreshLogView("fastFetchABTest: " + result);
                    }
                });
            } else if (invokeEnum == InvokeEnum.FetchCacheABTest) {
                JSONObject result = (JSONObject) SensorsABTest.shareInstance().fetchCacheABTest(mExperimentId, mExperimentDefaultValue);
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