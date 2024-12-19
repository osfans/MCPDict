package com.osfans.mcpdict.UI;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.Util.ThemeUtil;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.IOException;

public class MapView extends org.osmdroid.views.MapView {
    FolderOverlay mHzOverlay;
    boolean mProvinceInitialized = false;
    boolean mCountyInitialized = false;
    static String PROVINCE = "p";
    static String COUNTY = "c";

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, String hz) {
        this(context);
        init(hz);
        new Thread(()->{
            initHZ(hz);
            postInvalidate();
        }).start();
        new Thread(()->{
            initProvinces();
            initCounties();
            postInvalidate();
        }).start();
    }

    public void show() {
        new AlertDialog.Builder(getContext(), androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar)
                .setView(this)
                .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    private void initProvinces() {
        if (mProvinceInitialized) return;
        FolderOverlay overlay = geoJsonifyMap("province.json", 1);
        overlay.setDescription(PROVINCE);
        overlay.setEnabled(false);
        getOverlays().add(overlay);
        mProvinceInitialized = true;
    }

    private void initCounties() {
        if (mCountyInitialized) return;
        FolderOverlay overlay = geoJsonifyMap("county.json", 2);
        overlay.setDescription(COUNTY);
        overlay.setEnabled(false);
        getOverlays().add(overlay);
        mCountyInitialized = true;
    }

    public void init(String hz) {
        //setTileSource(TileSourceFactory.MAPNIK);
        addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                if (getOverlays().contains(mHzOverlay)) {
                    Double level = event.getZoomLevel();
                    for(Overlay item: mHzOverlay.getItems()) {
                        ((Marker)item).setZoomLevel(level);
                    }
                }
                int level = 0;
                if (event.getZoomLevel() >= 10) level = 2;
                else if (event.getZoomLevel() >= 7.5) level = 1;
                if (mProvinceInitialized) {
                    for (Overlay overlay : getOverlays()) {
                        if (overlay instanceof FolderOverlay folderOverlay) {
                            String desc = folderOverlay.getDescription();
                            if (!TextUtils.isEmpty(desc) && desc.contentEquals(PROVINCE)) {
                                folderOverlay.setEnabled(level == 1);
                            }
                        }
                    }
                } else if (level == 1) initProvinces();
                if (mCountyInitialized) {
                    for (Overlay overlay : getOverlays()) {
                        if (overlay instanceof FolderOverlay folderOverlay) {
                            String desc = folderOverlay.getDescription();
                            if (!TextUtils.isEmpty(desc) && desc.contentEquals(COUNTY)) {
                                folderOverlay.setEnabled(level == 2);
                            }
                        }
                    }
                } else if (level == 2) initCounties();
                invalidate();
                return true;
            }
        });
        setMultiTouchControls(true);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        setMinZoomLevel(4d);
        setMaxZoomLevel(20d);
//        GroundOverlay chinaOverlay = new GroundOverlay();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.china);
//        chinaOverlay.setImage(bitmap);
//        chinaOverlay.setPosition(new GeoPoint(57.5d, 67.1d), new GeoPoint(-6.9d, 141.4d));
//        chinaOverlay.setTransparency(0.5f);
        FolderOverlay chinaOverlay = geoJsonifyMap("china.json", 0);
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getContext()) {
            @Override
            public void setCopyrightNotice(String pCopyrightNotice) {
                super.setCopyrightNotice(Pref.getTitle()+"【"+ hz + "】");
            }
        };
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
        scaleBarOverlay.setAlignBottom(true);
        scaleBarOverlay.setAlignRight(true);

        //getOverlays().add(chinaOverlay);
        getOverlays().add(chinaOverlay);
        getOverlays().add(copyrightOverlay);
        getOverlays().add(scaleBarOverlay);
        invalidate();

        // Workaround for osmdroid issue
        // See: https://github.com/osmdroid/osmdroid/issues/337
        addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            BoundingBox boundingBox = chinaOverlay.getBounds();
            // Yep, it's called 2 times. Another workaround for zoomToBoundingBox.
            // See: https://github.com/osmdroid/osmdroid/issues/236#issuecomment-257061630
            zoomToBoundingBox(boundingBox, false);
            zoomToBoundingBox(boundingBox, false);
            invalidate();
        });
    }

    private void initHZ(String hz) {
        Cursor cursor = DB.directSearch(hz);
        cursor.moveToFirst();
        FolderOverlay folderOverlay = new FolderOverlay();
        double level = getZoomLevelDouble();
        try {
            for (String lang : DB.getVisibleColumns()) {
                GeoPoint point = DB.getPoint(lang);
                if (point == null) continue;
                int i = DB.getColumnIndex(lang);
                String string = cursor.getString(i);
                if (TextUtils.isEmpty(string)) continue;
                CharSequence yb = DisplayHelper.formatIPA(lang, DisplayHelper.getRawText(string));
                CharSequence js = DisplayHelper.formatIPA(lang, string);
                int size = DB.getSize(lang);
                Marker marker = new Marker(this, DB.getColor(lang), DB.getLabel(lang), yb.toString(), js.toString(), size);
                marker.setPosition(point);
                marker.setZoomLevel(level);
                folderOverlay.add(marker);
            }
            mHzOverlay = folderOverlay;
            getOverlays().add(mHzOverlay);
        } catch (Exception ignore) {
        }
    }

    public FolderOverlay geoJsonifyMap(String fileName, int level) {
        final KmlDocument kmlDocument = new KmlDocument();

        try {
            kmlDocument.parseGeoJSON(ThemeUtil.getStringFromAssets(fileName, getContext()));
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }

        Style defaultStyle;
        if (level == 2) {
            defaultStyle = new Style(null, 0x3F000000, 0.5f, 0);
        } else if (level == 1) {
            defaultStyle = new Style(null, 0x3F000000, 1f, 0);
        } else {
            defaultStyle = new Style(null, 0x3F000000, 2f, 0xffffffff);
        }
        FolderOverlay folderOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(this, defaultStyle, null, kmlDocument);
        for (KmlFeature mItem : kmlDocument.mKmlRoot.mItems) {
            GeoPoint point;
            if (level == 2) point = mItem.getBoundingBox().getCenterWithDateLine();
            else point = DB.parseLocation(mItem.getExtendedData("cp"));
            if (point == null) continue;
            Marker marker = new Marker(this, 0x3F000000, mItem.mName);
            marker.setPosition(point);
            marker.setInfoWindow(null);
            folderOverlay.add(marker);
        }

        return folderOverlay;
    }
}
