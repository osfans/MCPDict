package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.osfans.mcpdict.Orth.*;
import com.osfans.mcpdict.Util.UserDB;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DB extends SQLiteAssetHelper {

    private static final String DB_NAME = "mcpdict.db";

    // Must be the same order as defined in the string array "search_as"

    public static final String HZ = "漢字";
    private static final String BS = "部首餘筆";
    public static final String SW = "說文";
    private static final String GYHZ = "匯纂";
    private static final String KX = "康熙";
    private static final String HD = "漢大";
    private static final String ZX = "字形描述";
    private static final String LF = "兩分";
    private static final String BJJS = "部件檢索";
    private static final String WB_ = "五筆";
    private static final String CJ_ = "倉頡";
    private static final String SR = "山人碼LTS";
    private static final String WBH = "五筆畫";
    private static final String VA = "異體字";
    private static final String FL = "分類";

    public static final String MAP = " 🌏 ";
    public static final String VARIANTS = "variants";
    public static final String INDEX = "索引";
    public static final String LANGUAGE = "語言";
    public static final String LABEL = "簡稱";
    public static final String SYLLABLES = "音節數";

    public static final String SG = "鄭張上古";
    public static final String BA = "白-沙上古";
    public static final String GY = "廣韻";
    public static final String ZT = "中唐";
    public static final String ZYYY = "中原音韻";
    public static final String DGY = "東干甘肅話";
    public static final String CMN = "普通話";
    public static final String CMN_TW = "國語";
    public static final String HK = "香港";
    public static final String TW = "臺灣";
    public static final String KOR = "朝鮮";
    public static final String VI = "越南";
    public static final String JA_GO = "日語吳音";
    public static final String JA_KAN = "日語漢音";
    public static final String JA_OTHER = "日語其他";

    public static String FQ = null;
    public static String ORDER = null;
    public static String COLOR = null;
    public static final String _FQ = "分區";
    public static final String _COLOR = "顏色";
    public static final String _ORDER = "排序";
    public static final String FIRST_FQ = "地圖集二分區";
    public static final String PROVINCE = "省";
    public static final String RECOMMEND = "推薦人";
    public static final String EDITOR = "維護人";
    private static String[] DIVISIONS = null;
    private static String[] LABELS = null;
    private static String[] LANGUAGES = null;
    private static String[] SEARCH_COLUMNS = null;

    public static int COL_HZ = 0, COL_IPA = 1, COL_ZS = 2, COL_LANG = 3;
    public static int COL_SW, COL_KX, COL_GYHZ, COL_HD;
    public static int COL_ZX, COL_BJJS;
    public static int COL_VA;
    public static int COL_FIRST_DICT, COL_LAST_DICT;
    public static int COL_FIRST_INFO, COL_LAST_INFO;
    public static int COL_FIRST_SHAPE, COL_LAST_SHAPE;

    public enum SEARCH {
        HZ, YIN, COMMENT, DICT,
    }

    public enum FILTER {
        ALL, ISLAND, HZ, CURRENT, RECOMMEND, CUSTOM, DIVISION, AREA, EDITOR
    }

    public static int COL_ALL_LANGUAGES = 10000;
    public static final String ALL_LANGUAGES = "*";

    private static final String TABLE_NAME = "mcpdict";
    private static final String TABLE_LANG = "langs";
    private static final String TABLE_INFO = "info";

    private final static String[] JA_COLUMNS = new String[] {JA_KAN, JA_GO, JA_OTHER};
    private static String[] COLUMNS;
    private static String[] FQ_COLUMNS;
    private static String[] DICTIONARY_COLUMNS;
    private static String[] SHAPE_COLUMNS;
    private static final String[] EDITOR_COLUMNS = new String[]{
            "作者", "錄入人", "維護人"
    };
    private static SQLiteDatabase db = null;

    public static void initialize(Context context) {
        if (db != null) return;
        db = new DB(context).getReadableDatabase();
        String userDbPath = UserDB.getDatabasePath();
        db.execSQL("ATTACH DATABASE '" + userDbPath + "' AS user");
        initArrays();
        initFQ();
    }

    public DB(Context context) {
        super(context, DB_NAME, null, BuildConfig.DB_VER);
        setForcedUpgrade();
    }

    private static String getCharsetSelect(int matchClause) {
        // Get options and settings
        int charset = Pref.getInt(R.string.pref_key_charset);
        if (charset == 0) return "";
        String value = Pref.getStringArray(R.array.pref_values_charset)[charset];
        String selection;
        if (matchClause == 1) {
            selection = String.format(" AND `%s` MATCH '%s'", FL, value);
        } else if (matchClause == 2) {
            selection = String.format(" %s:%s", FL, value);
        } else {
            selection = String.format(" AND `%s` LIKE '%%%s%%'", FL, value);
        }
        return selection;
    }

    public static boolean inCharset(String hz) {
        if (db == null) return false;
        if (HanZi.isUnknown(hz)) return true;
        String charset = getCharsetSelect(2);
        if (TextUtils.isEmpty(charset)) return true;
        Cursor cursor = db.rawQuery(String.format("select * from mcpdict where mcpdict MATCH '漢字:%s %s'", hz, charset), null);
        int count = cursor.getCount();
        cursor.close();
        return count == 1;
    }

    private static boolean isMatchBegins(String lang) {
        return lang.startsWith(CJ_) || (lang.startsWith(WB_) && !lang.contentEquals(WBH)) || lang.contentEquals(SR);
    }

    public static boolean isHzParts(String lang) {
        return lang.contentEquals(BJJS) || lang.contentEquals(ZX);
    }

    private static List<String> normInput(String lang, String input) {
        List<String> keywords = new ArrayList<>();
        if (lang.contentEquals(BS)) input = input.replace("-", "f");
        else if (isHzParts(lang)) {
            keywords.add(Orthography.normParts(input));
            return keywords;
        }
        else if (isMatchBegins(lang)) input += "*";
        else if (lang.contentEquals(GY) && HanZi.isHz(input)) {
            input += "*";
        } else if (lang.contentEquals(KOR)) { // For Korean, put separators around all hangul
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (Korean.isHangul(c)) {
                    sb.append(" ").append(c).append(" ");
                }
                else {
                    sb.append(c);
                }
            }
            input = sb.toString();
        }
        if (HanZi.isHz(input)) {
            keywords.add(input);
            return keywords;
        }
        int cantoneseSystem = Pref.getStrAsInt(R.string.pref_key_cantonese_romanization, 0);
        for (String token : input.split("[\\s,]+")) {
            if (TextUtils.isEmpty(token)) continue;
            // Canonicalization
            switch (lang) {
                case CMN:
                case CMN_TW:
                    token = Mandarin.canonicalize(token); break;
                case HK: token = Cantonese.canonicalize(token, cantoneseSystem); break;
                case KOR:
                    token = Korean.canonicalize(token); break;
                case VI: token = Vietnamese.canonicalize(token); break;
                case JA_KAN:
                case JA_GO:
                case JA_OTHER:
                    token = Japanese.canonicalize(token); break;
                default:
                    token = token.toLowerCase(Locale.US);
                    break;
            }
            if (token == null) continue;
            List<String> allTones = null;
            if ((token.endsWith("?") || !Tones.hasTone(token)) && hasTone(lang)) {
                if (token.endsWith("?")) token = token.substring(0, token.length()-1);
                allTones = switch (lang) {
                    case GY -> MiddleChinese.getAllTones(token);
                    case CMN, CMN_TW -> Mandarin.getAllTones(token);
                    case HK -> Cantonese.getAllTones(token);
                    case VI -> Vietnamese.getAllTones(token);
                    default -> Tones.getAllTones(token, lang);
                };
            }
            if (allTones != null) {
                keywords.addAll(allTones);
            }
            else {
                keywords.add(token);
            }
        }
        return keywords;
    }

    public static Cursor search() {
        // Search for one or more keywords, considering mode and options
        String input = Pref.getInput();
        if (input.startsWith("-")) input = input.substring(1); //may crash sqlite
        input = input.strip();

        String label = Pref.getLabel();
        String lang = label;
        SEARCH searchType = SEARCH.values()[Pref.getInt(R.string.pref_key_type)];

        // Split the input string into keywords and canonicalize them
        List<String> keywords = new ArrayList<>();
        if (searchType == SEARCH.COMMENT || searchType == SEARCH.DICT) {
            if (HanZi.isHz(input)) {
                String[] hzs = Orthography.normWords(input);
                if (hzs.length > 0) keywords = Arrays.asList(hzs);
            }
        } else if (HanZi.isHz(input)) {
            if (!lang.contentEquals(GY) || searchType != SEARCH.YIN) lang = HZ;
        } else if (HanZi.isUnicode(input)) {
            input = HanZi.toHz(input);
            lang = HZ;
        } else if (HanZi.isPY(input) && !isLang(lang)) lang = CMN;
        if (isLanguageHZ(lang) && searchType == SEARCH.YIN) searchType = SEARCH.HZ;
        if (isLanguageHZ(lang) && searchType == SEARCH.HZ) {     // Each character is a query
            for (int unicode : input.codePoints().toArray()) {
                if (!HanZi.isHz(unicode)) continue;
                String hz = HanZi.toHz(unicode);
                if (keywords.contains(hz)) continue;
                keywords.add(hz);
            }
        } else if (searchType == SEARCH.HZ || searchType == SEARCH.YIN) {                          // Each contiguous run of non-separator and non-comma characters is a query
            keywords.addAll(normInput(lang, input));
        }
        if (keywords.isEmpty()) return null;

        // Columns to search
        boolean allowVariants = isLanguageHZ(lang) && Pref.getBool(R.string.pref_key_allow_variants, true) && (SEARCH.HZ == searchType);
        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_LANG);
        List<String> queries = new ArrayList<>();

        FILTER filter = Pref.getFilter();
        if (filter == FILTER.HZ && SEARCH.HZ == searchType) {
            qb.setTables(TABLE_NAME);
            for (int i = 0; i < keywords.size(); i++) {
                String key = keywords.get(i);
                String variant = allowVariants ? ("'" + key + "'") : "''";
                String[] projection = {"rowid AS _id", i + " AS rank", "0 AS vaIndex", variant + " AS variants", "*"};
                String sql = String.format("漢字 MATCH '%s'", key);
                queries.add(qb.buildQuery(projection, sql, null, null, null, null));
                if (allowVariants) {
                    projection[2] = "1 AS vaIndex";
                    sql = String.format("異體字 MATCH '%s'", key);
                    queries.add(qb.buildQuery(projection, sql, null, null, null, null));
                }
            }
            String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, null);
            qb.setTables("(" + query + ")");
            String[] projection = {"漢字", "'' AS 讀音", "'' AS 註釋", "'' AS 語言", "_id", "variants"};
            query = qb.buildQuery(projection, null, null, null, "rank,vaIndex,_id", null);
            return db.rawQuery(query, null);
        }

        String[] languages = getVisibleLanguages();
        String languageClause = (languages.length == 0)? "" : ("語言:" + String.join(" OR 語言:", languages));

        if (searchType == SEARCH.COMMENT) {
            String[] projection = {"rowid AS _id", "0 AS rank", "0 AS vaIndex", "null AS variants", "*", "trim(substr(字組, 0, 3)) AS 漢字"};
            String sql = String.format("langs MATCH '註釋:%s %s'", String.join(" 註釋:", keywords), languageClause);
            queries.add(qb.buildQuery(projection, sql, null, null, null, null));
        } else {
            if (searchType == SEARCH.YIN) {
                String hzs = getResult(String.format("SELECT group_concat(字組, ' ') from langs where langs MATCH '語言:%s 讀音:%s'", lang, String.join(" OR 讀音:", keywords)));
                if (TextUtils.isEmpty(hzs)) hzs = "";
                hzs = getResult(String.format("SELECT group_concat(漢字, ' ') from mcpdict where 漢字 MATCH '%s'", hzs.replaceAll(" ", " OR ")));
                if (TextUtils.isEmpty(hzs)) hzs = "";
                keywords = Arrays.asList(hzs.split(" "));
            } else if (searchType == SEARCH.DICT) {
                String dict = Pref.getDict();
                String match = TextUtils.isEmpty(dict) ? "mcpdict" : DB.getLabelByLanguage(dict);
                String hzs = getResult(String.format("SELECT group_concat(漢字, ' ') from mcpdict where %s MATCH '%s'", match, String.join(" ", keywords)));
                keywords = Arrays.asList((TextUtils.isEmpty(hzs) ? "" : hzs).split(" "));
            }

            int max_size = keywords.size();
            if (max_size > 100) max_size = 100;

            for (int i = 0; i < max_size; i++) {
                String key = keywords.get(i);
                if (HanZi.isUnknown(key)) {
                    String variant = allowVariants ? ("'" + key + "'") : "''";
                    String[] projection = {"rowid AS _id", i + " AS rank", "0 AS vaIndex", variant + " AS variants", "*", "'" + key + "' AS 漢字"};
                    String sql = String.format("langs MATCH '字組:%s 語言:%s'", key, label);
                    queries.add(qb.buildQuery(projection, sql, null, null, null, null));
                    continue;
                }
                String variant = allowVariants ? ("'" + key + "'") : "null";
                String[] projection = {"rowid AS _id", i + " AS rank", "0 AS vaIndex", variant + " AS variants", "*", "trim(substr(snippet(langs, '', ' ', ' ', 0, 1), 0, 3)) AS 漢字"};
                String sql = String.format("langs MATCH '字組:%s %s'", key, languageClause);
                queries.add(qb.buildQuery(projection, sql, null, null, null, null));
                if (allowVariants) {
                    projection[2] = "1 AS vaIndex";
                    String matchClause = getResult(String.format("SELECT group_concat(漢字, ' OR 字組:') from mcpdict where mcpdict MATCH '異體字: %s'", key));
                    if (!TextUtils.isEmpty(matchClause)) {
                        queries.add(qb.buildQuery(projection, String.format("langs MATCH '字組:%s %s'", matchClause, languageClause), null, null, null, null));
                    }
                }
            }
        }
        String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, String.valueOf(COL_ALL_LANGUAGES));

        // Build outer query statement (returning all information about the matching Chinese characters)
        qb.setTables("(" + query + ") AS u, info");
