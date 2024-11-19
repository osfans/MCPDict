package com.osfans.mcpdict.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.osfans.mcpdict.R;

public class StringArrayAdapter extends ArrayAdapter<CharSequence> {
    public StringArrayAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_item);
    }
}
