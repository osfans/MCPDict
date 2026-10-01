package com.osfans.mcpdict.UI;

import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_ZS;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.getColor;
import static com.osfans.mcpdict.DB.getSubColor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * V4 virtualized matrix view.
 *
 * Frozen panes:
 * - the top header is a separate horizontal RecyclerView, so it stays visible
 *   while the body scrolls vertically;
 * - the left label column is outside each row's horizontal RecyclerView, so it
 *   stays visible while the body scrolls horizontally.
 *
 * The horizontal offsets of the frozen header and all visible rows are kept in
 * sync. Character headers are deliberately simple: one character only, using
 * the same MCPDict font/color/relative-size treatment as the normal result
 * header, with no Unicode/dictionary/map/favorite links.
 */
public class MatrixTableView {

    public enum Orientation {
        LANGUAGES_AS_ROWS,
        CHARACTERS_AS_ROWS
    }

    private final Context context;
    private final RecyclerView headerView;
    private final RecyclerView bodyView;
    private final boolean isMainPage;

    private final int cellWidth;
    private final int labelWidth;
    private final int rowMinHeight;
    private final TextDrawable.IBuilder languageLabelBuilder;
    private final RecyclerView.RecycledViewPool cellPool = new RecyclerView.RecycledViewPool();

    private Orientation orientation = Orientation.LANGUAGES_AS_ROWS;

    // Shared absolute horizontal scroll state.  We intentionally keep the
    // RecyclerView anchor item and its pixel offset instead of accumulating dx.
    // That makes recycled rows snap back to the exact same position when they
    // re-enter the viewport after a long vertical scroll.
    private int horizontalAnchorPosition = 0;
    private int horizontalAnchorOffsetPx = 0;
    private boolean syncingHorizontalScroll = false;

    private final List<String> hzOrder = new ArrayList<>();
    private final List<String> langOrder = new ArrayList<>();
    private final LinkedHashMap<String, LinkedHashMap<String, List<Reading>>> byHz = new LinkedHashMap<>();
    private final LinkedHashMap<String, LinkedHashMap<String, List<Reading>>> byLang = new LinkedHashMap<>();

    private final HeaderAdapter headerAdapter = new HeaderAdapter();
    private final BodyAdapter bodyAdapter = new BodyAdapter();

    private static class Reading {
        final String hz;
        final String lang;
        final String ipa;
        final String zs;

        Reading(String hz, String lang, String ipa, String zs) {
            this.hz = hz;
            this.lang = lang;
            this.ipa = ipa;
            this.zs = zs;
        }
    }

