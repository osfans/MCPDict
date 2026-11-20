package com.osfans.mcpdict.UI;

import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_ZS;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

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
    private JSONArray mComparisonData = null;
    private static final int HISTORICAL_LAYER = 1;
    private static final int ISLAND_LAYER = 2;
    private boolean thematicMap;
    private int hiddenPointTypes;
    private FolderOverlay[] thematicLayers;
    private String mCharacter = "";
    private final android.os.Handler layerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private int loadingLayers;
    private boolean detached, released;
    public MapView(Context context) {
        super(context);
        // Closing a map must not destroy its repository while a layer is still creating markers.
        setDestroyMode(false);
    }

    public MapView(Context context, String hz) {
        this(context);
        setUseDataConnection(false);
        init(hz);
        initAsyncLayers();
    }

    /**
     * 音典小工具的字音比较地图。
     * 底图、行政区、缩放等仍然复用原 MapView。
     */
    public MapView(Context context, JSONArray comparisonData) {
        this(context, comparisonData, false);
    }

    private MapView(Context context, JSONArray comparisonData, boolean thematicMap) {
        this(context);
        mComparisonData = comparisonData;
        this.thematicMap = thematicMap;
        setUseDataConnection(false);
        init("");
        initAsyncLayers();
    }

    private void loadLayer(Runnable loader) {
        loadingLayers++;
        new Thread(() -> {
            try {
                synchronized (lock) { loader.run(); }
            } finally {
                layerHandler.post(() -> {
                    loadingLayers--;
                    releaseWhenReady();
                });
            }
        }).start();
    }

    private void releaseWhenReady() {
        if (detached && loadingLayers == 0 && !released) {
            released = true;
            onDetach();
        }
    }

    private void requestRelease() {
        if (!detached) {
            onPause();
            detached = true;
        }
        releaseWhenReady();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        requestRelease();
    }

    private void initAsyncLayers() {
        loadLayer(() -> {
            initProvinces();
            postInvalidate();
        });
        loadLayer(() -> {
            if (mComparisonData == null) initInfo();
            postInvalidate();
        });
    }

    public static void showThematic(Context context, JSONArray points, CharSequence title,
                                    String[] labels, int[] colors) {
        new MapView(context, points, true).showDialog(title, labels, colors);
    }

    public void show() {
        showDialog(TextUtils.isEmpty(mCharacter) ? "地圖" : "地圖 · " + mCharacter,
                new String[0], new int[0]);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static android.app.Activity findActivity(Context context) {
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof android.app.Activity) return (android.app.Activity) context;
            Context base = ((android.content.ContextWrapper) context).getBaseContext();
            if (base == context) break;
            context = base;
        }
        return null;
    }

    /** Keep the same map and overlays while the host handles orientation/size changes. */
    private void showDialog(CharSequence title, String[] labels, int[] colors) {
        Context context = getContext();
        android.app.Activity host = findActivity(context);
        int previousOrientation = host == null
                ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : host.getRequestedOrientation();
        boolean[] orientationChanged = {false};

        android.widget.LinearLayout root = new android.widget.LinearLayout(context);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout bar = new android.widget.LinearLayout(context);
        bar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        android.widget.TextView heading = new android.widget.TextView(context);
        heading.setText(title);
        heading.setTextSize(14);
        heading.setSingleLine(true);
        heading.setEllipsize(TextUtils.TruncateAt.END);
        heading.setPadding(dp(12), dp(8), dp(4), dp(8));
        com.osfans.mcpdict.Util.FontUtil.setTypeface(heading);
        bar.addView(heading, new android.widget.LinearLayout.LayoutParams(0, -2, 1));
        android.widget.Button rotate = new android.widget.Button(context);
        android.widget.Button close = new android.widget.Button(context);
        for (android.widget.Button button : new android.widget.Button[]{rotate, close}) {
            button.setTextSize(13);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(dp(10), 0, dp(10), 0);
            com.osfans.mcpdict.Util.FontUtil.setTypeface(button);
        }
        close.setText("關閉");
        rotate.setEnabled(host != null);
        Runnable updateDirection = () -> {
            boolean landscape = getResources().getConfiguration().orientation
                    == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
            String label = landscape ? "豎屏" : "橫屏";
            if (!TextUtils.equals(rotate.getText(), label)) {
                rotate.setText(label);
                rotate.setContentDescription(landscape ? "切換至豎屏" : "切換至橫屏");
            }
        };
        updateDirection.run();
        rotate.setOnClickListener(v -> {
            if (host == null || host.isFinishing() || host.isDestroyed()) return;
            boolean landscape = getResources().getConfiguration().orientation
                    == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
            orientationChanged[0] = true;
            host.setRequestedOrientation(landscape
                    ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        root.addView(bar);

        TextView[] legendItems = new TextView[labels.length];
        if (thematicMap) {
            int[][] counts = new int[4][labels.length];
            for (int point = 0; point < mComparisonData.length(); point++) {
                JSONObject row = mComparisonData.optJSONObject(point);
                if (row == null) continue;
                JSONArray indices = row.optJSONArray("legendItems");
                if (indices == null) continue;
                for (int i = 0; i < indices.length(); i++) {
                    int index = indices.optInt(i, -1);
                    if (index >= 0 && index < labels.length) counts[thematicType(row)][index]++;
                }
            }
            Runnable refreshLegend = () -> updateThematicLegend(labels, colors, legendItems, counts);
            addMapFilter(bar, "歷史音", HISTORICAL_LAYER, refreshLegend);
            addMapFilter(bar, "方言島", ISLAND_LAYER, refreshLegend);
            root.post(refreshLegend);
        }
        bar.addView(rotate, new LinearLayout.LayoutParams(-2, dp(48)));
        bar.addView(close, new LinearLayout.LayoutParams(-2, dp(48)));

        android.widget.FrameLayout canvas = new android.widget.FrameLayout(context);
        canvas.addView(this, new android.widget.FrameLayout.LayoutParams(-1, -1));
        if (labels.length > 0) {
            android.widget.ScrollView scroll = new android.widget.ScrollView(context);
            android.widget.LinearLayout legend = new android.widget.LinearLayout(context);
            legend.setOrientation(android.widget.LinearLayout.VERTICAL);
            legend.setPadding(dp(8), dp(6), dp(8), dp(6));
            android.util.TypedValue bg = new android.util.TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.colorBackground, bg, true);
            legend.setBackgroundColor(bg.data);
            for (int i = 0; i < labels.length; i++) {
                android.widget.TextView item = new android.widget.TextView(context);
                android.text.SpannableString text = new android.text.SpannableString("● " + labels[i]);
                text.setSpan(new android.text.style.ForegroundColorSpan(colors[i]), 0, 1,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                item.setText(text);
                com.osfans.mcpdict.Util.FontUtil.setTypeface(item);
                item.setTextSize(12);
                legend.addView(item);
                legendItems[i] = item;
            }
            scroll.addView(legend);
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    dp(160), dp(220), android.view.Gravity.BOTTOM | android.view.Gravity.START);
            canvas.addView(scroll, params);
            canvas.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
                int width = Math.min(dp(160), Math.max(dp(48), (r - l) / 2));
                int height = Math.min(dp(220), Math.max(dp(48), b - t - dp(16)));
                if (params.width != width || params.height != height) {
                    params.width = width;
                    params.height = height;
                    scroll.setLayoutParams(params);
                }
            });
        }
        root.addView(canvas, new android.widget.LinearLayout.LayoutParams(-1, 0, 1));
        root.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> updateDirection.run());

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.AppTheme).setView(root).create();
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> {
            requestRelease();
            if (orientationChanged[0] && host != null && !host.isFinishing() && !host.isDestroyed()) {
                host.setRequestedOrientation(previousOrientation);
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(-1, -1);
        onResume();
    }

    private static int thematicType(JSONObject row) {
        return (row.optBoolean("historical") ? HISTORICAL_LAYER : 0)
                | (row.optBoolean("island") ? ISLAND_LAYER : 0);
    }

    private void addMapFilter(LinearLayout bar, String title, int flag, Runnable refreshLegend) {
        SwitchCompat control = new SwitchCompat(getContext());
        control.setText(title);
        control.setTextSize(13);
        control.setSingleLine(true);
        control.setShowText(false);
        control.setSwitchMinWidth(dp(32));
        control.setSwitchPadding(dp(4));
        control.setPadding(dp(3), 0, dp(7), 0);
        com.osfans.mcpdict.Util.FontUtil.setTypeface(control);
        control.setChecked((hiddenPointTypes & flag) == 0);
        control.setOnCheckedChangeListener((button, checked) -> {
            hiddenPointTypes = checked ? hiddenPointTypes & ~flag : hiddenPointTypes | flag;
            updateThematicLayers();
            refreshLegend.run();
        });
        bar.addView(control, new LinearLayout.LayoutParams(-2, dp(48)));
    }

    private void updateThematicLayers() {
        if (thematicLayers != null) {
            for (int type = 0; type < thematicLayers.length; type++) {
                thematicLayers[type].setEnabled((type & hiddenPointTypes) == 0);
            }
            invalidate();
        }
    }

    private void updateThematicLegend(String[] labels, int[] colors, TextView[] items, int[][] counts) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) continue;
            int count = 0;
            for (int type = 0; type < counts.length; type++) {
                if ((type & hiddenPointTypes) == 0) count += counts[type][i];
            }
            android.text.SpannableString text = new android.text.SpannableString("● " + labels[i] + "（" + count + " 點）");
            text.setSpan(new android.text.style.ForegroundColorSpan(colors[i]), 0, 1,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            items[i].setText(text);
        }
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
        mCharacter = hz;
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
                    if (TextUtils.isEmpty(hz) && mComparisonData == null)
                        mInfoOverlay.setEnabled(false);
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
        loadLayer(() -> {
            if (mComparisonData == null) {
                initHZ(hz);
            } else {
                initComparison(mComparisonData);
            }
            postInvalidate();
        });
    }

    private static int comparisonColor(String status) {
        return switch (status) {
            case "same" ->
                    Color.rgb(21, 128, 61);

            case "partial" ->
                    Color.rgb(202, 138, 4);

            case "different" ->
                    Color.rgb(185, 28, 28);

            default ->
                    Color.rgb(148, 163, 184);
        };
    }

    private void initComparison(JSONArray data) {
        FolderOverlay[] layers = new FolderOverlay[thematicMap ? 4 : 1];
        FolderOverlay[][] markers = new FolderOverlay[layers.length][6];
        for (int type = 0; type < layers.length; type++) {
            layers[type] = new FolderOverlay();
            for (int size = 0; size < markers[type].length; size++) {
                markers[type][size] = new FolderOverlay();
                layers[type].add(markers[type][size]);
            }
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.optJSONObject(i);

            if (row == null)
                continue;

            String label =
                    row.optString("label", "");

            String coordinate =
                    row.optString("coordinate", "");

            if (TextUtils.isEmpty(label)
                    || TextUtils.isEmpty(coordinate))
                continue;

            int size = Math.max(
                    0,
                    Math.min(
                            markers[0].length - 1,
                            row.optInt("mapLevel", 0)
                    )
            );

            int color = comparisonColor(
                    row.optString(
                            "status",
                            "missing"
                    )
            );

            if(row.has("color"))color=row.optInt("color",color);
            String detail =
                    row.optString("detail", "");

            try {
                // Comparison map markers show only the place name on the map.
                // Pronunciation/status details are shown after tapping the marker.
                Marker marker = new Marker(
                        this,
                        label,
                        coordinate,
                        size,
                        color,
                        color,
                        "",
                        detail
                );

                org.json.JSONArray parts=row.optJSONArray("colors");
                if(parts!=null){java.util.List<Integer> palette=new java.util.ArrayList<>();for(int j=0;j<parts.length();j++)palette.add(parts.optInt(j));marker.setThematicColors(palette);}
                marker.setOnMarkerClickListener((marker1, mapView) -> {
                    android.app.Dialog detailDialog=new AlertDialog.Builder(getContext())
                            .setTitle(marker1.getTitle())
                            .setMessage(marker1.getSnippet())
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    android.widget.TextView message=detailDialog.findViewById(android.R.id.message);if(message!=null){com.osfans.mcpdict.Util.FontUtil.setTypeface(message);message.setTextIsSelectable(true);}
                    return true;
                });

                markers[thematicMap ? thematicType(row) : 0][size].add(marker);

            } catch (Exception ignored) {
                // 某一个方言点坐标异常时跳过，
                // 不影响整张地图。
            }
        }
        layerHandler.post(() -> {
            for (FolderOverlay layer : layers) getOverlays().add(layer);
            if (thematicMap) {
                thematicLayers = layers;
                updateThematicLayers();
            } else {
                invalidate();
            }
        });
    }

    private void initHZ(String hz) {
        FolderOverlay[] hzMarkers = new FolderOverlay[6];
        for (int i = 0; i < 6; i++) {
            FolderOverlay overlay = new FolderOverlay();
            getOverlays().add(overlay);
            hzMarkers[i] = overlay;
        }
        if (com.osfans.mcpdict.Util.Pref.getFilter() == DB.FILTER.CUSTOM) {
            initCustom(hz, hzMarkers);
            return;
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

    private void initCustom(String hz, FolderOverlay[] overlays) {
        java.util.Map<String, java.util.List<com.osfans.mcpdict.custom.CustomSchemes.DisplayEntry>> entries =
                new java.util.LinkedHashMap<>();
        for (var row : com.osfans.mcpdict.custom.CustomSchemes.visibleEntries()) {
            entries.computeIfAbsent(row.label(), key -> new java.util.ArrayList<>()).add(row);
        }
        java.util.Map<String, StringBuilder> readings = new java.util.HashMap<>();
        java.util.Map<String, StringBuilder> notes = new java.util.HashMap<>();
        if (!TextUtils.isEmpty(hz)) try (Cursor cursor = DB.directSearch(hz)) {
            while (cursor != null && cursor.moveToNext()) {
                String label = cursor.getString(COL_LANG);
                String raw = cursor.getString(COL_IPA);
                if (raw == null || raw.trim().isEmpty()) continue;
                CharSequence ipa = DisplayHelper.formatIPA(label, raw);
                readings.computeIfAbsent(label, key -> new StringBuilder()).append(ipa).append(' ');
                StringBuilder detail = notes.computeIfAbsent(label, key -> new StringBuilder());
                detail.append(ipa);
                String comment = cursor.getString(COL_ZS);
                if (!TextUtils.isEmpty(comment)) detail.append(' ').append(DisplayHelper.formatZS(hz, comment));
                detail.append('\n');
            }
        }
        try (Cursor cursor = DB.getCursor(String.format(
                "select 簡稱,經緯度,地圖級別,%s from info where length(經緯度) > 0", DB.COLOR))) {
            if (cursor == null) return;
            boolean customColor = com.osfans.mcpdict.Util.Pref.getBool(R.string.pref_key_custom_language_color_by_scheme, false);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String label = cursor.getString(0);
                var rows = entries.get(label);
                if (rows == null || !TextUtils.isEmpty(hz) && !readings.containsKey(label)) continue;
                java.util.List<Integer> colors = new java.util.ArrayList<>();
                StringBuilder details = new StringBuilder();
                for (var row : rows) {
                    colors.add(customColor ? com.osfans.mcpdict.custom.CustomSchemes.color(row.color(), label) : DB.getColor(label));
                    details.append(row.language()).append(" · ")
                            .append(com.osfans.mcpdict.custom.CustomSchemes.displayGroup(row.group())).append('\n');
                }
                details.append(notes.getOrDefault(label, new StringBuilder()));
                String ipa = readings.getOrDefault(label, new StringBuilder()).toString().trim();
                int size = Math.max(0, Math.min(overlays.length - 1, cursor.getInt(2)));
                Marker marker = new Marker(this, label, cursor.getString(1), size,
                        colors.get(0), colors.get(0), ipa, details.toString());
                if (customColor && marker.mIconName instanceof TextDrawable text) {
                    text.setTextColor(android.graphics.Color.WHITE);
                }
                marker.setThematicColors(colors);
                overlays[size].add(marker);
            }
        }
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

}