//        qb.setDistinct(true);
        String[] projection = {"漢字", "讀音", "註釋", "u.語言 AS 語言", "_id", "variants"};
        query = qb.buildQuery(projection, "info.簡稱 MATCH u.語言", null, null, "rank,vaIndex,漢字,"+ORDER, null);
        // Search
        return db.rawQuery(query, null);
    }

    public static Cursor directSearch(String hz) {
        // Search for a single Chinese character without any conversions
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_LANG);
        String[] projection = {"'" + hz + "' AS 漢字", "讀音", "註釋", "語言", "rowid AS _id", "NULL AS variants"};
        String[] languages = getVisibleLanguages();
        String languageClause = (languages.length == 0)? "" : ("語言:" + String.join(" OR 語言:", languages));
        String selection = String.format("langs MATCH '字組:%s %s'", hz, languageClause);
        String query = qb.buildQuery(projection, selection, null, null, null, null);
        return db.rawQuery(query, null);
    }

    public static Cursor getDictCursor(String hz) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {"*"};
        String selection = "漢字 MATCH ?";
        String query = qb.buildQuery(projection, selection, null, null, null, null);
        String[] args = {hz};
        return db.rawQuery(query, args);
    }

    public static Cursor getInputCursor(String input) {
        if (TextUtils.isEmpty(input) || isHzInput()) return null;
        String lang = Pref.getShape();
        boolean isYinPrompt = isYinPrompt();
        if (isYinInput() || isYinPrompt()) lang = Pref.getLabel();
        if (TextUtils.isEmpty(lang) || isLanguageHZ(lang)) lang = CMN;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {HZ, lang, "rowid as _id"};
        String selection = "";
        String field = isYinPrompt ? HZ : lang;
        if (isYinPrompt) {
            String charset = getCharsetSelect(2);
            String va = "";
            if (Pref.getBool(R.string.pref_key_allow_variants, true)) {
                va = " OR " + VA + ":" + input;
            }
            if (charset.contains("AND")) {
                selection = String.format("%s MATCH \"%s:%s %s\" %s", TABLE_NAME, field, input, va, charset);
            } else {
                selection = String.format("%s MATCH \"%s:%s %s%s\"", TABLE_NAME, field, input, va, charset);
            }
            selection += String.format(" AND %s is not null", lang);
            String query = qb.buildQuery(projection, selection, null, null, lang, "0,100");
            return db.rawQuery(query, null);
        } else {
            String charset = getCharsetSelect(0);
            if (TextUtils.isEmpty(charset)) {
                selection = String.format("%s MATCH ?", field);
            } else if (charset.contains("AND")) {
                selection = String.format("%s MATCH ? %s", field, charset);
            }
            String query = qb.buildQuery(projection, selection, null, null, lang, "0,100");
            List<String> keywords = normInput(field, input);
            if (keywords.isEmpty()) return null;
            String arg = String.join(" OR ", keywords);
            return db.rawQuery(query, new String[]{arg});
        }
    }

    public static void initFQ() {
        FQ = Pref.getStr(R.string.pref_key_fq, Pref.getString(R.string.default_fq));
        ORDER = FQ.replace(_FQ, _ORDER);
        COLOR = FQ.replace(_FQ, _COLOR);
        DIVISIONS = getFieldByLabel(HZ, FQ).split(",");
        SEARCH_COLUMNS = queryLabel(FIRST_FQ.replace(_FQ, _COLOR) + " is not null");
        LABELS = queryLabel(FQ + " is not null");
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
        COL_HZ = getColumnIndex(HZ);
        COL_SW = getColumnIndex(SW);
        COL_ZX = getColumnIndex(ZX);
        COL_BJJS = getColumnIndex(BJJS);
        COL_VA = getColumnIndex(VA);
        COL_HD = getColumnIndex(HD);
        COL_GYHZ = getColumnIndex(GYHZ);
        COL_KX = getColumnIndex(KX);
        COL_FIRST_DICT = COL_SW;
        COL_LAST_DICT = COL_HD;
        COL_FIRST_INFO = COL_VA;
        COL_LAST_INFO = COLUMNS.length - 2;
        COL_FIRST_SHAPE = COL_VA + 2;
        COL_LAST_SHAPE = COL_LAST_INFO;
        // COL_ALL_LANGUAGES = 5000; //TODO: not hardcoded
        cursor.close();
        ArrayList<String> arrayList = new ArrayList<>();
        for(int col = COL_FIRST_DICT; col <= COL_LAST_DICT; col++) {
            arrayList.add(getLanguageByLabel(COLUMNS[col]));
        }
        DICTIONARY_COLUMNS = arrayList.toArray(new String[0]);
        arrayList.clear();
        arrayList.add(Pref.getString(R.string.yin_code));
        arrayList.add(Pref.getString(R.string.yin_input));
        arrayList.add(GY);
        arrayList.add(CMN);
        arrayList.add(HK);
        arrayList.add(TW);
        arrayList.add(DGY);
        arrayList.add(KOR);
        arrayList.add(VI);
        arrayList.add(Pref.getString(R.string.shape_code));
        arrayList.addAll(Arrays.asList(COLUMNS).subList(COL_FIRST_SHAPE, COL_LAST_SHAPE + 1));
        SHAPE_COLUMNS = arrayList.toArray(new String[0]);
    
        qb.setTables(TABLE_INFO);
        query = qb.buildQuery(projection, selection,  null, null, null, null);
        cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        arrayList.clear();
        for(String s: cursor.getColumnNames()) {
            if (s.endsWith(_FQ)) arrayList.add(s);
        }
        FQ_COLUMNS = arrayList.toArray(new String[0]);
        cursor.close();
    }

    public static String[] getDictionaryColumns() {
        initArrays();
        return DICTIONARY_COLUMNS;
    }

    public static String[] getShapeColumns() {
        initArrays();
        return SHAPE_COLUMNS;
    }

    private static String[] query(String col, String selection, String args) {
        if (db == null) return null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {col};
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

    private static String[] queryLabel(String selection) {
        return queryLabel(selection, null);
    }

    private static String[] queryLabel(String selection, String args) {
        return query(LABEL, String.format("%s and rowid > 1", selection), args);
    }

    public static Cursor getLanguageCursor(CharSequence constraint) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {LANGUAGE, "rowid as _id"};
        String query = qb.buildQuery(projection, LANGUAGE + INDEX + " LIKE ? and 音節數 is not null",  null, null, ORDER, null);
        Cursor cursor = db.rawQuery(query, new String[]{"%"+constraint+"%"});
        if (cursor.getCount() > 0) return cursor;
        cursor.close();
        return getLanguageCursor("");
    }

    public static String[] getLanguages() {
        initArrays();
        if (LANGUAGES == null) {
            LANGUAGES = query(LANGUAGE, SYLLABLES + " is not null", null);
        }
        return LANGUAGES;
    }

    public static String[] getArrays(String col) {
        initArrays();
        return getFieldByLabel(HZ, col).split(",");
    }

    public static String[] getLabels() {
        initArrays();
        if (LABELS == null) {
            LABELS = queryLabel(FQ + " is not null");
        }
        return LABELS;
    }

    public static String[] getLabelsByFq(String type) {
        if (type.contentEquals("*")) return getLabels();
        if (TextUtils.isEmpty(type)) return null;
        return queryLabel(String.format("%s MATCH ?", FQ), type);
    }

    public static String[] getSearchColumns() {
        initArrays();
        if (SEARCH_COLUMNS == null) {
            SEARCH_COLUMNS = queryLabel(COLOR + " is not null");
        }
        return SEARCH_COLUMNS;
    }

    public static int getColumnIndex(String lang) {
        initArrays();
        for (int i = 0; i < COLUMNS.length; i++) {
            if (COLUMNS[i].contentEquals(lang)) return i;
        }
        return -1;
    }

    public static String getColumn(int i) {
        initArrays();
        return i < 0 ? "" : COLUMNS[i];
    }

    private static String matchColumns(String[] cols, String value) {
        ArrayList<String> array = new ArrayList<>();
        for (String s: cols) {
            array.add(String.format("%s:%s", s, value));
        }
        return String.join(" OR ", array);
    }

    public static String[] getVisibleLanguages() {
        FILTER filter = Pref.getFilter();
        String label = Pref.getLabel();
        switch (filter) {
            case AREA -> {
                int level = Pref.getInt(R.string.pref_key_area_level);
                String province = Pref.getProvince();
                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(province)) sb.append(String.format("%s:%s", DB.PROVINCE, province));
                if (level > 0) {
                    String[] levels = Pref.getStringArray(R.array.entries_area_level);
                    sb.append(String.format(" 行政區級別:%s", levels[level]));
                }
                if (!TextUtils.isEmpty(sb)) return queryLabel(String.format("info MATCH '%s'", sb));
            }
            case RECOMMEND -> {
                String value = Pref.getStr(R.string.pref_key_recommend, "");
                if (TextUtils.isEmpty(value)) break;
                return queryLabel(String.format("%s MATCH '%s'", DB.RECOMMEND, value));
            }
            case EDITOR -> {
                String value = Pref.getStr(R.string.pref_key_editor, "");
                if (TextUtils.isEmpty(value)) break;
                return queryLabel(String.format("info MATCH '%s'", matchColumns(EDITOR_COLUMNS, value)));
            }
            case DIVISION -> {
                String division = Pref.getDivision();
                if (TextUtils.isEmpty(division)) break;
                String[] a = DB.getLabelsByFq(division);
                if (a != null && a.length > 0) {
                    return a;
                }
            }
            case CUSTOM -> {
                Set<String> customs = Pref.getCustomLanguages();
                if (customs.isEmpty()) return new String[]{};
                ArrayList<String> array = new ArrayList<>();
                for (String lang: getLanguages()) {
                    if (customs.contains(lang)) {
                        array.add(getLabelByLanguage(lang));
                    }
                }
                return array.toArray(new String[0]);
            }
            case ISLAND -> {
                return queryLabel("方言島");
            }
            case CURRENT -> {
                ArrayList<String> array = new ArrayList<>();
                if (!TextUtils.isEmpty(label) && !isLanguageHZ(label)) array.add(label);
                boolean pfg = Pref.getBool(R.string.pref_key_pfg, false);
                if (pfg) {
                    if(!label.contentEquals(GY)) array.add(GY);
                    if(!label.contentEquals(CMN)) array.add(CMN);
                    if(!label.contentEquals(CMN_TW)) array.add(CMN_TW);
                }
                return array.toArray(new String[0]);
            }
        }
        return new String[]{};
    }

    public static boolean isLanguageHZ(String lang) {
        return lang.contentEquals(HZ);
    }

    public static boolean hasTone(String lang) {
        return getToneName(lang) != null;
    }

    public static String getResult(String sql) {
        if (db == null) return null;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            sb.append(cursor.getString(0));
        }
        cursor.close();
        return sb.toString();
    }

    public static String getField(String selection, String lang, String field) {
        if (db == null) return "";
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {String.format("\"%s\", \"%s\"", field, selection)};
        String query = qb.buildQuery(projection, selection + " MATCH ?",  null, null, null, null);
        Cursor cursor = db.rawQuery(query, new String[]{String.format("\"%s\"", lang)});
        String s = "";
        int n = cursor.getCount();
        if (n > 0) {
            cursor.moveToFirst();
            s = cursor.getString(0);
            for (int i = 1; i < n; i++) {
                cursor.moveToNext();
                String l = cursor.getString(1);
                if (!TextUtils.isEmpty(l) && l.contentEquals(lang)) {
                    s = cursor.getString(0);
                }
            }
        }
        cursor.close();
        if (TextUtils.isEmpty(s)) s = "";
        return s;
    }

    public static String getFieldByLabel(String lang, String field) {
        return getField(LABEL, lang, field);
    }

    public static String getFieldByLanguage(String lang, String field) {
        return getField(LANGUAGE, lang, field);
    }

    public static String getLabelByLanguage(String lang) {
        return getFieldByLanguage(lang, LABEL);
    }

    public static String getLabel(String lang) {
        return lang;
    }

    public static String getLabel(int i) {
        return getColumn(i);
    }

    public static int getColor(String lang, int i) {
        initArrays();
        String c = getFieldByLabel(lang, COLOR);
        if (TextUtils.isEmpty(c)) c = getFieldByLabel(lang, FIRST_FQ.replace(_FQ, _COLOR));
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
        return getFieldByLabel(lang, "網站");
    }

    public static String getDictLink(String lang) {
        return getFieldByLabel(lang, "網址");
    }

    public static String getLanguageByLabel(String label) {
        return getFieldByLabel(label, LANGUAGE);
    }

    private static String _getIntro(String language) {
        if (TextUtils.isEmpty(language) || Pref.getFilter() == FILTER.HZ) language = HZ;
        String intro = TextUtils.isEmpty(language) ? "" : getFieldByLanguage(language, "說明").replace("\n", "<br>");
        if (isLanguageHZ(language)) {
            StringBuilder sb = new StringBuilder();
            String[] fields = new String[] {"版本","字數"};
            for (String field: fields) {
                sb.append(String.format(Locale.getDefault(), "%s：%s<br>", field, getFieldByLanguage(language, field)));
            }
            sb.append(intro);
            intro = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.getDefault(), "%s%s<br>", Pref.getString(R.string.name), language));
            ArrayList<String> fields = new ArrayList<>(Arrays.asList("地點","經緯度", "作者", "錄入人", "維護人", "字表來源","參考文獻","補充閲讀","文件名","版本","字數","□數", SYLLABLES,"不帶調音節數","")); //,"相似度"
            fields.addAll(Arrays.asList(FQ_COLUMNS));
            fields.add("");
            for (String field: fields) {
                if (TextUtils.isEmpty(field)) sb.append("<br>");
                String value = getFieldByLanguage(language, field);
                if (!TextUtils.isEmpty(value) && !value.contentEquals("/")) {
                    if (field.endsWith(_FQ)) {
                        value = value.replace(","," ,").split(",")[0].trim();
                        if (TextUtils.isEmpty(value)) continue;
                    }
                    sb.append(String.format(Locale.getDefault(), "%s：%s<br>", field, value));
                }
            }
            sb.append(intro);
            intro = sb.toString();
        }
        return intro;
    }

    public static String getIntroText(String language) {
        initArrays();
        if (TextUtils.isEmpty(language)) language = Pref.getLanguage();
        String intro = _getIntro(language);
        if (TextUtils.isEmpty(language) || isLanguageHZ(language) || Pref.getFilter() == FILTER.HZ) {
            StringBuilder sb = new StringBuilder();
            sb.append(intro);
            sb.append("<br><h2>已收錄語言</h2><table border=1 cellSpacing=0>");
            sb.append("<tr>");
            String[] fields = new String[]{LANGUAGE, "字數", "□數", SYLLABLES, "不帶調音節數"};
            for (String field: fields) {
                sb.append(String.format("<th>%s</th>", field));
            }
            sb.append("</tr>");
            for (String l : LABELS) {
                sb.append("<tr>");
                for (String field: fields) {
                    sb.append(String.format("<td>%s</td>", getFieldByLabel(l, field)));
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
            intro = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("<h1>%s</h1>", language));
            sb.append(intro);
            String[] fields = new String[]{"音系說明", "解析日志", "同音字表"};
            for (String field: fields) {
                String text = getFieldByLanguage(language, field).replace("\n", "<br>");
                if (TextUtils.isEmpty(text)) continue;
                sb.append(String.format("<h2>%s</h2>%s", field, text));
            }
            intro = sb.toString();
        }
        return intro;
    }

    public static String getIntro() {
        initArrays();
        return _getIntro(Pref.getLanguage());
    }

    public static JSONObject getToneName(String lang) {
        String s = getFieldByLabel(lang, "聲調");
        if (TextUtils.isEmpty(s)) return null;
        try {
            return new JSONObject(s);
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static GeoPoint parseLocation(String location) {
        if (TextUtils.isEmpty(location)) return null;
        location = location.replace("[", "").replace("]", "").strip();
        String[] ss = location.split(", ?");
        if (ss.length != 2) return null;
        Double[] ds = new Double[2];
        int i = 0;
        for (String s: ss) {
            ds[i++] = Double.parseDouble(s);
        }
        return new GeoPoint(ds[1], ds[0]);
    }

    public static GeoPoint getPoint(String lang) {
        String location = getFieldByLabel(lang, "經緯度");
        return parseLocation(location);
    }

    public static int getSize(String lang) {
        String s = getFieldByLabel(lang, "地圖級別");
        if (TextUtils.isEmpty(s)) return 0;
        return Integer.parseInt(s);
    }

    public static boolean isLang(String lang) {
        return !TextUtils.isEmpty(getFieldByLabel(lang, "方言島")) && !isLanguageHZ(lang);
    }

    public static String[] getFqColumns() {
        initArrays();
        return FQ_COLUMNS;
    }

    public static String[] getDivisions() {
        initArrays();
        if (DIVISIONS == null) DIVISIONS = getFieldByLabel(HZ, FQ).split(",");
        return DIVISIONS;
    }

    public static String getWebFq(String lang) {
        initArrays();
        String s = getFieldByLabel(lang, FQ);
        if (TextUtils.isEmpty(s)) return "";
        if (s.contains(",")) {
            s = s.replace(",", " ,");
            String[] fs = s.split(",");
            if (fs.length < 2 || TextUtils.isEmpty(fs[1].trim())) return fs[0].trim();
            return fs[1].trim();
        }
        return s;
    }

    private static String formatIDS(String s) {
        s = s.replace("UCS2003", "2003")
            .replace("G", "陸")
            .replace("H", "港")
            .replace("M", "澳")
            .replace("T", "臺")
            .replace("J", "日")
            .replace("K", "韓")
            .replace("P", "朝")
            .replace("V", "越")
            .replace("U", "統")
            .replace("S", "大")
            .replace("B", "英")
            .replace("2003", "UCS2003")
            .replace(" ", "");
        return s;
    }

    public static String getUnicode(Cursor cursor) {
        String hz = cursor.getString(COL_HZ);
        String s = HanZi.toUnicode(hz);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<p>【統一碼】%s %s</p>", s, HanZi.getUnicodeExt(hz)));
        for (int j = DB.COL_FIRST_INFO; j <= DB.COL_LAST_INFO; j++) {
            s = cursor.getString(j);
            if (TextUtils.isEmpty(s)) continue;
            if (j == COL_ZX || j == COL_BJJS) s = formatIDS(s);
            s = s.replace(",", " ");
            sb.append(String.format("<p>【%s】%s</p>", getColumn(j), s));
        }
        return sb.toString();
    }

    public static boolean isHzInput() {
        String shape = Pref.getShape();
        return TextUtils.isEmpty(shape) || shape.contentEquals(Pref.getString(R.string.hz_input));
    }

    public static boolean isYinPrompt() {
        return Pref.getShape().contentEquals(Pref.getString(R.string.yin_prompt));
    }

    public static boolean isYinInput() {
        return Pref.getShape().contentEquals(Pref.getString(R.string.yin_input));
    }

    public static boolean isHzInputCode() {
        String shape = Pref.getShape();
        return isHzInput() || isYinPrompt() || shape.contentEquals(BJJS) || shape.contentEquals(LF) || shape.contentEquals(ZX) || shape.contentEquals(BS);
    }
}
