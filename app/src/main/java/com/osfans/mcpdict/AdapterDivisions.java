package com.osfans.mcpdict;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AdapterDivisions extends ArrayAdapter<CharSequence> {
    int mColor, mColorDim;
    public AdapterDivisions(@NonNull Context context, int resource) {
        super(context, resource);
        mColor = getTextColorPrimary(context);
        mColorDim = context.getResources().getColor(R.color.dim, context.getTheme());
    }

    public int getTextColorPrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary,typedValue,false);
        int color = -1;
        try (TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{
                android.R.attr.textColorPrimary})) {
            color = arr.getColor(0, color);
        }
        return color;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        if (position == 0) {
            textView.setTextSize(16f);
            textView.setTextColor(mColor);
            return textView;
        }
        String[] divisions = DB.getDivisions();
        if (divisions != null && divisions.length > 0) {
            String s = divisions[position - 1];
            String last = s.replaceAll("([^-]+)-", "   ");
            int count = s.replaceAll("[^-]", "").length();
            textView.setTextSize(16f - count * 1.0f);
            textView.setText(last);
            textView.setTextColor(count > 0 ? mColorDim : mColor);
        }
        return  textView;
    }
}
