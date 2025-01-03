package com.osfans.mcpdict.UI;

import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;

public class ClickableSpan extends android.text.style.ClickableSpan {
    public ClickableSpan() {
        super();
    }

    @Override
    public void onClick(@NonNull View v) {
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
    }
}
