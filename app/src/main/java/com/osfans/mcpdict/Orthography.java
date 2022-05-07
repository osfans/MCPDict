package com.osfans.mcpdict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Orthography {

    // One static inner class for each language.
    // All inner classes (except for HZ) have the two following methods:
    //   public static String canonicalize(String s);
    //   public static String display(String s);
    // However, some may require an additional int argument specifying the format.
    // Inner classes for tonal languages also have the following method:
    //   public static List<String> getAllTones(String s);
    // which returns the given *canonicalized* syllable in all possible tones.
    // All methods return null on failure.

    private static int mToneStyle = 0;
    private static int mToneValueStyle = 0;

    public static void setToneStyle(int style) {
        mToneStyle = style;
    }

    public static void setToneValueStyle(int style) {
        mToneValueStyle = style;
    }

    private static String getJSONString(JSONArray styles, int index) {
        try {
            return styles.getString(index);
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatTone(String base, String tone, String lang) {
        if (tone.contentEquals("0") || TextUtils.isEmpty(tone)) return base;
        tone = tone.toUpperCase();
        JSONArray styles = null;
        try {
            JSONObject jsonObject = DB.getToneName(lang);
            styles = jsonObject.getJSONArray(tone);
        } catch (Exception ignored) {
        }
        if (styles == null || styles.length() != 5) return base + tone;
        String tv = getJSONString(styles, 0);
        if (!TextUtils.isEmpty(tv) && mToneValueStyle <= 1) {
            if (mToneValueStyle == 0) { //符號
                if (tv.length() == 2 && tv.charAt(0) == tv.charAt(1))
                    tv = tv.substring(0, 1);
                tv = tv.replace('1', '˩')
                        .replace('2', '˨')
                        .replace('3', '˧')
                        .replace('4', '˦')
                        .replace('5', '˥')
                        .replace('6', ' ')
                        .replace('0', ' ');
            } else { //數字
                tv = tv.replace('1', '¹')
                        .replace('2', '²')
                        .replace('3', '³')
                        .replace('4', '⁴')
                        .replace('5', '⁵')
                        .replace('6', '⁶')
                        .replace('0', '⁰');
            }
            base += tv;
        }
        switch (mToneStyle) {
            case 5:
                return base;
            case 0:
                return base + (1 + tone);
            default:
                String sTone = getJSONString(styles, mToneStyle);
                String style1 = getJSONString(styles, 1);
                if (TextUtils.isEmpty(sTone) || sTone.contentEquals("0")) return base;
                if (mToneStyle == 4 && !TextUtils.isEmpty(style1)) {
                    char a = style1.charAt(0);
                    if (a >= '1' && a <= '4') return sTone + base;
                }
                if (mToneStyle <= 2) {
                    char a = sTone.charAt(0);
                    sTone = sTone.replace(a, (char)(a - '1' + '①'));
                }
                return base + sTone;
        }
    }

    public static class HZ {
        private static final Map<Integer,Integer> compatibility = new HashMap<>();

        public static boolean isHz(int unicode) {
            return (unicode >= 0x4E00 && unicode <= 0x9FFF)
                    || unicode == 0x25A1 //□
                    || unicode == 0x3007 //〇
                    || (unicode >= 0x3400 && unicode <= 0x4DBF)
                    || (unicode >= 0x20000 && unicode <= 0x2A6DF)
                    || (unicode >= 0x2A700 && unicode <= 0x2B73F)
                    || (unicode >= 0x2B740 && unicode <= 0x2B81F)
                    || (unicode >= 0x2B820 && unicode <= 0x2CEAF)
                    || (unicode >= 0x2CEB0 && unicode <= 0x2EBEF)
                    || (unicode >= 0x30000 && unicode <= 0x3134F)
                    || (unicode >= 0xF900 && unicode <= 0xFAFF)
                    || (unicode >= 0x2F800 && unicode <= 0x2FA1F);
        }

        public static boolean isHz(String hz) {
            if (TextUtils.isEmpty(hz)) return false;
            return isHz(hz.codePointAt(0));
        }

        public static boolean isSingleHZ(String hz) {
            if (TextUtils.isEmpty(hz)) return false;
            return hz.codePoints().toArray().length == 1;
        }

        public static boolean isUnicode(String input) {
            if (TextUtils.isEmpty(input)) return false;
            return input.toUpperCase().matches("(U\\+)?[0-9A-F]{4,5}");
        }

        public static boolean isBH(String s) {
            if (TextUtils.isEmpty(s)) return false;
            return s.matches("[1-9][0-9]?");
        }

        public static boolean isBS(String s) {
            if (TextUtils.isEmpty(s)) return false;
            return isHz(s.codePointAt(0)) && s.substring(1).matches("-?[0-9]{1,2}");
        }

        public static boolean isPY(String s) {
            if (TextUtils.isEmpty(s)) return false;
            return s.matches("[a-z]+[0-5?]?");
        }

        public static boolean isIPA(char c) {
            int type = Character.getType(c);
            if (isHz(c)) return false;
            return Character.isLetterOrDigit(c)
                    || type == Character.NON_SPACING_MARK
                    || type == Character.MODIFIER_SYMBOL
                    || type == Character.OTHER_NUMBER;
        }

        public static int getCompatibility(int unicode) {
            return compatibility.getOrDefault(unicode, unicode);
        }

        public static String toHz(String input) {
            if (input.toUpperCase().startsWith("U+")) input = input.substring(2);
            int unicode = Integer.parseInt(input, 16);
            return toHz(unicode);
        }

        public static String toHz(int unicode) {
            unicode = getCompatibility(unicode);
            return String.valueOf(Character.toChars(unicode));
        }

        public static String toUnicodeHex(String hz) {
            int unicode = hz.codePointAt(0);
            return String.format("%04X", unicode);
        }

        public static String toUnicode(String hz) {
            return String.format("U+%s", toUnicodeHex(hz));
        }

        public static String getUnicodeExt(String hz) {
            int unicode = hz.codePointAt(0);
            String ext = "";
            if (unicode >= 0x3400 && unicode <= 0x4DBF) ext = "A";
            else if (unicode >= 0x20000 && unicode <= 0x2A6DF) ext = "B";
            else if (unicode >= 0x2A700 && unicode <= 0x2B73F) ext = "C";
            else if (unicode >= 0x2B740 && unicode <= 0x2B81F) ext = "D";
            else if (unicode >= 0x2B820 && unicode <= 0x2CEAF) ext = "E";
            else if (unicode >= 0x2CEB0 && unicode <= 0x2EBEF) ext = "F";
            else if (unicode >= 0x30000 && unicode <= 0x3134F) ext = "G";
            if (!TextUtils.isEmpty(ext)) ext = "擴" + ext;
            return ext;
        }
    }

    public static class MiddleChinese {
        private static final Map<String, String> mapInitials = new HashMap<>();
        private static final Map<String, String> mapFinals = new HashMap<>();
        private static final Map<String, String> mapSjep = new HashMap<>(); // 攝
        private static final Map<String, String> mapTongx = new HashMap<>();// 等
        private static final Map<String, String> mapHo = new HashMap<>();   // 呼
        private static final Map<Character, String> mapBiengSjyix = new HashMap<>();
        private static final Map<String, String[]> mapSms = new HashMap<>();
        private static final Map<String, String[]> mapYms = new HashMap<>();

        public static String canonicalize(String s) {
            // Replace apostrophes with zeros to make SQLite FTS happy
            return s.replace('\'', '0');
        }

        private static boolean isIY(String fin, int i) {
            return fin.charAt(i) == 'i' || fin.charAt(i) == 'y';
        }

        public static String display(String s, int system) {
            // Restore apostrophes
            s =  s.replace('0', '\'');
            if (system < 0) return HZ.isPY(s) ? String.format("%s(%s)", s,detail(s)) : s;
            if (system == 0) system = 5;
            // Get tone first
            int tone = 1;
            String s0 = s;
            switch (s.charAt(s.length() - 1)) {
                case 'x': tone = 2; s = s.substring(0, s.length() - 1); break;
                case 'h': tone = 3; s = s.substring(0, s.length() - 1); break;
                case 'd': tone = 3; break;
                case 'p': tone = 4; s = s.substring(0, s.length() - 1) + "m"; break;
                case 't': tone = 4; s = s.substring(0, s.length() - 1) + "n"; break;
                case 'k': tone = 4; s = s.substring(0, s.length() - 1) + "ng"; break;
            }

            // Split initial and final
            String init = null, fin = null;
            boolean extraJ = false;
            int p = s.indexOf('\'');
            if (p >= 0) {               // Abnormal syllables containing apostrophes
                init = s.substring(0, p);
                fin = s.substring(p + 1);
                if (init.equals("i")) init = "";
                if (!mapInitials.containsKey(init)) return null;    // Fail
                if (!mapFinals.containsKey(fin)) return null;       // Fail
            }
            else {
                for (int i = 3; i >= 0; i--) {
                    if (i <= s.length() && mapInitials.containsKey(s.substring(0, i))) {
                        init = s.substring(0, i);
                        fin = s.substring(i);
                        break;
                    }
                }
                if (Objects.requireNonNull(fin).equals("")) return null;        // Fail

                // Extract extra "j" in syllables that look like 重紐A類
                if (fin.charAt(0) == 'j') {
                    if (fin.length() < 2) return null;  // Fail
                    extraJ = true;
                    if (isIY(fin, 1)) {
                        fin = fin.substring(1);
                    }
                    else {
                        fin = "i" + fin.substring(1);
                    }
                }

                // Recover omitted glide in final
                if (Objects.requireNonNull(init).endsWith("r")) {       // 只能拼二等或三等韻，二等韻省略介音r
                    if (!isIY(fin, 0)) {
                        fin = "r" + fin;
                    }
                }
                else if (init.endsWith("j")) {  // 只能拼三等韻，省略介音i
                    if (!isIY(fin, 0)) {
                        fin = "i" + fin;
                    }
                }
            }
            if (!mapFinals.containsKey(fin)) return null;   // Fail

            // Distinguish 重韻
            switch (fin) {
                case "ia":          // 牙音声母爲戈韻，其餘爲麻韻
                    if (Arrays.asList("k", "kh", "g", "ng").contains(init)) {
                        fin = "Ia";
                    }
                    break;
                case "ieng":
                case "yeng":
                    // 脣牙喉音声母直接接-ieng,-yeng者及莊組爲庚韻，其餘爲清韻
                    if (Arrays.asList("p", "ph", "b", "m",
                            "k", "kh", "g", "ng",
                            "h", "gh", "q", "",
                            "cr", "chr", "zr", "sr", "zsr").contains(init) && !extraJ) {
                        fin = (fin.equals("ieng")) ? "Ieng" : "Yeng";
                    }
                    break;
                case "in":     // 莊組声母爲臻韻，其餘爲眞韻
                    if (Arrays.asList("cr", "chr", "zr", "sr", "zsr").contains(init)) {
                        fin = "In";
                    }
                    break;
                case "yn":     // 脣牙喉音声母直接接-yn者爲眞韻，其餘爲諄韻
                    if (Arrays.asList("p", "ph", "b", "m",
                            "k", "kh", "g", "ng",
                            "h", "gh", "q", "").contains(init) && !extraJ) {
                        fin = "Yn";
                    }
                    break;
            }

            // Resolve 重紐
            String dryungNriux = "";
            if ("支脂祭眞仙宵侵鹽".indexOf(Objects.requireNonNull(mapFinals.get(fin)).charAt(0)) >= 0 &&
                    Arrays.asList("p", "ph", "b", "m",
                            "k", "kh", "g", "ng",
                            "h", "gh", "q", "", "j").contains(init)) {
                dryungNriux = (extraJ || init.equals("j")) ? "A" : "B";
            }
            String ym = Objects.requireNonNull(mapYms.get(dryungNriux + fin))[system];
            if (tone == 4) {
                ym = ym.replace('m', 'p').replace('n', 't').replace('ŋ','k');
            }
            return String.format("%s(%s)", formatTone(Objects.requireNonNull(mapSms.get(init))[system] + ym, tone + "", DB.GY), detail(s0));
        }

        public static String detail(String s) {
            // Get tone first
            int tone = 0;
            switch (s.charAt(s.length() - 1)) {
                case 'x': tone = 1; s = s.substring(0, s.length() - 1); break;
                case 'h': tone = 2; s = s.substring(0, s.length() - 1); break;
                case 'd': tone = 2; break;
                case 'p': tone = 3; s = s.substring(0, s.length() - 1) + "m"; break;
                case 't': tone = 3; s = s.substring(0, s.length() - 1) + "n"; break;
                case 'k': tone = 3; s = s.substring(0, s.length() - 1) + "ng"; break;
            }

            // Split initial and final
            String init = null, fin = null;
            boolean extraJ = false;
            int p = s.indexOf('\'');
            if (p >= 0) {               // Abnormal syllables containing apostrophes
                init = s.substring(0, p);
                fin = s.substring(p + 1);
                if (init.equals("i")) init = "";
                if (!mapInitials.containsKey(init)) return null;    // Fail
                if (!mapFinals.containsKey(fin)) return null;       // Fail
            }
            else {
                for (int i = 3; i >= 0; i--) {
                    if (i <= s.length() && mapInitials.containsKey(s.substring(0, i))) {
                        init = s.substring(0, i);
                        fin = s.substring(i);
                        break;
                    }
                }
                if (Objects.requireNonNull(fin).equals("")) return null;        // Fail

                // Extract extra "j" in syllables that look like 重紐A類
                if (fin.charAt(0) == 'j') {
                    if (fin.length() < 2) return null;  // Fail
                    extraJ = true;
                    if (fin.charAt(1) == 'i' || fin.charAt(1) == 'y') {
                        fin = fin.substring(1);
                    }
                    else {
                        fin = "i" + fin.substring(1);
                    }
                }

                // Recover omitted glide in final
                if (Objects.requireNonNull(init).endsWith("r")) {       // 只能拼二等或三等韻，二等韻省略介音r
                    if (!isIY(fin, 0)) {
                        fin = "r" + fin;
                    }
                }
                else if (init.endsWith("j")) {  // 只能拼三等韻，省略介音i
                    if (!isIY(fin, 0)) {
                        fin = "i" + fin;
                    }
                }
            }
            if (!mapFinals.containsKey(fin)) return null;   // Fail

            // Distinguish 重韻
            switch (fin) {
                case "ia":          // 牙音声母爲戈韻，其餘爲麻韻
                    if (Arrays.asList("k", "kh", "g", "ng").contains(init)) {
                        fin = "Ia";
                    }
                    break;
                case "ieng":
                case "yeng":
                    // 脣牙喉音声母直接接-ieng,-yeng者及莊組爲庚韻，其餘爲清韻
                    if (Arrays.asList("p", "ph", "b", "m",
                            "k", "kh", "g", "ng",
                            "h", "gh", "q", "",
                            "cr", "chr", "zr", "sr", "zsr").contains(init) && !extraJ) {
                        fin = (fin.equals("ieng")) ? "Ieng" : "Yeng";
                    }
                    break;
                case "in":     // 莊組声母爲臻韻，其餘爲眞韻
                    if (Arrays.asList("cr", "chr", "zr", "sr", "zsr").contains(init)) {
                        fin = "In";
                    }
                    break;
                case "yn":     // 脣牙喉音声母直接接-yn者爲眞韻，其餘爲諄韻
                    if (Arrays.asList("p", "ph", "b", "m",
                            "k", "kh", "g", "ng",
                            "h", "gh", "q", "").contains(init) && !extraJ) {
                        fin = "Yn";
                    }
                    break;
            }

            // Resolve 重紐
            String dryungNriux = "";
            if ("支脂祭眞仙宵侵鹽".indexOf(Objects.requireNonNull(mapFinals.get(fin)).charAt(0)) >= 0 &&
                    Arrays.asList("p", "ph", "b", "m",
                                  "k", "kh", "g", "ng",
                                  "h", "gh", "q", "", "j").contains(init)) {
                dryungNriux = (extraJ || init.equals("j")) ? "A" : "B";
            }

            // Render details
            String mux = mapInitials.get(init);
            String sjep = mapSjep.get(fin);
            char yonh = Objects.requireNonNull(mapFinals.get(fin)).charAt(fin.endsWith("d") ? 0 : tone);
            String tongx = mapTongx.get(fin);
            String ho = mapHo.get(fin);
            String biengSjyix = mapBiengSjyix.get(yonh);

            return mux + sjep + yonh + dryungNriux + tongx + ho + " " + biengSjyix;
        }

        public static List<String> getAllTones(String s) {
            if (s == null || s.equals("")) return null;                 // Fail
            String base = s.substring(0, s.length() - 1);
            if (base.equals("")) return null;                           // Fail
            switch (s.charAt(s.length() - 1)) {
                case 'x': return Arrays.asList(s, base, base + "h");    // 上 -> 上,平,去
                case 'h': return Arrays.asList(s, base, base + "x");    // 去 -> 去,平,上
                case 'd': case 'p': case 't': case 'k':
                          return Collections.singletonList(s);                      // 次入、入 -> self
                default:  return Arrays.asList(s, s + "x", s + "h");    // 平 -> 平,上,去
            }
        }
    }

    public static class Mandarin {
        public static final int IPA = 0;
        public static final int PINYIN = 1;
        public static final int BOPOMOFO = 2;

        private static final Map<String, String> mapPinyin = new HashMap<>();
        private static final char[] vowels = {'a', 'o', 'e', 'ê', 'i', 'u', 'v', 'n', 'm'};

        private static final Map<String, String> mapFromBopomofoPartial = new HashMap<>();
        private static final Map<String, String> mapFromBopomofoWhole = new HashMap<>();
        private static final Map<Character, Character> mapFromBopomofoTone = new HashMap<>();
        private static final Map<String, String> mapToBopomofoPartial = new HashMap<>();
        private static final Map<String, String> mapToBopomofoWhole = new HashMap<>();
        private static final Map<Character, Character> mapToBopomofoTone = new HashMap<>();

        public static String canonicalize(String s) {
            // Input can be either pinyin or bopomofo
            if (s == null || s.length() == 0) return s;

            if (mapFromBopomofoPartial.containsKey(s.substring(0, 1)) ||
                mapFromBopomofoTone.containsKey(s.charAt(0))) {   // Bopomofo
                // Allow tones at either end
                char tone = '1';
                if (mapFromBopomofoTone.containsKey(s.charAt(0))) {
                    tone = mapFromBopomofoTone.get(s.charAt(0));
                    s = s.substring(1);
                }
                else if (mapFromBopomofoTone.containsKey(s.charAt(s.length() - 1))) {
                    tone = mapFromBopomofoTone.get(s.charAt(s.length() - 1));
                    s = s.substring(0, s.length() - 1);
                }
                if (s.length() == 0) return null;   // Fail
                if (mapFromBopomofoWhole.containsKey(s)) {
                    s = mapFromBopomofoWhole.get(s);
                }
                else if (mapFromBopomofoPartial.containsKey(s.substring(0, 1)) &&
                         mapFromBopomofoPartial.containsKey(s.substring(1))) {
                    s = mapFromBopomofoPartial.get(s.substring(0, 1)) +
                        mapFromBopomofoPartial.get(s.substring(1));
                    if (s.startsWith("jv") || s.startsWith("qv") || s.startsWith("xv")) {
                        s = s.charAt(0) + "u" + s.substring(2);
                    }
                }
                else {
                    return null;    // Fail
                }
                return s + (tone == '_' ? "" : tone);
            }
            else {  // Pinyin
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
                    }
                    else {
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
            }
            else {
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
                }
                else {                      // Find letter in this order: a,o,e,i,u,v,n,m
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
                    }
                    else {
                        sb.append(s.charAt(i));
                        if (t != '_') sb.append(mapPinyin.get("_" + t));
                    }
                }
                return sb.toString();
            case BOPOMOFO:
                if (mapToBopomofoWhole.containsKey(s)) {
                    s = mapToBopomofoWhole.get(s);
                }
                else {
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
                switch (tone) {
                case '2': case '3': case '4':
                    return s + mapToBopomofoTone.get(tone);
                case '_':
                    return mapToBopomofoTone.get(tone) + s;
                default:
                    return s;
                }
            default:
                return null;    // Fail
            }
        }

        public static String getIPA(String s, char tone) {
            s = s.replace("yu","v").replace("y","i").replace("ii","i")
                    .replace("v","y").replaceFirst("^([jqx])u", "$1y")
                    .replaceFirst("([zcs])i", "$1ɿ").replaceFirst("([zcs]h|r)i", "$1ʅ")
                    .replace("w","u").replace("uu","u")
                    .replace("un", "uen").replace("ui", "uei").replace("iu", "iou")
                    .replaceFirst("([iy])e$","$1ɛ").replace("ea", "ɛ").replaceFirst("e$", "ɤ").replaceFirst("e(ng)$", "ɤ$1").replace("er", "ɚ").replace("en", "ən")
                    .replace("ao", "au").replaceFirst("([iy])an$", "$1ɛn")
                    .replace("ong", "uŋ").replace("ng", "ŋ");
            s = s.replace("p", "pʰ").replace("t", "tʰ").replace("k", "kʰ")
                    .replace("b", "p").replace("d", "t").replace("g", "k")
                    .replace("zh", "tʂ").replace("ch", "tʂʰ").replace("sh", "ʂ").replace("r", "ɻ")
                    .replace("z", "ts").replace("c", "tsʰ")
                    .replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("h", "x");
            return formatTone(s, tone + "", DB.CMN);
        }

        public static List<String> getAllTones(String s) {
            if (s == null || s.equals("")) return null;     // Fail
            char tone = s.charAt(s.length() - 1);
            String base = s;
            if (tone >= '1' && tone <= '4') {
                base = s.substring(0, s.length() - 1);
            }
            else {
                tone = '_';
            }
            if (base.equals("")) return null;               // Fail

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

    public static class Cantonese {
        public static final int IPA= 0;
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

        private static final List<Map<String, String>> listInitials = new ArrayList<>();
        private static final List<Map<String, String>> listFinals = new ArrayList<>();
        private static final List<Map<String, String>> listInitialsR = new ArrayList<>();
        private static final List<Map<String, String>> listFinalsR = new ArrayList<>();

        public static String canonicalize(String s, int system) {
            // Convert from given system to Jyutping

            // Check for null or empty string
            if (s == null || s.length() == 0) return s;

            // Choose map first
            Map<String, String> mapInitials, mapFinals;
            int index;
            switch (system) {
                case JYUTPING:          return s;
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
            }
            else {
                tone = '_';
            }

            // Get final
            int p = 0;
            while (p < s.length() && !Objects.requireNonNull(mapFinals).containsKey(s.substring(p))) p++;
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
            switch (tone) {
                case '7': tone = '1'; break;
                case '8': tone = '3'; break;
                case '9': tone = '6'; break;
            }

            // In Yale, initial "y" is omitted if final begins with "yu"
            // If that happens, we need to put the initial "j" back in Jyutping
            if (system == YALE && Objects.requireNonNull(init).equals("") && Objects.requireNonNull(fin).startsWith("yu")) init = "j";

            return init + fin + (tone == '_' ? "" : tone);
        }

        public static String display(String s, int system) {
            // Convert from Jyutping to given system

            // Choose map first
            Map<String, String> mapInitials, mapFinals;
            int index;
            switch (system) {
                case JYUTPING:          return s;
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
            }
            else {
                tone = '_';
            }

            // Get final
            int p = 0;
            while (p < s.length() && !Objects.requireNonNull(mapFinals).containsKey(s.substring(p))) p++;
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
                switch (tone) {
                    case '1': tone = '7'; break;
                    case '3': tone = '8'; break;
                    case '6': tone = '9'; break;
                }
            }

            // IPA tones
            if (system == IPA) {
                if (isEnteringTone) {
                    switch (tone) {
                        case '1': tone = '7'; break;
                        case '3': tone = '8'; break;
                        case '6': tone = '9'; break;
                    }
                }
            }

            // In Yale, initial "y" is omitted if final begins with "yu"
            if (system == YALE && Objects.requireNonNull(init).equals("y") && Objects.requireNonNull(fin).startsWith("yu")) init = "";
            if (system == IPA) return formatTone(init + fin, tone + "", DB.HK);
            return init + fin + (tone == '_' ? "" : tone);
        }

        public static List<String> getAllTones(String s) {
            if (s == null || s.equals("")) return null;     // Fail
            char tone = s.charAt(s.length() - 1);
            String base = s;
            if (tone >= '1' && tone <= '6') {
                base = s.substring(0, s.length() - 1);
            }
            else {
                tone = '_';
            }
            if (base.equals("")) return null;               // Fail

            boolean isEnteringTone = "ptk".indexOf(base.charAt(base.length() - 1)) >= 0;
            List<String> result = new ArrayList<>();
            result.add(s);
            if (tone != '1')                    result.add(base + '1');
            if (tone != '2' && !isEnteringTone) result.add(base + '2');
            if (tone != '3')                    result.add(base + '3');
            if (tone != '4' && !isEnteringTone) result.add(base + '4');
            if (tone != '5' && !isEnteringTone) result.add(base + '5');
            if (tone != '6')                    result.add(base + '6');
            return result;
        }
    }

    public static class Minnan {
        private static final int IPA = 0;
        private static final int ROMAN = 1;
        public static String display(String s, int system) {
            if (system == ROMAN) return s;
            char tone = s.charAt(s.length() - 1);
            String base = s;
            if (tone >= '1' && tone <= '8') {
                base = s.substring(0, s.length() - 1);
            } else {
                tone = '0';
            }
            s = base;
            s = s.replace("oo", "ɔ").replaceFirst("o(k|ng)", "ɔ$1").replace("o", "ə");
            s = s.replaceFirst("^(p|t|k|ts)h", "$1ʰ").replace("ng", "ŋ").replace("j", "dz").replaceFirst("^g", "ɡ").replaceFirst("h$","ʔ").replace("nn","̃");
            s = formatTone(s, "" + tone, DB.TW);
            return s;
        }
    }

    public static class Tones {
        public static List<String> getAllTones(String s) {
            if (s == null || s.equals("")) return null;     // Fail
            char tone = s.charAt(s.length() - 1);
            String base = s;
            if ("123456789".indexOf(tone) >= 0) {
                base = s.substring(0, s.length() - 1);
            } else {
                if (tone == '0') base = s.substring(0, s.length() - 1);
                tone = '_';
            }
            if (base.equals("")) return null;               // Fail

            List<String> result = new ArrayList<>();
            result.add(s);
            if (tone != '1') result.add(base + '1');
            if (tone != '2') result.add(base + '2');
            if (tone != '3') result.add(base + '3');
            if (tone != '4') result.add(base + '4');
            if (tone != '5') result.add(base + '5');
            if (tone != '6') result.add(base + '6');
            if (tone != '7') result.add(base + '7');
            if (tone != '8') result.add(base + '8');
            if (tone != '9') result.add(base + '9');
            return result;
        }

        public static String display(String s, String lang) {
            if (TextUtils.isEmpty(s) || s.length() < 2) return s;
            if (Character.isDigit(s.charAt(0))) return s;
            Pattern pattern = Pattern.compile("^(.+)([0-9]{1,2}[a-z]?)$");
            Matcher matcher = pattern.matcher(s);
            if (matcher.matches()) {
                String tone = matcher.group(2);
                if (TextUtils.isEmpty(tone)) return s;
                String base = matcher.group(1);
                return formatTone(base, tone, lang);
            }
            return s;
        }
    }

    public static class Korean {
        public static final int IPA = 0;
        public static final int HANGUL = 1;
        public static final int ROMANIZATION = 2;   // This is the database representation

        public static final char FIRST_HANGUL = 0xAC00;
        public static final char LAST_HANGUL = 0xD7A3;

        // Arrays: index to spelling
        private static final String[] initials = {"g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h"};
        private static final String[] vowels = {"a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"};
        private static final String[] finals = {"", "k", "kk0", "ks0", "n", "nj0", "nh0", "d0", "l", "lg0", "lm0", "lb0", "ls0", "lt0", "lp0", "lh0", "m", "p", "bs0", "s0", "ss0", "ng", "j0", "ch0", "k0", "t0", "p0", "h0"};
            // Finals with "0" are not valid pronunciations of Chinese characters
            // And they won't match anything in the database
        // Maps: spelling to index
        private static final Map<String, Integer> mapInitials = new HashMap<>();
        private static final Map<String, Integer> mapVowels = new HashMap<>();
        private static final Map<String, Integer> mapFinals = new HashMap<>();

        public static boolean isHangul(char c) {
            return c >= FIRST_HANGUL && c <= LAST_HANGUL;
        }

        public static String canonicalize(String s) {
            // Input can be either a hangul, or non-canonicalized romanization
            if (s == null || s.length() == 0) return s;
            char unicode = s.charAt(0);
            if (isHangul(unicode)) {    // Hangul
                unicode -= FIRST_HANGUL;
                int z = unicode % finals.length;
                int x = unicode / finals.length;
                int y = x % vowels.length;
                x /= vowels.length;
                return initials[x] + vowels[y] + finals[z];
            }
            else {      // Romanization, do some obvious corrections
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
            if (system == ROMANIZATION) return s;

            int L = s.length();
            int x, y, z, p, q;
            // Initial
            p = 0;
            for (int i = 2; i > 0; i--) {
                if (i <= L && mapInitials.containsKey(s.substring(0, i))) {
                    p = i; break;
                }
            }
            x = mapInitials.get(s.substring(0, p));
            // Final
            q = L;
            for (int i = L - 2; i < L; i++) {
                if (i >= p && mapFinals.containsKey(s.substring(i))) {
                    q = i; break;
                }
            }
            z = mapFinals.get(s.substring(q));
            // Vowel
            if (!mapVowels.containsKey(s.substring(p, q))) return null; // Fail
            y = mapVowels.get(s.substring(p, q));
            return String.valueOf((char) (FIRST_HANGUL + (x * vowels.length + y) * finals.length + z));
        }
    }

    public static class Vietnamese {
        public static final int IPA = 0;
        public static final int OLD_STYLE = 1;
        public static final int NEW_STYLE = 2;

        private static final Map<String, String> map = new HashMap<>();

        public static String canonicalize(String s) {
            // Input can be either with diacritics, or non-canonicalized Telex string
            if (s == null || s.length() == 0) return s;
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
                }
                else {
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
                    }
                    else {
                        if (sb.charAt(i-1) != 'a' && sb.charAt(i-1) != 'u') sb.setCharAt(i, 'i');
                    }
                }
            }
            return sb.toString() + (tone == '_' ? "" : tone);
        }

        private static String getIPA(String s, char tone) {
            boolean isEnteringTone = "ptc".indexOf(s.charAt(s.length() - 1)) >= 0 ||
                    s.endsWith("ch");
            s = s.replace("b","ɓ").replace("dd", "ɗ").replace("d", "j")
                    .replace("ph", "f").replace("gi", "z").replaceFirst("^gh?", "ɣ")
                    .replace("s", "ʂ").replace("x", "s").replace("kh", "kʰ").replaceFirst("^[cq]([^h])", "k$1")
                    .replaceFirst("^nh", "ɲ").replace("ngh", "ŋ").replace("ng", "ŋ").replace("th", "tʰ")
                    .replaceFirst("^ch", "c").replace("tr", "ʈ");
            s = s.replace("nh", "ŋ̟").replaceFirst("c$", "k").replaceFirst("ch$", "k̟");
            s = s.replace("y", "i").replaceFirst("o([ae])","u$1")
                    .replace("aa", "ə").replace("aw", "ă").replace("a", "aː").replace("ă", "a")
                    .replace("ee", "ê").replace("e", "ɛ").replace("ê", "e")
                    .replace("uw", "ɨ").replace("ow", "əː").replace("oo", "ô").replace("o", "ɔ").replace("ô", "o")
                    .replace("ie", "iə").replaceFirst("([iuɨ])a$", "$1ə")
                    .replace("ɨəː", "ɨə").replace("uo", "uə").replaceFirst("([aːəeɛiɨ])[uɔ]$", "$1w");
            int index;
            if (isEnteringTone) {
                index = tone == 's' ? 7 : 8;
            } else {
                index = "_frxsj".indexOf(tone) + 1;
            }
            return formatTone(s, index + "", DB.VI);
        }

        // Rules for placing the tone marker follows this page in Vietnamese Wikipedia:
        // Quy tắc đặt dấu thanh trong chữ quốc ngữ
        public static String display(String s, int style) {
            // Get tone
            char tone = s.charAt(s.length() - 1);
            if ("rsfxj".indexOf(tone) >= 0) {
                s = s.substring(0, s.length() - 1);
            }
            else {
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
                String key = s.substring(p, p+2);
                if (map.containsKey(key + "_")) {
                    if (key.equals("dd") || p+4 <= s.length() && s.startsWith("uwow", p)) {
                        sb.append(map.get(key + "_"));      // Tone marker doesn't go here
                    }
                    else {
                        sb.append(map.get(key + tone));     // Tone marker goes here
                        tone = '_';
                    }
                    p += 2;
                }
                else {
                    sb.append(s.charAt(p++));
                }
            }
            if (tone == '_') return sb.toString();          // No tone marker to place

            // Place tone marker
            // Find first and last vowel
            p = 0; while (p < sb.length() && "aeiouy".indexOf(sb.charAt(p)) < 0) p++;
            if (p == sb.length()) return null;              // There has to be a vowel, otherwise fail
            int q = p + 1;  while (q < sb.length() && "aeiouy".indexOf(sb.charAt(q)) >= 0) q++;
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
            if (s == null || s.equals("")) return null;     // Fail
            char tone = s.charAt(s.length() - 1);
            String base = s;
            if ("rsfxj".indexOf(tone) >= 0) {
                base = s.substring(0, s.length() - 1);
            }
            else {
                tone = '_';
            }
            if (base.equals("")) return null;               // Fail

            boolean isEnteringTone = "ptc".indexOf(base.charAt(base.length() - 1)) >= 0 ||
                                     base.endsWith("ch");
            List<String> result = new ArrayList<>();
            result.add(s);
            if (tone != '_' && !isEnteringTone) result.add(base);
            if (tone != 'r' && !isEnteringTone) result.add(base + 'r');
            if (tone != 's')                    result.add(base + 's');
            if (tone != 'f' && !isEnteringTone) result.add(base + 'f');
            if (tone != 'x' && !isEnteringTone) result.add(base + 'x');
            if (tone != 'j')                    result.add(base + 'j');
            return result;
        }
    }

    public static class Japanese {
        public static final int IPA = 0;
        public static final int HIRAGANA = 1;
        public static final int KATAKANA = 2;
        public static final int NIPPON = 3;     // This is the database representation
        public static final int HEPBURN = 4;
        // Reference: Japanese Wikipedia ローマ字

        private static final Map<String, String> mapIPA = new HashMap<>();
        private static final Map<String, String> mapHiragana = new HashMap<>();
        private static final Map<String, String> mapKatakana = new HashMap<>();
        private static final Map<String, String> mapNippon = new HashMap<>();
        private static final Map<String, String> mapHepburn = new HashMap<>();

        public static String convertTo(String s, int system) {
            if (s == null || s.length() == 0) return s;

            // Choose map
            Map<String, String> map = null;
            switch (system) {
                case IPA:       map = mapIPA;       break;
                case HIRAGANA:  map = mapHiragana;  break;
                case KATAKANA:  map = mapKatakana;  break;
                case NIPPON:    map = mapNippon;    break;
                case HEPBURN:   map = mapHepburn;   break;
            }

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

            return sb.toString();
        }

        public static String canonicalize(String s) {
            return convertTo(s, NIPPON);
        }

        public static String display(String s, int system) {
            return (system == NIPPON) ? s : convertTo(s, system);
        }
    }

    // Initialization code
    public static void initialize(Resources resources) {
        if (initialized) return;

        InputStream inputStream;
        BufferedReader reader;
        String line;
        String[] fields;

        try {
            // Character compatibility variants
            inputStream = resources.openRawResource(R.raw.orthography_hz_compatibility);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                int c = line.codePointAt(0);
                HZ.compatibility.put(c, line.codePoints().toArray()[1]);
            }
            reader.close();

            // Middle Chinese
            inputStream = resources.openRawResource(R.raw.orthography_mc_initials);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\t");
                if (fields[0].equals("_")) fields[0] = "";
                MiddleChinese.mapInitials.put(fields[0], fields[1]);
                MiddleChinese.mapSms.put(fields[0], Arrays.copyOfRange(fields, 1, fields.length));
            }
            reader.close();

            inputStream = resources.openRawResource(R.raw.orthography_mc_finals);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\t");
                MiddleChinese.mapSjep.put(fields[0], fields[1]);
                MiddleChinese.mapTongx.put(fields[0], fields[2]);
                MiddleChinese.mapHo.put(fields[0], fields[3]);
                MiddleChinese.mapFinals.put(fields[0], fields[4]);
                MiddleChinese.mapYms.put(fields[0], Arrays.copyOfRange(fields, 4, fields.length));
            }
            reader.close();

            // Middle Chinese: 平水韻
            inputStream = resources.openRawResource(R.raw.orthography_mc_bieng_sjyix);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                for (int i = 0; i < fields[1].length(); i++) {
                    MiddleChinese.mapBiengSjyix.put(fields[1].charAt(i), fields[0]);
                }
            }
            reader.close();

            // Mandarin: Pinyin
            inputStream = resources.openRawResource(R.raw.orthography_pu_pinyin);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                Mandarin.mapPinyin.put(fields[0], fields[1] + fields[2]);
                Mandarin.mapPinyin.put(fields[1] + fields[2], fields[0]);
            }
            reader.close();

            // Mandarin: Bopomofo
            inputStream = resources.openRawResource(R.raw.orthography_pu_bopomofo);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                if ("234_".contains(fields[1])) {
                    Mandarin.mapFromBopomofoTone.put(fields[0].charAt(0), fields[1].charAt(0));
                    Mandarin.mapToBopomofoTone.put(fields[1].charAt(0), fields[0].charAt(0));
                }
                else {
                    Mandarin.mapFromBopomofoPartial.put(fields[0], fields[1]);
                    Mandarin.mapToBopomofoPartial.put(fields[1], fields[0]);
                    if (fields.length > 2) {
                        Mandarin.mapFromBopomofoWhole.put(fields[0], fields[2]);
                        Mandarin.mapToBopomofoWhole.put(fields[2], fields[0]);
                    }
                }
            }
            reader.close();

            // Cantonese
            for (int i = 0; i <= 3; i++) {
                Cantonese.listInitials.add(new HashMap<>());
                Cantonese.listInitialsR.add(new HashMap<>());
                Cantonese.listFinals.add(new HashMap<>());
                Cantonese.listFinalsR.add(new HashMap<>());
            }
            inputStream = resources.openRawResource(R.raw.orthography_ct_initials);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                for (int i = 0; i <= 3; i++) {
                    Cantonese.listInitials.get(i).put(fields[0], fields[i + 1]);
                    Cantonese.listInitialsR.get(i).put(fields[i + 1], fields[0]);
                }
            }
            reader.close();

            inputStream = resources.openRawResource(R.raw.orthography_ct_finals);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                for (int i = 0; i <= 3; i++) {
                    Cantonese.listFinals.get(i).put(fields[0], fields[i + 1]);
                    Cantonese.listFinalsR.get(i).put(fields[i + 1], fields[0]);
                }
            }
            reader.close();

            // Korean
            for (int i = 0; i < Korean.initials.length; i++) {
                Korean.mapInitials.put(Korean.initials[i], i);
            }
            for (int i = 0; i < Korean.vowels.length; i++) {
                Korean.mapVowels.put(Korean.vowels[i], i);
            }
            for (int i = 0; i < Korean.finals.length; i++) {
                Korean.mapFinals.put(Korean.finals[i], i);
            }

            // Vietnamese
            inputStream = resources.openRawResource(R.raw.orthography_vn);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                Vietnamese.map.put(fields[0], fields[1] + fields[2]);
                Vietnamese.map.put(fields[1] + fields[2], fields[0]);
            }
            reader.close();

            // Japanese
            inputStream = resources.openRawResource(R.raw.orthography_jp);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '#') continue;
                fields = line.split("\\s+");
                for (int i = 0; i < 4; i++) {
                    Japanese.mapHiragana.put(fields[i], fields[0]);
                    Japanese.mapKatakana.put(fields[i], fields[1]);
                    Japanese.mapNippon.put(fields[i], fields[2]);
                    Japanese.mapHepburn.put(fields[i], fields[3]);
                    Japanese.mapIPA.put(fields[i], fields[4]);
                }
            }
            reader.close();
        }
        catch (IOException ignored) {}

        initialized = true;
    }

    private static boolean initialized = false;
}
