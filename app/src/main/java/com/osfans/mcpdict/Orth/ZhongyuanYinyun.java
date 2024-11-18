package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Utils;

public class ZhongyuanYinyun {
    public static String display(String pys, String[] list) {
        StringBuilder sb = new StringBuilder();
        String[] ss = pys.split("/");
        int n = list.length;
        String[] names = Utils.getStringArray(R.array.pref_entries_zyyy_display);
        for (String system : list) {
            int i = Integer.parseInt(system);
            String s = ss[i];
            char tone = s.charAt(s.length() - 1);
            s = s.substring(0, s.length() - 1);
            s = Orthography.formatTone(s, tone + "", DB.ZYYY);
            sb.append(s);
            String name = names[i].replace("（", "{").replace("）", "}");
            if (n > 1) sb.append(String.format("(%s)", name));
            sb.append(" ");
        }
        return sb.toString();
    }
}
