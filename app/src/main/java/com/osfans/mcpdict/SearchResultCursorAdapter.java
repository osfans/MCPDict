package com.osfans.mcpdict;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.CursorAdapter;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;

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

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        final int unicode;
        String string;
        StringBuilder sb;
        TextView textView;
        int tag = 0;

        TextView[] textViewNames = {
                null,
                null,
                view.findViewById(R.id.name_mc),
                view.findViewById(R.id.name_c3),
                view.findViewById(R.id.name_c4),
                view.findViewById(R.id.name_c5),
                view.findViewById(R.id.name_c6),
                view.findViewById(R.id.name_c7),
                view.findViewById(R.id.name_c8),
                view.findViewById(R.id.name_c9),
                view.findViewById(R.id.name_c10),
                view.findViewById(R.id.name_c11),
                view.findViewById(R.id.name_c12),
                view.findViewById(R.id.name_c13),
                view.findViewById(R.id.name_c14),
                view.findViewById(R.id.name_c15),
                view.findViewById(R.id.name_c16),
                view.findViewById(R.id.name_c17),
                view.findViewById(R.id.name_c18),
                view.findViewById(R.id.name_c19),
                view.findViewById(R.id.name_c20),
        };

        TextView[] textViewDetails = {
                view.findViewById(R.id.text_hz),
                view.findViewById(R.id.text_unicode),
                view.findViewById(R.id.text_mc),
                view.findViewById(R.id.text_c3),
                view.findViewById(R.id.text_c4),
                view.findViewById(R.id.text_c5),
                view.findViewById(R.id.text_c6),
                view.findViewById(R.id.text_c7),
                view.findViewById(R.id.text_c8),
                view.findViewById(R.id.text_c9),
                view.findViewById(R.id.text_c10),
                view.findViewById(R.id.text_c11),
                view.findViewById(R.id.text_c12),
                view.findViewById(R.id.text_c13),
                view.findViewById(R.id.text_c14),
                view.findViewById(R.id.text_c15),
                view.findViewById(R.id.text_c16),
                view.findViewById(R.id.text_c17),
                view.findViewById(R.id.text_c18),
                view.findViewById(R.id.text_c19),
                view.findViewById(R.id.text_c20)
        };

        for (int i = 0; i < textViewNames.length ; i++) {
            if (i > MCPDatabase.COL_LAST_READING) {
                textViewNames[i].setVisibility(View.GONE);
                textViewDetails[i].setVisibility(View.GONE);
                continue;
            }
            string = cursor.getString(i);
            textView = textViewDetails[i];
            switch (MCPDatabase.getColumnName(i)) {
                case MCPDatabase.SEARCH_AS_UNICODE:
                    textView.setText("U+" + string);
                    break;
                case MCPDatabase.SEARCH_AS_MC:
                    textView.setText(middleChineseDisplayer.display(string));
                    textView = view.findViewById(R.id.text_mc_detail);
                    if (string != null) {
                        textView.setText(middleChineseDetailDisplayer.display(string));
                    }
                    else {
                        textView.setText("");
                    }
                    break;
                case MCPDatabase.SEARCH_AS_PU:
                    textView.setText(mandarinDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_CT:
                    textView.setText(cantoneseDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_KR:
                    textView.setText(koreanDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_VN:
                    textView.setText(vietnameseDisplayer.display(string));
                    break;
                case MCPDatabase.SEARCH_AS_JP_GO:
                case MCPDatabase.SEARCH_AS_JP_KAN:
                case MCPDatabase.SEARCH_AS_JP_TOU:
                case MCPDatabase.SEARCH_AS_JP_KWAN:
                case MCPDatabase.SEARCH_AS_JP_OTHER:
                    setRichText(textView, japaneseDisplayer.display(string));
                    break;
                default:
                    setRichText(textView, tone8Displayer.display(string));
                    break;
            }
            if (i >= MCPDatabase.COL_FIRST_READING) {
                textView = textViewNames[i];
                String name = MCPDatabase.getName(i);
                textView.setText(name);
                int color = MCPDatabase.getColor(i);
                textView.setTextColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textView.setBackgroundTintList(ColorStateList.valueOf(color));
                }
                if (name.length() == 2) textView.setScaleX(0.6f);
                if (name.length() == 3) textView.setScaleX(0.5f);
            }
            if (i >= MCPDatabase.COL_JP_ANY && string == null) {
                textViewNames[i].setVisibility(View.GONE);
                textViewDetails[i].setVisibility(View.GONE);
                continue;
            }
            if (string != null) tag |= 1 << i;
        }

        // Unicode
        string = cursor.getString(MCPDatabase.COL_UNICODE);
        unicode = Integer.parseInt(string, 16);

        // Variants
        string = cursor.getString(cursor.getColumnIndex("variants"));
        textView = view.findViewById(R.id.text_variants);
        if (string == null) {
            textView.setVisibility(View.GONE);
        }
        else {
            sb = new StringBuilder();
            for (String s : string.split(" ")) {
                sb.append(Orthography.Hanzi.toString(s));
            }
            textView.setText("(" + sb.toString() + ")");
            textView.setVisibility(View.VISIBLE);
        }

         // "Favorite" button
        boolean favorite = cursor.getInt(cursor.getColumnIndex("is_favorite")) == 1;
        Button button = view.findViewById(R.id.button_favorite);
        button.setOnClickListener(v -> {
            Boolean is_favorite = (Boolean) view.getTag(R.id.tag_favorite);
            if (is_favorite) {
                FavoriteDialogs.view(unicode, view);
            } else {
                FavoriteDialogs.add(unicode);
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

        // Set the view's tag to indicate which readings exist
        view.setTag(tag);
    }

    public void setRichText(TextView view, String richTextString) {
        StringBuilder sb = new StringBuilder();
        List<Integer> bolds = new ArrayList<>();
        List<Integer> dims = new ArrayList<>();

        for (int i = 0; i < richTextString.length(); i++) {
            char c = richTextString.charAt(i);
            switch (c) {
                case '*': bolds.add(sb.length()); break;
                case '|': dims.add(sb.length()); break;
                default : sb.append(c); break;
            }
        }

        view.setText(sb.toString(), BufferType.SPANNABLE);
        Spannable spannable = (Spannable) view.getText();
        for (int i = 1; i < bolds.size(); i += 2) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), bolds.get(i-1), bolds.get(i), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        for (int i = 1; i < dims.size(); i += 2) {
            spannable.setSpan(new ForegroundColorSpan(0xFF808080), dims.get(i-1), dims.get(i), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
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
        public String displayOne(String s) {return Orthography.MiddleChinese.display(s);}
    };

    private final Displayer middleChineseDetailDisplayer = new Displayer() {
        public String lineBreak(String s) {return s.replace(",", "\n");}
        public String displayOne(String s) {return "(" + Orthography.MiddleChinese.detail(s) + ")";}
        public String display(String s) {return " " + super.display(s);}
    };

    private final Displayer mandarinDisplayer = new Displayer() {
        public String displayOne(String s) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Resources r = context.getResources();
            int style = sp.getInt(r.getString(R.string.pref_key_mandarin_display), 0);
            return Orthography.Mandarin.display(s, style);
        }
    };

    private final Displayer cantoneseDisplayer = new Displayer() {
        public String displayOne(String s) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Resources r = context.getResources();
            int system = sp.getInt(r.getString(R.string.pref_key_cantonese_romanization), 0);
            return Orthography.Cantonese.display(s, system);
        }
    };

    private final Displayer tone8Displayer = new Displayer() {
        public String displayOne(String s) {
            return s;
        }
    };

    private final Displayer koreanDisplayer = new Displayer() {
        public String displayOne(String s) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Resources r = context.getResources();
            int style = sp.getInt(r.getString(R.string.pref_key_korean_display), 0);
            return Orthography.Korean.display(s, style);
        }
    };

    private final Displayer vietnameseDisplayer = new Displayer() {
        public String displayOne(String s) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Resources r = context.getResources();
            int style = sp.getInt(r.getString(R.string.pref_key_vietnamese_tone_position), 0);
            return Orthography.Vietnamese.display(s, style);
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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Resources r = context.getResources();
            int style = sp.getInt(r.getString(R.string.pref_key_japanese_display), 0);
            return Orthography.Japanese.display(s, style);
        }
    };
}
