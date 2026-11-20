package com.osfans.mcpdict.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.custom.CustomSchemes;
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

    // Matrix table orientation preference.
    // true  = language labels are in the frozen left column
    // false = Han characters are in the frozen left column
    private static final String PREF_TABLE_LANGUAGE_LEFT = "table_language_left";

    public static boolean isTableLanguageLeft() {
        return get().getBoolean(PREF_TABLE_LANGUAGE_LEFT, true);
    }

    public static void setTableLanguageLeft(boolean languageLeft) {
        // This is a tiny user-action preference. commit() keeps menu state and
        // the live ResultFragment state immediately consistent.
        get().edit().putBoolean(PREF_TABLE_LANGUAGE_LEFT, languageLeft).commit();
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


    public static void putCustomLanguage(String lang) { CustomSchemes.toggle(lang); }
    public static Set<String> getCustomLanguages() { return CustomSchemes.languages(); }
    public static Set<String> getAllCustomLanguages() {
        Set<String> result = new LinkedHashSet<>();
        for (CustomSchemes.Scheme scheme : CustomSchemes.load())
            for (CustomSchemes.Entry row : scheme.entries) result.add(row.language());
        return result;
    }
    public static String getCustomLanguageSchemeScope() { return CustomSchemes.scope(); }
    public static void setCustomLanguageSchemeScope(String scope) { CustomSchemes.setScope(scope); }
    public static String[] getCustomLanguageSchemeScopeNames() { return CustomSchemes.groups().toArray(new String[0]); }
    public static int getCustomLanguageSchemeColor(String lang) { return CustomSchemes.firstColor(lang); }

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


    public static String[] getCustomLanguageSchemeNames() { return CustomSchemes.names(); }
    public static String getCurrentCustomLanguageSchemeName() { return CustomSchemes.currentName(); }
    public static void setCurrentCustomLanguageSchemeName(String name) { CustomSchemes.select(name); }
    public static boolean createCustomLanguageScheme(String name) { return CustomSchemes.create(name); }
    public static boolean renameCurrentCustomLanguageScheme(String name) { return CustomSchemes.rename(name); }
    public static boolean deleteCurrentCustomLanguageScheme() { return CustomSchemes.delete(); }
    public static boolean saveCurrentCustomLanguageSchemeAs(String name) {
        CustomSchemes.Scheme old = CustomSchemes.current();
        if (!CustomSchemes.create(name)) return false;
        CustomSchemes.Scheme copy = CustomSchemes.current();
        for (CustomSchemes.Entry row : old.entries)
            copy.entries.add(new CustomSchemes.Entry(row.language(), row.group(), row.color()));
        copy.colors.putAll(old.colors);
        CustomSchemes.replaceCurrent(copy);
        return true;
    }
    public static String getCustomLanguageSummary() { return CustomSchemes.summary(); }

    public static String getTitle() {
        return getStr(R.string.pref_key_custom_title, getString(R.string.app_name));
    }
}
