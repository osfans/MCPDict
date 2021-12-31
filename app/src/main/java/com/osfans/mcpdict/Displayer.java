package com.osfans.mcpdict;

import android.text.TextUtils;

abstract class Displayer {
    protected static final String NULL_STRING = "-";
    public int col;

    public String display(String s) {
        if (s == null) return NULL_STRING;
        s = lineBreak(s);
        // Find all regions of [a-z0-9]+ in s, and apply displayer to each of them
        StringBuilder sb = new StringBuilder();
        int L = s.length(), p = 0;
        boolean isMeaning;
        while (p < L) {
            int q = p;
            while (q < L && Orthography.HZ.isIPA(s.charAt(q))) q++;
            if (q > p) {
                String t1 = s.substring(p, q);
                String t2 = displayOne(t1);
                sb.append(TextUtils.isEmpty(t2) ? t1 : t2);
                p = q;
            }
            isMeaning = false;
            while (p < L && (isMeaning || !Orthography.HZ.isIPA(s.charAt(p)))) {
                if (s.charAt(p) == '{') isMeaning = true;
                else if (s.charAt(p) == '}') isMeaning = false;
                p++; //
            }
            sb.append(s.substring(q, p));
        }
        // Add spaces as hints for line wrapping
        s = sb.toString().replace(",", " ")
                .replace("(", " (")
                .replace("]", "] ")
                .replace(" ,", ",")
                .trim();
        return s;
    }

    public String display(String s, int i) {
        col = i;
        return display(s);
    }

    public int getCol() {
        return col;
    }


    public String lineBreak(String s) {
        return s;
    }

    public abstract String displayOne(String s);
}
