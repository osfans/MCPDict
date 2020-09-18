package com.osfans.mcpdict;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.mobiRic.ui.widget.Boast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import static com.osfans.mcpdict.MCPDatabase.COL_FIRST_READING;
import static com.osfans.mcpdict.MCPDatabase.COL_HZ;
import static com.osfans.mcpdict.MCPDatabase.COL_JP_FIRST;
import static com.osfans.mcpdict.MCPDatabase.COL_LAST_READING;
import static com.osfans.mcpdict.MCPDatabase.MASK_ALL_READINGS;
import static com.osfans.mcpdict.MCPDatabase.MASK_HZ;
import static com.osfans.mcpdict.MCPDatabase.MASK_JP_ALL;

public class SearchResultFragment extends ListFragment {

    private View selfView;
    private ListView listView;
    private SearchResultCursorAdapter adapter;
    private final boolean showFavoriteButton;
    private View selectedEntry;

    private static SearchResultFragment selectedFragment;

    public SearchResultFragment() {
        this(true);
    }

    public SearchResultFragment(boolean showFavoriteButton) {
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

        // Get a reference to the ListView
        listView = selfView.findViewById(android.R.id.list);

        // Set up a context menu for each item of the search result
        registerForContextMenu(listView);

        return selfView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up the adapter
        if (adapter == null) {
            adapter = new SearchResultCursorAdapter(
                getActivity(),
                R.layout.search_result_item,
                null,
                showFavoriteButton
            );
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(ListView list, @NonNull View view, int position, long id) {
        // Show context menu on short clicks, too
        list.showContextMenuForChild(view);
    }

    private Intent getDictIntent(int i, String hz) {
        String link = MCPDatabase.getDictLink(i);
        if (TextUtils.isEmpty(link)) return null;
        String utf8 = null;
        String big5 = null;
        int unicode = hz.codePointAt(0);
        String hex = Orthography.HZ.toHex(unicode);
        try {
            utf8 = URLEncoder.encode(hz, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        try {
            big5 = URLEncoder.encode(hz, "big5");
        } catch (UnsupportedEncodingException ignored) {
        }
        if (Objects.requireNonNull(big5).equals("%3F")) big5 = null;    // Unsupported character
        link = String.format(link, utf8, hex, big5);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        Object obj = view.getTag(R.id.tag_col);
        view.setTag(R.id.tag_col, null);
        int col = obj == null ? -1 : (Integer) obj;
        selectedFragment = this;
            // This is a bug with Android: when a context menu item is clicked,
            // all fragments of this class receive a call to onContextItemSelected.
            // Therefore we need to remember which fragment created the context menu.

        // Find the Chinese character in the view being clicked
        ListView list = (ListView) view;
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position - list.getFirstVisiblePosition();
            // info.position is the position of the item in the entire list
            // but list.getChildAt() on the next line requires the position of the item in currently visible items
        selectedEntry = list.getChildAt(position);
        TextView text = selectedEntry.findViewById(R.id.text_hz);
        String hanzi = text.getText().toString();
        int tag = (Integer) selectedEntry.getTag(R.id.tag_mask);

        // Inflate the context menu
        requireActivity().getMenuInflater().inflate(R.menu.search_result_context_menu, menu);
        MenuItem itemCopy = menu.findItem(R.id.menu_item_copy_readings);
        SubMenu menuCopy = itemCopy.getSubMenu();
        MenuItem itemDict = menu.findItem(R.id.menu_item_dict_links);
        SubMenu menuDictLinks = itemDict.getSubMenu();
        MenuItem item;

        if (col < COL_HZ) {
            if ((tag & MASK_ALL_READINGS) > 0)
                menuCopy.add(MASK_ALL_READINGS, 0, 0, getString(R.string.all_reading));
            for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
                int mask = 1 << i;
                if ((tag & mask) > 0) menuCopy.add(mask, 0, 0, MCPDatabase.getSearchAsName(i));
            }
            if ((tag & MASK_JP_ALL) > 0)
                menuCopy.add(MASK_JP_ALL, 0, 0, getString(R.string.all_jp));

            for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
                int mask = 1 << i;
                String dict = MCPDatabase.getDictName(i);
                if ((tag & mask) > 0 && !TextUtils.isEmpty(dict)) {
                    item = menuDictLinks.add(dict);
                    item.setIntent(getDictIntent(i, hanzi));
                }
            }
        } else {
            String dict = MCPDatabase.getDictName(col);
            if (!TextUtils.isEmpty(dict)) {
                item = menu.add(getString(R.string.one_dict_links, hanzi, dict));
                item.setIntent(getDictIntent(col, hanzi));
            }
            menu.add(MASK_HZ, 0, 90, getString(R.string.copy_hz));
            if (col >= COL_FIRST_READING) {
                String searchAsName = MCPDatabase.getSearchAsName(col);
                item = menu.add(getString(R.string.search_homophone, hanzi, searchAsName));
                item.setOnMenuItemClickListener(i->{
                    DictionaryFragment dictionaryFragment = ((MainActivity) requireActivity()).getDictionaryFragment();
                    String query;
                    if (MCPDatabase.isDisplayOnly(col))
                        query = selectedEntry.findViewWithTag(col).getTag(R.id.tag_raw).toString();
                    else
                        query = ((TextView)selectedEntry.findViewWithTag(col)).getText().toString();
                    dictionaryFragment.refresh(query, col);
                    return true;
                });
                menu.add(1 << col, 0, 0, getString(R.string.copy_one_reading, hanzi, searchAsName));
            }
            if (((1 << col) & MASK_JP_ALL) > 0)
                menu.add(MASK_JP_ALL, 0, 0, getString(R.string.copy_all_jp));
            if ((tag & MASK_ALL_READINGS) > 0)
                menu.add(MASK_ALL_READINGS, 0, 0, getString(R.string.copy_all_reading));
            itemCopy.setVisible(false);
            itemDict.setVisible(false);
        }

        item = menu.findItem(R.id.menu_item_share_readings);
        item.setOnMenuItemClickListener(i->{
           shareReadings();
           return true;
        });

        // Determine the functionality of the "favorite" item
        item = menu.findItem(R.id.menu_item_favorite);
        Boolean is_favorite = (Boolean) selectedEntry.getTag(R.id.tag_favorite);
        item.setTitle(is_favorite ? R.string.favorite_view_or_edit : R.string.favorite_add);
        item.setOnMenuItemClickListener(i->{
            selectedEntry.findViewById(R.id.button_favorite).performClick();
            return true;
        });

        // Replace the placeholders in the menu items with the character selected
        for (Menu m : new Menu[] {menu, menuCopy, menuDictLinks}) {
            for (int i = 0; i < m.size(); i++) {
                item = m.getItem(i);
                item.setTitle(String.format(item.getTitle().toString(), hanzi));
            }
        }
    }

    public void shareReadings() {
        String text = getCopyText(selectedEntry, MASK_ALL_READINGS);
        String title = getCopyText(selectedEntry, MASK_HZ);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        startActivity(Intent.createChooser(intent, title));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedFragment != this) return false;
        int mask = item.getGroupId();
        if (mask > 0) {
            // Generate the text to copy to the clipboard
            String text = getCopyText(selectedEntry, mask);
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            Boast.showText(getActivity(), R.string.copy_done, Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    private String getCopyText(View entry, int mask) {
        int tag = (Integer) entry.getTag(R.id.tag_mask);
        if ((tag & mask) == 0) return null;

        StringBuilder sb = new StringBuilder();
        if (mask == MASK_JP_ALL || mask == MASK_ALL_READINGS) {
            String hz = getCopyText(entry, MASK_HZ);
            assert hz != null;
            sb.append(String.format("%s %s\n", hz, Orthography.HZ.toUnicode(hz)));
            for (int i = (mask == MASK_JP_ALL ? COL_JP_FIRST : COL_FIRST_READING); i <= COL_LAST_READING; i ++) {
                String s = getCopyText(entry, 1<<i);
                if (s != null) {
                    sb.append(formatReading(entry, i));
                }
            }
            return sb.toString();
        }
        int index = Integer.toBinaryString(mask).length() - 1;
        return ((TextView)entry.findViewWithTag(index)).getText().toString();
    }

    private String formatReading(String prefix, String reading) {
        String separator = reading.contains("\n") ? "\n" : " ";
        return "[" + prefix + "]" + separator + reading + "\n";
    }

    private String formatReading(View entry, int index) {
        int mask = 1 << index;
        String prefix = MCPDatabase.getName(index);
        String reading = Objects.requireNonNull(getCopyText(entry, mask));
        return formatReading(prefix, reading);
    }

    public void setData(Cursor data) {
        if (adapter == null) return;
        adapter.changeCursor(data);
    }

    public void scrollToTop() {
        listView.setSelectionAfterHeaderView();
    }

    public void scroll(int index) {
        listView.setSelection(index);
    }
}
