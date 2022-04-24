package com.osfans.mcpdict;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class MyWeb {
    MyWebView mWebView;

    public MyWeb(MyWebView view) {
        mWebView = view;
    }

    @JavascriptInterface
    public void showMap(String hz) {
        new MyMapView(mWebView.getContext(), hz).show();
    }

    @JavascriptInterface
    public void showFavorite(String hz, int favorite, String comment) {
        if (favorite == 1) {
            FavoriteDialogs.view(hz, comment);
        } else {
            FavoriteDialogs.add(hz);
        }
    }

    @JavascriptInterface
    public void onClick(String hz, String lang, String raw, int favorite, String comment, int x, int y) {
        Object obj = mWebView.getTag();
        if (obj == null) return;
        if (!(obj instanceof ResultFragment)) return;
        ResultFragment resultFragment = (ResultFragment) obj;
        resultFragment.setEntry(hz, lang, raw, favorite==1, comment);
        resultFragment.showContextMenu(x*DictApp.getScale(), y*DictApp.getScale());
    }

}

