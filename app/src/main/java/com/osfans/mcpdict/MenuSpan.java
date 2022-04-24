package com.osfans.mcpdict;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class MenuSpan extends MyClickableSpan {
    Entry entry;
    public MenuSpan(Entry e) {
        super();
        entry = e;
    }

    @Override
    public void onClick(View v) {
        ResultFragment resultFragment = (ResultFragment) v.getTag();
        resultFragment.setEntry(entry);
        resultFragment.openContextMenu(v);
    }
}
