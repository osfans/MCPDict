package com.osfans.mcpdict;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.osfans.mcpdict.DB.COL_BH;
import static com.osfans.mcpdict.DB.COL_BS;
import static com.osfans.mcpdict.DB.COL_HD;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_KX;
import static com.osfans.mcpdict.DB.COL_SW;
import static com.osfans.mcpdict.DB.HD;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.KX;
import static com.osfans.mcpdict.DB.SW;
import static com.osfans.mcpdict.DB.getLabel;

public class ResultAdapter extends CursorAdapter {

    private static WeakReference<Context> context;
    private final int layout;
    private final LayoutInflater inflater;
    private final boolean showFavoriteButton;

    public ResultAdapter(Context context, int layout, Cursor cursor, boolean showFavoriteButton) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
        ResultAdapter.context = new WeakReference<>(context);
        this.layout = layout;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showFavoriteButton = showFavoriteButton;
    }

    private static Context getContext() {
        return context.get();
    }

    private static View.OnClickListener getListener(final String lang) {
        return v -> {
            ViewHolder holder = (ViewHolder) ((View)v.getParent().getParent()).getTag();
            holder.col = DB.getColumnIndex(lang);
            BaseActivity activity = (BaseActivity) getContext();
            activity.registerForContextMenu(v);
            activity.openContextMenu(v);
            activity.unregisterForContextMenu(v);
        };
    }

    private static int getMeasuredWidth(TextView textView) {
        textView.measure(0, 0);
        return textView.getMeasuredWidth();
    }

    private static int getMaxWidth(TextView textView) {
        textView.setText("中文");
        return getMeasuredWidth(textView);
    }

    private static void formatTextView(TextView tv, String lang) {
        int color = DB.getColor(lang);
        int subColor = DB.getSubColor(lang);
        if (color == subColor) {
            tv.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] {subColor, color, color});
            gd.setCornerRadius(5f);
            tv.setBackground(gd);
        }
        //tv.setAlpha(0.8f);
    }

    static class ViewHolder {
        TextView tvHZ, tvUnicode, tvSW, tvKX, tvHD, tvComment, tvVariant;
        Button btnMap, btnFavorite;
        TextView[] tvDetails;
        TableRow[] rows;
        boolean isFavorite;
        Set<Integer> cols;
        int col = -1;

        public ViewHolder(View view, Context context) {
            tvHZ = view.findViewById(R.id.text_hz);
            tvHZ.setOnClickListener(getListener(HZ));
            tvUnicode = view.findViewById(R.id.text_unicode);
            tvSW = view.findViewById(R.id.text_sw);
            tvSW.setText(getLabel(SW));
            tvKX = view.findViewById(R.id.text_kx);
            tvKX.setText(getLabel(KX));
            tvHD = view.findViewById(R.id.text_hd);
            tvHD.setText(getLabel(HD));
            tvComment = view.findViewById(R.id.text_comment);
            tvVariant = view.findViewById(R.id.text_variants);
            btnMap = view.findViewById(R.id.button_map);
            btnFavorite = view.findViewById(R.id.button_favorite);
            tvDetails = new TextView[DB.COL_LAST_LANG + 1];
            rows = new TableRow[DB.COL_LAST_LANG + 1];
            tvDetails[0] = tvHZ;
            TableLayout table = view.findViewById(R.id.text_readings);
            for (String lang: DB.getLanguages()) {
                int i = DB.getColumnIndex(lang);
                TableRow row = (TableRow)LayoutInflater.from(context).inflate(R.layout.search_result_row, null);
                TextView textViewName = row.findViewById(R.id.text_name);
                String name = DB.getLabel(lang);
                if (TextUtils.isEmpty(name)) continue;
                formatTextView(textViewName, lang);
                textViewName.setText(name);
                float ratio = 8f/(name.getBytes().length + name.length()) * 1.25f;
                if (ratio < 1) textViewName.setTextScaleX(ratio);
                row.setOnClickListener(getListener(lang));
                table.addView(row);
                tvDetails[i]  = row.findViewById(R.id.text_detail);
                rows[i] = row;
            }
            table.setColumnShrinkable(1, true);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder(view, context);
        view.setTag(holder);
        return view;
    }

    public static boolean isColumnVisible(String languages, Set<String> customs, String lang) {
        if (DB.isPreLang(lang) || languages.contentEquals("*")) return true;
        if (languages.contentEquals("3") || languages.contentEquals("5")) {
            int size = DB.getSize(lang);
            return size >= Integer.parseInt(languages);
        }
        if (TextUtils.isEmpty(languages)) {
            if (customs == null || customs.size() == 0) return true;
            return customs.contains(lang);
        }
        ArrayList<String> array = DB.getLanguages(languages);
        if (array != null && array.size() > 0) {
            return array.contains(lang);
        }
        return lang.matches(languages);
    }

    public static CharSequence formatIPA(String lang, String string) {
        CharSequence cs;
        if (TextUtils.isEmpty(string)) return "";
        switch (lang) {
            case DB.SG:
                cs = getRichText(string);
                break;
            case DB.BA:
                cs = baDisplayer.display(string);
                break;
            case DB.GY:
                cs = getRichText(gyDisplayer.display(string));
                break;
            case DB.CMN:
                cs = getRichText(cmnDisplayer.display(string));
                break;
            case DB.HK:
                cs = hkDisplayer.display(string);
                break;
            case DB.TW:
                cs = getRichText(twDisplayer.display(string));
                break;
            case DB.KOR:
                cs = korDisplayer.display(string);
                break;
            case DB.VI:
                cs = viDisplayer.display(string);
                break;
            case DB.JA_GO:
            case DB.JA_KAN:
            case DB.JA_TOU:
            case DB.JA_KWAN:
            case DB.JA_OTHER:
                cs = getRichText(jaDisplayer.display(string));
                break;
            default:
                cs = getRichText(toneDisplayer.display(string, lang));
                break;
        }
        return cs;
    }

    private CharSequence formatPassage(String hz, String js) {
        String[] fs = (js+"\n").split("\n", 2);
        String s = String.format("<p><big><big><big>%s</big></big></big> %s</p><br><p>%s</p>", hz, fs[0], fs[1].replace("\n", "<br/>"));
        return getRichText(s);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        String hz, string;
        TextView textView;
        Set<Integer> cols = new HashSet<>();
        Orthography.setToneStyle(getStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(getStyle(R.string.pref_key_tone_value_display));
        String languages = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_show_language_names), "");
        Set<String> customs = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(context.getString(R.string.pref_key_custom_languages), null);

        for (String lang: DB.getLanguages()) {
            int i = DB.getColumnIndex(lang);
            string = cursor.getString(i);
            boolean visible = string != null && isColumnVisible(languages, customs, lang);
            if (holder.rows[i] == null) continue;
            holder.rows[i].setVisibility(visible ? View.VISIBLE : View.GONE);
            if (!visible) continue;
            cols.add(i);
            textView = holder.tvDetails[i];
            textView.setTag(getRawText(string));
            CharSequence cs = formatIPA(lang, string);
            textView.setText(cs);
        }

        // HZ
        hz = cursor.getString(COL_HZ);
        holder.tvHZ.setText(hz);
        cols.add(COL_HZ);
        textView = holder.tvUnicode;
        String unicode = Orthography.HZ.toUnicode(hz);
        textView.setText(unicode);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<p><big><big><big>%s</big></big></big></p><p>【統一碼】%s %s</p>", hz, unicode, Orthography.HZ.getUnicodeExt(hz)));
        for (int i = DB.COL_LF; i < DB.COL_VA; i++) {
            if (i == COL_SW) i = COL_BH;
            String str = cursor.getString(i);
            if (i == COL_BS) str = str.replace("f", "-");
            if (TextUtils.isEmpty(str)) continue;
            str = str.toUpperCase();
            sb.append(String.format("<p>【%s】%s</p>", DB.getColumn(i), str));
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
        TextView tv = holder.tvSW;
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
        tv = holder.tvKX;
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
        tv = holder.tvHD;
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
        textView = holder.tvVariant;
        if (!TextUtils.isEmpty(string) && !string.contentEquals(hz)) {
            textView.setText(String.format("(%s)", string));
        } else {
            textView.setText("");
        }

         // "Favorite" button
        boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1;
        holder.isFavorite = favorite;
        holder.btnMap.setOnClickListener(v -> new MyMapView(getContext(), hz).show());
        Button button = holder.btnFavorite;
        button.setOnClickListener(v -> {
            if (holder.isFavorite) {
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

        // Favorite comment
        string = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
        textView = holder.tvComment;
        textView.setText(string);

        // Set the view's cols to indicate which readings exist
        holder.cols = cols;
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

    private static final Displayer gyDisplayer = new Displayer() {
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

    private static final Displayer cmnDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Mandarin.display(s, getStyle(R.string.pref_key_mandarin_display));
        }
    };

    private static final Displayer hkDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Cantonese.display(s, getStyle(R.string.pref_key_cantonese_romanization));
        }
    };

    private static final Displayer twDisplayer = new Displayer() {
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
            return Orthography.Tones.display(s, getLang());
        }
    };

    private static final Displayer korDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Korean.display(s, getStyle(R.string.pref_key_korean_display));
        }
    };

    private static final Displayer viDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Vietnamese.display(s, getStyle(R.string.pref_key_vietnamese_tone_position));
        }
    };

    private static final Displayer jaDisplayer = new Displayer() {
        public String displayOne(String s) {
            return Orthography.Japanese.display(s, getStyle(R.string.pref_key_japanese_display));
        }
    };
}
