package com.osfans.mcpdict.UI;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;

/** Same PopupSpan implementation as the list header, including nested dictionary links. */
public final class CharacterDetails {
    public static void show(Context context,String hz) {
        TextView text=new TextView(context);text.setTextAppearance(R.style.FontDetail);
        FontUtil.setTypeface(text);text.setTextIsSelectable(true);
        int padding=Math.round(16*context.getResources().getDisplayMetrics().density);
        text.setPadding(padding,padding,padding,padding);
        int color=App.obtainColor(context,android.R.attr.textColorLink);
        SpannableStringBuilder links=new SpannableStringBuilder();
        Cursor cursor=DB.getDictCursor(hz);
        if(cursor!=null)try {
            if(cursor.moveToFirst()) {
                links.append(" "+HanZi.toUnicode(hz)+" ",new PopupSpan(DisplayHelper.formatPopUp(hz,DB.COL_HZ,DB.getUnicode(cursor)),DB.COL_HZ,color),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for(int i=DB.COL_FIRST_DICT;i<=DB.COL_LAST_DICT;i++) {
                    String value=cursor.getString(i);
                    if(value!=null&&!value.isEmpty()) links.append(" "+DB.getLabel(i)+" ",new PopupSpan(DisplayHelper.formatPopUp(hz,i,value),i,color),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } finally {cursor.close();}
        links.append(" 地圖 ",new PopupSpan(hz,0,color){@Override public void onClick(@NonNull View view){new MapView(context,hz).show();}},Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setText(links);text.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(context).setTitle(hz).setView(text).setPositiveButton(R.string.ok,null).show();
    }
}
