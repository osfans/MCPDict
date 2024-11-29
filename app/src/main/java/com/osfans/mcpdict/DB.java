package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.text.TextUtils;

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

    private static final String DATABASE_NAME = "mcpdict.db";
    private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;

    // Must be the same order as defined in the string array "search_as"

    public static final String HZ = "漢字";
    public static final String BH = "總筆畫數";
    public static final String BS = "部首餘筆";
    public static final String SW = "說文";
    public static final String GYHZ = "匯纂";
    public static final String KX = "康熙";
    public static final String HD = "漢大";
    public static final String ZX = "字形描述";
    public static final String WBH = "五筆畫";
    public static final String VA = "異體字";
    public static final String FL = "分類";

    public static final String MAP = " \uD83C\uDF0F ";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String VARIANTS = "variants";
    public static final String COMMENT = "comment";
    public static final String INDEX = "索引";
    public static final String LANGUAGE = "語言";
    public static final String LABEL = "簡稱";
    public static final String SYLLABLES = "音節數";

    public static final String SG = "鄭張";
    public static final String BA = "白-沙";
    public static final String GY = "廣韻";
    public static final String ZYYY = "中原音韻";
    public static final String DGY = "東干語";
    public static final String CMN = "普通話";
    public static final String HK = "香港";
    public static final String TW = "臺灣";
    public static final String KOR = "朝鮮";
    public static final String VI = "越南";
    public static final String JA_GO = "日語吳音";
    public static final String JA_KAN = "日語漢音";
    public static final String JA_OTHER = "日語其他";
    public static final String WB_ = "五筆";
    public static final String CJ_ = "倉頡";

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

    public static int COL_HZ;
    public static int COL_SW;
    public static int COL_KX;
    public static int COL_GYHZ;
    public static int COL_HD;
    public static int COL_ZX;
    public static int COL_VA;
    public static int COL_FIRST_DICT, COL_LAST_DICT;
    public static int COL_FIRST_LANG, COL_LAST_LANG;
    public static int COL_FIRST_INFO, COL_LAST_INFO;
    public static int COL_FIRST_SHAPE, COL_LAST_SHAPE;

    public enum SEARCH {
        HZ, YIN, YI, DICT,
    }

    public enum FILTER {
        ALL, ISLAND, HZ, CURRENT, RECOMMEND, CUSTOM, DIVISION, AREA, EDITOR
    }

    public static int COL_ALL_LANGUAGES = 1000;
    public static final String ALL_LANGUAGES = "*";

    private static final String TABLE_NAME = "mcpdict";
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
        db = new DB(context).getWritableDatabase();
        String userDbPath = UserDB.getDatabasePath();
        db.execSQL("ATTACH DATABASE '" + userDbPath + "' AS user");
        initArrays();
        initFQ();
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

    private static String[] getMatchColumns(String lang, SEARCH searchType, boolean allowVariants) {
        List<String> columns = new ArrayList<>();
        if (lang.contentEquals(JA_OTHER))
            columns.addAll(Arrays.asList(JA_COLUMNS));
        else {
            if (searchType == SEARCH.YI) {
                String[] cols = getVisibleColumns();
                if (cols.length <= 100) columns.addAll(Arrays.asList(cols));
                else lang = TABLE_NAME;
            }
            if (!columns.contains(lang) && !columns.contains(TABLE_NAME)) columns.add(lang);
        }
        if (allowVariants) columns.add(VA);
        return columns.toArray(new String[0]);
    }

    private static String getCharsetSelect() {
        // Get options and settings
        int charset = Pref.getInt(R.string.pref_key_charset);
        String value = Pref.getStringArray(R.array.pref_values_charset)[charset];
        String selection;
        if (charset <= 5) {
            selection = String.format(" AND `%s` IS NOT NULL", value);
        } else {
            selection = String.format(" AND `%s` MATCH '%s'", FL, value);
        }
        return selection;
    }

    private static List<String> normInput(String lang, String input) {
        List<String> keywords = new ArrayList<>();
        if (lang.contentEquals(BS)) input = input.replace("-", "f");
        else if (lang.startsWith(CJ_) || (lang.startsWith(WB_) && !lang.contentEquals(WBH))) input += "*";
        else if (lang.contentEquals(KOR)) { // For Korean, put separators around all hangul
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
        int cantoneseSystem = Pref.getStrAsInt(R.string.pref_key_cantonese_romanization, 0);
        for (String token : input.split("[\\s,]+")) {
            if (TextUtils.isEmpty(token)) continue;
            // Canonicalization
            switch (lang) {
                case CMN: token = Mandarin.canonicalize(token); break;
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
                    case CMN -> Mandarin.getAllTones(token);
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
        String lang = Pref.getLabel();
        String dict = Pref.getDict();
        SEARCH searchType = SEARCH.values()[Pref.getInt(R.string.pref_key_type)];
        boolean isAll = Pref.getFilter() == FILTER.ALL;

        if (input.startsWith("-")) input = input.substring(1); //may crash sqlite
        if (searchType == SEARCH.DICT) {
            searchType = SEARCH.YI;
            lang = TextUtils.isEmpty(dict) ? TABLE_NAME : DB.getLabelByLanguage(dict);
        }

        // Split the input string into keywords and canonicalize them
        List<String> keywords = new ArrayList<>();
        if (searchType == SEARCH.YI){ //yi
            if (HanZi.isHz(input)) {
                String hzs = DisplayHelper.normInput(input);
                if (!TextUtils.isEmpty(hzs)) keywords.add(hzs);
            }
            if (isAll) lang = TABLE_NAME;
        }
        else if (HanZi.isBH(input)) lang = BH;
        else if (HanZi.isBS(input)) {
            lang = BS;
        } else if (HanZi.isHz(input)) {
            lang = HZ;
        } else if (HanZi.isUnicode(input)) {
            input = HanZi.toHz(input);
            lang = HZ;
        } else if (HanZi.isPY(input) && !isLang(lang)) lang = CMN;
        if (isHzMode(lang) && searchType == SEARCH.YIN) searchType = SEARCH.HZ;
        if (isHzMode(lang) && searchType == SEARCH.HZ) {     // Each character is a query
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
        boolean allowVariants = isHzMode(lang) && Pref.getBool(R.string.pref_key_allow_variants, true) && (SEARCH.HZ == searchType);
        String[] columns = getMatchColumns(lang, searchType, allowVariants);

        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        List<String> queries = new ArrayList<>();
        List<String> args = new ArrayList<>();

        for (int i = 0; i < keywords.size(); i++) {
            String key = keywords.get(i);
            String variant = allowVariants ? ("'" + key + "'") : "null";
            String[] projection = {"rowid AS _id", i + " AS rank", "offsets(mcpdict) AS vaIndex", variant + " AS variants"};
            String sel = (key.startsWith("%") && key.endsWith("%")) ? "LIKE" : "MATCH";
            for (String col : columns) {
                queries.add(qb.buildQuery(projection, String.format("`%s` %s ?", col, sel), null, null, null, null));
                args.add(key);
            }
        }
        String query = qb.buildUnionQuery(queries.toArray(new String[0]), null, null);

        // Build outer query statement (returning all information about the matching Chinese characters)
        qb.setTables("(" + query + ") AS u, mcpdict AS v LEFT JOIN user.favorite AS w ON v.漢字 = w.hz");
        qb.setDistinct(true);
        String[] projection = {"v.*", "_id",
                   "v.漢字 AS `漢字`", "variants",
                   "timestamp IS NOT NULL AS is_favorite", "comment"};
        String selection = "u._id = v.rowid" + getCharsetSelect();
        query = qb.buildQuery(projection, selection, null, null, "rank,vaIndex", "0,100");

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
        String selection = "v.漢字 MATCH ?";
        String query = qb.buildQuery(projection, selection, null, null, null, "0,100");
        String[] args = {hz};
        return db.rawQuery(query, args);
    }

    public static Cursor getShapeCursor(String input) {
        String lang = Pref.getShape();
        if (TextUtils.isEmpty(lang)) return null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        String[] projection = {HZ, lang, "rowid as _id"};
        String selection = String.format("%s MATCH ?", lang);
        String query = qb.buildQuery(projection, selection, null, null, lang, "0,100");
        List<String> keywords = normInput(lang, input);
        if (keywords.isEmpty()) return null;
        String arg = String.join(" OR ", keywords);
        return db.rawQuery(query, new String[]{arg});
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
        COL_VA = getColumnIndex(VA);
        COL_HD = getColumnIndex(HD);
        COL_GYHZ = getColumnIndex(GYHZ);
        COL_KX = getColumnIndex(KX);
        COL_FIRST_DICT = COL_SW;
        COL_LAST_DICT = COL_HD;
        COL_FIRST_LANG = COL_LAST_DICT + 1;
        COL_LAST_LANG = COL_VA - 1;
        COL_FIRST_INFO = COL_VA;
        COL_LAST_INFO = COLUMNS.length - 2;
        COL_FIRST_SHAPE = COL_VA + 2;
        COL_LAST_SHAPE = COL_LAST_INFO;
        cursor.close();
        ArrayList<String> arrayList = new ArrayList<>();
        for(int col = COL_FIRST_DICT; col <= COL_LAST_DICT; col++) {
            arrayList.add(getLanguageByLabel(COLUMNS[col]));
        }
        DICTIONARY_COLUMNS = arrayList.toArray(new String[0]);
        arrayList.clear();
        arrayList.add(GY);
        arrayList.add(CMN);
        arrayList.add(HK);
        arrayList.add(TW);
        arrayList.add(DGY);
        arrayList.add(KOR);
        arrayList.add(VI);
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

    private static String[] queryLanguage(String selection) {
        return query(LANGUAGE, selection, null);
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
            LANGUAGES = queryLanguage(SYLLABLES + " is not null");
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

    public static String[] getVisibleColumns() {
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
            case HZ -> {
                return new String[]{};
            }
            case CURRENT -> {
                ArrayList<String> array = new ArrayList<>();
                if (!TextUtils.isEmpty(label) && !label.contentEquals(HZ)) array.add(label);
                boolean pfg = Pref.getBool(R.string.pref_key_pfg, false);
                if (pfg) {
                    if(!label.contentEquals(GY)) array.add(GY);
                    if(!label.contentEquals(CMN)) array.add(CMN);
                }
                return array.toArray(new String[0]);
            }
        }
        return LABELS;
    }

    public static boolean isHzMode(String lang) {
        return lang.contentEquals(HZ);
    }

    public static boolean hasTone(String lang) {
        return getToneName(lang) != null;
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
        if (language.contentEquals(HZ)) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", Pref.getString(R.string.version), BuildConfig.VERSION_NAME, intro);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.getDefault(), "%s%s<br>", Pref.getString(R.string.name), language));
            ArrayList<String> fields = new ArrayList<>(Arrays.asList("地點","經緯度", "作者", "錄入人", "維護人","來源", "參考文獻","文件名","版本","字數","□數", SYLLABLES,"不帶調音節數",""));
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
        if (language.contentEquals(HZ) || Pref.getFilter() == FILTER.HZ) {
            StringBuilder sb = new StringBuilder();
            sb.append(intro);
            sb.append("<br><h2>已收錄語言</h2><table border=1 cellspacing=0>");
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
            String phonology = getFieldByLanguage(language, "音系").replace("\n", "<br>");
            intro = String.format(Locale.getDefault(), "<h1>%s</h1>%s<h2>音系說明</h2>%s<h2>同音字表</h2>", language, intro, phonology);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Double getLocation(String lang, int pos) {
        String location = getFieldByLabel(lang, "經緯度");
        if (TextUtils.isEmpty(location)) return null;
        return Double.parseDouble(location.split(",")[pos]);
    }

    private static Double getLat(String lang) {
        return getLocation(lang, 1);
    }

    private static Double getLong(String lang) {
        return getLocation(lang, 0);
    }

    public static GeoPoint getPoint(String lang) {
        if (getLat(lang) == null) return null;
        return new GeoPoint(getLat(lang), getLong(lang));
    }

    public static int getSize(String lang) {
        String s = getFieldByLabel(lang, "地圖級別");
        if (TextUtils.isEmpty(s)) return 0;
        return Integer.parseInt(s);
    }

    private static String getLangType(String lang) {
        return getFieldByLabel(lang, FIRST_FQ);
    }

    public static boolean isLang(String lang) {
        return !TextUtils.isEmpty(getLangType(lang)) && !lang.contentEquals(HZ);
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
            .replace("2003", "UCS2003");
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
            if (j == COL_ZX) s = formatIDS(s);
            s = s.replace(",", " ");
            sb.append(String.format("<p>【%s】%s</p>", getColumn(j), s));
        }
        return sb.toString();
    }
}
