package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.osfans.mcpdict.R;

import java.util.Objects;

public class StringArrayAdapter extends ArrayAdapter<CharSequence> {
    public StringArrayAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_item);
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
