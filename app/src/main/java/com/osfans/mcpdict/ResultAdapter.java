package com.osfans.mcpdict;

import static com.osfans.mcpdict.DB.COL_BH;
import static com.osfans.mcpdict.DB.COL_BS;
import static com.osfans.mcpdict.DB.COL_HD;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_KX;
import static com.osfans.mcpdict.DB.COL_SW;
import static com.osfans.mcpdict.DB.COMMENT;
import static com.osfans.mcpdict.DB.HD;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.KX;
import static com.osfans.mcpdict.DB.SW;
import static com.osfans.mcpdict.DB.UNICODE;
import static com.osfans.mcpdict.DB.VARIANTS;
import static com.osfans.mcpdict.DB.getColor;
import static com.osfans.mcpdict.DB.getColumn;
import static com.osfans.mcpdict.DB.getLabel;
import static com.osfans.mcpdict.DB.getLanguages;
import static com.osfans.mcpdict.DB.getSubColor;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.DrawableMarginSpan;
import android.text.style.ParagraphStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

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

    static class ViewHolder implements View.OnClickListener{
        TextView tvHZ, tvUnicode, tvSW, tvKX, tvHD, tvComment, tvVariant;
        Button btnMap, btnFavorite;
        TextView tvReadings;
        boolean isFavorite;
        Set<String> cols;
        String col = "";
        WeakHashMap<String, ParagraphStyle> spans;
        WeakHashMap<String, String> rawTexts;
        WeakHashMap<String, TextView> tvs;

        public ViewHolder(View view) {
            tvHZ = view.findViewById(R.id.text_hz);
            tvHZ.setOnClickListener(this);
            tvUnicode = view.findViewById(R.id.text_unicode);
            tvUnicode.setOnClickListener(this);
            tvSW = view.findViewById(R.id.text_sw);
            tvSW.setText(getLabel(SW));
            tvSW.setOnClickListener(this);
            tvKX = view.findViewById(R.id.text_kx);
            tvKX.setText(getLabel(KX));
            tvKX.setOnClickListener(this);
            tvHD = view.findViewById(R.id.text_hd);
            tvHD.setText(getLabel(HD));
            tvHD.setOnClickListener(this);
            tvComment = view.findViewById(R.id.text_comment);
            tvVariant = view.findViewById(R.id.text_variants);
            tvs = new WeakHashMap<>();
            tvs.put(UNICODE, tvUnicode);
            tvs.put(SW, tvSW);
            tvs.put(KX, tvKX);
            tvs.put(HD, tvHD);
            tvs.put(COMMENT, tvComment);
            tvs.put(VARIANTS, tvVariant);
            btnMap = view.findViewById(R.id.button_map);
            btnMap.setOnClickListener(this);
            btnFavorite = view.findViewById(R.id.button_favorite);
            tvReadings = view.findViewById(R.id.text_readings);
            spans = new WeakHashMap<>();
            rawTexts = new WeakHashMap<>();
            float fontSize = tvReadings.getTextSize();
            TextDrawable.IBuilder builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(1)
                    .width((int) (fontSize * 3.1f))  // width in px
                    .height((int) (fontSize * 1.25f)) // height in px
                    .fontSize(fontSize)
                    .endConfig()
                    .roundRect(5);
            for (String lang: getLanguages()) {
                Drawable drawable = builder.build(getLabel(lang), getColor(lang), getSubColor(lang));
                DrawableMarginSpan span = new DrawableMarginSpan(drawable, 10);
                spans.put(lang, span);
            }
        }

        @Override
        public void onClick(View view) {
            Context context = getContext();
            String hz = rawTexts.get(HZ);
            if (view == btnMap) {
                new MyMapView(context, hz).show();
                return;
            } else if (view == tvReadings) {
                BaseActivity activity = (BaseActivity) context;
                activity.registerForContextMenu(view);
                activity.openContextMenu(view);
                activity.unregisterForContextMenu(view);
            }
            for (String key: new String[]{UNICODE, SW, KX, HD}) {
                if (view == tvs.get(key)) {
                    TextView showText = new TextView(context);
                    showText.setPadding(24, 24, 24, 24);
                    showText.setTextIsSelectable(true);
                    showText.setMovementMethod(LinkMovementMethod.getInstance());
                    //showText.setText(formatPassage(hz, rawTexts.get(key)));
                    new AlertDialog.Builder(context).setView(showText).show();
                    return;
                }
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) view = convertView;
        else {
            view = inflater.inflate(layout, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        return super.getView(position, view, parent);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        String hz, s;
        Set<String> cols = new HashSet<>();
        Orthography.setToneStyle(DictApp.getStyle(R.string.pref_key_tone_display));
        Orthography.setToneValueStyle(DictApp.getStyle(R.string.pref_key_tone_value_display));
        hz = cursor.getString(COL_HZ);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (String lang : DB.getVisibleColumns(getContext())) {
            int i = DB.getColumnIndex(lang);
            s = cursor.getString(i);
            if (TextUtils.isEmpty(s)) continue;
            CharSequence cs = DictApp.formatIPA(lang, s);
            int n = ssb.length();
            ssb.append(" ", holder.spans.get(lang), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(cs);
            ssb.append("\n");
            ssb.setSpan(new MyClickableSpan(lang), n, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            cols.add(lang);
        }
        holder.tvReadings.setText(ssb);
        holder.tvReadings.setMovementMethod(LinkMovementMethod.getInstance());

        for (int i = DB.COL_HZ; i < DB.COL_VA; i++) {
            s = cursor.getString(i);
            if (TextUtils.isEmpty(s)) s = "";
            if (i == COL_BS) s = s.replace("f", "-");
            else if (i == COL_KX) s = s.replaceFirst("^(.*?)(\\d+).(\\d+)", "$1<a href=https://kangxizidian.com/kxhans/" + hz + ">第$2頁第$3字</a>");
            else if (i == COL_HD) s = s.replaceFirst("(\\d+).(\\d+)", "【汉語大字典】<a href=https://www.homeinmists.com/hd/png/$1.png>第$1頁</a>第$2字");
            holder.rawTexts.put(DB.getColumn(i), s);
        }

        // HZ
        TextView tv;
        holder.tvHZ.setText(hz);
        cols.add(HZ);
        // Unicode
        s = Orthography.HZ.toUnicode(hz);
        holder.tvUnicode.setText(s);
        StringBuilder sb = new StringBuilder();
        String unicode = s;
        sb.append(String.format("<p>【統一碼】%s %s</p>", unicode, Orthography.HZ.getUnicodeExt(hz)));
        for (int i = DB.COL_LF; i < DB.COL_VA; i++) {
            if (i == COL_SW) i = COL_BH;
            String lang = getColumn(i);
            s = holder.rawTexts.getOrDefault(lang, "");
            if (TextUtils.isEmpty(s)) continue;
            sb.append(String.format("<p>【%s】%s</p>", lang, s));
        }
        String info = sb.toString().replace(",", ", ");
        holder.rawTexts.put(UNICODE, info);
        // Variants
        s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
        if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
            s = String.format("(%s)", s);
        } else s = "";
        holder.rawTexts.put(VARIANTS, s);
        holder.tvVariant.setText(s);
        // Favorite comment
        s = cursor.getString(cursor.getColumnIndexOrThrow(COMMENT));
        holder.rawTexts.put(COMMENT, s);
        holder.tvComment.setText(s);

        for (String key: new String[]{SW, KX, HD, VARIANTS, COMMENT}) {
            tv = holder.tvs.get(key);
            s =  holder.rawTexts.get(key);
            tv.setVisibility(TextUtils.isEmpty(s) ? View.GONE: View.VISIBLE);
        }

         // "Favorite" button
        boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1;
        holder.isFavorite = favorite;
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
        } else {
            button.setVisibility(View.GONE);
        }

        // Set the view's cols to indicate which readings exist
        holder.cols = cols;
    }
}
