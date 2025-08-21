package com.osfans.mcpdict;

import static com.osfans.mcpdict.DB.ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_LAST_DICT;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_FIRST_DICT;
import static com.osfans.mcpdict.DB.COL_ZS;
import static com.osfans.mcpdict.DB.COMMENT;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.VARIANTS;
import static com.osfans.mcpdict.DB.getColor;
import static com.osfans.mcpdict.DB.getColumn;
import static com.osfans.mcpdict.DB.getColumnIndex;
import static com.osfans.mcpdict.DB.getLabel;
import static com.osfans.mcpdict.DB.getSubColor;
import static com.osfans.mcpdict.DB.getUnicode;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.DrawableMarginSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.UI.MenuSpan;
import com.osfans.mcpdict.UI.MapView;
import com.osfans.mcpdict.UI.WebView;
import com.osfans.mcpdict.UI.PopupSpan;
import com.osfans.mcpdict.UI.TextDrawable;
import com.osfans.mcpdict.Util.FontUtil;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ResultFragment extends Fragment {

    private static final String TAG = "ResultFragment";
    private View selfView;
    private View mScroll;
    private TextView mTextView;
    private WebView mWebView;
    private final boolean showFavoriteButton;
    private final Entry mEntry = new Entry();
    private boolean showMenu;
    private final HashMap<String, String> mRaws = new HashMap<>();
    private final int GROUP_READING = 1;

    private final int MSG_SEARCH_HOMOPHONE = 1;
    private final int MSG_GOTO_INFO = 2;
    private final int MSG_FAVORITE = 3;
    private final int MSG_MAP = 4;
    private final int MSG_FULLSCREEN = 5;
    private final int MSG_CUSTOM_LANGUAGE = 6;
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            DictFragment dictFragment = ((MainActivity) requireActivity()).getDictionaryFragment();
            switch (what) {
                case MSG_SEARCH_HOMOPHONE:
                    removeCallbacksAndMessages(null);
                    dictFragment.setType(1);
                    dictFragment.refresh(mEntry.raw, mEntry.lang);
                    break;
                case MSG_GOTO_INFO:
                    removeCallbacksAndMessages(null);
                    Utils.info(requireActivity(), mEntry.lang);
                    break;
                case MSG_FAVORITE:
                    removeCallbacksAndMessages(null);
                    if (mEntry.favorite) {
                        FavoriteDialogs.view(mEntry.hz, mEntry.comment);
                    } else {
                        FavoriteDialogs.add(mEntry.hz);
                    }
                    break;
                case MSG_MAP:
                    new MapView(getContext(), mEntry.hz).show();
                    break;
                case MSG_FULLSCREEN: {
                    removeCallbacksAndMessages(null);
                    dictFragment.toggleFullscreen();
                }
                    break;
                case MSG_CUSTOM_LANGUAGE:
                    removeCallbacksAndMessages(null);
                    String language = DB.getLanguageByLabel(mEntry.lang);
                    dictFragment.updateCustomLanguage(language);
                    Toast.makeText(getContext(), Utils.isCustomLanguage(language) ? R.string.add_to_custom_language_done : R.string.rm_from_custom_language_done, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private static WeakReference<ResultFragment> selectedFragment;

    public ResultFragment() {
        this(true);
    }

    public ResultFragment(boolean showFavoriteButton) {
        super();
        this.showFavoriteButton = showFavoriteButton;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // A hack to avoid nested fragments from being inflated twice
        // Reference: http://stackoverflow.com/a/14695397
        if (selfView != null) {
            ViewGroup parent = (ViewGroup) selfView.getParent();
            if (parent != null) parent.removeView(selfView);
            return selfView;
        }

        // Inflate the fragment view
        selfView = inflater.inflate(R.layout.search_result_fragment, container, false);
        mScroll = selfView.findViewById(R.id.scroll);
        mWebView = selfView.findViewById(R.id.map);
        mWebView.setTag(this);
        registerForContextMenu(mWebView);
        mTextView = new TextView(requireContext());
        mTextView.setTextAppearance(R.style.FontDetail);
        FontUtil.setTypeface(mTextView);
        mTextView.setTextIsSelectable(true);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout layout = selfView.findViewById(R.id.layout);
        layout.addView(mTextView);
        mTextView.setTag(this);
        mTextView.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NONE);
        registerForContextMenu(mTextView);
        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));

        View.OnTouchListener listener = new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    mHandler.sendEmptyMessage(MSG_FULLSCREEN);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        };
        mTextView.setOnTouchListener(listener);
        mWebView.setOnTouchListener(listener);
        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Pref.getDisplayFormat() == 2) {
            mTextView.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

    }

    private Intent getDictIntent(String lang, String hz) {
        String link = DB.getDictLink(lang);
        if (TextUtils.isEmpty(link)) return null;
        String big5 = null;
        String hex = HanZi.toUnicodeHex(hz);
        try {
            big5 = URLEncoder.encode(hz, "big5");
        } catch (UnsupportedEncodingException ignored) {
        }
        if (Objects.requireNonNull(big5).equals("%3F")) big5 = null;    // Unsupported character
        link = String.format(link, hz, hex, big5);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public void setEntry(String hz, String lang, String raw, boolean favorite, String comment) {
        mEntry.set(hz, lang, raw, favorite, comment);
    }

    public void setEntry(Entry entry) {
        mEntry.set(entry);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenuInfo menuInfo) {
        if (!showMenu) return;
        showMenu = false;
        selectedFragment = new WeakReference<>(this);
            // This is a bug with Android: when a context menu item is clicked,
            // all fragments of this class receive a call to onContextItemSelected.
            // Therefore we need to remember which fragment created the context menu.
        String hz = mEntry.hz;
        if (TextUtils.isEmpty(hz)) return;
        String col = mEntry.lang;
        boolean favorite = mEntry.favorite;

        // Inflate the context menu
        requireActivity().getMenuInflater().inflate(R.menu.search_result_context_menu, menu);
        MenuItem itemCopy = menu.findItem(R.id.menu_item_copy_readings);
        SubMenu menuCopy = itemCopy.getSubMenu();
        MenuItem itemDict = menu.findItem(R.id.menu_item_dict_links);
        SubMenu menuDictLinks = itemDict.getSubMenu();
        MenuItem item;
        List<String> cols = Arrays.asList(DB.getVisibleLanguages());

        if (TextUtils.isEmpty(col)) {
            if (cols.size() > 2)
                menuCopy.add(GROUP_READING, COL_ALL_LANGUAGES, 0, getString(R.string.all_reading));
            for (String lang: DB.getSearchColumns()) {
                if ((cols.contains(lang))) {
                    menuCopy.add(GROUP_READING, getColumnIndex(lang), 0, lang);
                }
                String dict = DB.getDictName(lang);
                if ((cols.contains(lang)) && !TextUtils.isEmpty(dict)) {
                    item = menuDictLinks.add(dict);
                    item.setIntent(getDictIntent(lang, hz));
                }
            }
        } else {
            String dict = DB.getDictName(col);
            if (!TextUtils.isEmpty(dict)) {
                item = menu.add(getString(R.string.one_dict_links, hz, dict));
                item.setIntent(getDictIntent(col, hz));
            }
            menu.add(GROUP_READING, COL_HZ, 90, getString(R.string.copy_hz));
            String language = DB.getLanguageByLabel(col);
            if (DB.isLang(col)) {
                item = menu.add(getString(Utils.isCustomLanguage(language) ? R.string.rm_from_custom_language : R.string.add_to_custom_language, language));
                item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_CUSTOM_LANGUAGE));
                item = menu.add(getString(R.string.goto_info, language));
                item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_GOTO_INFO));
                item = menu.add(getString(R.string.search_homophone, hz, language));
                item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_SEARCH_HOMOPHONE));
                menu.add(GROUP_READING, getColumnIndex(col), 0, getString(R.string.copy_one_reading, hz, language));
            }
            if (cols.size() > 2)
                menu.add(GROUP_READING, COL_ALL_LANGUAGES, 0, getString(R.string.copy_all_reading));
            itemCopy.setVisible(false);
            itemDict.setVisible(false);
        }

        item = menu.findItem(R.id.menu_item_share_readings);
        item.setOnMenuItemClickListener(i->shareReadings());

        // Determine the functionality of the "favorite" item
        item = menu.findItem(R.id.menu_item_favorite);
        item.setTitle(favorite ? R.string.favorite_view_or_edit : R.string.favorite_add);
        item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_FAVORITE));

        // Replace the placeholders in the menu items with the character selected
        for (Menu m : new Menu[] {menu, menuCopy, menuDictLinks}) {
            for (int i = 0; i < m.size(); i++) {
                item = m.getItem(i);
                item.setTitle(String.format(item.getTitle().toString(), hz));
            }
        }
    }

    public boolean shareReadings() {
        String text = getCopyText(ALL_LANGUAGES);
        String title = mEntry.hz;
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        startActivity(Intent.createChooser(intent, title));
        return true;
    }

    public void copyReadings(String lang) {
        String text = getCopyText(lang);
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("item", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), R.string.copy_done, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedFragment.get() != this) return false;
        int groupId = item.getGroupId();
        if (groupId == GROUP_READING) {
            int itemId = item.getItemId();
            // Generate the text to copy to the clipboard
            String lang = itemId == COL_ALL_LANGUAGES ? ALL_LANGUAGES : getColumn(itemId);
            copyReadings(lang);
            return true;
        }
        return false;
    }

    private String getCopyText(String col) {
        if (col.contentEquals(HZ)) return mEntry.hz;
        if (!col.contentEquals(ALL_LANGUAGES)) return mEntry.getSingleRaw();
        return mRaws.get(mEntry.hz);
    }

    private String formatReading(String label, String reading) {
        String separator = reading.contains("\n") ? "\n" : " ";
        return "[" + label + "]" + separator + reading + "\n";
    }

    public void scrollToTop() {
        //listView.setSelectionAfterHeaderView();
    }

    private void setWebData(String query, Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <html><head><style>
                  @font-face {
                    font-family: tone;
                    src: url('file:///android_res/font/tone.ttf');
                  }
                  @font-face {
                    font-family: ipa;
                    src: url('file:///android_res/font/ipa.ttf');
                  }
                  @font-face {
                    font-family: p0;
                    src: url('file:///android_res/font/p0.otf') format('opentype');
                  }
                  @font-face {
                    font-family: p2;
                    src: url('file:///android_res/font/p2.otf') format('opentype');
                  }
                  @font-face {
                    font-family: p3;
                    src: url('file:///android_res/font/p3.otf') format('opentype');
                  }
                  @font-face {
                    font-family: pua;
                    src: url('file:///android_res/font/pua.ttf');
                  }
                  @font-face {
                    font-family: nyushu;
                    src: url('file:///android_res/font/nyushu.ttf');
                  }
                  details summary::-webkit-details-marker {display: none}
                  details summary::-moz-list-bullet {font-size: 0}
                  summary {color: #808080}
                  div {
                         display:inline-block;
                         align: left;
                      }
                      .block{display:none;}
                      .row{display:block}
                      .place,.dict {
                         border: 1px black solid;
                         padding: 0 3px;
                         border-radius: 5px;
                         color: white;\
                         background: #1E90FF;
                         text-align: center;
                         vertical-align: top;
                         transform-origin: right;
                         font-size: 0.8em;
                      }
                      body {
                """);
        String feat = FontUtil.getFontFeatureSettings();
        if (!feat.isEmpty()) sb.append(String.format("font-feature-settings: %s;\n", feat));
        sb.append("      font-family: ");
        if (FontUtil.fontExFirst()) {
            sb.append(String.format("nyushu, p0, p2, p3, pua, %s; }\n", FontUtil.getDefaultFont()));
        } else {
            sb.append(FontUtil.useFontTone() ? "tone," : "ipa,");
            sb.append(String.format("%s, nyushu, p2, p3, pua; }\n", FontUtil.getDefaultFont()));
        }
        sb.append("""
                .ipa {
                    padding: 0 5px;
                }
                .desc {
                    font-size: 0.6em;
                }
                .hz {
                    font-size: 1.8em;
                    color: #9D261D;
                }
                .variant {
                    color: #808080;
                }
                .ivs {
                font-size: 1.8em;
                font-family: nyushu, p0, p2, p3, pua;
                }
                .y {
                    color: #1E90FF;
                    margin: 0 5px;
                }
                p {
                    margin: 0.2em 0;
                }
                td {
                    vertical-align: top;
                    align: left;
                }
                ul {
                    margin: 1px;
                    padding: 0px 6px;
                }
                rt {font-size: 0.9em; background-color: #F0FFF0;}
            </style></head><body>
        """);
        if (TextUtils.isEmpty(query)) {
            sb.append(DB.getIntro());
        } else if (cursor == null || cursor.getCount() == 0) {
            sb.append(getString(R.string.no_matches));
        } else {
            StringBuilder ssb = new StringBuilder();
            int n = cursor.getCount();
            String[] cols = DB.getVisibleLanguages();
            String lang = Pref.getLabel();
            boolean isZY = DB.isLang(lang) && query.length() >= 3 && n >= 3
                    && !HanZi.isBS(query)
                    && HanZi.isHz(query);
            Map<String, String> pys = new HashMap<>();
            StringBuilder hzs = new StringBuilder();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(COL_HZ);
                int i = cursor.getColumnIndex(lang);
                CharSequence py = DisplayHelper.formatIPA(lang, DisplayHelper.getRawText(cursor.getString(i)));
                if (isZY) {
                    pys.put(hz, py.toString());
                } else {
                    //hzs.append(String.format("<a href=\"#%s\">%s</a>&nbsp;", hz, hz));
                    hzs.append(hz);
                }
                String s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
                if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
                    s = String.format("(%s)", s);
                } else s = "";
                int current = cursor.getPosition();
                boolean openDetails = current < 3 && !HanZi.isUnknown(hz);
                ssb.append(String.format("<details %s><summary>" +
                        "<div class=hz>%s</div><div class=variant>%s</div></summary>", openDetails ? "open" : "", hz, s));
                ssb.append("<div style='display: block; float:right; margin-top: -2em;'>");
                String unicode = HanZi.toUnicode(hz);
                ssb.append(String.format("<div class=y onclick='mcpdict.showDict(\"%s\", %s, \"%s\")'>%s</div>", hz, COL_HZ, getUnicode(cursor), unicode));
                StringBuilder raws = new StringBuilder();
                raws.append(String.format("%s %s\n", hz, s));
                for (int j = COL_FIRST_DICT; j <= COL_LAST_DICT; j++) {
                    s = cursor.getString(j);
                    if (TextUtils.isEmpty(s)) continue;
                    String col = getColumn(j);
                    s = s.replace("\n", "<br>");
                    ssb.append(String.format(Locale.CHINESE,"<div class=y onclick='mcpdict.showDict(\"%s\", %d, \"%s\")'>%s</div>", hz, j, s, col));
                }
                ssb.append(String.format("<div class=y onclick='mcpdict.showMap(\"%s\")'>%s</div>", hz, DB.MAP));
                // "Favorite" button
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(COMMENT));
                boolean bFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DB.IS_FAVORITE)) == 1;
                int favorite = bFavorite ? 1 : 0;
                if (showFavoriteButton) {
                    String label = bFavorite ? "⭐":"⛤";
                    ssb.append(String.format(Locale.CHINESE,"<div class=y onclick='mcpdict.showFavorite(\"%s\", %d, \"%s\")'>&nbsp;%s&nbsp;</div>", hz, favorite, comment, label));
                }
                ssb.append("</div>");
                String fq = "";
                String fqTemp;
                boolean opened = false;
                if (HanZi.isUnknown(hz)) {
                    String col = Pref.getLabel();
                    if (!DB.isLang(col)) continue;
                    int index = DB.getColumnIndex(col);
                    s = cursor.getString(index);
                    if (TextUtils.isEmpty(s)) continue;
                    fqTemp = DB.getWebFq(col);
                    if (!fqTemp.contentEquals(fq)) {
                        ssb.append(String.format("<details open><summary>%s</summary>", fqTemp));
                    }
                    CharSequence ipa = DisplayHelper.formatUnknownIPA(col, s);
                    String raw = DisplayHelper.getRawText(s);
                    String label = DB.getLabel(col);
                    ssb.append(String.format(Locale.CHINESE,"<div onclick='mcpdict.onClick(\"%s\", \"%s\", \"%s\", %d, \"%s\",event.pageX, event.pageY)' class=row><div class=place style='background: linear-gradient(to left, %s, %s);'>%s</div><br><div class=ipa>%s</div></div>",
                            hz, col, raw, favorite, comment,
                            DB.getHexColor(col), DB.getHexSubColor(col), label, ipa));
                    raws.append(formatReading(label, raw));
                } else {
                    for (String col : cols) {
                        int index = DB.getColumnIndex(col);
                        s = cursor.getString(index);
                        if (TextUtils.isEmpty(s)) continue;
                        fqTemp = DB.getWebFq(col);
                        if (!fqTemp.contentEquals(fq)) {
                            if (opened) ssb.append("</details>");
                            ssb.append(String.format("<details open><summary>%s</summary>", fqTemp));
                            opened = true;
                        }
                        CharSequence ipa = DisplayHelper.formatIPA(col, s);
                        String raw = DisplayHelper.getRawText(s);
                        String label = DB.getLabel(col);
                        ssb.append(String.format(Locale.CHINESE,"<div onclick='mcpdict.onClick(\"%s\", \"%s\", \"%s\", %d, \"%s\",event.pageX, event.pageY)' class=row><div class=place style='background: linear-gradient(to left, %s, %s);'>%s</div><div class=ipa>%s</div></div>",
                                hz, col, raw, favorite, comment,
                                DB.getHexColor(col), DB.getHexSubColor(col), label, ipa));
                        fq = fqTemp;
                        raws.append(formatReading(label, raw));
                    }
                }
                mRaws.put(hz, raws.toString());
                if (opened) ssb.append("</details>");
                ssb.append("</details>");
            }
            if (isZY) {
                sb.append("<nav>");
                for (int unicode : query.codePoints().toArray()) {
                    if (!HanZi.isHz(unicode)) continue;
                    String hz = HanZi.toHz(unicode);
                    sb.append(String.format("<ruby>%s<rt>%s</rt></ruby>&nbsp;&nbsp;&nbsp;&nbsp;", hz, pys.getOrDefault(hz, "")));
                }
                sb.append("</nav>");
            } else if (n >= 2){
                sb.append("<nav>");
                sb.append(hzs);
                sb.append("</nav>");
            }
            sb.append(ssb);
        }
        mWebView.loadDataWithBaseURL(null, sb.toString(), "text/html; charset=utf-8", "utf-8", null);
    }

    private CharSequence setTextData(String query, Cursor cursor) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (TextUtils.isEmpty(query)) {
            sb.append(HtmlCompat.fromHtml(DB.getIntro(), HtmlCompat.FROM_HTML_MODE_COMPACT));
        } else if (cursor == null || cursor.getCount() == 0) {
            sb.append(getString(R.string.no_matches));
        } else {
            StringBuilder hzs = new StringBuilder();
            int count = cursor.getCount();
            String[] cols = DB.getVisibleLanguages();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(COL_HZ);
                sb.append(hz);
                hzs.append(hz);
                // Variants
                String s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
                if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
                    s = String.format("(%s)", s);
                    sb.append(s);
                }
                String unicode = HanZi.toUnicode(hz);
                sb.append(" ").append(unicode);
                // DICTS
                for (int i = COL_FIRST_DICT; i <= COL_LAST_DICT; i++) {
                    s = cursor.getString(i);
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(" ").append(getLabel(i));
                    }
                }
                sb.append("\n");
                StringBuilder sb2 = new StringBuilder();
                if (HanZi.isUnknown(hz)) {
                    String col = Pref.getLabel();
                    if (!DB.isLang(col)) continue;
                    int i = cursor.getColumnIndex(col);
                    s = cursor.getString(i);
                    if (TextUtils.isEmpty(s)) continue;
                    String label = getLabel(col);
                    sb2.append(String.format("［%s］", label));
                    sb2.append(HtmlCompat.fromHtml(DisplayHelper.formatUnknownIPA(col, s).toString(),HtmlCompat.FROM_HTML_MODE_COMPACT));
                    sb2.append("\n");
                } else {
                    for (String col : cols) {
                        int i = cursor.getColumnIndex(col);
                        s = cursor.getString(i);
                        if (TextUtils.isEmpty(s)) continue;
                        String label = getLabel(col);
                        sb2.append(String.format("［%s］", label));
                        String ipa = DisplayHelper.formatIPA(col, s).toString();
                        if (ipa.contains("<") && !ipa.contains(">")) ipa = ipa.replace("<", "&lt;");
                        sb2.append(HtmlCompat.fromHtml(ipa, HtmlCompat.FROM_HTML_MODE_COMPACT));
                        sb2.append("\n");
                    }
                }
                if (!TextUtils.isEmpty(sb2)) {
                    sb.append("──────────\n");
                    sb.append(sb2);
                }
                if (!cursor.isLast()) sb.append("══════════\n");
            }
            if (count > 1) {
                hzs.append("\n══════════\n");
                sb.insert(0, hzs);
            }
        }
        return sb.toString();
    }

    private SpannableStringBuilder setTableData(String query, Cursor cursor) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (TextUtils.isEmpty(query)) {
            ssb.append(HtmlCompat.fromHtml(DB.getIntro(), HtmlCompat.FROM_HTML_MODE_COMPACT));
        } else if (cursor == null || cursor.getCount() == 0) {
            ssb.append(getString(R.string.no_matches));
        } else {
            String s;
            float fontSize = mTextView.getTextSize() * 0.8f;
            TextDrawable.IBuilder builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(3)
                    .width((int) (fontSize * 3.4f))  // width in px
                    .height((int) (fontSize * 1.6f)) // height in px
                    .fontSize(fontSize)
                    .endConfig()
                    .roundRect(5);
            StringBuilder hzs = new StringBuilder();
            int hzCount = 0;
            int index = 0;
            int linesCount = 0;
            int n = 0;
            StringBuilder raws = new StringBuilder();
            String lastHz = "", lastLang = "";
            boolean bNewHz, bNewLang;
            Cursor dictCursor = null;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(COL_HZ);
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(COMMENT));
                boolean bFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DB.IS_FAVORITE)) == 1;
                int color = getResources().getColor(R.color.accent, requireContext().getTheme());
                bNewHz = !hz.contentEquals(lastHz);
                if (bNewHz) {
                    if (!lastHz.isEmpty()) {
                        ssb.append("\n");
                    }
                    hzs.append(hz);
                    hzCount++;
                    n = ssb.length();
                    ssb.append(hz, new ForegroundColorSpan(getColor(HZ)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.setSpan(new RelativeSizeSpan(1.8f), n, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Variants
                    s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
                    if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
                        s = String.format("(%s)", s);
                        ssb.append(s, new ForegroundColorSpan(getResources().getColor(R.color.dim, requireContext().getTheme())), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    // Unicode
                    String unicode = HanZi.toUnicode(hz);
                    dictCursor = DB.getDictCursor(hz);
                    if (dictCursor.getCount() == 1) dictCursor.moveToNext();
                    ssb.append(" " + unicode + " ", new PopupSpan(DisplayHelper.formatPopUp(hz, COL_HZ, getUnicode(dictCursor)), COL_HZ, color), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    raws.setLength(0);
                    raws.append(String.format("%s %s\n", hz, unicode));
                }
                // yb
                SpannableStringBuilder ssb2 = new SpannableStringBuilder();
                String lang = cursor.getString(COL_LANG);
                s = cursor.getString(COL_IPA);
                String zs = cursor.getString(COL_ZS);
                if (!zs.isEmpty()) s = String.format("%s{%s}", s, zs);
                if (TextUtils.isEmpty(s)) continue;
                linesCount++;
                String ipa = DisplayHelper.formatIPA(lang, s).toString();
                if (ipa.contains("<") && !ipa.contains(">")) ipa = ipa.replace("<", "&lt;");
                CharSequence cs = HtmlCompat.fromHtml(ipa, HtmlCompat.FROM_HTML_MODE_COMPACT);
                n = ssb2.length();
                if (bNewHz) lastLang = "";
                bNewLang = !lang.contentEquals(lastLang);
                String raw = DisplayHelper.getRawText(s);
                if (bNewLang) {
                    if (!lastLang.isEmpty()) ssb2.append("\n");
                    Drawable drawable = builder.build(lang, getColor(lang), getSubColor(lang));
                    DrawableMarginSpan span = new DrawableMarginSpan(drawable, 10);
                    ssb2.append(" ", span, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                Entry e = new Entry(hz, lang, raw, bFavorite, comment);
                ssb2.setSpan(new MenuSpan(e), n, ssb2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb2.append(cs);
                ssb2.append(" ");
                raws.append(formatReading(lang, raw));
                lastLang = lang;
                mRaws.put(hz, raws.toString());
                if (bNewHz) {
                    // DICTS
                    if (dictCursor.getCount() == 1) {
                        for (int i = COL_FIRST_DICT; i <= COL_LAST_DICT; i++) {
                            s = dictCursor.getString(i);
                            if (!TextUtils.isEmpty(s)) {
                                ssb.append(" " + getLabel(i) + " ", new PopupSpan(DisplayHelper.formatPopUp(hz, i, s), i, color), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                    dictCursor.close();
                    // Map
                    if (!TextUtils.isEmpty(ssb2)) {
                        ssb.append(DB.MAP + " ", new PopupSpan(hz, 0, color) {
                            @Override
                            public void onClick(@NonNull View view) {
                                view.post(() -> showMap(hz));
                            }
                        }, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    // Favorite
                    if (showFavoriteButton) {
                        String label = bFavorite ? "⭐":"⛤";
                        ssb.append(" " + label + " ", new PopupSpan(hz, 0, color) {
                            @Override
                            public void onClick(@NonNull View view) {
                                showFavorite(hz, bFavorite, comment);
                            }
                        }, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    ssb.append("\n");
                }
                lastHz = hz;
                if (linesCount > COL_ALL_LANGUAGES) continue;
                index++;
                ssb.append(ssb2);
            }
            if (hzCount > 1) {
                if (hzCount == 100) hzs.append("…");
                hzs.append("\n");
                ssb.insert(0, hzs);
            }
        }
        return ssb;
    }

    private CharSequence getTextData(String query, Cursor cursor, int format) {
        if (format == 0) return setTextData(query, cursor);
        return setTableData(query, cursor);
    }

    public void setData(String query, Cursor cursor) {
        mRaws.clear();
        int format = Pref.getDisplayFormat();
        if (format == 2) { //web
            setWebData(query, cursor);
            mWebView.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            mScroll.setScrollY(0);
        } else {
            FontUtil.setTypeface(mTextView);
            long startTime = System.currentTimeMillis();
            new AsyncTask<Void, Void, CharSequence>() {
                @Override
                protected CharSequence doInBackground(Void... params) {
                    CharSequence ssb = getTextData(query, cursor, format);
                    if (cursor != null) cursor.close();
                    return ssb;
                }

                @Override
                protected void onPostExecute(CharSequence text) {
                    mTextView.setText(text);
                    mTextView.setVisibility(View.VISIBLE);
                    mScroll.setScrollY(0);
                    Log.d(TAG, String.format("setData %s cost %d ms", query, (System.currentTimeMillis() - startTime)));
                }
            }.execute();
        }
    }

    public void setData(Cursor cursor) {
        setData(Pref.getInput(), cursor);
    }
    public void showContextMenu(float x, float y) {
        requireActivity().runOnUiThread(() -> {
            if (!mWebView.isDirty()) {
                showMenu = true;
                mWebView.showContextMenu(x, y);
                //openContextMenu(mWebView);
            }
        });
    }

    public void openContextMenu(View v) {
        showMenu = true;
        requireActivity().openContextMenu(v);
    }

    public void showMap(String hz) {
        mEntry.hz = hz;
        mHandler.sendEmptyMessage(MSG_MAP);
    }

    public void showFavorite(String hz, boolean favorite, String comment) {
        mEntry.set(hz, favorite, comment);
        mHandler.sendEmptyMessage(MSG_FAVORITE);
    }
}
