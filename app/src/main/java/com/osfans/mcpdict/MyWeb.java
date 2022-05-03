package com.osfans.mcpdict;

import android.webkit.JavascriptInterface;

public class MyWeb {
    MyWebView mWebView;

    public MyWeb(MyWebView view) {
        mWebView = view;
    }

    private ResultFragment getFragment() {
        return (ResultFragment) mWebView.getTag();
    }

    @JavascriptInterface
    public void showMap(String hz) {
        getFragment().showMap(hz);
    }

    @JavascriptInterface
    public void showDict(String hz, int i, String text) {
        Utils.showDict(mWebView.getContext(), Utils.formatPopUp(hz, i, text));
    }

    @JavascriptInterface
    public void showFavorite(String hz, int favorite, String comment) {
        getFragment().showFavorite(hz, favorite == 1, comment);
    }

    @JavascriptInterface
    public void onClick(String hz, String lang, String raw, int favorite, String comment, int x, int y) {
        ResultFragment resultFragment = getFragment();
        resultFragment.setEntry(hz, lang, raw, favorite==1, comment);
        resultFragment.showContextMenu(x* Utils.getScale(), y* Utils.getScale());
    }

}

