package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MCPDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "mcpdict.db";
    private static final int DATABASE_VERSION = 15;

    // Must be the same order as defined in the string array "search_as"

    public static final String SEARCH_AS_HZ = "hz";
    public static final String SEARCH_AS_BH = "bh";
    public static final String SEARCH_AS_BS = "bs";
    public static final String SEARCH_AS_MC = "mc";
    public static final String SEARCH_AS_PU = "pu";
    public static final String SEARCH_AS_CT = "ct";
    public static final String SEARCH_AS_MN = "mn";
    public static final String SEARCH_AS_KR = "kr";
    public static final String SEARCH_AS_VN = "vn";
    public static final String SEARCH_AS_JP_GO = "jp_go";
    public static final String SEARCH_AS_JP_KAN = "jp_kan";
    public static final String SEARCH_AS_JP_TOU = "jp_tou";
    public static final String SEARCH_AS_JP_KWAN = "jp_kwan";
    public static final String SEARCH_AS_JP_OTHER = "jp_other";
    public static final String SEARCH_AS_JP_ANY = SEARCH_AS_JP_TOU;

    public static int COL_HZ;
    public static int COL_BH;
    public static int COL_BS;
    private static int COL_MC;
    private static int COL_PU;

    private static int COL_KR;
    public static int COL_JP_ANY;
    public static int COL_JP_FIRST;
    public static int COL_FIRST_READING;
    public static int COL_LAST_READING;

    public static int MASK_HZ;
    public static int MASK_JP_ALL;
    public static int MASK_ALL_READINGS;

    private static final String TABLE_NAME = "mcpdict";

    private static String[] COLUMNS;
    private static final String[] JP_COLUMNS = new String[] {SEARCH_AS_JP_GO, SEARCH_AS_JP_KAN, SEARCH_AS_JP_TOU, SEARCH_AS_JP_KWAN, SEARCH_AS_JP_OTHER};
    private static ArrayList<String> SEARCH_AS_NAMES;
    private static ArrayList<String> NAMES;
    private static ArrayList<String> COLORS;
    private static ArrayList<String> DICT_NAMES;
    private static ArrayList<String> DICT_LINKS;
    private static ArrayList<String> INTROS;

    private static Context context;
    private static SQLiteDatabase db = null;

    public static void initialize(Context c) {
        if (db != null) return;
        context = c;
        db = new MCPDatabase(context).getWritableDatabase();
        String userDbPath = UserDatabase.getDatabasePath();
        db.execSQL("ATTACH DATABASE '" + userDbPath + "' AS user");
        getSearchAsColumns();
    }

    public MCPDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
        // Uncomment the following statements to force a database upgrade during development
        // SQLiteDatabase db = getWritableDatabase();
        // db.setVersion(-1);
        // db.close();
        // db = getWritableDatabase();
    }

    public static Cursor search(String input, int mode) {
        // Search for one or more keywords, considering mode and options

        // Get options and settings from SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        boolean kuangxYonhOnly = sp.getBoolean(r.getString(R.string.pref_key_kuangx_yonh_only), false);
        boolean allowVariants = sp.getBoolean(r.getString(R.string.pref_key_allow_variants), true);
        boolean toneInsensitive = sp.getBoolean(r.getString(R.string.pref_key_tone_insensitive), false);
        int cantoneseSystem = Integer.parseInt(Objects.requireNonNull(sp.getString(r.getString(R.string.pref_key_cantonese_romanization), "0")));

        // Split the input string into keywords and canonicalize them
        List<String> keywords = new ArrayList<>();
        List<String> variants = new ArrayList<>();
        if (Orthography.HZ.isBH(input)) mode = COL_BH;
        else if (Orthography.HZ.isBS(input)) {
            mode = COL_BS;
            input = input.replace("-", "f");
        } else if (Orthography.HZ.isHz(input)) mode = COL_HZ;
        else if (Orthography.HZ.isUnicode(input)) {
            input = Orthography.HZ.toHz(input);
            mode = COL_HZ;
        } else if (Orthography.HZ.isPY(input) && mode < COL_FIRST_READING) mode = COL_PU;
        if (isHZ(mode)) {     // Each character is a query
            for (int unicode: input.codePoints().toArray()) {
                if (!Orthography.HZ.isHz(unicode)) continue;
                String hz = Orthography.HZ.toHz(unicode);
                if (keywords.contains(hz)) continue;
                if (!allowVariants) {
                    keywords.add(hz);
                } else {
                    for (int variant : Orthography.HZ.getVariants(unicode)) {
                        String variantHz = Orthography.HZ.toHz(variant);
                        int p = keywords.indexOf(variantHz);
                        if (variant == unicode) {
                            if (p >= 0) {       // The character itself must appear where it is
                                keywords.remove(p);
                                variants.remove(p);
                            }
                            keywords.add(variantHz);
                            variants.add(null); // And no variant information is appended
                        } else {
                            if (p == -1) {      // This variant character may have appeared before
                                keywords.add(variantHz);
                                variants.add(hz);
                            } else {
                                if (variants.get(p) != null) {
                                    variants.set(p, variants.get(p) + hz);
                                }
                            }
                        }
                    }
                }
            }
        }
        else {                          // Each contiguous run of non-separator and non-comma characters is a query
            if (isKR(mode)) { // For Korean, put separators around all hanguls
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (Orthography.Korean.isHangul(c)) {
                        sb.append(" ").append(c).append(" ");
                    }
                    else {
                        sb.append(c);
                    }
                }
                input = sb.toString();
            }
            for (String token : input.split("[\\s,]+")) {
                if (token.equals("")) continue;
                token = token.toLowerCase(Locale.US);
                // Canonicalization
                switch (getColumnName(mode)) {
                    case SEARCH_AS_MC: token = Orthography.MiddleChinese.canonicalize(token); break;
                    case SEARCH_AS_PU: token = Orthography.Mandarin.canonicalize(token); break;
                    case SEARCH_AS_CT: token = Orthography.Cantonese.canonicalize(token, cantoneseSystem); break;
                    case SEARCH_AS_KR: token = Orthography.Korean.canonicalize(token); break;
                    case SEARCH_AS_VN: token = Orthography.Vietnamese.canonicalize(token); break;
                    case SEARCH_AS_JP_GO:
                    case SEARCH_AS_JP_KAN:
                    case SEARCH_AS_JP_ANY:
                        token = Orthography.Japanese.canonicalize(token); break;
                    default:
                        break;
                }
                if (token == null) continue;
                List<String> allTones = null;
                if (toneInsensitive && isToneInsensitive(mode)) {
                    switch (getColumnName(mode)) {
                        case SEARCH_AS_MC: allTones = Orthography.MiddleChinese.getAllTones(token); break;
                        case SEARCH_AS_PU: allTones = Orthography.Mandarin.getAllTones(token); break;
                        case SEARCH_AS_CT: allTones = Orthography.Cantonese.getAllTones(token); break;
                        case SEARCH_AS_VN: allTones = Orthography.Vietnamese.getAllTones(token); break;
                        default:
                            allTones = Orthography.Tone8.getAllTones(token); break;
                    }
                }
                if (allTones != null) {
                    keywords.addAll(allTones);
                }
                else {
                    keywords.add(token);
                }
            }
        }
        if (keywords.isEmpty()) return null;

        // Columns to search
        String[] columns = isAnyJP(mode) ? JP_COLUMNS : new String[] {getColumnName(mode)};

        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("mcpdict");
        List<String> queries = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            String variant = (isHZ(mode) && allowVariants && variants.get(i) != null) ?
                             ("\"" + variants.get(i) + "\"") : "null";
            String[] projection = {"rowid AS _id", i + " AS rank", variant + " AS variants"};
            for (String column : columns) {
                queries.add(qb.buildQuery(projection, column + " MATCH ?", null, null, null, null));
                args.add(keywords.get(i));
            }
        }
        String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, null);

        // Build outer query statement (returning all information about the matching Chinese characters)
        qb.setTables("(" + query + ") AS u, mcpdict AS v LEFT JOIN user.favorite AS w ON v.hz = w.hz");
        qb.setDistinct(true);
        String[] projection = {"v.*", "_id",
                   "v.hz AS hz", "variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "u._id = v.rowid";
        if (kuangxYonhOnly) {
            selection += " AND mc IS NOT NULL";
        }
        query = qb.buildQuery(projection, selection, null, null, "rank", null);

        // Search
        return db.rawQuery(query, args.toArray(new String[0]));
    }

    public static Cursor directSearch(String hz) {
        // Search for a single Chinese character without any conversions
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("mcpdict AS v LEFT JOIN user.favorite AS w ON v.hz = w.hz");
        String[] projection = {"v.*", "v.rowid AS _id",
                   "v.hz AS hz", "NULL AS variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "v.hz = ?";
        String query = qb.buildQuery(projection, selection, null, null, null, null);
        String[] args = {hz};
        return db.rawQuery(query, args);
    }

    private static void getSearchAsColumns() {
        if (COLUMNS != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 1",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        COLUMNS = cursor.getColumnNames();
        int n = cursor.getColumnCount();

        COL_HZ = cursor.getColumnIndex(SEARCH_AS_HZ);
        COL_BH = cursor.getColumnIndex(SEARCH_AS_BH);
        COL_BS = cursor.getColumnIndex(SEARCH_AS_BS);
        COL_MC = cursor.getColumnIndex(SEARCH_AS_MC);
        COL_PU = cursor.getColumnIndex(SEARCH_AS_PU);
        COL_KR = cursor.getColumnIndex(SEARCH_AS_KR);

        COL_FIRST_READING = COL_BS + 1;
        COL_LAST_READING = n - 1;
        COL_JP_FIRST = cursor.getColumnIndex(SEARCH_AS_JP_GO);
        COL_JP_ANY = COL_JP_FIRST + 2;

        MASK_HZ = 1 << COL_HZ;
        MASK_JP_ALL = 0b11111 << COL_JP_FIRST;
        MASK_ALL_READINGS   = (1 << n) - (1 << COL_FIRST_READING);

        SEARCH_AS_NAMES = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            SEARCH_AS_NAMES.add(cursor.getString(i));
        }
        cursor.close();
    }

    public static ArrayList<String> getSearchAsNames() {
        if (SEARCH_AS_NAMES == null) getSearchAsColumns();
        return SEARCH_AS_NAMES;
    }

    public static String getSearchAsName(int index) {
        if (SEARCH_AS_NAMES == null) getSearchAsColumns();
        return SEARCH_AS_NAMES.get(index);
    }

    public static boolean isMC(int mode) {
        return mode == COL_MC;
    }

    public static boolean isHZ(int mode) {
        return mode == COL_HZ;
    }

    private static boolean isKR(int mode) {
        return mode == COL_KR;
    }

    private static boolean isJP(int mode) {
        return mode >= COL_JP_FIRST;
    }

    public static boolean isAnyJP(int mode) {
        return mode == COL_JP_ANY;
    }

    public static boolean isDisplayOnly(int mode) {
        String name = getColumnName(mode);
        return name.contentEquals(SEARCH_AS_VN) || name.contentEquals(SEARCH_AS_PU) || name.contentEquals(SEARCH_AS_MN) || isMC(mode) || isKR(mode) || isJP(mode);
    }

    public static boolean isToneInsensitive(int mode) {
        boolean ret = true;
        if (mode < COL_MC || isKR(mode) || isJP(mode)) ret = false;
        return ret;
    }

    private static void getNames() {
        if (NAMES != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 2", null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        NAMES = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            NAMES.add(cursor.getString(i));
        }
        cursor.close();
    }

    public static String getName(int index) {
        if (NAMES == null) getNames();
        return NAMES.get(index);
    }

    private static void getColors() {
        if (COLORS != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 3",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        COLORS = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String c = cursor.getString(i);
            COLORS.add(c);
        }
        cursor.close();
    }

    public static String getColor(int index) {
        if (COLORS == null) getColors();
        return COLORS.get(index);
    }

    private static void getDictNames() {
        if (DICT_NAMES != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 4",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        DICT_NAMES = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String c = cursor.getString(i);
            DICT_NAMES.add(c);
        }
        cursor.close();
    }

    public static String getDictName(int index) {
        if (DICT_NAMES == null) getDictNames();
        return DICT_NAMES.get(index);
    }

    private static void getDictLinks() {
        if (DICT_LINKS != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 5",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        DICT_LINKS = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String c = cursor.getString(i);
            DICT_LINKS.add(c);
        }
        cursor.close();
    }

    public static String getDictLink(int index) {
        if (DICT_LINKS == null) getDictLinks();
        return DICT_LINKS.get(index);
    }

    private static void getIntros() {
        if (INTROS != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 6",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        INTROS = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String c = cursor.getString(i);
            INTROS.add(c);
        }
        cursor.close();
    }

    public static Spanned getIntro(int index) {
        if (INTROS == null) getIntros();
        String intro = INTROS.get(index);
        if (TextUtils.isEmpty(intro)) intro = INTROS.get(0);
        return HtmlCompat.fromHtml(intro, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    public static String getColumnName(int index) {
        if (COLUMNS == null) getSearchAsColumns();
        return COLUMNS[index];
    }
}
