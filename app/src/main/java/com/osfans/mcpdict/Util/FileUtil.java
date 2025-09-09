package com.osfans.mcpdict.Util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

public class FileUtil {

    public static void copyFile(String srcPath, String dstPath) throws IOException {
        makeParentDirs(dstPath);
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        FileInputStream srcStream = new FileInputStream(srcFile);
        FileOutputStream dstStream = new FileOutputStream(dstFile);
        FileChannel srcChannel = srcStream.getChannel();
        FileChannel dstChannel = dstStream.getChannel();
        srcChannel.transferTo(0, srcChannel.size(), dstChannel);
        srcStream.close();
        dstStream.close();
    }

    public static void makeParentDirs(String path) {
        File parent = new File(path).getParentFile();
        if (parent != null && !parent.exists()) {
            boolean ignore = parent.mkdirs();
        }
    }

    public static void dumpException(String path, Throwable e) throws IOException {
        PrintWriter writer = new PrintWriter(path);
        e.printStackTrace(writer);
        writer.close();
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromAssets(String fileName, Context context) throws IOException {
        InputStream fin = context.getAssets().open("maps/" + fileName);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}
