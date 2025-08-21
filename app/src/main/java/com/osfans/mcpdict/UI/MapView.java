package com.osfans.mcpdict.UI;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
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
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.IOException;

public class MapView extends org.osmdroid.views.MapView {
    FolderOverlay mHzOverlay, mProvinceOverlay, mSmallCityOverlay;
    boolean mProvinceInitialized = false;

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
        mProvinceOverlay = geoJsonifyMap("province.json", 1);
        mProvinceOverlay.setEnabled(false);
        getOverlays().add(mProvinceOverlay);
        mProvinceInitialized = true;
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
                Double zoomLevel = event.getZoomLevel();
                if (getOverlays().contains(mHzOverlay)) {
                    for (Overlay item: mHzOverlay.getItems()) {
                        ((Marker)item).setZoomLevel(zoomLevel);
                    }
                }
                int level = 0;
                if (zoomLevel >= 7.5) level = 1;
                if (mProvinceInitialized) {
                    if (getOverlays().contains(mProvinceOverlay)) {
                        mProvinceOverlay.setEnabled(level == 1);
                        mSmallCityOverlay.setEnabled(zoomLevel >= 9.5);
                    }
                } else if (level == 1) initProvinces();
                invalidate();
                return true;
            }
        });
        setMultiTouchControls(true);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        setMinZoomLevel(4d);
        setMaxZoomLevel(20d);

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
            for (String lang : DB.getVisibleLanguages()) {
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
        cursor.close();
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
        if (level == 1) {
            defaultStyle = new Style(null, 0x3F000000, 0.5f, 0);
        } else {
            defaultStyle = new Style(null, 0x3F000000, 2f, 0xffffffff);
        }
        FolderOverlay folderOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(this, defaultStyle, null, kmlDocument);
        for (Overlay o: folderOverlay.getItems()) {
            if (o instanceof Polygon) ((Polygon) o).setInfoWindow(null);
            else if (o instanceof FolderOverlay) {
                for (Overlay o2: ((FolderOverlay) o).getItems()) {
                    if (o2 instanceof Polygon) ((Polygon) o2).setInfoWindow(null);
                }
            }
        }
        if (level == 1) {
            mSmallCityOverlay = new FolderOverlay();
            for (KmlFeature mItem : kmlDocument.mKmlRoot.mItems) {
                GeoPoint point = DB.parseLocation(mItem.getExtendedData("cp"));
                if (point == null) point = mItem.getBoundingBox().getCenterWithDateLine();
                if (point == null) continue;

                Marker marker = new Marker(this, 0x3F000000, mItem.mName);
                marker.setPosition(point);
                marker.setInfoWindow(null);
                double length = mItem.getBoundingBox().getDiagonalLengthInMeters();
                if (length <= 60000d) mSmallCityOverlay.add(marker);
                else folderOverlay.add(marker);
            }
            folderOverlay.add(mSmallCityOverlay);
        }

        return folderOverlay;
    }
}
