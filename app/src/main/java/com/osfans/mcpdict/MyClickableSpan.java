package com.osfans.mcpdict;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class MyClickableSpan extends ClickableSpan {
    String lang;
    public MyClickableSpan(String s) {
        super();
        lang = s;
    }
    @Override
    public void onClick(View v) {
        ResultAdapter.ViewHolder holder = (ResultAdapter.ViewHolder) ((View)v.getParent()).getTag();
        holder.col = lang;
        holder.onClick(holder.tvReadings);
    }

    @Override
    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
    }
}
