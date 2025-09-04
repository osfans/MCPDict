package com.osfans.mcpdict.UI;

import android.webkit.JavascriptInterface;

import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.ResultFragment;
import com.osfans.mcpdict.Utils;

public class Web {
    WebView mWebView;

    public Web(WebView view) {
        mWebView = view;
    }

    private ResultFragment getFragment() {
        return (ResultFragment) mWebView.getTag();
    }
}

