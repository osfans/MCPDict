package com.osfans.mcpdict.UI;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.osfans.mcpdict.Adapter.HzAdapter;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FontUtil;

public class SearchView extends ConstraintLayout {

    private final AutoCompleteTextView editText;
    private final View clearButton, searchButton;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_search_view, this, true);

        editText = findViewById(R.id.text_query);
        FontUtil.setTypeface(editText);
        clearButton = findViewById(R.id.button_clear);
        searchButton = findViewById(R.id.button_search);

        // Toggle the clear button when user edits text
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                clearButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
            }
        });
        editText.setAdapter(new HzAdapter(context));

        // Invoke the search button when user hits Enter
        editText.setOnEditorActionListener((v, actionId, event) -> {
            searchButton.performClick();
            return true;
        });

        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(v -> editText.setText(""));

        findViewById(R.id.button_keyboard).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            Menu menu = popup.getMenu();
            int index = 0;
            menu.add(0, index++, 0, R.string.hz_shapes);
            String value = Pref.getShape();
            String[] columns = DB.getShapeColumns();
            for (String col: columns) {
                menu.add(0, index++, 0, col);
            }
            int head = 1;
            menu.setGroupCheckable(0, true, true);
            if (TextUtils.isEmpty(value)) menu.getItem(0).setChecked(true);
            else {
                for (int i = head; i < index; i++) {
                    if (value.contentEquals(columns[i - head])) {
                        menu.getItem(i).setChecked(true);
                        break;
                    }
                }
            }
            popup.setOnMenuItemClickListener(item -> {
                int position = item.getItemId();
                String shape = item.getTitle().toString();
                Pref.putShape(position == 0 ? "" : shape);
                item.setChecked(true);
                return true;
            });
            popup.show();
        });
    }

    public void setSearchButtonOnClickListener(final View.OnClickListener listener) {
        searchButton.setOnClickListener(v -> {
            // Hide the keyboard before performing the search
            editText.clearFocus();
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            saveQuery(getQuery());
            listener.onClick(v);
        });
    }

    public String getQuery() {
        return editText.getText().toString();
    }

    public void setQuery(String query, boolean submit) {
        editText.setText(query);
        saveQuery(query);
        if (submit) {
            searchButton.performClick();
        }
    }

    private void saveQuery(String query) {
        Pref.putInput(query);
    }
}
