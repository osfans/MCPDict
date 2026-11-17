package com.osfans.mcpdict.Adapter;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.osfans.mcpdict.DictFragment;
import com.osfans.mcpdict.Favorite.FavoriteFragment;
import com.osfans.mcpdict.UI.GuessHzFragment;
import com.osfans.mcpdict.UI.GuessLangFragment;
import com.osfans.mcpdict.wordlist.WordFragment;

public class PagerAdapter extends FragmentStateAdapter {
    public enum PAGE {
        DICTIONARY, FAVORITE, GUESS_LANG, GUESS_HZ, WORD
    }

    public PagerAdapter(FragmentActivity fa) {
        super(fa);
        createFragments();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == PAGE.FAVORITE.ordinal()) return new FavoriteFragment();
        if (position == PAGE.GUESS_HZ.ordinal()) return new GuessHzFragment();
        if (position == PAGE.GUESS_LANG.ordinal()) return new GuessLangFragment();
        if (position == PAGE.WORD.ordinal()) return new WordFragment();
        return new DictFragment();
    }

    private void createFragments() {
        for (PAGE page : PAGE.values()) {
            createFragment(page.ordinal());
        }
    }

    @Override
    public int getItemCount() {
        return PAGE.values().length;
    }
}
