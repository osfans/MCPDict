package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.text.TextUtils;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    public static final String LF = "兩分";
    public static final String ZX = "字形描述";
    public static final String WBH = "五筆畫";
    public static final String VA = "異體字";
    public static final String VS = "字形變體";
    public static final String FL = "分類";

    public static final String MAP = " \uD83C\uDF0F ";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String VARIANTS = "variants";
    public static final String COMMENT = "comment";
    public static final String INDEX = "索引";
    public static final String LANGUAGE = "語言";
    public static final String LABEL = "簡稱";

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
    public static final String JA_ = "日語";
    public static final String WB_ = "五筆";

    public static String FQ = null;
    public static String ORDER = null;
    public static String COLOR = null;
    public static final String _FQ = "分區";
    public static final String _COLOR = "顏色";
    public static final String _ORDER = "排序";
    public static final String FIRST_FQ = "地圖集二分區";
    private static String[] DIVISIONS = null;
    private static String[] LABELS = null;
    private static String[] LANGUAGES = null;
    private static String[] SEARCH_COLUMNS = null;
    private static String[] PROVINCES = null;

    public static int COL_HZ;
    public static int COL_BH;
    public static int COL_BS;
    public static int COL_SW;
    public static int COL_KX;
    public static int COL_GYHZ;
    public static int COL_HD;
    public static int COL_LF;
    public static int COL_ZX;
    public static int COL_WBH;
    public static int COL_VA;
    public static int COL_VS;
    public static int COL_FIRST_DICT, COL_LAST_DICT;
    public static int COL_FIRST_LANG, COL_LAST_LANG;
    public static int COL_FIRST_INFO, COL_LAST_INFO;
    public static int COL_FIRST_SHAPE, COL_LAST_SHAPE;

    //public static final int FILTER_ALL = 0;
    public static final int FILTER_ISLAND = 1;
    public static final int FILTER_HZ = 2;
    public static final int FILTER_LANGUAGE = 3;
    public static final int FILTER_PFG = 4;
    public static final int FILTER_PROVINCE = 5;
    public static final int FILTER_DIVISION = 6;
    public static final int FILTER_CUSTOM = 7;
    public static final int FILTER_COUNTY = 8;
    public static final int FILTER_TYPICAL = 9;

    public static int COL_ALL_LANGUAGES = 1000;
    public static final String ALL_LANGUAGES = "*";

    private static final String TABLE_NAME = "mcpdict";
    private static final String TABLE_INFO = "info";

    private static String[] JA_COLUMNS = null;
    private static String[] WB_COLUMNS = null;
    private static String[] COLUMNS;
    private static String[] FQ_COLUMNS;
    private static String[] DICT_COLUMNS;
    private static String[] SHAPE_COLUMNS;
    private static SQLiteDatabase db = null;

    public static void initialize(Context context) {
        if (db != null) return;
        db = new DB(context).getWritableDatabase();
        String userDbPath = UserDatabase.getDatabasePath();
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

    public static Cursor search() {
        // Search for one or more keywords, considering mode and options
        String input = Utils.getInput();
        String lang = Utils.getLabel();
        String shape = Utils.getShape();
        String dict = Utils.getDict();
        int type = Utils.getInt(R.string.pref_key_type, 0);

        if (!TextUtils.isEmpty(shape) && type < 2) lang = shape;
        if (!TextUtils.isEmpty(dict) && type == 3) {
            type = 2;
            lang = DB.getLabelByLanguage(dict);
        }

        // Get options and settings
        int charset = Utils.getInt(R.string.pref_key_charset, 0);
        boolean mcOnly = charset == 1;
        boolean kxOnly = charset == 3;
        boolean hdOnly = charset == 4;
        boolean swOnly = charset == 2;
        int cantoneseSystem = Utils.getStrAsInt(R.string.pref_key_cantonese_romanization, 0);

        // Split the input string into keywords and canonicalize them
        List<String> keywords = new ArrayList<>();
        if (type == 2){ //yi
            if (Orthography.HZ.isHz(input)) {
                String hzs = Utils.splitHZ(input);
                if (!TextUtils.isEmpty(hzs)) keywords.add(hzs);
            }
        } else if (lang.contentEquals(KX) || lang.contentEquals(HD)) {
            if (!TextUtils.isEmpty(input) && !Orthography.HZ.isPY(input)){
                if (Orthography.HZ.isSingleHZ(input)) lang = HZ;
                else input = ":" + input;
            }
        }
        else if (Orthography.HZ.isBH(input)) lang = BH;
        else if (Orthography.HZ.isBS(input)) {
            lang = BS;
            input = input.replace("-", "f");
        } else if (!TextUtils.isEmpty(shape)) { //WB, CJ, LF
            // not search hz
        } else if (Orthography.HZ.isHz(input)) lang = HZ;
        else if (Orthography.HZ.isUnicode(input)) {
            input = Orthography.HZ.toHz(input);
            lang = HZ;
        } else if (Orthography.HZ.isPY(input) && !isLang(lang)) lang = CMN;
        if (isHzMode(lang) && type == 0) {     // Each character is a query
            for (int unicode : input.codePoints().toArray()) {
                if (!Orthography.HZ.isHz(unicode)) continue;
                String hz = Orthography.HZ.toHz(unicode);
                if (keywords.contains(hz)) continue;
                keywords.add(hz);
            }
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
                if (TextUtils.isEmpty(token)) continue;
                token = token.toLowerCase(Locale.US);
                // Canonicalization
                switch (lang) {
                    case CMN: token = Orthography.Mandarin.canonicalize(token); break;
                    case HK: token = Orthography.Cantonese.canonicalize(token, cantoneseSystem); break;
                    case KOR:
                        token = Orthography.Korean.canonicalize(token); break;
                    case VI: token = Orthography.Vietnamese.canonicalize(token); break;
                    case JA_KAN:
                    case JA_GO:
                    case JA_OTHER:
                        token = Orthography.Japanese.canonicalize(token); break;
                    default:
                        break;
                }
                if (token == null) continue;
                List<String> allTones = null;
                if ((token.endsWith("?") || !Orthography.Tones.hasTone(token)) && hasTone(lang)) {
                    if (token.endsWith("?")) token = token.substring(0, token.length()-1);
                    allTones = switch (lang) {
                        case GY -> Orthography.MiddleChinese.getAllTones(token);
                        case CMN -> Orthography.Mandarin.getAllTones(token);
                        case HK -> Orthography.Cantonese.getAllTones(token);
                        case VI -> Orthography.Vietnamese.getAllTones(token);
                        default -> Orthography.Tones.getAllTones(token, lang);
                    };
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
        String[] columns = lang.contentEquals(JA_OTHER) ? JA_COLUMNS : new String[] {lang};
        if (lang.contentEquals(WBH)) columns = WB_COLUMNS;

        // Build inner query statement (a union query returning the id's of matching Chinese characters)
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        List<String> queries = new ArrayList<>();
        List<String> args = new ArrayList<>();
        boolean allowVariants = isHzMode(lang) && Utils.getBool(R.string.pref_key_allow_variants, true);
        for (int i = 0; i < keywords.size(); i++) {
            String variant = allowVariants ? ("\"" + keywords.get(i) + "\"") : "null";
            String[] projection = {"rowid AS _id", i + " AS rank", "offsets(mcpdict) AS vaIndex", variant + " AS variants"};
            String key = keywords.get(i);
            String sel = " MATCH ?";
            if (key.startsWith("%") && key.endsWith("%")) {
                sel = " LIKE ?";
            }
            for (String column : columns) {
                String col = "\"" + column + "\"";
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
            selection += String.format(" AND `%s` MATCH '%s'", FL, Utils.getStringArray(R.array.pref_values_charset)[charset]);
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

    public static void initFQ() {
        FQ = Utils.getStr(R.string.pref_key_fq, Utils.getStringRes(R.string.default_fq));
        ORDER = FQ.replace(_FQ, _ORDER);
        COLOR = FQ.replace(_FQ, _COLOR);
        DIVISIONS = getFieldByLabel(HZ, FQ).split(",");
        SEARCH_COLUMNS = queryLabel(FIRST_FQ.replace(_FQ, _COLOR) + " is not null");
        LABELS = queryLabel(FQ + " is not null and rowid > 1");
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
        COL_ZX = getColumnIndex(ZX);
        COL_VA = getColumnIndex(VA);
        COL_VS = getColumnIndex(VS);
        COL_HD = getColumnIndex(HD);
        COL_GYHZ = getColumnIndex(GYHZ);
        COL_KX = getColumnIndex(KX);
        COL_WBH = getColumnIndex(WBH);
        COL_FIRST_DICT = COL_SW;
        COL_LAST_DICT = COL_HD;
        COL_FIRST_LANG = COL_LAST_DICT + 1;
        COL_LAST_LANG = COL_VA - 1;
        COL_FIRST_INFO = COL_VA;
        COL_LAST_INFO = COLUMNS.length - 2;
        COL_FIRST_SHAPE = COL_VA + 2;
        COL_LAST_SHAPE = COL_LAST_INFO;
        cursor.close();
        arrayList.clear();
        for(int col = COL_FIRST_DICT; col <= COL_LAST_DICT; col++) {
            arrayList.add(getLanguageByLabel(COLUMNS[col]));
        }
        DICT_COLUMNS = arrayList.toArray(new String[0]);
        arrayList.clear();
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

    public static String[] getDictColumns() {
        initArrays();
        return DICT_COLUMNS;
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
        return query(LABEL, selection, args);
    }

    private static String[] queryLanguage(String selection) {
        return query(LANGUAGE, selection, null);
    }

    public static Cursor getLanguageCursor(CharSequence constraint) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_INFO);
        String[] projection = {LANGUAGE, "rowid as _id"};
        String query = qb.buildQuery(projection, LANGUAGE + INDEX + " LIKE ? and 序號 is not null",  null, null, ORDER, null);
        Cursor cursor = db.rawQuery(query, new String[]{"%"+constraint+"%"});
        if (cursor.getCount() > 0) return cursor;
        cursor.close();
        return getLanguageCursor("");
    }

    public static String[] getLanguages() {
        initArrays();
        if (LANGUAGES == null) {
            LANGUAGES = queryLanguage("序號 is not null");
        }
        return LANGUAGES;
    }

    public static String[] getProvinces() {
        initArrays();
        if (PROVINCES == null || PROVINCES.length < 2) PROVINCES = getFieldByLabel(HZ, "省").split(",");
        return PROVINCES;
    }

    public static String[] getLabels() {
        initArrays();
        if (LABELS == null) {
            LABELS = queryLabel(FQ + " is not null and rowid > 1");
        }
        return LABELS;
    }

    public static String[] getLabelsByFq(String type) {
        if (type.contentEquals("*")) return getLabels();
        if (TextUtils.isEmpty(type)) return null;
        return queryLabel(String.format("%s MATCH ? and rowid > 1", FQ), type);
    }

    public static String[] getLabelsByProvince(String type) {
        if (TextUtils.isEmpty(type)) return null;
        return queryLabel(String.format("%s MATCH ? and rowid > 1", "省"), type);
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

    public static String[] getVisibleColumns() {
        int filter = Utils.getFilter();
        String label = Utils.getLabel();
        switch (filter) {
            case FILTER_PROVINCE -> {
                String province = Utils.getProvince();
                if (!TextUtils.isEmpty(province)) {
                    String[] a = DB.getLabelsByProvince(province);
                    if (a != null && a.length > 0) {
                        return a;
                    }
                }
            }
            case FILTER_DIVISION -> {
                String division = Utils.getDivision();
                if (!TextUtils.isEmpty(division)) {
                    String[] a = DB.getLabelsByFq(division);
                    if (a != null && a.length > 0) {
                        return a;
                    }
                }
            }
            case FILTER_CUSTOM -> {
                Set<String> customs = Utils.getStrSet(R.string.pref_key_custom_languages);
                if (customs == null || customs.isEmpty()) return LABELS;
                ArrayList<String> array = new ArrayList<>();
                for (String lang: getLabels()) {
                    if (!array.contains(lang) && customs.contains(lang)) {
                        array.add(lang);
                    }
                }
                return array.toArray(new String[0]);
            }
            case FILTER_ISLAND -> {
                return queryLabel("方言島");
            }
            case FILTER_COUNTY -> {
                return queryLabel("級別  >= \"3\"");
            }
            case FILTER_TYPICAL -> {
                return queryLabel("級別  >= \"5\"");
            }
            case FILTER_HZ -> {
                return new String[]{};
            }
            case FILTER_LANGUAGE -> {
                if (getColumnIndex(label) >= 0) return new String[]{label};
            }
            case FILTER_PFG -> {
                Set<String> array = new HashSet<>();
                array.add(GY);
                array.add(CMN);
                if (getColumnIndex(label) >= 0) array.add(label);
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
        String query = qb.buildQuery(projection, selection + " match ?",  null, null, null, null);
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
        if (TextUtils.isEmpty(language) || Utils.getFilter() == FILTER_HZ) language = HZ;
        String intro = TextUtils.isEmpty(language) ? "" : getFieldByLanguage(language, "說明").replace("\n", "<br>");
        if (language.contentEquals(HZ)) {
            intro = String.format(Locale.getDefault(), "%s%s<br>%s", Utils.getStringRes(R.string.version), BuildConfig.VERSION_NAME, intro);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.getDefault(), "%s%s<br>", Utils.getStringRes(R.string.name), language));
            ArrayList<String> fields = new ArrayList<>(Arrays.asList("序號","地點","經緯度","錄入人","參考資料","文件名","版本","字數","□數", "音節數","不帶調音節數",""));
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
        if (TextUtils.isEmpty(language)) language = Utils.getLanguage();
        String intro = _getIntro(language);
        if (language.contentEquals(HZ)) {
            StringBuilder sb = new StringBuilder();
            sb.append(intro);
            sb.append("<br><h2>已收錄語言</h2><table border=1 cellspacing=0>");
            sb.append("<tr>");
            String[] fields = new String[]{LANGUAGE, "字數", "□數", "音節數", "不帶調音節數"};
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
            intro = String.format(Locale.getDefault(), "<h1>%s</h1>%s<h2>音系說明</h2><h2>同音字表</h2>", language, intro);
        }
        return intro;
    }

    public static String getIntro() {
        initArrays();
        return _getIntro(Utils.getLanguage());
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
        String s = getFieldByLabel(lang, "級別");
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
        String s = Orthography.HZ.toUnicode(hz);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<p>【統一碼】%s %s</p>", s, Orthography.HZ.getUnicodeExt(hz)));
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
