package com.osfans.mcpdict;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

public class InfoActivity extends FragmentActivity {
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        Intent intent = getIntent();
        String lang = intent.getStringExtra("lang");

        MyWebView webView = findViewById(R.id.web_view_help);
        StringBuilder sb = new StringBuilder();
        sb.append("<style>\n" +
                "  @font-face {\n" +
                "      font-family: ipa;\n" +
                "      src: url(\"file:///android_res/font/ipa.ttf\")\n" +
                "  }\n" +
                "  body {font-size: 16px}\n" +
                "  h1 {font-size: 24px; color: #9D261D}\n" +
                "  h2 {font-size: 20px; color: #000080; text-indent: 10px}\n" +
                " </style>");
        sb.append(DB.getIntroText(this, lang));
        webView.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);
    }

}