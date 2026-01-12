package com.osfans.mcpdict.Orth;

import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.OpenCC;

public class HanZi {
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
                || (unicode >= 0x323B0 && unicode <= 0x3347F) // CJK Extension J
                || (unicode >= 0xF900 && unicode <= 0xFAFF)   // CJK Compatibility Ideographs
                || (unicode >= 0x2F800 && unicode <= 0x2FA1F); // CJK Compatibility Ideographs Supplement
    }

    public static boolean isHz(String hz) {
        if (TextUtils.isEmpty(hz)) return false;
        return isHz(hz.codePointAt(0));
    }

    public static String cp2str(int codePoint) {
        return Character.toString(codePoint);
    }

    public static String firstHz(String s) {
        if (TextUtils.isEmpty(s)) return "";
        int codePoint = s.codePointAt(0);
        return cp2str(codePoint);
    }

    public static String lastHz(String s) {
        if (TextUtils.isEmpty(s)) return "";
        int codePoint = Character.codePointBefore(s, s.length());
        return cp2str(codePoint);
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

    public static String toHz(String input) {
        if (input.toUpperCase().startsWith("U+")) input = input.substring(2);
        int unicode = Integer.parseInt(input, 16);
        return toHz(unicode);
    }

    public static String toHz(int unicode) {
        String hz = cp2str(unicode);
        return OpenCC.convert(hz, "c2u");
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
        if (unicode >= 0x3400 && unicode <= 0x4DB5) ext = "擴展A 3.0";
        else if (unicode >= 0x4DB6 && unicode <= 0x4DBF) ext = "擴展A 13.0";
        else if (unicode >= 0x20000 && unicode <= 0x2A6D6) ext = "擴展B 3.1";
        else if (unicode >= 0x2A6D7 && unicode <= 0x2A6DD) ext = "擴展B 13.0";
        else if (unicode >= 0x2A6DE && unicode <= 0x2A6DF) ext = "擴展B 14.0";
        else if (unicode >= 0x2A700 && unicode <= 0x2B734) ext = "擴展C 5.2";
        else if (unicode >= 0x2A735 && unicode <= 0x2B738) ext = "擴展C 14.0";
        else if (unicode == 0x2B739) ext = "擴展C 15.0";
        else if (unicode >= 0x2B73A && unicode <= 0x2B73F) ext = "擴展C 17.0";
        else if (unicode >= 0x2B740 && unicode <= 0x2B81F) ext = "擴展D 6.0";
        else if (unicode >= 0x2B820 && unicode <= 0x2CEA1) ext = "擴展E 8.0";
        else if (unicode >= 0x2CEA2 && unicode <= 0x2CEAF) ext = "擴展E 17.0";
        else if (unicode >= 0x2CEB0 && unicode <= 0x2EBEF) ext = "擴展F 10.0";
        else if (unicode >= 0x30000 && unicode <= 0x3134F) ext = "擴展G 13.0";
        else if (unicode >= 0x31350 && unicode <= 0x323AF) ext = "擴展H 15.0";
        else if (unicode >= 0x2EBF0 && unicode <= 0x2EE5F) ext = "擴展I 15.1";
        else if (unicode >= 0x323B0 && unicode <= 0x3347F) ext = "擴展J 17.0";
        else if (unicode >= 0x4E00 && unicode <= 0x9FA5) ext = "基本 1.1";
        else if (unicode >= 0x9FA6 && unicode <= 0x9FBB) ext = "基本 4.1";
        else if (unicode >= 0x9FBC && unicode <= 0x9FC3) ext = "基本 5.1";
        else if (unicode >= 0x9FC4 && unicode <= 0x9FCB) ext = "基本 5.2";
        else if (unicode == 0x9FCC) ext = "基本 6.1";
        else if (unicode >= 0x9FCD && unicode <= 0x9FD5) ext = "基本 8.0";
        else if (unicode >= 0x9FD6 && unicode <= 0x9FEA) ext = "基本 10.0";
        else if (unicode >= 0x9FEB && unicode <= 0x9FEF) ext = "基本 11.0";
        else if (unicode >= 0x9FF0 && unicode <= 0x9FFC) ext = "基本 13.0";
        else if (unicode >= 0x9FFD && unicode <= 0x9FFF) ext = "基本 14.0";
        else if (unicode >= 0xF900 && unicode <= 0xFA2D) ext = "兼容 1.1";
        else if (unicode >= 0xFA2E && unicode <= 0xFA2F) ext = "兼容 6.1";
        else if (unicode >= 0xFA30 && unicode <= 0xFA6A) ext = "兼容 3.2";
        else if (unicode >= 0xFA6B && unicode <= 0xFA6D) ext = "兼容 5.2";
        else if (unicode >= 0xFA70 && unicode <= 0xFAD9) ext = "兼容 4.1";
        else if (unicode >= 0x2F800 && unicode <= 0x2FA1D) ext = "兼容補充 3.1";
        return ext;
    }

    public static class Tokenizer implements MultiAutoCompleteTextView.Tokenizer {

        private boolean isEnd(int codePoint) {
            if (DB.isHzInputCode()) return true;
            return !isHz(codePoint);
        }

        public int findTokenStart(CharSequence text, int cursor) {
            if (DB.isHzInput()) return cursor;
            int i = cursor;
            boolean isHz = DB.isYinPrompt();
            if (isHz) {
                if (i > 1) {
                    int codePoint = Character.codePointBefore(text, i);
                    i -= Character.charCount(codePoint);
                } else {
                    i = 0;
                }
            }
            else {
                while (i > 0) {
                    int codePoint = Character.codePointAt(text, i - 1);
                    int n = Character.charCount(codePoint);
                    if (isEnd(codePoint)) {
                        i -= n;
                    }
                    else {
                        i += n - 1;
                        break;
                    }
                }
            }
            if (i < 0) i = 0;
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            if (DB.isHzInput()) return cursor;
            int i = cursor;
            int len = text.length();

            while (i < len) {
                int codePoint = Character.codePointAt(text, i);
                if (isEnd(codePoint)) {
                    return i;
                } else {
                    i += Character.charCount(codePoint);
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            return text;
        }
    }
}
