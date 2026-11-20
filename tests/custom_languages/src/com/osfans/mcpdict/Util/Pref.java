
package com.osfans.mcpdict.Util;
import java.util.*;
public class Pref {
    public enum ISLAND_HISTORY { NONE,ISLAND_ONLY,ISLAND_HIDE,HISTORY_ONLY,HISTORY_HIDE }
    public static ISLAND_HISTORY mode=ISLAND_HISTORY.NONE;
    public static class Memory {
        public final Map<String,Object> values=new HashMap<>();
        public String getString(String key,String fallback){return (String)values.getOrDefault(key,fallback);}
        public boolean getBoolean(String key,boolean fallback){return (boolean)values.getOrDefault(key,fallback);}
        public Memory edit(){return this;}
        public Memory putString(String key,String value){values.put(key,value);return this;}
        public Memory putBoolean(String key,boolean value){values.put(key,value);return this;}
        public boolean commit(){return true;}
        public void apply(){}
    }
    public static final Memory memory=new Memory();
    public static Memory get(){return memory;}
    public static String legacy="";
    public static String getStr(int key,String fallback){return key==1?legacy:fallback;}
    public static Set<String> getStrSet(int key){return new LinkedHashSet<>();}
    public static String getString(int key){return "預設方案";}
    public static boolean getBool(int key,boolean fallback){return fallback;}
    public static int schemeIndexToColor(int i){return 0xff123456;}
    public static ISLAND_HISTORY getIslandHistory(){return mode;}
    public static com.osfans.mcpdict.DB.FILTER getFilter(){return com.osfans.mcpdict.DB.FILTER.CUSTOM;}
}
