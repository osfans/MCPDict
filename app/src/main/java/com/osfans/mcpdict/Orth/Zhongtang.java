package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;

public class Zhongtang {
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return Zhongtang.display(s, Pref.getToneStyles(R.string.pref_key_zt_display));
        }

        public boolean isIPA(char c) {
            return super.isIPA(c) || c == '/';
        }
    };

    public static String display(String pys, String[] list) {
        StringBuilder sb = new StringBuilder();
        String[] ss = pys.split("/");
        int n = list.length;
        String[] names = Pref.getStringArray(R.array.pref_entries_zt_display);
        for (String system : list) {
            int i = Integer.parseInt(system);
            String s = ss[i];
            char tone = s.charAt(s.length() - 1);
            s = s.substring(0, s.length() - 1);
            s = Orthography.formatTone(s, tone + "", DB.ZT);
            sb.append(s);
            if (n > 1) sb.append(String.format("(%s)", names[i]));
            sb.append(" ");
        }
        return sb.toString();
    }
}
