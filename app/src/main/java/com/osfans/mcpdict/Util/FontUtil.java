package com.osfans.mcpdict.Util;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.text.TextUtils;
import android.widget.TextView;

import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Utils;

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

    private static Typeface getHanTypeface() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        try {
            if (useFontTone()) {
                if (tfHanTone == null) {
                    Typeface.CustomFallbackBuilder builder = new Typeface.CustomFallbackBuilder(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.tone).build()).build()
                    );
                    if (fontExFirst()) builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.p0).build()).build()
                    );
                    builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.p2).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.p3).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.pua).build()).build()
                    );
                    builder.setSystemFallback(getDefaultFont());
                    tfHanTone = builder.build();
                }
                return tfHanTone;
            } else {
                if (tfHan == null) {
                    Typeface.CustomFallbackBuilder builder = new Typeface.CustomFallbackBuilder(
                            new FontFamily.Builder(new Font.Builder(getResources(), fontExFirst() ? R.font.p0 : R.font.ipa).build()).build()
                    );
                    builder.addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.p2).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.p3).build()).build()
                    ).addCustomFallback(
                            new FontFamily.Builder(new Font.Builder(getResources(), R.font.pua).build()).build()
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

    static Typeface getDictTypeface() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return null;
        if (!enableFontExt()) return getIPATypeface();
        return getHanTypeface();
    }

    public static Typeface getIPATypeface() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        if (useFontTone()) {
            if (tfIPATone == null) {
                tfIPATone = getResources().getFont(R.font.tone);
            }
            return tfIPATone;
        }
        if (tfIPA == null) {
            tfIPA = getResources().getFont(R.font.ipa);
        }
        return tfIPA;
    }

    private static Resources getResources() {
        return Utils.getContext().getResources();
    }

    public static int getFontFormat() {
        return Pref.getStrAsInt(R.string.pref_key_font, 0);
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
