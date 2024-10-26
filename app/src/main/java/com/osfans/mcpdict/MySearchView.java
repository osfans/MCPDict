package com.osfans.mcpdict;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MySearchView extends ConstraintLayout {

    private final EditText editText;
    private final Button clearButton;
    private final Button searchButton;

    public MySearchView(Context context) {
        this(context, null);
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_search_view, this, true);

        editText = findViewById(R.id.text_query);
        Utils.setTypeface(editText);
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

        // Invoke the search button when user hits Enter
        editText.setOnEditorActionListener((v, actionId, event) -> {
            searchButton.performClick();
            return true;
        });

        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(v -> editText.setText(""));
    }

    public void setSearchButtonOnClickListener(final View.OnClickListener listener) {
        searchButton.setOnClickListener(v -> {
            // Hide the keyboard before performing the search
            editText.clearFocus();
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            setQuery();
            listener.onClick(v);
        });
    }

    public void clickSearchButton() {
        searchButton.performClick();
    }

    public String getQuery() {
        return Utils.getInput();
    }

    public void setQuery(String query) {
        editText.setText(query);
        Utils.putInput(query);
    }

    private void setQuery() {
        String query = editText.getText().toString();
        setQuery(query);
    }
}
