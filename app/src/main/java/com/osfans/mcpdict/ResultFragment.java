package com.osfans.mcpdict;

import static com.osfans.mcpdict.DB.ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_ALL_LANGUAGES;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.getColumn;
import static com.osfans.mcpdict.DB.getColumnIndex;
import static com.osfans.mcpdict.DB.isLanguageHZ;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.Adapter.IndexAdapter;
import com.osfans.mcpdict.Adapter.ResultAdapter;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.UI.MapView;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ResultFragment extends Fragment {

    private static final String TAG = "ResultFragment";
    private View selfView;
    private RecyclerView mIndexView;
    private IndexAdapter mIndexAdapter;
    private ResultAdapter mResultAdapter;
    private final boolean isMainPage;
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

    public ResultFragment(boolean isMainPage) {
        super();
        this.isMainPage = isMainPage;
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
        selfView = inflater.inflate(R.layout.search_result, container, false);
        mIndexView = selfView.findViewById(R.id.index_view);
        RecyclerView recyclerView = selfView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mIndexAdapter = new IndexAdapter(recyclerView);
        mIndexView.setAdapter(mIndexAdapter);
        mResultAdapter = new ResultAdapter(isMainPage);
        recyclerView.setAdapter(mResultAdapter);
        Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));

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

    private String getCopyText(String language) {
        if (isLanguageHZ(language)) return mEntry.hz;
        if (!language.contentEquals(ALL_LANGUAGES)) return mEntry.getSingleRaw();
        return mRaws.get(mEntry.hz);
    }

    private String formatReading(String label, String reading) {
        String separator = reading.contains("\n") ? "\n" : " ";
        return "[" + label + "]" + separator + reading + "\n";
    }

    public void scrollToTop() {
        //listView.setSelectionAfterHeaderView();
    }

    public void setData(Cursor cursor) {
        mRaws.clear();
        mIndexView.setVisibility(isMainPage ? View.VISIBLE : View.GONE);
        mIndexAdapter.changeCursor(cursor);
        mResultAdapter.changeCursor(cursor);
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
