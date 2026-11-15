package com.osfans.mcpdict;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Groups readings independently of the application's tone display style. */
final class SankeyReading {
    private static final Pattern NOTE = Pattern.compile("\\{[^}]*\\}|〚[^〛]*〛|\\([^)]*\\)");
    private static final Pattern SYLLABLE_SEPARATOR = Pattern.compile("[\\s\\p{Z}·‧.]+|(?<=[\\p{L}\\p{M}\\p{N}])-+(?=\\p{L})");
    private static final Pattern TONE_CODE = Pattern.compile("^.+?([0-9]{1,2}[a-z=]?)$");

    private SankeyReading() {}

    static List<String> syllables(String reading) {
        String phonetic = NOTE.matcher(reading).replaceAll("").trim();
        List<String> result = new ArrayList<>();
        for (String part : SYLLABLE_SEPARATOR.split(phonetic)) {
            String syllable = part.replaceAll("^[*`]+|[*`+?@]+$", "");
            if (syllable.codePoints().anyMatch(Character::isLetter)) result.add(syllable);
        }
        return result;
    }

    static String tone(String rawSyllable, String displayedSyllable, Map<String, String> toneNames) {
        // Match the full code, so 2a and 2b stay separate even if they share a tone name.
        Matcher matcher = TONE_CODE.matcher(rawSyllable);
        if (matcher.matches() && !Character.isDigit(rawSyllable.charAt(0))) {
            String code = matcher.group(1);
            return toneNames.getOrDefault(code, code);
        }
        return PhoneticParts.parse(displayedSyllable).tone;
    }
}
