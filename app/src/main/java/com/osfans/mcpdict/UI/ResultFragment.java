package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.Adapter.IndexAdapter;
import com.osfans.mcpdict.Adapter.ResultAdapter;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.Pref;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ResultFragment extends Fragment {

    private View selfView;
    private RecyclerView mIndexView, mRecyclerView;
    private IndexAdapter mIndexAdapter;
    private ResultAdapter mResultAdapter;
    private final boolean isMainPage;

    private View mIndexDivider;
    private View mMatrixLayout;
    private View mMatrixCorner;
    private SeekBar mMatrixHorizontalScrollbar;
    private RecyclerView mMatrixHeaderView, mMatrixBodyView;
    private MatrixTableView mMatrixTableView;
    private Cursor mCursor;
    private boolean tableMode = false;

    public ResultFragment() {
        this(true);
    }

    public ResultFragment(boolean isMainPage) {
        super();
        this.isMainPage = isMainPage;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // A hack to avoid nested fragments from being inflated twice
        // Reference: http://stackoverflow.com/a/14695397
        if (selfView != null) {
            ViewGroup parent = (ViewGroup) selfView.getParent();
            if (parent != null) parent.removeView(selfView);
            return selfView;
        }

        selfView = inflater.inflate(R.layout.search_result, container, false);

        // Original list view.
        mIndexView = selfView.findViewById(R.id.index_view);
        mIndexDivider = selfView.findViewById(R.id.index_divider);
        mIndexView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(requireContext(), LinearLayoutManager.HORIZONTAL);
        mIndexView.addItemDecoration(dividerItemDecoration);

        mRecyclerView = selfView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mIndexAdapter = new IndexAdapter(mRecyclerView);
        mIndexView.setAdapter(mIndexAdapter);
        mResultAdapter = new ResultAdapter(isMainPage);
        mRecyclerView.setAdapter(mResultAdapter);
        new FastScrollerBuilder(mRecyclerView).build();

        // Matrix view: frozen top header + frozen left column.
        mMatrixLayout = selfView.findViewById(R.id.matrix_layout);
        mMatrixCorner = selfView.findViewById(R.id.matrix_corner);
        mMatrixHorizontalScrollbar = selfView.findViewById(R.id.matrix_horizontal_scrollbar);
        mMatrixHeaderView = selfView.findViewById(R.id.matrix_header_view);
        mMatrixBodyView = selfView.findViewById(R.id.matrix_body_view);
        mMatrixTableView = new MatrixTableView(
                requireContext(),
                mMatrixHeaderView,
                mMatrixBodyView,
                mMatrixHorizontalScrollbar,
                isMainPage);

        boolean languageLeft = Pref.isTableLanguageLeft();

        mMatrixTableView.setOrientation(
                languageLeft
                        ? MatrixTableView.Orientation.LANGUAGES_AS_ROWS
                        : MatrixTableView.Orientation.CHARACTERS_AS_ROWS
        );

        // Vertical fast scroller: keep the library's normal auto-hide behavior.
        // Horizontal fast scrolling is handled by the custom bottom SeekBar,
        // because AndroidFastScroll is designed around vertical RecyclerView use.
        new FastScrollerBuilder(mMatrixBodyView).build();

        updateMatrixCornerWidth();

        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));
        setTableMode(false);
        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateMatrixCornerWidth() {
        if (mMatrixCorner == null || mMatrixTableView == null) return;

        int targetWidth = mMatrixTableView.getLabelWidth();

        ViewGroup.LayoutParams cornerParams = mMatrixCorner.getLayoutParams();
        if (cornerParams.width != targetWidth) {
            cornerParams.width = targetWidth;
            mMatrixCorner.setLayoutParams(cornerParams);
        }

        if (mMatrixHorizontalScrollbar != null) {
            ViewGroup.LayoutParams rawParams = mMatrixHorizontalScrollbar.getLayoutParams();
            if (rawParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams scrollbarParams =
                        (ViewGroup.MarginLayoutParams) rawParams;
                if (scrollbarParams.leftMargin != targetWidth) {
                    scrollbarParams.leftMargin = targetWidth;
                    mMatrixHorizontalScrollbar.setLayoutParams(scrollbarParams);
                }
            }
        }
    }

    private void setTableMode(boolean enabled) {
        tableMode = enabled;
        if (mRecyclerView == null) return;

        mRecyclerView.setVisibility(enabled ? View.GONE : View.VISIBLE);
        mMatrixLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);

        boolean showIndex = isMainPage && !enabled;
        mIndexView.setVisibility(showIndex ? View.VISIBLE : View.GONE);
        mIndexDivider.setVisibility(showIndex ? View.VISIBLE : View.GONE);

        if (enabled) {
            updateMatrixCornerWidth();
            mMatrixTableView.setCursor(mCursor);
        }
    }

    public boolean isTableMode() {
        return tableMode;
    }

    public MatrixTableView.Orientation getTableOrientation() {
        if (mMatrixTableView != null) {
            return mMatrixTableView.getOrientation();
        }
        return Pref.isTableLanguageLeft()
                ? MatrixTableView.Orientation.LANGUAGES_AS_ROWS
                : MatrixTableView.Orientation.CHARACTERS_AS_ROWS;
    }

    public void showListMode() {
        setTableMode(false);
    }

    public void showTableMode(MatrixTableView.Orientation orientation) {
        // Persist the exact orientation that is actually being displayed.
        // This keeps the menu dot, the current table and the next app/session
        // all on the same source of truth.
        Pref.setTableLanguageLeft(
                orientation == MatrixTableView.Orientation.LANGUAGES_AS_ROWS
        );

        if (mMatrixTableView != null) {
            mMatrixTableView.setOrientation(orientation);
            updateMatrixCornerWidth();
        }
        setTableMode(true);
    }

    public void setData(Cursor cursor) {
        mCursor = cursor;
        mIndexAdapter.changeCursor(cursor, mRecyclerView);
        mResultAdapter.changeCursor(cursor);
        if (tableMode) mMatrixTableView.setCursor(cursor);
    }
}
