package com.osfans.mcpdict.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        putStrSet(R.string.pref_key_custom_languages, lang);
    }

    public static Set<String> getCustomLanguages() {
        int key = R.string.pref_key_custom_languages;
        Set<String> customs = getStrSet(key);
        if (customs.isEmpty()) return customs;
        Set<String> set = new HashSet<>();
        String[] languages = DB.getLanguages();
        if (languages == null) return set;
        for (String lang: languages) {
            if (customs.contains(lang)) {
                set.add(lang);
            }
        }
        if (set.size() != customs.size()) {
            get().edit().putStringSet(getString(key), set).apply();
        }
        return set;
    }

    public static String getCustomLanguageSummary()  {
        Set<String> set = getCustomLanguages();
        return getString(R.string.select_custom_language_summary, set.size(), String.join("、", set));
    }

    public static String getTitle() {
        return getStr(R.string.pref_key_custom_title, getString(R.string.app_name));
    }
}
