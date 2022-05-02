package com.osfans.mcpdict;

import android.app.Dialog;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

public class BaseActivity extends AppCompatActivity {

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
            DictApp.info(this, "");
            return true;
        }
        if (id == R.id.menu_item_help) {
            DictApp.help(this);
            return true;
        }
        if (id == R.id.menu_item_about) {
            DictApp.about(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
