package com.osfans.mcpdict.custom;

import android.database.Cursor;
import android.database.MatrixCursor;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Util.Pref;
import java.util.*;

public final class CustomResults {
    private CustomResults() {}

    public static Cursor arrange(Cursor source) {
        if (source == null || Pref.getFilter() != DB.FILTER.CUSTOM) return source;
        try {
            Map<List<String>, Map<String, List<String[]>>> characters = new LinkedHashMap<>();
            for (source.moveToFirst(); !source.isAfterLast(); source.moveToNext()) {
                String[] row = new String[5];
                for (int i = 0; i < row.length; i++) row[i] = source.getString(i);
                characters.computeIfAbsent(Arrays.asList(row[0], row[4]), k -> new LinkedHashMap<>())
                        .computeIfAbsent(row[1], k -> new ArrayList<>()).add(row);
            }
            List<CustomSchemes.DisplayEntry> entries = CustomSchemes.visibleEntries();
            MatrixCursor result = new MatrixCursor(new String[]{"漢字", "語言", "讀音", "註釋", "variants",
                    "custom_entry", "custom_color", "custom_group"});
            for (Map<String, List<String[]>> languages : characters.values()) {
                for (CustomSchemes.DisplayEntry entry : entries) {
                    for (String[] row : languages.getOrDefault(entry.label(), Collections.emptyList())) {
                        result.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4],
                                entry.key(), entry.color(), entry.group()});
                    }
                }
            }
            return result;
        } finally { source.close(); }
    }
    public static String key(Cursor cursor) {
        int column = cursor.getColumnIndex("custom_entry");
        return column < 0 ? cursor.getString(DB.COL_LANG) : cursor.getString(column);
    }
    public static String color(Cursor cursor) {
        int column = cursor.getColumnIndex("custom_color");
        return column < 0 ? "" : cursor.getString(column);
    }
    public static String group(Cursor cursor) {
        int column = cursor.getColumnIndex("custom_group");
        return column < 0 ? "" : cursor.getString(column);
    }
}
