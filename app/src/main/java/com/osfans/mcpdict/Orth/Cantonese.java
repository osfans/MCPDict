package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cantonese {
    public static final int IPA = 0;
    public static final int JYUTPING = 1;   // This is the database representation
    public static final int CANTONESE_PINYIN = 2;
    public static final int YALE = 3;
    public static final int SIDNEY_LAU = 4;

    // References:
    // http://en.wikipedia.org/wiki/Jyutping
    // http://en.wikipedia.org/wiki/Cantonese_Pinyin
    // http://en.wikipedia.org/wiki/Yale_romanization_of_Cantonese
    // http://en.wikipedia.org/wiki/Sidney_Lau
    // http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/

    public static final List<Map<String, String>> listInitials = new ArrayList<>();
    public static final List<Map<String, String>> listFinals = new ArrayList<>();
    public static final List<Map<String, String>> listInitialsR = new ArrayList<>();
    public static final List<Map<String, String>> listFinalsR = new ArrayList<>();
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Cantonese.display(s, Pref.getToneStyle(R.string.pref_key_cantonese_romanization));
        }
    };

    public static String canonicalize(String s, int system) {
        // Convert from given system to Jyutping

        // Check for null or empty string
        if (TextUtils.isEmpty(s)) return s;

        // Choose map first
        Map<String, String> mapInitials, mapFinals;
        int index;
        switch (system) {
            case JYUTPING:
                return s;
            case IPA:
                index = 3;
                break;
            default:
                index = system - 2;
        }
        mapInitials = listInitialsR.get(index);
        mapFinals = listFinalsR.get(index);

        // Get tone
        char tone = s.charAt(s.length() - 1);
        if (tone >= '1' && tone <= '9') {
            s = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }

        // Get final
        int p = 0;
        while (p < s.length() && !Objects.requireNonNull(mapFinals).containsKey(s.substring(p)))
            p++;
        if (p == s.length()) return null;   // Fail
        String fin = mapFinals.get(s.substring(p));

        // Get initial
        String init = s.substring(0, p);
        if (p > 0) {
            if (!Objects.requireNonNull(mapInitials).containsKey(s.substring(0, p))) return null;
            init = mapInitials.get(s.substring(0, p));
        }

        // In Cantonese Pinyin, tones 7,8,9 are used for entering tones
        // They need to be replaced by with 1,3,6 in Jyutping
        tone = switch (tone) {
            case '7' -> '1';
            case '8' -> '3';
            case '9' -> '6';
            default -> tone;
        };

        // In Yale, initial "y" is omitted if final begins with "yu"
        // If that happens, we need to put the initial "j" back in Jyutping
        if (system == YALE && TextUtils.isEmpty(init) && Objects.requireNonNull(fin).startsWith("yu"))
            init = "j";

        return init + fin + (tone == '_' ? "" : tone);
    }

    public static String display(String s, int system) {
        // Convert from Jyutping to given system

        // Choose map first
        Map<String, String> mapInitials, mapFinals;
        int index;
        switch (system) {
            case JYUTPING:
                return Orthography.formatRoman(s);
            case IPA:
                index = 3;
                break;
            default:
                index = system - 2;
                break;
        }
        mapInitials = listInitials.get(index);
        mapFinals = listFinals.get(index);

        // Get tone
        char tone = s.charAt(s.length() - 1);
        if (tone >= '1' && tone <= '6') {
            s = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }

        // Get final
        int p = 0;
        while (p < s.length() && !Objects.requireNonNull(mapFinals).containsKey(s.substring(p)))
            p++;
        if (p == s.length()) return null;   // Fail
        String fin = mapFinals.get(s.substring(p));

        // Get initial
        String init = s.substring(0, p);
        if (p > 0) {
            if (!Objects.requireNonNull(mapInitials).containsKey(s.substring(0, p))) return null;
            init = mapInitials.get(s.substring(0, p));
        }

        // In Cantonese Pinyin, tones 7,8,9 are used for entering tones
        boolean isEnteringTone = "ptk".indexOf(Objects.requireNonNull(fin).charAt(fin.length() - 1)) >= 0;
        if (system == CANTONESE_PINYIN && isEnteringTone) {
            tone = switch (tone) {
                case '1' -> '7';
                case '3' -> '8';
                case '6' -> '9';
                default -> tone;
            };
        }

        // IPA tones
        if (system == IPA) {
            if (isEnteringTone) {
                tone = switch (tone) {
                    case '1' -> '7';
                    case '3' -> '8';
                    case '6' -> '9';
                    default -> tone;
                };
            }
        }

        // In Yale, initial "y" is omitted if final begins with "yu"
        if (system == YALE && Objects.requireNonNull(init).equals("y") && Objects.requireNonNull(fin).startsWith("yu"))
            init = "";
        if (system == IPA) return Orthography.formatTone(init + fin, tone + "", DB.HK);
        return Orthography.formatRoman(init + fin + (tone == '_' ? "" : tone));
    }

    public static List<String> getAllTones(String s) {
        if (TextUtils.isEmpty(s)) return null;     // Fail
        char tone = s.charAt(s.length() - 1);
        String base = s;
        if (tone >= '1' && tone <= '6') {
            base = s.substring(0, s.length() - 1);
        } else {
            tone = '_';
        }
        if (TextUtils.isEmpty(base)) return null;               // Fail

        boolean isEnteringTone = "ptk".indexOf(base.charAt(base.length() - 1)) >= 0;
        List<String> result = new ArrayList<>();
        result.add(s);
        if (tone != '1') result.add(base + '1');
        if (tone != '2' && !isEnteringTone) result.add(base + '2');
        if (tone != '3') result.add(base + '3');
        if (tone != '4' && !isEnteringTone) result.add(base + '4');
        if (tone != '5' && !isEnteringTone) result.add(base + '5');
        if (tone != '6') result.add(base + '6');
        return result;
    }
}
