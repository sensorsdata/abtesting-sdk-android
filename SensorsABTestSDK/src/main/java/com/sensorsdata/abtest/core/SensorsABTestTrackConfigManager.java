package com.sensorsdata.abtest.core;

import android.text.TextUtils;

import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.TrackConfig;
import com.sensorsdata.abtest.store.StoreManagerFactory;
import com.sensorsdata.abtest.util.CommonUtils;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class SensorsABTestTrackConfigManager {

    private static final String TAG = "SAB.SensorsABTestTrackConfigManager";
    private TrackConfig mTrackConfig = new TrackConfig();
    private static final TrackConfig mDefaultTrackConfig = new TrackConfig();
    private String mIdentifier = "";
    private final SensorsABTestPropertyPlugin mSensorsABTestPropertyPlugin = new SensorsABTestPropertyPlugin();

    private static class SensorsABTestTrackConfigManagerStaticNestedClass {
        private static final SensorsABTestTrackConfigManager INSTANCE = new SensorsABTestTrackConfigManager();
    }

    public static SensorsABTestTrackConfigManager getInstance() {
        return SensorsABTestTrackConfigManager.SensorsABTestTrackConfigManagerStaticNestedClass.INSTANCE;
    }

    public void clearCache() {
        mTrackConfig = new TrackConfig();
        StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.TRACK_CONFIG, "");
        updateSAPropertyPlugin();
    }

    public void saveTrackConfig(JSONObject configJson) {
        if (configJson == null) {
            mTrackConfig = new TrackConfig();
            mIdentifier = CommonUtils.getCurrentUserIdentifier();
            SALog.i(TAG, "Update Track Configs: Using Default Track Config.");
        } else {
            parseConfig(configJson);
            mIdentifier = configJson.optString("identifier", "");
            SALog.i(TAG, "Update Track Configs:\n" + JSONUtils.formatJson(configJson.toString()));
        }
        StoreManagerFactory.getStoreManager().putString(AppConstants.Property.Key.TRACK_CONFIG, configJson == null ? "" : configJson.toString());
        updateSAPropertyPlugin();
    }

    private void updateSAPropertyPlugin() {
        if (mTrackConfig.propertySetSwitch) {
            SensorsDataAPI.sharedInstance().registerPropertyPlugin(mSensorsABTestPropertyPlugin);
        } else {
            SensorsDataAPI.sharedInstance().unregisterPropertyPlugin(mSensorsABTestPropertyPlugin);
        }
    }

    public void loadTrackConfigCache() {
        String config = StoreManagerFactory.getStoreManager().getString(AppConstants.Property.Key.TRACK_CONFIG, "");
        if (!TextUtils.isEmpty(config)) {
            try {
                JSONObject configJson = new JSONObject(config);
                parseConfig(configJson);
                mIdentifier = configJson.optString("identifier", "");
            } catch (JSONException e) {
                SALog.printStackTrace(e);
            }
        }
        updateSAPropertyPlugin();
    }

    private void parseConfig(JSONObject configJson) {
        mTrackConfig = new TrackConfig();
        mTrackConfig.itemSwitch = configJson.optBoolean("item_switch", false);
        mTrackConfig.triggerSwitch = configJson.optBoolean("trigger_switch", false);
        mTrackConfig.propertySetSwitch = configJson.optBoolean("property_set_switch", false);
        JSONArray triggerContentExt = configJson.optJSONArray("trigger_content_ext");
        //清除默认值，使用配置覆盖其值
        mTrackConfig.triggerContentExtension.clear();
        if (triggerContentExt != null && triggerContentExt.length() != 0) {
            for (int index = 0; index < triggerContentExt.length(); index++) {
                mTrackConfig.triggerContentExtension.add(triggerContentExt.optString(index));
            }
        }
    }

    /**
     * 获取 Track 配置，需要用作只读。
     *
     * @return TrackConfig 事件配置对象
     */
    public TrackConfig getTrackConfig() {
        if (!CommonUtils.getCurrentUserIdentifier().equals(mIdentifier)) {
            return mDefaultTrackConfig;
        }
        return mTrackConfig.clone();
    }
}
