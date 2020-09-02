package com.osfans.mcpdict;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import com.mobiRic.ui.widget.Boast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import static com.osfans.mcpdict.MCPDatabase.MASK_ALL_READINGS;
import static com.osfans.mcpdict.MCPDatabase.MASK_JP_ALL;
import static com.osfans.mcpdict.MCPDatabase.MASK_MC;

@SuppressWarnings("deprecation")
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onListItemClick(ListView list, View view, int position, long id) {
        // Show context menu on short clicks, too
        list.showContextMenuForChild(view);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
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
        int unicode = hanzi.codePointAt(0);
        int tag = (Integer) selectedEntry.getTag();

        // Inflate the context menu
        getActivity().getMenuInflater().inflate(R.menu.search_result_context_menu, menu);
        SubMenu menuCopy = menu.getItem(0).getSubMenu();
        SubMenu menuDictLinks = menu.getItem(1).getSubMenu();
        MenuItem item;

        if ((tag & MASK_ALL_READINGS) > 0) menuCopy.add(MASK_ALL_READINGS, 0, 0, getString(R.string.copy_all));
        for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
            int mask = 1 << i;
            if ((tag & mask) > 0) menuCopy.add(mask, 0, 0, MCPDatabase.getSearchAsName(i));
        }
        if ((tag & MASK_JP_ALL) > 0) menuCopy.add(MASK_JP_ALL, 0, 0, getString(R.string.copy_jp_all));


        // Determine whether to enable each item in the sub-menu of external dictionaries,
        // and generate links for enabled items
        String utf8 = null;
        String big5 = null;
        String hex = Orthography.Hanzi.getHex(unicode);
        try {utf8 = URLEncoder.encode(hanzi, "utf-8");} catch (UnsupportedEncodingException ignored) {}
        try {big5 = URLEncoder.encode(hanzi, "big5");} catch (UnsupportedEncodingException ignored) {}
        if (Objects.requireNonNull(big5).equals("%3F")) big5 = null;    // Unsupported character
        for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
            int mask = 1 << i;
            String dict = MCPDatabase.getDictName(i);
            if ((tag & mask) > 0 && !TextUtils.isEmpty(dict)) {
                String link = MCPDatabase.getDictLink(i);
                item = menuDictLinks.add(dict);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(link, utf8, hex, big5)));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                item.setIntent(intent);
            }
        }

        // Determine the functionality of the "favorite" item
        item = menu.getItem(2);
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
                item.setTitle(String.format(item.getTitle().toString(), Orthography.Hanzi.toString(unicode)));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (selectedFragment != this) return false;
        int mask = item.getGroupId();
        if (mask > 0) {
            // Generate the text to copy to the clipboard
            String text = getCopyText(selectedEntry, mask);
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            String label = item.getTitle().toString();//.substring(2);     // this is ugly
            String message = String.format(getString(R.string.copy_done), label);
            Boast.showText(getActivity(), message, Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    private String getCopyText(View entry, int mask) {
        int tag = (Integer) entry.getTag();
        if ((tag & mask) == 0) return null;
        int index = Integer.toBinaryString(mask).length() - 1;

        TextView[] textViewDetails = {
                entry.findViewById(R.id.text_hz),
                entry.findViewById(R.id.text_unicode),
                entry.findViewById(R.id.text_mc),
                entry.findViewById(R.id.text_c3),
                entry.findViewById(R.id.text_c4),
                entry.findViewById(R.id.text_c5),
                entry.findViewById(R.id.text_c6),
                entry.findViewById(R.id.text_c7),
                entry.findViewById(R.id.text_c8),
                entry.findViewById(R.id.text_c9),
                entry.findViewById(R.id.text_c10),
                entry.findViewById(R.id.text_c11),
                entry.findViewById(R.id.text_c12),
                entry.findViewById(R.id.text_c13),
                entry.findViewById(R.id.text_c14),
                entry.findViewById(R.id.text_c15),
                entry.findViewById(R.id.text_c16),
                entry.findViewById(R.id.text_c17),
                entry.findViewById(R.id.text_c18),
                entry.findViewById(R.id.text_c19),
                entry.findViewById(R.id.text_c20)
        };
        StringBuilder sb;
        if (mask == MASK_MC) {
            String[] readings = ((TextView) entry.findViewById(R.id.text_mc)).getText().toString().split("\n");
            String[] details = ((TextView) entry.findViewById(R.id.text_mc_detail)).getText().toString().split("\n");
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < readings.length; i++) {
                if (i > 0) text.append("\n");
                text.append(readings[i]).append(details[i]);
            }
            return text.toString();
        } else if (mask == MASK_JP_ALL) {
            sb = new StringBuilder();
            for (int i = MCPDatabase.COL_JP_FIRST; i <= MCPDatabase.COL_LAST_READING; i++) {
                if ((tag & (1 << i)) > 0) sb.append(formatReading(entry, i));
            }
            return sb.toString();
        } else if (mask == MASK_ALL_READINGS) {
            sb = new StringBuilder();
            String hanzi = ((TextView) entry.findViewById(R.id.text_hz)).getText().toString();
            String unicode = ((TextView) entry.findViewById(R.id.text_unicode)).getText().toString();
            sb.append(hanzi).append(" ").append(unicode).append("\n");
            for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
                if ((tag & (1 << i)) > 0) sb.append(formatReading(entry, i));
            }
            return sb.toString();
        } else {
            return textViewDetails[index].getText().toString();
        }
        //return null;
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
}
