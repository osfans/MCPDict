package com.osfans.mcpdict.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class WebView extends android.webkit.WebView {
    @SuppressLint("SetJavaScriptEnabled")
    public WebView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        addJavascriptInterface(new Web(this), "mcpdict");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true);
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if (isNightMode()) {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON);
                } else {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF);
                }
            }
        }
    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}

