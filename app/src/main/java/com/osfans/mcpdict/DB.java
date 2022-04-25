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

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class DB extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "mcpdict.db";
    private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;

    // Must be the same order as defined in the string array "search_as"

    public static final String HZ = "漢字";
    public static final String BH = "總筆畫數";
    public static final String BS = "部首餘筆";
    public static final String SW = "說文解字";
    public static final String KX = "康熙字典";
    public static final String HD = "漢語大字典";
    public static final String LF = "兩分";
    public static final String WBH = "五筆畫";
    public static final String VA = "異體字";
    public static final String FL = "分類";

    public static final String MAP = " \uD83C\uDF0F ";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String VARIANTS = "variants";
    public static final String COMMENT = "comment";
    public static final String UNICODE = "unicode";
    public static final String LANG = "語言";

    public static final String SG = "上古擬音（鄭張尚芳）";
    public static final String BA = "上古擬音（白一平沙加爾2015）";
    public static final String GY = "廣韻擬音";
    public static final String CMN = "普通話";
    public static final String HK = "香港粵語標準音";
    public static final String TW = "臺灣閩南語";
    public static final String KOR = "朝鮮語";
    public static final String VI = "越南語";
    public static final String JA_GO = "日語吳音";
    public static final String JA_KAN = "日語漢音";
    public static final String JA_TOU = "日語唐音";
    public static final String JA_KWAN = "日語慣用音";
    public static final String JA_OTHER = "日語其他讀音";
    public static final String JA_ANY = JA_TOU;
    public static final String JA_ = "日語";
    public static final String WB_ = "五筆";

    public static String FQ = null;
    public static String ORDER = null;
    public static String COLOR = null;
    public static final String _FQ = "分區";
    public static final String _COLOR = "顏色";
    public static final String _ORDER = "排序";
    public static final String FIRST_FQ = "地圖集二分區";
    private static String[] FQS = null;
    private static String[] LANGUAGES = null;
    private static String[] SEARCH_COLUMNS = null;

    public static int COL_HZ;
    public static int COL_BH;
    public static int COL_BS;
    public static int COL_SW;
    public static int COL_KX;
    public static int COL_HD;
    public static int COL_LF;
    public static int COL_WBH;
    public static int COL_VA;
    private static int COL_FIRST_LANG;
    public static int COL_LAST_LANG;

    public static int COL_ALL_LANGUAGES = 1000;
    public static final String ALL_LANGUAGES = "*";

    private static final String TABLE_NAME = "mcpdict";
    private static final String TABLE_INFO = "info";

    private static String[] JA_COLUMNS = null;
    private static String[] WB_COLUMNS = null;
    private static String[] COLUMNS;
    private static String[] FQ_COLUMNS;
    private static SQLiteDatabase db = null;

    public static void initialize(Context context) {
        if (db != null) return;
        db = new DB(context).getWritableDatabase();
        String userDbPath = UserDatabase.getDatabasePath();
        db.execSQL("ATTACH DATABASE '" + userDbPath + "' AS user");
        initArrays();
        initFQ(context);
    }

    public DB(Context context) {
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
        String lang = Utils.getLanguage(context);

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
        if (lang.contentEquals(KX) || lang.contentEquals(HD)) {
            if (!TextUtils.isEmpty(input) && !input.startsWith(":") && !input.startsWith("：") && !Orthography.HZ.isPY(input)){
                if (Orthography.HZ.isSingleHZ(input)) lang = HZ;
                else input = ":" + input;
            }
        }
        else if (Orthography.HZ.isBH(input)) lang = BH;
        else if (Orthography.HZ.isBS(input)) {
            lang = BS;
            input = input.replace("-", "f");
        } else if (lang.contentEquals(LF) || lang.contentEquals(WBH)) {
            // not search hz
        } else if (Orthography.HZ.isHz(input)) lang = HZ;
        else if (Orthography.HZ.isUnicode(input)) {
            input = Orthography.HZ.toHz(input);
            lang = HZ;
        } else if (Orthography.HZ.isPY(input) && !isLang(lang)) lang = CMN;
        if (isHzMode(lang)) {     // Each character is a query
            if (input.startsWith(":") || input.startsWith("：")){
                keywords.add("%" + input.substring(1) + "%");
                lang = KX;
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
            if (lang.contentEquals(KOR)) { // For Korean, put separators around all hangul
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
                switch (lang) {
                    case GY: token = Orthography.MiddleChinese.canonicalize(token); break;
                    case CMN: token = Orthography.Mandarin.canonicalize(token); break;
                    case HK: token = Orthography.Cantonese.canonicalize(token, cantoneseSystem); break;
                    case KOR:
                        token = Orthography.Korean.canonicalize(token); break;
                    case VI: token = Orthography.Vietnamese.canonicalize(token); break;
                    case JA_GO:
                    case JA_KAN:
                    case JA_ANY:
                        token = Orthography.Japanese.canonicalize(token); break;
                    default:
                        break;
                }
                if (token == null) continue;
                List<String> allTones = null;
                if (token.endsWith("?") && hasTone(lang)) {
                    token = token.substring(0, token.length()-1);
                    switch (lang) {
                        case GY: allTones = Orthography.MiddleChinese.getAllTones(token); break;
                        case CMN: allTones = Orthography.Mandarin.getAllTones(token); break;
                        case HK: allTones = Orthography.Cantonese.getAllTones(token); break;
                        case VI: allTones = Orthography.Vietnamese.getAllTones(token); break;
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
        String[] columns = lang.contentEquals(JA_ANY) ? JA_COLUMNS : new String[] {lang};
        if (lang.contentEquals(WBH)) columns = WB_COLUMNS;

        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        List<String> queries = new ArrayList<>();
        List<String> args = new ArrayList<>();
        boolean allowVariants = isHzMode(lang) && sp.getBoolean(r.getString(R.string.pref_key_allow_variants), true);
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
                    col = VA;
                    queries.add(qb.buildQuery(projection, col + sel, null, null, null, null));
                    args.add(key);
                }
            }
        }
        String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, null);

        // Build outer query statement (returning all information about the matching Chinese characters)
        qb.setTables("(" + query + ") AS u, mcpdict AS v LEFT JOIN user.favorite AS w ON v.漢字 = w.hz");
        qb.setDistinct(true);
        String[] projection = {"v.*", "_id",
                   "v.漢字 AS `漢字`", "variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "u._id = v.rowid";
        if (mcOnly) {
            selection += String.format(" AND `%s` IS NOT NULL", GY);
        } else if (swOnly) {
            selection += String.format(" AND `%s` IS NOT NULL", SW);
        } else if (kxOnly) {
            selection += String.format(" AND `%s` IS NOT NULL", KX);
        } else if (hdOnly) {
            selection += String.format(" AND `%s` IS NOT NULL", HD);
        } else if (charset > 0) {
            selection += String.format(" AND `%s` MATCH '%s'", FL, r.getStringArray(R.array.pref_values_charset)[charset]);
        }
        query = qb.buildQuery(projection, selection, null, null, "rank,vaIndex", "0,1000");

        // Search
        return db.rawQuery(query, args.toArray(new String[0]));
    }

    public static Cursor directSearch(String hz) {
        // Search for a single Chinese character without any conversions
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("mcpdict AS v LEFT JOIN user.favorite AS w ON v.漢字 = w.hz");
        String[] projection = {"v.*", "v.rowid AS _id",
                   "v.漢字 AS 漢字", "NULL AS variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "v.漢字 = ?";
        String query = qb.buildQuery(projection, selection, null, null, null, "0,1000");
        String[] args = {hz};
        return db.rawQuery(query, args);
    }

    public static void initFQ(Context context) {
        FQ = Utils.getString(context, R.string.pref_key_fq, context.getString(R.string.default_fq));
        ORDER = FQ.replace(_FQ, _ORDER);
        COLOR = FQ.replace(_FQ, _COLOR);
        FQS = getFieldString(HZ, FQ).split(",");
        SEARCH_COLUMNS = queryInfo(FIRST_FQ.replace(_FQ, _COLOR) + " is not null");
        LANGUAGES = queryInfo(FQ + " is not null and rowid > 1");
    }

    private static void initArrays() {
        if (COLUMNS != null || db == null) return;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String selection = "rowid = 1";
        String query = qb.buildQuery(projection, selection,  null, null, null, null);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        COLUMNS = cursor.getColumnNames();
        ArrayList<String> arrayList = new ArrayList<>();
        for(String s: COLUMNS) {
            if (s.startsWith(JA_)) arrayList.add(s);
        }
        JA_COLUMNS = arrayList.toArray(new String[0]);
        arrayList.clear();
        for(String s: COLUMNS) {
            if (s.startsWith(WB_)) arrayList.add(s);
        }
        WB_COLUMNS = arrayList.toArray(new String[0]);
        COL_HZ = getColumnIndex(HZ);
        COL_BH = getColumnIndex(BH);
        COL_BS = getColumnIndex(BS);
        COL_SW = getColumnIndex(SW);
        COL_LF = getColumnIndex(LF);
        COL_VA = getColumnIndex(VA);
        COL_HD = getColumnIndex(HD);
        COL_KX = getColumnIndex(KX);
        COL_WBH = getColumnIndex(WBH);
        COL_FIRST_LANG = getColumnIndex(SG);
        COL_LAST_LANG = getColumnIndex(BH) - 1;
        cursor.close();

        qb.setTables(TABLE_INFO);
        query = qb.buildQuery(projection, selection,  null, null, null, null);
        cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        cursor.getColumnNames();
        arrayList.clear();
        for(String s: cursor.getColumnNames()) {
            if (s.endsWith(_FQ)) arrayList.add(s);
        }
        FQ_COLUMNS = new String[arrayList.size()];
        arrayList.toArray(FQ_COLUMNS);
        cursor.close();
    }

    private static String[] queryInfo(String selection) {
        return queryInfo(selection, null);
    }

    private static String[] queryInfo(String selection, String args) {
        if (db == null) return null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {"*"};
        String query = qb.buildQuery(projection, selection,  null, null, ORDER, null);
        Cursor cursor = db.rawQuery(query, TextUtils.isEmpty(args) ? null : new String[]{String.format("\"%s\"", args)});
        cursor.moveToFirst();
        int n = cursor.getCount();
        String[] a = new String[n];
        for (int i = 0; i < n; i++) {
            String s = cursor.getString(0);
            a[i] = s;
            cursor.moveToNext();
        }
        cursor.close();
        return a;
    }

    public static String[] getLanguages() {
        initArrays();
        if (LANGUAGES == null) {
            LANGUAGES = queryInfo(FQ + " is not null and rowid > 1");
        }
        return LANGUAGES;
    }

    public static String[] getLanguages(String type) {
        if (type.contentEquals("*")) return getLanguages();
        if (TextUtils.isEmpty(type)) return null;
        return queryInfo(String.format("%s MATCH ? and rowid > 1", FQ), type);
    }

    public static String[] getSearchColumns() {
        initArrays();
        if (SEARCH_COLUMNS == null) {
            SEARCH_COLUMNS = queryInfo(COLOR + " is not null");
        }
        return SEARCH_COLUMNS;
    }

    public static int getColumnIndex(String lang) {
        for (int i = 0; i < COLUMNS.length; i++) {
            if (COLUMNS[i].contentEquals(lang)) return i;
        }
        return -1;
    }

    public static String getColumn(int i) {
        if (COLUMNS == null) initArrays();
        return i < 0 ? "" : COLUMNS[i];
    }

    public static String[] getVisibleColumns(Context context) {
        String languages = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_show_language_names), "");
        Set<String> customs = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(context.getString(R.string.pref_key_custom_languages), null);

        if (languages.contentEquals("*")) return LANGUAGES;
        if (languages.contentEquals("3") || languages.contentEquals("5")) {
            return queryInfo(String.format("級別  >= \"%s\"", languages));
        }
        ArrayList<String> array = new ArrayList<>();
        if (TextUtils.isEmpty(languages)) {
            if (customs == null || customs.size() == 0) return LANGUAGES;
            for (String lang: getLanguages()) {
                if (!array.contains(lang) && customs.contains(lang)) {
                    array.add(lang);
                }
            }
            return array.toArray(new String[0]);
        }
        if (languages.contains(",")) {
            for (String lang: languages.split(",")) {
                if (getColumnIndex(lang) >= 1 && !array.contains(lang)) {
                    array.add(lang);
                }
            }
            return array.toArray(new String[0]);
        }
        String[] a = DB.getLanguages(languages);
        if (a != null && a.length > 0) {
            return a;
        }
        if (getColumnIndex(languages) >= 1) return new String[]{languages};
        return new String[0];
    }

    public static boolean isHzMode(String lang) {
        return lang.contentEquals(HZ);
    }

    public static boolean hasTone(String lang) {
        return !TextUtils.isEmpty(getToneName(lang));
    }

    public static String getFieldString(String lang, String field) {
        if (db == null) return "";
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {String.format("\"%s\"", field)};
        String query = qb.buildQuery(projection, LANG + " match ?",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, new String[]{String.format("\"%s\"", lang)});
        String s = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            s = cursor.getString(0);
        }
        cursor.close();
        if (TextUtils.isEmpty(s)) s = "";
        return s;
    }

    public static String getLabel(String lang) {
        String s = getFieldString(lang, "簡稱");
        //if (s.length() == 2) s = String.format(" %s ", s);
        return s;
    }

    public static String getLabel(int i) {
        String lang = getColumn(i);
        return getLabel(lang);
    }

    public static int getColor(String lang, int i) {
        if (COLUMNS == null) initArrays();
        String c = getFieldString(lang, COLOR);
        if (TextUtils.isEmpty(c)) return Color.BLACK;
        if (c.contains(",")) c = c.split(",")[i];
        return Color.parseColor(c);
    }

    public static int getColor(String lang) {
        return getColor(lang, 0);
    }

    public static int getSubColor(String lang) {
        return getColor(lang, 1);
    }

    public static String getHexColor(String lang) {
        return String.format("#%06X", getColor(lang) & 0xFFFFFF);
    }

    public static String getHexSubColor(String lang) {
        return String.format("#%06X", getSubColor(lang) & 0xFFFFFF);
    }

    public static String getDictName(String lang) {
        return getFieldString(lang, "網站");
    }

    public static String getDictLink(String lang) {
        return getFieldString(lang, "網址");
    }

    private static String _getIntro(Context context, String lang) {
        String intro = TextUtils.isEmpty(lang) ? "" : getFieldString(lang, "說明");
        if (lang.contentEquals(HZ)) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", context.getString(R.string.version), BuildConfig.VERSION_NAME, intro);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.getDefault(), "%s%s<br>", context.getString(R.string.name), lang));
            ArrayList<String> fields = new ArrayList<>(Arrays.asList("錄入人","參考資料","文件名","版本","字數","音節數","不帶調音節數",""));
            fields.addAll(Arrays.asList(FQ_COLUMNS));
            fields.add("");
            for (String field: fields) {
                if (TextUtils.isEmpty(field)) sb.append("<br>");
                String value = getFieldString(lang, field);
                if (!TextUtils.isEmpty(value)) {
                    sb.append(String.format(Locale.getDefault(), "%s：%s<br>", field, value));
                }
            }
            sb.append(intro);
            intro = sb.toString();
        }
        return intro;
    }

    public static String getIntroText(Context context, String lang) {
        initArrays();
        if (TextUtils.isEmpty(lang)) lang = Utils.getLanguage(context);
        String intro = _getIntro(context, lang);
        if (lang.contentEquals(HZ)) {
            StringBuilder sb = new StringBuilder();
            sb.append(intro);
            sb.append("<br><h2>已收錄語言</h2><table border=1 cellspacing=0>");
            sb.append("<tr>");
            String[] fields = new String[]{LANG, "字數", "音節數", "不帶調音節數"};
            for (String field: fields) {
                sb.append(String.format("<th>%s</th>", field));
            }
            sb.append("</tr>");
            for (String l : LANGUAGES) {
                sb.append("<tr>");
                for (String field: fields) {
                    sb.append(String.format("<td>%s</td>", getFieldString(l, field)));
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
            intro = sb.toString();
        } else {
            intro = String.format(Locale.getDefault(), "<h1>%s</h1>%s<h2>音系說明</h2><h2>同音字表</h2>", lang, intro);
        }
        return intro;
    }

    public static String getIntro(Context context) {
        initArrays();
        String lang = Utils.getLanguage(context);
        return _getIntro(context, lang);
    }

    public static String getToneName(String lang) {
        return getFieldString(lang, "聲調");
    }

    public static Double getLocation(String lang, int pos) {
        String location = getFieldString(lang, "坐標");
        if (TextUtils.isEmpty(location)) return null;
        return Double.parseDouble(location.split(",")[pos]);
    }

    private static Double getLat(String lang) {
        return getLocation(lang, 0);
    }

    private static Double getLong(String lang) {
        return getLocation(lang, 1);
    }

    public static GeoPoint getPoint(String lang) {
        if (getLat(lang) == null) return null;
        return new GeoPoint(getLat(lang), getLong(lang));
    }

    public static int getSize(String lang) {
        String s = getFieldString(lang, "級別");
        if (TextUtils.isEmpty(s)) return 0;
        return Integer.parseInt(s);
    }

    private static String getLangType(String lang) {
        return getFieldString(lang, FIRST_FQ);
    }

    public static boolean isLang(String lang) {
        return !TextUtils.isEmpty(getLangType(lang)) && !lang.contentEquals(HZ);
    }

    public static String[] getFqColumns() {
        initArrays();
        return FQ_COLUMNS;
    }

    public static String[] getFqs() {
        initArrays();
        if (FQS == null) FQS = getFieldString(HZ, FQ).split(",");
        return FQS;
    }

    public static String getFq(String lang) {
        initArrays();
        String s = getFieldString(lang, FQ);
        if (TextUtils.isEmpty(s)) return "";
        return s.split("[,-]")[0];
    }

    public static String getUnicode(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        String hz = cursor.getString(COL_HZ);
        sb.append(String.format("<div id=%s%s class=block>", hz, UNICODE));
        String s = Orthography.HZ.toUnicode(hz);
        sb.append(String.format("<div class=place>統一碼</div><div class=ipa>%s %s</div><br>", s, Orthography.HZ.getUnicodeExt(hz)));
        for (int i = DB.COL_LF; i < DB.COL_VA; i++) {
            if (i == COL_SW) i = COL_BH;
            String lang = getColumn(i);
            s = cursor.getString(i);
            if (i == COL_BS) s = s.replace("f", "-");
            if (TextUtils.isEmpty(s)) continue;
            sb.append(String.format("<div class=place>%s</div><div class=ipa>%s</div><br>", lang, s));
        }
        sb.append("</div>");
        return sb.toString();
    }
}
