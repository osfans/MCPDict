package com.osfans.mcpdict.Favorite;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.osfans.mcpdict.R;
import com.osfans.mcpdict.RefreshableFragment;
import com.osfans.mcpdict.Util.UserDatabase;

@SuppressLint("UseSparseArrays")
public class FavoriteFragment extends ListFragment implements RefreshableFragment {

    private View selfView;
    private View header;
    private TextView textTotal;
    private ListView listView;
    private FavoriteAdapter adapter;
    private boolean hasNewItem;

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
        selfView = inflater.inflate(R.layout.favorite_fragment, container, false);

        // Get references to some child views
        header = selfView.findViewById(R.id.favorite_header);
        textTotal = selfView.findViewById(R.id.text_total);
        listView = selfView.findViewById(android.R.id.list);

        // Set up the "management" button
        Button buttonManage = selfView.findViewById(R.id.button_favorite_manage);
        buttonManage.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.inflate(R.menu.favorite_manage_popup_menu);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if  (id == R.id.menu_item_export) {
                    FavoriteDialogs.export(false);
                    return true;
                }
                if (id == R.id.menu_item_import) {
                    FavoriteDialogs.import_(0);
                    return true;
                }
                if (id == R.id.menu_item_clear) {
                    FavoriteDialogs.deleteAll();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Set up the "import" link in the empty view
        TextView textEmpty = selfView.findViewById(android.R.id.empty);
        textEmpty.setText(textEmpty.getText(), BufferType.SPANNABLE);
        Spannable spannable = (Spannable) textEmpty.getText();
        int p = spannable.toString().length() - 5;
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Log.d("MCP", "A");
                FavoriteDialogs.import_(0);
                Log.d("MCP", "B");
            }
        }, p, p + 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textEmpty.setMovementMethod(LinkMovementMethod.getInstance());
            // The last line is necessary to make the link clickable
            // Reference: http://stackoverflow.com/a/8662457

        return selfView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the adapter
        if (adapter == null) {
            adapter = new FavoriteAdapter(getActivity(), R.layout.favorite_item, null, this);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(@NonNull final ListView list, final View view, final int position, long id) {
        // When a favorite item is clicked, display a SearchResultFragment below it
        //   to show the details about the character in the item, or hide the
        //   SearchResultFragment if it is already displayed

        // Find the Chinese character in the view being clicked
        TextView text = view.findViewById(R.id.text_hz);
        String hz = text.getText().toString();

        if (adapter.isItemExpanded(hz)) {
            adapter.collapseItem(hz, view, list);
        }
        else {
            adapter.expandItem(hz, view, list);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void refresh() {
        if (adapter == null) return;
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                return UserDatabase.selectAllFavorites();
            }

            @Override
            protected void onPostExecute(Cursor data) {
                adapter.changeCursor(data);
                if (data.getCount() == 0) {
                    header.setVisibility(View.GONE);
                } else {
                    header.setVisibility(View.VISIBLE);
                    textTotal.setText(String.format(getString(R.string.favorite_total), data.getCount()));
                }
            }
        }.execute();
        if (hasNewItem) {
            listView.setSelectionAfterHeaderView();
            hasNewItem = false;
        }
    }

    public void notifyAddItem() {
        hasNewItem = true;
    }
}
