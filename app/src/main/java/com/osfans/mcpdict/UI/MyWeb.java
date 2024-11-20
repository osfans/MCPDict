package com.osfans.mcpdict.UI;

import android.webkit.JavascriptInterface;

import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.ResultFragment;
import com.osfans.mcpdict.Utils;

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
        getFragment().requireActivity().runOnUiThread(() -> Utils.showDict(getFragment().getContext(), i, DisplayHelper.formatPopUp(hz, i, text)));
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

