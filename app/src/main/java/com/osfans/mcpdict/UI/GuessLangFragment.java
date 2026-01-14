package com.osfans.mcpdict.UI;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.Adapter.DivisionAdapter;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.Adapter.StringArrayAdapter;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;

import org.osmdroid.util.GeoPoint;

import java.util.Locale;
import java.util.Objects;

public class GuessLangFragment extends Fragment implements RefreshableFragment {
    private View selfView;
    private TextView mTextView;
    private TextView mTextViewIPA;
    private TextView mTextInput;
    private ScrollView mScrollView, mScrollViewIPA;
    private AutoCompleteTextView mAcSearchLang;
    private LanguageAdapter mLanguageAdapter;
    private Spinner spinnerProvinces, spinnerDivisions;
    ArrayAdapter<CharSequence> adapterProvince;
    DivisionAdapter adapterDivision;

    private String mAnswer = "";
    private GeoPoint mLocation = null;
    private int mType = 0;

    @Override
    public void refresh() {
    }

    private void append(String s) {
        SpannableStringBuilder sb = new SpannableStringBuilder(HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_COMPACT));
        sb.append("\n");
        sb.append(mTextView.getText());
        mTextView.setText(sb);
        mScrollView.fullScroll(View.FOCUS_UP);
    }

    private void newGuess(String level) {
        String province = (spinnerProvinces.getSelectedItemPosition() == 0) ? "" : spinnerProvinces.getSelectedItem().toString().split(" ")[0];
        int position = spinnerDivisions.getSelectedItemPosition();
        String division = (position == 0) ? "" : Objects.requireNonNull(adapterDivision.getItem(position)).toString();
        if (!TextUtils.isEmpty(province)) division = "";
        String hint = String.format("請猜一個%s%s<b>%s</b>方言", province, division, level);
        if (mType == 1) hint += "，距離一百公里内就算猜對。";
        mLanguageAdapter.setLevel(level);
        if (!TextUtils.isEmpty(level)) level = String.format("行政區級別 MATCH '%s' AND ", level);
        if (!TextUtils.isEmpty(province)) province = String.format("省 MATCH '%s' AND ", province);
        if (!TextUtils.isEmpty(division)) division = String.format("%s MATCH '%s' AND ", DB.FQ, division);
        String sql = String.format("select 語言,經緯度 from info where %s %s %s length(經緯度) order by random() limit 1", province, division, level);
        String[] results = DB.getResults(sql);
        if (results == null || results.length < 2) {
            sql = String.format("select 語言,經緯度 from info where %s %s length(經緯度) order by random() limit 1", province, division);
            results = DB.getResults(sql);
            hint = hint.replaceFirst("<b>.*?</b>", "");
        }
        if (results == null || results.length < 2) return;
        mAnswer = results[0];
        mLocation = GeoPoint.fromInvertedDoubleString(results[1], ',');
        mTextView.setText("");
        append(hint);
        hintHz(mTextInput.getText().toString());
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
        selfView = inflater.inflate(R.layout.fragment_guess_lang, container, false);

        mTextInput = selfView.findViewById(R.id.editTextInput);
        FontUtil.setTypeface(mTextInput);
        mTextInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                hintHz(s.toString());
            }
        });
        mTextView = selfView.findViewById(R.id.textView);
        FontUtil.setTypeface(mTextView);
        mTextViewIPA = selfView.findViewById(R.id.textViewIPA);
        FontUtil.setTypeface(mTextViewIPA);

        mScrollView = selfView.findViewById(R.id.scrollView);
        mScrollViewIPA = selfView.findViewById(R.id.scrollViewIPA);
        mAcSearchLang = selfView.findViewById(R.id.text_search_lang);
        mLanguageAdapter = new LanguageAdapter(requireContext());
        mAcSearchLang.setAdapter(mLanguageAdapter);
        mAcSearchLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        mAcSearchLang.setOnItemClickListener((adapterView, view, i, l) -> checkLang());
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            mAcSearchLang.setText("");
            mAcSearchLang.requestFocus();
        });

        spinnerProvinces = selfView.findViewById(R.id.spinner_provinces);
        adapterProvince = new StringArrayAdapter(requireActivity());
        adapterProvince.add(Pref.getString(R.string.province));
        adapterProvince.addAll(DB.getArrays(DB.PROVINCE));
        spinnerProvinces.setAdapter(adapterProvince);

        spinnerDivisions = selfView.findViewById(R.id.spinner_divisions);
        adapterDivision = new DivisionAdapter(requireActivity());
        adapterDivision.add(Pref.getString(R.string.division));
        adapterDivision.addAll(DB.getDivisions());
        spinnerDivisions.setAdapter(adapterDivision);
        spinnerDivisions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = (position == 0) ? "" : Objects.toString(adapterDivision.getItem(position));
                adapterDivision.getFilter().filter(value, count -> spinnerDivisions.setSelection(adapterDivision.getPosition(value)));
                spinnerProvinces.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button buttonNew = selfView.findViewById(R.id.buttonNew);
        buttonNew.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.guess_lang, popupMenu.getMenu());
            MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_item_answer) {
                    if (!TextUtils.isEmpty(mAnswer)) {
                        append(String.format("這個方言是<b>%s</b>", mAnswer));
                        mAnswer = "";
                    }
                } else if (id == R.id.menu_item_random) {
                    mType = 0;
                    newGuess("");
                } else if (id == R.id.menu_item_guess_area) {
                    mType = 1;
                    newGuess("");
                } else {
                    mType = 0;
                    String title = "";
                    if (item.getTitle() != null) title = item.getTitle().toString();
                    newGuess(title);
                }
                return true;
            });
            popupMenu.show();
        });

        return selfView;
    }

    private void hintHz(String input) {
        if (TextUtils.isEmpty(mAnswer)) return;
        if (TextUtils.isEmpty(input)) {
            mScrollViewIPA.setVisibility(View.GONE);
            return;
        }
        String label = DB.getLabelByLanguage(mAnswer);
        mTextViewIPA.setText("");
        mScrollViewIPA.setVisibility(View.VISIBLE);
        for (int codePoint : input.codePoints().toArray()) {
            if (!HanZi.isHz(codePoint)) continue;
            String hz = HanZi.toHz(codePoint);
            String sql = String.format("select 讀音 from langs where 語言 MATCH '%s' and 字組 MATCH '%s'", label, hz);
            String result = DB.getResult(sql);
            if (TextUtils.isEmpty(result)) {
                sql = String.format("SELECT group_concat(漢字, ' OR ') from mcpdict where 異體字 MATCH '%s'", hz);
                String hzs = DB.getResult(sql);
                if (!TextUtils.isEmpty(hzs)) {
                    sql = String.format("select DISTINCT 讀音 from langs where 語言 MATCH '%s' and 字組 MATCH '%s'", label, hzs);
                    result = DB.getResult(sql);
                }
            }
            mTextViewIPA.append(hz);
            mTextViewIPA.append(" ");
            if (TextUtils.isEmpty(result)) mTextViewIPA.append("-");
            else mTextViewIPA.append(DisplayHelper.formatIPA(label, result));
            mTextViewIPA.append("\n");
        }
        mScrollViewIPA.fullScroll(View.FOCUS_DOWN);
    }

    private void checkLang() {
        if (TextUtils.isEmpty(mAnswer)) return;
        String lang = mAcSearchLang.getText().toString();
        if (TextUtils.isEmpty(lang)) return;
        if (lang.contentEquals(mAnswer)) {
            append("恭喜你，答對了！");
            mAnswer = "";
            mLocation = null;
            return;
        }
        String label = DB.getLabelByLanguage(lang);
        if (TextUtils.isEmpty(label)) return;
        GeoPoint location = DB.getPoint(label);
        if (location == null) return;
        double distance = location.distanceToAsDouble(mLocation) / 1000d;
        double angle = location.bearingTo(mLocation);
        int direction = (int)Math.round(((angle + 360) % 360) / 45) % 8;
        String directions = "⬆️↗️➡️↘️⬇️↙️⬅️↖️";
        String arrow = directions.substring(direction * 2, direction * 2 + 2);
        String hint;
        String mono = String.format(Locale.getDefault(), "%7.2fkm %5.2f%%", distance, 100 - distance / 52d).replace(" ", "&nbsp;");
        hint = String.format("%s<font face=\"monospace\">%s</font> 不是%s", arrow, mono, lang);
        append(hint);
        if (mType == 1 && distance <= 100d) {
            hint = String.format(Locale.getDefault(), "恭喜你，過關了！<br>與<b>%s</b>直綫距離已不足一百公里", mAnswer);
            append(hint);
            mAnswer = "";
            mLocation = null;
            return;
        }
    }
}
