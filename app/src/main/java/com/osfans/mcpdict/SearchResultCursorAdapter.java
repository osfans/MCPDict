package com.osfans.mcpdict;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import static com.osfans.mcpdict.SearchResultFragment.KEY_COL;

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

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            String url = getURL();
            int color;
            if (url.startsWith("https://www.unicode.org")) {
                color = Color.parseColor(MCPDatabase.getColor(MCPDatabase.COL_UNICODE));
                ds.setColor(color);
            }
            ds.setUnderlineText(false);
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private void setText(TextView textView, StringBuilder sb) {
        String string = sb.toString();
        textView.setText(Html.fromHtml(string));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(textView);
    }

    private View.OnClickListener getListener(final int index) {
        return v -> {
            ActivityWithOptionsMenu activity = (ActivityWithOptionsMenu) context;
            activity.setIntent(activity.getIntent().putExtra(KEY_COL, index));
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
        TextView textViewUnicode = view.findViewById(R.id.text_unicode);
        textViewUnicode.setTag(MCPDatabase.COL_UNICODE);
        textViewUnicode.setOnClickListener(getListener(MCPDatabase.COL_UNICODE));
        TableLayout table = view.findViewById(R.id.text_readings);
        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.search_result_row, null);
            TextView textViewName = row.findViewById(R.id.text_name);
            String name = String.format("〔%s〕", MCPDatabase.getName(i));
            textViewName.setText(name);
            textViewName.setTextColor(Color.parseColor(MCPDatabase.getColor(i)));
            textViewName.setTag("name" + i);
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
        final int unicode;
        String hz, string;
        TextView textView;
        int tag = 0;

        for (int i = MCPDatabase.COL_HZ; i <= MCPDatabase.COL_LAST_READING; i++) {
            string = cursor.getString(i);
            boolean visible = string != null;
            if (i >= MCPDatabase.COL_FIRST_READING) {
                View row = view.findViewWithTag("row" + i);
                row.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (!visible) continue;
            tag |= 1 << i;
            textView = view.findViewWithTag(i);
            CharSequence cs;
            switch (MCPDatabase.getColumnName(i)) {
                case MCPDatabase.SEARCH_AS_UNICODE:
                    cs = "U+" + string;
                    break;
                case MCPDatabase.SEARCH_AS_MC:
                    cs = middleChineseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_PU:
                    cs = mandarinDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_CT:
                    cs = cantoneseDisplayer.display(string);
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
        unicode = hz.codePointAt(0);

        // Variants
        StringBuilder sb = new StringBuilder();
        string = cursor.getString(cursor.getColumnIndex("variants"));
        if (string != null) {
            sb.append("(");
            for (String s : string.split(" ")) {
                sb.append(Orthography.Hanzi.toString(s));
            }
            sb.append(")");
        }
        textView = view.findViewById(R.id.text_variants);
        textView.setText(sb);

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

    private String getHexColor() {
        int color = ContextCompat.getColor(context, R.color.dim);
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private CharSequence getRichText(String richTextString) {
        String s = richTextString
                .replaceAll("~~(.+?)~~", "<s>$1</s>")
                .replaceAll("```(.+?)```", "<i>$1</i>")
                .replaceAll("`(.+?)`", "<tt>$1</tt>")
                .replaceAll("___(.+?)___", "<sup>$1</sup>")
                .replaceAll("__(.+?)__", "<sub>$1</sub>")
                .replaceAll("_(.+?)_", "<u>$1</u>")
                .replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<small>$1</small>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<big>$1</big>")
                .replaceAll("\\*(.+?)\\*", "<b>$1</b>")
                .replaceAll("\\|(.+?)\\|", String.format("<span style='color: %s;'>$1</span>", getHexColor()));
        return Html.fromHtml(s);
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
        public String displayOne(String s) {return Orthography.MiddleChinese.display(s) + middleChineseDetailDisplayer.display(s);}
    };

    private final Displayer middleChineseDetailDisplayer = new Displayer() {
        public String lineBreak(String s) {return s.replace(",", "\n");}
        public String displayOne(String s) {return "(" + Orthography.MiddleChinese.detail(s) + ")";}
        public String display(String s) {return " " + super.display(s);}
    };

    private int getStyle(int id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        return Integer.parseInt(sp.getString(r.getString(id), "0"));
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
