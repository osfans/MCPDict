package com.osfans.mcpdict;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class AutoWebView extends WebView {
    public AutoWebView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (isNightMode()) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON);
            } else {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF);
            }
        }
    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}

