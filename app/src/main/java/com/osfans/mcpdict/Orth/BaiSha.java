package com.osfans.mcpdict.Orth;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;

public class BaiSha {
    public static final DisplayHelper displayHelper = new DisplayHelper() {
        public String displayOne(String s) {
            return BaiSha.display(s);
        }

        public boolean isIPA(char c) {
            return true;
        }
    };

    public static String display(String s) {
        return s.replace("．", ".").replace("－", "-").replace("（", "(").replace("）", ")").replace("［", "[").replace("］", "]").replace("〈", "<").replace("〉", ">");
    }

    public static String canonicalize(String s) {
        return s.replaceAll("[*~]", "").replace(".", "．").replace("-", "－").replace("(", "（").replace(")", "）").replace("[", "［").replace("]", "］").replace("<", "〈").replace(">", "〉").replaceAll(" .*$", "").replaceAll("（[^（）]*?）$", "");
    }
}
