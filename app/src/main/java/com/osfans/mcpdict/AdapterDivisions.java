package com.osfans.mcpdict;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AdapterDivisions extends ArrayAdapter<CharSequence> {
    int mColor, mColorDim;

    public AdapterDivisions(@NonNull Context context, int resource) {
        super(context, resource);
        mColor = Utils.obtainColor(context, android.R.attr.textColorPrimary);
        mColorDim = context.getResources().getColor(R.color.dim, context.getTheme());
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        if (position == 0) {
            textView.setTextSize(16f);
            textView.setTextColor(mColor);
            return textView;
        }
        String s = getItem(position).toString();
        String last = s.replaceAll("([^-]+)-", "   ");
        int count = s.replaceAll("[^-]", "").length();
        textView.setTextSize(16f - count * 1.0f);
        textView.setText(last);
        textView.setTextColor(count > 0 ? mColorDim : mColor);
        return  textView;
    }
}
