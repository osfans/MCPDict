package com.osfans.mcpdict.wordlist;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.R;
import com.osfans.mcpdict.UI.RefreshableFragment;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;

public class WordFragment extends Fragment implements RefreshableFragment {
    public WordFragment() {
        super(R.layout.fragment_word);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView query = view.findViewById(R.id.text_query);
        query.setHint(R.string.search_word_hint);
        query.setEnabled(false);
        FontUtil.setTypeface(query);
        view.findViewById(R.id.button_clear).setVisibility(View.GONE);
        view.findViewById(R.id.button_search).setEnabled(false);
        view.findViewById(R.id.button_keyboard).setEnabled(false);

        view.findViewById(R.id.word_exit_compact).setOnClickListener(button -> setCompact(false));
        view.findViewById(R.id.word_options).setOnClickListener(this::showOptions);
    }

    @Override public void onResume() {
        super.onResume();
        applyCompact(Pref.getBool(R.string.pref_key_word_fullscreen, false));
    }

    @Override public void onPause() {
        showActionBar(!Pref.getBool(R.string.pref_key_fullscreen, false));
        super.onPause();
    }

    @Override public void refresh() {}

    private void showOptions(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.inflate(R.menu.word_tool);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.menu_item_word_fullscreen) return false;
            setCompact(true);
            return true;
        });
        menu.show();
    }

    private void setCompact(boolean compact) {
        Pref.putBool(R.string.pref_key_word_fullscreen, compact);
        applyCompact(compact);
    }

    private void applyCompact(boolean compact) {
        View view = requireView();
        view.findViewById(R.id.word_filter_row).setVisibility(compact ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.word_exit_compact).setVisibility(compact ? View.VISIBLE : View.GONE);
        showActionBar(!compact);
    }

    private void showActionBar(boolean show) {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar == null) return;
        if (show) actionBar.show();
        else actionBar.hide();
    }
}
