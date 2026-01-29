package com.osfans.mcpdict.UI;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.osfans.mcpdict.Adapter.DivisionAdapter;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.Adapter.StringArrayAdapter;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;

import org.osmdroid.util.GeoPoint;

import java.util.List;
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
    private int mDiameter = 0;
    private String mDivision = "";
    private String mArea = "";
    private final static String FS = "－";
    private enum GUESS {
        LANGUAGE, DISTANCE, AREA_PROVINCE, AREA_CITY, AREA_COUNTY, DIVISION, SUBDIVISION,
    }
    private GUESS mType = GUESS.LANGUAGE;
    boolean hintDirection, hintDistance;
    private final int MIN_DIAMETER = 10;
    private final int MAX_DIAMETER = 200;

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

    private String formatDistanceMessage(int d) {
        Locale chineseNumbers = new Locale("en_US@numbers=hant");
        NumberFormat formatter = NumberFormat.getInstance(chineseNumbers);
        return String.format("%s里", formatter.format(d));
    }

    private void alertArea() {
        FragmentActivity activity = requireActivity();
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.guess_area, activity.findViewById(R.id.root));
        final SeekBar seekBar = layout.findViewById(R.id.seekbar);
        final TextView sbValue = layout.findViewById(R.id.seekbar_value);
        int diameter = Pref.getInt(R.string.pref_key_guess_area_diameter, MAX_DIAMETER);
        diameter = Math.max(diameter, MIN_DIAMETER);
        diameter = Math.min(diameter, MAX_DIAMETER);
        seekBar.setMax(MAX_DIAMETER / 10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(MIN_DIAMETER / 10);
        }
        seekBar.setProgress(diameter / 10);
        sbValue.setText(formatDistanceMessage(diameter));

        new AlertDialog.Builder(activity)
                .setTitle(R.string.guess_area)
                .setMessage(R.string.guess_diameter_help)
                .setView(layout)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> {
                    mDiameter = seekBar.getProgress() * 10;
                    mDiameter = Math.max(mDiameter, MIN_DIAMETER);
                    mDiameter = Math.min(mDiameter, MAX_DIAMETER);
                    Pref.putInt(R.string.pref_key_guess_area_diameter, mDiameter);
                    mType = GUESS.DISTANCE;
                    newGuess("");
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser) {
                sbValue.setText(formatDistanceMessage(progress * 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {
            }
        });
    }

    private void newGuess(String level) {
        String province = (spinnerProvinces.getSelectedItemPosition() == 0) ? "" : spinnerProvinces.getSelectedItem().toString().split(" ")[0];
        int position = spinnerDivisions.getSelectedItemPosition();
        String division = (position == 0) ? "" : Objects.requireNonNull(adapterDivision.getItem(position)).toString();
        if (!TextUtils.isEmpty(province)) division = "";
        String hint = String.format("請猜一個%s%s<b>%s</b>方言", province, division, level);
        if (mType == GUESS.DISTANCE) hint += String.format(Locale.getDefault(), "，距離%s之内即可通關。", formatDistanceMessage(mDiameter));
        mLanguageAdapter.setLevel(level);
        if (!TextUtils.isEmpty(level)) level = String.format("行政區級別 MATCH '%s' AND ", level);
        if (!TextUtils.isEmpty(province)) province = String.format("省 MATCH '%s' AND ", province);
        if (!TextUtils.isEmpty(division)) division = String.format("%s MATCH '%s' AND ", DB.FQ, division);
        String sql = String.format("select 語言,經緯度 from info where %s %s %s length(經緯度) order by random() limit 1", province, division, level);
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) {
            sql = String.format("select 語言,經緯度 from info where %s %s length(經緯度) order by random() limit 1", province, division);
            cursor = DB.getCursor(sql);
            hint = hint.replaceFirst("<b>.*?</b>", "");
        }
        if (cursor == null) return;
        mAnswer = cursor.getString(0);
        mLocation = GeoPoint.fromInvertedDoubleString(cursor.getString(1), ',');
        cursor.close();
        mTextView.setText("");
        append(hint);
        hintHz(mTextInput.getText().toString());
    }

    private void newGuessDivision(String title) {
        String sql = String.format("select 語言,經緯度,省,市,縣,%s from info where length(經緯度) and length(%s) order by random() limit 1", DB.FQ, DB.FQ);
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return;
        mAnswer = cursor.getString(0);
        mLocation = GeoPoint.fromInvertedDoubleString(cursor.getString(1), ',');
        mDivision = getDivision(cursor);
        cursor.close();
        mTextView.setText("");
        append(Pref.getString(R.string.guess_area_division_hint, title));
        hintHz(mTextInput.getText().toString());
    }

    private void newGuessArea(String title) {
        String sql = "select 語言,經緯度,省,市,縣 from info where length(經緯度) and 省 != '海外' order by random() limit 1";
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return;
        mAnswer = cursor.getString(0);
        mLocation = GeoPoint.fromInvertedDoubleString(cursor.getString(1), ',');
        mArea = getArea(cursor);
        cursor.close();
        mTextView.setText("");
        append(Pref.getString(R.string.guess_area_hint, title));
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

        hintDirection = Pref.getBool(R.string.pref_key_hint_direction, true);
        hintDistance = Pref.getBool(R.string.pref_key_hint_distance, true);

        Button buttonNew = selfView.findViewById(R.id.buttonNew);
        buttonNew.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.guess_lang, menu);
            MenuCompat.setGroupDividerEnabled(menu, true);
            menu.findItem(R.id.menu_item_answer).setEnabled(!TextUtils.isEmpty(mAnswer));
            menu.findItem(R.id.menu_item_guess_copy).setEnabled(!TextUtils.isEmpty(getCopyGuess()));
            menu.findItem(R.id.menu_item_hint_direction).setChecked(hintDirection);
            menu.findItem(R.id.menu_item_hint_distance).setChecked(hintDistance);

            popupMenu.setOnMenuItemClickListener(item -> {
                int gid = item.getGroupId();
                int id = item.getItemId();
                if (id == R.id.menu_item_answer) {
                    if (!TextUtils.isEmpty(mAnswer)) {
                        String hint = String.format("這個方言是<b>%s</b>", mAnswer);
                        if (mType == GUESS.DIVISION || mType == GUESS.SUBDIVISION) {
                            hint += String.format("(%s)", mDivision);
                        }
                        if (mType == GUESS.AREA_PROVINCE || mType == GUESS.AREA_CITY || mType == GUESS.AREA_COUNTY) {
                            hint += String.format("(%s)", mArea);
                        }
                        append(hint);
                        mAnswer = "";
                    }
                } else if (id == R.id.menu_item_guess_copy) {
                    copyGuess();
                } else if (gid == R.id.group_level){
                    mType = GUESS.LANGUAGE;
                    mDiameter = 0;
                    if (id == R.id.menu_item_random) {
                        newGuess("");
                    } else {
                        newGuess(Objects.requireNonNull(item.getTitle()).toString());
                    }
                } else if (id == R.id.menu_item_guess_area) {
                    alertArea();
                } else if (id == R.id.menu_item_hint_direction) {
                    hintDirection = !hintDirection;
                    Pref.putBool(R.string.pref_key_hint_direction, hintDirection);
                } else if (id == R.id.menu_item_hint_distance) {
                    hintDistance = !hintDistance;
                    Pref.putBool(R.string.pref_key_hint_distance, hintDistance);
                } else if (id == R.id.menu_item_guess_division) {
                    mType = GUESS.DIVISION;
                    newGuessDivision(Objects.requireNonNull(item.getTitle()).toString());
                } else if (id == R.id.menu_item_guess_sub_division) {
                    mType = GUESS.SUBDIVISION;
                    newGuessDivision(Objects.requireNonNull(item.getTitle()).toString());
                } else if (id == R.id.menu_item_area_province) {
                    mType = GUESS.AREA_PROVINCE;
                    newGuessArea(Objects.requireNonNull(item.getTitle()).toString());
                } else if (id == R.id.menu_item_area_city) {
                    mType = GUESS.AREA_CITY;
                    newGuessArea(Objects.requireNonNull(item.getTitle()).toString());
                } else if (id == R.id.menu_item_area_county) {
                    mType = GUESS.AREA_COUNTY;
                    newGuessArea(Objects.requireNonNull(item.getTitle()).toString());
                }
                return true;
            });
            popupMenu.show();
        });

        return selfView;
    }

    private String getCopyGuess() {
        StringBuilder sb = new StringBuilder();
        sb.append(mTextViewIPA.getText());
        sb.append("\n");
        String s = mTextView.getText().toString();
        String[] lines = s.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            sb.append(lines[i]);
            sb.append("\n");
        }
        s = sb.toString().trim();
        if (!s.contains("\n")) s = "";
        return s;
    }

    private void copyGuess() {
        String s = getCopyGuess();
        if (!TextUtils.isEmpty(s)) App.copyText(s);
    }

    private void hintHz(String input) {
        mTextViewIPA.setText("");
        if (TextUtils.isEmpty(mAnswer)) return;
        if (TextUtils.isEmpty(input)) {
            mScrollViewIPA.setVisibility(View.GONE);
            return;
        }
        mScrollViewIPA.setVisibility(View.VISIBLE);
        String label = DB.getLabelByLanguage(mAnswer);
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

    private String getDivision(Cursor cursor) {
        String division = "";
        if (mType == GUESS.DIVISION || mType == GUESS.SUBDIVISION) {
            division = cursor.getString(5);
            if (TextUtils.isEmpty(division)) return "";
            if (mType == GUESS.DIVISION) division = division.split(FS)[0];
        }
        return division;
    }

    private String getArea(Cursor cursor) {
        String area = cursor.getString(2);
        if (area.contentEquals("海外")) return "";
        if (mType == GUESS.AREA_CITY || mType == GUESS.AREA_COUNTY) {
            String city = cursor.getString(3).replace("/", "").strip();
            if (!TextUtils.isEmpty(city)) area += FS + city;
            List<String> municipalities = List.of("北京", "天津", "上海", "重慶");
            if (mType == GUESS.AREA_COUNTY || (TextUtils.isEmpty(city) && mType == GUESS.AREA_CITY && !municipalities.contains(area))) {
                String county = cursor.getString(4).replace("/", "").strip();
                if (!TextUtils.isEmpty(county)) area += FS + county;
            }
        }
        return area;
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
        String sql = String.format("select 語言,經緯度,省,市,縣,%s from info where 語言 MATCH '\"%s\"'", DB.FQ, lang);
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return;
        String point = cursor.getString(1);
        if (TextUtils.isEmpty(point)) {
            cursor.close();
            return;
        }
        GeoPoint location = GeoPoint.fromInvertedDoubleString(point, ',');
        double distance = location.distanceToAsDouble(mLocation) / 1000d;
        double angle = location.bearingTo(mLocation);
        int direction = (int)Math.round(((angle + 360) % 360) / 45) % 8;
        String directions = "⬆️↗️➡️↘️⬇️↙️⬅️↖️";
        String arrow = directions.substring(direction * 2, direction * 2 + 2);
        String hint;
        String mono = "";
        if (hintDirection) mono += arrow;
        if (hintDistance) mono += String.format(Locale.getDefault(), "%6.1f里 (%4.1f%%)", distance * 2, 100 - distance / 52d);
        mono = mono.replace(" ", "&nbsp;");
        hint = String.format("<font face=\"monospace\">%s</font> 不是%s", mono, lang);
        String division = getDivision(cursor);
        if (!TextUtils.isEmpty(division)) hint += String.format("(%s)", division);
        String area = getArea(cursor);
        if (!TextUtils.isEmpty(area)) hint += String.format("(%s)", area);
        append(hint);
        if (mType == GUESS.DISTANCE) {
            if (mDiameter > 0 && distance * 2 < mDiameter) {
                hint = String.format(Locale.getDefault(), "恭喜你，過關了！<br>距<b>%s</b>已不足%s", mAnswer, formatDistanceMessage(mDiameter));
                append(hint);
                mAnswer = "";
                mLocation = null;
            }
        } else if ((mType == GUESS.DIVISION || mType == GUESS.SUBDIVISION)) {
            if (division.contentEquals(mDivision)) {
                hint = String.format(Locale.getDefault(), "恭喜你，過關了！<br>與<b>%s</b>同屬<b>%s</b>", mAnswer, mDivision);
                append(hint);
                mAnswer = "";
                mDivision = "";
            }
        } else if (mType == GUESS.AREA_PROVINCE || mType == GUESS.AREA_CITY || mType == GUESS.AREA_COUNTY) {
            if (area.contentEquals(mArea)) {
                hint = String.format(Locale.getDefault(), "恭喜你，過關了！<br>與<b>%s</b>同屬<b>%s</b>", mAnswer, mArea);
                append(hint);
                mAnswer = "";
                mArea = "";
            }
        }
    }
}
