package com.osfans.mcpdict;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.UI.CharacterDetails;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Native analysis controls; SQLite and grouping never run on the UI thread. */
public class PhonologyActivity extends AppCompatActivity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private volatile int generation;
    private ToolsBridge bridge;
    private AutoCompleteTextView first,second;
    private Spinner mode,modern,middle;
    private EditText character;
    private static final String[] OBSERVATION_ITEMS={"聲母","韻母","聲調","無聲調音節","完整音節"};
    private TextView status;
    private RecyclerView results;
    private LinearLayout chartBox;
    private final List<Group> groups=new ArrayList<>();
    private final List<Group> historicalComparisonGroups = new ArrayList<>();
    private final List<Middle> ancient=new ArrayList<>();
    private static volatile List<Middle> sharedAncient;
    private final Map<String,Set<String>> filters=new LinkedHashMap<>();
    private static String voicing(String initial){if("幫端知精心莊生章書見影曉".contains(initial)&&!initial.isEmpty())return "全清";if("滂透徹清初昌溪".contains(initial)&&!initial.isEmpty())return "次清";if("並定澄從邪崇俟常船群匣".contains(initial)&&!initial.isEmpty())return "全濁";if("明泥孃娘來日疑云以".contains(initial)&&!initial.isEmpty())return "次濁";return "未分類";}
    private static final String[] MC={"聲母","韻類","聲調","攝","等","開合","完整地位","清濁"};
    private final LinkedHashMap<String,Map<String,Set<String>>> cache=new LinkedHashMap<>(4,.75f,true);
    private boolean destroyed;
    private int moduleId, candidateGeneration;
    private LinearLayout characterChoices, tableHeader;
    private Spinner mapMode;
    private Button soundPicker, comparisonMap;
    private final Set<String> selectedCharacters=new LinkedHashSet<>(), selectedSounds=new LinkedHashSet<>();
    private int selectedColor=android.graphics.Color.rgb(230,80,50);
    private boolean loadSoundsOnly;
    private static final int[] PALETTE={0xFFE65100,0xFF43A047,0xFF1976D2,0xFFAD1457,0xFF8E24AA,0xFF00897B,0xFFF9A825,0xFF6D4C41,0xFF3949AB,0xFF00ACC1,0xFF9E9E9E};
    private int displayedMode;
    private int minimumLinkCharacters, minimumNodeCharacters, sankeyCommonCount;
    private boolean ignoreMultisyllables;

    private record SankeyEntry(String reading, String value) {}
    private record MapLocation(String coordinate, int level, boolean historical, boolean island,
                               String language, String place) {}
    static class Middle {String chars;String[] values;}
    public static class Group {
        public String left,right;
        public int sourceTotal;
        public final Set<String> examples=new TreeSet<>();
        public final Set<String> characters=new TreeSet<>();
        final Map<String,Set<String>> detail=new TreeMap<>();
    }
    @Override public void onCreate(Bundle saved) {
        App.setLocale();App.setActivityTheme(this);super.onCreate(saved);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(8),dp(4),dp(8),0);setContentView(root);
        androidx.appcompat.widget.Toolbar toolbar=new androidx.appcompat.widget.Toolbar(this);toolbar.setTitle("音韻對應分析");root.addView(toolbar);setSupportActionBar(toolbar);getSupportActionBar().setDisplayHomeAsUpEnabled(true);toolbar.setNavigationOnClickListener(v->finish());
        moduleId=getIntent().getIntExtra("module",0);
        String[] titles={"中古→方言","方言→中古","單字音類","兩地對應","字音比較"};
        toolbar.setTitle(titles[Math.max(0,Math.min(4,moduleId))]);
        mode=spinner(titles);mode.setSelection(moduleId);
        LinearLayout languages=new LinearLayout(this);first=language("選擇語言 A");second=language("選擇語言 B");languages.addView(first,new LinearLayout.LayoutParams(0,-2,1));languages.addView(second,new LinearLayout.LayoutParams(0,-2,1));root.addView(languages);
        first.setVisibility(moduleId==2||moduleId==4?View.GONE:View.VISIBLE);second.setVisibility(moduleId==3?View.VISIBLE:View.GONE);languages.setVisibility(moduleId==2||moduleId==4?View.GONE:View.VISIBLE);
        LinearLayout dimensions=new LinearLayout(this);
        middle=spinner(MC);modern=spinner(OBSERVATION_ITEMS);
        if(moduleId==4)modern.setSelection(4);
        LinearLayout middleField=observationField("中古項目",middle);
        LinearLayout dialectField=observationField("方言項目",modern);
        if(moduleId==1){dimensions.addView(dialectField,new LinearLayout.LayoutParams(0,-2,1));dimensions.addView(middleField,new LinearLayout.LayoutParams(0,-2,1));}
        else {dimensions.addView(middleField,new LinearLayout.LayoutParams(0,-2,1));dimensions.addView(dialectField,new LinearLayout.LayoutParams(0,-2,1));}
        middleField.setVisibility(moduleId<2?View.VISIBLE:View.GONE);root.addView(dimensions);

        LinearLayout conditions=new LinearLayout(this);Button filter=new Button(this);filter.setText("中古條件：不限");filter.setOnClickListener(v->showFilters(filter));conditions.addView(filter,new LinearLayout.LayoutParams(0,-2,1));filter.setVisibility(moduleId<2?View.VISIBLE:View.GONE);
        root.addView(conditions);
        character=new EditText(this);character.setSingleLine();character.setHint(moduleId==2?"輸入一字，選擇繁簡候選":"輸入要比較的漢字");root.addView(character);character.setVisibility(moduleId==2||moduleId==4?View.VISIBLE:View.GONE);
        HorizontalScrollView candidatesScroll=new HorizontalScrollView(this);characterChoices=new LinearLayout(this);candidatesScroll.addView(characterChoices);root.addView(candidatesScroll);candidatesScroll.setVisibility(moduleId==2||moduleId==4?View.VISIBLE:View.GONE);
        character.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){int request=++candidateGeneration;String input=s.toString().trim();selectedCharacters.clear();selectedSounds.clear();groups.clear();comparisonMap.setVisibility(View.GONE);tableHeader.setVisibility(View.GONE);results.getAdapter().notifyDataSetChanged();characterChoices.removeAllViews();generation++;main.postDelayed(()->{if(request!=candidateGeneration||destroyed)return;worker.execute(()->resolveCandidates(input,request));},250);}public void afterTextChanged(android.text.Editable e){}});
        LinearLayout maps=new LinearLayout(this);mapMode=spinner(new String[]{"繪製全部地圖","繪製所選音值（可多選）"});maps.addView(mapMode,new LinearLayout.LayoutParams(0,-2,1));soundPicker=new Button(this);soundPicker.setText("音值 ▾");soundPicker.setOnClickListener(v->{if(groups.isEmpty()){loadSoundsOnly=true;run();}else chooseSounds();});maps.addView(soundPicker);root.addView(maps);maps.setVisibility(moduleId==2?View.VISIBLE:View.GONE);
        Button run=new Button(this);run.setText(moduleId==2?"生成地圖":moduleId==3?"生成桑基圖":moduleId==4?"比較字音":"生成對應表");run.setOnClickListener(v->{loadSoundsOnly=false;run();});
        if (moduleId == 3) {
            LinearLayout actions = new LinearLayout(this);
            actions.addView(run, new LinearLayout.LayoutParams(0, -2, 1));
            Button options = new Button(this);
            options.setText("桑基圖設定");
            options.setOnClickListener(v -> showSankeyOptions());
            actions.addView(options);
            root.addView(actions);
        } else {
            root.addView(run);
        }
        comparisonMap=new Button(this);comparisonMap.setText("全部地圖");comparisonMap.setVisibility(View.GONE);comparisonMap.setOnClickListener(v->drawComparisonMap(null));root.addView(comparisonMap);
        status=new TextView(this);status.setTextSize(12);status.setMaxLines(2);root.addView(status);
        tableHeader=new LinearLayout(this);root.addView(tableHeader);tableHeader.setVisibility(View.GONE);
        String[] columns=moduleId==0?new String[]{"中古","今音","字數","例字／詳情"}:moduleId==1?new String[]{"今音","中古","字數","例字／詳情"}:new String[]{"比較字組","同音情況","點數","方言／詳情"};
        for(int i=0;i<4;i++){TextView title=cell(columns[i],i);title.setTypeface(FontUtil.getDictTypeface(),android.graphics.Typeface.BOLD);tableHeader.addView(title);final int column=i;title.setOnClickListener(v->{if(column==2)groups.sort((a,b)->Integer.compare(b.characters.size(),a.characters.size()));else if(column<2)groups.sort(java.util.Comparator.comparing(g->column==0?g.left:g.right));results.getAdapter().notifyDataSetChanged();});}
        chartBox=new LinearLayout(this);chartBox.setOrientation(LinearLayout.VERTICAL);root.addView(chartBox,new LinearLayout.LayoutParams(-1,0,1));chartBox.setVisibility(moduleId==3?View.VISIBLE:View.GONE);
        results=(RecyclerView)getLayoutInflater().inflate(R.layout.phonology_results,root,false);results.setLayoutManager(new LinearLayoutManager(this));results.setAdapter(new Rows());results.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this,LinearLayoutManager.VERTICAL));root.addView(results,new LinearLayout.LayoutParams(-1,0,1));results.setVisibility(moduleId==2||moduleId==3?View.GONE:View.VISIBLE);
        applyFonts(root);bridge=new ToolsBridge(this);setStatus("載入中古地位…");
        worker.execute(()->{try {com.osfans.mcpdict.Favorite.UserDB.initialize(this);com.osfans.mcpdict.Orth.Orthography.initialize(getResources());DB.initialize(this);DB.initFQ();com.osfans.mcpdict.Orth.Orthography.setToneStyle(com.osfans.mcpdict.Util.Pref.getToneStyle(R.string.pref_key_tone_display));com.osfans.mcpdict.Orth.Orthography.setToneValueStyle(com.osfans.mcpdict.Util.Pref.getToneStyle(R.string.pref_key_tone_value_display));if(moduleId>=2){main.post(()->{if(!destroyed)setStatus("");});return;}
        List<Middle> parsed=sharedAncient;
        if(parsed==null){parsed=new ArrayList<>();Pattern positionPattern=Pattern.compile("^(.*?)([一二三四])([ABC]?)(.*?)([平上去入])$");
            android.database.Cursor cursor=DB.getCursor("select 字組,讀音 from langs where 語言 MATCH '\"廣韻\"'");
            if(cursor!=null)try{for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){String[] fields=cursor.getString(1).split("/",-1);if(fields.length<=20)continue;String desc=fields[18];Matcher m=positionPattern.matcher(desc);if(!m.find())continue;
                String initial=PhonologyOrders.canonical(m.group(1).replace("開","").replace("合",""));Middle item=new Middle();item.chars=cursor.getString(0);item.values=new String[]{initial,PhonologyOrders.canonical(m.group(4)),m.group(5),fields[19].replace("攝","").replace("摄",""),PhonologyOrders.division(m.group(2),m.group(3)),fields[20].contains("合")?"合":fields[20].contains("開")?"開":"未標",desc,voicing(initial)};parsed.add(item);
            }}finally{cursor.close();}sharedAncient=Collections.unmodifiableList(parsed);
        }
        List<Middle> ready=parsed;
        main.post(()->{if(destroyed)return;ancient.addAll(ready);setStatus("");});}catch(Exception e){error(e);}});
    }
    private void setStatus(CharSequence value){status.setText(value);status.setVisibility(value==null||value.length()==0?View.GONE:View.VISIBLE);}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Spinner spinner(String[] items){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items));return s;}
    private LinearLayout observationField(String title,Spinner input){
        LinearLayout field=new LinearLayout(this);field.setOrientation(LinearLayout.VERTICAL);
        TextView label=new TextView(this);label.setText(title);label.setTextSize(12);label.setTextColor(App.obtainColor(this,android.R.attr.textColorSecondary));label.setPadding(dp(4),dp(4),dp(4),0);FontUtil.setTypeface(label);
        input.setContentDescription(title);field.addView(label);field.addView(input,new LinearLayout.LayoutParams(-1,-2));return field;
    }
    private AutoCompleteTextView language(String hint){AutoCompleteTextView text=new AutoCompleteTextView(this);text.setSingleLine();text.setThreshold(1);text.setHint(hint);text.setAdapter(new NativeLanguageAdapter(this));text.setOnFocusChangeListener((v,on)->{if(on)text.showDropDown();});return text;}
    private void applyFonts(View view){if(view instanceof TextView)FontUtil.setTypeface((TextView)view);if(view instanceof android.view.ViewGroup)for(int i=0;i<((android.view.ViewGroup)view).getChildCount();i++)applyFonts(((android.view.ViewGroup)view).getChildAt(i));}
    private void showFilters(Button button){
        if(ancient.isEmpty()){Toast.makeText(this,"中古資料尚未載入",Toast.LENGTH_SHORT).show();return;}
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);Map<String,Set<String>> draft=new LinkedHashMap<>();for(String key:filters.keySet())draft.put(key,new LinkedHashSet<>(filters.get(key)));
        for(int i:new int[]{0,1,2,3,4,5,7}){final int dimension=i;Set<String> all=new LinkedHashSet<>();for(Middle m:ancient)all.add(m.values[i]);List<String> values=PhonologyOrders.ordered(i,all);Button category=new Button(this);category.setText(MC[i]+"："+summary(draft.get(MC[i])));body.addView(category);
            category.setOnClickListener(v->{Set<String> checked=new LinkedHashSet<>(draft.getOrDefault(MC[dimension],Collections.emptySet()));boolean[] selected=new boolean[values.size()];for(int j=0;j<selected.length;j++)selected[j]=checked.contains(values.get(j));
                new AlertDialog.Builder(this).setTitle(MC[dimension]+"（可多選）").setMultiChoiceItems(values.toArray(new String[0]),selected,(d,index,on)->{String value=values.get(index);if(on)checked.add(value);else checked.remove(value);if(dimension==4){android.widget.ListView list=((AlertDialog)d).getListView();if(value.equals("三")){for(int j=0;j<values.size();j++)if(values.get(j).startsWith("三")){if(on)checked.add(values.get(j));else checked.remove(values.get(j));list.setItemChecked(j,on);}}else if(value.startsWith("三")&&!on){checked.remove("三");int parent=values.indexOf("三");if(parent>=0)list.setItemChecked(parent,false);}}}).setPositiveButton("確定",(d,w)->{draft.put(MC[dimension],checked);category.setText(MC[dimension]+"："+summary(checked));}).setNeutralButton("不限",(d,w)->{draft.remove(MC[dimension]);category.setText(MC[dimension]+"：不限");}).setNegativeButton("取消",null).show();
            });
        }
        ScrollView scroll=new ScrollView(this);scroll.addView(body);
        new AlertDialog.Builder(this).setTitle("中古條件（項內或、項間且）").setView(scroll).setPositiveButton("套用",(d,w)->{filters.clear();for(String key:draft.keySet())if(!draft.get(key).isEmpty())filters.put(key,draft.get(key));button.setText(filters.isEmpty()?"中古條件：不限":"中古條件："+filters.size()+" 項");}).setNeutralButton("全部不限",(d,w)->{filters.clear();button.setText("中古條件：不限");}).setNegativeButton("取消",null).show();
    }
    private String modernLabel(int dimension){return OBSERVATION_ITEMS[dimension];}
    private String summary(Set<String> values){return values==null||values.isEmpty()?"不限":TextUtils.join("、",values);}
    private void resolveCandidates(String input,int request){try{JSONArray rows=new JSONArray(bridge.resolveCharacters(input));LinkedHashSet<String> values=new LinkedHashSet<>();for(int i=0;i<rows.length();i++){JSONArray candidates=rows.getJSONObject(i).optJSONArray("candidates");if(candidates!=null)for(int j=0;j<candidates.length();j++)values.add(candidates.getString(j));}main.post(()->{if(destroyed||request!=candidateGeneration)return;characterChoices.removeAllViews();selectedCharacters.clear();selectedCharacters.addAll(values);for(String value:values){CheckBox box=new CheckBox(this);box.setText(value);FontUtil.setTypeface(box);box.setChecked(true);box.setOnCheckedChangeListener((v,on)->{if(on)selectedCharacters.add(value);else selectedCharacters.remove(value);generation++;selectedSounds.clear();groups.clear();comparisonMap.setVisibility(View.GONE);tableHeader.setVisibility(View.GONE);results.getAdapter().notifyDataSetChanged();});characterChoices.addView(box);}});}catch(Exception e){error(e);}}
    private Map<String,Set<String>> readings(String language)throws Exception {
        String label=DB.getLabelByLanguage(language);if(label==null||label.isEmpty())throw new IllegalArgumentException("請從候選列表選擇完整語言名稱");
        if(cache.containsKey(label))return cache.get(label);
        Map<String,Set<String>> map=new HashMap<>();String match="\""+label.replace("\"","\"\"")+"\"";
        android.database.Cursor cursor=DB.getCursor("select 字組,讀音 from langs where 語言 MATCH '"+match.replace("'","''")+"'");
        if(cursor!=null)try{for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){String group=cursor.getString(0),raw=cursor.getString(1);if(raw==null)continue;for(String value:raw.split("\\t",-1)){if(value.isBlank())continue;String reading=com.osfans.mcpdict.Orth.DisplayHelper.getIPA(label,value).toString().trim();if(reading.isEmpty())continue;group.codePoints().filter(cp->Character.UnicodeScript.of(cp)==Character.UnicodeScript.HAN).forEach(cp->map.computeIfAbsent(new String(Character.toChars(cp)),k->new LinkedHashSet<>()).add(reading));}}}finally{cursor.close();}
        cache.put(label,map);while(cache.size()>2)cache.remove(cache.keySet().iterator().next());return map;
    }
    private Map<String, Set<SankeyEntry>> sankeyReadings(String language, int dimension, boolean ignoreMulti) {
        String label = DB.getLabelByLanguage(language);
        if (TextUtils.isEmpty(label)) {
            throw new IllegalArgumentException("請從候選列表選擇完整語言名稱");
        }
        Map<String, String> tones = new HashMap<>();
        JSONObject toneConfig = dimension == 2 ? DB.getToneName(label) : null;
        if (toneConfig != null) {
            for (Iterator<String> keys = toneConfig.keys(); keys.hasNext();) {
                String code = keys.next();
                JSONArray styles = toneConfig.optJSONArray(code);
                String name = styles == null || styles.isNull(3) ? "" : styles.optString(3).trim();
                if (!name.isEmpty()) tones.put(code, name + "（" + code + "）");
            }
        }
        Map<String, Set<SankeyEntry>> byCharacter = new HashMap<>();
        Map<String, Optional<SankeyEntry>> byRaw = new HashMap<>();
        String match = "\"" + label.replace("\"", "\"\"").replace("'", "''") + "\"";
        try (android.database.Cursor cursor = DB.getCursor("select 字組,讀音 from langs where 語言 MATCH '" + match + "'")) {
            while (cursor != null && cursor.moveToNext() && !Thread.currentThread().isInterrupted()) {
                String raw = cursor.getString(1);
                if (raw == null) continue;
                Set<SankeyEntry> entries = new LinkedHashSet<>();
                for (String reading : raw.split("\\t")) {
                    byRaw.computeIfAbsent(reading, key -> Optional.ofNullable(sankeyEntry(label, key,
                            dimension, ignoreMulti, tones))).ifPresent(entries::add);
                }
                if (entries.isEmpty()) continue;
                cursor.getString(0).codePoints().filter(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN)
                        .forEach(cp -> byCharacter.computeIfAbsent(new String(Character.toChars(cp)),
                                key -> new LinkedHashSet<>()).addAll(entries));
            }
        }
        return byCharacter;
    }

    private SankeyEntry sankeyEntry(String label, String raw, int dimension, boolean ignoreMulti,
                                    Map<String, String> tones) {
        List<String> syllables = SankeyReading.syllables(raw);
        if (syllables.isEmpty() || (ignoreMulti && syllables.size() > 1)) return null;
        List<String> values = new ArrayList<>(syllables.size());
        for (String syllable : syllables) {
            String ipa = DisplayHelper.getIPA(label, syllable).toString().trim();
            values.add(dimension == 2 ? SankeyReading.tone(syllable, ipa, tones)
                    : PhoneticParts.parse(ipa).dimension(dimension));
        }
        return new SankeyEntry(DisplayHelper.getIPA(label, raw).toString().trim(), String.join(" · ", values));
    }

    private void showSankeyOptions() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(8));
        SeekBar link = sankeyThreshold(content, "連線最少字數", 50, minimumLinkCharacters);
        SeekBar node = sankeyThreshold(content, "節點最少字數", 100, minimumNodeCharacters);
        CheckBox ignore = new CheckBox(this);
        ignore.setText("忽略多音節讀音");
        ignore.setChecked(ignoreMultisyllables);
        content.addView(ignore);
        applyFonts(content);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        new AlertDialog.Builder(this).setTitle("桑基圖設定").setView(scroll)
                .setNegativeButton("取消", null).setPositiveButton("套用", (dialog, button) -> {
                    boolean recalculate = ignoreMultisyllables != ignore.isChecked();
                    minimumLinkCharacters = link.getProgress();
                    minimumNodeCharacters = node.getProgress();
                    ignoreMultisyllables = ignore.isChecked();
                    if (displayedMode == 3 || sankeyPending) {
                        if (recalculate) run();
                        else if (!sankeyPending) renderSankey();
                    }
                }).show();
    }

    private SeekBar sankeyThreshold(LinearLayout content, String name, int maximum, int value) {
        TextView label = new TextView(this);
        label.setText(name + "：" + value);
        content.addView(label);
        SeekBar bar = new SeekBar(this);
        bar.setContentDescription(name);
        bar.setMax(maximum);
        bar.setProgress(value);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(name + "：" + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        content.addView(bar);
        return bar;
    }

    private boolean sankeyPending;

    private void renderSankey() {
        int request = generation;
        int linkMinimum = minimumLinkCharacters;
        int nodeMinimum = minimumNodeCharacters;
        int total = sankeyCommonCount;
        List<Group> snapshot = new ArrayList<>(groups);
        worker.execute(() -> {
            List<Group> visible = new ArrayList<>();
            for (Group group : snapshot) {
                if (group.characters.size() >= linkMinimum) visible.add(group);
            }
            // Count distinct characters per node. Removing a node can shrink its neighbours.
            if (nodeMinimum > 0) {
                boolean removed;
                do {
                    Map<String, Set<String>> left = new HashMap<>(), right = new HashMap<>();
                    for (Group group : visible) {
                        left.computeIfAbsent(group.left, key -> new HashSet<>()).addAll(group.characters);
                        right.computeIfAbsent(group.right, key -> new HashSet<>()).addAll(group.characters);
                    }
                    removed = visible.removeIf(group -> left.get(group.left).size() < nodeMinimum
                            || right.get(group.right).size() < nodeMinimum);
                } while (removed);
            }
            Set<String> shown = new HashSet<>();
            for (Group group : visible) shown.addAll(group.characters);
            main.post(() -> {
                if (destroyed || request != generation || linkMinimum != minimumLinkCharacters
                        || nodeMinimum != minimumNodeCharacters) return;
                chartBox.removeAllViews();
                if (visible.isEmpty()) {
                    setStatus(total == 0 ? "沒有符合條件的共同字" : "沒有符合門檻的連線 · 共同字 " + total);
                    return;
                }
                setStatus(visible.size() + " 條連線 · 顯示共同字 " + shown.size() + "／" + total);
                ScrollView scroll = new ScrollView(this);
                scroll.setScrollbarFadingEnabled(false);
                scroll.addView(new SankeyView(this, visible, group -> showExamples(group, 3)),
                        new ScrollView.LayoutParams(-1, -2));
                chartBox.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
            });
        });
    }

    private void add(Map<String,Group> output,String left,String right,String hz,String reading){Group g=output.computeIfAbsent(left+"\t"+right,k->{Group value=new Group();value.left=left;value.right=right;return value;});g.characters.add(hz);g.examples.add(hz+" /"+reading+"/");g.detail.computeIfAbsent(hz,k->new TreeSet<>()).add(reading);}
    private void run(){
        final int selectedMode=mode.getSelectedItemPosition(),mi=middle.getSelectedItemPosition(),di=modern.getSelectedItemPosition(),request=++generation;
        final boolean ignoreMulti = ignoreMultisyllables;
        sankeyPending = selectedMode == 3;
        displayedMode = -1;
        historicalComparisonGroups.clear();
        String a=first.getText().toString().trim(),b=second.getText().toString().trim(),hz=character.getText().toString().trim();Map<String,Set<String>> selected=new HashMap<>();for(String key:filters.keySet())selected.put(key,new LinkedHashSet<>(filters.get(key)));Set<String> chosenCharacters=new LinkedHashSet<>(selectedCharacters);List<Middle> positions=new ArrayList<>(ancient);
        setStatus("正在分析…");groups.clear();comparisonMap.setVisibility(View.GONE);tableHeader.setVisibility(View.GONE);results.getAdapter().notifyDataSetChanged();chartBox.removeAllViews();
        worker.execute(()->{try {
            if(request!=generation)return;
            Map<String,Group> output=new TreeMap<>();Set<String> common=new HashSet<>();
            Map<String, Group> historicalComparison = new TreeMap<>();
            if(selectedMode==2){
                if(hz.codePointCount(0,hz.length())!=1)throw new IllegalArgumentException("請輸入一個漢字，再選擇繁簡候選");
                if(chosenCharacters.isEmpty())throw new IllegalArgumentException("請先勾選漢字");
                Set<String> seen=new HashSet<>();Map<String,List<String>> formatted=new HashMap<>();
                for(String variant:chosenCharacters){String match="\""+variant.replace("\"","\"\"")+"\"";android.database.Cursor cursor=DB.getCursor("select 語言,讀音 from langs where 字組 MATCH '"+match.replace("'","''")+"'");
                    if(cursor!=null)try{for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){String label=cursor.getString(0),raw=cursor.getString(1);if(raw==null)continue;String key=label+"\t"+raw;List<String> readings=formatted.get(key);if(readings==null){readings=new ArrayList<>();for(String part:raw.split("\\t",-1))if(!part.isBlank())readings.add(DisplayHelper.getIPA(label,part).toString());formatted.put(key,readings);}for(String ipa:readings){if(ipa.isBlank()||!seen.add(variant+"\t"+label+"\t"+ipa))continue;add(output,PhoneticParts.parse(ipa).dimension(di),"各語言",label,variant+" /"+ipa+"/");}}}finally{cursor.close();}
                }
            }else if(selectedMode==3){
                Map<String, Set<SankeyEntry>> left = sankeyReadings(a, di, ignoreMulti);
                Map<String, Set<SankeyEntry>> right = a.equals(b) ? left : sankeyReadings(b, di, ignoreMulti);
                for (String c : left.keySet()) {
                    if (!right.containsKey(c)) continue;
                    common.add(c);
                    for (SankeyEntry x : left.get(c)) for (SankeyEntry y : right.get(c)) {
                        add(output, x.value(), y.value(), c, a + " " + x.reading() + " → " + b + " " + y.reading());
                    }
                }
            }else if(selectedMode==4){
                if(chosenCharacters.size()<2)throw new IllegalArgumentException("請至少勾選兩個要比較的漢字");
                Map<String,Map<String,Set<String>>> byLanguage=new TreeMap<>();
                android.database.Cursor languages=DB.getCursor("select 簡稱,歷史音 from info where 音節數 is not null");
                Set<String> historicalLanguages = new HashSet<>();
                if (languages != null) try (languages) {
                    while (languages.moveToNext()) {
                        String label = languages.getString(0);
                        byLanguage.put(label, new LinkedHashMap<>());
                        if (isHistorical(languages.getString(1))) historicalLanguages.add(label);
                    }
                }
                Map<String,List<String>> formatted=new HashMap<>();
                for(String c:chosenCharacters){String match="\""+c.replace("\"","\"\"")+"\"";android.database.Cursor cursor=DB.getCursor("select 語言,讀音 from langs where 字組 MATCH '"+match.replace("'","''")+"'");
                    if(cursor!=null)try{for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){String label=cursor.getString(0),raw=cursor.getString(1);if(raw==null||!byLanguage.containsKey(label))continue;String key=label+"\t"+raw;List<String> pronunciations=formatted.get(key);if(pronunciations==null){pronunciations=new ArrayList<>();for(String part:raw.split("\\t",-1))if(!part.isBlank())pronunciations.add(com.osfans.mcpdict.Orth.DisplayHelper.getIPA(label,part).toString().trim());formatted.put(key,pronunciations);}for(String ipa:pronunciations)if(!ipa.isBlank())byLanguage.get(label).computeIfAbsent(c,k->new LinkedHashSet<>()).add(ipa);}}finally{cursor.close();}
                }
                String wordGroup=TextUtils.join("、",chosenCharacters);
                for(String label:byLanguage.keySet()){List<Set<String>> parts=new ArrayList<>();List<String> detail=new ArrayList<>();for(String c:chosenCharacters){Set<String> readings=byLanguage.get(label).getOrDefault(c,Collections.emptySet());Set<String> observed=new HashSet<>();for(String ipa:readings)observed.add(PhoneticParts.parse(ipa).dimension(di));parts.add(observed);detail.add(c+"："+(readings.isEmpty()?"缺資料":"/"+TextUtils.join("/、/",readings)+"/")+"〔"+modernLabel(di)+"："+(observed.isEmpty()?"缺資料":TextUtils.join("、",new TreeSet<>(observed)))+"〕");}
                    add(historicalLanguages.contains(label) ? historicalComparison : output, wordGroup,
                            CharacterSoundComparison.classify(parts), label, TextUtils.join("\n",detail));
                }
            }else {
                if(positions.isEmpty())throw new IllegalArgumentException("中古資料尚未載入，請稍後再試");Map<String,Set<String>> dialect=readings(a);
                for(Middle m:positions){boolean accept=true;for(int i=0;i<MC.length;i++)if(selected.containsKey(MC[i])&&!PhonologyOrders.matches(i,selected.get(MC[i]),m.values[i]))accept=false;if(!accept)continue;
                    Set<String> chars=new LinkedHashSet<>();m.chars.codePoints().filter(cp->Character.UnicodeScript.of(cp)==Character.UnicodeScript.HAN).forEach(cp->chars.add(new String(Character.toChars(cp))));
                    for(String c:chars)for(String ipa:dialect.getOrDefault(c,Collections.emptySet())){String part=PhoneticParts.parse(ipa).dimension(di);add(output,selectedMode==0?m.values[mi]:part,selectedMode==0?part:m.values[mi],c,ipa+"〔"+m.values[6]+"〕");}
                }
            }
            Map<String,Set<String>> coverage=new HashMap<>();for(Group g:output.values())coverage.computeIfAbsent(g.left,k->new HashSet<>()).addAll(g.characters);for(Group g:output.values())g.sourceTotal=coverage.get(g.left).size();List<Group> data=new ArrayList<>(output.values());data.sort((x,y)->Integer.compare(y.characters.size(),x.characters.size()));int commonCount=common.size();
            main.post(()->{if(destroyed||request!=generation)return;displayedMode=selectedMode;groups.addAll(data);tableHeader.setVisibility(selectedMode==0||selectedMode==1||selectedMode==4?View.VISIBLE:View.GONE);results.getAdapter().notifyDataSetChanged();setStatus(data.size()+" 組 · 點擊一列查看詳情");if(selectedMode==4){int points=0;for(Group g:data)points+=g.characters.size();setStatus("共 "+data.size()+" 類情況 · "+points+" 個方言點");historicalComparisonGroups.addAll(historicalComparison.values());comparisonMap.setVisibility(data.isEmpty()&&historicalComparisonGroups.isEmpty()?View.GONE:View.VISIBLE);}
                if (selectedMode == 3) {
                    sankeyPending = false;
                    sankeyCommonCount = commonCount;
                    renderSankey();
                }
                if(selectedMode==2){if(loadSoundsOnly){loadSoundsOnly=false;chooseSounds();}else if(mapMode.getSelectedItemPosition()==1&&selectedSounds.isEmpty())chooseSounds();else drawSoundMap();}
            });
        }catch(Exception e){main.post(()->{if(!destroyed&&request==generation){sankeyPending=false;setStatus("分析失敗："+e.getMessage());}});}});
    }
    private void error(Exception e){main.post(()->{if(!destroyed)setStatus("載入失敗："+e.getMessage());});}
    private TextView cell(String value,int column){TextView text=new TextView(this);text.setTextAppearance(R.style.FontDetail);FontUtil.setTypeface(text);text.setTextSize(14);text.setText(value);text.setPadding(dp(5),dp(7),dp(5),dp(7));text.setLayoutParams(column<3?new LinearLayout.LayoutParams(dp(column==2?44:column==0?60:76),-2):new LinearLayout.LayoutParams(0,-2,1));return text;}
    private void showExamples(Group group,int selectedMode){
        List<String> keys=new ArrayList<>(group.detail.keySet());LinearLayout container=new LinearLayout(this);container.setOrientation(LinearLayout.VERTICAL);LinearLayout header=new LinearLayout(this);header.addView(cell(selectedMode==2||selectedMode==4?"語言":"漢字",0));header.addView(cell("讀音／地位",3));container.addView(header);
        ListView list=new ListView(this);list.setVerticalScrollBarEnabled(true);list.setScrollbarFadingEnabled(false);list.setAdapter(new BaseAdapter(){public int getCount(){return keys.size();}public Object getItem(int i){return keys.get(i);}public long getItemId(int i){return i;}public View getView(int pos,View convert,android.view.ViewGroup parent){String key=keys.get(pos);LinearLayout row=new LinearLayout(PhonologyActivity.this);TextView word=cell(key,0);row.addView(word);row.addView(cell(TextUtils.join("\n",group.detail.get(key)),3));word.setOnClickListener(v->{if(selectedMode==2||selectedMode==4)App.info(PhonologyActivity.this,key);else CharacterDetails.show(PhonologyActivity.this,key);});TextView more=cell("詳情",2);more.setOnClickListener(v->word.performClick());row.addView(more);return row;}});container.addView(list,new LinearLayout.LayoutParams(-1,dp(340)));
        AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle(group.left+" → "+group.right+"（"+keys.size()+"）").setView(container).setPositiveButton("關閉",null);
        if(selectedMode==4)dialog.setNeutralButton("地圖",(d,w)->drawComparisonMap(group));dialog.show();
    }
    private void chooseSounds(){if(groups.isEmpty()){Toast.makeText(this,"沒有可用音值",Toast.LENGTH_SHORT).show();return;}String[] labels=new String[groups.size()];boolean[] checked=new boolean[groups.size()];Set<String> draft=new LinkedHashSet<>(selectedSounds);for(int i=0;i<labels.length;i++){labels[i]=groups.get(i).left;checked[i]=draft.contains(labels[i]);}new AlertDialog.Builder(this).setTitle("選擇音值（可多選）").setMultiChoiceItems(labels,checked,(d,i,on)->{if(on)draft.add(labels[i]);else draft.remove(labels[i]);}).setPositiveButton("繪製地圖",(d,w)->{selectedSounds.clear();selectedSounds.addAll(draft);mapMode.setSelection(1);drawSoundMap();}).setNeutralButton("選擇顏色",(d,w)->{selectedSounds.clear();selectedSounds.addAll(draft);new AlertDialog.Builder(this).setTitle("所選音值顏色").setItems(new String[]{"橙","綠","藍","紅","紫","青","黃","棕","靛","湖藍"},(x,i)->{selectedColor=PALETTE[i];mapMode.setSelection(1);drawSoundMap();}).show();}).setNegativeButton("取消",null).show();}
    private void drawSoundMap(){
        soundPicker.setText("音值（"+selectedSounds.size()+"） ▾");
        boolean single=mapMode.getSelectedItemPosition()==1;if(single&&selectedSounds.isEmpty()){Toast.makeText(this,"請選擇音值",Toast.LENGTH_SHORT).show();return;}
        List<Group> snapshot=new ArrayList<>(groups);Set<String> wanted=new LinkedHashSet<>(selectedSounds);int colorChoice=selectedColor,request=++generation;String title=character.getText()+" · "+modern.getSelectedItem(),dimension=modern.getSelectedItem().toString();setStatus("正在繪製地圖…");
        worker.execute(()->{try{
        Map<String, MapLocation> locations = loadMapLocations();
        Map<String,JSONObject> points=new LinkedHashMap<>();Map<String,Set<Integer>> colors=new LinkedHashMap<>();Map<String,List<String>> details=new LinkedHashMap<>();List<String> legend=new ArrayList<>();List<Integer> legendColors=new ArrayList<>();
        Map<String, Set<Integer>> memberships = new HashMap<>();
        for(int i=0;i<snapshot.size();i++){Group g=snapshot.get(i);if(single&&!wanted.contains(g.left))continue;int color=single?colorChoice:PALETTE[Math.min(i,10)];
            int legendIndex = single ? legend.size() : Math.min(i, 10);
            if (legendIndex == legend.size()) {
                legend.add(single || i < 10 ? g.left : "其他");
                legendColors.add(color);
            }
            for (String label : g.detail.keySet()) {
                JSONObject row = points.get(label);
                if (row == null) {
                    MapLocation location = locations.get(label);
                    if (location == null) continue;
                    row = mapPoint(label, location);
                    points.put(label, row);
                }
                memberships.computeIfAbsent(label, key -> new LinkedHashSet<>()).add(legendIndex);
                colors.computeIfAbsent(label, key -> new LinkedHashSet<>()).add(color);
                details.computeIfAbsent(label, key -> new ArrayList<>()).add(g.left + "：" + TextUtils.join("、", g.detail.get(label)));
            }
        }
        JSONArray data=new JSONArray();for(String label:points.keySet()){JSONObject row=points.get(label);MapLocation location=locations.get(label);row.put("legendItems",new JSONArray(memberships.get(label)));row.put("color",colors.get(label).iterator().next());row.put("colors",new JSONArray(colors.get(label)));row.put("detail",location.language()+"\n"+location.place()+"\n"+dimension+"\n"+TextUtils.join("\n",details.get(label)));data.put(row);}
        if(data.length()==0){main.post(()->{if(!destroyed&&request==generation){setStatus("所選音值沒有可用座標");Toast.makeText(this,"所選音值沒有可用座標",Toast.LENGTH_SHORT).show();}});return;}
        int[] palette=new int[legendColors.size()];for(int i=0;i<palette.length;i++)palette[i]=legendColors.get(i);
        main.post(()->{if(destroyed||request!=generation)return;setStatus(data.length()+" 個地點");com.osfans.mcpdict.UI.MapView.showThematic(this,data,title,legend.toArray(new String[0]),palette);});
        }catch(Exception e){error(e);}});
    }
    private static boolean isHistorical(String value) {
        return !TextUtils.isEmpty(value) && !value.equals("0");
    }

    private Map<String, MapLocation> loadMapLocations() {
        Map<String, MapLocation> locations = new HashMap<>();
        try (android.database.Cursor cursor = DB.getCursor(
                "SELECT 簡稱, 經緯度, 地圖級別, 歷史音, 方言島, 語言, 地點 FROM info WHERE length(經緯度) > 0")) {
            while (cursor != null && cursor.moveToNext()) {
                String label = cursor.getString(0);
                String coordinate = cursor.getString(1);
                if (TextUtils.isEmpty(label) || coordinate == null || coordinate.isBlank()) continue;
                locations.putIfAbsent(label, new MapLocation(coordinate, cursor.getInt(2),
                        isHistorical(cursor.getString(3)), "1".equals(cursor.getString(4)),
                        Objects.toString(cursor.getString(5), ""), Objects.toString(cursor.getString(6), "")));
            }
        }
        return locations;
    }

    private JSONObject mapPoint(String label, MapLocation location) throws JSONException {
        JSONObject row = new JSONObject();
        row.put("label", label);
        row.put("coordinate", location.coordinate());
        row.put("mapLevel", location.level());
        row.put("historical", location.historical());
        row.put("island", location.island());
        return row;
    }

    private void drawComparisonMap(Group selected){
        List<Group> snapshot = selected == null ? new ArrayList<>(groups) : new ArrayList<>(List.of(selected));
        for (Group group : historicalComparisonGroups) {
            if (selected == null || selected.right.equals(group.right)) snapshot.add(group);
        }
        int request=++generation;String title=selected==null?"字音比較 · 全部地圖":"字音比較 · "+selected.right;setStatus("正在繪製地圖…");
        worker.execute(()->{try{JSONArray data=new JSONArray();List<String> legend=new ArrayList<>();List<Integer> palette=new ArrayList<>();
            Map<String, MapLocation> locations = loadMapLocations();
            for(Group g:snapshot){int color=g.right.equals("全同")?0xFF15803D:g.right.equals("部分相同")?0xFFCA8A04:g.right.equals("全異")?0xFFDC2626:0xFF94A3B8;
                int legendIndex = legend.indexOf(g.right);
                if (legendIndex < 0) { legendIndex = legend.size(); legend.add(g.right); palette.add(color); }
                for(String label:g.detail.keySet()){MapLocation location=locations.get(label);if(location==null)continue;JSONObject row=mapPoint(label,location);row.put("legendItems",new JSONArray().put(legendIndex));row.put("color",color);row.put("detail",location.language()+"\n"+location.place()+"\n"+g.left+" · "+g.right+"\n"+TextUtils.join("\n",g.detail.get(label)));data.put(row);}
            }
            int[] colors=new int[palette.size()];for(int i=0;i<colors.length;i++)colors[i]=palette.get(i);
            main.post(()->{if(destroyed||request!=generation)return;if(data.length()==0){setStatus("沒有可用座標");return;}setStatus("共 "+groups.size()+" 類情況 · "+data.length()+" 個可繪製地點");com.osfans.mcpdict.UI.MapView.showThematic(this,data,title,legend.toArray(new String[0]),colors);});
        }catch(Exception e){error(e);}});
    }
    class Rows extends RecyclerView.Adapter<Rows.Holder>{
        class Holder extends RecyclerView.ViewHolder{LinearLayout row;Holder(LinearLayout v){super(v);row=v;}}
        @Override public Holder onCreateViewHolder(android.view.ViewGroup parent,int type){LinearLayout row=new LinearLayout(parent.getContext());row.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));return new Holder(row);}
        @Override public void onBindViewHolder(Holder h,int pos){Group g=groups.get(pos);h.row.removeAllViews();h.row.addView(cell(g.left,0));h.row.addView(cell(g.right,1));h.row.addView(cell(String.valueOf(g.characters.size()),2));List<String> examples=new ArrayList<>(g.characters);h.row.addView(cell(TextUtils.join(" ",examples.subList(0,Math.min(examples.size(),8)))+"  ›",3));h.row.setOnClickListener(v->showExamples(g,displayedMode));}
        @Override public int getItemCount(){return groups.size();}
    }
    @Override public void onDestroy(){destroyed=true;generation++;worker.shutdownNow();((LanguageAdapter)first.getAdapter()).changeCursor(null);((LanguageAdapter)second.getAdapter()).changeCursor(null);first.setAdapter(null);second.setAdapter(null);super.onDestroy();}
}
