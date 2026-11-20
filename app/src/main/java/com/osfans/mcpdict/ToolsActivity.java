package com.osfans.mcpdict;
import android.os.Bundle;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.widget.*;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.audio.RecordingAnalysisActivity;
/** Native tools home. No WebView or secondary analysis entry. */
public class ToolsActivity extends AppCompatActivity {
 @Override public void onCreate(Bundle saved){App.setLocale();App.setActivityTheme(this);super.onCreate(saved);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);setContentView(root);
  androidx.appcompat.widget.Toolbar toolbar=new androidx.appcompat.widget.Toolbar(this);toolbar.setTitle("音典小工具");root.addView(toolbar);setSupportActionBar(toolbar);getSupportActionBar().setDisplayHomeAsUpEnabled(true);toolbar.setNavigationOnClickListener(v->finish());
  String[] names={"中古→方言","方言→中古","兩地對應","單字音類","字音比較",getString(R.string.audio_demo_title)};int[] modes={0,1,3,2,4};
  ListView list=new ListView(this);list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names){@Override public View getView(int position,View convert,ViewGroup parent){TextView v=(TextView)super.getView(position,convert,parent);FontUtil.setTypeface(v);v.setTextSize(17);v.setMinimumHeight(Math.round(54*getResources().getDisplayMetrics().density));return v;}});
  list.setOnItemClickListener((p,v,pos,id)->{
   if(pos==modes.length)startActivity(new Intent(this,RecordingAnalysisActivity.class));
   else startActivity(new Intent(this,PhonologyActivity.class).putExtra("module",modes[pos]));
  });root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
  TextView acknowledgements = new TextView(this);
  acknowledgements.setText(HtmlCompat.fromHtml(getString(R.string.tools_acknowledgements), HtmlCompat.FROM_HTML_MODE_COMPACT));
  acknowledgements.setTextSize(13);
  acknowledgements.setTextColor(App.obtainColor(this, android.R.attr.textColorSecondary));
  acknowledgements.setMovementMethod(LinkMovementMethod.getInstance());
  FontUtil.setTypeface(acknowledgements);
  float density = getResources().getDisplayMetrics().density;
  int inset = Math.round(16 * density);
  acknowledgements.setPadding(inset, Math.round(8 * density), inset, inset);
  root.addView(acknowledgements, new LinearLayout.LayoutParams(-1, -2));
 }
}
