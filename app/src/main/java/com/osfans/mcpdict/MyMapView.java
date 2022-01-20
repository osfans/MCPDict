package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.osmdroid.bonuspack.clustering.MarkerClusterer;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.FolderZOverlay;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.GroundOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyMapView extends MapView {
    FolderOverlay mFolderOverlay;

    public MyMapView(Context context) {
        super(context);
    }

    public MyMapView(Context context, String hz) {
        this(context);
        init(hz);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
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
                if (getOverlays().contains(mFolderOverlay)) {
                    Double level = event.getZoomLevel();
                    for(Overlay item: mFolderOverlay.getItems()) {
                        ((MyMarker)item).setZoomLevel(level);
                    }
                    invalidate();
                }
                return true;
            }
        });
        setMultiTouchControls(true);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        setMinZoomLevel(4d);
        setMaxZoomLevel(20d);
//        GroundOverlay groundOverlay = new GroundOverlay();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.china);
//        groundOverlay.setImage(bitmap);
//        groundOverlay.setPosition(new GeoPoint(54d, 73d), new GeoPoint(17d, 135d));
//        groundOverlay.setTransparency(0.5f);
        FolderOverlay chinaOverlay = geoJsonifyMap("china.geojson");
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getContext()) {
            @Override
            public void setCopyrightNotice(String pCopyrightNotice) {
                super.setCopyrightNotice(getResources().getString(R.string.app_name)+"【"+ hz + "】");
            }
        };
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
        scaleBarOverlay.setAlignBottom(true);
        scaleBarOverlay.setAlignRight(true);
        mFolderOverlay = initHZ(hz);

        getOverlays().add(chinaOverlay);
        getOverlays().add(copyrightOverlay);
        getOverlays().add(scaleBarOverlay);
        getOverlays().add(mFolderOverlay);
        invalidate();

        // Workaround for osmdroid issue
        // See: https://github.com/osmdroid/osmdroid/issues/337
        addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            BoundingBox boundingBox = (mFolderOverlay.getItems().size() >= 3 ? mFolderOverlay : chinaOverlay).getBounds();
            // Yep, it's called 2 times. Another workaround for zoomToBoundingBox.
            // See: https://github.com/osmdroid/osmdroid/issues/236#issuecomment-257061630
            zoomToBoundingBox(boundingBox, false);
            zoomToBoundingBox(boundingBox, false);
            invalidate();
        });
    }
    
    private FolderOverlay initHZ(String hz) {
        Cursor cursor = MCPDatabase.directSearch(hz);
        cursor.moveToFirst();
        Context context = getContext();
        String languages = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_show_language_names), "");
        Set<String> customs = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(context.getString(R.string.pref_key_custom_languages), null);
        FolderOverlay folderOverlay = new FolderOverlay();
        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            String string1 = cursor.getString(i);
            boolean visible = string1 != null && SearchResultCursorAdapter.isColumnVisible(languages, customs, i);
            if (!visible) continue;
            GeoPoint point = MCPDatabase.getPoint(i);
            if (point == null) continue;
            CharSequence yb = SearchResultCursorAdapter.formatIPA(i,  SearchResultCursorAdapter.getRawText(string1));
            CharSequence js = SearchResultCursorAdapter.formatIPA(i,  string1);
            int size = MCPDatabase.getSize(i);
            MyMarker marker = new MyMarker(this, MCPDatabase.getColor(i), MCPDatabase.getLabel(i), yb.toString() , js.toString(), size);
            marker.setPosition(point);
            folderOverlay.add(marker);
        }
        return folderOverlay;
    }

    public FolderOverlay geoJsonifyMap(String fileName) {
        final KmlDocument kmlDocument = new KmlDocument();

        try {
            kmlDocument.parseGeoJSON(FileUtils.getStringFromAssets(fileName, getContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Drawable defaultMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_default, getContext().getTheme());
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x1f000000, 2f, 0xffffffff);
        return (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(this, defaultStyle, null, kmlDocument);
    }
}
