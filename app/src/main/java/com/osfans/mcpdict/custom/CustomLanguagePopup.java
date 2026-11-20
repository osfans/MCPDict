package com.osfans.mcpdict.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.Util.FontUtil;
import java.util.*;

/** Two-column language dropdown: ordered selections on the left, search and add on the right. */
public final class CustomLanguagePopup {
    private final Context context;
    private final Runnable changed;
    private final CustomSchemes.Scheme scheme = CustomSchemes.current();
    private final List<CustomSchemes.Entry> selected = new ArrayList<>();
    private final List<String> candidates = new ArrayList<>();
    private final String[] languages = Optional.ofNullable(DB.getLanguages()).orElse(new String[0]);
    private final RecyclerView chosenList;
    private final RecyclerView availableList;
    private final TextView heading;
    private final ItemTouchHelper drag;
    private String query = "";
    private String activeGroup = CustomSchemes.scope().isEmpty() || CustomSchemes.scope().equals(CustomSchemes.ALL_SCOPE)
            ? null : CustomSchemes.normalizeGroup(CustomSchemes.scope());
    private final View groupSwatch;
    private final CheckBox palette;

    public static void show(View anchor, Runnable changed) { new CustomLanguagePopup(anchor, changed); }
    private int dp(int n) { return SchemeColorPicker.dp(context,n); }
    private CustomLanguagePopup(View anchor, Runnable changed) {
        context=anchor.getContext(); this.changed=changed;
        LinearLayout body=new LinearLayout(context); body.setOrientation(LinearLayout.HORIZONTAL); body.setPadding(dp(6),dp(4),dp(6),dp(4));
        android.util.TypedValue background=new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground,background,true); body.setBackgroundColor(background.data);
        LinearLayout left=new LinearLayout(context); left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout right=new LinearLayout(context); right.setOrientation(LinearLayout.VERTICAL);
        body.addView(left,new LinearLayout.LayoutParams(0,-1,1.15f));
        View divider=new View(context); divider.setBackgroundColor(0x33808080);
        LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(dp(1),-1); dividerParams.setMargins(dp(4),0,dp(4),0);
        body.addView(divider,dividerParams);
        body.addView(right,new LinearLayout.LayoutParams(0,-1,1));
        heading=new TextView(context); heading.setTextSize(12); heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setMaxLines(2); heading.setEllipsize(TextUtils.TruncateAt.END);
        heading.setOnClickListener(v -> chooseGroup());
        LinearLayout groupHeader = new LinearLayout(context); groupHeader.setGravity(Gravity.CENTER_VERTICAL);
        groupHeader.addView(heading,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout groupColor = new LinearLayout(context); groupColor.setGravity(Gravity.CENTER);
        groupSwatch = new View(context); groupColor.addView(groupSwatch,new LinearLayout.LayoutParams(dp(18),dp(18)));
        groupColor.setContentDescription("設定此分區顏色");
        groupHeader.addView(groupColor,new LinearLayout.LayoutParams(dp(28),-1));
        groupColor.setOnClickListener(v -> {
            String path = activeGroup == null ? "" : activeGroup;
            SchemeColorPicker.showOverall(context,(activeGroup == null ? scheme.name : CustomSchemes.displayGroup(path)) + (activeGroup == null ? " · 方案顏色" : " · 分區顏色"),
                    scheme, activeGroup, () -> { changed.run(); refresh(); });
        });
        left.addView(groupHeader,new LinearLayout.LayoutParams(-1,dp(48)));
        chosenList=new RecyclerView(context); chosenList.setLayoutManager(new LinearLayoutManager(context)); chosenList.setAdapter(new SelectedRows());
        chosenList.setVerticalScrollBarEnabled(true);
        left.addView(chosenList,new LinearLayout.LayoutParams(-1,0,1));
        EditText search=new EditText(context); search.setSingleLine(); search.setTextSize(14); search.setHint("搜尋語言");
        right.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));
        availableList=new RecyclerView(context); availableList.setLayoutManager(new LinearLayoutManager(context)); availableList.setAdapter(new AvailableRows());
        availableList.setVerticalScrollBarEnabled(true);
        right.addView(availableList,new LinearLayout.LayoutParams(-1,0,1));
        drag=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0) {
            public boolean isLongPressDragEnabled() { return false; }
            public boolean onMove(RecyclerView view,RecyclerView.ViewHolder from,RecyclerView.ViewHolder to) {
                int a=from.getBindingAdapterPosition(),b=to.getBindingAdapterPosition();
                if(a<0||b<0) return false;
                CustomSchemes.Entry moving=selected.get(a), target=selected.get(b);
                int old=scheme.entries.indexOf(moving), dest=scheme.entries.indexOf(target);
                scheme.entries.add(dest,scheme.entries.remove(old));
                selected.add(b,selected.remove(a)); view.getAdapter().notifyItemMoved(a,b); return true;
            }
            public void onSwiped(RecyclerView.ViewHolder holder,int direction) {}
            public void clearView(RecyclerView view,RecyclerView.ViewHolder holder) { super.clearView(view,holder); save(); }
        }); drag.attachToRecyclerView(chosenList);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { query=s.toString().trim().toLowerCase(Locale.ROOT); refresh(); }
            public void afterTextChanged(Editable value) {}
        });
        int width=anchor.getResources().getDisplayMetrics().widthPixels-dp(24);
        int height=Math.min(dp(480),(int)(anchor.getResources().getDisplayMetrics().heightPixels*.65));
        LinearLayout root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(background.data);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        palette=new CheckBox(context);palette.setText("使用自選配色");palette.setTextSize(12);
        palette.setChecked(Pref.getBool(R.string.pref_key_custom_language_color_by_scheme,false));
        palette.setOnCheckedChangeListener((button,on)->{
            if(Pref.getBool(R.string.pref_key_custom_language_color_by_scheme,false)!=on){Pref.putBool(R.string.pref_key_custom_language_color_by_scheme,on);changed.run();}
        });root.addView(palette,new LinearLayout.LayoutParams(-1,dp(40)));
        PopupWindow popup=new PopupWindow(root,width,height,true); popup.setBackgroundDrawable(new ColorDrawable(background.data));
        popup.setElevation(dp(8)); popup.setOutsideTouchable(true); popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        body.setFocusableInTouchMode(true); body.requestFocus();
        int[] location=new int[2]; anchor.getLocationOnScreen(location);
        popup.showAsDropDown(anchor,dp(12)-location[0],0);
        refresh();
    }
    private boolean inActiveGroup(CustomSchemes.Entry entry) {
        return activeGroup == null || entry.group().equals(activeGroup)
                || !activeGroup.isEmpty() && entry.group().startsWith(activeGroup + "／");
    }
    private void chooseGroup() {
        androidx.appcompat.widget.PopupMenu menu = new androidx.appcompat.widget.PopupMenu(context,heading);
        menu.getMenu().add("全部分區").setOnMenuItemClickListener(item -> {
            activeGroup=null; CustomSchemes.setScope(""); changed.run(); refresh(); return true;
        });
        for(String name:CustomSchemes.groups()) menu.getMenu().add(CustomSchemes.displayGroup(name)).setOnMenuItemClickListener(item -> {
            activeGroup=CustomSchemes.normalizeGroup(name); CustomSchemes.setScope(name); changed.run(); refresh(); return true;
        });
        menu.getMenu().add("＋ 新建分區").setOnMenuItemClickListener(item -> {
            promptGroup(context,path -> {
                // The helper has already saved the new group; retain the local record edits.
                scheme.groups.add(path); CustomSchemes.normalizeSubgroups(scheme); activeGroup=path; changed.run(); refresh();
            }); return true;
        });
        menu.show();
    }
    static void promptScheme(Context context, java.util.function.Consumer<String> created) {
        EditText name = new EditText(context); name.setSingleLine(); name.setHint("方案名稱");
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("新建方案").setView(name)
                .setNegativeButton("取消", null).setPositiveButton("建立", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = name.getText().toString().trim();
            if (!CustomSchemes.create(value)) { name.setError("請填寫尚未存在的方案名稱"); return; }
            dialog.dismiss(); created.accept(value);
        }));
        dialog.show();
    }
    static void promptGroup(Context context,java.util.function.Consumer<String> created) {
        EditText name = new EditText(context); name.setSingleLine(); name.setHint("分區名稱，可用 - 分級");
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("新建分區").setView(name)
                .setNegativeButton("取消",null).setPositiveButton("建立",null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String path=CustomSchemes.normalizeGroup(name.getText().toString());
            if(!CustomSchemes.createGroup(path)) { name.setError("請填寫尚未存在的分區名稱"); return; }
            dialog.dismiss(); created.accept(path);
        }));
        dialog.show();
    }
    private boolean matches(String name) { return query.isEmpty()||name.toLowerCase(Locale.ROOT).contains(query); }
    private void refresh() {
        palette.setChecked(Pref.getBool(R.string.pref_key_custom_language_color_by_scheme,false));
        selected.clear(); candidates.clear();
        for(CustomSchemes.Entry entry:scheme.entries) if(inActiveGroup(entry)) selected.add(entry);
        for(String language:languages) if(matches(language)) candidates.add(language);
        heading.setText((activeGroup == null ? "全部分區" : "分區：" + CustomSchemes.displayGroup(activeGroup)) + "（"+selected.size()+"） ▾");
        String value = scheme.colors.getOrDefault(activeGroup == null ? "" : activeGroup, "");
        android.graphics.drawable.GradientDrawable chip = new android.graphics.drawable.GradientDrawable();
        chip.setColor(value.isEmpty() ? Color.TRANSPARENT : SchemeColorPicker.parse(value));
        chip.setStroke(dp(1),Color.GRAY); groupSwatch.setBackground(chip);
        chosenList.getAdapter().notifyDataSetChanged(); availableList.getAdapter().notifyDataSetChanged();
    }
    private void save() { CustomSchemes.replaceCurrent(scheme); changed.run(); }
    private int color(CustomSchemes.Entry entry) {
        String value=entry.color(),group=entry.group();
        while(value.isEmpty()) {
            value=scheme.colors.getOrDefault(group,""); int end=group.lastIndexOf('／');
            if(!value.isEmpty()||group.isEmpty()) break;
            group=end<0?"":group.substring(0,end);
        }
        String label=DB.getLabelByLanguage(entry.language());
        return value.isEmpty()?(label==null?0xffb71c1c:DB.getColor(label)):SchemeColorPicker.parse(value);
    }
    private void moveRecord(CustomSchemes.Entry entry) {
        AutoCompleteTextView group=new AutoCompleteTextView(context);group.setSingleLine();group.setThreshold(0);
        group.setHint("分區名稱，留空為未分區");group.setText(entry.group().replace('／','-'));
        group.setAdapter(new ArrayAdapter<>(context,android.R.layout.simple_dropdown_item_1line,
                CustomSchemes.groups().stream().map(CustomSchemes::displayGroup).toArray(String[]::new)));
        group.setOnClickListener(v->group.showDropDown());
        new AlertDialog.Builder(context).setTitle(entry.language()+" · 更換分區").setView(group)
                .setNegativeButton("取消",null).setPositiveButton("儲存",(dialog,which)->{
                    String path=CustomSchemes.normalizeGroup(group.getText().toString());
                    for(int i=0;i<scheme.entries.size();i++)if(scheme.entries.get(i).id().equals(entry.id()))
                        scheme.entries.set(i,new CustomSchemes.Entry(entry.id(),entry.language(),path,entry.color()));
                    if(!path.isEmpty())scheme.groups.add(path);
                    save();refresh();
                }).show();
    }
    private class Row extends RecyclerView.ViewHolder {
        final CheckBox check; final TextView name; final TextView handle; final View swatch; final LinearLayout colorTouch;
        Row(boolean selected) {
            super(new LinearLayout(context)); LinearLayout row=(LinearLayout)itemView; row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutParams(new RecyclerView.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setMinimumHeight(dp(36));
            check=new CheckBox(context); check.setMinHeight(0); check.setMinimumHeight(0); check.setPadding(0,0,0,0); row.addView(check,new LinearLayout.LayoutParams(dp(28),-1));
            name=new TextView(context); name.setTextSize(13); name.setIncludeFontPadding(false); name.setPadding(0,dp(4),0,dp(4)); name.setMaxLines(2); name.setEllipsize(TextUtils.TruncateAt.END); FontUtil.setTypeface(name); row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            colorTouch=new LinearLayout(context); colorTouch.setGravity(Gravity.CENTER); swatch=new View(context); colorTouch.addView(swatch,new LinearLayout.LayoutParams(dp(18),dp(18))); row.addView(colorTouch,new LinearLayout.LayoutParams(dp(28),-1));
            handle=new TextView(context); handle.setText("☰"); handle.setTextSize(20); handle.setGravity(Gravity.CENTER); handle.setContentDescription("按住拖動調整順序"); row.addView(handle,new LinearLayout.LayoutParams(dp(28),-1));
            if(!selected) { colorTouch.setVisibility(View.GONE); handle.setVisibility(View.GONE); }
        }
    }
    private class SelectedRows extends RecyclerView.Adapter<Row> {
        public Row onCreateViewHolder(ViewGroup parent,int type) { return new Row(true); }
        public int getItemCount() { return selected.size(); }
        public void onBindViewHolder(Row row,int position) {
            CustomSchemes.Entry entry=selected.get(position); row.name.setText(entry.language());
            row.check.setOnCheckedChangeListener(null); row.check.setChecked(true); row.check.setContentDescription("取消選擇 "+entry.language());
            row.check.setOnCheckedChangeListener((v,on)-> { if(!on) { scheme.entries.removeIf(e->e.id().equals(entry.id())); save(); refresh(); } });
            row.name.setOnClickListener(v->{
                androidx.appcompat.widget.PopupMenu actions=new androidx.appcompat.widget.PopupMenu(context,row.name);
                actions.getMenu().add("更換分區").setOnMenuItemClickListener(item->{moveRecord(entry);return true;});
                actions.getMenu().add("移除此記錄").setOnMenuItemClickListener(item->{row.check.performClick();return true;});actions.show();
            });
            row.name.setOnLongClickListener(v->{moveRecord(entry);return true;});row.swatch.setBackgroundColor(color(entry)); row.colorTouch.setContentDescription("設定 "+entry.language()+" 的顏色");
            row.colorTouch.setOnClickListener(v->SchemeColorPicker.show(context,entry.language()+" · 顏色",String.format(Locale.ROOT,"#%06X",color(entry)&0xffffff), "跟隨分區顏色", value->{
                for(int i=0;i<scheme.entries.size();i++) if(scheme.entries.get(i).id().equals(entry.id())) scheme.entries.set(i,new CustomSchemes.Entry(entry.id(),entry.language(),entry.group(),value));
                Pref.putBool(R.string.pref_key_custom_language_color_by_scheme,true); save(); refresh();
            }));
            row.handle.setOnTouchListener((v,event)-> { if(event.getActionMasked()==MotionEvent.ACTION_DOWN) drag.startDrag(row); return false; });
        }
    }
    private class AvailableRows extends RecyclerView.Adapter<Row> {
        public Row onCreateViewHolder(ViewGroup parent,int type) { return new Row(false); }
        public int getItemCount() { return candidates.size(); }
        public void onBindViewHolder(Row row,int position) {
            String language=candidates.get(position);
            boolean included=selected.stream().anyMatch(entry->entry.language().equals(language));
            row.name.setText(language); row.check.setOnCheckedChangeListener(null);
            row.check.setChecked(included); row.check.setEnabled(!included);
            row.check.setContentDescription((included?"已選語言，點擊名稱定位左欄：":"加入 ")+language);
            row.check.setOnCheckedChangeListener((v,on)-> {
                if(on&&!included) {
                    scheme.entries.add(new CustomSchemes.Entry(language,activeGroup == null ? "" : activeGroup,"")); save(); refresh();
                    chosenList.scrollToPosition(selected.size()-1);
                }
            });
            row.name.setOnClickListener(v->{
                if(!included) row.check.performClick();
                else for(int i=0;i<selected.size();i++) if(selected.get(i).language().equals(language)) {
                    chosenList.scrollToPosition(i); break;
                }
            });
        }
    }

    public static void showSchemes(View anchor,Runnable changed) {
        SchemeGroupPopup.show(anchor, changed);
    }
}
