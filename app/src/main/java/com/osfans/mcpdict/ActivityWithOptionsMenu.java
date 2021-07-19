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

public class ActivityWithOptionsMenu extends AppCompatActivity {

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
        if (id == R.id.menu_item_help) {
            intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_item_about) {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.about)
                    .setMessage(HtmlCompat.fromHtml(getString(R.string.about_message, BuildConfig.VERSION_NAME), HtmlCompat.FROM_HTML_MODE_COMPACT))
                    .setPositiveButton(R.string.ok, null)
                    .show();
            TextView messageText = dialog.findViewById(android.R.id.message);
            assert messageText != null;
            messageText.setGravity(Gravity.CENTER);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
