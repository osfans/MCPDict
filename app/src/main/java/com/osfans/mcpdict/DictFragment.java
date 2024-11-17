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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.DB.FILTER;

public class DictFragment extends Fragment implements RefreshableFragment {

    private View selfView;
    private MySearchView searchView;
    private Spinner spinnerShape,  spinnerType, spinnerDict, spinnerProvinces, spinnerDivisions;
    private AutoCompleteTextView acSearchLang, acCustomLang;
    private ResultFragment fragmentResult;
    ArrayAdapter<CharSequence> adapterShape, adapterDict, adapterProvince;
    AdapterDivisions adapterDivisions;
    private View layoutSearchOption, layoutHz, layoutSearchLang;
    private LinearLayout layoutFilters;
    private View.OnTouchListener mListener;
    private Button buttonFullscreen;

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
            if (fragmentResult != null)fragmentResult.scrollToTop();
        });
//        String query = searchView.getQuery();
//        if (!TextUtils.isEmpty(query)) searchView.setQuery(query);

        // Set up the spinner
        layoutSearchOption = selfView.findViewById(R.id.layout_options);
        buttonFullscreen = selfView.findViewById(R.id.button_fullscreen);
        buttonFullscreen.setOnClickListener(v -> toggleFullscreen());
        setFullscreen(Utils.getBool(R.string.pref_key_fullscreen, false));

        layoutHz = selfView.findViewById(R.id.layout_hz);
        boolean showHzOption = Utils.getBool(R.string.pref_key_hz_option, false);
        layoutHz.setVisibility(showHzOption ? View.VISIBLE : View.GONE);
        selfView.findViewById(R.id.button_hz_option).setOnClickListener(v -> {
            boolean show = !Utils.getBool(R.string.pref_key_hz_option, false);
            Utils.putBool(R.string.pref_key_hz_option, show);
            layoutHz.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        layoutSearchLang = selfView.findViewById(R.id.layout_search_lang);

        Spinner spinnerCharset = selfView.findViewById(R.id.spinner_charset);
        spinnerCharset.setSelection(Utils.getInt(R.string.pref_key_charset, 0));
        spinnerCharset.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.putInt(R.string.pref_key_charset, position);
                search();
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
                boolean showDictionary = (position == DB.SEARCH_TYPE.DICTIONARY.ordinal());
                spinnerDict.setVisibility(showDictionary ? View.VISIBLE : View.GONE);
                layoutSearchLang.setVisibility(!showDictionary? View.VISIBLE : View.GONE);
                search();
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
                search();
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

        layoutFilters = selfView.findViewById(R.id.layout_filters);
        selfView.findViewById(R.id.layout_area).setTag(FILTER.AREA);
        selfView.findViewById(R.id.layout_current).setTag(FILTER.CURRENT);
        selfView.findViewById(R.id.layout_custom).setTag(FILTER.CUSTOM);
        selfView.findViewById(R.id.layout_division).setTag(FILTER.DIVISION);
        selfView.findViewById(R.id.layout_preset).setTag(FILTER.PRESET);

        spinnerProvinces = selfView.findViewById(R.id.spinner_provinces);
        adapterProvince = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item);
        adapterProvince.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvinces.setAdapter(adapterProvince);
        spinnerProvinces.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterProvince.getItem(position).toString().split(" ")[0];
                Utils.putProvince(value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDivisions = selfView.findViewById(R.id.spinner_divisions);
        adapterDivisions = new AdapterDivisions(requireActivity(), android.R.layout.simple_spinner_item);
        spinnerDivisions.setAdapter(adapterDivisions);
        spinnerDivisions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterDivisions.getItem(position).toString();
                adapterDivisions.setSelection(position);
                Utils.putDivision(value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        acSearchLang = selfView.findViewById(R.id.text_search_lang);
        acSearchLang.setAdapter(new LanguageAdapter(requireContext(), null, true));
        acSearchLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        String language = Utils.getLanguage();
        acSearchLang.setText(language);
        acSearchLang.setOnItemClickListener((adapterView, view, i, l) -> {
            String lang = acSearchLang.getText().toString();
            Utils.putLanguage(lang);
            search();
        });
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            acSearchLang.setText("");
            acSearchLang.requestFocus();
        });

        acCustomLang = selfView.findViewById(R.id.text_custom_lang);
        acCustomLang.setAdapter(new MultiLanguageAdapter(requireContext(), null, true));
        acCustomLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        acCustomLang.setHint(Utils.getCustomLanguageSummary());
        acCustomLang.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView tv = (TextView) view;
            String lang = tv.getText().toString();
            Utils.putCustomLanguage(lang);
            acCustomLang.setText("");
            refreshCustomLanguage();
        });
        selfView.findViewById(R.id.button_custom_lang_clear).setOnClickListener(v -> {
            acCustomLang.setText("");
            acCustomLang.requestFocus();
        });

        // Set up the checkboxes
        CheckBox checkBoxAllowVariants = selfView.findViewById(R.id.check_box_allow_variants);
        checkBoxAllowVariants.setChecked(Utils.getBool(R.string.pref_key_allow_variants, true));

        checkBoxAllowVariants.setOnCheckedChangeListener((view, isChecked) -> {
            Utils.putBool(R.string.pref_key_allow_variants, isChecked);
            search();
        });

        Spinner spinnerFilters = selfView.findViewById(R.id.spinner_filters);
        spinnerFilters.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.putFilter(position);
                FILTER filter = Utils.getFilter();
                toggleLayoutFilters(filter);
                search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerFilters.setSelection(Utils.getFilter().ordinal());

        CheckBox checkPfg = selfView.findViewById(R.id.checkbox_pfg);
        checkPfg.setChecked(Utils.getBool(R.string.pref_key_pfg, false));
        checkPfg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Utils.putBool(R.string.pref_key_pfg, isChecked);
            search();
        });

        RadioGroup radioGroup_area = selfView.findViewById(R.id.radioGroup_area);
        radioGroup_area.setOnCheckedChangeListener((group, checkedId) -> {
            int n = group.getChildCount();
            for (int i = 0; i < n; i++) {
                View radio = group.getChildAt(i);
                if (radio.getId() == checkedId) Utils.putInt(R.string.pref_key_area_level, i);
            }
            search();
        });
        RadioButton radioButton = (RadioButton) radioGroup_area.getChildAt(Utils.getInt(R.string.pref_key_area_level, 0));
        radioButton.setChecked(true);

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

    private void toggleLayoutFilters(FILTER filter) {
        int n = layoutFilters.getChildCount();
        for(int i = 0; i < n; i++) {
            View v = layoutFilters.getChildAt(i);
            FILTER f = (FILTER) v.getTag();
            v.setVisibility(f.compareTo(filter) == 0 ? View.VISIBLE : View.GONE);
        }
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
        String language = Utils.getLanguage();
        if (!DB.isLang(Utils.getLabel())) language = "";
        acSearchLang.setText(language);
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

    public void refreshCustomLanguage() {
        acCustomLang.setHint(Utils.getCustomLanguageSummary());
        search();
    }

    public void refresh(String query, String label) {
        searchView.setQuery(query);
        Utils.putLabel(label);
        refreshSearchLang();
        refresh();
    }

    public void refreshAdapter() {
        refreshSearchLang();
        if (adapterDivisions != null) refreshDivision();
        if (adapterProvince != null) refreshProvince();
        if (adapterShape != null) refreshShape();
        if (adapterDict != null) refreshDict();
    }

    public void setFullscreen(boolean full) {
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (ab == null) return;
        if (full) {
            ab.hide();
            layoutSearchOption.setVisibility(View.GONE);
            buttonFullscreen.setVisibility(View.VISIBLE);
        } else {
            ab.show();
            layoutSearchOption.setVisibility(View.VISIBLE);
            buttonFullscreen.setVisibility(View.GONE);
        }
    }

    public void toggleFullscreen() {
        boolean full = !Utils.getBool(R.string.pref_key_fullscreen, false);
        Utils.putBool(R.string.pref_key_fullscreen, full);
        setFullscreen(full);
    }
    
    private void search() {
        searchView.clickSearchButton();
    }
}
