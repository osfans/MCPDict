package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Locale;

class Utils {

    public static void putString(Context context, int key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(context.getString(key), value).apply();
    }

    public static String getString(Context context, int key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getResources().getString(key), "");
    }

    public static void putInput(Context context, String value) {
        putString(context, R.string.pref_key_input, value);
    }

    public static String getInput(Context context) {
        return getString(context, R.string.pref_key_input);
    }

    public static void putLanguage(Context context, String value) {
        putString(context, R.string.pref_key_language, value);
    }

    public static String getLanguage(Context context) {
        return getString(context, R.string.pref_key_language);
    }

    public static void setLocale(Context context) {
        String locale = getString(context, R.string.pref_key_locale);
        if (TextUtils.isEmpty(locale)) locale = "ko";
        Locale.setDefault(Locale.forLanguageTag(locale));
    }
}
