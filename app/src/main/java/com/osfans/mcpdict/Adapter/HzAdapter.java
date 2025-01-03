package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FontUtil;

public class HzAdapter extends CursorAdapter {

    public HzAdapter(Context context) {
        super(context, null, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView)view;
        FontUtil.setTypeface(tv);
        String text = String.format("%s  <small><span style='color: #808080;'>%s</span></small>", convertToString(cursor), getCode(cursor));
        Spanned ss = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
        tv.setText(ss);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return DB.getInputCursor(constraint.toString());
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(0);
    }

    private CharSequence getCode(Cursor cursor) {
        String value = cursor.getString(1);
        if (TextUtils.isEmpty(value)) return "";
        String lang = Pref.getShape();
        if (DB.isHzParts(lang)) value = value.replaceAll(" ", "");
        return value.replaceAll("\\{.*?\\}", "")
                .replace("|", " ")
                .replaceAll("[\t\r\n ]+", " ");
    }
}
