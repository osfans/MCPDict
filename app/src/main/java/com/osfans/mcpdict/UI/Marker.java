package com.osfans.mcpdict.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.core.graphics.ColorUtils;

import com.osfans.mcpdict.Util.FontUtil;

import org.osmdroid.views.MapView;

public class Marker extends org.osmdroid.views.overlay.Marker {
    TextPaint mIPAPaint, mCityPaint;
    String mIPA;
    int mSize;
    Drawable mIconDot, mIconName;

    public Marker(MapView mapView, int color, String city, String yb, String js, int size) {
        super(mapView);

        setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER);
        int fontSize = getTextLabelFontSize() * 4 / 3;
        setTextLabelFontSize(fontSize);

        setTextLabelBackgroundColor(color);
        setTextLabelForegroundColor(Color.WHITE);
        setTextIcon(city);
        mIconName = getIcon();

        int newColorWithAlpha =  ColorUtils.setAlphaComponent(color, 0xFF);
        setTextLabelForegroundColor(newColorWithAlpha);
        setTextLabelBackgroundColor(Color.TRANSPARENT);
        setTextIcon("▪");
        mIconDot = getIcon();

        setTitle(city);
        setSubDescription(js);
        mIPA = yb.replaceAll("<.*?>", "");

        mIPAPaint = new TextPaint();
        mIPAPaint.setColor(Color.BLACK);
        mIPAPaint.setTextSize(fontSize);
        mIPAPaint.setAntiAlias(true);
        mIPAPaint.setTextAlign(Paint.Align.CENTER);
        mIPAPaint.setTypeface(FontUtil.getIPATypeface());

        mCityPaint = new TextPaint();
        mCityPaint.setColor(color);
        mCityPaint.setTextSize(fontSize);
        mCityPaint.setAntiAlias(true);
        mCityPaint.setTextAlign(Paint.Align.CENTER);
        mCityPaint.setTypeface(FontUtil.getDictTypeface());
        mSize = size;
    }

    private boolean isIPAVisible(double d) {
        boolean enabled = true;
        switch (mSize) {
            case 5:
                break;
            case 4:
                enabled = (d >= 6);
                break;
            case 3:
                enabled = (d >= 7.5);
                break;
            case 2:
                enabled = (d >= 8.5);
                break;
            default:
                enabled = (d >= 9.5);
                break;
        }
        return enabled;
    }

    public void draw(final Canvas c, final MapView mapView, boolean shadow) {
        super.draw(c, mapView, shadow);
        Point p = this.mPositionPixels;  // already provisioned by Marker
        double level = mapView.getZoomLevelDouble();
        Drawable newIcon = (level >= 10 - mSize * mSize / 7d) ?  mIconName : mIconDot;
        if (newIcon != getIcon()) setIcon(newIcon);
        if (TextUtils.isEmpty(mIPA)) return;
        if (isIPAVisible(level)) {
            c.drawText(mIPA, p.x, p.y + getTextLabelFontSize() * 3f / 2, mIPAPaint);
        }
    }
}

