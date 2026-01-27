package com.osfans.mcpdict.UI;

import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_ZS;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.Util.FileUtil;

import org.json.JSONArray;
import org.osmdroid.bonuspack.kml.KmlDocument;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

public class MapView extends org.osmdroid.views.MapView {
    FolderOverlay mProvinceOverlay, mInfoOverlay;
    FolderOverlay[] mInfoMarkers;
    List<String> levels = Arrays.asList("province", "city"); //"district"
    final transient Object lock = new Object();
    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, String hz) {
        this(context);
        setUseDataConnection(false);
//        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        init(hz);
        new Thread(()->{
            initProvinces();
            postInvalidate();
        }).start();
        new Thread(()->{
            initInfo();
            postInvalidate();
        }).start();
    }

    public void show() {
        new AlertDialog.Builder(getContext(), R.style.AppTheme)
                .setView(this)
                .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    private void initProvinces() {
        if (getOverlays().contains(mProvinceOverlay)) return;
        mProvinceOverlay = geoJsonifyMap("province.json", 1);
        mProvinceOverlay.setEnabled(false);
    }

    private void initInfo() {
        if (getOverlays().contains(mInfoOverlay)) return;
        geoJsonifyInfo();
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
                double zoomLevel = event.getZoomLevel();
                int level = 0;
                if (zoomLevel >= 7) level = 1;
                if (getOverlays().contains(mProvinceOverlay)) {
                    mProvinceOverlay.setEnabled(level == 1);
                }
                if (getOverlays().contains(mInfoOverlay)) {
                    if (TextUtils.isEmpty(hz)) mInfoOverlay.setEnabled(false);
                    else {
                        mInfoOverlay.setEnabled(true);
                        mInfoMarkers[0].setEnabled(zoomLevel >= 5);
                        mInfoMarkers[1].setEnabled(zoomLevel >= 8);
//                        mInfoMarkers[2].setEnabled(zoomLevel >= 9);
                    }
                }
                invalidate();
                return true;
            }
        });
        setMultiTouchControls(true);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        setMinZoomLevel(4d);
        setMaxZoomLevel(15.7d);

        FolderOverlay chinaOverlay = geoJsonifyMap("china.json", 0);
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getContext()) {
            @Override
            public void setCopyrightNotice(String pCopyrightNotice) {
                String title = Pref.getTitle();
                if (!TextUtils.isEmpty(hz)) title += "【"+ hz + "】";
                super.setCopyrightNotice(title);
            }
        };
        getOverlays().add(copyrightOverlay);

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
        scaleBarOverlay.setAlignBottom(true);
        scaleBarOverlay.setAlignRight(true);
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
        new Thread(()->{
            synchronized (lock) {
                initHZ(hz);
                postInvalidate();
            }
        }).start();
    }

    private void initHZ(String hz) {
        FolderOverlay[] hzMarkers = new FolderOverlay[6];
        for (int i = 0; i < 6; i++) {
            FolderOverlay overlay = new FolderOverlay();
            getOverlays().add(overlay);
            hzMarkers[i] = overlay;
        }
        if (TextUtils.isEmpty(hz)) {
            Cursor cursor = DB.getCursor(String.format("select 簡稱,經緯度,地圖級別,%s from info where length(經緯度) > 0", DB.COLOR));
            if (cursor == null) return;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Marker marker = new Marker(this, cursor, "", "");
                marker.setOnMarkerClickListener((marker1, mapView) -> {
                    App.info(getContext(), marker1.getTitle());
                    return true;
                });
                int size = marker.getSize();
                hzMarkers[size].add(marker);
            }
            cursor.close();
            return;
        }
        Cursor cursor = DB.directSearch(hz);
        String lastLang = "";
        List<CharSequence> IPAs = new ArrayList<>(), comments = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String lang = cursor.getString(COL_LANG);
            if (!lastLang.contentEquals(lang) && !TextUtils.isEmpty(lastLang)) {
                Cursor c = DB.getCursor(String.format("select 簡稱,經緯度,地圖級別,%s from info where 簡稱 MATCH '%s' and length(經緯度) > 0", DB.COLOR, lastLang));
                if (c != null) {
                    Marker marker = new Marker(this, c, String.join(" ", IPAs), String.join(" ", comments));
                    int size = marker.getSize();
                    hzMarkers[size].add(marker);
                    c.close();
                }
                IPAs.clear();
                comments.clear();
            }
            lastLang = lang;
            CharSequence ipa = DisplayHelper.formatIPA(lang, cursor.getString(COL_IPA));
            IPAs.add(ipa);
            comments.add(ipa);
            String zs = cursor.getString(COL_ZS);
            if (!TextUtils.isEmpty(zs)) {
                comments.add(DisplayHelper.formatZS(hz, zs));
            }
        }
        if (!TextUtils.isEmpty(lastLang)) {
            Cursor c = DB.getCursor(String.format("select 簡稱,經緯度,地圖級別,%s from info where 簡稱 MATCH '%s' and length(經緯度) > 0", DB.COLOR, lastLang));
            if (c != null) {
                Marker marker = new Marker(this, c, String.join(" ", IPAs), String.join(" ", comments));
                int size = marker.getSize();
                hzMarkers[size].add(marker);
                c.close();
            }
            IPAs.clear();
            comments.clear();
        }
        cursor.close();
    }

    private void createMarker(JSONObject o) {
        String level = o.optString("level", "");
        int index = levels.indexOf(level);
        if (index == -1) return;
        String name = o.optString("name", "");
        if (TextUtils.isEmpty(name)) return;
        JSONArray centroids = o.optJSONArray("centroid");
        if (centroids == null) centroids = o.optJSONArray("center");
        if (centroids == null || centroids.length() != 2) return;
        GeoPoint point = new GeoPoint(centroids.optDouble(1), centroids.optDouble(0));
        org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(this);
        marker.setTextLabelBackgroundColor(Color.TRANSPARENT);
        marker.setTextLabelForegroundColor(0xC000000 * (2 + index));
        marker.setTextLabelFontSize(marker.getTextLabelFontSize() * 4 / 3);
        marker.setTextIcon(name);
        marker.setPosition(point);
        marker.setInfoWindow(null);
        mInfoMarkers[index].add(marker);
    }

    private void geoJsonifyInfo() {
        FolderOverlay folderOverlay = new FolderOverlay();
        mInfoMarkers = new FolderOverlay[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            FolderOverlay overlay = new FolderOverlay();
            overlay.setName(levels.get(i));
            overlay.setEnabled(false);
            folderOverlay.add(overlay);
            mInfoMarkers[i] = overlay;
        }
        folderOverlay.setEnabled(true);
        mInfoOverlay = folderOverlay;
        int size = getOverlays().size();
        getOverlays().add(Math.min(size, 3), folderOverlay);
        try {
            String jsonString = FileUtil.getStringFromAssets("info.json", getContext());
            JSONObject jsonObjects = new JSONObject(jsonString);
            for (Iterator<String> it = jsonObjects.keys(); it.hasNext(); ) {
                String key = it.next();
                createMarker(jsonObjects.getJSONObject(key));
            }
        } catch (Exception ignored) {
        }
    }

    public FolderOverlay geoJsonifyMap(String fileName, int level) {
        final KmlDocument kmlDocument = new KmlDocument();

        try {
            kmlDocument.parseGeoJSON(FileUtil.getStringFromAssets(fileName, getContext()));
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
        int size = getOverlays().size();
        getOverlays().add(Math.min(size, 3), folderOverlay);
        for (Overlay o: folderOverlay.getItems()) {
            if (o instanceof Polygon) ((Polygon) o).setInfoWindow(null);
            else if (o instanceof FolderOverlay) {
                for (Overlay o2: ((FolderOverlay) o).getItems()) {
                    if (o2 instanceof Polygon) ((Polygon) o2).setInfoWindow(null);
                }
            }
        }

        return folderOverlay;
    }

    @Override
    protected void onDetachedFromWindow() {
        synchronized (lock) {
            super.onDetachedFromWindow();
        }
    }
}
