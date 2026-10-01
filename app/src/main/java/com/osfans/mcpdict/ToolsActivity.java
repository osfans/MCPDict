package com.osfans.mcpdict;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.osfans.mcpdict.Favorite.UserDB;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.Pref;

/**
 * 音典小工具入口。
 *
 * 界面文件打包在 assets/yindian-tools 中；所有数据通过 ToolsBridge 直接读取
 * 音典自带 mcpdict.db，不发起网络请求。
 */
public class ToolsActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.setLocale();
        App.setActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        // 正常从 MainActivity 进入时数据库已经初始化。这里仍做幂等初始化，
        // 以便系统恢复 Activity 时也能独立工作。
        UserDB.initialize(this);
        DB.initialize(this);
        DB.initFQ();
        Orthography.initialize(getResources());

        // Keep pronunciation rendering identical to the main result list.
        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));

        Toolbar toolbar = findViewById(R.id.tools_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.yindian_tools);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.tools_webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccess(true);
        settings.setBlockNetworkLoads(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // 只把接口暴露给 APK 内置的受信任页面。
        webView.addJavascriptInterface(new ToolsBridge(this), "YindianBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // 小工具没有外链需求，阻止页面跳出本地 assets。
                return !url.startsWith("file:///android_asset/yindian-tools/");
            }
        });
        webView.loadUrl("file:///android_asset/yindian-tools/index.html?embedded=1");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("YindianBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
