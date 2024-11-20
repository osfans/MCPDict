package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;

public class LanguageAdapter extends CursorAdapter {

    public LanguageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String language = convertToString(cursor).toString();
        TextView tv = (TextView)view;
        tv.setText(language);
        tv.setBackgroundColor(DB.getColor(DB.getLabelByLanguage(language)));
        tv.setTextColor(Color.WHITE);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return DB.getLanguageCursor(constraint);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(0);
    }
}
