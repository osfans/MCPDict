package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;

public class Minnan {
    private static final int IPA = 0;
    private static final int ROMAN = 1;
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Minnan.display(s, Pref.getToneStyle(R.string.pref_key_minnan_display));
        }
    };

    public static String display(String s, int system) {
        if (system == ROMAN) return Orthography.formatRoman(s);
        char tone = s.charAt(s.length() - 1);
        String base = s;
        if (tone >= '1' && tone <= '8') {
            base = s.substring(0, s.length() - 1);
        } else {
            tone = '0';
        }
        s = base;
        s = s.replace("oo", "ɔ").replaceFirst("o(k|ng)", "ɔ$1").replace("o", "ə");
        s = s.replaceFirst("^(p|t|k|ts)h", "$1ʰ").replace("ng", "ŋ").replace("j", "dz").replaceFirst("^g", "ɡ").replaceFirst("h$", "ʔ").replace("nn", "̃");
        s = Orthography.formatTone(s, "" + tone, DB.TW);
        return s;
    }
}
