package com.osfans.mcpdict;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class PopupSpan extends ClickableSpan {
    CharSequence text;
    int color;
    int lang;
    public PopupSpan(CharSequence s, int lang, int color) {
        super();
        text = s;
        this.color = color;
        this.lang = lang;
    }

    @Override
    public void onClick(View v) {
        Utils.showDict(v.getContext(), lang, text);
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
        ds.setColor(color);
    }
}
