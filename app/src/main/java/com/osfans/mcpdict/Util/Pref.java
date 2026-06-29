package com.osfans.mcpdict.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pref {
    private static class CustomLanguageSchemeStore {
        final LinkedHashMap<String, LinkedHashSet<String>> schemes = new LinkedHashMap<>();
        String current;
    }

    private static Context getContext() {
        return App.getContext();
    }

    public static SharedPreferences get() {
        return getContext().getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(getContext()), Context.MODE_PRIVATE);
    }

    public static int getStrAsInt(int key, int defaultValue) {
        String value = getStr(key);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return defaultValue;
    }

    public static String getString(int key) {
        return getContext().getString(key);
    }

    public static String getString(int key, Object... formatArgs) {
        return getContext().getString(key, formatArgs);
    }

    public static String[] getStringArray(int id) {
        return getContext().getResources().getStringArray(id);
    }

    public static void putBool(int key, boolean value) {
         get().edit().putBoolean(getContext().getString(key), value).apply();
    }

    public static boolean getBool(int key, boolean defaultValue) {
        return get().getBoolean(getContext().getString(key), defaultValue);
    }

    public static void putStr(int key, String value) {
        get().edit().putString(getContext().getString(key), value).apply();
    }

    public static String getStr(int key, String defaultValue) {
        return get().getString(getContext().getString(key), defaultValue);
    }

    public static String getStr(int key) {
        return getStr(key, "");
    }
    
    public static Set<String> getStrSet(int key) {
        return getStrSet(key, new HashSet<>());
    }

    public static Set<String> getStrSet(int key, Set<String> defaultValue) {
        return new HashSet<>(get().getStringSet(getContext().getString(key), defaultValue));
    }

    public static void putStrSet(int key, String value) {
        Set<String> set = getStrSet(key);
        if (set.contains(value)) set.remove(value);
        else set.add(value);
        get().edit().putStringSet(getContext().getString(key), set).apply();
    }

    public static int getInt(int key, int defValue) {
        return get().getInt(getContext().getString(key), defValue);
    }

    public static int getInt(int key) {
        return getInt(key, 0);
    }

    public static void putInt(int key, int value) {
        get().edit().putInt(getContext().getString(key), value).apply();
    }

    public static void putInput(String value) {
        putStr(R.string.pref_key_input, value);
    }

    public static String getDict() {
        String value = getStr(R.string.pref_key_dict);
        if (value.contentEquals(getString(R.string.dict))) value = "";
        return value;
    }

    public static void putDict(String value) {
        putStr(R.string.pref_key_dict, value);
    }

    public static String getShape() {
        return getStr(R.string.pref_key_shape, getString(R.string.hz_input));
    }

    public static void putShape(String value) {
        putStr(R.string.pref_key_shape, value);
    }

    public static String getProvince() {
        String value = getStr(R.string.pref_key_province);
        if (value.contentEquals(getString(R.string.province))) value = "";
        return value;
    }

    public static void putProvince(String value) {
        putStr(R.string.pref_key_province, value);
    }

    public static String getDivision() {
        String value = getStr(R.string.pref_key_division);
        if (value.contentEquals(getString(R.string.division))) value = "";
        return value;
    }

    public static void putDivision(String value) {
        putStr(R.string.pref_key_division, value);
    }

    public static DB.FILTER getFilter() {
        int i = getInt(R.string.pref_key_filters);
        return DB.FILTER.values()[i];
    }

    public static void putFilter(int value) {
        putInt(R.string.pref_key_filters, value);
    }

    public enum ISLAND_HISTORY {
        NONE, ISLAND_ONLY, ISLAND_HIDE, HISTORY_ONLY, HISTORY_HIDE,
    }

    public static ISLAND_HISTORY getIslandHistory() {
        int i = getInt(R.string.pref_key_island_history);
        return ISLAND_HISTORY.values()[i];
    }

    public static void putIslandHistory(int value) {
        putInt(R.string.pref_key_island_history, value);
    }

    public static String getInput() {
        return getStr(R.string.pref_key_input);
    }

    public static void putLanguage(String value) {
        putStr(R.string.pref_key_language, value);
    }

    public static void putLabel(String lang) {
        String language = DB.getLanguageByLabel(lang);
        putLanguage(language);
    }

    public static String getLanguage() {
        return getStr(R.string.pref_key_language);
    }

    public static String getLabel() {
        String language = getStr(R.string.pref_key_language);
        if (TextUtils.isEmpty(language)) language = DB.HZ;
        return DB.getLabelByLanguage(language);
    }

    public static int getToneStyle(int id) {
        int value = 0;
        if (id == R.string.pref_key_tone_display) value = 1;
        return getStrAsInt(id, value);
    }

    public static String[] getToneStyles(int id) {
        String[] defaultList = new String[5];
        if (id == R.string.pref_key_zyyy_display) defaultList = getStringArray(R.array.pref_default_values_zyyy_display);
        else if (id == R.string.pref_key_dgy_display) defaultList = getStringArray(R.array.pref_default_values_dgy_display);
        else if (id == R.string.pref_key_mc_display) defaultList = getStringArray(R.array.pref_default_values_mc_display);
        else if (id == R.string.pref_key_zt_display) defaultList = getStringArray(R.array.pref_default_values_zt_display);
        try {
            Set<String> defaultSet = new HashSet<>(Arrays.asList(defaultList));
            Set<String> set = getStrSet(id, defaultSet);
            String[] ret = set.toArray(new String[0]);
            Arrays.sort(ret);
            return ret;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return defaultList;
    }

    public static int[] getToneStylesIndex(int id) {
        String[] values = getStringArray(R.array.pref_values_mc_display);
        List<String> list = Arrays.asList(values);
        String[] selected = getToneStyles(id);
        int[] index = new int[selected.length];
        for (int i = 0; i < selected.length; i++) {
            index[i] = list.indexOf(selected[i]);
        }
        Arrays.sort(index);
        return index;
    }

    public static void putCustomLanguage(String lang) {
        if (TextUtils.isEmpty(lang)) return;
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        LinkedHashSet<String> set = store.schemes.get(store.current);
        if (set == null) set = new LinkedHashSet<>();
        if (set.contains(lang)) set.remove(lang);
        else set.add(lang);
        store.schemes.put(store.current, sanitizeCustomLanguages(set));
        saveCustomLanguageSchemeStore(store);
    }

    public static Set<String> getCustomLanguages() {
        return getFilteredCustomLanguages(getCustomLanguageSchemeScope());
    }

    public static Set<String> getAllCustomLanguages() {
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (LinkedHashSet<String> set : store.schemes.values()) {
            merged.addAll(set);
        }
        return sanitizeCustomLanguages(merged);
    }

    public static Set<String> getFilteredCustomLanguages(String scope) {
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        if (TextUtils.isEmpty(scope)) {
            LinkedHashSet<String> set = store.schemes.get(store.current);
            if (set != null) return new LinkedHashSet<>(set);
            String prefix = store.current + "－";
            LinkedHashSet<String> merged = new LinkedHashSet<>();
            for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    merged.addAll(entry.getValue());
                }
            }
            return sanitizeCustomLanguages(merged);
        } else if ("ALL".equals(scope)) {
            return getAllCustomLanguages();
        } else {
            String prefix = scope + "－";
            LinkedHashSet<String> merged = new LinkedHashSet<>();
            for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    merged.addAll(entry.getValue());
                }
            }
            return sanitizeCustomLanguages(merged);
        }
    }

    public static String getCustomLanguageSchemeScope() {
        return getStr(R.string.pref_key_custom_languages_all, "");
    }

    public static void setCustomLanguageSchemeScope(String scope) {
        putStr(R.string.pref_key_custom_languages_all, scope == null ? "" : scope);
    }

    public static String[] getCustomLanguageSchemeScopeNames() {
        // First entry: all schemes, then virtual parents
        String[] names = getCustomLanguageSchemeNames();
        LinkedHashSet<String> parents = new LinkedHashSet<>();
        for (String name : names) {
            int idx = -1;
            while ((idx = name.indexOf("－", idx + 1)) != -1) {
                parents.add(name.substring(0, idx));
            }
        }
        if (parents.isEmpty()) return new String[0];
        String[] result = new String[parents.size() + 1];
        result[0] = "ALL";
        int i = 1;
        for (String p : parents) result[i++] = p;
        java.util.Arrays.sort(result, 1, result.length, (a, b) -> a.compareTo(b));
        return result;
    }

    public static int getCustomLanguageSchemeColor(String lang) {
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        String byLabel = DB.getLanguageByLabel(lang);
        String byLanguage = DB.getLabelByLanguage(lang);

        String scope = getCustomLanguageSchemeScope();
        if (TextUtils.isEmpty(scope)) {
            String prefix = store.current + "－";
            boolean isParent = !store.schemes.containsKey(store.current);
            if (isParent) {
                for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
                    if (entry.getKey().startsWith(prefix) && matchesCustomLanguage(entry.getValue(), lang, byLabel, byLanguage)) {
                        int idx = 0;
                        for (String schemeName : store.schemes.keySet()) {
                            if (TextUtils.equals(schemeName, store.current)) return schemeIndexToColor(idx);
                            idx++;
                        }
                    }
                }
            } else {
                LinkedHashSet<String> currentSet = store.schemes.get(store.current);
                if (matchesCustomLanguage(currentSet, lang, byLabel, byLanguage)) {
                    int currentIndex = 0;
                    for (String schemeName : store.schemes.keySet()) {
                        if (TextUtils.equals(schemeName, store.current)) return schemeIndexToColor(currentIndex);
                        currentIndex++;
                    }
                }
            }
        } else if (!"ALL".equals(scope)) {
            String prefix = scope + "－";
            for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
                if (entry.getKey().startsWith(prefix) && matchesCustomLanguage(entry.getValue(), lang, byLabel, byLanguage)) {
                    int idx = 0;
                    for (String schemeName : store.schemes.keySet()) {
                        if (TextUtils.equals(schemeName, entry.getKey())) return schemeIndexToColor(idx);
                        idx++;
                    }
                }
            }
        }

        int index = 0;
        for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
            if (matchesCustomLanguage(entry.getValue(), lang, byLabel, byLanguage)) return schemeIndexToColor(index);
            index++;
        }
        return Color.GRAY;
    }

    private static boolean matchesCustomLanguage(Set<String> set, String lang, String byLabel, String byLanguage) {
        if (set == null) return false;
        return set.contains(lang)
                || (!TextUtils.isEmpty(byLabel) && set.contains(byLabel))
                || (!TextUtils.isEmpty(byLanguage) && set.contains(byLanguage));
    }

    public static int schemeIndexToColor(int index) {
        // First schemes use vivid dark colors for better readability with white text.
        final int[] vivid = {
            Color.parseColor("#B71C1C"), // red
            Color.parseColor("#0D47A1"), // blue
            Color.parseColor("#1B5E20"), // green
            Color.parseColor("#E65100"), // orange
            Color.parseColor("#4A148C"), // purple
            Color.parseColor("#006064"), // cyan
            Color.parseColor("#880E4F"), // magenta
            Color.parseColor("#3E2723")  // brown
        };
        if (index < vivid.length) return vivid[index];

        // Fallback for many schemes: high saturation + medium-low value, and skip light-yellow range.
        int i = index - vivid.length;
        float hue = (i * 47 + (i / 7) * 19) % 360;
        if (hue >= 45 && hue <= 75) hue = (hue + 40) % 360;
        float saturation = 0.78f;
        float value = 0.62f;
        return Color.HSVToColor(new float[]{hue, saturation, value});
    }

    public static String[] getCustomLanguageSchemeNames() {
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        String[] names = store.schemes.keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    public static String getCurrentCustomLanguageSchemeName() {
        return getCustomLanguageSchemeStore().current;
    }

    public static void setCurrentCustomLanguageSchemeName(String schemeName) {
        if (TextUtils.isEmpty(schemeName)) return;
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        if (!store.schemes.containsKey(schemeName)) return;
        store.current = schemeName;
        saveCustomLanguageSchemeStore(store);
    }

    public static boolean saveCurrentCustomLanguageSchemeAs(String schemeName) {
        String name = normalizeSchemeName(schemeName);
        if (TextUtils.isEmpty(name)) return false;
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        LinkedHashSet<String> currentSet = store.schemes.get(store.current);
        if (currentSet == null) currentSet = new LinkedHashSet<>();
        store.schemes.put(name, new LinkedHashSet<>(currentSet));
        store.current = name;
        saveCustomLanguageSchemeStore(store);
        return true;
    }

    public static boolean createCustomLanguageScheme(String schemeName) {
        String name = normalizeSchemeName(schemeName);
        if (TextUtils.isEmpty(name)) return false;
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        if (store.schemes.containsKey(name)) return false;
        store.schemes.put(name, new LinkedHashSet<>());
        store.current = name;
        saveCustomLanguageSchemeStore(store);
        return true;
    }

    public static boolean renameCurrentCustomLanguageScheme(String schemeName) {
        String name = normalizeSchemeName(schemeName);
        if (TextUtils.isEmpty(name)) return false;
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        String oldName = store.current;
        if (TextUtils.isEmpty(oldName) || !store.schemes.containsKey(oldName)) return false;
        if (oldName.contentEquals(name)) return true;
        if (store.schemes.containsKey(name)) return false;

        LinkedHashMap<String, LinkedHashSet<String>> renamed = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
            if (entry.getKey().contentEquals(oldName)) renamed.put(name, entry.getValue());
            else renamed.put(entry.getKey(), entry.getValue());
        }
        store.schemes.clear();
        store.schemes.putAll(renamed);
        store.current = name;
        saveCustomLanguageSchemeStore(store);
        return true;
    }

    public static boolean deleteCurrentCustomLanguageScheme() {
        CustomLanguageSchemeStore store = getCustomLanguageSchemeStore();
        if (store.schemes.size() <= 1) return false;
        store.schemes.remove(store.current);
        if (store.schemes.isEmpty()) return false;
        store.current = store.schemes.keySet().iterator().next();
        saveCustomLanguageSchemeStore(store);
        return true;
    }

    public static String getCustomLanguageSummary()  {
        Set<String> set = getCustomLanguages();
        return getString(R.string.select_custom_language_summary, set.size());
    }

    private static CustomLanguageSchemeStore getCustomLanguageSchemeStore() {
        CustomLanguageSchemeStore store = new CustomLanguageSchemeStore();
        String json = getStr(R.string.pref_key_custom_language_schemes, "");
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    if (object == null) continue;
                    String name = normalizeSchemeName(object.optString("name", ""));
                    if (TextUtils.isEmpty(name)) continue;
                    JSONArray languages = object.optJSONArray("languages");
                    LinkedHashSet<String> set = new LinkedHashSet<>();
                    if (languages != null) {
                        for (int j = 0; j < languages.length(); j++) {
                            String lang = languages.optString(j, "");
                            if (!TextUtils.isEmpty(lang)) set.add(lang);
                        }
                    }
                    store.schemes.put(name, sanitizeCustomLanguages(set));
                }
            } catch (Exception ignored) {
            }
        }

        if (store.schemes.isEmpty()) {
            LinkedHashSet<String> legacy = sanitizeCustomLanguages(getStrSet(R.string.pref_key_custom_languages));
            store.schemes.put(getString(R.string.custom_scheme_default_name), legacy);
        }

        String current = getStr(R.string.pref_key_custom_language_scheme_current, "");
        if (TextUtils.isEmpty(current) || !store.schemes.containsKey(current)) {
            current = store.schemes.keySet().iterator().next();
        }
        store.current = current;
        return store;
    }

    private static void saveCustomLanguageSchemeStore(CustomLanguageSchemeStore store) {
        if (store.schemes.isEmpty()) {
            store.schemes.put(getString(R.string.custom_scheme_default_name), new LinkedHashSet<>());
        }
        if (TextUtils.isEmpty(store.current) || !store.schemes.containsKey(store.current)) {
            store.current = store.schemes.keySet().iterator().next();
        }
        JSONArray array = new JSONArray();
        for (Map.Entry<String, LinkedHashSet<String>> entry : store.schemes.entrySet()) {
            JSONObject object = new JSONObject();
            try {
                object.put("name", entry.getKey());
                object.put("languages", new JSONArray(new ArrayList<>(entry.getValue())));
                array.put(object);
            } catch (Exception ignored) {
            }
        }
        SharedPreferences.Editor editor = get().edit();
        editor.putString(getString(R.string.pref_key_custom_language_schemes), array.toString());
        editor.putString(getString(R.string.pref_key_custom_language_scheme_current), store.current);
        editor.putStringSet(getString(R.string.pref_key_custom_languages), new HashSet<>(store.schemes.get(store.current)));
        editor.apply();
    }

    private static String normalizeSchemeName(String name) {
        if (TextUtils.isEmpty(name)) return "";
        return name.trim().replaceAll("[\\-/／ 　]", "－");
    }

    private static LinkedHashSet<String> sanitizeCustomLanguages(Set<String> rawLanguages) {
        LinkedHashSet<String> input = new LinkedHashSet<>(rawLanguages);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        String[] languages = DB.getLanguages();
        if (languages == null) return result;
        for (String lang : languages) {
            if (input.contains(lang)) {
                result.add(lang);
            }
        }
        return result;
    }

    public static String getTitle() {
        return getStr(R.string.pref_key_custom_title, getString(R.string.app_name));
    }
}
