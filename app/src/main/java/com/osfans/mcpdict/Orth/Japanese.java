package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Japanese {
    public static final int IPA = 0;
    public static final int HIRAGANA = 1;
    public static final int KATAKANA = 2;
    public static final int NIPPON = 3;     // This is the database representation
    public static final int HEPBURN = 4;
    // Reference: Japanese Wikipedia ローマ字

    public static final Map<String, String> mapIPA = new HashMap<>();
    public static final Map<String, String> mapHiragana = new HashMap<>();
    public static final Map<String, String> mapKatakana = new HashMap<>();
    public static final Map<String, String> mapNippon = new HashMap<>();
    public static final Map<String, String> mapHepburn = new HashMap<>();
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Japanese.display(s, Pref.getToneStyle(R.string.pref_key_japanese_display));
        }
    };

    public static String convertTo(String s, int system) {
        if (TextUtils.isEmpty(s)) return s;

        // Choose map
        Map<String, String> map = switch (system) {
            case IPA -> mapIPA;
            case HIRAGANA -> mapHiragana;
            case KATAKANA -> mapKatakana;
            case NIPPON -> mapNippon;
            case HEPBURN -> mapHepburn;
            default -> null;
        };

        StringBuilder sb = new StringBuilder();
        int p = 0;
        while (p < s.length()) {
            int q = p;
            for (int i = 4; i > 0; i--) {
                if (p + i <= s.length() && Objects.requireNonNull(map).containsKey(s.substring(p, p + i))) {
                    q = p + i;
                    sb.append(map.get(s.substring(p, q)));
                    break;
                }
            }
            if (q == p) return null;
            p = q;
        }
        s = sb.toString();
        if (system == HEPBURN) s = Orthography.formatRoman(s);
        return s;
    }

    public static String canonicalize(String s) {
        return convertTo(s, NIPPON);
    }

    public static String display(String s, int system) {
        return (system == NIPPON) ? Orthography.formatRoman(s) : convertTo(s, system);
    }
}
