package com.osfans.mcpdict.UI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.osfans.mcpdict.Util.FontUtil;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MyMarker extends Marker {
    Paint mTextPaint;
    String mLabel;
    String mCity;
    int mSize;

    public MyMarker(MapView mapView, int color, String city, String yb, String js, int size) {
        super(mapView);
        mCity = city;
        setTextLabelForegroundColor(Color.WHITE);
        setTextLabelBackgroundColor(color);
        setTextLabelFontSize(24);
        setTextIcon(mCity);
        setAnchor(Marker.ANCHOR_LEFT, Marker.ANCHOR_BOTTOM);
        setTitle(city);
        setSubDescription(js);
        mLabel = yb;
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(24);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTypeface(FontUtil.getIPATypeface());
        mSize = size;
    }

    public void draw(final Canvas c, final MapView mapView) {
        draw(c, mapView, false);
    }

    public void draw( final Canvas c, final MapView mapView, boolean shadow) {
        super.draw(c, mapView, shadow);
        Point p = this.mPositionPixels;  // already provisioned by Marker
        mTextPaint.setAlpha((int)(getAlpha() * 255));
        c.drawText(mLabel, p.x, p.y+26, mTextPaint);
    }

    public void setZoomLevel(Double d) {
        boolean enabled = true;
        float alpha = 0f;
        switch (mSize) {
            case 5:
                break;
            case 4:
                enabled = (d >= 6);
                if (d > 5) alpha = 0.05f ;
                break;
            case 3:
                enabled = (d >= 7.5);
                if (d > 7) alpha = 0.05f ;
                break;
            case 2:
                enabled = (d >= 8.5);
                if (d > 8) alpha = 0.05f ;
                break;
            default:
                enabled = (d >= 9.5);
                if (d > 9) alpha = 0.05f ;
                break;
        }
        setAlpha(enabled ? 1f : alpha);
        //setEnabled(enabled);
    }
}

