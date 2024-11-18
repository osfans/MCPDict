package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class HanZi {
    public static final Map<Integer, Integer> compatibility = new HashMap<>();

    public static boolean isUnknown(int unicode) {
        return unicode == 0x25A1; //□
    }

    public static boolean isUnknown(String hz) {
        return isUnknown(hz.codePointAt(0));
    }

    public static boolean isHz(int unicode) {
        return isUnknown(unicode) //□
                || unicode == 0x3007 //〇
                || (unicode >= 0x4E00 && unicode <= 0x9FFF)   // CJK Unified Ideographs
                || (unicode >= 0x3400 && unicode <= 0x4DBF)   // CJK Extension A
                || (unicode >= 0x20000 && unicode <= 0x2A6DF) // CJK Extension B
                || (unicode >= 0x2A700 && unicode <= 0x2B73F) // CJK Extension C
                || (unicode >= 0x2B740 && unicode <= 0x2B81F) // CJK Extension D
                || (unicode >= 0x2B820 && unicode <= 0x2CEAF) // CJK Extension E
                || (unicode >= 0x2CEB0 && unicode <= 0x2EBEF) // CJK Extension F
                || (unicode >= 0x30000 && unicode <= 0x3134F) // CJK Extension G
                || (unicode >= 0x31350 && unicode <= 0x323AF) // CJK Extension H
                || (unicode >= 0x2EBF0 && unicode <= 0x2EE5F) // CJK Extension I
                || (unicode >= 0xF900 && unicode <= 0xFAFF)   // CJK Compatibility Ideographs
                || (unicode >= 0x2F800 && unicode <= 0x2FA1F); // CJK Compatibility Ideographs Supplement
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
        else if (unicode >= 0x31350 && unicode <= 0x323AF) ext = "H";
        else if (unicode >= 0x2EBF0 && unicode <= 0x2EE5F) ext = "I";
        if (!TextUtils.isEmpty(ext)) ext = "擴" + ext;
        return ext;
    }
}
