package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

public class LanguageAdapter extends CursorAdapter {

    public LanguageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.custom_spinner_dropdown_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view).setText(convertToString(cursor));
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
