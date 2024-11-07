package com.osfans.mcpdict;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DictFragment extends Fragment implements RefreshableFragment {

    private View selfView;
    private MySearchView searchView;
    private Spinner spinnerShowLang, spinnerShape, spinnerType;
    private AutoCompleteTextView autoCompleteSearchLang;
    private ResultFragment fragmentResult;
    ArrayAdapter<CharSequence> adapterShowLang, adapterShape;
    private View layoutHzOption, layoutSearchOption;
    private View.OnTouchListener mListener;

    private void updateCurrentLanguage() {
        String lang = autoCompleteSearchLang.getText().toString();
        Utils.putLanguage(lang);
        int position = spinnerShowLang.getSelectedItemPosition();
        Utils.putInt(R.string.pref_key_show_language_index, position);
        String[] preFqs = Utils.getStringArray(R.array.pref_values_show_languages);
        String name;
        if (position < 0) name = "*";
        else if (position < preFqs.length) name = preFqs[position];
        else name = spinnerShowLang.getSelectedItem().toString();
        if (position == 1 || position == 2) {
            name = Utils.getLabel();
            if (!DB.isLang(name)) name = DB.HZ;
            if (position == 1) name = String.format("%s,%s,%s", DB.CMN, DB.GY, name);
        }
        Utils.putStr(R.string.pref_key_show_language_names, name);
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
        selfView = inflater.inflate(R.layout.dictionary_fragment, container, false);

        // Set up the search view
        searchView = selfView.findViewById(R.id.search_view);
        searchView.setSearchButtonOnClickListener(view -> {
            updateCurrentLanguage();
            refresh();
            fragmentResult.scrollToTop();
        });
//        String query = searchView.getQuery();
//        if (!TextUtils.isEmpty(query)) searchView.setQuery(query);

        // Set up the spinner
        spinnerShowLang = selfView.findViewById(R.id.spinner_show_languages);
        adapterShowLang = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterShowLang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShowLang.setAdapter(adapterShowLang);

        layoutSearchOption = selfView.findViewById(R.id.layout_search_options);
        layoutHzOption = selfView.findViewById(R.id.layout_hz_option);
        boolean showHzOption = Utils.getBool(R.string.pref_key_hz_option, false);
        layoutHzOption.setVisibility(showHzOption ? View.VISIBLE : View.GONE);
        selfView.findViewById(R.id.button_hz_option).setOnClickListener(v -> {
            boolean show = !Utils.getBool(R.string.pref_key_hz_option, false);
            Utils.putBool(R.string.pref_key_hz_option, show);
            layoutHzOption.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        
        Spinner spinnerCharset = selfView.findViewById(R.id.spinner_charset);
        spinnerCharset.setSelection(Utils.getInt(R.string.pref_key_charset, 0));
        spinnerCharset.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.putInt(R.string.pref_key_charset, position);
                searchView.clickSearchButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerType = selfView.findViewById(R.id.spinner_type);
        spinnerType.setSelection(Utils.getInt(R.string.pref_key_type, 0));
        spinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.putInt(R.string.pref_key_type, position);
                searchView.clickSearchButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    
        spinnerShape = selfView.findViewById(R.id.spinner_shape);
        adapterShape = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterShape.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShape.setAdapter(adapterShape);
        spinnerShape.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shape = adapterShape.getItem(position).toString();
                Utils.putShape(shape);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        autoCompleteSearchLang = selfView.findViewById(R.id.search_lang);
        autoCompleteSearchLang.setAdapter(new LanguageAdapter(requireContext(), null, true));
        autoCompleteSearchLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            autoCompleteSearchLang.setText("");
            autoCompleteSearchLang.requestFocus();
        });
        // Set up the checkboxes
        CheckBox checkBoxAllowVariants = selfView.findViewById(R.id.check_box_allow_variants);
        checkBoxAllowVariants.setChecked(Utils.getBool(R.string.pref_key_allow_variants, true));

        checkBoxAllowVariants.setOnCheckedChangeListener((view, isChecked) -> {
             Utils.putBool(R.string.pref_key_allow_variants, isChecked);
            searchView.clickSearchButton();
        });

        spinnerShowLang.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchView.clickSearchButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        autoCompleteSearchLang.setOnItemClickListener((adapterView, view, i, l) -> searchView.clickSearchButton());

        // Get a reference to the SearchResultFragment
        fragmentResult = (ResultFragment) getChildFragmentManager().findFragmentById(R.id.fragment_search_result);
        refreshAdapter();
        mListener = new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    toggleFullscreen();
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        };
        searchView.findViewById(R.id.text_query).setOnTouchListener(mListener);
        selfView.setClickable(true);
        selfView.setOnTouchListener(mListener);
        return selfView;
    }

    public void setType(int value) {
        spinnerType.setSelection(value);
        Utils.putInt(R.string.pref_key_type, value);
    }

    @Override
    public void refresh() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                return DB.search();
            }
            @Override
            protected void onPostExecute(Cursor cursor) {
                fragmentResult.setData(cursor);
            }
        }.execute();
    }

    private void refreshSearchAs() {
        String lang = Utils.getLanguage();
        if (TextUtils.isEmpty(lang)) lang = DB.HZ;
        autoCompleteSearchLang.setText(lang);
    }

    private void refreshShowLang() {
        adapterShowLang.clear();
        String[] preFqs = Utils.getStringArray(R.array.pref_entries_show_languages);
        adapterShowLang.addAll(preFqs);
        String[] fqs = DB.getFqs();
        adapterShowLang.addAll(fqs);
        int index = Utils.getInt(R.string.pref_key_show_language_index, 0);
        if (index >= adapterShowLang.getCount()) index = 0;
        if (index >= 0) spinnerShowLang.setSelection(index);
    }

    private void refreshShape() {
        String[] columns = DB.getShapeColumns();
        if (columns == null) return;
        adapterShape.clear();
        String head = Utils.getContext().getString(R.string.hz_shapes);
        adapterShape.add(head);
        adapterShape.addAll(columns);
        String shape = Utils.getShape();
        int index = TextUtils.isEmpty(shape) ? -1 : adapterShape.getPosition(shape);
        if (index >= adapterShape.getCount() || index < 0 ) index = 0;
        spinnerShape.setSelection(index);
    }

    public void refresh(String query, String label) {
        searchView.setQuery(query);
        Utils.putLabel(label);
        refreshSearchAs();
        refresh();
    }

    public void refreshAdapter() {
        refreshSearchAs();
        if (adapterShowLang != null) refreshShowLang();
        if (adapterShape != null) refreshShape();
    }

    public void toggleFullscreen() {
        boolean show = layoutSearchOption.getVisibility() != View.VISIBLE;
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (ab == null) return;
        if (show)
            ab.show();
        else {
            ab.hide();
        }
        layoutSearchOption.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
