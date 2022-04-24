package com.osfans.mcpdict;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Objects;

public class DictApp extends Application {
    private static DictApp mApp;

    public DictApp() {
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
        public String displayOne(String s) {return Orthography.MiddleChinese.display(s, getToneStyle(R.string.pref_key_mc_display));}
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
            case DB.JA_TOU:
            case DB.JA_KWAN:
            case DB.JA_OTHER:
                cs = getRichText(jaDisplayer.display(string));
                break;
            default:
                cs = getRichText(toneDisplayer.display(string, lang));
                break;
        }
        return cs;
    }

    public static CharSequence formatPassage(String hz, String js) {
        String[] fs = (js+"\n").split("\n", 2);
        String s = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br/>"));
        return getRichText(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void customTypeFace(TextView view) {
        try {
            Typeface tf = new Typeface.CustomFallbackBuilder(
                    new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.ipa).build()).build()
            ).addCustomFallback(
                    new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.hanbcde).build()).build()
            ).addCustomFallback(
                    new FontFamily.Builder(new Font.Builder(mApp.getResources(), R.font.hanfg).build()).build()
            ).setSystemFallback("sans-serif").build();
            view.setTypeface(tf);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
