package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;

public class Dongganyu {
    public static String display(String pys, String[] list) {
        StringBuilder sb = new StringBuilder();
        String[] ss = pys.split("/");
        int n = list.length;
        for (String system : list) {
            int i = 1 - Integer.parseInt(system);
            String s = ss[i];
            char tone = s.charAt(s.length() - 1);
            s = s.substring(0, s.length() - 1);
            s = Orthography.formatTone(s, tone + "", DB.DGY);
            sb.append(String.format(n > 1 && i == 0 ? "(%s)" : "%s", s));
            sb.append(" ");
        }
        return sb.toString();
    }
}
