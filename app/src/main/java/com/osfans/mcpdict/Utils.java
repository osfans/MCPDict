package com.osfans.mcpdict;

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
import androidx.core.text.HtmlCompat;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class Utils extends Application {
    private static Utils mApp;
    private static Typeface tf, tfIPA;

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

    public static int getToneStyle(int id) {
        int value = 0;
        if (id == R.string.pref_key_tone_display) value = 1;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        try {
            return Integer.parseInt(Objects.requireNonNull(sp.getString(mApp.getString(id), String.valueOf(value))));
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return value;
    }

    private static final Displayer gyDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.MiddleChinese.display(s, getToneStyle(R.string.pref_key_mc_display));
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
        switch (lang) {
            case DB.SG:
                cs = getRichText(string.replace(",", "  "));
                break;
            case DB.BA:
                cs = baDisplayer.display(string);
                break;
            case DB.GY:
                cs = getRichText(gyDisplayer.display(string));
                break;
            case DB.CMN:
                cs = getRichText(cmnDisplayer.display(string));
                break;
            case DB.HK:
                cs = hkDisplayer.display(string);
                break;
            case DB.TW:
                cs = getRichText(twDisplayer.display(string));
                break;
            case DB.KOR:
                cs = korDisplayer.display(string);
                break;
            case DB.VI:
                cs = viDisplayer.display(string);
                break;
            case DB.JA_GO:
            case DB.JA_KAN:
            case DB.JA_OTHER:
                cs = getRichText(jaDisplayer.display(string));
                break;
            default:
                cs = getRichText(toneDisplayer.display(string, lang));
                break;
        }
        return cs;
    }

    public static CharSequence formatPopUp(String hz, int i, String s) {
        if (TextUtils.isEmpty(s)) return "";
        if (i == COL_SW) s = s.replace("{", "<small>").replace("}", "</small>");
        else if (i == COL_KX) s = s.replaceFirst("^(.*?)(\\d+).(\\d+)", "$1<a href=https://kangxizidian.com/kxhans/" + hz + ">第$2頁第$3字</a>");
        else if (i == COL_HD) s = s.replaceFirst("(\\d+).(\\d+)", "【汉語大字典】<a href=https://www.homeinmists.com/hd/png/$1.png>第$1頁</a>第$2字");
        String[] fs = (s + "\n").split("\n", 2);
        String text = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br/>"));
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    public static Typeface getDictTypeFace() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            if (tf == null) {
                tf = new Typeface.CustomFallbackBuilder(
                        new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.ipa).build()).build()
                ).addCustomFallback(
                        new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.hanbcde).build()).build()
                ).addCustomFallback(
                        new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.hanfg).build()).build()
                ).setSystemFallback("sans-serif")
                        .build();
            }
            return tf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Typeface getIPA() {
        if (tfIPA == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tfIPA = mApp.getResources().getFont(R.font.ipa);
        }
        return tfIPA;
    }

    public static float getScale() {
        return mApp.getResources().getDisplayMetrics().density;
    }

    public static int getDisplayFormat() {
        int value = 1;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
            return Integer.parseInt(Objects.requireNonNull(sp.getString(mApp.getString(R.string.pref_key_format), String.valueOf(value))));
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return value;
    }

    public static boolean enableFontExt() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        return sp.getBoolean(mApp.getString(R.string.pref_key_font_ext), true);
    }

    public static String getTitle() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        return sp.getString(mApp.getString(R.string.pref_key_custom_title), mApp.getString(R.string.app_name));
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
        new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_NoActionBar)
                .setView(webView)
                .show();
    }

    public static void showDict(Context context, CharSequence s) {
        TextView tv = new TextView(context);
        tv.setPadding(24, 24, 24, 24);
        if (Utils.enableFontExt()) tv.setTypeface(Utils.getDictTypeFace());
        tv.setTextIsSelectable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(s);
        new AlertDialog.Builder(context).setView(tv).show();
    }

    public static void putStr(int key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        sp.edit().putString(mApp.getString(key), value).apply();
    }

    public static String getStr(int key, String defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        return sp.getString(mApp.getString(key), defaultValue);
    }

    public static Set<String> getStrSet(int key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        return sp.getStringSet(mApp.getString(key), null);
    }

    public static String getStr(int key) {
        return getStr(key, "");
    }

    public static void putInput(String value) {
        putStr(R.string.pref_key_input, value);
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

    public static void setLocale() {
        String locale = getStr(R.string.pref_key_locale);
        if (TextUtils.isEmpty(locale)) locale = "ko";
        Locale.setDefault(Locale.forLanguageTag(locale));
    }

    public static Context getContext() {
        return mApp;
    }
}
