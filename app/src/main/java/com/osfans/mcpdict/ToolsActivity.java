package com.osfans.mcpdict;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
/** Native tools home. No WebView or secondary analysis entry. */
public class ToolsActivity extends AppCompatActivity {
 @Override public void onCreate(Bundle saved){App.setLocale();App.setActivityTheme(this);super.onCreate(saved);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);setContentView(root);
  androidx.appcompat.widget.Toolbar toolbar=new androidx.appcompat.widget.Toolbar(this);toolbar.setTitle("音典小工具");root.addView(toolbar);setSupportActionBar(toolbar);getSupportActionBar().setDisplayHomeAsUpEnabled(true);toolbar.setNavigationOnClickListener(v->finish());
  String[] names={"中古→方言","方言→中古","兩地對應","單字音類","字音比較"};int[] modes={0,1,3,2,4};
  ListView list=new ListView(this);list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names){@Override public View getView(int position,View convert,ViewGroup parent){TextView v=(TextView)super.getView(position,convert,parent);FontUtil.setTypeface(v);v.setTextSize(17);v.setMinimumHeight(Math.round(54*getResources().getDisplayMetrics().density));return v;}});
  list.setOnItemClickListener((p,v,pos,id)->startActivity(new Intent(this,PhonologyActivity.class).putExtra("module",modes[pos])));root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
 }
}
