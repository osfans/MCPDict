package com.osfans.mcpdict;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

public class MultiLanguageAdapter extends LanguageAdapter {
    int mColorHighlight;
    View.OnClickListener onClick = null;

    public MultiLanguageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mColorHighlight = Utils.obtainColor(context, android.R.attr.textColorHighlight);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String language = convertToString(cursor).toString();
        Set<String> set = Utils.getCustomLanguages();
        TextView tv = (TextView)view;
        tv.setText(language);
        tv.setBackgroundColor(set.contains(language) ? mColorHighlight : Color.TRANSPARENT);
    }

    public void setOnItemClickListener(View.OnClickListener onClick) {
        this.onClick = onClick;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);
         setCustomOnClick(v, position);
         return v;
    }

    private void setCustomOnClick(final View view, final int position){
        view.setTag(position);
        view.setOnClickListener(v -> {
            if(onClick==null)
                return;
            onClick.onClick(v);
            notifyDataSetChanged();
        });
    }
}
