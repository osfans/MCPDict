package com.osfans.mcpdict;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

public class HelpActivity extends FragmentActivity {
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);

        MyWebView webView = findViewById(R.id.web_view_help);
        webView.loadUrl("file:///android_asset/help/index.htm");
    }
}
