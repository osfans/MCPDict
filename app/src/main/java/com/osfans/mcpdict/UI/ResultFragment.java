package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // Inflate the fragment view
        selfView = inflater.inflate(R.layout.search_result, container, false);
        mIndexView = selfView.findViewById(R.id.index_view);
        mIndexView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), LinearLayoutManager.HORIZONTAL);
        mIndexView.addItemDecoration(dividerItemDecoration);
        mRecyclerView = selfView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mIndexAdapter = new IndexAdapter(mRecyclerView);
        mIndexView.setAdapter(mIndexAdapter);
        mResultAdapter = new ResultAdapter(isMainPage);
        mRecyclerView.setAdapter(mResultAdapter);
        new FastScrollerBuilder(mRecyclerView).build();
        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));

        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setData(Cursor cursor) {
        mIndexView.setVisibility(isMainPage ? View.VISIBLE : View.GONE);
        mIndexAdapter.changeCursor(cursor, mRecyclerView);
        mResultAdapter.changeCursor(cursor);
    }

}
