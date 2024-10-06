package com.osfans.mcpdict;

import android.view.View;

import androidx.annotation.NonNull;

public class MenuSpan extends MyClickableSpan {
    Entry entry;
    public MenuSpan(Entry e) {
        super();
        entry = e;
    }

    @Override
    public void onClick(@NonNull View v) {
        ResultFragment resultFragment = (ResultFragment) v.getTag();
        resultFragment.setEntry(entry);
        resultFragment.openContextMenu(v);
    }
}
