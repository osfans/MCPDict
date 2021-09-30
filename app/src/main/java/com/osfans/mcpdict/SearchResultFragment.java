package com.osfans.mcpdict;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.Set;

import static com.osfans.mcpdict.MCPDatabase.COL_FIRST_READING;
import static com.osfans.mcpdict.MCPDatabase.COL_HZ;
import static com.osfans.mcpdict.MCPDatabase.COL_LAST_READING;
import static com.osfans.mcpdict.MCPDatabase.COL_ALL_READINGS;

public class SearchResultFragment extends ListFragment {

    private View selfView;
    private ListView listView;
    private SearchResultCursorAdapter adapter;
    private final boolean showFavoriteButton;
    private View selectedEntry;
    private final int GROUP_READING = 1;

    private static WeakReference<SearchResultFragment> selectedFragment;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    private Intent getDictIntent(int i, String hz) {
        String link = MCPDatabase.getDictLink(i);
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

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        Object obj = view.getTag(R.id.tag_col);
        view.setTag(R.id.tag_col, null);
        int col = obj == null ? -1 : (Integer) obj;
        selectedFragment = new WeakReference<>(this);
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
        String hz = text.getText().toString();
        Set<Integer> cols = (Set) selectedEntry.getTag(R.id.tag_cols);

        // Inflate the context menu
        requireActivity().getMenuInflater().inflate(R.menu.search_result_context_menu, menu);
        MenuItem itemCopy = menu.findItem(R.id.menu_item_copy_readings);
        SubMenu menuCopy = itemCopy.getSubMenu();
        MenuItem itemDict = menu.findItem(R.id.menu_item_dict_links);
        SubMenu menuDictLinks = itemDict.getSubMenu();
        MenuItem item;

        if (col < COL_HZ) {
            if (cols.size() > 2)
                menuCopy.add(GROUP_READING, COL_ALL_READINGS, 0, getString(R.string.all_reading));
            for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
                if ((cols.contains(i))) menuCopy.add(GROUP_READING, i, 0, MCPDatabase.getFullName(i));
            }

            for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
                String dict = MCPDatabase.getDictName(i);
                if ((cols.contains(i)) && !TextUtils.isEmpty(dict)) {
                    item = menuDictLinks.add(dict);
                    item.setIntent(getDictIntent(i, hz));
                }
            }
        } else {
            String dict = MCPDatabase.getDictName(col);
            if (!TextUtils.isEmpty(dict)) {
                item = menu.add(getString(R.string.one_dict_links, hz, dict));
                item.setIntent(getDictIntent(col, hz));
            }
            menu.add(GROUP_READING, COL_HZ, 90, getString(R.string.copy_hz));
            if (col >= COL_FIRST_READING) {
                String searchAsName = MCPDatabase.getFullName(col);
                item = menu.add(getString(R.string.search_homophone, hz, searchAsName));
                item.setOnMenuItemClickListener(i->{
                    DictionaryFragment dictionaryFragment = ((MainActivity) requireActivity()).getDictionaryFragment();
                    String query = selectedEntry.findViewWithTag(col).getTag(R.id.tag_raw).toString();
                    dictionaryFragment.refresh(query, col);
                    return true;
                });
                menu.add(GROUP_READING, col, 0, getString(R.string.copy_one_reading, hz, searchAsName));
            }
            if (cols.size() > 2)
                menu.add(GROUP_READING, COL_ALL_READINGS, 0, getString(R.string.copy_all_reading));
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
                item.setTitle(String.format(item.getTitle().toString(), hz));
            }
        }
    }

    public void shareReadings() {
        String text = getCopyText(selectedEntry, COL_ALL_READINGS);
        String title = getCopyText(selectedEntry, COL_HZ);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        startActivity(Intent.createChooser(intent, title));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedFragment.get() != this) return false;
        int groupId = item.getGroupId();
        if (groupId == GROUP_READING) {
            int itemId = item.getItemId();
            // Generate the text to copy to the clipboard
            String text = getCopyText(selectedEntry, itemId);
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("item", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), R.string.copy_done, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private String getCopyText(View entry, int col) {
        Set<Integer> cols = (Set<Integer>) entry.getTag(R.id.tag_cols);

        if (cols.contains(col))
            return ((TextView)entry.findViewWithTag(col)).getText().toString();

        if (col == COL_ALL_READINGS) {
            if (cols.size() <= 2) return null;
            StringBuilder sb = new StringBuilder();
            String hz = getCopyText(entry, COL_HZ);
            assert hz != null;
            sb.append(String.format("%s %s\n", hz, Orthography.HZ.toUnicode(hz)));
            for (int i = COL_FIRST_READING; i <= COL_LAST_READING; i ++) {
                String s = getCopyText(entry, i);
                if (s != null) {
                    sb.append(formatReading(entry, i));
                }
            }
            return sb.toString();
        }
        return null;
    }

    private String formatReading(String prefix, String reading) {
        String separator = reading.contains("\n") ? "\n" : " ";
        return "[" + prefix + "]" + separator + reading + "\n";
    }

    private String formatReading(View entry, int index) {
        String prefix = MCPDatabase.getLabel(index);
        String reading = Objects.requireNonNull(getCopyText(entry, index));
        return formatReading(prefix, reading);
    }

    public void setData(Cursor data) {
        if (adapter == null) return;
        adapter.changeCursor(data);
    }

    public void scrollToTop() {
        listView.setSelectionAfterHeaderView();
    }
}
