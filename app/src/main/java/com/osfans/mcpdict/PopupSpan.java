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
        Context context = v.getContext();
        TextView tv = new TextView(context);
        tv.setPadding(24, 24, 24, 24);
        if (DictApp.enableFontExt()) tv.setTypeface(DictApp.getDictTypeFace());
        tv.setTextIsSelectable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(text);
        new AlertDialog.Builder(context).setView(tv).show();
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
        ds.setColor(color);
    }
}
