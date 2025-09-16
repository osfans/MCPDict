package com.osfans.mcpdict.Util;

import static com.osfans.mcpdict.DB.COL_GYHZ;
import static com.osfans.mcpdict.DB.COL_HD;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.osfans.mcpdict.BuildConfig;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.UI.WebView;

import java.util.Locale;

public class App extends Application {
    private static App mApp;

    public App() {
        mApp = this;
    }

    public static Context getContext() {
        return mApp;
    }

    public static void info(Context context, String lang) {
        WebView webView = new WebView(context, null);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>\n")
          .append("                  @font-face {\n")
          .append("                    font-family: ipa;\n")
          .append("                    src: url('file:///android_res/font/ipa.ttf');\n")
          .append("                  }\n")
          .append("                  @font-face {\n")
          .append("                    font-family: p0;\n")
          .append("                    src: url('file:///android_res/font/p0.otf') format('opentype');\n")
          .append("                  }\n")
          .append("                  @font-face {\n")
          .append("                    font-family: p2;\n")
          .append("                    src: url('file:///android_res/font/p2.otf') format('opentype');\n")
          .append("                  }\n")
          .append("                  @font-face {\n")
          .append("                    font-family: p3;\n")
          .append("                    src: url('file:///android_res/font/p3.otf') format('opentype');\n")
          .append("                  }\n")
          .append("  h1 {font-size: 1.8em; color: #9D261D}\n")
          .append("  h2 {font-size: 1.2em; color: #000080;}\n")
          .append("  body {font-family: ipa, ");
        if (FontUtil.fontWenJinFirst()) {
            sb.append(String.format("p0, p2, p3, %s;}", FontUtil.getDefaultFont()));
        } else {
            sb.append(String.format("%s, p2, p3;}", FontUtil.getDefaultFont()));
        }
        sb.append("</style></head><body>");
        sb.append(DB.getIntroText(DB.getLanguageByLabel(lang)));
        webView.loadDataWithBaseURL(null, sb.toString(), "text/html; charset=utf-8", "utf-8", null);

        new AlertDialog.Builder(context)
                .setView(webView)
                .show();
    }

    public static void about(Context context) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.about)
                .setMessage(HtmlCompat.fromHtml(context.getString(R.string.about_message, Pref.getTitle(), BuildConfig.VERSION_NAME), HtmlCompat.FROM_HTML_MODE_COMPACT))
                .setPositiveButton(R.string.ok, null)
                .show();
        TextView messageText = dialog.findViewById(android.R.id.message);
        assert messageText != null;
        messageText.setGravity(Gravity.CENTER);
        messageText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void help(Context context) {
        WebView webView = new WebView(context, null);
        webView.loadUrl("file:///android_asset/help/index.htm");
        new AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar)
                .setView(webView)
                .show();
    }

    public static void showDict(Context context, int lang, CharSequence s) {
        TextView tv = new TextView(context);
        tv.setPadding(24, 24, 24, 24);
        FontUtil.setTypeface(tv);
        if (lang == COL_HD) tv.setFontFeatureSettings("ss01"); // zh-cn and pinyin
        else if (lang == COL_GYHZ) tv.setFontFeatureSettings(String.format("'ss01', '%s'", FontUtil.getFontFeatureSettings())); // zh-cn and pinyin
        tv.setTextIsSelectable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(s);
        new AlertDialog.Builder(context).setView(tv).show();
    }

    public static boolean isCustomLanguage(String lang) {
        return Pref.getCustomLanguages().contains(lang);
    }

    static int getAppTheme() {
        return switch (FontUtil.getFontFormat()) {
            case 0 -> R.style.AppThemeSans;
            case 1, 2 -> R.style.AppThemeSerif;
            default -> R.style.AppTheme;
        };
    }

    public static void setLocale() {
        String locale = Pref.getStr(R.string.pref_key_locale);
        if (TextUtils.isEmpty(locale)) locale = "ko";
        Locale.setDefault(Locale.forLanguageTag(locale));
    }

    public static int obtainColor(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue,false);
        int color = -1;
        try (TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{resId})) {
            color = arr.getColor(0, color);
        }
        return color;
    }

    public static void setActivityTheme(AppCompatActivity app) {
        app.setTheme(getAppTheme());
    }
}
