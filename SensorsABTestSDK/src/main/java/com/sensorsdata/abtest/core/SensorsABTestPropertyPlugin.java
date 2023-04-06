package com.sensorsdata.abtest.core;

import android.text.TextUtils;

import com.sensorsdata.abtest.entity.TrackConfig;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.plugin.property.SAPropertyPlugin;
import com.sensorsdata.analytics.android.sdk.plugin.property.beans.SAPropertiesFetcher;

import org.json.JSONArray;

import java.util.Set;

public class SensorsABTestPropertyPlugin extends SAPropertyPlugin {

    private static final String TAG = "SAB.SensorsABTestPropertyPlugin";

    @Override
    public void properties(SAPropertiesFetcher fetcher) {
        try {
            if (TextUtils.equals(fetcher.getProperties().optString("$lib", ""), "js")) {
                SALog.i(TAG, "The event from H5, will not add abtest_result and abtest_dispatch_result.");
                return;
            }
            TrackConfig trackConfig = SensorsABTestTrackConfigManager.getInstance().getTrackConfig();
            if (trackConfig.propertySetSwitch) {
                Set<String> uniqueIdSet = SensorsABTestCacheManager.getInstance().getDispatchUniqueIdResult();
                JSONArray jsonArray = transfer2Json(uniqueIdSet);
                if (jsonArray.length() > 0) {
                    fetcher.getProperties().put("abtest_dispatch_result", jsonArray);
                }

                uniqueIdSet = SensorsABTestTrackHelper.getInstance().getCachedABTestUniqueIdSet();
                JSONArray cachedABJsonArray = transfer2Json(uniqueIdSet);
                if (cachedABJsonArray.length() > 0) {
                    fetcher.getProperties().put("abtest_result", cachedABJsonArray);
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private JSONArray transfer2Json(Set<String> set) {
        JSONArray jsonArray = new JSONArray();
        if (set != null && set.size() > 0) {
            for (String item : set) {
                jsonArray.put(item);
            }
        }
        return jsonArray;
    }
}
