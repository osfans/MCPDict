package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.FontUtil;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class Marker extends org.osmdroid.views.overlay.Marker {
    TextPaint mIPAPaint;
    String mIPA;
    int mSize;
    Drawable mIconDot, mIconName;
    float mBaseline;

    public void makeIcon() {
        int fontSize = getTextLabelFontSize();
        int color = getTextLabelForegroundColor();
        int subColor = getTextLabelBackgroundColor();

        mIPAPaint = new TextPaint();
        mIPAPaint.setColor(Color.BLACK);
        mIPAPaint.setTextSize(fontSize);
        mIPAPaint.setAntiAlias(true);
        mIPAPaint.setTextAlign(Paint.Align.CENTER);
        mIPAPaint.setTypeface(FontUtil.getIPATypeface());
        mBaseline = (int) (-mIPAPaint.ascent() + 0.5f);

        mIconName = TextDrawable.builder()
                .beginConfig()
                .withBorder(3)
                .fontSize(getTextLabelFontSize())
                .useFont(FontUtil.getDictTypeface())
                .wrapContent(true)
                .endConfig()
                .roundRect(5).build(getTitle(), color, subColor);
        
        int radius = fontSize / 2;
        int width = radius * 2;
        int height = radius * 2;
        final Bitmap imageDot = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cDot = new Canvas(imageDot);
        Paint p = new Paint();
        p.setColor(color);
//        p.setAlpha(0xB0);
        if (color == subColor) {
            // draw full circle
            cDot.drawCircle(width / 2f, height / 2f, radius, p);
        } else {
            // draw left half
            cDot.drawArc(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius, 90, 180, true, p);
            // draw right half
            p.setColor(subColor);
            cDot.drawArc(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius, 270, 180, true, p);
        }
        mIconDot = new BitmapDrawable(mResources, imageDot);
        setIcon(mIconDot);
        setAnchor(ANCHOR_CENTER, ANCHOR_CENTER);
    }

    public Marker(MapView mapView, Cursor cursor, String yb, String js) {
        super(mapView);
        String label = cursor.getString(0);
        GeoPoint point = GeoPoint.fromInvertedDoubleString(cursor.getString(1), ',');
        setPosition(point);
        int size = cursor.getInt(2);
        String colors = cursor.getString(3);
        int color = DB.parseColor(colors, 0);
        int subColor = DB.parseColor(colors, 1);

        setTitle(label);
        if (!TextUtils.isEmpty(js)) setSnippet(js);
        mIPA = yb.replaceAll("<.*?>", "");
        mSize = size;
        setTextLabelForegroundColor(color);
        setTextLabelBackgroundColor(subColor);
        int fontSize = getTextLabelFontSize() * 4 / 3;
        setTextLabelFontSize(fontSize);
        makeIcon();
    }

    private boolean isIPAVisible(double d) {
        return d >= 11 - mSize * mSize / 7d;
    }

    public int getSize() {
        return mSize;
    }

    public void draw(final Canvas c, final MapView mapView, boolean shadow) {
        super.draw(c, mapView, shadow);
        Point p = this.mPositionPixels;  // already provisioned by Marker
        double level = mapView.getZoomLevelDouble();
        boolean visible = isIPAVisible(level);
        Drawable newIcon = visible ?  mIconName : mIconDot;
        if (newIcon != getIcon()) {
            setIcon(newIcon);
        }
        if (TextUtils.isEmpty(mIPA)) return;
        if (isIPAVisible(level + 1.5)) {
            c.drawText(mIPA, p.x, p.y + getTextLabelFontSize() + mBaseline / 2, mIPAPaint);
        }
    }
}

