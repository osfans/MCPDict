package com.osfans.mcpdict.Orth;

import static com.osfans.mcpdict.DB.COL_GYHZ;
import static com.osfans.mcpdict.DB.COL_HD;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_KX;
import static com.osfans.mcpdict.DB.COL_SW;

import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;

public abstract class DisplayHelper {
    protected static final String NULL_STRING = "-";
    private static final String PAGE_FORMAT = "(\\d+)\\.(\\d+)";
    private static final DisplayHelper SG_DISPLAY_HELPER = new DisplayHelper() {
        public String displayOne(String s) {
            return s;
        }

        public boolean isIPA(char c) {
            return c != '{';
        }
    };
    public String mLang;

    public static CharSequence getRichText(String richTextString) {
        String s = richTextString
                .replace("?", "？").replace("!", "！").replace(":", "：").replace(";", "；").replace("~", "～")
                .replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                .replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<font color='red'>$1</font>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>")
                .replaceAll("\\*(.+?)\\*", "<i>$1</i>")
                .replaceAll("`(.+?)`", "<span style='color: #808080;'>$1</span>");
        s = s.replace("{", "<small><small>").replace("}", "</small></small>");
        return s;
    }

    public static String formatJS(String hz, String s) {
        if (TextUtils.isEmpty(s)) return "";
        return s.replace("*" + hz + "*", "～").replace("  ", "　").replace(" ", "").replace("　", " ").replace("?", "？").replace("!", "！").replace(",", "，").replace(":", "：").replace(";", "；").replace("~", "～");
    }

    public static String formatZS(String hz, String s) {
        return String.format("<small><small>%s</small></small>", formatJS(hz, s));
    }

    public static String getRawText(String s) {
        if (TextUtils.isEmpty(s)) return "";
        return s.replaceAll("[|*\\[\\]]", "").replaceAll("\\{.*?\\}", "");
    }
    
    public static CharSequence formatPopUp(String hz, int i, String s) {
        if (TextUtils.isEmpty(s)) return "";
        if (i != COL_HZ) s = formatJS(hz, s);
        if (i == COL_SW) s = s.replace("{", "<small>").replace("}", "</small>");
        else if (i == COL_KX) s = s.replaceAll(PAGE_FORMAT, "<a href=https://www.kangxizidian.com/v1/index.php?page=$1>第$1頁</a>第$2字");
        else if (i == COL_GYHZ) s = Pref.getString(R.string.book_format, DB.getLanguageByLabel(DB.getColumn(i))) + s.replaceFirst(PAGE_FORMAT, "第$1頁第$2字");
        else if (i == COL_HD) s = Pref.getString(R.string.book_format, DB.getLanguageByLabel(DB.getColumn(i))) + s.replaceAll(PAGE_FORMAT, "<a href=https://homeinmists.ilotus.org/hd/png/$1.png>第$1頁</a>第$2字").replace("lv", "lü").replace("nv", "nü");
        String[] fs = (s + "\n").split("\n", 2);
        String text = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br>"));
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    public static CharSequence formatIPA(String lang, String s) {
        if (TextUtils.isEmpty(s)) return "";
        if (!lang.contentEquals(DB.BA)) s = s.replaceAll("-([/ ])|-(/?)$", "{(白)}$1").replaceAll("=([/ ])|=(/?)$", "{(文)}$1");
        return switch (lang) {
            case DB.HK -> Cantonese.displayHelper.display(s, lang);
            case DB.KOR -> Korean.displayHelper.display(s, lang);
            case DB.VI -> Vietnamese.displayHelper.display(s, lang);
            case DB.BA -> BaiSha.displayHelper.display(s, lang);

            case DB.SG -> SG_DISPLAY_HELPER.displayRich(s, lang);
            case DB.GY -> MiddleChinese.displayHelper.displayRich(s, lang);
            case DB.ZT -> Zhongtang.displayHelper.displayRich(s, lang);
            case DB.ZYYY -> ZhongyuanYinyun.displayHelper.displayRich(s, lang);
            case DB.DGY -> Dungan.displayHelper.displayRich(s, lang);
            case DB.CMN, DB.CMN_TW -> Mandarin.displayHelper.displayRich(s, lang);
            case DB.TW -> Minnan.displayHelper.displayRich(s, lang);
            case DB.JA_GO, DB.JA_KAN, DB.JA_OTHER -> Japanese.displayHelper.displayRich(s, lang);
            default -> Tones.displayHelper.displayRich(s, lang);
        };
    }

    public static CharSequence getIPA(String lang, String s) {
        return HtmlCompat.fromHtml(formatIPA(lang, s).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
    }

    public boolean isIPA(char c) {
        if (HanZi.isHz(c)) return false;
        if (c == '_' || c == '*' || c == '`') return false;
        int type = Character.getType(c);
        return Character.isLetterOrDigit(c)
                || type == Character.NON_SPACING_MARK
                || type == Character.MODIFIER_SYMBOL
                || type == Character.OTHER_NUMBER;
    }

    public String display(String s) {
        if (s == null) return NULL_STRING;
        // Find all regions of [a-z0-9]+ in s, and apply display helper to each of them
        StringBuilder sb = new StringBuilder();
        int L = s.length(), p = 0;
        String js;
        while (p < L) {
            int q = p;
            while (q < L && isIPA(s.charAt(q))) q++;
            if (q > p) {
                String t1 = s.substring(p, q);
                String t2 = displayOne(t1);
                sb.append(TextUtils.isEmpty(t2) ? t1 : t2);
                p = q;
            }
            while (p < L && !isIPA(s.charAt(p))) {
                p++; //
            }
            js = s.substring(q, p);
            sb.append(js);
        }
        // Add spaces as hints for line wrapping
        s = sb.toString().replace("\t", " ").replace(",", " ").replace("  ", " ")
                .replace("(", " (")
                .replace("]", "] ")
                .trim();
        return s;
    }

    public String display(String s, String lang) {
        mLang = lang;
        return display(s);
    }

    public CharSequence displayRich(String s, String lang) {
        return getRichText(display(s, lang));
    }

    public String getLang() {
        return mLang;
    }

    public abstract String displayOne(String s);
}
