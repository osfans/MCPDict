package com.osfans.mcpdict.Favorite;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.osfans.mcpdict.MainActivity;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FileUtils;
import com.osfans.mcpdict.Util.UserDatabase;
import com.osfans.mcpdict.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class FavoriteDialogs {

    private static MainActivity activity;

    private static int importMode;

    public static void initialize(MainActivity activity) {
       FavoriteDialogs.activity = activity;
    }

    public static void add(final String hz) {
        final EditText editText = new EditText(activity);
        editText.setHint(R.string.favorite_add_hint);
        editText.setSingleLine(false);
        new AlertDialog.Builder(activity)
            .setIcon(android.R.drawable.btn_star_big_on)
            .setTitle(String.format(activity.getString(R.string.favorite_add), hz))
            .setView(editText)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String comment = editText.getText().toString();
                UserDatabase.insertFavorite(hz, comment);
                String message = String.format(activity.getString(R.string.favorite_add_done), hz);
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                FavoriteFragment fragment = activity.getFavoriteFragment();
                if (fragment != null) {
                    fragment.notifyAddItem();
                    fragment.refresh();
                }
                activity.refresh();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    public static void view(final String hz, String comment) {
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.btn_star_big_on)
                .setTitle(String.format(activity.getString(R.string.favorite_view), hz))
                .setMessage(comment)
                .setPositiveButton(String.format(activity.getString(R.string.favorite_edit_2lines), hz),
                        (dialog, which) -> FavoriteDialogs.edit(hz, comment))
                .setNegativeButton(String.format(activity.getString(R.string.favorite_delete_2lines), hz),
                        (dialog, which) -> FavoriteDialogs.delete(hz, false))
                .setNeutralButton(R.string.back, null)
                .show();
    }

    public static void view(final String hz, final View view) {
        view(hz, ((TextView) view.findViewById(R.id.text_comment)).getText().toString());
    }

    public static void edit(final String hz, String comment) {
        final EditText editText = new EditText(activity);
        editText.setText(comment);
        editText.setSingleLine(false);
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.btn_star_big_on)
                .setTitle(String.format(activity.getString(R.string.favorite_edit), hz))
                .setView(editText)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String s = editText.getText().toString();
                    UserDatabase.updateFavorite(hz, s);
                    String message = String.format(activity.getString(R.string.favorite_edit_done), hz);
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    activity.getCurrentFragment().refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void delete(final String hz, boolean force) {
        if (force) {
            UserDatabase.deleteFavorite(hz);
            String message = String.format(activity.getString(R.string.favorite_delete_done), hz);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            FavoriteFragment fragment = activity.getFavoriteFragment();
            if (fragment != null) {
                FavoriteAdapter adapter = (FavoriteAdapter) fragment.getListAdapter();
                assert adapter != null;
                adapter.collapseItem(hz);
                fragment.refresh();
            }
            activity.refresh();
            return;
        }

        final SharedPreferences sp = Utils.getPreference();
        final String prefKey = activity.getString(R.string.pref_key_favorite_delete_no_confirm_expiry);
        long expiry = sp.getLong(prefKey, 0);
        long now = System.currentTimeMillis();
        boolean expired = (expiry == 0 || now > expiry);
        if (!expired) {
            delete(hz, true);
            return;
        }

        final CheckBox checkBox = new CheckBox(activity);
        checkBox.setText(R.string.favorite_delete_no_confirm);
        new AlertDialog.Builder(activity)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(String.format(activity.getString(R.string.favorite_delete), hz))
            .setMessage(String.format(activity.getString(R.string.favorite_delete_confirm), hz))
            .setView(checkBox)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                delete(hz, true);
                if (checkBox.isChecked()) {
                    sp.edit().putLong(prefKey, System.currentTimeMillis() + 3600000).apply();
                        // No confirmation for 1 hour
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    public static void deleteAll() {
        new AlertDialog.Builder(activity)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(activity.getString(R.string.favorite_clear))
            .setMessage(activity.getString(R.string.favorite_clear_confirm))
            .setPositiveButton(R.string.clear, (dialog, which) -> {
                UserDatabase.deleteAllFavorites();
                String message = activity.getString(R.string.favorite_clear_done);
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                FavoriteFragment fragment = activity.getFavoriteFragment();
                if (fragment != null) {
                    FavoriteAdapter adapter = (FavoriteAdapter) fragment.getListAdapter();
                    assert adapter != null;
                    adapter.collapseAll();
                    fragment.refresh();
                }
                activity.refresh();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    public static void export(boolean force) {
        File backupFile = new File(UserDatabase.getBackupPath());
        if (force || !backupFile.exists()) {
            try {
                UserDatabase.exportFavorites();
                new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(activity.getString(R.string.favorite_export))
                    .setMessage(String.format(activity.getString(R.string.favorite_export_done),
                                              UserDatabase.getBackupPath()))
                    .setPositiveButton(R.string.ok, null)
                    .show();
            }
            catch (IOException e) {
                crash(e);
            }
        }
        else {
            long timestamp = backupFile.lastModified();
            new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(activity.getString(R.string.favorite_export))
                .setMessage(String.format(activity.getString(R.string.favorite_export_overwrite),
                            UserDatabase.getBackupPath(),
                            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timestamp))))
                .setPositiveButton(R.string.overwrite, (dialog, which) -> export(true))
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
    }

    public static void import_(int state) {
        // States:
        //   0: check if the backup file exists, is readable, and contains entries,
        //      and display info about the backup file
        //   1: prompt for import mode
        //   2: do the importing, and (optionally) delete the backup file

        switch (state) {
        case 0:
            File backupFile = new File(UserDatabase.getBackupPath());
            if (!backupFile.exists()) {
                new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle(activity.getString(R.string.favorite_import))
                    .setMessage(String.format(activity.getString(R.string.favorite_import_file_not_found),
                                              UserDatabase.getBackupPath()))
                    .setPositiveButton(R.string.ok, null)
                    .show();
                break;
            }

            int count;
            try {
                count = UserDatabase.selectBackupFavoriteCount();
            }
            catch (SQLiteException e) {
                new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle(activity.getString(R.string.favorite_import))
                    .setMessage(String.format(activity.getString(R.string.favorite_import_read_fail),
                                              UserDatabase.getBackupPath()))
                    .setPositiveButton(R.string.ok, null)
                    .show();
                break;
            }

            if (count == 0) {
                new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle(activity.getString(R.string.favorite_import))
                    .setMessage(String.format(activity.getString(R.string.favorite_import_empty_file),
                                              UserDatabase.getBackupPath()))
                    .setPositiveButton(R.string.ok, null)
                    .show();
                break;
            }

            if (UserDatabase.selectAllFavorites().getCount() == 0) {
                new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(activity.getString(R.string.favorite_import))
                .setMessage(String.format(activity.getString(R.string.favorite_import_detail),
                                          UserDatabase.getBackupPath(),
                                          count))
                .setPositiveButton(R.string.import_, (dialog, which) -> {
                    importMode = 0;
                    import_(2);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
            }
            else {
                new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(activity.getString(R.string.favorite_import))
                    .setMessage(String.format(activity.getString(R.string.favorite_import_detail_select_mode),
                                              UserDatabase.getBackupPath(),
                                              count))
                    .setPositiveButton(R.string.next, (dialog, which) -> import_(1))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            }
            break;

        case 1:
            new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_menu_help)
                .setTitle(activity.getString(R.string.favorite_import_select_mode))
                .setSingleChoiceItems(R.array.favorite_import_modes, -1, null)
                .setPositiveButton(R.string.import_, (dialog, which) -> {
                    importMode = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    import_(2);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
            break;

        case 2:
            try {
                switch (importMode) {
                    case 0: UserDatabase.importFavoritesOverwrite(); break;
                    case 1: UserDatabase.importFavoritesMix(); break;
                    case 2: UserDatabase.importFavoritesAppend(); break;
                }
            }
            catch (IOException | SQLiteException e) {
                crash(e);
                break;
            }

            FavoriteFragment fragment = activity.getFavoriteFragment();
            if (fragment != null) {
                fragment.notifyAddItem();
                FavoriteAdapter adapter = (FavoriteAdapter) fragment.getListAdapter();
                Objects.requireNonNull(adapter).collapseAll();
                fragment.refresh();
            }
            activity.refresh();

            new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(activity.getString(R.string.favorite_import))
                .setMessage(String.format(activity.getString(R.string.favorite_import_done),
                        UserDatabase.getBackupPath()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    File backupFile1 = new File(UserDatabase.getBackupPath());
                    boolean deleted = backupFile1.delete();
                    String message = activity.getString(deleted ?
                                                        R.string.favorite_import_delete_backup_done :
                                                        R.string.favorite_import_delete_backup_fail);
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.keep, null)
                .show();
            break;
        }
    }

    public static void crash(Throwable e) {
        try {
            String logPath = activity.getExternalFilesDir(null) + "/crash.log";
            FileUtils.dumpException(logPath, e);
            new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_delete)
                .setTitle(activity.getString(R.string.crash))
                .setMessage(String.format(activity.getString(R.string.crash_saved), logPath))
                .setPositiveButton(R.string.ok, null)
                .show();
        }
        catch (IOException ex) {
            new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_delete)
                .setTitle(activity.getString(R.string.crash))
                .setMessage(activity.getString(R.string.crash_unsaved))
                .setPositiveButton(R.string.ok, null)
                .show();
        }
    }
}
