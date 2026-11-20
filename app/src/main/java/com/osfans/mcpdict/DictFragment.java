package com.osfans.mcpdict;

import android.database.Cursor;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import com.osfans.mcpdict.custom.CustomSchemes;
import com.osfans.mcpdict.custom.CustomSchemeFiles;
import com.osfans.mcpdict.custom.CustomLanguagePopup;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.Adapter.DivisionAdapter;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.Adapter.StringArrayAdapter;
import com.osfans.mcpdict.DB.FILTER;
import com.osfans.mcpdict.UI.MatrixTableView;
import com.osfans.mcpdict.UI.RefreshableFragment;
import com.osfans.mcpdict.UI.ResultFragment;
import com.osfans.mcpdict.UI.SearchView;
import com.osfans.mcpdict.Util.Pref;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;

public class DictFragment extends Fragment implements RefreshableFragment {

    private static final String TAG = "DictFragment";
    private View selfView;
    private SearchView searchView;
    private Spinner spinnerType, spinnerDict, spinnerProvinces, spinnerDivisions, spinnerRecommend, spinnerEditor;
    private AutoCompleteTextView acSearchLang;
    private TextView customCount, customScheme;
    private ResultFragment fragmentResult;
    ArrayAdapter<CharSequence> adapterDict, adapterProvince, adapterRecommend, adapterEditor;
    DivisionAdapter adapterDivision;
    private View layoutSearchOption, layoutSearchLang;
    private LinearLayout layoutFilters;
    private View buttonFullscreen;
    private boolean mInitialized = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // A hack to avoid nested fragments from being inflated twice
        // Reference: http://stackoverflow.com/a/14695397
        if (selfView != null) {
            ViewGroup parent = (ViewGroup) selfView.getParent();
            if (parent != null) parent.removeView(selfView);
            return selfView;
        }
        Pref.putInput("");

        // Inflate the fragment view
        selfView = inflater.inflate(R.layout.fragment_dictionary, container, false);

        // Set up the search view
        searchView = selfView.findViewById(R.id.search_view);
        searchView.setSearchButtonOnClickListener(view -> refresh());

        // Set up the spinner
        layoutSearchOption = selfView.findViewById(R.id.layout_options);
        buttonFullscreen = selfView.findViewById(R.id.button_fullscreen);
        buttonFullscreen.setOnClickListener(v -> toggleFullscreen());
        setFullscreen(Pref.getBool(R.string.pref_key_fullscreen, false));

