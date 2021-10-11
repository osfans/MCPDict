package com.osfans.mcpdict;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends ActivityWithOptionsMenu {

    private ViewPager2 mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale(this);
        // Initialize the some "static" classes on separate threads
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Orthography.initialize(getResources());
                return null;
            }
        }.execute();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                UserDatabase.initialize(MainActivity.this);
                MCPDatabase.initialize(MainActivity.this);
                return null;
            }
            protected void onPostExecute(Void result) {
                if (getDictionaryFragment()!=null)
                    getDictionaryFragment().refreshAdapter();
            }
        }.execute();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                FavoriteDialogs.initialize(MainActivity.this);
                return null;
            }
        }.execute();

        // Set up activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mPager = findViewById(R.id.pager);
        initAdapter();
    }

    private void initAdapter() {
        PagerAdapter mAdapter = new PagerAdapter(this);
        mAdapter.createFragment(PagerAdapter.PAGE_DICTIONARY);
        mAdapter.createFragment(PagerAdapter.PAGE_FAVORITE);
        mPager.setAdapter(mAdapter);
    }

    private RefreshableFragment getFragment(int index) {
        return (RefreshableFragment) getSupportFragmentManager().findFragmentByTag("f" + index);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        // Make settings take effect immediately as the user navigates back to the dictionary
        refresh();
    }

    public RefreshableFragment getCurrentFragment() {
        return getFragment(mPager.getCurrentItem());
    }

    public DictionaryFragment getDictionaryFragment() {
        return (DictionaryFragment) getFragment(PagerAdapter.PAGE_DICTIONARY);
    }

    public FavoriteFragment getFavoriteFragment() {
        return (FavoriteFragment) getFragment(PagerAdapter.PAGE_FAVORITE);
    }

    public void refresh() {
        RefreshableFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.refresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String title = sp.getString(getString(R.string.pref_key_custom_title), getString(R.string.app_name));
        setTitle(title);
    }
}
