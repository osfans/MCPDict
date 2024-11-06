package com.osfans.mcpdict;

import static com.osfans.mcpdict.DB.COL_GYHZ;
import static com.osfans.mcpdict.DB.COL_HD;
import static com.osfans.mcpdict.DB.COL_KX;
import static com.osfans.mcpdict.DB.COL_SW;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.text.HtmlCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Utils extends Application {
    private static Utils mApp;
    private static Typeface tfHan, tfHanTone, tfIPA, tfIPATone;

    public Utils() {
        mApp = this;
    }

    public static CharSequence getRichText(String richTextString) {
        String s = richTextString
                .replace("\n", "<br/>")
                .replaceAll("\\*(.+?)\\*", "<b>$1</b>")
                .replaceAll("\\|(.+?)\\|", "<span style='color: #808080;'>$1</span>");
        int i = getDisplayFormat();
        if (i == 1) {
            s = s.replace("{", "<small><small>").replace("}", "</small></small>");
        } else if (i == 2) {
            s = s.replace("{", "<div class=desc>").replace("}", "</div>");
        }
        return s;
    }

    public static String getRawText(String s) {
        if (TextUtils.isEmpty(s)) return "";
        return s.replaceAll("[|*\\[\\]]", "").replaceAll("\\{.*?\\}", "");
    }

    public static SharedPreferences getPreference() {
        return mApp.getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(mApp), Context.MODE_PRIVATE);
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

    public static String[] getStringArray(int id) {
        return mApp.getResources().getStringArray(id);
    }

    private static final Displayer gyDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.MiddleChinese.display(s, getToneStylesIndex(R.string.pref_key_mc_display));
        }

        public boolean isIPA(char c) {
            return c != '{';
        }
    };

    private static final Displayer zyyyDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.ZhongyuanYinyun.display(s, getToneStyles(R.string.pref_key_zyyy_display));
        }

        public boolean isIPA(char c) {
            return super.isIPA(c) || c == '/' || c == '(' || c == ')';
        }
    };

    private static final Displayer dgyDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Dongganyu.display(s, getToneStyles(R.string.pref_key_dgy_display));
        }

        private static boolean isCyrillic(char ch) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            return block == Character.UnicodeBlock.CYRILLIC
                || block == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY;
        }

        public boolean isIPA(char c) {
            return super.isIPA(c) || isCyrillic(c) || c == '/' || c == '(' || c == ')';
        }
    };

    private static final Displayer cmnDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Mandarin.display(s, getToneStyle(R.string.pref_key_mandarin_display));
        }
    };

    private static final Displayer hkDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Cantonese.display(s, getToneStyle(R.string.pref_key_cantonese_romanization));
        }
    };

    private static final Displayer twDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Minnan.display(s, getToneStyle(R.string.pref_key_minnan_display));
        }
    };

    private static final Displayer baDisplayer = new Displayer() {
        public String displayOne(String s) {
            return s;
        }
    };

    private static final Displayer toneDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tones.display(s, getLang());
        }
    };

    private static final Displayer korDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Korean.display(s, getToneStyle(R.string.pref_key_korean_display));
        }
    };

    private static final Displayer viDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Vietnamese.display(s, getToneStyle(R.string.pref_key_vietnamese_tone_position));
        }
    };

    private static final Displayer jaDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Japanese.display(s, getToneStyle(R.string.pref_key_japanese_display));
        }
    };


    public static CharSequence formatIPA(String lang, String string) {
        CharSequence cs;
        if (TextUtils.isEmpty(string)) return "";
        cs = switch (lang) {
            case DB.SG -> getRichText(string.replace(",", "  "));
            case DB.BA -> baDisplayer.display(string);
            case DB.GY -> getRichText(gyDisplayer.display(string));
            case DB.ZYYY -> getRichText(zyyyDisplayer.display(string));
            case DB.DGY -> getRichText(dgyDisplayer.display(string));
            case DB.CMN -> getRichText(cmnDisplayer.display(string));
            case DB.HK -> hkDisplayer.display(string);
            case DB.TW -> getRichText(twDisplayer.display(string));
            case DB.KOR -> korDisplayer.display(string);
            case DB.VI -> viDisplayer.display(string);
            case DB.JA_GO, DB.JA_KAN, DB.JA_OTHER -> getRichText(jaDisplayer.display(string));
            default -> getRichText(toneDisplayer.display(string, lang));
        };
        return cs;
    }

    public static CharSequence formatUnknownIPA(String lang, String string) {
        StringBuilder sb = new StringBuilder();
        String s = string.replace("}\t", "}\n");
        String input = getInput();
        if (Orthography.HZ.isUnknown(input)) sb.append(s);
        else {
            if (input.startsWith(":") || input.startsWith("：")) {
                input = input.substring(1);
            }
            String[] inputs = input.split("[, ]+");
            for (String i : s.split("\n")) {
                for (String j: inputs) {
                    if (i.contains(j)) {
                        sb.append(i).append("\n");
                        break;
                    }
                }
            }
        }
        return formatIPA(lang, sb.toString());
    }

    public static CharSequence formatPopUp(String hz, int i, String s) {
        if (TextUtils.isEmpty(s)) return "";
        if (i == COL_SW) s = s.replace("{", "<small>").replace("}", "</small>");
        else if (i == COL_KX) s = s.replaceFirst("^(.*?)(\\d+).(\\d+)", "$1<a href=https://kangxizidian.com/kxhans/" + hz + ">第$2頁第$3字</a>");
        else if (i == COL_GYHZ) s = mApp.getString(R.string.book_format, DB.getLanguageByLabel(DB.getColumn(i))) + s.replaceFirst("(\\d+).(\\d+)", "第$1頁第$2字");
        else if (i == COL_HD) s = mApp.getString(R.string.book_format, DB.getLanguageByLabel(DB.getColumn(i))) + s.replaceFirst("(\\d+).(\\d+)", "<a href=https://homeinmists.ilotus.org/hd/png/$1.png>第$1頁</a>第$2字").replace("lv", "lü").replace("nv", "nü");
        String[] fs = (s + "\n").split("\n", 2);
        String text = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br/>"));
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    public static boolean useFontTone() {
        return getToneStyle(R.string.pref_key_tone_display) == 5;
    }

    public static void refreshTypeface() {
        tfHan = null;
        tfHanTone = null;
        tfIPA = null;
        tfIPATone = null;
    }

    private static Typeface getHanTypeface() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            if (useFontTone()) {
                if (tfHanTone == null) {
                    Typeface.CustomFallbackBuilder builder = new Typeface.CustomFallbackBuilder(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.tone).build()).build()
                    );
                    if (fontExFirst()) builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.p0).build()).build()
                    );
                    builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.p2).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.p3).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.pua).build()).build()
                    );
                    builder.setSystemFallback(getDefaultFont());
                    tfHanTone = builder.build();
                }
                return tfHanTone;
            } else {
                if (tfHan == null) {
                    Typeface.CustomFallbackBuilder builder = new Typeface.CustomFallbackBuilder(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), fontExFirst() ? R.font.p0 : R.font.ipa).build()).build()
                    );
                    builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.p2).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.p3).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.pua).build()).build()
                    );
                    builder.setSystemFallback(getDefaultFont());
                    tfHan = builder.build();
                }
                return tfHan;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private static Typeface getDictTypeface() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        if (!enableFontExt()) return getIPATypeface();
        return getHanTypeface();
    }

    public static Typeface getIPATypeface() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        if (useFontTone()) {
            if (tfIPATone == null) {
                tfIPATone = mApp.getResources().getFont(R.font.tone);
            }
            return tfIPATone;
        }
        if (tfIPA == null) {
            tfIPA = mApp.getResources().getFont(R.font.ipa);
        }
        return tfIPA;
    }

    public static float getScale() {
        return mApp.getResources().getDisplayMetrics().density;
    }

    public static int getDisplayFormat() {
        return getStrAsInt(R.string.pref_key_format, 1);
    }

    public static int getFontFormat() {
        return getStrAsInt(R.string.pref_key_font, 0);
    }

    public static boolean useSerif() {
        return getFontFormat() == 1;
    }

    public static String getDefaultFont() {
        return useSerif() ? "serif" : "sans";
    }

    public static boolean fontExFirst() {
        return getFontFormat() == 2;
    }

    public static boolean enableFontExt() {
        return getFontFormat() != 3;
    }

    private static int getAppTheme() {
        return switch (getFontFormat()) {
            case 0 -> R.style.AppThemeSans;
            case 1, 2 -> R.style.AppThemeSerif;
            default -> R.style.AppTheme;
        };
    }

    public static void setActivityTheme(AppCompatActivity app) {
        app.setTheme(getAppTheme());
    }

    public static String getTitle() {
        return getStr(R.string.pref_key_custom_title, mApp.getString(R.string.app_name));
    }

    public static void info(Context context, String lang) {
        MyWebView webView = new MyWebView(context, null);
        String sb = "<style>\n" +
                "  @font-face {\n" +
                "      font-family: ipa;\n" +
                "      src: url(\"file:///android_res/font/ipa.ttf\")\n" +
                "  }\n" +
                "  body {font-size: 16px}\n" +
                "  h1 {font-size: 24px; color: #9D261D}\n" +
                "  h2 {font-size: 20px; color: #000080; text-indent: 10px}\n" +
                " </style>" +
                DB.getIntroText(DB.getLanguageByLabel(lang));
        webView.loadDataWithBaseURL(null, sb, "text/html", "utf-8", null);

        new AlertDialog.Builder(context)
                .setView(webView)
                .show();
    }

    public static void about(Context context) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.about)
                .setMessage(HtmlCompat.fromHtml(context.getString(R.string.about_message, BuildConfig.VERSION_NAME), HtmlCompat.FROM_HTML_MODE_COMPACT))
                .setPositiveButton(R.string.ok, null)
                .show();
        TextView messageText = dialog.findViewById(android.R.id.message);
        assert messageText != null;
        messageText.setGravity(Gravity.CENTER);
        messageText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void help(Context context) {
        MyWebView webView = new MyWebView(context, null);
        webView.loadUrl("file:///android_asset/help/index.htm");
        new AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar)
                .setView(webView)
                .show();
    }

    public static String getFontFeatureSettings() {
        String locale = getStr(R.string.pref_key_locale);
        if (!TextUtils.isEmpty(locale) && locale.contentEquals("zh-cn")) return "";
        return "trad";
    }

    public static void setTypeface(TextView tv) {
        tv.setTypeface(getDictTypeface());
        tv.setFontFeatureSettings(getFontFeatureSettings());
        tv.setElegantTextHeight(true);
    }

    public static void showDict(Context context, int lang, CharSequence s) {
        TextView tv = new TextView(context);
        tv.setPadding(24, 24, 24, 24);
        setTypeface(tv);
        if (lang == COL_HD) tv.setFontFeatureSettings("ss01"); // zh-cn and pinyin
        tv.setTextIsSelectable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(s);
        new AlertDialog.Builder(context).setView(tv).show();
    }

    public static void putBool(int key, boolean value) {
         getPreference().edit().putBoolean(mApp.getString(key), value).apply();
    }

    public static boolean getBool(int key, boolean defaultValue) {
        return getPreference().getBoolean(mApp.getString(key), defaultValue);
    }

    public static void putStr(int key, String value) {
        getPreference().edit().putString(mApp.getString(key), value).apply();
    }

    public static String getStr(int key, String defaultValue) {
        return getPreference().getString(mApp.getString(key), defaultValue);
    }

    public static Set<String> getStrSet(int key) {
        return getStrSet(key, null);
    }

    public static Set<String> getStrSet(int key, Set<String> defaultValue) {
        return getPreference().getStringSet(mApp.getString(key), defaultValue);
    }

    public static String getStr(int key) {
        return getStr(key, "");
    }

    public static void putInput(String value) {
        putStr(R.string.pref_key_input, value);
    }

    public static String getShape() {
        String shape = getStr(R.string.pref_key_shape);
        if (shape.contentEquals(mApp.getString(R.string.hz_shapes))) shape = "";
        return shape;
    }

    public static void putShape(String value) {
        putStr(R.string.pref_key_shape, value);
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
        return DB.getLabelByLanguage(language);
    }

    public static int getInt(int key, int defaultValue) {
        return getPreference().getInt(mApp.getString(key), defaultValue);
    }

    public static void putInt(int key, int value) {
        getPreference().edit().putInt(mApp.getString(key), value).apply();
    }

    public static void setLocale() {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("zh-TW");
        AppCompatDelegate.setApplicationLocales(appLocale);
        String locale = getStr(R.string.pref_key_locale);
        if (TextUtils.isEmpty(locale)) locale = "ko";
        Locale.setDefault(Locale.forLanguageTag(locale));
    }

    public static Context getContext() {
        return mApp;
    }
}
