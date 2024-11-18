package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class Korean {
    public static final int IPA = 0;
    public static final int HANGUL = 1;
    public static final int ROMANIZATION = 2;   // This is the database representation

    public static final char FIRST_HANGUL = 0xAC00;
    public static final char LAST_HANGUL = 0xD7A3;

    // Arrays: index to spelling
    public static final String[] initials = {"g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h"};
    public static final String[] vowels = {"a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"};
    public static final String[] finals = {"", "k", "kk0", "ks0", "n", "nj0", "nh0", "d0", "l", "lg0", "lm0", "lb0", "ls0", "lt0", "lp0", "lh0", "m", "p", "bs0", "s0", "ss0", "ng", "j0", "ch0", "k0", "t0", "p0", "h0"};
    // Finals with "0" are not valid pronunciations of Chinese characters
    // And they won't match anything in the database
    // Maps: spelling to index
    public static final Map<String, Integer> mapInitials = new HashMap<>();
    public static final Map<String, Integer> mapVowels = new HashMap<>();
    public static final Map<String, Integer> mapFinals = new HashMap<>();

    public static boolean isHangul(char c) {
        return c >= FIRST_HANGUL && c <= LAST_HANGUL;
    }

    public static String canonicalize(String s) {
        // Input can be either a hangul, or non-canonicalized romanization
        if (TextUtils.isEmpty(s)) return s;
        char unicode = s.charAt(0);
        if (isHangul(unicode)) {    // Hangul
            unicode -= FIRST_HANGUL;
            int z = unicode % finals.length;
            int x = unicode / finals.length;
            int y = x % vowels.length;
            x /= vowels.length;
            return initials[x] + vowels[y] + finals[z];
        } else {      // Romanization, do some obvious corrections
            if (s.startsWith("l")) s = "r" + s.substring(1);
            else if (s.startsWith("gg")) s = "kk" + s.substring(2);
            else if (s.startsWith("dd")) s = "tt" + s.substring(2);
            else if (s.startsWith("bb")) s = "pp" + s.substring(2);
            s = s.replace("weo", "wo").replace("oi", "oe").replace("eui", "ui");
            if (s.endsWith("r")) s = s.substring(0, s.length() - 1) + "l";
            else if (s.endsWith("g") && !s.endsWith("ng")) s = s.substring(0, s.length() - 1) + "k";
            else if (s.endsWith("d")) s = s.substring(0, s.length() - 1) + "t";
            else if (s.endsWith("b")) s = s.substring(0, s.length() - 1) + "p";
            return s;
        }
    }

    private static String getIPA(String s) {
        s = s.replace("t", "tʰ").replace("d", "t")
                .replace("j", "tɕ").replace("y", "j").replace("ch", "tɕʰ")
                .replace("r", "ɾ").replace("ng", "ŋ")
                .replace("p", "pʰ").replace("b", "p")
                .replace("kk", "K").replace("k", "kʰ").replace("g", "k").replace("K", "k͈")
                .replace("ss", "S").replace("S", "s͈").replaceFirst("ʰ$", "");
        s = s.replace("ae", "ɛ").replace("oe", "ø").replace("eo", "ʌ")
                .replace("eu", "ɯ").replace("ui", "ɰi").replace("wi", "y");
        return s;
    }

    public static String display(String s, int system) {
        if (system == IPA) return getIPA(s);
        if (system == ROMANIZATION) return Orthography.formatRoman(s);

        int L = s.length();
        int x, y, z, p, q;
        // Initial
        p = 0;
        for (int i = 2; i > 0; i--) {
            if (i <= L && mapInitials.containsKey(s.substring(0, i))) {
                p = i;
                break;
            }
        }
        x = mapInitials.get(s.substring(0, p));
        // Final
        q = L;
        for (int i = L - 2; i < L; i++) {
            if (i >= p && mapFinals.containsKey(s.substring(i))) {
                q = i;
                break;
            }
        }
        z = mapFinals.get(s.substring(q));
        // Vowel
        if (!mapVowels.containsKey(s.substring(p, q))) return null; // Fail
        y = mapVowels.get(s.substring(p, q));
        return String.valueOf((char) (FIRST_HANGUL + (x * vowels.length + y) * finals.length + z));
    }
}
