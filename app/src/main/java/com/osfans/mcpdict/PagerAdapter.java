package com.osfans.mcpdict;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.osfans.mcpdict.Favorite.FavoriteFragment;

public class PagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;
    public static final int PAGE_DICTIONARY = 0;
    public static final int PAGE_FAVORITE = 1;

    public PagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == PAGE_FAVORITE) return new FavoriteFragment();
        return new DictFragment();
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
