package com.osfans.mcpdict;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static com.osfans.mcpdict.MCPDatabase.COL_BH;
import static com.osfans.mcpdict.MCPDatabase.COL_BS;
import static com.osfans.mcpdict.MCPDatabase.COL_HD;
import static com.osfans.mcpdict.MCPDatabase.COL_HZ;
import static com.osfans.mcpdict.MCPDatabase.COL_KX;
import static com.osfans.mcpdict.MCPDatabase.COL_SW;
import static com.osfans.mcpdict.MCPDatabase.getColor;
import static com.osfans.mcpdict.MCPDatabase.getLabel;
import static com.osfans.mcpdict.MCPDatabase.getSubColor;

import org.osmdroid.util.GeoPoint;

public class SearchResultCursorAdapter extends CursorAdapter {

    private static WeakReference<Context> context;
    private final int layout;
    private final LayoutInflater inflater;
    private final boolean showFavoriteButton;
    private final Typeface mTypefaceHan;

    public SearchResultCursorAdapter(Context context, int layout, Cursor cursor, boolean showFavoriteButton) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
        SearchResultCursorAdapter.context = new WeakReference<>(context);
        this.layout = layout;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showFavoriteButton = showFavoriteButton;
        mTypefaceHan = ResourcesCompat.getFont(context, R.font.han);
    }

    private static Context getContext() {
        return context.get();
    }

    private View.OnClickListener getListener(final int index) {
        return v -> {
            ((View)v.getParent().getParent().getParent()).setTag(R.id.tag_col, index);
            ActivityWithOptionsMenu activity = (ActivityWithOptionsMenu) getContext();
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

    private void formatTextView(TextView tv, int i) {
        int color = MCPDatabase.getColor(i);
        int subColor = MCPDatabase.getSubColor(i);
        if (color == subColor) {
            tv.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] {subColor, color, color});
            gd.setCornerRadius(5f);
            tv.setBackground(gd);
        }
        tv.setAlpha(0.8f);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        final TextView textViewHZ = view.findViewById(R.id.text_hz);
        textViewHZ.setTag(COL_HZ);
        textViewHZ.setOnClickListener(getListener(COL_HZ));
        TextView textView = view.findViewById(R.id.text_unicode);
        int color = MCPDatabase.getColor(MCPDatabase.COL_LF);
        textView.setTextColor(color);
        textView = view.findViewById(R.id.text_sw);
        textView.setTag(COL_SW);
        textView.setText(getLabel(COL_SW));
        color = MCPDatabase.getColor(COL_SW);
        textView.setTextColor(color);
        textView = view.findViewById(R.id.text_kx);
        textView.setTag(COL_KX);
        textView.setText(getLabel(COL_KX));
        color = MCPDatabase.getColor(COL_KX);
        textView.setTextColor(color);
        textView = view.findViewById(R.id.text_hd);
        textView.setTag(COL_HD);
        textView.setText(getLabel(COL_HD));
        color = MCPDatabase.getColor(COL_HD);
        textView.setTextColor(color);
        TableLayout table = view.findViewById(R.id.text_readings);
        int width = 0;
        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.search_result_row, null);
            TextView textViewName = row.findViewById(R.id.text_name);
            String name = MCPDatabase.getLabel(i);
            if (width == 0) width = getMaxWidth(textViewName);
            formatTextView(textViewName, i);
            textViewName.setText(name);
            if (name.length() != 2) textViewName.setTextScaleX(width/(float)getMeasuredWidth(textViewName));
            final TextView textViewDetail = row.findViewById(R.id.text_detail);
            textViewDetail.setTag(i);
            row.setTag("row" + i);
            row.setOnClickListener(getListener((Integer)textViewDetail.getTag()));
            table.addView(row);
        }
        table.setColumnShrinkable(1, true);
        return view;
    }

    public static boolean isColumnVisible(String languages, Set<String> customs, int i) {
        if (i < MCPDatabase.COL_FIRST_READING) return true;
        if (languages.contentEquals("3")) { //縣級
            String name = MCPDatabase.getLabel(i);
            return MCPDatabase.isDialect(i) && name.length() <= 3;
        }
        String column = MCPDatabase.getColumnName(i);
        if (TextUtils.isEmpty(languages)) {
            if (customs == null || customs.size() == 0) return true;
            return customs.contains(column);
        }
        return column.matches(languages);
    }

    public static CharSequence formatIPA(int i, String string) {
        CharSequence cs;
        if (TextUtils.isEmpty(string)) return "";
        switch (MCPDatabase.getColumnName(i)) {
            case MCPDatabase.SEARCH_AS_SG:
                cs = getRichText(string);
                break;
            case MCPDatabase.SEARCH_AS_BA:
                cs = baDisplayer.display(string);
                break;
            case MCPDatabase.SEARCH_AS_MC:
                cs = getRichText(middleChineseDisplayer.display(string));
                break;
            case MCPDatabase.SEARCH_AS_CMN:
                cs = getRichText(mandarinDisplayer.display(string));
                break;
            case MCPDatabase.SEARCH_AS_GZ:
                cs = cantoneseDisplayer.display(string);
                break;
            case MCPDatabase.SEARCH_AS_NAN:
                cs = getRichText(nanDisplayer.display(string));
                break;
            case MCPDatabase.SEARCH_AS_KOR:
                cs = koreanDisplayer.display(string);
                break;
            case MCPDatabase.SEARCH_AS_VI:
                cs = vietnameseDisplayer.display(string);
                break;
            case MCPDatabase.SEARCH_AS_JA_GO:
            case MCPDatabase.SEARCH_AS_JA_KAN:
            case MCPDatabase.SEARCH_AS_JA_TOU:
            case MCPDatabase.SEARCH_AS_JA_KWAN:
            case MCPDatabase.SEARCH_AS_JA_OTHER:
                cs = getRichText(japaneseDisplayer.display(string));
                break;
            default:
                cs = getRichText(toneDisplayer.display(string, i));
                break;
        }
        return cs;
    }

    private CharSequence formatPassage(String hz, String js) {
        String[] fs = (js+"\n").split("\n", 2);
        String s = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br/>"));
        return getRichText(s);
    }

    private void hasGlyph(String s) {
        Paint paint = new Paint();
        Typeface typeface = Typeface.DEFAULT;//ResourcesCompat.getFont(getContext(), R.font.han);
        //paint.setTypeface(typeface);
        for(int i:s.codePoints().toArray()
             ) {
            String t = Orthography.HZ.toHz(i);
            //Log.e("kyle", t+"="+paint.hasGlyph(t));
        }
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        String hz, string;
        TextView textView;
        Set<Integer> cols = new HashSet<>();
        Orthography.setToneStyle(getStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(getStyle(R.string.pref_key_tone_value_display));
        String languages = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_show_language_names), "");
        Set<String> customs = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(context.getString(R.string.pref_key_custom_languages), null);

        for (int i = MCPDatabase.COL_FIRST_READING; i <= MCPDatabase.COL_LAST_READING; i++) {
            string = cursor.getString(i);
            boolean visible = string != null && isColumnVisible(languages, customs, i);
            View row = view.findViewWithTag("row" + i);
            row.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (!visible) continue;
            cols.add(i);
            textView = view.findViewWithTag(i);
            textView.setTag(R.id.tag_raw, getRawText(string));
            CharSequence cs = formatIPA(i, string);
            //hasGlyph(string);
            textView.setText(cs);
        }

        // HZ
        hz = cursor.getString(COL_HZ);
        textView = view.findViewWithTag(COL_HZ);
        textView.setText(hz);
        cols.add(COL_HZ);
        textView = view.findViewById(R.id.text_unicode);
        String unicode = Orthography.HZ.toUnicode(hz);
        textView.setText(unicode);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<p><big><big><big>%s</big></big></big></p><p>【統一碼】%s %s</p>", hz, unicode, Orthography.HZ.getUnicodeExt(hz)));
        for (int i = MCPDatabase.COL_LF; i < MCPDatabase.COL_VA; i++) {
            if (i == COL_SW) i = COL_BH;
            String str = cursor.getString(i);
            if (i == COL_BS) str = str.replace("f", "-");
            if (TextUtils.isEmpty(str)) continue;
            str = str.toUpperCase();
            sb.append(String.format("<p>【%s】%s</p>", MCPDatabase.getFullName(i), str));
        }
        String info = sb.toString().replace(",", ", ");
        textView.setOnClickListener(view1 -> {
            TextView showText = new TextView(getContext());
            showText.setPadding(24, 24, 24, 24);
            showText.setTextIsSelectable(true);
            showText.setText(HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_COMPACT));
            new AlertDialog.Builder(getContext())
                    .setView(showText)
                    .show();
        });
        TextView tv = view.findViewWithTag(COL_SW);
        String sw =  cursor.getString(COL_SW);
        if (!TextUtils.isEmpty(sw)) {
            tv.setOnClickListener(view1 -> {
                TextView showText = new TextView(getContext());
                showText.setPadding(24, 24, 24, 24);
                showText.setTextIsSelectable(true);
                showText.setMovementMethod(LinkMovementMethod.getInstance());
                showText.setText(formatPassage(hz, sw));
                new AlertDialog.Builder(getContext())
                        .setView(showText)
                        .show();
            });
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
        tv = view.findViewWithTag(COL_KX);
        String kx =  cursor.getString(COL_KX);
        if (!TextUtils.isEmpty(kx)) {
            tv.setOnClickListener(view1 -> {
                TextView showText = new TextView(getContext());
                showText.setPadding(24, 24, 24, 24);
                showText.setTextIsSelectable(true);
                showText.setMovementMethod(LinkMovementMethod.getInstance());
                String text = kx.replaceFirst("^(.*?)(\\d+).(\\d+)", "$1<a href=https://kangxizidian.com/kxhans/"+hz+">第$2頁第$3字</a>");
                showText.setText(formatPassage(hz, text));
                new AlertDialog.Builder(getContext())
                        .setView(showText)
                        .show();
            });
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
        tv = view.findViewWithTag(COL_HD);
        String hd =  cursor.getString(COL_HD);
        if (!TextUtils.isEmpty(hd)) {
            tv.setOnClickListener(view1 -> {
                TextView showText = new TextView(getContext());
                showText.setPadding(24, 24, 24, 24);
                showText.setTextIsSelectable(true);
                showText.setMovementMethod(LinkMovementMethod.getInstance());
                String text = hd.replaceFirst("(\\d+).(\\d+)", "【汉語大字典】<a href=https://www.homeinmists.com/hd/png/$1.png>第$1頁</a>第$2字");
                showText.setText(formatPassage(hz, text));
                new AlertDialog.Builder(getContext())
                        .setView(showText)
                        .show();
            });
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }

        // Variants
        string = cursor.getString(cursor.getColumnIndexOrThrow("variants"));
        textView = view.findViewById(R.id.text_variants);
        if (!TextUtils.isEmpty(string) && !string.contentEquals(hz)) {
            textView.setText(String.format("(%s)", string));
        } else {
            textView.setText("");
        }

         // "Favorite" button
        boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1;
        Button button = view.findViewById(R.id.button_map);
        button.setOnClickListener(v -> {
            new MyMapView(getContext(), hz).show();
        });
        button = view.findViewById(R.id.button_favorite);
        button.setOnClickListener(v -> {
            Boolean is_favorite = (Boolean) view.getTag(R.id.tag_favorite);
            if (is_favorite) {
                FavoriteDialogs.view(hz, view);
            } else {
                FavoriteDialogs.add(hz);
            }
        });
        if (showFavoriteButton) {
            button.setBackgroundResource(favorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
        else {
            button.setVisibility(View.GONE);
        }
        view.setTag(R.id.tag_favorite, favorite);

        // Favorite comment
        string = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
        textView = view.findViewById(R.id.text_comment);
        textView.setText(string);

        // Set the view's cols to indicate which readings exist
        view.setTag(R.id.tag_cols, cols);
    }

    private static String getHexColor() {
        int color = ContextCompat.getColor(getContext(), R.color.dim);
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private static CharSequence getRichText(String richTextString) {
        String s = richTextString
                .replace("\n", "<br/>")
                .replace("{", "<small><small>")
                .replace("}", "</small></small>")
                .replaceAll("\\*(.+?)\\*", "<b>$1</b>")
                .replaceAll("\\|(.+?)\\|", String.format("<span style='color: %s;'>$1</span>", getHexColor()));
        return HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    public static String getRawText(String s) {
        if (TextUtils.isEmpty(s)) return "";
        return s.replaceAll("[|*\\[\\]]", "").replaceAll("\\{.*?\\}", "");
    }

    private static final Displayer middleChineseDisplayer = new Displayer() {
        public String displayOne(String s) {return Orthography.MiddleChinese.display(s, getStyle(R.string.pref_key_mc_display));}
    };

    private static int getStyle(int id) {
        int value = 0;
        if (id == R.string.pref_key_tone_display) value = 1;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        Resources r = getContext().getResources();
        try {
            return Integer.parseInt(Objects.requireNonNull(sp.getString(r.getString(id), String.valueOf(value))));
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return value;
    }

    private static final Displayer mandarinDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Mandarin.display(s, getStyle(R.string.pref_key_mandarin_display));
        }
    };

    private static final Displayer cantoneseDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Cantonese.display(s, getStyle(R.string.pref_key_cantonese_romanization));
        }
    };

    private static final Displayer nanDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Minnan.display(s, getStyle(R.string.pref_key_minnan_display));
        }
    };

    private static final Displayer baDisplayer = new Displayer() {
        public String displayOne(String s) {
            return s;
        }
    };

    private static final Displayer toneDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Tones.display(s, getCol());
        }
    };

    private static final Displayer koreanDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Korean.display(s, getStyle(R.string.pref_key_korean_display));
        }
    };

    private static final Displayer vietnameseDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Vietnamese.display(s, getStyle(R.string.pref_key_vietnamese_tone_position));
        }
    };

    private static final Displayer japaneseDisplayer = new Displayer() {
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
