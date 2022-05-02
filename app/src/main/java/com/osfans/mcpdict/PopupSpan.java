package com.osfans.mcpdict;

import android.content.Context;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;

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
        DictApp.showDict(v.getContext(), text);
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
        ds.setColor(color);
    }
}
