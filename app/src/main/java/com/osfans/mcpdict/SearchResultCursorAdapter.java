package com.osfans.mcpdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            String url = getURL();
            int color;
            if (url.startsWith("http://yedict.com")) {
                color = ContextCompat.getColor(context, R.color.hz);
                ds.setColor(color);
            }
            else if (url.startsWith("https://www.unicode.org")) {
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

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, parent, false);
    }

    private String getLink(int i, String hanzi) {
        String link = MCPDatabase.getDictLink(i);
        if (link != null) {
            String utf8 = null;
            String big5 = null;
            int unicode = hanzi.codePointAt(0);
            String hex = Orthography.Hanzi.getHex(unicode);
            try {
                utf8 = URLEncoder.encode(hanzi, "utf-8");
            } catch (UnsupportedEncodingException ignored) {
            }
            try {
                big5 = URLEncoder.encode(hanzi, "big5");
            } catch (UnsupportedEncodingException ignored) {
            }
            if (Objects.requireNonNull(big5).equals("%3F")) big5 = null;    // Unsupported character
            link = String.format(link, utf8, hex, big5);
        }
        return link;
    }
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        final int unicode;
        String hz, string;
        StringBuilder sb = new StringBuilder();
        TextView textView = view.findViewById(R.id.text_hz);
        String[] readings = new String[MCPDatabase.getColumnCount()];
        int tag = 0b11;

        // HZ
        hz = cursor.getString(MCPDatabase.COL_HZ);
        unicode = hz.codePointAt(0);
        sb.append(String.format("<span style='color:%s;'><big><a href='%s'>%s</a></big></span>",
                MCPDatabase.getColor(MCPDatabase.COL_HZ),
                getLink(MCPDatabase.COL_HZ, hz),
                hz));
        readings[MCPDatabase.COL_HZ] = hz;

        // Variants
        string = cursor.getString(cursor.getColumnIndex("variants"));
        if (string != null) {
            for (String s : string.split(" ")) {
                sb.append(String.format("<span style='color: %s;'><small>(%s)</small></span>",
                        MCPDatabase.getColor(MCPDatabase.COL_UNICODE),
                        Orthography.Hanzi.toString(s)));
            }
        }

        // Unicode
        string = String.format("U+%04X", unicode);
        sb.append(String.format("&nbsp;<span style='color:%s'><small><a href='%s'>%s</a></small></span>&nbsp;",
                MCPDatabase.getColor(MCPDatabase.COL_UNICODE),
                getLink(MCPDatabase.COL_UNICODE, hz),
                string));
        readings[MCPDatabase.COL_UNICODE] = string;
        sb.append("<br/>");

        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            if ((string = cursor.getString(i)) == null) continue;
            String name = MCPDatabase.getName(i);
            switch (MCPDatabase.getColumnName(i)) {
                case MCPDatabase.SEARCH_AS_MC:
                    string = middleChineseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_PU:
                    string = mandarinDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_CT:
                    string = cantoneseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_KR:
                    string = koreanDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_VN:
                    string = vietnameseDisplayer.display(string);
                    break;
                case MCPDatabase.SEARCH_AS_JP_GO:
                case MCPDatabase.SEARCH_AS_JP_KAN:
                case MCPDatabase.SEARCH_AS_JP_TOU:
                case MCPDatabase.SEARCH_AS_JP_KWAN:
                case MCPDatabase.SEARCH_AS_JP_OTHER:
                    string = getRichText(japaneseDisplayer.display(string));
                    break;
                default:
                    string = getRichText(tone8Displayer.display(string));
                    break;
            }
            sb.append(String.format("<small><span style='color: %s;'>〔%s〕</span></small>", MCPDatabase.getColor(i), name));
            String link = getLink(i, hz);
            if (link != null) {
                sb.append(String.format("<a href='%s'>%s  </a>&nbsp;", link, string));
            } else {
                sb.append(String.format("%s", string));
            }
            sb.append("<br/>");
            tag |= 1 << i;
            readings[i] = Html.fromHtml(string).toString();
        }

        textView.setText(Html.fromHtml(sb.toString()));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(textView);

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
        view.setTag(R.id.tag_readings, readings);
    }

    private String getRichText(String richTextString) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;

        for (int i = 0; i < richTextString.length(); i++) {
            char c = richTextString.charAt(i);
            switch (c) {
                case '*':
                    sb.append(start ? "<b>" : "</b>");
                    start = !start;
                    break;
                case '|':
                    sb.append(start ? "<span style='color: #808080'>" : "</span>");
                    start = !start;
                    break;
                default : sb.append(c); break;
            }
        }
        return sb.toString();
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
