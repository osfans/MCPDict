package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Vietnamese {
    public static final int IPA = 0;
    public static final int OLD_STYLE = 1;
    public static final int NEW_STYLE = 2;

    public static final Map<String, String> map = new HashMap<>();
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Vietnamese.display(s, Pref.getToneStyle(R.string.pref_key_vietnamese_tone_position));
        }
    };

    public static String canonicalize(String s) {
        // Input can be either with diacritics, or non-canonicalized Telex string
        if (TextUtils.isEmpty(s)) return s;
        char tone = '_';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0 && "zrsfxj".indexOf(c) >= 0 && !(i == 1 && s.startsWith("tr"))) { // c is tone
                if (c != 'z') tone = c;
                continue;
            }
            String key = s.substring(i, i + 1);
            if (map.containsKey(key)) {
                String value = map.get(key);
                String base = Objects.requireNonNull(value).substring(0, value.length() - 1);
                sb.append(base);
                c = value.charAt(value.length() - 1);
                if (c != '_') tone = c;
            } else {
                sb.append(s.charAt(i));
            }
        }

        // Canonicalizing "y" and "i":
        // At the beginning of a word, use "y" if it's the only letter, or if it's followed by "e"
        // At other places, both "y" and "i" can occur after "a" or "u", but only "i" can occur after other letters
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == 'y' || sb.charAt(i) == 'i') {
                if (i == 0) {
                    sb.setCharAt(0, (sb.length() == 1 || sb.charAt(1) == 'e') ? 'y' : 'i');
                } else {
                    if (sb.charAt(i - 1) != 'a' && sb.charAt(i - 1) != 'u') sb.setCharAt(i, 'i');
                }
            }
        }
        return sb.toString() + (tone == '_' ? "" : tone);
    }

    private static String getIPA(String s, char tone) {
        boolean isEnteringTone = "ptc".indexOf(s.charAt(s.length() - 1)) >= 0 ||
                s.endsWith("ch");
        s = s.replace("b", "ɓ").replace("dd", "ɗ").replace("d", "z")
                .replace("ph", "f").replace("gi", "z").replaceFirst("^gh?", "ɣ")
                .replace("x", "s").replace("kh", "x").replaceFirst("^[cq]([^h])", "k$1")
                .replaceFirst("^nh", "ɲ").replace("ngh", "ŋ").replace("ng", "ŋ").replace("th", "tʰ")
                .replaceFirst("^ch", "tɕ").replace("tr", "tɕ")
                .replaceFirst("^r", "z").replace("gi", "z").replace("ʂ", "s");
        s = s.replace("nh", "ŋ̟").replaceFirst("c$", "k").replaceFirst("ch$", "k̟");
        s = s.replace("y", "i").replaceFirst("o([ae])", "u$1")
                .replace("aa", "ə").replace("aw", "ă").replace("a", "aː").replace("ă", "a")
                .replace("ee", "ê").replace("e", "ɛ").replace("ê", "e")
                .replace("uw", "ɨ").replace("ow", "əː").replace("oo", "ô").replace("o", "ɔ").replace("ô", "o")
                .replace("ie", "iə").replaceFirst("([iuɨ])a$", "$1ə")
                .replace("ɨəː", "ɨə").replace("uo", "uə").replaceFirst("([aːəeɛiɨ])[uɔ]$", "$1w").replaceFirst("([uoɔ]k)", "$1p");
        int index;
        if (isEnteringTone) {
            index = tone == 's' ? 7 : 8;
        } else {
            index = "_frxsj".indexOf(tone) + 1;
        }
        return Orthography.formatTone(s, index + "", DB.VI);
    }

    // Rules for placing the tone marker follows this page in Vietnamese Wikipedia:
    // Quy tắc đặt dấu thanh trong chữ quốc ngữ
    public static String display(String s, int style) {
        // Get tone
        char tone = s.charAt(s.length() - 1);
        if ("rsfxj".indexOf(tone) >= 0) {
            s = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }
        if (style == IPA) return getIPA(s, tone);

        // If any vowel carries quality marker, put tone marker there, too
        // In the combination "ươ", "ơ" gets the tone marker
        StringBuilder sb = new StringBuilder();
        int p = 0;
        while (p < s.length()) {
            if (p == s.length() - 1) {
                sb.append(s.charAt(p));
                break;
            }
            String key = s.substring(p, p + 2);
            if (map.containsKey(key + "_")) {
                if (key.equals("dd") || p + 4 <= s.length() && s.startsWith("uwow", p)) {
                    sb.append(map.get(key + "_"));      // Tone marker doesn't go here
                } else {
                    sb.append(map.get(key + tone));     // Tone marker goes here
                    tone = '_';
                }
                p += 2;
            } else {
                sb.append(s.charAt(p++));
            }
        }
        if (tone == '_') return sb.toString();          // No tone marker to place

        // Place tone marker
        // Find first and last vowel
        p = 0;
        while (p < sb.length() && "aeiouy".indexOf(sb.charAt(p)) < 0) p++;
        if (p == sb.length()) return null;              // There has to be a vowel, otherwise fail
        int q = p + 1;
        while (q < sb.length() && "aeiouy".indexOf(sb.charAt(q)) >= 0) q++;
        // Decide which vowel to get the tone marker
        if (q - p == 3 ||
                q - p == 2 && (q < sb.length() ||
                        s.startsWith("gi") ||
                        s.startsWith("qu") ||
                        (style == NEW_STYLE) && (sb.substring(p, q).equals("oa") ||
                                sb.substring(p, q).equals("oe") ||
                                sb.substring(p, q).equals("uy")))) p++;
        // Place the tone marker
        sb.setCharAt(p, Objects.requireNonNull(map.get(String.valueOf(sb.charAt(p)) + tone)).charAt(0));
        return sb.toString();
    }

    public static List<String> getAllTones(String s) {
        if (TextUtils.isEmpty(s)) return null;     // Fail
        char tone = s.charAt(s.length() - 1);
        String base = s;
        if ("rsfxj".indexOf(tone) >= 0) {
            base = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }
        if (TextUtils.isEmpty(base)) return null;               // Fail

        boolean isEnteringTone = "ptc".indexOf(base.charAt(base.length() - 1)) >= 0 ||
                base.endsWith("ch");
        List<String> result = new ArrayList<>();
        result.add(s);
        if (tone != '_' && !isEnteringTone) result.add(base);
        if (tone != 'r' && !isEnteringTone) result.add(base + 'r');
        if (tone != 's') result.add(base + 's');
        if (tone != 'f' && !isEnteringTone) result.add(base + 'f');
        if (tone != 'x' && !isEnteringTone) result.add(base + 'x');
        if (tone != 'j') result.add(base + 'j');
        return result;
    }
}
