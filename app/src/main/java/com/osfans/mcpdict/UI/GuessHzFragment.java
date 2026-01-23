package com.osfans.mcpdict.UI;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;

import com.osfans.mcpdict.Adapter.LanguageAdapter;
import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;

import java.util.Locale;

public class GuessHzFragment extends Fragment implements RefreshableFragment {
    private View selfView;
    private TextView mTextView, mTextInput;
    private ScrollView mScrollView;
    private AutoCompleteTextView mAcSearchLang;

    private String mAnswer = "", mLast = "";
    private Spinner mSpinnerHint;
    private final String strokes = "横竖撇捺折";
    private final String ids = "⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽㇯";
    
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

    private void check(String input) {
        if (TextUtils.isEmpty(mAnswer)) return;
        if (TextUtils.isEmpty(input)) return;
        if (input.contentEquals(mLast)) return;
        if (!HanZi.isHz(input)) return;
        if (input.contentEquals(mAnswer)) {
            append("答對了！恭喜！");
            mAnswer = "";
            mLast = "";
        } else {
            mLast = input;
            append(String.format("這個字不是<b>%s</b>", input));
        }
    }

    private void newGuess(int level) {
        int left = 2;
        int right = 6000;
        if (level > 0) {
            left = (level - 1) * 2000;
            right = level * 2000;
        }
        if (left < 1) left = 1;
        String sql = String.format(Locale.getDefault(), "select 漢字 from mcpdict where ROWID > %d AND ROWID <= %d ORDER by random() limit 1", left, right);
        mSpinnerHint.setSelection(0);
        mAnswer = DB.getResult(sql);

        sql = "select 語言,讀音 from langs where 字組 match '%s' order by random() limit 1";
        sql = String.format(sql, mAnswer);
        Cursor cursor = DB.getCursor(sql);
        if (cursor == null) return;
        String label = cursor.getString(0);
        String ipa = DisplayHelper.formatIPA(label, cursor.getString(1)).toString();
        cursor.close();
        String lang = DB.getLanguageByLabel(label);
        mTextView.setText("");
        String hint = String.format("請猜一個在<b>%s</b>中可以讀<b>%s</b>的字", lang, ipa);
        append(hint);
        hintLang();
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
        selfView = inflater.inflate(R.layout.fragment_guess_hz, container, false);

        mTextInput = selfView.findViewById(R.id.editTextInput);
        FontUtil.setTypeface(mTextInput);
        mTextInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                String last = HanZi.lastHz(s.toString());
                if (s.length() > last.length()) {
                    mTextInput.removeTextChangedListener(this);
                    s.delete(0, s.length() - last.length());
                    mTextInput.addTextChangedListener(this);
                }
                check(last);
            }
        });
        mTextView = selfView.findViewById(R.id.textView);
        FontUtil.setTypeface(mTextView);
        mScrollView = selfView.findViewById(R.id.scrollView);
        mSpinnerHint = selfView.findViewById(R.id.spinner_hints);
        mAcSearchLang = selfView.findViewById(R.id.text_search_lang);
        mAcSearchLang.setAdapter(new LanguageAdapter(requireContext()));
        mAcSearchLang.setOnFocusChangeListener((v, b) -> {
            if (b) ((AutoCompleteTextView)v).showDropDown();
        });
        mAcSearchLang.setOnItemClickListener((adapterView, view, i, l) -> hintLang());
        selfView.findViewById(R.id.button_lang_clear).setOnClickListener(v -> {
            mAcSearchLang.setText("");
            mAcSearchLang.requestFocus();
        });
        Button buttonNew = selfView.findViewById(R.id.buttonNew);
        buttonNew.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.guess_hz, menu);
            MenuCompat.setGroupDividerEnabled(menu, true);
            MenuItem menuItem = menu.findItem(R.id.menu_item_answer);
            menuItem.setEnabled(!TextUtils.isEmpty(mAnswer));
            menuItem = menu.findItem(R.id.menu_item_guess_copy);
            menuItem.setEnabled(!TextUtils.isEmpty(getCopyGuess()));
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_item_answer) {
                    if (!TextUtils.isEmpty(mAnswer)) {
                        append(String.format("這個字是<b>%s</b>", mAnswer));
                        mAnswer = "";
                        mLast = "";
                    }
                } else if (id == R.id.menu_item_guess_copy) {
                    copyGuess();
                } else if (id == R.id.menu_item_random) {
                    newGuess(0);
                } else if (id == R.id.menu_item_easy) {
                    newGuess(1);
                } else if (id == R.id.menu_item_normal) {
                    newGuess(2);
                } else if (id == R.id.menu_item_hard) {
                    newGuess(3);
                }
                return true;
            });
            popupMenu.show();
        });

        mSpinnerHint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (TextUtils.isEmpty(mAnswer)) return;
                if (position == 0) return;
                String hint = "";
                if (position == 1) {
                    String sql = String.format("select 總筆畫數 from mcpdict where 漢字 match '%s'", mAnswer);
                    hint = DB.getResult(sql);
                    if (TextUtils.isEmpty(hint)) {
                        hint = "不知道這個字有幾畫";
                    } else {
                        hint = String.format("這個字共<b>%s</b>畫", hint.replace(" ", "或"));
                    }
                } else if (position == 2 || position == 3) {
                    String sql = String.format("select 五筆畫 from mcpdict where 漢字 match '%s'", mAnswer);
                    hint = DB.getResult(sql);
                    if (TextUtils.isEmpty(hint)) {
                        hint = "不知道這個字的筆順";
                    } else if (position == 2) {
                        hint = String.format("這個字的首筆是%s中的<b>%s</b>", strokes, strokes.charAt(Integer.parseInt(hint.substring(0, 1)) - 1));
                    } else {
                        hint = String.format("這個字的末筆是%s中的<b>%s</b>", strokes, strokes.charAt(Integer.parseInt(hint.substring(hint.length() - 1)) - 1));
                    }
                } else if (position == 4) {
                    String sql = String.format("select 部首餘筆 from mcpdict where 漢字 match '%s'", mAnswer);
                    hint = DB.getResult(sql);
                    if (TextUtils.isEmpty(hint)) {
                        hint = "不知道這個字的部首";
                    } else if (hint.substring(1).equals("0")) {
                        hint = "這個字就是部首";
                    } else {
                        hint = String.format("這個字的部首是<b>%s</b>", hint.charAt(0));
                    }
                } else if (position == 5) {
                    String sql = String.format("select 字形描述 from mcpdict where 漢字 match '%s'", mAnswer);
                    hint = DB.getResult(sql);
                    if (TextUtils.isEmpty(hint)) {
                        hint = "不知道這個字的結構";
                    } else {
                        String start = hint.charAt(0) + "";
                        if (ids.contains(start))
                            hint = String.format("這個字是%s結構", hint.charAt(0));
                        else
                            hint = "這個字是獨體字";
                    }
                }
                append(hint);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return selfView;
    }

    private String getCopyGuess() {
        String s = mTextView.getText().toString();
        if (s.contentEquals(Pref.getString(R.string.guess_hz_instructions))) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = s.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            sb.append(lines[i]);
            sb.append("\n");
        }
        s = sb.toString().trim();
        return s;
    }

    private void copyGuess() {
        String s = getCopyGuess();
        if (!TextUtils.isEmpty(s)) App.copyText(s);
    }

    private void hintLang() {
        String lang = mAcSearchLang.getText().toString();
        if (TextUtils.isEmpty(lang)) return;
        String label = DB.getLabelByLanguage(lang);
        if (TextUtils.isEmpty(label)) return;
        if (TextUtils.isEmpty(mAnswer)) return;
        String sql = "select 讀音 from langs where 語言 match '%s' and 字組 match '%s'";
        sql = String.format(sql, label, mAnswer);
        String result = DB.getResult(sql);
        if (TextUtils.isEmpty(result)) {
            append(String.format("不知道這個字的<b>%s</b>讀音", lang));
        } else {
            String ipa = DisplayHelper.formatIPA(label, result).toString();
            append(String.format("這個字的%s讀音是<b>%s</b>", lang, ipa));
        }
    }
}
