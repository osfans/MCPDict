package com.osfans.mcpdict.Util;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    // STATIC VARIABLES AND METHODS

    private static final String DATABASE_NAME = "user";
    private static final int DATABASE_VERSION = 1;

    private static WeakReference<Context> mContext;

    private static SQLiteDatabase db = null;

    public static Context getContext() {
        return mContext.get();
    }

    public static void initialize(Context context) {
        if (db != null) return;
        mContext = new WeakReference<>(context);
        db = new UserDatabase(context).getWritableDatabase();
    }

    public static String getDatabasePath() {
        return getContext().getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    public static String getBackupPath() {
        return getContext().getExternalFilesDir(null) + "/" + DATABASE_NAME + ".db";
    }

    // "READ" OPERATIONS

    public static Cursor selectAllFavorites() {
        String query = "SELECT rowid AS _id, hz, comment, " +
                       "STRFTIME('%Y/%m/%d', timestamp, 'localtime') AS local_timestamp " +
                       "FROM favorite ORDER BY timestamp DESC";
        return db.rawQuery(query, null);
    }

    // "WRITE" OPERATIONS

    public static void insertFavorite(String hz, String comment) {
        ContentValues values = new ContentValues();
        values.put("hz", hz);
        values.put("comment", comment);
        db.insert("favorite", null, values);
    }

    public static void updateFavorite(String hz, String comment) {
        ContentValues values = new ContentValues();
        values.put("comment", comment);
        String[] args = {hz};
        db.update("favorite", values, "hz = ?", args);
    }

    public static void deleteFavorite(String hz) {
        String[] args = {hz};
        db.delete("favorite", "hz = ?", args);
    }

    public static void deleteAllFavorites() {
        db.delete("favorite", null, null);
    }

    // EXPORTING AND IMPORTING

    public static void exportFavorites() throws IOException {
        FileUtils.copyFile(getDatabasePath(), getBackupPath());
    }

    public static int selectBackupFavoriteCount() {
        db.execSQL("ATTACH DATABASE '" + getBackupPath() + "' AS backup");
        String query = "SELECT rowid, hz, comment, timestamp FROM backup.favorite";
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        db.execSQL("DETACH DATABASE backup");
        return count;
    }

    public static void importFavoritesOverwrite() throws IOException {
        FileUtils.copyFile(getBackupPath(), getDatabasePath());
    }

    public static void importFavoritesMix() {
        db.execSQL("ATTACH DATABASE '" + getBackupPath() + "' AS backup");
        db.execSQL("DELETE FROM favorite WHERE hz IN (SELECT hz FROM backup.favorite)");
        db.execSQL("INSERT INTO favorite(hz, comment, timestamp) SELECT hz, comment, timestamp FROM backup.favorite");
        db.execSQL("DETACH DATABASE backup");
    }

    public static void importFavoritesAppend() {
        db.execSQL("ATTACH DATABASE '" + getBackupPath() + "' AS backup");
        db.execSQL("DELETE FROM favorite WHERE hz IN (SELECT hz FROM backup.favorite)");
        db.execSQL("INSERT INTO favorite(hz, comment) SELECT hz, comment FROM backup.favorite");
        db.execSQL("DETACH DATABASE backup");
    }

    // NON-STATIC METHODS IMPLEMENTING THOSE OF THE ABSTRACT SUPER-CLASS

    public UserDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favorite (" +
                   "    hz TEXT UNIQUE NOT NULL," +
                   "    comment TEXT," +
                   "    timestamp REAL DEFAULT (JULIANDAY('now')) NOT NULL" +
                   ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
