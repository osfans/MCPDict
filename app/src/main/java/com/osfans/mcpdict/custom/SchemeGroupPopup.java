package com.osfans.mcpdict.custom;

import android.content.Context;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import java.util.*;

/** Scheme selection and its groups are edited side by side. */
public final class SchemeGroupPopup {
    public static void show(View anchor, Runnable changed) { new SchemeGroupPopup(anchor, changed); }
    private final Context context;
    private final Runnable changed;
    private final List<String> schemes = new ArrayList<>(), groups = new ArrayList<>();
    private final Rows schemeRows = new Rows(true), groupRows = new Rows(false);
    private final TextView allGroups;
    private ItemTouchHelper schemeDrag, groupDrag;
    private Button renameGroup, deleteGroup, deleteScheme;
    private int dp(int n) { return SchemeColorPicker.dp(context,n); }
    private SchemeGroupPopup(View anchor, Runnable changed) {
        Context context=anchor.getContext(); this.context=context; this.changed=changed;
        LinearLayout body=new LinearLayout(context); body.setOrientation(LinearLayout.HORIZONTAL);
        body.setPadding(dp(8),dp(4),dp(8),dp(4));
        LinearLayout left=column(), right=column();
        body.addView(left,actionParams(1));
        View divider=new View(context); divider.setBackgroundColor(0x33808080);
        LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(dp(1),-1); line.setMargins(dp(4),0,dp(4),0); body.addView(divider,line);
        body.addView(right,actionParams(1));
        heading(left,"方案"); heading(right,"分區");
        allGroups=new TextView(context); allGroups.setText("全部分區"); allGroups.setTextSize(13); allGroups.setGravity(Gravity.CENTER_VERTICAL); allGroups.setPadding(dp(8),0,dp(8),0); allGroups.setFocusable(true);
        allGroups.setOnClickListener(v->{ CustomSchemes.setScope(""); refresh(); changed.run(); });
        right.addView(allGroups,new LinearLayout.LayoutParams(-1,dp(36)));
        RecyclerView schemeList=list(left,schemeRows), groupList=list(right,groupRows);
        schemeDrag=attach(schemeList,schemeRows); groupDrag=attach(groupList,groupRows);
        Button addScheme=button("新建方案");
        addScheme.setOnClickListener(v->CustomLanguagePopup.promptScheme(context,name->{ refresh(); changed.run(); }));
        Button addGroup=button("新建分區");
        addGroup.setOnClickListener(v->CustomLanguagePopup.promptGroup(context,name->{ refresh(); changed.run(); }));
        LinearLayout schemeActions = new LinearLayout(context), groupActions = new LinearLayout(context);
        left.addView(schemeActions,new LinearLayout.LayoutParams(-1,dp(36)));
        right.addView(groupActions,new LinearLayout.LayoutParams(-1,dp(36)));
        Button renameScheme=button("改名"); deleteScheme=button("刪除");
        renameGroup=button("改名"); deleteGroup=button("刪除");
        schemeActions.addView(addScheme,actionParams(1.25f));
        groupActions.addView(addGroup,actionParams(1.25f));
        schemeActions.addView(renameScheme,actionParams(1)); schemeActions.addView(deleteScheme,actionParams(1));
        groupActions.addView(renameGroup,actionParams(1)); groupActions.addView(deleteGroup,actionParams(1));
        renameScheme.setOnClickListener(v->rename(true)); renameGroup.setOnClickListener(v->rename(false));
        deleteScheme.setOnClickListener(v->delete(true)); deleteGroup.setOnClickListener(v->delete(false));
        int height=Math.min(dp(400),(int)(context.getResources().getDisplayMetrics().heightPixels*.6));
        LinearLayout root=column();
        android.util.TypedValue background=new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground,background,true); root.setBackgroundColor(background.data);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=new LinearLayout(context); footer.setGravity(Gravity.END);
        Button done=button("完成"); done.setTextSize(14); footer.addView(done,new LinearLayout.LayoutParams(dp(64),dp(40)));
        root.addView(footer,new LinearLayout.LayoutParams(-1,dp(40)));
        int width=context.getResources().getDisplayMetrics().widthPixels-dp(24);
        PopupWindow popup=new PopupWindow(root,width,height,true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(background.data));
        popup.setElevation(dp(8)); popup.setOutsideTouchable(true);
        done.setOnClickListener(v->popup.dismiss());
        int[] position=new int[2]; anchor.getLocationOnScreen(position);
        refresh(); popup.showAsDropDown(anchor,dp(12)-position[0],0);
    }
    private void selectionBackground(View view, boolean selected) {
        android.util.TypedValue accent=new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent,accent,true);
        int tint=accent.data & 0x00ffffff;
        android.graphics.drawable.ColorDrawable fill=new android.graphics.drawable.ColorDrawable(selected ? tint | 0x2e000000 : android.graphics.Color.TRANSPARENT);
        view.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(tint | 0x33000000),fill,null));
        view.setSelected(selected);
    }
    private LinearLayout column() { LinearLayout view=new LinearLayout(context); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private void heading(LinearLayout column,String text) { TextView view=new TextView(context); view.setText(text); view.setTextSize(14); view.setGravity(Gravity.CENTER_VERTICAL); column.addView(view,new LinearLayout.LayoutParams(-1,dp(32))); }
    private Button button(String text) { Button view=new Button(context); view.setText(text); view.setTextSize(13); view.setMinWidth(0); view.setMinimumWidth(0); view.setMinHeight(0); view.setMinimumHeight(0);
        view.setPadding(0,0,0,0); view.setSingleLine(true);
        android.util.TypedValue ripple=new android.util.TypedValue(); context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);
        if (text.equals("完成")) view.setBackgroundResource(ripple.resourceId);
        else {
            android.graphics.drawable.GradientDrawable gray=new android.graphics.drawable.GradientDrawable();
            gray.setColor(android.graphics.Color.rgb(224,224,224));
            android.graphics.drawable.RippleDrawable background=new android.graphics.drawable.RippleDrawable(
                    android.content.res.ColorStateList.valueOf(0x33000000),gray,null);
            view.setBackground(background);
            view.setTextColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled},new int[]{}},
                    new int[]{android.graphics.Color.rgb(33,33,33),android.graphics.Color.rgb(158,158,158)}));
        }
        return view; }
    private LinearLayout.LayoutParams actionParams(float weight) {
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-1,weight);params.setMargins(dp(2),dp(2),dp(2),dp(2));return params;
    }
    private RecyclerView list(LinearLayout column,Rows rows) {
        RecyclerView view=new RecyclerView(context); view.setLayoutManager(new LinearLayoutManager(context)); view.setAdapter(rows); view.setVerticalScrollBarEnabled(true);
        column.addView(view,new LinearLayout.LayoutParams(-1,0,1)); return view;
    }
    private void refresh() {
        schemes.clear(); schemes.addAll(Arrays.asList(CustomSchemes.names()));
        groups.clear(); groups.addAll(CustomSchemes.groups());
        selectionBackground(allGroups,CustomSchemes.scope().isEmpty());
        schemeRows.notifyDataSetChanged(); groupRows.notifyDataSetChanged();
        boolean groupSelected = groups.contains(CustomSchemes.scope()) && !CustomSchemes.isUnclassifiedGroup(CustomSchemes.scope());
        renameGroup.setEnabled(groupSelected); deleteGroup.setEnabled(groupSelected);
        deleteScheme.setEnabled(schemes.size()>1);
    }
    private void rename(boolean forScheme) {
        String old = forScheme ? CustomSchemes.currentName() : CustomSchemes.scope();
        EditText name = new EditText(context); name.setSingleLine(); name.setText(forScheme ? old : CustomSchemes.displayGroup(old)); name.selectAll();
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(forScheme ? "方案改名" : "分區改名").setView(name)
                .setNegativeButton("取消",null).setPositiveButton("儲存",null).create();
        dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String value=name.getText().toString().trim();
            boolean saved=forScheme?CustomSchemes.rename(value):CustomSchemes.renameGroup(old,value);
            if(!saved) { name.setError("名稱空白、重複或與既有分區衝突"); return; }
            dialog.dismiss(); refresh(); changed.run();
        })); dialog.show();
    }
    private void delete(boolean forScheme) {
        String name = forScheme ? CustomSchemes.currentName() : CustomSchemes.scope();
        String message = forScheme ? "刪除方案「"+name+"」及其全部自選記錄？"
                : "刪除分區「"+CustomSchemes.displayGroup(name)+"」及其子分區內的語言選擇？其他分區會保留。";
        new AlertDialog.Builder(context).setTitle(forScheme?"刪除方案":"刪除分區").setMessage(message)
                .setNegativeButton("取消",null).setPositiveButton("刪除",(d,w)->{
                    if(forScheme) CustomSchemes.delete(); else CustomSchemes.deleteGroup(name);
                    refresh(); changed.run();
                }).show();
    }
    private ItemTouchHelper attach(RecyclerView list,Rows rows) {
        ItemTouchHelper helper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0) {
            public boolean isLongPressDragEnabled() { return false; }
            public boolean onMove(RecyclerView recycler,RecyclerView.ViewHolder from,RecyclerView.ViewHolder to) {
                int a=from.getBindingAdapterPosition(),b=to.getBindingAdapterPosition(); if(a<0||b<0) return false;
                List<String> names=rows.names();
                if(rows.forSchemes){names.add(b,names.remove(a));rows.notifyItemMoved(a,b);return true;}
                String moving=names.get(a);int size=1;while(a+size<names.size()&&names.get(a+size).startsWith(moving+"／"))size++;
                if(!CustomSchemes.moveGroupBranch(names,a,b))return false;
                int target=names.indexOf(moving);
                if(a<target){for(int i=size-1;i>=0;i--)rows.notifyItemMoved(a+i,target+i);}
                else {for(int i=0;i<size;i++)rows.notifyItemMoved(a+i,target+i);}
                return true;
            }
            public boolean canDropOver(RecyclerView recycler,RecyclerView.ViewHolder current,RecyclerView.ViewHolder target) {
                if(rows.forSchemes)return true;
                int a=current.getBindingAdapterPosition(),b=target.getBindingAdapterPosition();
                return a>=0&&b>=0&&a<groups.size()&&b<groups.size()&&CustomSchemes.parentGroup(groups.get(a)).equals(CustomSchemes.parentGroup(groups.get(b)));
            }
            public void onSwiped(RecyclerView.ViewHolder holder,int direction) {}
            public void clearView(RecyclerView recycler,RecyclerView.ViewHolder holder) {
                super.clearView(recycler,holder);
                try {
                    if(rows.forSchemes) CustomSchemes.reorderSchemes(new ArrayList<>(schemes));
                    else CustomSchemes.reorderGroups(new ArrayList<>(groups));
                    changed.run();
                } catch (IllegalArgumentException error) { refresh(); }
            }
        }); helper.attachToRecyclerView(list); return helper;
    }
    private class Row extends RecyclerView.ViewHolder {
        final TextView name; final TextView handle;
        Row() {
            super(new LinearLayout(context)); LinearLayout row=(LinearLayout)itemView; row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutParams(new RecyclerView.LayoutParams(-1,-2)); row.setMinimumHeight(dp(36));
            name=new TextView(context); name.setTextSize(13); name.setMaxLines(2); name.setEllipsize(TextUtils.TruncateAt.END); name.setMinHeight(0); name.setPadding(dp(8),dp(4),0,dp(4));
            row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            handle=new TextView(context); handle.setText("☰"); handle.setTextSize(20); handle.setGravity(Gravity.CENTER);
            row.addView(handle,new LinearLayout.LayoutParams(dp(28),dp(36)));
        }
    }
    private class Rows extends RecyclerView.Adapter<Row> {
        final boolean forSchemes;
        Rows(boolean schemes) { forSchemes=schemes; }
        List<String> names() { return forSchemes?schemes:groups; }
        public Row onCreateViewHolder(ViewGroup parent,int type) { return new Row(); }
        public int getItemCount() { return names().size(); }
        public void onBindViewHolder(Row row,int position) {
            String value=names().get(position);
            int split=value.lastIndexOf('／');
            row.name.setText(forSchemes?value:value.substring(split+1));
            int depth=forSchemes?0:value.split("／").length-1;
            row.itemView.setPadding(dp(Math.min(depth,6)*12),0,0,0);
            row.name.setContentDescription(forSchemes?value:CustomSchemes.displayGroup(value));
            selectionBackground(row.itemView,value.equals(forSchemes?CustomSchemes.currentName():CustomSchemes.scope()));
            row.itemView.setFocusable(true);
            row.itemView.setContentDescription((forSchemes?"方案：":"分區：")+(forSchemes?value:CustomSchemes.displayGroup(value)));
            row.itemView.setOnClickListener(v->{
                if(forSchemes) CustomSchemes.select(value); else CustomSchemes.setScope(value);
                refresh(); changed.run();
            });
            row.handle.setContentDescription(forSchemes?"按住拖動調整方案順序":"按住拖動調整同級分區順序，子分區會一起移動");
            row.handle.setOnTouchListener((v,event)->{ if(event.getActionMasked()==MotionEvent.ACTION_DOWN) (forSchemes?schemeDrag:groupDrag).startDrag(row); return false; });
        }
    }
}
