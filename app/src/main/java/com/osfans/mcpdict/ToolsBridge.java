package com.osfans.mcpdict;

import android.database.Cursor;
import android.webkit.JavascriptInterface;

import androidx.annotation.Keep;

import com.osfans.mcpdict.UI.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地 WebView 与 MCPDict Android 数据层之间的只读桥接。
 *
 * 不访问网络，不复制现代方言数据库，也不另带一份 QYS 数据：
 * - 语言列表来自 info；
 * - 广韵完整音韵地位来自 langs 中的“廣韻”；
 * - 现代字音来自 langs；
 * - 地图交给现有 UI.MapView。
 */
@Keep
public class ToolsBridge {
    private final ToolsActivity activity;

    public ToolsBridge(ToolsActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public int getApiVersion() {
        return 2;
    }

    @JavascriptInterface
    public String getLanguages() {
        JSONArray result = new JSONArray();
        // DB.ORDER 已由 DB.initFQ() 设置，对应 App 当前采用的“音典排序”等排序字段。
        String order = (DB.ORDER == null || DB.ORDER.isBlank()) ? "rowid" : DB.ORDER;
        String sql = String.format(
                "select 語言,簡稱,經緯度,地圖級別,聲調,地點,歷史音 " +
                        "from info where 音節數 is not null order by %s", order);
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return result.toString();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                put(row, "language", cursor.getString(0));
                put(row, "label", cursor.getString(1));
                put(row, "coordinate", cursor.getString(2));
                row.put("mapLevel", cursor.isNull(3) ? 0 : cursor.getInt(3));
                put(row, "toneConfig", cursor.getString(4));
                put(row, "location", cursor.getString(5));
                put(row, "historical", cursor.getString(6));
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    @JavascriptInterface
    public String getGuangyunRows() {
        JSONArray result = new JSONArray();
        Cursor cursor = DB.getCursor(
                "select 字組,讀音 from langs where 語言 MATCH '" + sqlQuote(ftsPhrase(DB.GY)) + "'");
        if (cursor == null) return result.toString();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String charGroup = cursor.getString(0);
                String reading = cursor.getString(1);
                if (reading == null) continue;
                String[] fields = reading.split("/", -1);
                // MiddleChinese.java 同样把 ss[18] 当作“切韻音系描述”。
                if (fields.length <= 20) continue;
                JSONObject row = new JSONObject();
                put(row, "charGroup", charGroup);
                put(row, "description", fields[18]);
                put(row, "she", fields.length > 19 ? fields[19] : "");
                put(row, "fanqieDescription", fields.length > 20 ? fields[20] : "");
                put(row, "guangyunRime", fields.length > 21 ? fields[21] : "");
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    @JavascriptInterface
    public String getLanguageRows(String label) {
        JSONArray result = new JSONArray();
        if (label == null || label.isBlank()) return result.toString();
        Cursor cursor = DB.getCursor(
                "select 字組,讀音 from langs where 語言 MATCH '" + sqlQuote(ftsPhrase(label)) + "'");
        if (cursor == null) return result.toString();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                put(row, "漢字", cursor.getString(0));
                put(row, "音標", cursor.getString(1));
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    @JavascriptInterface
    public String queryChars(String chars) {
        JSONArray result = new JSONArray();
        List<String> tokens = splitCodePoints(chars);
        if (tokens.isEmpty()) return result.toString();
        List<String> fts = new ArrayList<>();
        for (String token : tokens) {
            // 用户输入在网页端已限制为汉字；双引号再保证 FTS5 按单个 token 查询。
            fts.add("\"" + token.replace("\"", "\"\"") + "\"");
        }
        String match = String.join(" OR ", fts);
        Cursor cursor = DB.getCursor(
                "select 字組,語言,讀音,註釋 from langs where 字組 MATCH '" + sqlQuote(match) + "'");
        if (cursor == null) return result.toString();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                put(row, "字組", cursor.getString(0));
                put(row, "語言", cursor.getString(1));
                put(row, "讀音", cursor.getString(2));
                put(row, "註釋", cursor.getString(3));
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    @JavascriptInterface
    public void showCompareMap(String json) {
        if (json == null || json.isBlank()) return;
        try {
            JSONArray rows = new JSONArray(json);
            activity.runOnUiThread(() -> new MapView(activity, rows).show());
        } catch (JSONException ignored) {
        }
    }

    private static List<String> splitCodePoints(String s) {
        List<String> result = new ArrayList<>();
        if (s == null) return result;
        s.codePoints().forEach(cp -> {
            String value = new String(Character.toChars(cp));
            if (!value.isBlank() && !result.contains(value)) result.add(value);
        });
        return result;
    }

    private static String ftsPhrase(String s) {
        String value = s == null ? "" : s.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }

    private static String sqlQuote(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    private static void put(JSONObject object, String key, String value) throws JSONException {
        object.put(key, value == null ? "" : value);
    }
}
