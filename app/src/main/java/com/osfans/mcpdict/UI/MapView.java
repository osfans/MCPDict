package com.osfans.mcpdict.UI;

import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_ZS;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
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
    FolderOverlay mHzOverlay, mBorderOverlay;
    List<String> levels = Arrays.asList("province", "city", "district");
    FolderOverlay[] mInfoMarkers;
    boolean mProvinceInitialized = false, mInfoInitialized = false;

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, String hz) {
        this(context);
        setUseDataConnection(false);
//        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        init(hz);
        new Thread(()->{
            initHZ(hz);
            postInvalidate();
        }).start();
        new Thread(()->{
            initProvinces();
            initInfo();
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
        mBorderOverlay = geoJsonifyMap("province.json", 1);
        mBorderOverlay.setEnabled(false);
        getOverlays().add(mBorderOverlay);
        mProvinceInitialized = true;
    }

    private void initInfo() {
        if (mInfoInitialized) return;
        getOverlays().add(geoJsonifyInfo());
        mInfoInitialized = true;
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
                    if (getOverlays().contains(mBorderOverlay)) {
                        mBorderOverlay.setEnabled(level == 1);
                    }
                } else if (level == 1) initProvinces();
                if (mInfoInitialized) {
                    mInfoMarkers[0].setEnabled(zoomLevel >= 5 && zoomLevel < 7.5);
                    mInfoMarkers[1].setEnabled(zoomLevel >= 7.5 && zoomLevel < 10);
                    mInfoMarkers[2].setEnabled(zoomLevel >= 10);
                } else initInfo();
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
        FolderOverlay folderOverlay = new FolderOverlay();
        double level = getZoomLevelDouble();
        String lastLang = "";
        List<CharSequence> IPAs = new ArrayList<>(), comments = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String lang = cursor.getString(COL_LANG);
            if (!lastLang.contentEquals(lang) && !TextUtils.isEmpty(lastLang)) {
                GeoPoint point = DB.getPoint(lastLang);
                if (point != null) {
                    int size = DB.getSize(lastLang);
                    Marker marker = new Marker(this, DB.getColor(lastLang), lastLang, String.join(" ", IPAs), String.join(" ", comments), size);
                    marker.setPosition(point);
                    marker.setZoomLevel(level);
                    folderOverlay.add(marker);
                }
                IPAs.clear();
                comments.clear();
            }
            lastLang = lang;
            GeoPoint point = DB.getPoint(lastLang);
            if (point == null) continue;
            CharSequence ipa = DisplayHelper.formatIPA(lang, cursor.getString(COL_IPA));
            IPAs.add(ipa);
            comments.add(ipa);
            String zs = cursor.getString(COL_ZS);
            if (!TextUtils.isEmpty(zs)) {
                comments.add(DisplayHelper.formatZS(hz, zs));
            }
        }
        if (!TextUtils.isEmpty(lastLang)) {
            GeoPoint point = DB.getPoint(lastLang);
            if (point != null) {
                int size = DB.getSize(lastLang);
                Marker marker = new Marker(this, DB.getColor(lastLang), lastLang, String.join(" ", IPAs), String.join(" ", comments), size);
                marker.setPosition(point);
                marker.setZoomLevel(level);
                folderOverlay.add(marker);
            }
            IPAs.clear();
            comments.clear();
        }
        mHzOverlay = folderOverlay;
        getOverlays().add(mHzOverlay);
        cursor.close();
    }

    private Marker createMarker(JSONObject o) {
        String name = o.optString("name", "");
        if (TextUtils.isEmpty(name)) return null;
        GeoPoint point = DB.parseLocation(o.optString("centroid", o.optString("center")));
        if (point == null) return null;
        Marker marker = new Marker(this, 0x3F000000, name);
        marker.setPosition(point);
        marker.setInfoWindow(null);
        return marker;
    }

    private FolderOverlay geoJsonifyInfo() {
        FolderOverlay folderOverlay = new FolderOverlay();
        mInfoMarkers = new FolderOverlay[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            FolderOverlay overlay = new FolderOverlay();
            overlay.setName(levels.get(i));
            overlay.setEnabled(false);
            folderOverlay.add(overlay);
            mInfoMarkers[i] = overlay;
        }
        try {
            String jsonString = FileUtil.getStringFromAssets("info.json", getContext());
            JSONObject jsonObjects = new JSONObject(jsonString);
            for (Iterator<String> it = jsonObjects.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject o = jsonObjects.getJSONObject(key);
                String level = o.optString("level", "");
                int index = levels.indexOf(level);
                if (index == -1) continue;
                Marker marker = createMarker(o);
                if (marker == null) continue;
                mInfoMarkers[index].add(marker);
                int childrenNum = o.optInt("childrenNum");
                if (childrenNum == 0 && index + 1 < levels.size() ) {
                    mInfoMarkers[index + 1].add(marker);
                }
                if (index == 0) {
                    String fullName = o.optString("fullname");
                    if (fullName.endsWith("市") || fullName.endsWith("行政區")) mInfoMarkers[index + 1].add(marker);
                }
            }
        } catch (Exception ignored) {
        }
        folderOverlay.setEnabled(true);
        return folderOverlay;
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
}
