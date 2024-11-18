package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiddleChinese {
    private static final List<String> toneSuffixesUnchecked = Arrays.asList(
            "q", "X", "x", "ʔ", // 上聲；包含 unt 和 msoeg 擬音入聲 -q
            "h", "H", "d" // 去聲
    );
    private static final List<String> toneSuffixesChecked = Arrays.asList(
            "p", "t", "k", "q" // 入聲
    );
    private static final List<String> vowelsHighUnt = Arrays.asList(
            "i", "ɨ", "ʉ", "u", "e" // 支韻是 ie，所以 e 算高元音
    );
    private static final List<String> vowelsNonHighUnt = Arrays.asList(
            "ɛ", "ə", "o", "ɔ",
            "æ", "a", "ɑ"
    );

    public static String display(String pys, int[] systems) {
        String[] ss = pys.split("/");

        // Get tone first
        int tone = 1;
        String tshetUinhDescription = ss[18];
        tone = switch (tshetUinhDescription.charAt(tshetUinhDescription.length() - 1)) {
            case '平' -> 1;
            case '上' -> 2;
            case '去' -> 3;
            case '入' -> 4;
            default -> tone;
        };

        String[] values = Utils.getStringArray(R.array.pref_values_mc_display);

        int pyCount = 0;
        int ybCount = 0;
        int descriptionCount = 0;
        int bookCount = 0;
        for (int j : systems) {
            int i = Integer.parseInt(values[j]);
            if (i < 100) pyCount++;
            else if (i < 200) ybCount++;
            else descriptionCount++;
            if (300 <= i && i < 400) bookCount++;
        }

        StringBuilder sb = new StringBuilder();
        String[] names = Utils.getStringArray(R.array.pref_entries_mc_display);
        // 拼音和擬音
        for (int j : systems) {
            int i = Integer.parseInt(values[j]);
            if (i >= 200) continue;
            String s = ss[j];
            String name = names[j];
            if (i < 100) {
                // 拼音
                s = Orthography.formatRoman(s);
                name = name.replace("切韻拼音", "切拼");
                name = name.replace("轉寫", "").replace("羅馬字", "");
            } else {
                // 擬音
                s = Orthography.formatTone(s, tone + "", DB.GY);
                name = name.replace("（", "{").replace("）", "}");
                name = name.replace("通俗", "{通俗}");
                name = name.replace("擬音", "");
            }
            sb.append(s);
            if ((pyCount > 1 && i < 100) || (ybCount > 1 && i >= 100 && i < 200))
                sb.append(String.format("(%s)", name));
            sb.append(" ");
        }
        // 既有拼音擬音又有描述時，給描述添加括號
        if (pyCount + ybCount > 0 && descriptionCount > 0) {
            sb.append("(");
        }
        // 描述
        for (int j : systems) {
            int i = Integer.parseInt(values[j]);
            if (i < 200) continue;
            String s = ss[j];
            if (bookCount > 1) {
                s = switch (i) {
                    case 300 -> "廣韻" + s;
                    case 301 -> "平水" + s;
                    default -> s;
                };
            }
            sb.append(s);
            sb.append(" ");
        }
        if (pyCount + ybCount > 0 && descriptionCount > 0) {
            sb.deleteCharAt(sb.length() - 1); // Remove last space
            sb.append(")");
        }
        return sb.toString();
    }

    public static List<String> getAllTones(String s) {
        if (TextUtils.isEmpty(s)) return null;                 // Fail
        List<String> result = new ArrayList<>();
        result.add(s);

        if (s.contains("/")) return result;

        // 輸入上去入聲則只查詢該聲調
        // 有女羅馬字因聲調變通拼寫複雜且使用者少，此處不考慮
        if (toneSuffixesUnchecked.contains(s.substring(s.length() - 1)) ||
                toneSuffixesChecked.contains(s.substring(s.length() - 1)) ||
                s.contains("\u0301") || s.contains("\u0300")) // unt 擬音平上聲
            return result;

        // 輸入平聲則查詢對應四聲
        // 上去聲
        for (String suffix : toneSuffixesUnchecked) {
            result.add(s + suffix);
        }
        boolean hasNonHighVowel = false;
        for (String vowel : vowelsNonHighUnt) {
            if (s.contains(vowel)) {
                hasNonHighVowel = true;
                result.add(s.replace(vowel, vowel + "\u0301"));
                result.add(s.replace(vowel, vowel + "\u0300"));
            }
        }
        if (!hasNonHighVowel) {
            // 在有非高元音時，高元音不可能是主元音
            for (String vowel : vowelsHighUnt) {
                if (s.contains(vowel)) {
                    result.add(s.replace(vowel, vowel + "\u0301"));
                    result.add(s.replace(vowel, vowel + "\u0300"));
                }
            }
        }
        // 入聲
        String base = s.substring(0, s.length() - 1);
        switch (s.charAt(s.length() - 1)) {
            case 'm':
                result.add(base + "p");
                break;
            case 'n':
                result.add(base + "t");
                break;
            case 'ŋ':
                result.add(base + "k");
                break;
            case 'ɴ':
                result.add(base + "q");
                break;
        }
        if (s.length() > 1 && s.substring(s.length() - 2).contentEquals("ng")) {
            String base2 = s.substring(0, s.length() - 2);
            result.add(base2 + "k");
        }
        return result;
    }
}
