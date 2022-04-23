package com.osfans.mcpdict;

import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

public class MyWeb {
    MyWebView myWebView;

    public MyWeb(MyWebView view) {
        myWebView = view;
    }

    @JavascriptInterface
    public void showMap(String hz) {
        new MyMapView(myWebView.getContext(), hz).show();
    }

    @JavascriptInterface
    public void showFavorite(String hz, int isFavorite, String comment) {
        if (isFavorite == 1) {
            FavoriteDialogs.view(hz, comment);
        } else {
            FavoriteDialogs.add(hz);
        }
    }

    @JavascriptInterface
    public void onClick() {
        BaseActivity activity = (BaseActivity) myWebView.getTag();
        View view = myWebView;
        activity.registerForContextMenu(view);
        activity.openContextMenu(view);
        activity.unregisterForContextMenu(view);
    }

}

