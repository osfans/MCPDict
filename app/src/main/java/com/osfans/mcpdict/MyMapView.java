package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.IOException;

public class MyMapView extends MapView {
    FolderOverlay mHzOverlay;
    boolean mHasProvinces = false;

    public MyMapView(Context context) {
        super(context);
    }

    public MyMapView(Context context, String hz) {
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
        if (mHasProvinces) return;
        for (int i = 11; i <= 82; i++) {
            FolderOverlay provinceOverlay = geoJsonifyMap(String.format("%s0000.geojson", i), true);
            if (provinceOverlay == null) continue;
            provinceOverlay.setDescription("province");
            provinceOverlay.setEnabled(false);
            getOverlays().add(provinceOverlay);
        }
        mHasProvinces = true;
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
                        ((MyMarker)item).setZoomLevel(level);
                    }
                }
                boolean enabled = event.getZoomLevel() >= 7.5;
                if (mHasProvinces) {
                    for (Overlay overlay : getOverlays()) {
                        if (overlay instanceof FolderOverlay) {
                            FolderOverlay folderOverlay = (FolderOverlay) overlay;
                            String desc = folderOverlay.getDescription();
                            if (!TextUtils.isEmpty(desc) && desc.contentEquals("province")) {
                                folderOverlay.setEnabled(enabled);
                            }
                        }
                    }
                } else if (enabled) initProvinces();
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
        FolderOverlay chinaOverlay = geoJsonifyMap("china.geojson", false);
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getContext()) {
            @Override
            public void setCopyrightNotice(String pCopyrightNotice) {
                super.setCopyrightNotice(getResources().getString(R.string.app_name)+"【"+ hz + "】");
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
                CharSequence yb = Utils.formatIPA(lang, Utils.getRawText(string));
                CharSequence js = Utils.formatIPA(lang, string);
                int size = DB.getSize(lang);
                MyMarker marker = new MyMarker(this, DB.getColor(lang), DB.getLabel(lang), yb.toString(), js.toString(), size);
                marker.setPosition(point);
                marker.setZoomLevel(level);
                folderOverlay.add(marker);
            }
            mHzOverlay = folderOverlay;
            getOverlays().add(mHzOverlay);
        } catch (Exception ignore) {
        }
    }

    public FolderOverlay geoJsonifyMap(String fileName, boolean isProvince) {
        final KmlDocument kmlDocument = new KmlDocument();

        try {
            kmlDocument.parseGeoJSON(FileUtils.getStringFromAssets(fileName, getContext()));
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }

        Style defaultStyle;
        if (isProvince) {
            defaultStyle = new Style(null, 0x3F000000, 0.5f, 0);
        } else {
            defaultStyle = new Style(null, 0x3F000000, 2f, 0xffffffff);
        }
        return (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(this, defaultStyle, null, kmlDocument);
    }
}
