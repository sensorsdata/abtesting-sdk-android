package com.sensorsdata.sensorsabtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import java.util.Arrays;
import java.util.List;

public class H5VisualTestActivity extends Activity {

    private List<String> mItems = Arrays.asList(
            "http://jjcheng123.gitee.io/test_page/ABTest.html",
            "自定义");
    private WebView mWebView;
    private static String SA_KEY_WEBVIEW_URL = "SA_KEY_WEBVIEW_URL";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5_visual);
        mWebView = findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.loadUrl("file:///android_asset/visual_h5_test/test.html");
        //mWebView.loadUrl(mItems.get(0));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        for (int i = 0; i < mItems.size(); i++) {
            menu.add(Menu.NONE, i, 0, mItems.get(i));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == mItems.size() - 1) {
            showInputDialog();
            return true;
        }
        handlerClick(mItems.get(item.getItemId()));
        return true;
    }

    private void handlerClick(String url) {
        if (TextUtils.isEmpty(url))
            return;
        if (!TextUtils.equals(url, mWebView.getUrl())) {
            mWebView.loadUrl(url);
            Toast.makeText(H5VisualTestActivity.this, "select url: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputDialog() {
        final EditText editText = new EditText(H5VisualTestActivity.this);
        editText.setHint("忽略大小写、空格");
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(H5VisualTestActivity.this);
        inputDialog.setTitle("url").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        H5VisualTestActivity.this.handlerClick(editText.getText().toString().toLowerCase().replace(" ", ""));
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SensorsDataUtils.handleSchemeUrl(this, intent);
    }
}
