package com.osfans.mcpdict.UI;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class MyClickableSpan extends ClickableSpan {
    public MyClickableSpan() {
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
