package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.osfans.mcpdict.Pref;

import java.util.Set;

public class MultiLanguageAdapter extends LanguageAdapter {
    View.OnClickListener onClick = null;

    public MultiLanguageAdapter(Context context) {
        super(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String language = convertToString(cursor).toString();
        Set<String> set = Pref.getCustomLanguages();
        CheckedTextView tv = (CheckedTextView)view;
        tv.setText(language);
        tv.setChecked(set.contains(language));
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
