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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.Adapter.DivisionAdapter;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.Adapter.MultiLanguageAdapter;
import com.osfans.mcpdict.Adapter.StringArrayAdapter;
import com.osfans.mcpdict.DB.FILTER;
import com.osfans.mcpdict.UI.SearchView;

public class DictFragment extends Fragment implements RefreshableFragment {

    private View selfView;
    private SearchView searchView;
    private Spinner spinnerShape,  spinnerType, spinnerDict, spinnerProvinces, spinnerDivisions, spinnerRecommend, spinnerEditor;
    private AutoCompleteTextView acSearchLang, acCustomLang;
    private ResultFragment fragmentResult;
    ArrayAdapter<CharSequence> adapterShape, adapterDict, adapterProvince, adapterRecommend, adapterEditor;
    DivisionAdapter adapterDivision;
    private View layoutSearchOption, layoutHz, layoutSearchLang;
    private LinearLayout layoutFilters;
    private View buttonFullscreen;

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
        });

        // Set up the spinner
        layoutSearchOption = selfView.findViewById(R.id.layout_options);
        buttonFullscreen = selfView.findViewById(R.id.button_fullscreen);
        buttonFullscreen.setOnClickListener(v -> toggleFullscreen());
        setFullscreen(Pref.getBool(R.string.pref_key_fullscreen, false));

        layoutHz = selfView.findViewById(R.id.layout_hz);
        boolean showHzOption = Pref.getBool(R.string.pref_key_hz_option, false);
        layoutHz.setVisibility(showHzOption ? View.VISIBLE : View.GONE);
        selfView.findViewById(R.id.button_hz_option).setOnClickListener(v -> {
            boolean show = !Pref.getBool(R.string.pref_key_hz_option, false);
            Pref.putBool(R.string.pref_key_hz_option, show);
            layoutHz.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        layoutSearchLang = selfView.findViewById(R.id.layout_search_lang);

        Spinner spinnerCharset = selfView.findViewById(R.id.spinner_charset);
        ((ArrayAdapter<?>)spinnerCharset.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        spinnerCharset.setSelection(Pref.getInt(R.string.pref_key_charset));
        spinnerCharset.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pref.putInt(R.string.pref_key_charset, position);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerType = selfView.findViewById(R.id.spinner_type);
        ((ArrayAdapter<?>)spinnerType.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        spinnerType.setSelection(Pref.getInt(R.string.pref_key_type));
        spinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pref.putInt(R.string.pref_key_type, position);
                boolean showDictionary = (position == DB.SEARCH_TYPE.DICTIONARY.ordinal());
                spinnerDict.setVisibility(showDictionary ? View.VISIBLE : View.GONE);
                layoutSearchLang.setVisibility(!showDictionary? View.VISIBLE : View.GONE);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDict = selfView.findViewById(R.id.spinner_dict);
        adapterDict = new StringArrayAdapter(requireActivity());
        spinnerDict.setAdapter(adapterDict);
        spinnerDict.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterDict.getItem(position).toString();
                Pref.putDict(value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerShape = selfView.findViewById(R.id.spinner_shape);
        adapterShape = new StringArrayAdapter(requireActivity());
        spinnerShape.setAdapter(adapterShape);
        spinnerShape.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shape = adapterShape.getItem(position).toString();
                Pref.putShape(position == 0 ? "" : shape);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        layoutFilters = selfView.findViewById(R.id.layout_filters);
        selfView.findViewById(R.id.layout_area).setTag(FILTER.AREA);
        selfView.findViewById(R.id.layout_current).setTag(FILTER.CURRENT);
        selfView.findViewById(R.id.layout_custom).setTag(FILTER.CUSTOM);
        selfView.findViewById(R.id.layout_division).setTag(FILTER.DIVISION);
        selfView.findViewById(R.id.layout_recommend).setTag(FILTER.RECOMMEND);
        selfView.findViewById(R.id.layout_editor).setTag(FILTER.EDITOR);

        spinnerRecommend= selfView.findViewById(R.id.spinner_recommend);
        adapterRecommend = new StringArrayAdapter(requireActivity());
        spinnerRecommend.setAdapter(adapterRecommend);
        spinnerRecommend.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterRecommend.getItem(position).toString().split(" ")[0];
                Pref.putStr(R.string.pref_key_recommend, position == 0 ? "" : value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerEditor= selfView.findViewById(R.id.spinner_editor);
        adapterEditor = new StringArrayAdapter(requireActivity());
        spinnerEditor.setAdapter(adapterEditor);
        spinnerEditor.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterEditor.getItem(position).toString().split(" ")[0];
                Pref.putStr(R.string.pref_key_editor, position == 0 ? "" : value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProvinces = selfView.findViewById(R.id.spinner_provinces);
        adapterProvince = new StringArrayAdapter(requireActivity());
        spinnerProvinces.setAdapter(adapterProvince);
        spinnerProvinces.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterProvince.getItem(position).toString().split(" ")[0];
                Pref.putProvince(position == 0 ? "" : value);
                search();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDivisions = selfView.findViewById(R.id.spinner_divisions);
        adapterDivision = new DivisionAdapter(requireActivity());
        spinnerDivisions.setAdapter(adapterDivision);
        spinnerDivisions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = adapterDivision.getItem(position).toString();
                Pref.putDivision(position == 0 ? "" : value);
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
        String language = Pref.getLanguage();
        acSearchLang.setText(language);
        acSearchLang.setOnItemClickListener((adapterView, view, i, l) -> {
            String lang = acSearchLang.getText().toString();
            Pref.putLanguage(lang);
            search();
        });
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            acSearchLang.setText("");
            acSearchLang.requestFocus();
        });

        acCustomLang = selfView.findViewById(R.id.text_custom_lang);
        MultiLanguageAdapter acAdapter = new MultiLanguageAdapter(requireContext(), null, true);
        acAdapter.setOnItemClickListener(view -> {
            TextView tv = (TextView) view;
            String lang = tv.getText().toString();
            updateCustomLanguage(lang);
        });
        acCustomLang.setAdapter(acAdapter);
        acCustomLang.setOnFocusChangeListener((v, b) -> {
            AutoCompleteTextView tv = (AutoCompleteTextView)v;
            if (b) tv.showDropDown();
            else tv.setText("");
        });
        acCustomLang.setHint(Pref.getCustomLanguageSummary());
        selfView.findViewById(R.id.button_custom_lang_clear).setOnClickListener(v -> {
            acCustomLang.setText("");
            acCustomLang.requestFocus();
        });

        // Set up the checkboxes
        CheckBox checkBoxAllowVariants = selfView.findViewById(R.id.check_box_allow_variants);
        checkBoxAllowVariants.setChecked(Pref.getBool(R.string.pref_key_allow_variants, true));

        checkBoxAllowVariants.setOnCheckedChangeListener((view, isChecked) -> {
            Pref.putBool(R.string.pref_key_allow_variants, isChecked);
            search();
        });

        Spinner spinnerFilters = selfView.findViewById(R.id.spinner_filters);
        ((ArrayAdapter<?>)spinnerFilters.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        spinnerFilters.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pref.putFilter(position);
                FILTER filter = Pref.getFilter();
                toggleLayoutFilters(filter);
                search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerFilters.setSelection(Pref.getFilter().ordinal());

        CheckBox checkPfg = selfView.findViewById(R.id.checkbox_pfg);
        checkPfg.setChecked(Pref.getBool(R.string.pref_key_pfg, false));
        checkPfg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Pref.putBool(R.string.pref_key_pfg, isChecked);
            search();
        });

        Spinner spinnerAreaLevel = selfView.findViewById(R.id.spinner_area_level);
        ((ArrayAdapter<?>)spinnerAreaLevel.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        spinnerAreaLevel.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                Pref.putInt(R.string.pref_key_area_level, i);
                search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerAreaLevel.setSelection(Pref.getInt(R.string.pref_key_area_level));

        // Get a reference to the SearchResultFragment
        fragmentResult = (ResultFragment) getChildFragmentManager().findFragmentById(R.id.fragment_search_result);
        refreshAdapter();
        View.OnTouchListener listener = new View.OnTouchListener() {
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
        searchView.findViewById(R.id.text_query).setOnTouchListener(listener);
        selfView.setClickable(true);
        selfView.setOnTouchListener(listener);
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
        Pref.putInt(R.string.pref_key_type, value);
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
                if (fragmentResult != null) {
                    fragmentResult.setData(cursor);
                    fragmentResult.scrollToTop();
                }
            }
        }.execute();
    }

    private void refreshSearchLang() {
        String language = Pref.getLanguage();
        if (!DB.isLang(Pref.getLabel())) language = "";
        acSearchLang.setText(language);
    }

    private void refreshDict() {
        String[] columns = DB.getDictColumns();
        if (columns == null) return;
        adapterDict.clear();
        String head = Pref.getString(R.string.dict);
        adapterDict.add(head);
        adapterDict.addAll(columns);
        String value = Pref.getDict();
        int index = TextUtils.isEmpty(value) ? -1 : adapterDict.getPosition(value);
        if (index >= adapterDict.getCount() || index < 0 ) index = 0;
        spinnerDict.setSelection(index);
    }

    private void refreshShape() {
        String[] columns = DB.getShapeColumns();
        if (columns == null) return;
        adapterShape.clear();
        String head = Pref.getString(R.string.hz_shapes);
        adapterShape.add(head);
        adapterShape.addAll(columns);
        String shape = Pref.getShape();
        int index = TextUtils.isEmpty(shape) ? -1 : adapterShape.getPosition(shape);
        if (index >= adapterShape.getCount() || index < 0 ) index = 0;
        spinnerShape.setSelection(index);
    }

    private void refreshProvince() {
        String[] columns = DB.getArrays(DB.PROVINCE);
        adapterProvince.clear();
        String head = Pref.getString(R.string.province);
        adapterProvince.add(head);
        adapterProvince.addAll(columns);
        String value = Pref.getProvince();
        int index = adapterProvince.getPosition(value);
        if (index >= adapterProvince.getCount() || index < 0 ) index = 0;
        spinnerProvinces.setSelection(index);
    }

    private void refreshRecommend() {
        String[] columns = DB.getArrays(DB.RECOMMEND);
        adapterRecommend.clear();
        String head = Pref.getString(R.string.recommend);
        adapterRecommend.add(head);
        adapterRecommend.addAll(columns);
        String value = Pref.getStr(R.string.pref_key_recommend, "");
        int index = adapterRecommend.getPosition(value);
        if (index >= adapterRecommend.getCount() || index < 0 ) index = 0;
        spinnerRecommend.setSelection(index);
    }

    private void refreshEditor() {
        String[] columns = DB.getArrays(DB.EDITOR);
        adapterEditor.clear();
        String head = Pref.getString(R.string.editor);
        adapterEditor.add(head);
        adapterEditor.addAll(columns);
        String value = Pref.getStr(R.string.pref_key_editor, "");
        int index = adapterEditor.getPosition(value);
        if (index >= adapterEditor.getCount() || index < 0 ) index = 0;
        spinnerEditor.setSelection(index);
    }

    private void refreshDivision() {
        adapterDivision.clear();
        String head = Pref.getString(R.string.division);
        adapterDivision.add(head);
        String[] fqs = DB.getDivisions();
        adapterDivision.addAll(fqs);
        String value = Pref.getDivision();
        int index = TextUtils.isEmpty(value) ? -1 : adapterDivision.getPosition(value);
        if (index >= adapterDivision.getCount() || index < 0 ) index = 0;
        spinnerDivisions.setSelection(index);
    }

    public void updateCustomLanguage(String lang) {
        Pref.putCustomLanguage(lang);
        acCustomLang.setHint(Pref.getCustomLanguageSummary());
        search();
    }

    public void refresh(String query, String label) {
        searchView.setQuery(query, false);
        Pref.putLabel(label);
        refresh(query);
    }

    public void refresh(String query) {
        Pref.putInput(query);
        refreshSearchLang();
        refresh();
    }

    public void refreshAdapter() {
        refreshSearchLang();
        if (adapterDivision != null) refreshDivision();
        if (adapterRecommend != null) refreshRecommend();
        if (adapterEditor != null) refreshEditor();
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
        boolean full = !Pref.getBool(R.string.pref_key_fullscreen, false);
        Pref.putBool(R.string.pref_key_fullscreen, full);
        setFullscreen(full);
    }
    
    private void search() {
        searchView.setQuery(searchView.getQuery(), true);
    }
}
