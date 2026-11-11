package com.osfans.mcpdict;
import java.util.*;
/** Compare characters within ONE language point. No reference language exists. */
public final class CharacterSoundComparison {
    private CharacterSoundComparison(){}
    public static String classify(List<Set<String>> values){
        if(values.size()<2)throw new IllegalArgumentException("請至少選擇兩個漢字");
        for(Set<String> value:values)if(value==null||value.isEmpty())return "缺資料";
        boolean equal=true;for(Set<String> value:values)if(!values.get(0).equals(value))equal=false;
        if(equal)return "全同";
        for(int i=0;i<values.size();i++)for(int j=i+1;j<values.size();j++)for(String value:values.get(i))if(values.get(j).contains(value))return "部分相同";
        return "全異";
    }
}
