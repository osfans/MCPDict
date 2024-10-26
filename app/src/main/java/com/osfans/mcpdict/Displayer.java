package com.osfans.mcpdict;

import android.text.TextUtils;

abstract class Displayer {
    protected static final String NULL_STRING = "-";
    public String mLang;

    public boolean isIPA(char c) {
        int type = Character.getType(c);
        if (Orthography.HZ.isHz(c)) return false;
        return Character.isLetterOrDigit(c)
                || type == Character.NON_SPACING_MARK
                || type == Character.MODIFIER_SYMBOL
                || type == Character.OTHER_NUMBER;
    }

    public String display(String s) {
        if (s == null) return NULL_STRING;
        s = lineBreak(s);
        // Find all regions of [a-z0-9]+ in s, and apply displayer to each of them
        StringBuilder sb = new StringBuilder();
        int L = s.length(), p = 0;
        boolean isMeaning;
        while (p < L) {
            int q = p;
            while (q < L && isIPA(s.charAt(q))) q++;
            if (q > p) {
                String t1 = s.substring(p, q);
                String t2 = displayOne(t1);
                sb.append(TextUtils.isEmpty(t2) ? t1 : t2);
                p = q;
            }
            isMeaning = false;
            while (p < L && (isMeaning || !isIPA(s.charAt(p)))) {
                if (s.charAt(p) == '{') isMeaning = true;
                else if (s.charAt(p) == '}') isMeaning = false;
                p++; //
            }
            sb.append(s.substring(q, p));
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

    public String getLang() {
        return mLang;
    }


    public String lineBreak(String s) {
        return s;
    }

    public abstract String displayOne(String s);
}
