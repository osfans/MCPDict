package com.osfans.mcpdict;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

import java.util.Locale;

public class HelpActivity extends FragmentActivity {
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale.setDefault(Locale.KOREA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);

        WebView webview = findViewById(R.id.web_view_help);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("file:///android_asset/help/index.htm");
    }
}
