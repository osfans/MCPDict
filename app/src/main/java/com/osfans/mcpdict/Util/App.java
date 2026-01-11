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
        String language = DB.getLanguageByLabel(lang);
        if (TextUtils.isEmpty(language)) language = Pref.getLanguage();
        if (DB.isMainPage(language)) {
            WebView webView = new WebView(context, null);
            webView.loadDataWithBaseURL(null, DB.getMainIntro(), "text/html; charset=utf-8", "utf-8", null);
            new AlertDialog.Builder(context)
                    .setView(webView)
                    .show();
        } else {
            Dialog dialog = new AlertDialog.Builder(context)
                    .setMessage(HtmlCompat.fromHtml(DB.getLanguageIntro(language), HtmlCompat.FROM_HTML_MODE_COMPACT))
                    .setTitle(language)
                    .show();
            TextView textView = dialog.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setTextIsSelectable(true);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                FontUtil.setTypeface(textView);
            }
        }
    }

    public static void about(Context context) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.about)
                .setMessage(HtmlCompat.fromHtml(context.getString(R.string.about_message, Pref.getTitle(), BuildConfig.VERSION_NAME), HtmlCompat.FROM_HTML_MODE_COMPACT))
                .setPositiveButton(R.string.ok, null)
                .show();
        TextView messageText = dialog.findViewById(android.R.id.message);
        if (messageText != null) {
            messageText.setGravity(Gravity.CENTER);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static void help(Context context) {
        WebView webView = new WebView(context, null);
        webView.loadUrl("file:///android_asset/help/index.htm");
        new AlertDialog.Builder(context)
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
        String family = FontUtil.getFontFamily();
        if (family.contains("sans")) return R.style.AppThemeSans;
        if (family.contains("serif")) return R.style.AppThemeSerif;
        return R.style.AppTheme;
    }

    public static void setLocale() {
        String locale = Pref.getStr(R.string.pref_key_locale);
        if (TextUtils.isEmpty(locale)) locale = Pref.getString(R.string.default_locale);
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
