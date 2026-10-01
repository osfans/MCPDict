package com.osfans.mcpdict;

import android.database.Cursor;
import android.webkit.JavascriptInterface;

import androidx.annotation.Keep;

import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.UI.MapView;
import com.osfans.mcpdict.Util.OpenCC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        return 3;
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

                // Match MCPDict's own language colours (LanguageAdapter / ResultAdapter).
                String label = cursor.getString(1);
                try {
                    put(row, "color", colorToCss(DB.getColor(label)));
                    put(row, "subColor", colorToCss(DB.getSubColor(label)));
                } catch (Exception ignored) {
                    put(row, "color", "#64748b");
                    put(row, "subColor", "#64748b");
                }

                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    /**
     * Reuse MCPDict's own language-search path.  DB.getLanguageCursor() already
     * expands Simplified/Traditional variants through OpenCC and also supports
     * location matching, so the tools page behaves like the main dictionary.
     */
    @JavascriptInterface
    public String searchLanguages(String query) {
        JSONArray result = new JSONArray();
        CharSequence constraint = query == null ? "" : query.trim();
        Cursor cursor = DB.getLanguageCursor(constraint, "");
        if (cursor == null) return result.toString();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                put(row, "language", cursor.getString(0));
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    /**
     * Return Simplified/Traditional variants using the same OpenCC layer used by
     * MCPDict search.  The Web UI uses this for local result filtering without
     * forcing the main DB cursor to fall back to the full language list.
     */
    @JavascriptInterface
    public String getTextVariants(String text) {
        JSONArray result = new JSONArray();
        LinkedHashSet<String> values = new LinkedHashSet<>();
        String source = text == null ? "" : text.trim();
        if (source.isEmpty()) return result.toString();

        values.add(source);
        try {
            String[] converted = OpenCC.convertAll(source);
            if (converted != null) {
                for (String value : converted) {
                    if (value != null && !value.isBlank()) values.add(value);
                }
            }
        } catch (Exception ignored) {
        }

        for (String value : values) result.put(value);
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
                String raw = cursor.getString(1);
                String display = displayReadings(label, raw);

                JSONObject row = new JSONObject();
                put(row, "漢字", cursor.getString(0));
                // The tools analyse the same displayed reading that MCPDict shows.
                put(row, "音標", display);
                // Keep the database source form for diagnostics if needed.
                put(row, "原始音標", raw);
                result.put(row);
            }
        } catch (JSONException ignored) {
        } finally {
            cursor.close();
        }
        return result.toString();
    }

    /**
     * Resolve user-entered Simplified/Traditional characters to MCPDict's
     * canonical character entries.  The returned candidates are shown as
     * checkboxes in the tools page so the user decides which character(s) to
     * compare.
     */
    @JavascriptInterface
    public String resolveCharacters(String chars) {
        JSONArray result = new JSONArray();
        List<String> tokens = splitCodePoints(chars);

        for (String token : tokens) {
            LinkedHashSet<String> searchForms = new LinkedHashSet<>();
            searchForms.add(token);

            try {
                String[] converted = OpenCC.convertAll(token);
                if (converted != null) {
                    for (String form : converted) {
                        if (isSingleCodePoint(form)) searchForms.add(form);
                    }
                }
            } catch (Exception ignored) {
            }

            LinkedHashSet<String> candidates = new LinkedHashSet<>();
            for (String form : searchForms) {
                addCharacterMatches(candidates, form, false);
            }
            for (String form : searchForms) {
                addCharacterMatches(candidates, form, true);
            }

            // If the character exists only in langs (or conversion dictionaries
            // did not yield a canonical mcpdict entry), keep usable forms.
            if (candidates.isEmpty()) {
                for (String form : searchForms) {
                    if (hasLanguageReading(form)) candidates.add(form);
                }
            }
            if (candidates.isEmpty()) candidates.add(token);

            try {
                JSONObject group = new JSONObject();
                put(group, "input", token);
                JSONArray values = new JSONArray();
                for (String candidate : candidates) values.put(candidate);
                group.put("candidates", values);
                result.put(group);
            } catch (JSONException ignored) {
            }
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
                String lang = cursor.getString(1);
                String raw = cursor.getString(2);
                String display = displayReadings(lang, raw);

                JSONObject row = new JSONObject();
                put(row, "字組", cursor.getString(0));
                put(row, "語言", lang);
                put(row, "讀音", display);
                put(row, "原始讀音", raw);
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

    private static void addCharacterMatches(Set<String> out,
                                            String form,
                                            boolean variants) {
        if (!isSingleCodePoint(form)) return;

        String column = variants ? "異體字" : "漢字";
        String sql = "select 漢字 from mcpdict where " + column + " MATCH '"
                + sqlQuote(ftsPhrase(form)) + "' limit 20";
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return;
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(0);
                if (isSingleCodePoint(hz) && hasLanguageReading(hz)) {
                    out.add(hz);
                }
            }
        } finally {
            cursor.close();
        }
    }

    private static boolean hasLanguageReading(String hz) {
        if (!isSingleCodePoint(hz)) return false;
        Cursor cursor = DB.getCursor(
                "select 字組 from langs where 字組 MATCH '"
                        + sqlQuote(ftsPhrase(hz)) + "' limit 1");
        if (cursor == null) return false;
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    private static boolean isSingleCodePoint(String value) {
        return value != null
                && !value.isBlank()
                && value.codePointCount(0, value.length()) == 1;
    }

    /**
     * DisplayHelper normally replaces tab separators with spaces.  The tools
     * need to keep individual readings separate, so format each reading first
     * and then restore the tab delimiter.
     */
    private static String displayReadings(String lang, String raw) {
        if (raw == null || raw.isBlank()) return "";
        String[] readings = raw.split("\\t", -1);
        List<String> output = new ArrayList<>();
        for (String reading : readings) {
            if (reading == null || reading.isBlank()) continue;
            output.add(DisplayHelper.getIPA(lang, reading).toString().trim());
        }
        return String.join("\t", output);
    }

    private static String colorToCss(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
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
