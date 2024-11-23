package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Mandarin {
    public static final int IPA = 0;
    public static final int PINYIN = 1;
    public static final int BOPOMOFO = 2;

    public static final Map<String, String> mapPinyin = new HashMap<>();
    private static final char[] vowels = {'a', 'o', 'e', 'ê', 'i', 'u', 'v', 'n', 'm'};

    public static final Map<String, String> mapFromBopomofoPartial = new HashMap<>();
    public static final Map<String, String> mapFromBopomofoWhole = new HashMap<>();
    public static final Map<Character, Character> mapFromBopomofoTone = new HashMap<>();
    public static final Map<String, String> mapToBopomofoPartial = new HashMap<>();
    public static final Map<String, String> mapToBopomofoWhole = new HashMap<>();
    public static final Map<Character, Character> mapToBopomofoTone = new HashMap<>();
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Mandarin.display(s, Pref.getToneStyle(R.string.pref_key_mandarin_display));
        }
    };

    public static String canonicalize(String s) {
        // Input can be either pinyin or bopomofo
        if (TextUtils.isEmpty(s)) return s;

        if (mapFromBopomofoPartial.containsKey(s.substring(0, 1)) ||
                mapFromBopomofoTone.containsKey(s.charAt(0))) {   // Bopomofo
            // Allow tones at either end
            char tone = '1';
            if (mapFromBopomofoTone.containsKey(s.charAt(0))) {
                tone = mapFromBopomofoTone.get(s.charAt(0));
                s = s.substring(1);
            } else if (mapFromBopomofoTone.containsKey(s.charAt(s.length() - 1))) {
                tone = mapFromBopomofoTone.get(s.charAt(s.length() - 1));
                s = s.substring(0, s.length() - 1);
            }
            if (TextUtils.isEmpty(s)) return null;   // Fail
            if (mapFromBopomofoWhole.containsKey(s)) {
                s = mapFromBopomofoWhole.get(s);
            } else if (mapFromBopomofoPartial.containsKey(s.substring(0, 1)) &&
                    mapFromBopomofoPartial.containsKey(s.substring(1))) {
                s = mapFromBopomofoPartial.get(s.substring(0, 1)) +
                        mapFromBopomofoPartial.get(s.substring(1));
                if (s.startsWith("jv") || s.startsWith("qv") || s.startsWith("xv")) {
                    s = s.charAt(0) + "u" + s.substring(2);
                }
            } else {
                return null;    // Fail
            }
            return s + (tone == '_' ? "" : tone);
        } else {  // Pinyin
            StringBuilder sb = new StringBuilder();
            char tone = '_';
            for (int i = 0; i < s.length(); i++) {
                String key = s.substring(i, i + 1);
                if (mapPinyin.containsKey(key)) {
                    String value = mapPinyin.get(key);
                    char base = Objects.requireNonNull(value).charAt(0);
                    if (base != '_') sb.append(base);
                    char t = value.charAt(1);
                    if (t != '_') tone = t;
                } else {
                    sb.append(s.charAt(i));
                }
            }
            return sb.toString() + (tone == '_' ? "" : tone);
        }
    }

    public static String display(String s, int system) {
        // Get tone
        char tone = s.charAt(s.length() - 1);
        if (tone >= '1' && tone <= '4') {
            s = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }

        switch (system) {
            case IPA:
                return getIPA(s, tone);
            case PINYIN:
                // Find letter to carry the tone
                s = s.replace("ea", "ê");
                int pos = -1;
                if (s.endsWith("iu")) {     // In the combination "iu", "u" gets the tone
                    pos = s.length() - 1;
                } else {                      // Find letter in this order: a,o,e,i,u,v,n,m
                    for (char c : vowels) {
                        pos = s.indexOf(c);
                        if (pos >= 0) break;
                    }
                }
                if (pos == -1) return null; // Fail
                // Transform the string and add tone to letter
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    char t = (i == pos) ? tone : '_';
                    String key = String.valueOf(s.charAt(i)) + t;
                    if (mapPinyin.containsKey(key)) {
                        sb.append(mapPinyin.get(key));
                    } else {
                        sb.append(s.charAt(i));
                        if (t != '_') sb.append(mapPinyin.get("_" + t));
                    }
                }
                return sb.toString(); //pinyin
            case BOPOMOFO:
                if (mapToBopomofoWhole.containsKey(s)) {
                    s = mapToBopomofoWhole.get(s);
                } else {
                    if (s.startsWith("ju") || s.startsWith("qu") || s.startsWith("xu")) {
                        s = s.charAt(0) + "v" + s.substring(2);
                    }
                    int p = s.length();
                    if (p > 2) p = 2;
                    while (p > 0) {
                        if (mapToBopomofoPartial.containsKey(s.substring(0, p))) break;
                        p--;
                    }
                    if (p == 0) return null;    // Fail
                    if (!mapToBopomofoPartial.containsKey(s.substring(p))) return null;   // Fail
                    s = mapToBopomofoPartial.get(s.substring(0, p)) + mapToBopomofoPartial.get(s.substring(p));
                }
                return switch (tone) {
                    case '2', '3', '4' -> s + mapToBopomofoTone.get(tone);
                    case '_' -> mapToBopomofoTone.get(tone) + s;
                    default -> s;
                };
            default:
                return null;    // Fail
        }
    }

    public static String getIPA(String s, char tone) {
        s = s.replace("yu", "v").replace("y", "i").replace("ii", "i")
                .replace("v", "y").replaceFirst("^([jqx])u", "$1y")
                .replaceFirst("([zcs])i", "$1ɿ").replaceFirst("([zcs]h|r)i", "$1ʅ")
                .replace("w", "u").replace("uu", "u")
                .replace("un", "uen").replace("ui", "uei").replace("iu", "iou")
                .replaceFirst("([iy])e$", "$1ɛ").replace("ea", "ɛ").replaceFirst("e$", "ɤ").replace("er", "ɚ").replace("en", "ən")
                .replace("ao", "au").replaceFirst("ian$", "iɛn")
                .replace("ong", "uŋ").replace("ng", "ŋ");
        s = s.replace("p", "pʰ").replace("t", "tʰ").replace("k", "kʰ")
                .replace("b", "p").replace("d", "t").replace("g", "k")
                .replace("zh", "tʂ").replace("ch", "tʂʰ").replace("sh", "ʂ").replace("r", "ɻ")
                .replace("z", "ts").replace("c", "tsʰ")
                .replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("h", "x");
        s = s.replaceFirst("^x([mnŋ])$", "h$1").replaceFirst("^h?[mn]$", "$0\u0329").replaceFirst("^h?ŋ$", "$0\u030D"); // 成音節鼻音
        return Orthography.formatTone(s, tone + "", DB.CMN);
    }

    public static List<String> getAllTones(String s) {
        if (TextUtils.isEmpty(s)) return null;     // Fail
        char tone = s.charAt(s.length() - 1);
        String base = s;
        if (tone >= '1' && tone <= '4') {
            base = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }
        if (base.isEmpty()) return null;               // Fail

        List<String> result = new ArrayList<>();
        result.add(s);
        for (char c = '1'; c <= '4'; c++) {
            if (c != tone) {
                result.add(base + c);
            }
        }
        if (tone != '_') {
            result.add(base);
        }
        return result;
    }
}
