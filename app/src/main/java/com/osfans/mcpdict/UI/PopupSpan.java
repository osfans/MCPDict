package com.osfans.mcpdict.UI;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.osfans.mcpdict.Util.App;

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
        App.showDict(v.getContext(), lang, text);
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
        ds.setColor(color);
    }
}