        selfView.findViewById(R.id.button_view_toggle).setOnClickListener(v -> {
            if (fragmentResult == null) return;
            String state;
            if (!fragmentResult.isTableMode()) {
                fragmentResult.showTableMode(MatrixTableView.Orientation.LANGUAGES_AS_ROWS);
                state = "語言在左";
            } else if (fragmentResult.getTableOrientation() == MatrixTableView.Orientation.LANGUAGES_AS_ROWS) {
                fragmentResult.showTableMode(MatrixTableView.Orientation.CHARACTERS_AS_ROWS);
                state = "語言在上";
            } else {
                fragmentResult.showListMode();
                state = "列表";
            }
            v.setContentDescription("目前：" + state + "；點擊切換下一個視圖");
            androidx.appcompat.widget.TooltipCompat.setTooltipText(v, state);
        });
        selfView.findViewById(R.id.button_hz_option).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.tool);
            Menu menu = popup.getMenu();
            MenuItem item = menu.findItem(R.id.menu_item_allow_variants);
            item.setChecked(Pref.getBool(R.string.pref_key_allow_variants, true));
            item = menu.findItem(R.id.menu_item_long_text_ipa);
            item.setChecked(Pref.getBool(R.string.pref_key_show_ipa, false));
            menu.findItem(R.id.menu_item_merge_comment_results).setChecked(Pref.getBool(R.string.pref_key_merge_comment_results, false));
            item = menu.findItem(R.id.menu_group_hz_range);
            SubMenu subMenu = item.getSubMenu();
            if (subMenu != null) {
                String[] charsets = Pref.getStringArray(R.array.pref_entries_charset);
                MenuItem subItem;
                int charsetIndex = Pref.getInt(R.string.pref_key_charset);
                for (int i = 0; i < charsets.length; i ++) {
                    subItem = subMenu.add(R.id.group_hz_range, i, i, charsets[i]);
                    if (charsetIndex == i) subItem.setChecked(true);
                }
                subMenu.setGroupCheckable(R.id.group_hz_range, true, true);
            }
            // Set island/history radio group checked state
            Pref.ISLAND_HISTORY currentMode = Pref.getIslandHistory();
            int islandHistoryId;
            switch (currentMode) {
                case ISLAND_ONLY -> islandHistoryId = R.id.menu_item_island_only;
                case ISLAND_HIDE -> islandHistoryId = R.id.menu_item_island_hide;
                case HISTORY_ONLY -> islandHistoryId = R.id.menu_item_history_only;
                case HISTORY_HIDE -> islandHistoryId = R.id.menu_item_history_hide;
                default -> islandHistoryId = R.id.menu_item_both_island_history;
            }
            menu.findItem(islandHistoryId).setChecked(true);

            popup.setOnMenuItemClickListener(item1 -> {
                int id = item1.getItemId();
                int gid = item1.getGroupId();

                if (gid == R.id.group_hz_range) {
                    Pref.putInt(R.string.pref_key_charset, id);
                    search();
                }
                else if (gid == R.id.group_island_history) {
                    if (id == R.id.menu_item_both_island_history)
                        Pref.putIslandHistory(Pref.ISLAND_HISTORY.NONE.ordinal());
                    else if (id == R.id.menu_item_island_only)
                        Pref.putIslandHistory(Pref.ISLAND_HISTORY.ISLAND_ONLY.ordinal());
                    else if (id == R.id.menu_item_island_hide)
                        Pref.putIslandHistory(Pref.ISLAND_HISTORY.ISLAND_HIDE.ordinal());
                    else if (id == R.id.menu_item_history_only)
                        Pref.putIslandHistory(Pref.ISLAND_HISTORY.HISTORY_ONLY.ordinal());
                    else if (id == R.id.menu_item_history_hide)
                        Pref.putIslandHistory(Pref.ISLAND_HISTORY.HISTORY_HIDE.ordinal());
                    search();
                }
                else if (id == R.id.menu_item_merge_comment_results) {
                    Pref.putBool(R.string.pref_key_merge_comment_results, !item1.isChecked());
                    search();
                }
                else if (id == R.id.menu_item_fullscreen) toggleFullscreen();
                else if (id == R.id.menu_item_allow_variants) {
                    Pref.putBool(R.string.pref_key_allow_variants, !item1.isChecked());
                    search();
                } else if (id == R.id.menu_item_long_text_ipa) {
                    Pref.putBool(R.string.pref_key_show_ipa, !item1.isChecked());
                    search();
                }
                return true;
            });
            popup.show();
        });

        layoutSearchLang = selfView.findViewById(R.id.layout_search_lang);

        spinnerType = selfView.findViewById(R.id.spinner_type);
        ((ArrayAdapter<?>)spinnerType.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        spinnerType.setSelection(Pref.getInt(R.string.pref_key_type));
        spinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pref.putInt(R.string.pref_key_type, position);
                boolean showDictionary = (position == DB.SEARCH.DICT.ordinal());
                spinnerDict.setVisibility(showDictionary ? View.VISIBLE : View.GONE);
                layoutSearchLang.setVisibility(!showDictionary? View.VISIBLE : View.GONE);
                searchView.setHint(position);
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
                String value = Objects.toString(adapterDict.getItem(position));
                Pref.putDict(value);
                search();
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
                String value = Objects.toString(adapterRecommend.getItem(position)).split(" ")[0];
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
                String value = Objects.toString(adapterEditor.getItem(position)).split(" ")[0];
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
                String value = Objects.toString(adapterProvince.getItem(position)).split(" ")[0];
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
                String value = (position == 0) ? "" : Objects.toString(adapterDivision.getItem(position));
                Pref.putDivision(value);
                search();
                adapterDivision.getFilter().filter(value, count -> spinnerDivisions.setSelection(adapterDivision.getPosition(value)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        acSearchLang = selfView.findViewById(R.id.text_search_lang);
        acSearchLang.setAdapter(new LanguageAdapter(requireContext()));
        acSearchLang.setOnFocusChangeListener((v, focused) -> {
            if (focused) showLanguageSuggestions((AutoCompleteTextView) v);
        });
        String language = Pref.getLanguage();
        acSearchLang.setText(language, false);
        acSearchLang.setOnItemClickListener((adapterView, view, i, l) -> {
            String lang = acSearchLang.getText().toString();
            Pref.putLanguage(lang);
            search();
        });
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            String lang = "";
            acSearchLang.setText(lang);
            Pref.putLanguage(lang);
            acSearchLang.requestFocus();
        });

        customCount = selfView.findViewById(R.id.text_custom_count);
        customCount.setOnClickListener(anchor -> CustomLanguagePopup.show(anchor, this::customChanged));
        customScheme = selfView.findViewById(R.id.spinner_custom_scheme);
        customScheme.setOnClickListener(anchor -> CustomLanguagePopup.showSchemes(anchor, this::customChanged));
        getChildFragmentManager().setFragmentResultListener(CustomSchemeFiles.CHANGED,this,(key,result)->customChanged());
        selfView.findViewById(R.id.button_custom_import).setOnClickListener(v -> CustomSchemeFiles.get(this).importExcel());
        selfView.findViewById(R.id.button_custom_export).setOnClickListener(v -> CustomSchemeFiles.get(this).exportExcel());
        androidx.appcompat.widget.TooltipCompat.setTooltipText(selfView.findViewById(R.id.button_custom_import), "導入 Excel");
        androidx.appcompat.widget.TooltipCompat.setTooltipText(selfView.findViewById(R.id.button_custom_export), "導出 Excel");
        CheckBox deduplicate = selfView.findViewById(R.id.checkbox_custom_deduplicate);
        deduplicate.setChecked(CustomSchemes.deduplicate());
        deduplicate.setOnCheckedChangeListener((button, checked) -> {
            CustomSchemes.setDeduplicate(checked);
            refreshCustomCount();
            if (Pref.getFilter() == FILTER.CUSTOM) search();
        });
        // Set up the checkboxes
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
        View.OnTouchListener listener = new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    toggleFullscreen();
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = gestureDetector.onTouchEvent(event);
                if (!ret) v.performClick();
                return false;
            }
        };
        searchView.findViewById(R.id.text_query).setOnTouchListener(listener);
        selfView.setClickable(true);
        return selfView;
    }

    private void toggleLayoutFilters(FILTER filter) {
        int n = layoutFilters.getChildCount();
        layoutFilters.setVisibility(View.GONE);
        for(int i = 0; i < n; i++) {
            View v = layoutFilters.getChildAt(i);
            FILTER f = (FILTER) v.getTag();
            v.setVisibility(f.compareTo(filter) == 0 ? View.VISIBLE : View.GONE);
        }
        layoutFilters.setVisibility(View.VISIBLE);
    }

    public void setType(int value) {
        spinnerType.setSelection(value);
        Pref.putInt(R.string.pref_key_type, value);
    }

    @Override
    public void refresh() {
        refreshCustomCount();
        long start = System.currentTimeMillis();
        Cursor cursor = DB.search();
        if (fragmentResult != null) {
            Log.d(TAG, String.format("search %s cost %d ms",  Pref.getInput(), (System.currentTimeMillis() - start)));
            fragmentResult.setData(cursor);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomSchemes();
        CheckBox deduplicate = selfView.findViewById(R.id.checkbox_custom_deduplicate);
        deduplicate.setChecked(CustomSchemes.deduplicate());
        refresh();
    }

    private void refreshSearchLang() {
        String language = Pref.getLanguage();
        if (DB.isNotLang(Pref.getLabel())) language = "";
        acSearchLang.setText(language, false);
        if (acSearchLang.hasFocus()) showLanguageSuggestions(acSearchLang);
    }

    private void showLanguageSuggestions(AutoCompleteTextView input) {
        String query = input.getText().toString();
        LanguageAdapter adapter = (LanguageAdapter) input.getAdapter();
        adapter.getFilter().filter(query, count -> {
            if (count > 0 && input.isAttachedToWindow() && input.hasFocus()
                    && TextUtils.equals(query, input.getText())) {
                input.showDropDown();
            }
        });
    }

    private void refreshDict() {
        String[] columns = DB.getDictionaryColumns();
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
        adapterDivision.getFilter().filter(value, count -> {
            int index = TextUtils.isEmpty(value) ? -1 : adapterDivision.getPosition(value);
            if (index >= adapterDivision.getCount() || index < 0 ) index = 0;
            spinnerDivisions.setSelection(index);
        });
    }

    public void updateCustomLanguage(String lang) {
        Pref.putCustomLanguage(lang);
        refreshCustomCount();
        if (Pref.getFilter() == FILTER.CUSTOM) search();
    }

    private void refreshCustomSchemes() {
        if (customScheme != null) {
            String scope = CustomSchemes.scope();
            String group = scope.isEmpty() ? "全部分區" : scope.equals(CustomSchemes.ALL_SCOPE) ? "全部方案" : scope;
            String label = CustomSchemes.currentName() + " | " + CustomSchemes.displayGroup(group);
            customScheme.setText(label);
            customScheme.setContentDescription(label);
        }
        refreshCustomCount();
    }

    private void customChanged() {
        refreshCustomSchemes();
        if (Pref.getFilter() == FILTER.CUSTOM) search();
    }

    private void refreshCustomCount() {
        if (customCount != null) {
            customCount.setText(CustomSchemes.languages().size() + " 個語言");

        }
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

    @Override
    public void onStart() {
        super.onStart();
        refreshAdapter();
    }

    public void refreshAdapter() {
        refreshSearchLang();
        refreshCustomSchemes();
        if (adapterDivision != null) refreshDivision();
        if (adapterRecommend != null) refreshRecommend();
        if (adapterEditor != null) refreshEditor();
        if (adapterProvince != null) refreshProvince();
        if (adapterDict != null) refreshDict();
        requireActivity().setTitle(Pref.getTitle());
        searchView.updateButtonKeyboard();
        mInitialized = true;
        refresh();
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
        if (mInitialized) searchView.setQuery(searchView.getQuery(), true);
    }
}
