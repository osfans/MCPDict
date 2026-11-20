package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.database.MatrixCursor;
import java.util.*;

/** Independent list rows; the original cursor remains available to the comparison table. */
public final class ListReadings {
    private ListReadings() {}
    private static final String MERGED="comment_merged";
    public static boolean isMerged(Cursor cursor) {
        if(cursor==null)return false;
        int column=cursor.getColumnIndex(MERGED);return column>=0&&"1".equals(cursor.getString(column));
    }
    public static Cursor copy(Cursor source) { return copy(source,false); }
    public static Cursor copy(Cursor source,boolean mergeComments) { return copy(source,mergeComments,Collections.emptyList()); }
    public static Cursor copy(Cursor source,boolean mergeComments,List<String> languageOrder) {
        if(source==null)return null;
        String[] columns=source.getColumnNames();
        String[] targetColumns=mergeComments?Arrays.copyOf(columns,columns.length+1):columns;
        if(mergeComments)targetColumns[columns.length]=MERGED;
        MatrixCursor result=new MatrixCursor(targetColumns);
        Map<String,List<Object[]>> grouped=new LinkedHashMap<>();
        int membership=source.getColumnIndex("custom_entry");
        int language=source.getColumnIndex("語言"),reading=source.getColumnIndex("讀音");
        int previous=source.getPosition();
        try {
            for(source.moveToFirst();!source.isAfterLast();source.moveToNext()) {
                String lang=language<0?null:source.getString(language);
                String ipa=reading<0?null:source.getString(reading);
                // A character-only row has no language label and must retain its heading/links.
                if(lang!=null&&!lang.isEmpty()&&reading>=0&&(ipa==null||ipa.trim().isEmpty()))continue;
                Object[] values=new Object[targetColumns.length];
                for(int i=0;i<columns.length;i++)values[i]=source.getString(i);
                if(mergeComments) {
                    values[columns.length]="1";
                    String key=membership>=0?source.getString(membership):lang;
                    grouped.computeIfAbsent(key==null?"":key,k->new ArrayList<>()).add(values);
                } else result.addRow(values);
            }
            if(mergeComments) {
                Map<String,Integer> ranks=new HashMap<>();
                for(int i=0;i<languageOrder.size();i++)ranks.putIfAbsent(languageOrder.get(i),i);
                List<String> keys=new ArrayList<>(grouped.keySet());
                keys.sort(Comparator.comparingInt(key->ranks.getOrDefault(key,Integer.MAX_VALUE)));
                for(String key:keys)for(Object[] row:grouped.get(key))result.addRow(row);
            }
            return result;
        } finally { source.moveToPosition(previous); }
    }
}