    public MatrixTableView(Context context,
                           RecyclerView headerView,
                           RecyclerView bodyView,
                           boolean isMainPage) {
        this.context = context;
        this.headerView = headerView;
        this.bodyView = bodyView;
        this.isMainPage = isMainPage;

        cellWidth = dp(132);
        labelWidth = Math.max(dp(92),
                context.getResources().getDimensionPixelOffset(R.dimen.label_width) + dp(16));
        rowMinHeight = Math.max(dp(52),
                context.getResources().getDimensionPixelOffset(R.dimen.label_height) + dp(10));

        int labelW = context.getResources().getDimensionPixelOffset(R.dimen.label_width);
        int labelH = context.getResources().getDimensionPixelOffset(R.dimen.label_height);
        languageLabelBuilder = TextDrawable.builder()
                .beginConfig()
                .withBorder(3)
                .width(labelW)
                .height(labelH)
                .endConfig()
                .roundRect(5);

        LinearLayoutManager headerLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        headerView.setLayoutManager(headerLayoutManager);
        headerView.setAdapter(headerAdapter);
        headerView.setItemAnimator(null);
        headerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        headerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                onHorizontalScrolled(recyclerView, dx);
            }
        });

        bodyView.setLayoutManager(new LinearLayoutManager(context));
        bodyView.setAdapter(bodyAdapter);
        bodyView.setItemAnimator(null);
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation next) {
        if (orientation == next) return;
        orientation = next;
        resetHorizontalScrollState();
        bodyView.scrollToPosition(0);
        headerView.scrollToPosition(0);
        headerAdapter.notifyDataSetChanged();
        bodyAdapter.notifyDataSetChanged();
    }

    public void setCursor(Cursor cursor) {
        buildModel(cursor);
        resetHorizontalScrollState();
        headerView.scrollToPosition(0);
        bodyView.scrollToPosition(0);
        headerAdapter.notifyDataSetChanged();
        bodyAdapter.notifyDataSetChanged();
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private void buildModel(Cursor cursor) {
        hzOrder.clear();
        langOrder.clear();
        byHz.clear();
        byLang.clear();

        if (cursor == null || cursor.getCount() == 0) return;

        int oldPosition = cursor.getPosition();
        LinkedHashSet<String> hzSeen = new LinkedHashSet<>();
        LinkedHashSet<String> langSeen = new LinkedHashSet<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String hz = cursor.getString(COL_HZ);
            String lang = cursor.getString(COL_LANG);
            String ipa = cursor.getString(COL_IPA);
            String zs = cursor.getString(COL_ZS);

            if (TextUtils.isEmpty(hz) || TextUtils.isEmpty(lang) || TextUtils.isEmpty(ipa)) {
                continue;
            }

            hzSeen.add(hz);
            langSeen.add(lang);
            Reading reading = new Reading(hz, lang, ipa, zs);

            byHz.computeIfAbsent(hz, k -> new LinkedHashMap<>())
                    .computeIfAbsent(lang, k -> new ArrayList<>())
                    .add(reading);
            byLang.computeIfAbsent(lang, k -> new LinkedHashMap<>())
                    .computeIfAbsent(hz, k -> new ArrayList<>())
                    .add(reading);
        }

        hzOrder.addAll(hzSeen);
        langOrder.addAll(langSeen);

        if (oldPosition >= 0 && oldPosition < cursor.getCount()) {
            cursor.moveToPosition(oldPosition);
        }
    }

    private int getColumnCount() {
        return orientation == Orientation.LANGUAGES_AS_ROWS ? hzOrder.size() : langOrder.size();
    }

    private int getRowCount() {
        return orientation == Orientation.LANGUAGES_AS_ROWS ? langOrder.size() : hzOrder.size();
    }

    private String getColumnKey(int position) {
        return orientation == Orientation.LANGUAGES_AS_ROWS
                ? hzOrder.get(position)
                : langOrder.get(position);
    }

    private String getRowKey(int position) {
        return orientation == Orientation.LANGUAGES_AS_ROWS
                ? langOrder.get(position)
                : hzOrder.get(position);
    }

    private List<Reading> getReadings(String rowKey, String columnKey) {
        if (orientation == Orientation.LANGUAGES_AS_ROWS) {
            Map<String, List<Reading>> row = byLang.get(rowKey);
            return row == null ? null : row.get(columnKey);
        }
        Map<String, List<Reading>> row = byHz.get(rowKey);
        return row == null ? null : row.get(columnKey);
    }

    private void resetHorizontalScrollState() {
        horizontalAnchorPosition = 0;
        horizontalAnchorOffsetPx = 0;
    }

    /**
     * Capture the source RecyclerView's absolute horizontal position, then
     * force every other visible horizontal list to that same absolute state.
     *
     * V3 synchronized with scrollBy(dx).  That only updated rows that happened
     * to be visible at the time, so recycled rows could later reappear with an
     * old offset.  V4 stores an absolute anchor (first visible item + its left
     * offset), which survives vertical recycling without drift.
     */
    private void onHorizontalScrolled(RecyclerView source, int dx) {
        if (syncingHorizontalScroll || dx == 0) return;

        // Programmatic scrollToPositionWithOffset() may dispatch onScrolled
        // while the RecyclerView is idle.  Only a RecyclerView that is actively
        // dragging/flinging is allowed to become the source of truth.
        if (source.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) return;

        captureHorizontalState(source);
        synchronizeVisibleHorizontalLists(source);
    }

    private void captureHorizontalState(RecyclerView source) {
        RecyclerView.LayoutManager manager = source.getLayoutManager();
        if (!(manager instanceof LinearLayoutManager)) return;

        LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
        int first = layoutManager.findFirstVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION) return;

        View firstView = layoutManager.findViewByPosition(first);
        if (firstView == null) return;

        horizontalAnchorPosition = first;
        horizontalAnchorOffsetPx = firstView.getLeft();
    }

    private void synchronizeVisibleHorizontalLists(RecyclerView source) {
        syncingHorizontalScroll = true;
        try {
            if (source != headerView) {
                alignHorizontal(headerView);
            }

            for (int i = 0; i < bodyView.getChildCount(); i++) {
                View child = bodyView.getChildAt(i);
                RecyclerView.ViewHolder holder = bodyView.getChildViewHolder(child);
                if (holder instanceof BodyRowHolder) {
                    RecyclerView rowCells = ((BodyRowHolder) holder).cells;
                    if (rowCells != source) {
                        alignHorizontal(rowCells);
                    }
                }
            }
        } finally {
            syncingHorizontalScroll = false;
        }
    }

    private void alignHorizontal(RecyclerView recyclerView) {
        if (getColumnCount() == 0) return;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (!(manager instanceof LinearLayoutManager)) return;

        int position = Math.max(0, Math.min(getColumnCount() - 1, horizontalAnchorPosition));
        ((LinearLayoutManager) manager).scrollToPositionWithOffset(
                position, horizontalAnchorOffsetPx);
    }

    private View makeCharacterLabel(String hz, int width) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setMinimumHeight(rowMinHeight);
        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        tv.setPadding(dp(8), dp(4), dp(8), dp(4));
        tv.setTextAppearance(R.style.FontDetail);
        FontUtil.setTypeface(tv);

        if (isMainPage) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(hz, new ForegroundColorSpan(getColor(HZ)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new RelativeSizeSpan(1.8f), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ssb);
        } else {
            tv.setText(hz);
        }
        return tv;
    }

    private View makeLanguageLabel(String lang, int width) {
        LinearLayout box = new LinearLayout(context);
        box.setGravity(Gravity.CENTER);
        box.setMinimumHeight(rowMinHeight);
        box.setPadding(dp(6), dp(5), dp(6), dp(5));
        box.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));

        View label = new View(context);
        int w = context.getResources().getDimensionPixelOffset(R.dimen.label_width);
        int h = context.getResources().getDimensionPixelOffset(R.dimen.label_height);
        label.setLayoutParams(new LinearLayout.LayoutParams(w, h));

        int endangeredBorderColor = DB.getHistoryColor(lang);
        boolean hasEndangeredColor = endangeredBorderColor != Color.TRANSPARENT;
        languageLabelBuilder.borderColor(hasEndangeredColor ? endangeredBorderColor : Integer.MIN_VALUE);
        languageLabelBuilder.borderThickness(hasEndangeredColor ? 6 : 3);

        boolean colorByScheme = Pref.getBool(R.string.pref_key_custom_language_color_by_scheme, false)
                && Pref.getFilter() == DB.FILTER.CUSTOM;
        Drawable drawable;
        if (colorByScheme) {
            int c = Pref.getCustomLanguageSchemeColor(lang);
            drawable = languageLabelBuilder.build(lang, c, c);
        } else {
            drawable = languageLabelBuilder.build(lang, getColor(lang), getSubColor(lang));
        }

        label.setBackground(drawable);
        label.setContentDescription(DB.getLanguageByLabel(lang));
        label.setOnClickListener(v -> App.info(v.getContext(), lang));
        box.addView(label);
        return box;
    }

    private View makeReadingCell(List<Reading> readings) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setMinimumHeight(rowMinHeight);
        cell.setPadding(dp(6), dp(4), dp(6), dp(4));
        cell.setLayoutParams(new ViewGroup.LayoutParams(cellWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (readings == null || readings.isEmpty()) {
            TextView dash = new TextView(context);
            dash.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            dash.setMinimumHeight(rowMinHeight - dp(8));
            dash.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(dash);
            dash.setText("—");
            cell.addView(dash, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return cell;
        }

        for (Reading reading : readings) {
            TextView tv = new TextView(context);
            tv.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(tv);
            tv.setPadding(dp(4), dp(2), dp(4), dp(2));
            tv.setGravity(Gravity.CENTER_VERTICAL);

            SpannableStringBuilder text = new SpannableStringBuilder();
            String ipa = DisplayHelper.formatIPA(reading.lang, reading.ipa).toString();
            if (ipa.contains("<") && !ipa.contains(">")) ipa = ipa.replace("<", "&lt;");
            text.append(HtmlCompat.fromHtml(ipa, HtmlCompat.FROM_HTML_MODE_COMPACT));

            if (!TextUtils.isEmpty(reading.zs)) {
                String zs = DisplayHelper.formatZS(reading.hz, reading.zs);
                text.append(HtmlCompat.fromHtml(zs, HtmlCompat.FROM_HTML_MODE_COMPACT));
            }

            tv.setText(text);
            cell.addView(tv, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        return cell;
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        final FrameLayout host;

        HeaderHolder(@NonNull FrameLayout host) {
            super(host);
            this.host = host;
        }
    }

    private class HeaderAdapter extends RecyclerView.Adapter<HeaderHolder> {
        @NonNull
        @Override
        public HeaderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout host = new FrameLayout(context);
            host.setMinimumHeight(rowMinHeight);
            host.setLayoutParams(new RecyclerView.LayoutParams(cellWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderHolder(host);
        }

        @Override
        public void onBindViewHolder(@NonNull HeaderHolder holder, int position) {
            holder.host.removeAllViews();
            String key = getColumnKey(position);
            View child = orientation == Orientation.LANGUAGES_AS_ROWS
                    ? makeCharacterLabel(key, cellWidth)
                    : makeLanguageLabel(key, cellWidth);
            holder.host.addView(child, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @Override
        public int getItemCount() {
            return getColumnCount();
        }
    }

    private class BodyRowHolder extends RecyclerView.ViewHolder {
        final FrameLayout labelHost;
        final RecyclerView cells;
        final CellAdapter cellAdapter;

        BodyRowHolder(@NonNull View itemView, FrameLayout labelHost, RecyclerView cells) {
            super(itemView);
            this.labelHost = labelHost;
            this.cells = cells;
            this.cellAdapter = new CellAdapter();

            cells.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            cells.setAdapter(cellAdapter);
            cells.setRecycledViewPool(cellPool);
            cells.setNestedScrollingEnabled(false);
            cells.setHorizontalScrollBarEnabled(false);
            cells.setItemAnimator(null);
            cells.setOverScrollMode(View.OVER_SCROLL_NEVER);
            cells.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    onHorizontalScrolled(recyclerView, dx);
                }
            });
        }
    }

    private class BodyAdapter extends RecyclerView.Adapter<BodyRowHolder> {
        @NonNull
        @Override
        public BodyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            row.setMinimumHeight(rowMinHeight);
            row.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            FrameLayout labelHost = new FrameLayout(context);
            labelHost.setMinimumHeight(rowMinHeight);
            labelHost.setLayoutParams(new LinearLayout.LayoutParams(
                    labelWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(labelHost);

            RecyclerView cells = new RecyclerView(context);
            cells.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f));
            row.addView(cells);

            return new BodyRowHolder(row, labelHost, cells);
        }

        @Override
        public void onBindViewHolder(@NonNull BodyRowHolder holder, int position) {
            String rowKey = getRowKey(position);
            holder.labelHost.removeAllViews();

            View rowLabel = orientation == Orientation.LANGUAGES_AS_ROWS
                    ? makeLanguageLabel(rowKey, labelWidth)
                    : makeCharacterLabel(rowKey, labelWidth);
            holder.labelHost.addView(rowLabel, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            holder.cells.stopScroll();
            holder.cellAdapter.setRowKey(rowKey);
            alignHorizontal(holder.cells);
        }

        @Override
        public int getItemCount() {
            return getRowCount();
        }
    }

    private class CellHolder extends RecyclerView.ViewHolder {
        final FrameLayout host;

        CellHolder(@NonNull FrameLayout host) {
            super(host);
            this.host = host;
        }
    }

    private class CellAdapter extends RecyclerView.Adapter<CellHolder> {
        private String rowKey;

        void setRowKey(String rowKey) {
            this.rowKey = rowKey;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CellHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout host = new FrameLayout(context);
            host.setMinimumHeight(rowMinHeight);
            host.setLayoutParams(new RecyclerView.LayoutParams(
                    cellWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new CellHolder(host);
        }

        @Override
        public void onBindViewHolder(@NonNull CellHolder holder, int position) {
            holder.host.removeAllViews();
            String columnKey = getColumnKey(position);
            View cell = makeReadingCell(getReadings(rowKey, columnKey));
            holder.host.addView(cell, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @Override
        public int getItemCount() {
            return getColumnCount();
        }
    }
}
