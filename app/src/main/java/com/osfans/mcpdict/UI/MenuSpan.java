package com.osfans.mcpdict.UI;

import android.view.View;

import androidx.annotation.NonNull;

import com.osfans.mcpdict.Entry;
import com.osfans.mcpdict.ResultFragment;

public class MenuSpan extends ClickableSpan {
    Entry entry;
    public MenuSpan(Entry e) {
        super();
        entry = e;
    }

    @Override
    public void onClick(@NonNull View v) {
        ResultFragment resultFragment = (ResultFragment) v.getTag();
    }
}
