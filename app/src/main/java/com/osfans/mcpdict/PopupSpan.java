package com.osfans.mcpdict;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class PopupSpan extends ClickableSpan {
    CharSequence text;
    int color;
    public PopupSpan(CharSequence s, int color) {
        super();
        text = s;
        this.color = color;
    }

    @Override
    public void onClick(View v) {
        Utils.showDict(v.getContext(), text);
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
        ds.setColor(color);
    }
}
