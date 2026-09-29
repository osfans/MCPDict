package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private RecyclerView mMatrixHeaderView, mMatrixBodyView;
    private Button mButtonList, mButtonTable, mButtonTranspose;
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

        // V3 matrix view: frozen top header + frozen left column.
        mMatrixLayout = selfView.findViewById(R.id.matrix_layout);
        mMatrixCorner = selfView.findViewById(R.id.matrix_corner);
        mMatrixHeaderView = selfView.findViewById(R.id.matrix_header_view);
        mMatrixBodyView = selfView.findViewById(R.id.matrix_body_view);
        mMatrixTableView = new MatrixTableView(
                requireContext(),
                mMatrixHeaderView,
                mMatrixBodyView,
                isMainPage);

        ViewGroup.LayoutParams cornerParams = mMatrixCorner.getLayoutParams();
        cornerParams.width = mMatrixTableView.getLabelWidth();
        mMatrixCorner.setLayoutParams(cornerParams);

        // View controls.
        mButtonList = selfView.findViewById(R.id.button_view_list);
        mButtonTable = selfView.findViewById(R.id.button_view_table);
        mButtonTranspose = selfView.findViewById(R.id.button_table_transpose);

        mButtonList.setOnClickListener(v -> setTableMode(false));
        mButtonTable.setOnClickListener(v -> setTableMode(true));
        mButtonTranspose.setOnClickListener(v -> {
            MatrixTableView.Orientation next =
                    mMatrixTableView.getOrientation() == MatrixTableView.Orientation.LANGUAGES_AS_ROWS
                            ? MatrixTableView.Orientation.CHARACTERS_AS_ROWS
                            : MatrixTableView.Orientation.LANGUAGES_AS_ROWS;
            mMatrixTableView.setOrientation(next);
            updateTransposeLabel();
        });

        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));
        setTableMode(false);
        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setTableMode(boolean enabled) {
        tableMode = enabled;
        if (mRecyclerView == null) return;

        mRecyclerView.setVisibility(enabled ? View.GONE : View.VISIBLE);
        mMatrixLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mButtonTranspose.setVisibility(enabled ? View.VISIBLE : View.GONE);

        boolean showIndex = isMainPage && !enabled;
        mIndexView.setVisibility(showIndex ? View.VISIBLE : View.GONE);
        mIndexDivider.setVisibility(showIndex ? View.VISIBLE : View.GONE);

        mButtonList.setEnabled(enabled);
        mButtonTable.setEnabled(!enabled);

        if (enabled) {
            mMatrixTableView.setCursor(mCursor);
            updateTransposeLabel();
        }
    }

    private void updateTransposeLabel() {
        if (mButtonTranspose == null || mMatrixTableView == null) return;
        if (mMatrixTableView.getOrientation() == MatrixTableView.Orientation.LANGUAGES_AS_ROWS) {
            mButtonTranspose.setText("↔ 行列：語言在行");
        } else {
            mButtonTranspose.setText("↔ 行列：漢字在行");
        }
    }

    public void setData(Cursor cursor) {
        mCursor = cursor;
        mIndexAdapter.changeCursor(cursor, mRecyclerView);
        mResultAdapter.changeCursor(cursor);
        if (tableMode) mMatrixTableView.setCursor(cursor);
    }
}
