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
    private Spinner spinnerFilters, spinnerShape, spinnerType, spinnerDict, spinnerProvinces, spinnerDivisions;
    private AutoCompleteTextView autoCompleteSearchLang;
    private ResultFragment fragmentResult;
    ArrayAdapter<CharSequence> adapterDivisions, adapterShape, adapterDict, adapterProvince;
    private View layoutSearchOption, layoutHzOption, layoutSearchRange, layoutShowRange;
    private View.OnTouchListener mListener;
    private boolean initProvinceSelect, initDivisionSelect;

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
            refresh();
            fragmentResult.scrollToTop();
        });
//        String query = searchView.getQuery();
//        if (!TextUtils.isEmpty(query)) searchView.setQuery(query);

        // Set up the spinner
        layoutSearchOption = selfView.findViewById(R.id.layout_search_options);
        layoutHzOption = selfView.findViewById(R.id.layout_hz_option);
        boolean showHzOption = Utils.getBool(R.string.pref_key_hz_option, false);
        layoutHzOption.setVisibility(showHzOption ? View.VISIBLE : View.GONE);
        selfView.findViewById(R.id.button_hz_option).setOnClickListener(v -> {
            boolean show = !Utils.getBool(R.string.pref_key_hz_option, false);
            Utils.putBool(R.string.pref_key_hz_option, show);
            layoutHzOption.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        layoutSearchRange = selfView.findViewById(R.id.layout_search_range);
        selfView.findViewById(R.id.button_search_range).setOnClickListener(v -> {
            boolean show = layoutSearchRange.getVisibility() != View.VISIBLE;
            layoutSearchRange.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        layoutShowRange = selfView.findViewById(R.id.layout_show_range);
        selfView.findViewById(R.id.button_show_range).setOnClickListener(v -> {
            boolean show = layoutShowRange.getVisibility() != View.VISIBLE;
            layoutShowRange.setVisibility(show ? View.VISIBLE : View.GONE);
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

        spinnerDict = selfView.findViewById(R.id.spinner_dict);
        adapterDict = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterDict.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDict.setAdapter(adapterDict);
        spinnerDict.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterDict.getItem(position).toString();
                Utils.putDict(value);
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

        spinnerProvinces = selfView.findViewById(R.id.spinner_provinces);
        adapterProvince = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterProvince.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvinces.setAdapter(adapterProvince);
        spinnerProvinces.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterProvince.getItem(position).toString();
                Utils.putProvince(value);
                if (initProvinceSelect) spinnerFilters.setSelection(DB.FILTER_PROVINCE);
                else initProvinceSelect = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDivisions = selfView.findViewById(R.id.spinner_divisions);
        adapterDivisions = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterDivisions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisions.setAdapter(adapterDivisions);
        spinnerDivisions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterDivisions.getItem(position).toString();
                Utils.putDivision(value);
                if (initDivisionSelect) spinnerFilters.setSelection(DB.FILTER_DIVISION);
                else initDivisionSelect = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        autoCompleteSearchLang = selfView.findViewById(R.id.search_lang);
        autoCompleteSearchLang.setAdapter(new LanguageAdapter(requireContext(), null, true));
        autoCompleteSearchLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        String language = Utils.getLanguage();
        autoCompleteSearchLang.setText(language);
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

        spinnerFilters = selfView.findViewById(R.id.spinner_filters);
        spinnerFilters.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.putFilter(position);
                searchView.clickSearchButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerFilters.setSelection(Utils.getFilter());

        autoCompleteSearchLang.setOnItemClickListener((adapterView, view, i, l) -> {
            String lang = autoCompleteSearchLang.getText().toString();
            Utils.putLanguage(lang);
            searchView.clickSearchButton();
        });

        // Get a reference to the SearchResultFragment
        fragmentResult = (ResultFragment) getChildFragmentManager().findFragmentById(R.id.fragment_search_result);
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

    private void refreshSearchLang() {
        String lang = Utils.getLanguage();
        if (!DB.isLang(lang)) lang = "";
        autoCompleteSearchLang.setText(lang);
    }

    private void refreshDict() {
        String[] columns = DB.getDictColumns();
        if (columns == null) return;
        adapterDict.clear();
        String head = Utils.getStringRes(R.string.dict);
        adapterDict.add(head);
        adapterDict.addAll(columns);
        String value = Utils.getDict();
        int index = TextUtils.isEmpty(value) ? -1 : adapterDict.getPosition(value);
        if (index >= adapterDict.getCount() || index < 0 ) index = 0;
        spinnerDict.setSelection(index);
    }

    private void refreshShape() {
        String[] columns = DB.getShapeColumns();
        if (columns == null) return;
        adapterShape.clear();
        String head = Utils.getStringRes(R.string.hz_shapes);
        adapterShape.add(head);
        adapterShape.addAll(columns);
        String shape = Utils.getShape();
        int index = TextUtils.isEmpty(shape) ? -1 : adapterShape.getPosition(shape);
        if (index >= adapterShape.getCount() || index < 0 ) index = 0;
        spinnerShape.setSelection(index);
    }

    private void refreshProvince() {
        String[] columns = DB.getProvinces();
        if (columns == null) return;
        adapterProvince.clear();
        String head = Utils.getStringRes(R.string.province);
        adapterProvince.add(head);
        adapterProvince.addAll(columns);
        String value = Utils.getProvince();
        int index = TextUtils.isEmpty(value) ? -1 : adapterProvince.getPosition(value);
        if (index >= adapterProvince.getCount() || index < 0 ) index = 0;
        spinnerProvinces.setSelection(index);
    }

    private void refreshDivision() {
        adapterDivisions.clear();
        String head = Utils.getStringRes(R.string.division);
        adapterDivisions.add(head);
        String[] fqs = DB.getDivisions();
        adapterDivisions.addAll(fqs);
        String value = Utils.getDivision();
        int index = TextUtils.isEmpty(value) ? -1 : adapterDivisions.getPosition(value);
        if (index >= adapterDivisions.getCount() || index < 0 ) index = 0;
        spinnerDivisions.setSelection(index);
    }

    public void refresh(String query, String label) {
        searchView.setQuery(query);
        Utils.putLabel(label);
        refreshSearchLang();
        refresh();
    }

    public void refreshAdapter() {
        if (adapterDivisions != null) refreshDivision();
        if (adapterProvince != null) refreshProvince();
        if (adapterShape != null) refreshShape();
        if (adapterDict != null) refreshDict();
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
