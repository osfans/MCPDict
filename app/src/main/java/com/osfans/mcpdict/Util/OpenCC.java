package com.osfans.mcpdict.Util;

import android.content.res.AssetManager;

import com.readystatesoftware.sqliteasset.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OpenCC {
    private static final String FOLDER = "opencc";
    private static final String[] CONFIGS = new String[] {
            "hk2s","hk2t","s2hk","s2t","s2tw","s2twp","t2hk","t2s","t2tw","tw2s","tw2sp","tw2t"
    };

    public static native String openCCLineConv(String input, String configFullPath);

    public static String convert(String input, String configFileName) {
        File openccDir = new File(App.getContext().getDataDir(), FOLDER);
        return openCCLineConv(input, new File(openccDir, configFileName + ".json").getAbsolutePath());
    }

    public static String[] convertAll(String input) {
        Set<String> set = new HashSet<>();
        set.add(input);
        for (String config: CONFIGS) {
            set.add(convert(input, config));
        }
        return set.toArray(new String[0]);
    }

    public static native void openCCDictConv(String src, String dest, boolean mode);

    static {
        System.loadLibrary("opencc");
    }

    public static void initOpenCC() {
        try {
            AssetManager assetManager = App.getContext().getAssets();
            String[] names = assetManager.list(FOLDER);
            assert names != null;
            File data = App.getContext().getDataDir();
            File opencc = new File(data, FOLDER);
            boolean ignore = opencc.mkdirs();
            for (String name: names) {
                InputStream is = assetManager.open(FOLDER + "/" + name);
                File dest = new File(opencc, name);
                String destName = dest.getAbsolutePath();
                Utils.writeExtractedFileToDisk(is, new FileOutputStream(destName));
                if (name.endsWith(".txt")) {
                    String ocdName = destName.replace(".txt", ".ocd2");
                    openCCDictConv(destName, ocdName, false);
                    dest.delete();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
