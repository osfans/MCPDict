package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

public class MultiLanguageAdapter extends LanguageAdapter {
    int mColorHighlight;

    public MultiLanguageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mColorHighlight = Utils.obtainColor(context, android.R.attr.textColorHighlight);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String language = convertToString(cursor).toString();
        Set<String> set = Utils.getStrSet(R.string.pref_key_custom_languages);
        TextView tv = (TextView)view;
        tv.setText(language);
        tv.setTextColor(DB.getColor(DB.getLabelByLanguage(language)));
        tv.setBackgroundColor(set.contains(language) ? mColorHighlight : Color.TRANSPARENT);
    }
}
