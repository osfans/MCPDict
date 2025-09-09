package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;

import java.util.Objects;

public class DivisionAdapter extends ArrayAdapter<CharSequence> {
    int mColor, mColorDim;

    public DivisionAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_item);
        mColor = App.obtainColor(context, android.R.attr.textColorPrimary);
        mColorDim = context.getResources().getColor(R.color.dim, context.getTheme());
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        if (position == 0) {
            textView.setTextSize(16f);
            textView.setTextColor(mColor);
            return textView;
        }
        String s = Objects.requireNonNull(getItem(position)).toString();
        String last = s.replaceAll(String.format("([^%s]+)%s", FS, FS), "   ");
        int count = s.length() - s.replace(FS, "").length();
        textView.setTextSize(16f - count * 1.0f);
        textView.setText(last);
        textView.setTextColor(count > 0 ? mColorDim : mColor);
        return  textView;
    }

    @Override
    public int getPosition(@Nullable CharSequence item) {
        if (TextUtils.isEmpty(item)) return 0;
        int index = super.getPosition(item);
        if (index >= 0) return index;
        for (int i = 0; i < getCount(); i++) {
            CharSequence cs = getItem(i);
            if (Objects.requireNonNull(cs).toString().startsWith(item + " ")) return i;
        }
        return index;
    }
}
