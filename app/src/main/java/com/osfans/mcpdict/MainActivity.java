package com.osfans.mcpdict;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.osfans.mcpdict.Adapter.PagerAdapter;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.Favorite.FavoriteFragment;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.UI.RefreshableFragment;
import com.osfans.mcpdict.Favorite.UserDB;
import com.osfans.mcpdict.Util.App;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mPager;
    ExecutorService mExecutorService;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        if (id == R.id.menu_item_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_item_info) {
            App.info(this, "");
            return true;
        }
        if (id == R.id.menu_item_help) {
            App.help(this);
            return true;
        }
        if (id == R.id.menu_item_about) {
            App.about(this);
            return true;
        }
        if (id == R.id.menu_item_home) {
            if (getDictionaryFragment() != null) {
                getDictionaryFragment().refresh("", "");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.setLocale();
        App.setActivityTheme(this);
        mExecutorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        mExecutorService.execute(() -> {
            //Background work here
            Orthography.initialize(getResources());
            UserDB.initialize(this);
            DB.initialize(this);
            DB.initFQ();
            FavoriteDialogs.initialize(this);
            handler.post(() -> {
                //UI Thread work here
                if (getDictionaryFragment() != null) getDictionaryFragment().refreshAdapter();
            });
        });

        // Set up activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mPager = findViewById(R.id.pager);
        mPager.setUserInputEnabled(true);
        initAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.close();
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
        refresh();
    }

    public RefreshableFragment getCurrentFragment() {
        return getFragment(mPager.getCurrentItem());
    }

    public DictFragment getDictionaryFragment() {
        return (DictFragment) getFragment(PagerAdapter.PAGE_DICTIONARY);
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
}
