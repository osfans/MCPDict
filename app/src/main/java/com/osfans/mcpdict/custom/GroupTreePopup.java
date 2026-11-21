package com.osfans.mcpdict.custom;

import android.view.*;
import android.widget.*;
import android.graphics.drawable.ColorDrawable;
import com.osfans.mcpdict.Util.FontUtil;
import java.util.*;
import java.util.function.Consumer;

/** Indented native dropdown shared by the language panel's group selectors. */
public final class GroupTreePopup {
    private GroupTreePopup() {}
    public static void show(View anchor, List<String> paths, String selected, Consumer<String> chosen, Runnable create) {
        show(anchor,paths,selected,chosen,create,false);
    }
    public static void show(View anchor,List<String> paths,String selected,Consumer<String> chosen,Runnable create,boolean counts) {
        show(anchor,paths,selected,chosen,create,counts,"全部分區");
    }
    public static void show(View anchor,List<String> paths,String selected,Consumer<String> chosen,Runnable create,boolean counts,String rootLabel) {
        var context=anchor.getContext();
        int unit=Math.max(1,Math.round(context.getResources().getDisplayMetrics().density));
        android.util.TypedValue background=new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground,background,true);
        LinearLayout body=new LinearLayout(context);body.setOrientation(LinearLayout.VERTICAL);body.setBackgroundColor(background.data);
        ListView list=new ListView(context);list.setDividerHeight(0);body.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        int maxWidth=context.getResources().getDisplayMetrics().widthPixels-64*unit;
        PopupWindow popup=new PopupWindow(body,Math.min(maxWidth,Math.max(anchor.getWidth(),160*unit)),-2,true);
        popup.setBackgroundDrawable(new ColorDrawable(background.data));popup.setOutsideTouchable(true);popup.setElevation(8*unit);
        list.setAdapter(new BaseAdapter(){
            public int getCount(){return paths.size();}
            public Object getItem(int p){return paths.get(p);}
            public long getItemId(int p){return p;}
            public View getView(int p,View recycled,android.view.ViewGroup parent){
                TextView text=recycled instanceof TextView?(TextView)recycled:(TextView)LayoutInflater.from(context).inflate(com.osfans.mcpdict.R.layout.spinner_item,parent,false);
                String path=paths.get(p);int depth=path.isEmpty()?0:path.split("／").length-1;
                text.setText((path.isEmpty()?rootLabel:path.substring(path.lastIndexOf('／')+1))+(counts?"（"+CustomSchemes.languageCount(path)+"）":""));
                text.setTextSize(Math.max(10,16-depth));text.setGravity(Gravity.CENTER_VERTICAL);text.setMinHeight(0);text.setMinimumHeight(0);text.setSingleLine(true);
                int padding=context.getResources().getDimensionPixelOffset(com.osfans.mcpdict.R.dimen.padding_size);
                text.setPadding(padding+Math.min(depth,10)*8*unit,padding,padding,padding);
                text.setContentDescription(path.isEmpty()?rootLabel:CustomSchemes.displayGroup(path));FontUtil.setTypeface(text);
                text.setTextColor(com.osfans.mcpdict.Util.App.obtainColor(context,depth==0?android.R.attr.textColorPrimary:android.R.attr.textColorSecondary));
                android.util.TypedValue color=new android.util.TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorAccent,color,true);
                text.setBackgroundColor(Objects.equals(path,selected)?(color.data&0xffffff)|0x22000000:android.graphics.Color.TRANSPARENT);
                return text;
            }
        });
        list.setOnItemClickListener((parent,view,index,id)->{popup.dismiss();chosen.accept(paths.get(index));});
        if(create!=null){Button add=new Button(context);add.setText("＋ 新建分區");add.setTextSize(13);body.addView(add,new LinearLayout.LayoutParams(-1,40*unit));add.setOnClickListener(v->{popup.dismiss();create.run();});}
        int contentWidth=160*unit,totalHeight=create==null?0:40*unit;
        for(int i=0;i<list.getAdapter().getCount();i++){
            View row=list.getAdapter().getView(i,null,list);row.measure(View.MeasureSpec.makeMeasureSpec(maxWidth,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
            contentWidth=Math.max(contentWidth,row.getMeasuredWidth()+8*unit);totalHeight+=row.getMeasuredHeight();
        }
        popup.setWidth(Math.min(maxWidth,Math.max(anchor.getWidth(),contentWidth)));
        popup.setHeight(Math.min(totalHeight,Math.max(80*unit,popup.getMaxAvailableHeight(anchor)-8*unit)));
        popup.showAsDropDown(anchor);
    }
}
