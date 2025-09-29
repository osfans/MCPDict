package com.osfans.mcpdict.Util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.SystemFonts;
import android.os.Build;
import android.text.TextUtils;
import android.widget.TextView;

import com.osfans.mcpdict.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FontUtil {
    static Typeface tfHan;
    static Typeface tfHanTone;
    static Typeface tfIPA;
    static Typeface tfIPATone;

    public static boolean useFontTone() {
        return Pref.getToneStyle(R.string.pref_key_tone_display) == 5;
    }

    public static void refreshTypeface() {
        tfHan = null;
        tfHanTone = null;
        tfIPA = null;
        tfIPATone = null;
    }

    public static List<String> getFontPackages() {
        List<String> l = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return l;
        PackageManager pm = App.getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info: packages) {
            if (info.packageName.startsWith("com.osfans.font.")) l.add(info.packageName);
        }
        return l;
    }

    public static List<String> getFontNames(List<String> packages, boolean isFontValues) {
        List<String> l = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return l;
        String name = isFontValues ? "fonts" : "names";
        try {
            for (String packageName: packages) {
                Context context = App.getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
                Resources res = context.getResources();
                int id = res.getIdentifier(name, "array", packageName);
                String[] array = res.getStringArray(id);
                if (array.length == 0) continue;
                if (isFontValues) {
                    for (String a: array) {
                        l.add(String.format("%s:%s", a, packageName));
                    }
                } else l.addAll(Arrays.asList(array));
            }
        } catch (Exception ignore) {
        }
        return l;
    }

    private static FontFamily getSystemFamily(String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null;
        Locale locale = Locale.getDefault();
        String defaultName = name.toLowerCase();
        FontFamily family = null;
        for (Font f: SystemFonts.getAvailableFonts()) {
            Locale l = f.getLocaleList().getFirstMatch(new String[]{locale.toLanguageTag()});
            if (l == null || TextUtils.isEmpty(l.toString())) continue;
            if (!l.toLanguageTag().contentEquals(locale.toLanguageTag())) continue;
            File file = f.getFile();
            if (file == null) continue;
            String path = file.getAbsolutePath();
            if (path.contains("CJK") && path.toLowerCase().contains(defaultName)) {
                family = new FontFamily.Builder(f).build();
                break;
            }
        }
        return family;
    }

    private static FontFamily getUnicode17Family(String font) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            String packageName = "com.osfans.font.unicode17";
            Context context = App.getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            Resources res = context.getResources();
            int resId = res.getIdentifier(font, "font", packageName);
            return new FontFamily.Builder(new Font.Builder(res, resId).build()).build();
        } catch (Exception ignore) {
        }
        return null;
    }

    private static Typeface getTypeface(boolean useFontTone) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            String ids = getFontFamily();
            if (!ids.contains(":")) return null;
            String[] a = ids.split(":");
            String packageName = a[1];
            Context context = App.getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            Resources res = context.getResources();
            String[] fonts = a[0].split(",");
            Typeface.CustomFallbackBuilder builder = null;

            FontFamily familyIPA = new FontFamily.Builder(new Font.Builder(getResources(), useFontTone ? R.font.tone : getIpaFontId()).build()).build();
            for (String font: fonts) {
                FontFamily family;
                if (font.contentEquals("sans") || font.contentEquals("serif")) {
                    if (fonts[fonts.length - 1].contentEquals(font)) {
                        if (fonts.length == 2 && builder != null) {
                            if (font.contentEquals("sans")) {
                                family = getUnicode17Family("plangothicp1");
                                if (family == null) continue;
                                builder.addCustomFallback(family);
                                family = getUnicode17Family("plangothicp2");
                            } else {
                                family = getUnicode17Family("wenjinminchop2");
                                if (family == null) continue;
                                builder.addCustomFallback(family);
                                family = getUnicode17Family("wenjinminchop3");
                            }
                        } else continue;
                    } else {
                        family = getSystemFamily(font);
                    }
                } else if (font.contentEquals("ipa")) {
                    family = familyIPA;
                } else {
                    int resId = res.getIdentifier(font, "font", packageName);
                    family = new FontFamily.Builder(new Font.Builder(res, resId).build()).build();
                }
                if (family == null) continue;
                if (builder == null) {
                    builder = new Typeface.CustomFallbackBuilder(family);
                } else {
                    builder.addCustomFallback(family);
                }
            }
            if (builder == null) builder = new Typeface.CustomFallbackBuilder(familyIPA);
            else builder.addCustomFallback(familyIPA);
            builder.addCustomFallback(
                        new FontFamily.Builder(new Font.Builder(getResources(), R.font.charis).build()).build())
                    .addCustomFallback(
                        new FontFamily.Builder(new Font.Builder(getResources(), R.font.nyushu).build()).build());
            if (!getSystemFallbackFont().contains("default")) builder.setSystemFallback(getSystemFallbackFont());
            return builder.build();
        } catch (Exception ignore) {
        }
        return null;
    }

    private static Typeface getHanTypeface() {
        if (useFontTone()) {
            if (tfHanTone == null) tfHanTone = getTypeface(true);
            return tfHanTone;
        } else {
            if (tfHan == null) tfHan = getTypeface(false);
            return tfHan;
        }
    }

    public static Typeface getDictTypeface() {
        if (!enableFontExt()) return getIPATypeface();
        return getHanTypeface();
    }

    private static Typeface getLocalTypeface() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            boolean useFontTone = useFontTone();
            Typeface.CustomFallbackBuilder builder;
            if (useFontTone) {
                builder = new Typeface.CustomFallbackBuilder(
                        new FontFamily.Builder(new Font.Builder(getResources(), R.font.tone).build()).build());
                builder.addCustomFallback(
                        new FontFamily.Builder(new Font.Builder(getResources(), getIpaFontId()).build()).build()
                );
            } else {
                builder = new Typeface.CustomFallbackBuilder(
                        new FontFamily.Builder(new Font.Builder(getResources(), getIpaFontId()).build()).build());
            }
            builder.addCustomFallback(
                    new FontFamily.Builder(new Font.Builder(getResources(), R.font.nyushu).build()).build()
            );
            if (!getSystemFallbackFont().contains("default")) builder.setSystemFallback(getSystemFallbackFont());
            return builder.build();
        } catch (Exception ignore) {
        }
        return null;
    }

    public static Typeface getIPATypeface() {
        if (useFontTone()) {
            if (tfIPATone == null) tfIPATone = getLocalTypeface();
            return tfIPATone;
        }
        if (tfIPA == null) tfIPA = getLocalTypeface();
        return tfIPA;
    }

    private static Resources getResources() {
        return App.getContext().getResources();
    }

    public static String getFontFamily() {
        return Pref.getStr(R.string.pref_key_font);
    }

    private static boolean isSerif() {
        return getSystemFallbackFont().contains("serif");
    }

    public static int getIpaFontId() {
        return isSerif() ? R.font.charis: R.font.voces;
    }

    public static String getSystemFallbackFont() {
        String family = getFontFamily();
        if (family.contains("serif")) return "serif";
        if (family.contains("sans")) return "sans";
        return "default";
    }

    public static boolean enableFontExt() {
        return getFontFamily().contains(":");
    }

    public static String getFontFeatureSettings() {
        String locale = Pref.getStr(R.string.pref_key_locale);
        if (!TextUtils.isEmpty(locale) && locale.contentEquals("zh-cn")) return "";
        return "ss12";
    }

    public static void setTypeface(TextView tv) {
        tv.setTypeface(getDictTypeface());
        tv.setFontFeatureSettings(getFontFeatureSettings());
        tv.setElegantTextHeight(true);
    }

}
