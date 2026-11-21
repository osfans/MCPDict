package com.osfans.mcpdict.custom;

import android.content.Context;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import java.util.*;

/** Group-only dropdown for the first selector. Schemes have their own activity. */
public final class SchemeGroupPopup {
    public static PopupWindow show(View anchor,Runnable changed){return new SchemeGroupPopup(anchor,changed,null).window;}
    public static PopupWindow show(View anchor,Runnable changed,Runnable beforeDialog){return new SchemeGroupPopup(anchor,changed,beforeDialog).window;}
    private PopupWindow window;private final Runnable beforeDialog;
    private final Context context; private final Runnable changed;
    private final List<String> groups=new ArrayList<>();
    private final Rows groupRows=new Rows();
    private TextView allGroups;private Button addGroup,renameGroup,deleteGroup,moveGroup;
    private String movingSource,parentSelection;
    private ItemTouchHelper groupDrag;private String pendingBranch,pendingParent,pendingSibling;
    private int dp(int n){return SchemeColorPicker.dp(context,n);}
    private SchemeGroupPopup(View anchor,Runnable changed,Runnable beforeDialog){
        context=anchor.getContext();this.changed=changed;this.beforeDialog=beforeDialog;
        LinearLayout root=column();root.setPadding(dp(8),dp(4),dp(8),dp(4));
        android.util.TypedValue color=new android.util.TypedValue();context.getTheme().resolveAttribute(android.R.attr.colorBackground,color,true);root.setBackgroundColor(color.data);
        allGroups=new TextView(context);allGroups.setText("全部分區（"+CustomSchemes.languageCount("")+"）");allGroups.setTextSize(14);allGroups.setGravity(Gravity.CENTER_VERTICAL);allGroups.setPadding(dp(8),0,0,0);
        root.addView(allGroups,new LinearLayout.LayoutParams(-1,dp(36)));allGroups.setOnClickListener(v->{
            if(movingSource!=null){parentSelection="";refresh();return;}
            CustomSchemes.setScope("");refresh();changed.run();
        });
        RecyclerView groupList=list(root,groupRows);groupDrag=attach(groupList,groupRows);
        LinearLayout actions=new LinearLayout(context);root.addView(actions,new LinearLayout.LayoutParams(-1,dp(36)));
        Button add=button("新建分區");addGroup=add;renameGroup=button("改名");deleteGroup=button("刪除");moveGroup=button("移動");
        actions.addView(add,actionParams(1));actions.addView(renameGroup,actionParams(1));actions.addView(moveGroup,actionParams(1));actions.addView(deleteGroup,actionParams(1));
        add.setOnClickListener(v->{
            if(movingSource!=null){finishMoveMode();return;}
            closeForDialog();CustomLanguagePopup.promptGroup(context,name->{refresh();changed.run();});
        });
        renameGroup.setOnClickListener(v->{
            if(movingSource==null){rename();return;}
            String parent=CustomSchemes.parentGroup(movingSource);
            if(parent.isEmpty()){Toast.makeText(context,"目前已在頂層",Toast.LENGTH_SHORT).show();return;}
            moveTo(movingSource,CustomSchemes.parentGroup(parent));
        });
        deleteGroup.setOnClickListener(v->{if(movingSource!=null)moveTo(movingSource,"");else delete();});
        moveGroup.setOnClickListener(v->{if(movingSource!=null)moveTo(movingSource,parentSelection);else move();});
        int width=context.getResources().getDisplayMetrics().widthPixels-dp(40);
        PopupWindow popup=new PopupWindow(root,width,-2,true);window=popup;popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(color.data));popup.setElevation(dp(8));popup.setOutsideTouchable(true);
        popup.setHeight(Math.max(dp(180),popup.getMaxAvailableHeight(anchor)-dp(24)));
        int[] position=new int[2];anchor.getLocationOnScreen(position);refresh();popup.showAsDropDown(anchor,dp(20)-position[0],0);
    }
    private void closeForDialog(){if(window!=null)window.dismiss();if(beforeDialog!=null)beforeDialog.run();}
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
    private void refresh(){
        boolean moving=movingSource!=null;
        allGroups.setText(moving?"頂層（不設父分區）":"全部分區（"+CustomSchemes.languageCount("")+"）");
        groups.clear();groups.addAll(CustomSchemes.groups());
        selectionBackground(allGroups,moving?"".equals(parentSelection):CustomSchemes.scope().isEmpty());groupRows.notifyDataSetChanged();
        boolean selected=groups.contains(CustomSchemes.scope());
        addGroup.setText(moving?"取消移動":"新建分區");renameGroup.setText(moving?"提升一級":"改名");
        moveGroup.setText(moving?"確定移動":"移動");deleteGroup.setText(moving?"設為頂層":"刪除");
        renameGroup.setEnabled(moving?!CustomSchemes.parentGroup(movingSource).isEmpty():selected);
        deleteGroup.setEnabled(moving||selected);moveGroup.setEnabled(moving||selected);
    }
    private void finishMoveMode(){movingSource=null;parentSelection=null;refresh();}
    private void rename(){
        closeForDialog();
        String old=CustomSchemes.scope();EditText name=new EditText(context);name.setSingleLine();name.setText(CustomSchemes.displayGroup(old));name.selectAll();
        AlertDialog dialog=new AlertDialog.Builder(context).setTitle("分區改名").setView(name).setNegativeButton("取消",null).setPositiveButton("儲存",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(!CustomSchemes.renameGroup(old,name.getText().toString().trim())){name.setError("名稱空白、重複或與既有分區衝突");return;}dialog.dismiss();refresh();changed.run();
        }));dialog.show();
    }
    private void delete(){closeForDialog();String name=CustomSchemes.scope();new AlertDialog.Builder(context).setTitle("刪除分區")
        .setMessage("刪除分區「"+CustomSchemes.displayGroup(name)+"」及其子分區內的語言選擇？")
        .setNegativeButton("取消",null).setPositiveButton("刪除",(d,w)->{CustomSchemes.deleteGroup(name);refresh();changed.run();}).show();}
    private void move(){
        String source=CustomSchemes.scope();if(!groups.contains(source))return;
        movingSource=source;parentSelection=CustomSchemes.parentGroup(source);
        refresh();
    }
    private void moveTo(String source,String parent){
        if(!CustomSchemes.reparentGroup(source,parent)){
            Toast.makeText(context,"無法移動：目標已有同名分區或形成循環",Toast.LENGTH_SHORT).show();return;
        }
        movingSource=null;parentSelection=null;refresh();changed.run();
    }
    private ItemTouchHelper attach(RecyclerView list,Rows rows) {
        ItemTouchHelper helper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0) {
            public boolean isLongPressDragEnabled(){return false;}
            public boolean onMove(RecyclerView recycler,RecyclerView.ViewHolder from,RecyclerView.ViewHolder to){
                int a=from.getBindingAdapterPosition(),b=to.getBindingAdapterPosition();
                if(movingSource!=null||a<0||b<0||a>=groups.size()||b>=groups.size()||a==b)return false;
                String moving=groups.get(a),target=groups.get(b);
                if(target.startsWith(moving+"／"))return false;
                if(!CustomSchemes.parentGroup(moving).equals(CustomSchemes.parentGroup(target)))return false;
                pendingBranch=moving;pendingSibling=target;pendingParent=null;
                // Reorder siblings only; moving to another parent is an explicit action.
                return true;
            }
            public boolean canDropOver(RecyclerView recycler,RecyclerView.ViewHolder current,RecyclerView.ViewHolder target){
                int a=current.getBindingAdapterPosition(),b=target.getBindingAdapterPosition();
                if(movingSource!=null||a<0||b<0||a>=groups.size()||b>=groups.size())return false;
                String source=groups.get(a),destination=groups.get(b);
                int depth=source.split("／").length,next=destination.split("／").length;
                return !destination.startsWith(source+"／")&&CustomSchemes.parentGroup(source).equals(CustomSchemes.parentGroup(destination));
            }
            public void onSwiped(RecyclerView.ViewHolder holder,int direction){}
            public void clearView(RecyclerView recycler,RecyclerView.ViewHolder holder){
                super.clearView(recycler,holder);
                String branch=pendingBranch,parent=pendingParent,sibling=pendingSibling;
                pendingBranch=null;pendingParent=null;pendingSibling=null;
                if(branch==null)return;
                recycler.post(()->{
                    if(window==null||!window.isShowing())return;
                    try {
                        boolean moved;
                        if(sibling!=null){
                            List<String> order=new ArrayList<>(CustomSchemes.groups());
                            moved=CustomSchemes.moveGroupBranch(order,order.indexOf(branch),order.indexOf(sibling));
                            if(moved)CustomSchemes.reorderGroups(order);
                        }else moved=CustomSchemes.reparentGroup(branch,parent);
                        if(!moved)Toast.makeText(context,"無法移動：目標已有同名分區或形成循環",Toast.LENGTH_SHORT).show();
                    }catch(IllegalArgumentException error){Toast.makeText(context,"分區清單已改變，請重新選擇目標",Toast.LENGTH_SHORT).show();}
                    refresh();changed.run();
                });
            }
        });helper.attachToRecyclerView(list);return helper;
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
        List<String> names() { return groups; }
        public Row onCreateViewHolder(ViewGroup parent,int type) { return new Row(); }
        public int getItemCount() { return names().size(); }
        public void onBindViewHolder(Row row,int position) {
            String value=names().get(position);
            int split=value.lastIndexOf('／');
            row.name.setText(value.substring(split+1)+"（"+CustomSchemes.languageCount(value)+"）");
            int depth=value.split("／").length-1;
            row.itemView.setPadding(dp(Math.min(depth,6)*12),0,0,0);
            row.name.setTextSize(Math.max(10,14-depth));
            row.name.setContentDescription(CustomSchemes.displayGroup(value));
            boolean allowed=movingSource==null||(!value.equals(movingSource)&&!value.startsWith(movingSource+"／"));
            row.itemView.setAlpha(allowed?1f:.35f);row.handle.setEnabled(movingSource==null);
            selectionBackground(row.itemView,value.equals(movingSource==null?CustomSchemes.scope():parentSelection));
            row.itemView.setFocusable(true);
            row.itemView.setContentDescription("分區："+(CustomSchemes.displayGroup(value)));
            row.itemView.setOnClickListener(v->{
                if(!allowed)return;
                if(movingSource!=null){parentSelection=CustomSchemes.normalizeGroup(value);refresh();return;}
                CustomSchemes.setScope(value);
                refresh(); changed.run();
            });
            row.name.setOnClickListener(v->row.itemView.performClick());
            row.name.setOnLongClickListener(v->{if(movingSource!=null)return false;CustomSchemes.setScope(value);refresh();changed.run();move();return true;});
            row.handle.setContentDescription("按住拖動調整同級順序；更換父分區請選擇移動");
            row.handle.setOnTouchListener((v,event)->{ if(movingSource==null&&event.getActionMasked()==MotionEvent.ACTION_DOWN) groupDrag.startDrag(row); return false; });
        }
    }
}
