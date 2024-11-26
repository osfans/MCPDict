package com.osfans.mcpdict.Orth;

import android.content.res.Resources;
import android.text.TextUtils;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Orthography {

    // One static inner class for each language.
    // All inner classes (except for HanZi) have the two following methods:
    //   public static String canonicalize(String s);
    //   public static String display(String s);
    // However, some may require an additional int argument specifying the format.
    // Inner classes for tonal languages also have the following method:
    //   public static List<String> getAllTones(String s);
    // which returns the given *canonicalized* syllable in all possible tones.
    // All methods return null on failure.

    private static int mToneStyle = 0;
    private static int mToneValueStyle = 0;
    public static final Pattern mPattern = Pattern.compile("^(.+?)([0-9]{1,2}[a-z]?)$");


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

    public static String formatRoman(String s) {
        return String.format("<i>%s</i>", s);
    }

    private static final String[] toneBars = {"ˀ˩˨˧˦˥ˀ", "ˀ꜖꜕꜔꜓꜒ˀ", "ˀ꜌꜋꜊꜉꜈ˀ", "ˀ꜑꜐꜏꜎꜍ˀ", "⁰¹²³⁴⁵⁶"};
    private static String formatToneBar(String s, int index) {
        if (TextUtils.isEmpty(s)) return "";
        if (s.length() == 2 && s.charAt(0) == s.charAt(1)) s = s.substring(1);
        for (int i = 0; i <= 6; i++)
            s = s.replace((char)('0' + i), toneBars[index].charAt(i));
        return s;
    }

    public static String formatTone(String base, String tone, String lang) {
        if (TextUtils.isEmpty(tone) || tone.contentEquals("_")) return base;
        JSONArray styles = null;
        try {
            JSONObject jsonObject = DB.getToneName(lang);
            styles = jsonObject.getJSONArray(tone);
        } catch (Exception ignored) {
        }
        if (styles == null || styles.length() != 5) {
            if (tone.contentEquals("0")) return base;
            return base + tone;
        }
        String tv = getJSONString(styles, 0);
        String style1 = getJSONString(styles, 1);
        if (mToneStyle == 5) mToneValueStyle = 1;
        if (!TextUtils.isEmpty(tv)) {
            if (mToneValueStyle == 0) { //符號
                if (tv.length() == 2 && tv.charAt(0) == tv.charAt(1)) tv = tv.substring(0, 1);
                if (tv.contains("-")) {
                    String [] tvs = tv.split("-");
                    tvs[0] = formatToneBar(tvs[0], 0);
                    tvs[1] = formatToneBar(tvs[1], style1.startsWith("0") ? 3 : 1);
                    tv = tvs[0] + tvs[1];
                } else if (style1.startsWith("0") && tv.length() == 1) {
                    tv = formatToneBar(tv, 2);
                } else {
                    tv = formatToneBar(tv, 0);
                }
            } else if (mToneValueStyle == 1) { //數字
                tv = formatToneBar(tv, 4).replace('-', '⁻');
            } else tv = "";
        }
        switch (mToneStyle) {
            case 6:
                return base + tv;
            case 0:
                return base + tv + tone;
            default:
                String sTone = getJSONString(styles, mToneStyle == 5 ? 1 : mToneStyle);
                if (TextUtils.isEmpty(sTone)) return base + tv;
                if (mToneStyle == 4 && !TextUtils.isEmpty(style1)) {
                    char a = style1.charAt(0);
                    if (a >= '1' && a <= '4') return sTone + base + tv;
                    else return base + tv + sTone;
                }
                if (mToneStyle <= 2) {
                    char a = sTone.charAt(0);
                    sTone = sTone.replace('0', '⓪').replace(a, (char)(a - '1' + '①'));
                    if (sTone.length() == 2) {
                        char b = sTone.charAt(1);
                        sTone = sTone.replace(b, (char)(b - 'a' + 'ⓐ'));
                    }
                    return base + tv + sTone;
                }
                if (mToneStyle == 5) {
                    char a = sTone.charAt(0);
                    sTone = sTone.replace(a, (char)(a - '0' + '₀'));
                    if (sTone.length() == 2) {
                        char b = sTone.charAt(1);
                        sTone = sTone.replace(b, (char)(b - 'a' + 'ⓐ'));
                    }
                    return base + sTone + tv;
                }
                return base + tv + sTone;
        }
    }

    private static boolean skip(String line) {
        return TextUtils.isEmpty(line) || line.charAt(0) == '#';
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
                HanZi.compatibility.put(c, line.codePoints().toArray()[1]);
            }
            reader.close();

            // Mandarin: Pinyin
            inputStream = resources.openRawResource(R.raw.orthography_pu_pinyin);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (skip(line)) continue;
                fields = line.split("\\s+");
                Mandarin.mapPinyin.put(fields[0], fields[1] + fields[2]);
                Mandarin.mapPinyin.put(fields[1] + fields[2], fields[0]);
            }
            reader.close();

            // Mandarin: Bopomofo
            inputStream = resources.openRawResource(R.raw.orthography_pu_bopomofo);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (skip(line)) continue;
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
                if (skip(line)) continue;
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
                if (skip(line)) continue;
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
                if (skip(line)) continue;
                fields = line.split("\\s+");
                Vietnamese.map.put(fields[0], fields[1] + fields[2]);
                Vietnamese.map.put(fields[1] + fields[2], fields[0]);
            }
            reader.close();

            // Japanese
            inputStream = resources.openRawResource(R.raw.orthography_jp);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                if (skip(line)) continue;
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
