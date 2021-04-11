/*
 * Created by zhangxiangwei on 2020/09/15.
 * Copyright 2015－2021 Sensors Data Inc.
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

package com.sensorsdata.abtest.core;

import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import com.sensorsdata.abtest.entity.AppConstants;
import com.sensorsdata.abtest.entity.H5Request;
import com.sensorsdata.analytics.android.sdk.SALog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

class SensorsABTestH5Helper implements WebViewJavascriptBridge {

    private static final String TAG = "SAB.SensorsABTestH5Helper";
    private String mMessage;
    private WeakReference<View> mWebView;

    public SensorsABTestH5Helper(WeakReference<View> view, String message) {
        this.mWebView = view;
        this.mMessage = message;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handlerJSMessage() {
        try {
            SALog.i(TAG, "handlerJSMessage enter.");
            if (mWebView == null || mWebView.get() == null) {
                SALog.i(TAG, "mWebView is null");
                return;
            }

            if (TextUtils.isEmpty(mMessage)) {
                SALog.i(TAG, "content is null");
                return;
            }

            SALog.i(TAG, "handlerJSMessage content: " + mMessage);
            JSONObject jsonObject = new JSONObject(mMessage);
            String callType = jsonObject.optString("callType");
            if (TextUtils.equals("abtest", callType)) {
                final H5Request request = new H5Request();
                JSONObject data = jsonObject.optJSONObject("data");
                if (data != null) {
                    request.messageId = data.optString("message_id");
                    new SensorsABTestApiRequestHelper<>().requestExperiments(new IApiCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            if (TextUtils.isEmpty(s)) {
                                SALog.i(TAG, "response is null");
                                return;
                            }

                            JSONObject object = new JSONObject();
                            try {
                                object.put("message_id", request.messageId);
                                JSONObject dataObject = new JSONObject(s);
                                object.put("data", dataObject);
                                SALog.i(TAG, "onSuccess callJS object: " + object.toString());
                                callJs(string2Base64(object.toString()));
                            } catch (JSONException e) {
                                SALog.printStackTrace(e);
                            }

                            try {
                                JSONObject originObject = new JSONObject(s);
                                JSONArray array = originObject.optJSONArray("results");
                                String status = originObject.optString("status");
                                if (TextUtils.equals(AppConstants.AB_TEST_SUCCESS, status)) {
                                    SensorsABTestCacheManager.getInstance().loadExperimentsFromCache(array != null ? array.toString() : "");
                                }
                            } catch (JSONException e) {
                                SALog.printStackTrace(e);
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, String message) {
                            JSONObject object = new JSONObject();
                            try {
                                object.put("message_id", request.messageId);
                                SALog.i(TAG, "onFailure callJS object: " + object.toString());
                                callJs(string2Base64(object.toString()));
                            } catch (JSONException e) {
                                SALog.printStackTrace(e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    @Override
    public void callJs(final String message) {
        if (mWebView != null) {
            final View view = mWebView.get();
            if (view != null) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        String result = "'abtest','" + message + "'";
                        invokeWebViewLoad(view, "loadUrl", new Object[]{"javascript:window.sensorsdata_app_call_js(" + result + ")"}, new Class[]{String.class});
                    }
                });
            }
        }
    }

    private static void invokeWebViewLoad(View webView, String methodName, Object[] params, Class[] paramTypes) {
        try {
            Class<?> clazz = webView.getClass();
            Method loadMethod = clazz.getMethod(methodName, paramTypes);
            loadMethod.invoke(webView, params);
        } catch (Exception var6) {
            SALog.printStackTrace(var6);
        }
    }

    private static String string2Base64(String s) {
        return Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
    }
}
