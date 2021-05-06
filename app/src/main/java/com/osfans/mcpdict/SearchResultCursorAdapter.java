package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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

import java.util.Objects;

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

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        final TextView textViewHZ = view.findViewById(R.id.text_hz);
        textViewHZ.setTag(MCPDatabase.COL_HZ);
        textViewHZ.setOnClickListener(getListener(MCPDatabase.COL_HZ));
        TextView textView = view.findViewById(R.id.text_bh);
        textView.setTag(MCPDatabase.COL_BH);
        textView = view.findViewById(R.id.text_bs);
        textView.setTag(MCPDatabase.COL_BS);
        TableLayout table = view.findViewById(R.id.text_readings);
        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.search_result_row, null);
            TextView textViewName = row.findViewById(R.id.text_name);
            String name = MCPDatabase.getName(i);
            textViewName.setText(name);
            int color = Color.parseColor(MCPDatabase.getColor(i));
            textViewName.setBackgroundTintList(ColorStateList.valueOf(color));
            textViewName.setTextColor(color);
            final TextView textViewDetail = row.findViewById(R.id.text_detail);
            textViewDetail.setTag(i);
            row.setTag("row" + i);
            row.setOnClickListener(getListener((Integer)textViewDetail.getTag()));
            table.addView(row);
        }
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        String hz, string;
        TextView textView;
        int mask = 0;

        for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
            string = cursor.getString(i);
            boolean visible = string != null;
            if (i >= MCPDatabase.COL_FIRST_READING) {
                View row = view.findViewWithTag("row" + i);
                row.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (!visible) continue;
            mask |= 1 << i;
            textView = view.findViewWithTag(i);
            if (MCPDatabase.isDisplayOnly(i)) {
                textView.setTag(R.id.tag_raw, getRawText(string));
            }
            CharSequence cs;
            switch (MCPDatabase.getColumnName(i)) {
                case MCPDatabase.SEARCH_AS_HZ:
                    cs = string;
                    break;
                case MCPDatabase.SEARCH_AS_BH:
                    cs = context.getResources().getString(R.string.total_strokes_format, string);
                    break;
                case MCPDatabase.SEARCH_AS_BS:
                    String bh = string.substring(1).replace('f', '-');
                    cs = context.getResources().getString(R.string.radical_count_format, string.substring(0, 1), bh);
                    break;
                case MCPDatabase.SEARCH_AS_BA:
                    cs = tone8Displayer.display(string);
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
                default:
                    cs = getRichText(tone8Displayer.display(string));
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

    private abstract static class Displayer {
        protected static final String NULL_STRING = "-";

        public String display(String s) {
            if (s == null) return NULL_STRING;
            s = lineBreak(s);
            // Find all regions of [a-z0-9]+ in s, and apply displayer to each of them
            StringBuilder sb = new StringBuilder();
            int L = s.length(), p = 0;
            while (p < L) {
                int q = p;
                while (q < L && Character.isLetterOrDigit(s.charAt(q))) q++;
                if (q > p) {
                    String t1 = s.substring(p, q);
                    String t2 = displayOne(t1);
                    sb.append(t2 == null ? t1 : t2);
                    p = q;
                }
                while (p < L && !Character.isLetterOrDigit(s.charAt(p))) p++;
                sb.append(s.substring(q, p));
            }
            // Add spaces as hints for line wrapping
            s = sb.toString().replace(",", ", ")
                             .replace("(", " (")
                             .replace("]", "] ")
                             .replaceAll(" +", " ")
                             .replace(" ,", ",")
                             .trim();
            return s;
        }

        public String lineBreak(String s) {return s;}
        public abstract String displayOne(String s);
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

    private final Displayer tone8Displayer = new Displayer() {
        public String displayOne(String s) {
            return s;
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
