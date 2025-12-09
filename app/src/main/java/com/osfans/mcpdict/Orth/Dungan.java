package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;

public class Dungan {
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Dungan.display(s, Pref.getToneStyles(R.string.pref_key_dgy_display));
        }

        private static boolean isCyrillic(char ch) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            return block == Character.UnicodeBlock.CYRILLIC
                || block == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY;
        }

        public boolean isIPA(char c) {
            return super.isIPA(c) || isCyrillic(c) || c == '/' || c == '(' || c == ')';
        }
    };

    public static String display(String pys, String[] list) {
        StringBuilder sb = new StringBuilder();
        String[] ss = pys.split("/");
        int n = list.length;
        for (String system : list) {
            int i = Integer.parseInt(system);
            String s = ss[i];
            char tone = s.charAt(s.length() - 1);
            s = s.substring(0, s.length() - 1);
            s = Orthography.formatTone(s, tone + "", DB.DGY);
            sb.append(String.format(n > 1 && i == 1 ? "(%s)" : "%s", s));
            sb.append(" ");
        }
        return sb.toString();
    }
}
