package com.osfans.mcpdict.Adapter;

import static com.osfans.mcpdict.DB.COL_FIRST_DICT;
import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;
import static com.osfans.mcpdict.DB.COL_LAST_DICT;
import static com.osfans.mcpdict.DB.COL_ZS;
import static com.osfans.mcpdict.DB.HZ;
import static com.osfans.mcpdict.DB.VARIANTS;
import static com.osfans.mcpdict.DB.getColor;
import static com.osfans.mcpdict.DB.getLabel;
import static com.osfans.mcpdict.DB.getResult;
import static com.osfans.mcpdict.DB.getSubColor;
import static com.osfans.mcpdict.DB.getUnicode;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.DrawableMarginSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.UI.MapView;
import com.osfans.mcpdict.UI.PopupSpan;
import com.osfans.mcpdict.UI.TextDrawable;
import com.osfans.mcpdict.Util.FontUtil;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Cursor mCursor = null;
    boolean isMainPage;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        float fontSize;
        int mWidth, mHeight;
        TextDrawable.IBuilder builder;
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String lastLang, lastHz;
        View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            // Define click listener for the ViewHolder's View
            mTextView = view.findViewById(R.id.text);
            mTextView.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(mTextView);
            mTextView.setTextIsSelectable(true);
            mTextView.setMovementMethod(LinkMovementMethod.getInstance());
            mTextView.setTag(this);
            mTextView.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NONE);
            fontSize = mTextView.getTextSize();
            mWidth = (int) (fontSize * 3.0f);
            mHeight = (int) (fontSize * 1.6f);
            builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(3)
                    .width(mWidth)  // width in px
                    .height(mHeight) // height in px
                    .fontSize(fontSize * 0.85f)
                    .endConfig()
                    .roundRect(5);
        }

        public void checkLast(Cursor cursor) {
            int position = cursor.getPosition();
            if (position == 0) {
                lastHz = "";
                lastLang = "";
                return;
            }
            cursor.moveToPrevious();
            lastHz = cursor.getString(COL_HZ);
            lastLang = cursor.getString(COL_LANG);
            cursor.moveToNext();
        }

        public void showFavorite(String hz, String comment) {
            if (comment != null) {
                FavoriteDialogs.view(hz, comment);
            } else {
                FavoriteDialogs.add(hz);
            }
        }

        public void showMap(String hz) {
            new MapView(mView.getContext(), hz).show();
        }

        public void set(Cursor cursor, boolean isMainPage) {
            if (TextUtils.isEmpty(Pref.getInput())) {
                mTextView.setText(HtmlCompat.fromHtml(DB.getIntro(), HtmlCompat.FROM_HTML_MODE_COMPACT));
                return;
            } else if (cursor == null || cursor.getCount() == 0) {
                mTextView.setText(R.string.no_matches);
                return;
            }
            checkLast(cursor);
            String hz = cursor.getString(COL_HZ);
            String comment = getResult(String.format("select comment from user.favorite where hz = '%s'", hz));
            boolean bFavorite = (comment != null);
            boolean bNewHz = !hz.contentEquals(lastHz);
            StringBuilder raws = new StringBuilder();
            ssb.clear();
            if (bNewHz) {
                lastLang = "";
                int n = ssb.length();
                ssb.append(hz, new ForegroundColorSpan(getColor(HZ)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.8f), n, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Variants
                String s = cursor.getString(cursor.getColumnIndexOrThrow(VARIANTS));
                if (!TextUtils.isEmpty(s) && !s.contentEquals(hz)) {
                    s = String.format("(%s)", s);
                    ssb.append(s, new ForegroundColorSpan(mView.getResources().getColor(R.color.dim, mView.getContext().getTheme())), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                int color = mView.getResources().getColor(R.color.accent, mView.getContext().getTheme());
                // Unicode
                String unicode = HanZi.toUnicode(hz);
                Cursor dictCursor = DB.getDictCursor(hz);
                if (dictCursor.getCount() == 1) {
                    dictCursor.moveToFirst();
                    ssb.append(" " + unicode + " ", new PopupSpan(DisplayHelper.formatPopUp(hz, COL_HZ, getUnicode(dictCursor)), COL_HZ, color), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    raws.setLength(0);
                    raws.append(String.format("%s %s\n", hz, unicode));
                    // DICTS
                    for (int i = COL_FIRST_DICT; i <= COL_LAST_DICT; i++) {
                        s = dictCursor.getString(i);
                        if (!TextUtils.isEmpty(s)) {
                            ssb.append(" " + getLabel(i) + " ", new PopupSpan(DisplayHelper.formatPopUp(hz, i, s), i, color), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                dictCursor.close();
                // Map
                ssb.append(DB.MAP + " ", new PopupSpan(hz, 0, color) {
                    @Override
                    public void onClick(@NonNull View view) {
                        view.post(() -> showMap(hz));
                    }
                }, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Favorite
                if (isMainPage) {
                    String label = bFavorite ? "⭐":"⛤";
                    ssb.append(" " + label + " ", new PopupSpan(hz, 0, color) {
                        @Override
                        public void onClick(@NonNull View view) {
                            showFavorite(hz, comment);
                        }
                    }, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            String lang = cursor.getString(COL_LANG);
            String ipa = cursor.getString(COL_IPA);
            if (!TextUtils.isEmpty(lang) && !TextUtils.isEmpty(ipa)) {
                if (bNewHz) ssb.append("\n");
                ipa = DisplayHelper.formatIPA(lang, ipa).toString();
                if (ipa.contains("<") && !ipa.contains(">")) ipa = ipa.replace("<", "&lt;");
                int n = ssb.length();
                String raw = DisplayHelper.getRawText(ipa);

                if (lang.contentEquals(lastLang)) {
                    LeadingMarginSpan.LeadingMarginSpan2 span = new LeadingMarginSpan.LeadingMarginSpan2() {
                        @Override
                        public int getLeadingMarginLineCount() {
                            return 0;
                        }

                        @Override
                        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {

                        }

                        @Override
                        public int getLeadingMargin(boolean first) {
                            return mWidth + 3;
                        }
                    };
                    ssb.append(" ", span, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    Drawable drawable = builder.build(lang.replace("－", "-").replace("（", "(").replace("）", ")"), getColor(lang), getSubColor(lang));
                    DrawableMarginSpan span = new DrawableMarginSpan(drawable, 3);
                    ssb.append(" ", span, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                CharSequence cs = HtmlCompat.fromHtml(ipa, HtmlCompat.FROM_HTML_MODE_COMPACT);
                ssb.append(cs);
                String zs = cursor.getString(COL_ZS);
                if (!TextUtils.isEmpty(zs)) {
                    zs = DisplayHelper.formatZS(zs);
                    cs = HtmlCompat.fromHtml(zs, HtmlCompat.FROM_HTML_MODE_COMPACT);
                    ssb.append(cs);
                }
            }
            mTextView.setText(ssb);
        }
    }

    public ResultAdapter(boolean isMainPage) {
        super();
        this.isMainPage = isMainPage;
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) mCursor.close();
        if (cursor != null) cursor.moveToFirst();
        mCursor = cursor;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.result_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (mCursor != null) mCursor.moveToPosition(position);
        viewHolder.set(mCursor, isMainPage);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mCursor == null || mCursor.getCount() == 0) return 1;
        return mCursor.getCount();
    }
}