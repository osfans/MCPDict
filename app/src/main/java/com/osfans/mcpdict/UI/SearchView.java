package com.osfans.mcpdict.UI;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.osfans.mcpdict.Adapter.HzAdapter;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.App;

import java.util.Objects;

public class SearchView extends ConstraintLayout {

    private final MultiAutoCompleteTextView editText;
    private final View clearButton, searchButton;
    private final ImageButton buttonKeyboard;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_search, this, true);

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
        editText.setTokenizer(new HanZi.Tokenizer());
        editText.setAdapter(new HzAdapter(context));

        // Invoke the search button when user hits Enter
        editText.setOnEditorActionListener((v, actionId, event) -> {
            searchButton.performClick();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editText.setAccessibilityDelegate(new AccessibilityDelegate() {
                @Override
                public void sendAccessibilityEvent(@NonNull View host, int eventType) {
                    super.sendAccessibilityEvent(host, eventType);
                    if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                        editText.refreshAutoCompleteResults();
                    }
                }
            });
            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) editText.refreshAutoCompleteResults();
            });
        }

        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(v -> editText.setText(""));

        buttonKeyboard = findViewById(R.id.button_keyboard);
        updateButtonKeyboard();
        buttonKeyboard.setOnClickListener(v -> {
            String[] columns = DB.getShapeColumns();
            if (columns == null) return;
            PopupMenu popup = new PopupMenu(context, v);
            popup.inflate(R.menu.input);
            Menu menu = popup.getMenu();
            int index = menu.size();
            int head = index;
            String shape = Pref.getShape();
            String shape_code = Pref.getString(R.string.shape_code);
            String yin_code = Pref.getString(R.string.yin_code);
            for (String col: columns) {
                boolean isGray = col.contentEquals(shape_code) || col.contentEquals(yin_code);
                MenuItem item = menu.add(isGray ? -1 : 0, index++, 0, col);
                if (isGray) {
                    item.setEnabled(false);
                    item.setCheckable(false);
                }
            }
            menu.setGroupCheckable(0, true, true);
            popup.setForceShowIcon(true);
            boolean selected = false;
            if (DB.isHzInput()) {
                menu.getItem(0).setChecked(true);
                selected = true;
            }
            else if (DB.isYinPrompt()) {
                menu.getItem(1).setChecked(true);
                selected = true;
            }
            else {
                for (int i = head; i < index; i++) {
                    if (shape.contentEquals(columns[i - head])) {
                        menu.getItem(i).setChecked(true);
                        selected = true;
                        break;
                    }
                }
            }
            if (!selected) {
                menu.getItem(0).setChecked(true);
                Pref.putShape(Pref.getString(R.string.hz_input));
            }
            popup.setOnMenuItemClickListener(item -> {
                String title = Objects.requireNonNull(item.getTitle()).toString();
                Pref.putShape(title);
                item.setChecked(true);
                updateButtonKeyboard();
                return true;
            });
            popup.show();
        });
    }

    public void updateButtonKeyboard() {
        String shape = Pref.getShape();
        if (TextUtils.isEmpty(shape) || shape.contentEquals(Pref.getString(R.string.hz_input))) {
            buttonKeyboard.setImageResource(R.drawable.ic_keyboard);
            return;
        }
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(3)
                .width(buttonKeyboard.getWidth())
                .height(buttonKeyboard.getHeight())
                .fontSize(editText.getTextSize() * 0.8f)
                .textColor(App.obtainColor(getContext(), android.R.attr.textColorPrimary))
                .endConfig()
                .roundRect(5);
        String label = shape.substring(0, 1);
        if (shape.contentEquals(DB.HK)) label = shape.substring(1);
        buttonKeyboard.setImageDrawable(builder.build(label, android.R.color.transparent));
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

    public void setHint(int position) {
        editText.setHint(
            switch (DB.SEARCH.values()[position]) {
                case YIN -> R.string.search_ipa_hint;
                case COMMENT -> R.string.search_comment_hint;
                case DICT -> R.string.search_dict_hint;
                default -> R.string.search_hz_hint;
            }
        );
    }
}
