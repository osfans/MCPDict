package com.osfans.mcpdict;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.osfans.mcpdict.Adapter.PagerAdapter;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.Favorite.FavoriteFragment;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.Util.UserDB;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mPager;

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
            Utils.info(this, "");
            return true;
        }
        if (id == R.id.menu_item_help) {
            Utils.help(this);
            return true;
        }
        if (id == R.id.menu_item_about) {
            Utils.about(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale();
        Utils.setActivityTheme(this);
        DB.initFQ();
        // Initialize the some "static" classes on separate threads
        new Thread(()-> Orthography.initialize(getResources())).start();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                UserDB.initialize(MainActivity.this);
                DB.initialize(MainActivity.this);
                return null;
            }
            protected void onPostExecute(Void result) {
                if (getDictionaryFragment()!=null)
                    getDictionaryFragment().refreshAdapter();
            }
        }.execute();

        new Thread(()-> FavoriteDialogs.initialize(MainActivity.this)).start();

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
        setTitle(Pref.getTitle());
        // Make settings take effect immediately as the user navigates back to the dictionary
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
