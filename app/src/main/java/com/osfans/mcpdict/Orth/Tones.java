package com.osfans.mcpdict.Orth;

import android.text.TextUtils;

import com.osfans.mcpdict.DB;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class Tones {
    public static List<String> getAllTones(String s, String lang) {
        if (TextUtils.isEmpty(s)) return null;     // Fail
        JSONObject jsonObject = DB.getToneName(lang);
        if (jsonObject == null) return null;
        List<String> result = new ArrayList<>();
        result.add(s);
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String tone = it.next();
            result.add(s + tone);
        }
        return result;
    }

    public static boolean hasTone(String s) {
        Matcher matcher = Orthography.mPattern.matcher(s);
        return matcher.matches();
    }

    public static String display(String s, String lang) {
        if (TextUtils.isEmpty(s) || s.length() < 2) return s;
        if (Character.isDigit(s.charAt(0))) return s;
        Matcher matcher = Orthography.mPattern.matcher(s);
        if (matcher.matches()) {
            String tone = matcher.group(2);
            if (TextUtils.isEmpty(tone)) return s;
            String base = matcher.group(1);
            return Orthography.formatTone(base, tone, lang);
        }
        return s;
    }
}
