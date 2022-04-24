package com.osfans.mcpdict;

import static com.osfans.mcpdict.DB.ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_HD;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_KX;
import static com.osfans.mcpdict.DB.COMMENT;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.VARIANTS;
import static com.osfans.mcpdict.DB.getColumn;
import static com.osfans.mcpdict.DB.getColumnIndex;
import static com.osfans.mcpdict.DB.getLabel;
import static com.osfans.mcpdict.DB.getUnicode;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResultFragment extends Fragment {

    private View selfView;
    private MyWebView mWebView;
    private final boolean showFavoriteButton;
    private Entry mEntry = new Entry();
    private boolean showMenu;
    private HashMap<String, String> mRaws = new HashMap<>();
    private final int GROUP_READING = 1;

    private final int MSG_SEARCH = 1;
    private final int MSG_GOTO_INFO = 2;
    private final int MSG_FAVORITE = 3;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_SEARCH:
                    removeCallbacksAndMessages(null);
                    DictFragment dictFragment = ((MainActivity) requireActivity()).getDictionaryFragment();
                    dictFragment.refresh(mEntry.raw, mEntry.lang);
                    break;
                case MSG_GOTO_INFO:
                    removeCallbacksAndMessages(null);
                    Intent intent = new Intent(getContext(), InfoActivity.class);
                    intent.putExtra("lang", mEntry.lang);
                    startActivity(intent);
                    break;
                case MSG_FAVORITE:
                    removeCallbacksAndMessages(null);
                    if (mEntry.favorite) {
                        FavoriteDialogs.view(mEntry.hz, mEntry.comment);
                    } else {
                        FavoriteDialogs.add(mEntry.hz);
                    }
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

        mWebView = selfView.findViewById(R.id.webResult);
        mWebView.setTag(this);
        registerForContextMenu(mWebView);
        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private Intent getDictIntent(String lang, String hz) {
        String link = DB.getDictLink(lang);
        if (TextUtils.isEmpty(link)) return null;
        String big5 = null;
        String hex = Orthography.HZ.toUnicodeHex(hz);
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
        mEntry.hz = hz;
        mEntry.lang = lang;
        mEntry.raw = raw;
        mEntry.favorite = favorite;
        mEntry.comment = comment;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
        List<String> cols = Arrays.asList(DB.getVisibleColumns(getContext()));

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
            String lang = col;
            String dict = DB.getDictName(col);
            if (!TextUtils.isEmpty(dict)) {
                item = menu.add(getString(R.string.one_dict_links, hz, dict));
                item.setIntent(getDictIntent(col, hz));
            }
            menu.add(GROUP_READING, COL_HZ, 90, getString(R.string.copy_hz));
            if (DB.isLang(lang)) {
                item = menu.add(getString(R.string.goto_info, lang));
                item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_GOTO_INFO));
                item = menu.add(getString(R.string.search_homophone, hz, lang));
                item.setOnMenuItemClickListener(i->mHandler.sendEmptyMessage(MSG_SEARCH));
                menu.add(GROUP_READING, getColumnIndex(col), 0, getString(R.string.copy_one_reading, hz, lang));
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
        if (!col.contentEquals(ALL_LANGUAGES)) return mEntry.raw;
        return mRaws.get(mEntry.hz);
    }

    private String formatReading(String prefix, String reading) {
        String separator = reading.contains("\n") ? "\n" : " ";
        return "[" + prefix + "]" + separator + reading + "\n";
    }

    public void scrollToTop() {
        //listView.setSelectionAfterHeaderView();
    }

    public void setData(Cursor cursor) {
        final String query = Utils.getInput(getContext());
        StringBuilder sb = new StringBuilder();
        mRaws.clear();

        sb.append("<html><head><style>\n" +
                "  @font-face {\n" +
                "    font-family: ipa;\n" +
                "    src: url('file:///android_res/font/ipa.ttf');\n" +
                "  }\n" +
                "  @font-face {\n" +
                "    font-family: hanbcde;\n" +
                "    src: url('file:///android_res/font/hanbcde.ttf');\n" +
                "  }\n" +
                "  @font-face {\n" +
                "    font-family: hanfg;\n" +
                "    src: url('file:///android_res/font/hanfg.ttf');\n" +
                "  }\n" +
                "  details summary::-webkit-details-marker {display: none}\n" +
                "  details summary::-moz-list-bullet {font-size: 0}\n" +
                "  summary {color: #808080}\n" +
                "  div {\n" +
                "         display:inline-block;\n" +
                "         align: left;\n" +
                "      }\n" +
                "      .block{display:none;}\n" +
                "      .row{display:block}\n"+
                "      .place,.dict {\n" +
                "         border: 1px black solid;\n" +
                "         padding: 0 3px;\n" +
                "         border-radius: 5px;\n" +
                "         color: white;" +
                "         background: #1E90FF;\n" +
                "         text-align: center;\n" +
                "         vertical-align: top;\n" +
                "         transform-origin: right;\n" +
                "         font-size: 0.8em;\n" +
                "      }\n" +
                "      body {\n" +
                "         font-family: ipa, hanfg, hanbcde, sans;\n" +
                "      }\n" +
                "      .ipa {\n" +
                "         padding: 0 5px;\n" +
                "      }\n" +
                "      .desc {\n" +
                "         font-size: 0.6em;\n" +
                "      }\n" +
                "      .hz {\n" +
                "         font-size: 1.8em;\n" +
                "         color: #9D261D;\n" +
                "      }\n" +
                "      .variant {\n" +
                "         color: #808080;\n" +
                "      }\n" +
                "      .y {\n" +
                "         color: #1E90FF;\n" +
                "         margin: 0 5px;\n" +
                "      }\n" +
                "      p {\n" +
                "         margin: 0.2em 0;\n" +
                "      }\n" +
                "      td {\n" +
                "         vertical-align: top;\n" +
                "         align: left;\n" +
                "      }\n" +
                "      ul {\n" +
                "         margin: 1px;\n" +
                "         padding: 0px 6px;\n" +
                "      }" +
                "    rt {font-size: 0.9em; background-color: #F0FFF0;}  " +
                "  </style><script>" +
                "function toggleInfo(s) {" +
                "var d = document.getElementById(s); " +
                "d.style.display = d.style.display == 'block' ? 'none' : 'block';" +
                "}" +
                "</script></head><body>");
        if (TextUtils.isEmpty(query)) {
            sb.append(DB.getIntro(getContext()));
        } else if (cursor == null || cursor.getCount() == 0) {
            sb.append(getString(R.string.no_matches));
        } else {
            Orthography.setToneStyle(DictApp.getStyle(R.string.pref_key_tone_display));
            Orthography.setToneValueStyle(DictApp.getStyle(R.string.pref_key_tone_value_display));
            StringBuilder ssb = new StringBuilder();
            int n = cursor.getCount();
            String lang = Utils.getLanguage(getContext());
            boolean isZY = DB.isLang(lang) && query.length() >= 3 && n >= 3
                    && !Orthography.HZ.isBS(query)
                    && Orthography.HZ.isHz(query);
            Map<String, String> pys = new HashMap<>();
            StringBuilder hzB = new StringBuilder();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(COL_HZ);
                int i = cursor.getColumnIndex(lang);
                CharSequence py = DictApp.formatIPA(lang, DictApp.getRawText(cursor.getString(i)));
                if (isZY) {
                    pys.put(hz, py.toString());
                } else {
                    //hzB.append(String.format("<a href=\"#%s\">%s</a>&nbsp;", hz, hz));
                    hzB.append(hz);
                }
                String s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
                if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
                    s = String.format("(%s)", s);
                } else s = "";
                int current = cursor.getPosition();
                boolean openDetails = current < 3 && !hz.contentEquals("□");
                ssb.append(String.format("<details %s><summary>" +
                        "<div class=hz>%s</div><div class=variant>%s</div></summary>", openDetails ? "open" : "", hz, s));
                ssb.append("<div style='display: block; float:right; margin-top: -2em;'>");
                s = Orthography.HZ.toUnicode(hz);
                ssb.append(String.format("<div class=y onclick='toggleInfo(\"%s%s\")'>%s</div>", hz, DB.UNICODE, s));
                StringBuilder dictBuilder = new StringBuilder();
                dictBuilder.append(getUnicode(cursor));
                StringBuilder raws = new StringBuilder();
                raws.append(String.format("%s %s\n", hz, s));
                for (int j = DB.COL_SW; j <= DB.COL_HD; j++) {
                    s = cursor.getString(j);
                    if (TextUtils.isEmpty(s)) continue;
                    String col = getColumn(j);
                    ssb.append(String.format("<div class=y onclick='toggleInfo(\"%s%s\")'>%s</div>", hz, col, getLabel(j)));
                    if (j == COL_KX) s = s.replaceFirst("^(.*?)(\\d+).(\\d+)", "$1<a href=https://kangxizidian.com/kxhans/" + hz + ">第$2頁第$3字</a>");
                    else if (j == COL_HD) s = s.replaceFirst("(\\d+).(\\d+)", "<a href=https://www.homeinmists.com/hd/png/$1.png>第$1頁</a>第$2字");
                    s = DictApp.getRichText(s).toString();
                    dictBuilder.append(String.format("<div id=%s%s class=block><div class=place>%s</div><div class=ipa>%s</div><br></div>", hz, col, col, s));
                }
                ssb.append(String.format("<div class=y onclick='mcpdict.showMap(\"%s\")'>%s</div>", hz, DB.MAP));
                // "Favorite" button
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(COMMENT));
                boolean bFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DB.IS_FAVORITE)) == 1;
                int favorite = bFavorite ? 1 : 0;
                if (showFavoriteButton) {
                    String label = bFavorite ? "⭐":"⛤";
                    ssb.append(String.format("<div class=y onclick='mcpdict.showFavorite(\"%s\", %d, \"%s\")'>&nbsp;%s&nbsp;</div>", hz, favorite, comment, label));
                }
                ssb.append("</div>");
                ssb.append(dictBuilder);
                String fq = "";
                String fqTemp;
                boolean opened = false;
                for (String col : DB.getVisibleColumns(getContext())) {
                    int index = DB.getColumnIndex(col);
                    s = cursor.getString(index);
                    if (TextUtils.isEmpty(s)) continue;
                    fqTemp = DB.getFq(col);
                    if (!fqTemp.contentEquals(fq)) {
                        if (opened) ssb.append("</details>");
                        ssb.append(String.format("<details open><summary>%s</summary>", fqTemp));
                        opened = true;
                    }
                    CharSequence ipa = DictApp.formatIPA(col, s);
                    String raw = DictApp.getRawText(s);
                    String label = DB.getLabel(col);
                    ssb.append(String.format("<div onclick='mcpdict.onClick(\"%s\", \"%s\", \"%s\", %d, \"%s\",event.pageX, event.pageY)' class=row><div class=place style='background: linear-gradient(to left, %s, %s);'>%s</div><div class=ipa>%s</div></div>",
                            hz, col, raw, favorite, comment,
                            DB.getHexColor(col), DB.getHexSubColor(col), label, ipa));
                    fq = fqTemp;
                    raws.append(formatReading(label, raw));
                }
                mRaws.put(hz, raws.toString());
                if (opened) ssb.append("</details>");
                ssb.append("</details>");
            }
            if (isZY) {
                sb.append("<nav>");
                for (int unicode : query.codePoints().toArray()) {
                    if (!Orthography.HZ.isHz(unicode)) continue;
                    String hz = Orthography.HZ.toHz(unicode);
                    sb.append(String.format("<ruby>%s<rt>%s</rt></ruby>&nbsp;&nbsp;&nbsp;&nbsp;", hz, pys.getOrDefault(hz, "")));
                }
                sb.append("</nav>");
            } else if (n >= 2){
                sb.append("<nav>");
                sb.append(hzB);
                sb.append("</nav>");
            }
            sb.append(ssb);
        }
        mWebView.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);
    }

    public void showContextMenu(float x, float y) {
        requireActivity().runOnUiThread(() -> {
            if (!mWebView.isDirty()) {
                showMenu = true;
                mWebView.showContextMenu(x, y);
            }
        });
    }
}
