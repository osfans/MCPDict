package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
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
    private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;

    // Must be the same order as defined in the string array "search_as"

    public static final String SEARCH_AS_HZ = "hz";
    public static final String SEARCH_AS_BH = "bh";
    public static final String SEARCH_AS_BS = "bs";
    public static final String SEARCH_AS_SW = "sw";
    public static final String SEARCH_AS_KX = "kx";
    public static final String SEARCH_AS_HD = "hd";
    public static final String SEARCH_AS_LF = "lf";
    public static final String SEARCH_AS_WBH = "wbh";
    public static final String SEARCH_AS_VA = "va";

    public static final String SEARCH_AS_SG = "och_sg";
    public static final String SEARCH_AS_BA = "och_ba";
    public static final String SEARCH_AS_MC = "ltc_mc";
    public static final String SEARCH_AS_CMN = "cmn_";
    public static final String SEARCH_AS_GZ = "yue_hk";
    public static final String SEARCH_AS_NAN = "nan_zq_tw";
    public static final String SEARCH_AS_KOR = "ko_kor";
    public static final String SEARCH_AS_VI = "vi_";
    public static final String SEARCH_AS_JA_GO = "ja_go";
    public static final String SEARCH_AS_JA_KAN = "ja_kan";
    public static final String SEARCH_AS_JA_TOU = "ja_tou";
    public static final String SEARCH_AS_JA_KWAN = "ja_kwan";
    public static final String SEARCH_AS_JA_OTHER = "ja_other";
    public static final String SEARCH_AS_JA_ANY = SEARCH_AS_JA_TOU;

    public static int COL_HZ;
    public static int COL_BH;
    public static int COL_BS;
    public static int COL_SW;
    public static int COL_KX;
    public static int COL_HD;
    public static int COL_LF;
    public static int COL_WBH;
    public static int COL_VA;

    public static int COL_SG;
    public static int COL_MC;
    public static int COL_CMN;
    public static int COL_GZ;
    public static int COL_NAN;
    public static int COL_VI;

    public static int COL_JA_ANY;
    public static int COL_JA_FIRST;
    public static int COL_FIRST_READING;
    public static int COL_LAST_READING;

    public static int COL_ALL_READINGS = 1000;

    private static final String TABLE_NAME = "mcpdict";

    private static String[] ALL_COLUMNS;
    private static final String[] JA_COLUMNS = new String[] {SEARCH_AS_JA_GO, SEARCH_AS_JA_KAN, SEARCH_AS_JA_TOU, SEARCH_AS_JA_KWAN, SEARCH_AS_JA_OTHER};
    private static final String[] WB_COLUMNS = new String[] {"wbh", "wb86", "wb98", "wb06"};
    private static ArrayList<String> LANGUAGES, ALL_LANGUAGES;
    private static ArrayList<String> COLUMNS;
    private static ArrayList<String> LABELS;
    private static ArrayList<String> COLORS;
    private static ArrayList<String> DICT_NAMES;
    private static ArrayList<String> DICT_LINKS;
    private static ArrayList<String> INTROS;
    private static ArrayList<String> TONE_NAMES;

    private static SQLiteDatabase db = null;

    public static void initialize(Context context) {
        if (db != null) return;
        db = new MCPDatabase(context).getWritableDatabase();
        String userDbPath = UserDatabase.getDatabasePath();
        db.execSQL("ATTACH DATABASE '" + userDbPath + "' AS user");
        initArrays();
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

    public static Cursor search(Context context) {
        // Search for one or more keywords, considering mode and options
        String input = Utils.getInput(context);
        int mode = getColumnIndex(context);

        // Get options and settings from SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        int charset = sp.getInt(r.getString(R.string.pref_key_charset), 0);
        boolean mcOnly = charset == 1;
        boolean kxOnly = charset == 3;
        boolean hdOnly = charset == 4;
        boolean swOnly = charset == 2;
        int cantoneseSystem = Integer.parseInt(Objects.requireNonNull(sp.getString(r.getString(R.string.pref_key_cantonese_romanization), "0")));

        // Split the input string into keywords and canonicalize them
        List<String> keywords = new ArrayList<>();
        if (mode == COL_KX || mode == COL_HD) {
            if (!TextUtils.isEmpty(input) && !input.startsWith(":") && !input.startsWith("：") && !Orthography.HZ.isPY(input)){
                if (Orthography.HZ.isSingleHZ(input)) mode = COL_HZ;
                else input = ":" + input;
            }
        }
        else if (Orthography.HZ.isBH(input)) mode = COL_BH;
        else if (Orthography.HZ.isBS(input)) {
            mode = COL_BS;
            input = input.replace("-", "f");
        } else if (mode == COL_LF || mode == COL_WBH) {
            // not search hz
        } else if (Orthography.HZ.isHz(input)) mode = COL_HZ;
        else if (Orthography.HZ.isUnicode(input)) {
            input = Orthography.HZ.toHz(input);
            mode = COL_HZ;
        } else if (Orthography.HZ.isPY(input) && mode < COL_FIRST_READING) mode = COL_CMN;
        if (isHzMode(mode)) {     // Each character is a query
            if (input.startsWith(":") || input.startsWith("：")){
                keywords.add("%" + input.substring(1) + "%");
                mode = COL_KX;
            } else {
                for (int unicode : input.codePoints().toArray()) {
                    if (!Orthography.HZ.isHz(unicode)) continue;
                    String hz = Orthography.HZ.toHz(unicode);
                    if (keywords.contains(hz)) continue;
                    keywords.add(hz);
                }
            }
        } else if (input.startsWith(":") || input.startsWith("：")){
            keywords.add("%" + input.substring(1) + "%");
        } else {                          // Each contiguous run of non-separator and non-comma characters is a query
            if (isKO(mode)) { // For Korean, put separators around all hangul
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
                    case SEARCH_AS_CMN: token = Orthography.Mandarin.canonicalize(token); break;
                    case SEARCH_AS_GZ: token = Orthography.Cantonese.canonicalize(token, cantoneseSystem); break;
                    case SEARCH_AS_KOR:
                        token = Orthography.Korean.canonicalize(token); break;
                    case SEARCH_AS_VI: token = Orthography.Vietnamese.canonicalize(token); break;
                    case SEARCH_AS_JA_GO:
                    case SEARCH_AS_JA_KAN:
                    case SEARCH_AS_JA_ANY:
                        token = Orthography.Japanese.canonicalize(token); break;
                    default:
                        break;
                }
                if (token == null) continue;
                List<String> allTones = null;
                if (token.endsWith("?") && isToneInsensitive(mode)) {
                    token = token.substring(0, token.length()-1);
                    switch (getColumnName(mode)) {
                        case SEARCH_AS_MC: allTones = Orthography.MiddleChinese.getAllTones(token); break;
                        case SEARCH_AS_CMN: allTones = Orthography.Mandarin.getAllTones(token); break;
                        case SEARCH_AS_GZ: allTones = Orthography.Cantonese.getAllTones(token); break;
                        case SEARCH_AS_VI: allTones = Orthography.Vietnamese.getAllTones(token); break;
                        default:
                            allTones = Orthography.Tones.getAllTones(token); break;
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
        String[] columns = isAnyJA(mode) ? JA_COLUMNS : new String[] {getColumnName(mode)};
        if (mode == COL_WBH) columns = WB_COLUMNS;

        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("mcpdict");
        List<String> queries = new ArrayList<>();
        List<String> args = new ArrayList<>();
        boolean allowVariants = isHzMode(mode) && sp.getBoolean(r.getString(R.string.pref_key_allow_variants), true);
        for (int i = 0; i < keywords.size(); i++) {
            String variant = allowVariants ? ("\"" + keywords.get(i) + "\"") : "null";
            String[] projection = {"rowid AS _id", i + " AS rank", "offsets(mcpdict) AS vaIndex", variant + " AS variants"};
            String key = keywords.get(i);
            String sel = " MATCH ?";
            if (key.startsWith("%") && key.endsWith("%")) {
                sel = " LIKE ?";
            }
            for (String column : columns) {
                String col = column;
                queries.add(qb.buildQuery(projection, col + sel, null, null, null, null));
                args.add(key);
                if (allowVariants) {
                    col = "va";
                    queries.add(qb.buildQuery(projection, col + sel, null, null, null, null));
                    args.add(key);
                }
            }
        }
        String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, null);

        // Build outer query statement (returning all information about the matching Chinese characters)
        qb.setTables("(" + query + ") AS u, mcpdict AS v LEFT JOIN user.favorite AS w ON v.hz = w.hz");
        qb.setDistinct(true);
        String[] projection = {"v.*", "_id",
                   "v.hz AS hz", "variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "u._id = v.rowid AND v.rowid > 7";
        if (mcOnly) {
            selection += " AND ltc_mc IS NOT NULL";
        } else if (swOnly) {
            selection += " AND sw IS NOT NULL";
        } else if (kxOnly) {
            selection += " AND kx IS NOT NULL";
        } else if (hdOnly) {
            selection += " AND hd IS NOT NULL";
        } else if (charset > 0) {
            selection += String.format(" AND fl MATCH '%s'", r.getStringArray(R.array.pref_values_charset)[charset]);
        }
        query = qb.buildQuery(projection, selection, null, null, "rank,vaIndex", "0,1000");

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
        String query = qb.buildQuery(projection, selection, null, null, null, "0,1000");
        String[] args = {hz};
        return db.rawQuery(query, args);
    }

    private static void initArrays() {
        if (ALL_COLUMNS != null || db == null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 1",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        ALL_COLUMNS = cursor.getColumnNames();
        int n = cursor.getColumnCount();

        COL_HZ = cursor.getColumnIndex(SEARCH_AS_HZ);
        COL_BH = cursor.getColumnIndex(SEARCH_AS_BH);
        COL_BS = cursor.getColumnIndex(SEARCH_AS_BS);
        COL_SW = cursor.getColumnIndex(SEARCH_AS_SW);
        COL_KX = cursor.getColumnIndex(SEARCH_AS_KX);
        COL_HD = cursor.getColumnIndex(SEARCH_AS_HD);
        COL_LF = cursor.getColumnIndex(SEARCH_AS_LF);
        COL_WBH = cursor.getColumnIndex(SEARCH_AS_WBH);
        COL_VA = cursor.getColumnIndex(SEARCH_AS_VA);

        COL_SG = cursor.getColumnIndex(SEARCH_AS_SG);
        COL_MC = cursor.getColumnIndex(SEARCH_AS_MC);
        COL_CMN = cursor.getColumnIndex(SEARCH_AS_CMN);
        COL_GZ = cursor.getColumnIndex(SEARCH_AS_GZ);
        COL_NAN = cursor.getColumnIndex(SEARCH_AS_NAN);
        COL_VI = cursor.getColumnIndex(SEARCH_AS_VI);

        COL_JA_FIRST = cursor.getColumnIndex(SEARCH_AS_JA_GO);
        COL_JA_ANY = COL_JA_FIRST + 2;
        COL_FIRST_READING = COL_SG;
        COL_LAST_READING = COL_JA_FIRST + 4;

        LANGUAGES = new ArrayList<>();
        COLUMNS = new ArrayList<>();
        ALL_LANGUAGES = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i < COL_JA_ANY) {
                LANGUAGES.add(cursor.getString(i));
                COLUMNS.add(cursor.getColumnName(i));
            }
            ALL_LANGUAGES.add(cursor.getString(i));
        }
        cursor.close();
    }

    public static ArrayList<String> getLanguages() {
        if (LANGUAGES == null) initArrays();
        return LANGUAGES;
    }

    public static ArrayList<String> getFields() {
        if (COLUMNS == null) initArrays();
        return COLUMNS;
    }

    public static String getFullName(int index) {
        if (ALL_LANGUAGES == null) initArrays();
        return ALL_LANGUAGES.get(index);
    }

    public static boolean isHzMode(int mode) {
        return mode == COL_HZ;
    }

    private static boolean isKO(int mode) {
        return getColumnName(mode).contentEquals(SEARCH_AS_KOR);
    }

    private static boolean isJA(int mode) {
        return mode >= COL_JA_FIRST;
    }

    public static boolean isAnyJA(int mode) {
        return mode == COL_JA_ANY;
    }

    public static boolean isToneInsensitive(int mode) {
        return !isKO(mode) && !isJA(mode);
    }

    private static void getLabels() {
        if (LABELS != null || db == null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 2", null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        LABELS = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LABELS.add(cursor.getString(i));
        }
        cursor.close();
    }

    public static String getLabel(int index) {
        if (LABELS == null) getLabels();
        return LABELS.get(index);
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
            COLORS.add(cursor.getString(i));
        }
        cursor.close();
    }

    public static int getColor(int index, int i) {
        if (COLORS == null) getColors();
        String c = COLORS.get(index);
        if (c.contains(",")) c = c.split(",")[i];
        return Color.parseColor(c);
    }

    public static int getColor(int index) {
        return getColor(index, 0);
    }

    public static int getSubColor(int index) {
        return getColor(index, 1);
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

    public static String getIntroText(Context context) {
        if (INTROS == null) getIntros();
        int index = getColumnIndex(context);
        String intro = index < 0 ? "" : INTROS.get(index);
        if (TextUtils.isEmpty(intro)) intro = INTROS.get(0);
        if (index == 0) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", context.getString(R.string.version), BuildConfig.VERSION_NAME, intro);
        } else if (index > 0) {
            intro = String.format(Locale.getDefault(), "<h1>%s</h1>%s<h2>音系說明</h2><h2>同音字表</h2>",getFullName(index), intro);
        }
        return intro;
    }

    public static Spanned getIntro(Context context) {
        if (INTROS == null) getIntros();
        int index = getColumnIndex(context);
        String intro = index < 0 ? "" : INTROS.get(index);
        if (TextUtils.isEmpty(intro)) intro = INTROS.get(0);
        if (index == 0) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", context.getString(R.string.version), BuildConfig.VERSION_NAME, intro);
        } else if (index > 0) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", context.getString(R.string.name), getFullName(index), intro);
        }
        return HtmlCompat.fromHtml(intro, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private static void getToneNames() {
        if (TONE_NAMES != null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, "rowid = 7",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int n = cursor.getColumnCount();
        TONE_NAMES = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String c = cursor.getString(i);
            TONE_NAMES.add(c);
        }
        cursor.close();
    }

    public static String getToneName(int lang) {
        if (TONE_NAMES == null) getToneNames();
        return TONE_NAMES.get(lang);
    }

    public static String getColumnName(int index) {
        if (ALL_COLUMNS == null) initArrays();
        return index < 0 ? "" : ALL_COLUMNS[index];
    }

    public static boolean isReading(int index) {
        return index >= COL_FIRST_READING && index <= COL_LAST_READING;
    }

    public static boolean isDialect(int index) {
        return index >= COL_CMN && index < COL_VI;
    }

    public static void putColumnIndex(Context context, int mode) {
        String value = getFullName(mode);
        Utils.putLanguage(context, value);
    }

    public static int getColumnIndex(Context context) {
        String value = Utils.getLanguage(context);
        return ALL_LANGUAGES.indexOf(value);
    }
}
