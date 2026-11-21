package com.osfans.mcpdict.custom;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import java.util.*;

/** Independent scheme screen; group and language editing stay in the two dropdowns. */
public final class CustomSchemesActivity extends AppCompatActivity {
    private final List<String> names=new ArrayList<>();
    private final Rows rows=new Rows();
    private TextView count;
    private Button delete;
    private ItemTouchHelper drag;
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        App.setLocale();App.setActivityTheme(this);super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);setContentView(root);
        androidx.appcompat.widget.Toolbar toolbar=new androidx.appcompat.widget.Toolbar(this);toolbar.setTitle("編輯方案");
        root.addView(toolbar);setSupportActionBar(toolbar);getSupportActionBar().setDisplayHomeAsUpEnabled(true);toolbar.setNavigationOnClickListener(v->finish());
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(8),dp(4),dp(8),dp(8));root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout files=new LinearLayout(this);body.addView(files);
        Button load=button("導入方案"),save=button("導出方案");files.addView(load,weight());files.addView(save,weight());
        CustomSchemeFiles actions=CustomSchemeFiles.get(this);
        load.setOnClickListener(v->actions.importExcel());save.setOnClickListener(v->actions.exportExcel());
        getSupportFragmentManager().setFragmentResultListener(CustomSchemeFiles.CHANGED,this,(key,result)->refresh());
        count=new TextView(this);count.setTextSize(14);count.setPadding(dp(8),dp(10),dp(8),dp(6));body.addView(count);
        RecyclerView list=new RecyclerView(this);list.setLayoutManager(new LinearLayoutManager(this));list.setAdapter(rows);list.setVerticalScrollBarEnabled(true);body.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        drag=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0){
            public boolean isLongPressDragEnabled(){return false;}
            public boolean onMove(RecyclerView view,RecyclerView.ViewHolder from,RecyclerView.ViewHolder to){
                int a=from.getBindingAdapterPosition(),b=to.getBindingAdapterPosition();if(a<0||b<0)return false;
                names.add(b,names.remove(a));rows.notifyItemMoved(a,b);return true;
            }
            public void onSwiped(RecyclerView.ViewHolder holder,int direction){}
            public void clearView(RecyclerView view,RecyclerView.ViewHolder holder){super.clearView(view,holder);CustomSchemes.reorderSchemes(new ArrayList<>(names));refresh();}
        });drag.attachToRecyclerView(list);
        LinearLayout edits=new LinearLayout(this);body.addView(edits);
        Button add=button("新建方案"),rename=button("改名");delete=button("刪除");
        edits.addView(add,weight());edits.addView(rename,weight());edits.addView(delete,weight());
        add.setOnClickListener(v->CustomLanguagePopup.promptScheme(this,name->refresh()));rename.setOnClickListener(v->rename());delete.setOnClickListener(v->delete());
        refresh();
    }
    @Override protected void onResume(){super.onResume();if(count!=null)refresh();}
    private Button button(String title){Button view=new Button(this);view.setText(title);view.setTextSize(14);view.setMinWidth(0);view.setMinimumWidth(0);view.setPadding(0,0,0,0);return view;}
    private LinearLayout.LayoutParams weight(){return new LinearLayout.LayoutParams(0,dp(44),1);}
    private void refresh(){names.clear();names.addAll(Arrays.asList(CustomSchemes.names()));count.setText(names.size()+" 個方案");delete.setEnabled(names.size()>1);rows.notifyDataSetChanged();}
    private void rename(){
        EditText input=new EditText(this);input.setSingleLine();input.setText(CustomSchemes.currentName());input.selectAll();
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("方案改名").setView(input).setNegativeButton("取消",null).setPositiveButton("儲存",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(!CustomSchemes.rename(input.getText().toString().trim())){input.setError("名稱不能空白或重複");return;}dialog.dismiss();refresh();
        }));dialog.show();
    }
    private void delete(){new AlertDialog.Builder(this).setTitle("刪除方案").setMessage("刪除方案「"+CustomSchemes.currentName()+"」及其全部自選記錄？")
            .setNegativeButton("取消",null).setPositiveButton("刪除",(d,w)->{CustomSchemes.delete();refresh();}).show();}
    private final class Row extends RecyclerView.ViewHolder {
        final TextView name,handle;
        Row(){super(new LinearLayout(CustomSchemesActivity.this));LinearLayout box=(LinearLayout)itemView;box.setGravity(Gravity.CENTER_VERTICAL);box.setMinimumHeight(dp(44));box.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));
            name=new TextView(CustomSchemesActivity.this);name.setTextSize(16);name.setPadding(dp(12),dp(8),dp(4),dp(8));FontUtil.setTypeface(name);box.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            handle=new TextView(CustomSchemesActivity.this);handle.setText("☰");handle.setTextSize(20);handle.setGravity(Gravity.CENTER);handle.setContentDescription("按住拖動調整方案順序");box.addView(handle,new LinearLayout.LayoutParams(dp(40),dp(44)));
        }
    }
    private final class Rows extends RecyclerView.Adapter<Row>{
        public Row onCreateViewHolder(ViewGroup parent,int type){return new Row();}
        public int getItemCount(){return names.size();}
        public void onBindViewHolder(Row row,int position){String name=names.get(position);row.name.setText(name);boolean selected=name.equals(CustomSchemes.currentName());
            int tint=App.obtainColor(CustomSchemesActivity.this,android.R.attr.colorAccent)&0xffffff;
            row.itemView.setBackgroundColor(selected?tint|0x2e000000:android.graphics.Color.TRANSPARENT);row.itemView.setSelected(selected);
            row.itemView.setOnClickListener(v->{CustomSchemes.select(name);refresh();});
            row.handle.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN)drag.startDrag(row);return false;});
        }
    }
}
