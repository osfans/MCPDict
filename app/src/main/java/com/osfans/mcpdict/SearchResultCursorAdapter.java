package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import static com.osfans.mcpdict.MCPDatabase.COL_BH;
import static com.osfans.mcpdict.MCPDatabase.COL_BS;

public class SearchResultCursorAdapter extends CursorAdapter {

    private final Context context;
    private final int layout;
    private final LayoutInflater inflater;
    private final boolean showFavoriteButton;

    public SearchResultCursorAdapter(Context context, int layout, Cursor cursor, boolean showFavoriteButton) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
        this.context = context;
        this.layout = layout;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showFavoriteButton = showFavoriteButton;
    }

    private View.OnClickListener getListener(final int index) {
        return v -> {
            ((View)v.getParent().getParent().getParent()).setTag(R.id.tag_col, index);
            ActivityWithOptionsMenu activity = (ActivityWithOptionsMenu) context;
            activity.registerForContextMenu(v);
            activity.openContextMenu(v);
            activity.unregisterForContextMenu(v);
        };
    }

    private int getMeasuredWidth(TextView textView) {
        textView.measure(0, 0);
        return textView.getMeasuredWidth();
    }

    private int getMaxWidth(TextView textView) {
        textView.setText("中文");
        return getMeasuredWidth(textView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        final TextView textViewHZ = view.findViewById(R.id.text_hz);
        textViewHZ.setTag(MCPDatabase.COL_HZ);
        textViewHZ.setOnClickListener(getListener(MCPDatabase.COL_HZ));
        TextView textView = view.findViewById(R.id.text_bh);
        textView.setTag(COL_BH);
        textView = view.findViewById(R.id.text_bs);
        textView.setTag(COL_BS);
        TableLayout table = view.findViewById(R.id.text_readings);
        int width = 0;
        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.search_result_row, null);
            TextView textViewName = row.findViewById(R.id.text_name);
            String name = MCPDatabase.getName(i);
            if (width == 0) width = getMaxWidth(textViewName);
            textViewName.setText(name);
            int color = Color.parseColor(MCPDatabase.getColor(i));
            textViewName.setBackgroundTintList(ColorStateList.valueOf(color));
            textViewName.setTextColor(color);
            textViewName.setTextScaleX(width/(float)getMeasuredWidth(textViewName));
            final TextView textViewDetail = row.findViewById(R.id.text_detail);
            textViewDetail.setTag(i);
            row.setTag("row" + i);
            row.setOnClickListener(getListener((Integer)textViewDetail.getTag()));
            table.addView(row);
        }
        table.setColumnShrinkable(1, true);
        return view;
    }

    private boolean isColumnVisible(String languages, int i) {
        if (TextUtils.isEmpty(languages) || i < MCPDatabase.COL_FIRST_READING) return true;
        String col = MCPDatabase.getColumnName(i);
        return languages.matches("(.*,)?"+col+"(,.*)?");
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        String hz, string;
        TextView textView;
        int mask = 0;
        Orthography.setToneStyle(getStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(getStyle(R.string.pref_key_tone_value_display));
        int showLanguages = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_show_languages), 0);
        String languages = context.getResources().getStringArray(R.array.pref_values_show_languages)[showLanguages];

        for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
            string = cursor.getString(i);
            boolean visible = string != null && isColumnVisible(languages, i);
            if (i >= MCPDatabase.COL_FIRST_READING) {
                View row = view.findViewWithTag("row" + i);
                row.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (!visible) continue;
            mask |= 1 << i;
            textView = view.findViewWithTag(i);
            textView.setTag(R.id.tag_raw, getRawText(string));
            CharSequence cs;
            switch (MCPDatabase.getColumnName(i)) {
                case MCPDatabase.SEARCH_AS_HZ:
                    cs = string;
                    String str = cursor.getString(COL_BH);
                    TextView tv = view.findViewWithTag(COL_BH);
                    tv.setText(context.getResources().getString(R.string.total_strokes_format, str));
                    str = cursor.getString(COL_BS);
                    tv = view.findViewWithTag(COL_BS);
                    String bs = str.substring(0, 1);
                    String bh = str.substring(1).replace('f', '-');
                    str = context.getResources().getString(R.string.radical_count_format, bs, bh);
                    tv.setText(str);
                    break;
                case MCPDatabase.SEARCH_AS_BA:
                    cs = baDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_MC:
                    cs = getRichText(middleChineseDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_PU:
                    cs = getRichText(mandarinDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_CT:
                    cs = cantoneseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_MN:
                    cs = getRichText(minnanDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_KR:
                    cs = koreanDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_VN:
                    cs = vietnameseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_JP_GO:
                case MCPDatabase.SEARCH_AS_JP_KAN:
                case MCPDatabase.SEARCH_AS_JP_TOU:
                case MCPDatabase.SEARCH_AS_JP_KWAN:
                case MCPDatabase.SEARCH_AS_JP_OTHER:
                    cs = getRichText(japaneseDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_YT:
                    cs = getRichText(tone4Displayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_IC:
                case MCPDatabase.SEARCH_AS_ZY:
                    cs = getRichText(tone5Displayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_SX:
                    cs = getRichText(tone6Displayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_RA:
                    cs = getRichText(tone8Displayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_TD:
                    cs = getRichText(toneTdDisplayer.display(string));
                    break;
                default:
                    cs = getRichText(tone7Displayer.display(string));
                    break;
            }
            textView.setText(cs);
        }

        // HZ
        hz = cursor.getString(MCPDatabase.COL_HZ);
        textView = view.findViewById(R.id.text_unicode);
        textView.setText(Orthography.HZ.toUnicode(hz));

        // Variants
        string = cursor.getString(cursor.getColumnIndex("variants"));
        textView = view.findViewById(R.id.text_variants);
        if (!TextUtils.isEmpty(string)) {
            textView.setText(String.format("(%s)", string));
        } else {
            textView.setText("");
        }

         // "Favorite" button
        boolean favorite = cursor.getInt(cursor.getColumnIndex("is_favorite")) == 1;
        Button button = view.findViewById(R.id.button_favorite);
        button.setOnClickListener(v -> {
            Boolean is_favorite = (Boolean) view.getTag(R.id.tag_favorite);
            if (is_favorite) {
                FavoriteDialogs.view(hz, view);
            } else {
                FavoriteDialogs.add(hz);
            }
        });
        if (showFavoriteButton) {
            button.setBackgroundResource(favorite ? R.drawable.ic_star_yellow : R.drawable.ic_star_white);
        }
        else {
            button.setVisibility(View.GONE);
        }
        view.setTag(R.id.tag_favorite, favorite);

        // Favorite comment
        string = cursor.getString(cursor.getColumnIndex("comment"));
        textView = view.findViewById(R.id.text_comment);
        textView.setText(string);

        // Set the view's mask to indicate which readings exist
        view.setTag(R.id.tag_mask, mask);
    }

    private String getHexColor() {
        int color = ContextCompat.getColor(context, R.color.dim);
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private CharSequence getRichText(String richTextString) {
        String s = richTextString
                .replace("\n", "<br/>")
                .replaceAll("~~(.+?)~~", "<s>$1</s>")
                .replaceAll("`(.+?)`", "<small><small>$1</small></small>")
                .replaceAll("___(.+?)___", "<sup>$1</sup>")
                .replaceAll("__(.+?)__", "<sub>$1</sub>")
                .replaceAll("_(.+?)_", "<u>$1</u>")
                .replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<small>$1</small>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<big>$1</big>")
                .replaceAll("\\*(.+?)\\*", "<b>$1</b>")
                .replaceAll("\\|(.+?)\\|", String.format("<span style='color: %s;'>$1</span>", getHexColor()));
        return HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private String getRawText(String s) {
        return s.replaceAll("[~_|*\\[\\]]", "").replaceAll("`.+?`", "");
    }

    private final Displayer middleChineseDisplayer = new Displayer() {
        public String lineBreak(String s) {return s.replace(",", "\n");}
        public String displayOne(String s) {return Orthography.MiddleChinese.display(s, getStyle(R.string.pref_key_mc_display)) + middleChineseDetailDisplayer.display(s);}
    };

    private final Displayer middleChineseDetailDisplayer = new Displayer() {
        public String lineBreak(String s) {return s.replace(",", "\n");}
        public String displayOne(String s) {return "(" + Orthography.MiddleChinese.detail(s) + ")";}
        public String display(String s) {return " " + super.display(s);}
    };

    private int getStyle(int id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        int i;
        try {
            i = sp.getInt(r.getString(id), 0);
        } catch (Exception e) {
            e.printStackTrace();
            i = Integer.parseInt(Objects.requireNonNull(sp.getString(r.getString(id), "0")));
        }
        return i;
    }

    private final Displayer mandarinDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Mandarin.display(s, getStyle(R.string.pref_key_mandarin_display));
        }
    };

    private final Displayer cantoneseDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Cantonese.display(s, getStyle(R.string.pref_key_cantonese_romanization));
        }
    };

    private final Displayer minnanDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Minnan.display(s, getStyle(R.string.pref_key_minnan_display));
        }
    };

    private final Displayer baDisplayer = new Displayer() {
        public String displayOne(String s) {
            return s;
        }
    };

    private final Displayer tone4Displayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.display4(s);
        }
    };

    private final Displayer tone5Displayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.display5(s);
        }
    };

    private final Displayer tone6Displayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.display6(s);
        }
    };

    private final Displayer tone7Displayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.display7(s);
        }
    };

    private final Displayer tone8Displayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.display(s);
        }
    };

    private final Displayer toneTdDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tone8.displayTD(s);
        }
    };

    private final Displayer koreanDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Korean.display(s, getStyle(R.string.pref_key_korean_display));
        }
    };

    private final Displayer vietnameseDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Vietnamese.display(s, getStyle(R.string.pref_key_vietnamese_tone_position));
        }
    };

    private final Displayer japaneseDisplayer = new Displayer() {
        public String lineBreak(String s) {
            if (s.charAt(0) == '[') {
                s = '[' + s.substring(1).replace("[", "\n[");
            }
            return s;
        }

        public String displayOne(String s) {
            return Orthography.Japanese.display(s, getStyle(R.string.pref_key_japanese_display));
        }
    };
}
