package com.osfans.mcpdict.Adapter;

import static com.osfans.mcpdict.DB.BA;
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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DictFragment;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.MainActivity;
import com.osfans.mcpdict.Orth.BaiSha;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.UI.MapView;
import com.osfans.mcpdict.UI.PopupSpan;
import com.osfans.mcpdict.UI.TextDrawable;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Utils;

import java.net.URLEncoder;
import java.util.Objects;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Cursor mCursor = null;
    boolean isMainPage;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView, mTvHead;
        float fontSize;
        int mWidth, mHeight;
        TextDrawable.IBuilder builder;
        String lastLang, lastHz;
        View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            // Define click listener for the ViewHolder's View
            mTvHead = view.findViewById(R.id.head);
            mTvHead.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(mTvHead);
            mTvHead.setMovementMethod(LinkMovementMethod.getInstance());
            mTvHead.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NONE);
            mTextView = view.findViewById(R.id.text);
            mTextView.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(mTextView);
            mTextView.setOnClickListener(this);
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
            mTvHead.setVisibility(View.GONE);
            mTextView.setVisibility(View.GONE);
            if (isMainPage && TextUtils.isEmpty(Pref.getInput())) {
                mTvHead.setText(HtmlCompat.fromHtml(DB.getIntro(), HtmlCompat.FROM_HTML_MODE_COMPACT));
                mTvHead.setVisibility(View.VISIBLE);
                return;
            }
            if (cursor == null || cursor.getCount() == 0) {
                mTvHead.setText(R.string.no_matches);
                mTvHead.setVisibility(View.VISIBLE);
                return;
            }
            checkLast(cursor);
            String hz = cursor.getString(COL_HZ);
            String comment = getResult(String.format("select comment from user.favorite where hz = '%s'", hz));
            boolean bFavorite = (comment != null);
            boolean bNewHz = !hz.contentEquals(lastHz);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            if (bNewHz) {
                lastLang = "";
                if (isMainPage) {
                    ssb.append(hz, new ForegroundColorSpan(getColor(HZ)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.setSpan(new RelativeSizeSpan(1.8f), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
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
                mTvHead.setText(ssb);
                mTvHead.setVisibility(View.VISIBLE);
            }
            String lang = cursor.getString(COL_LANG);
            String ipa = cursor.getString(COL_IPA);
            ssb.clear();
            if (!TextUtils.isEmpty(lang) && !TextUtils.isEmpty(ipa)) {
                ipa = DisplayHelper.formatIPA(lang, ipa).toString();
                if (ipa.contains("<") && !ipa.contains(">")) ipa = ipa.replace("<", "&lt;");
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
                    String label = lang.replace("－", "-").replace("（", "(").replace("）", ")");
                    Drawable drawable = builder.build(label, getColor(lang), getSubColor(lang));
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
                mTextView.setText(ssb);
                mTextView.setVisibility(View.VISIBLE);
            }
        }

        public void copyText(String text) {
            Context context = Utils.getContext();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("item", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.copy_done, Toast.LENGTH_SHORT).show();
        }

        public Cursor getCursor() {
            ResultAdapter adapter = (ResultAdapter) getBindingAdapter();
            if (adapter != null) {
                Cursor cursor = adapter.getCursor();
                if (cursor != null) {
                    int position = getBindingAdapterPosition();
                    if (position >= 0 && position < cursor.getCount()) cursor.moveToPosition(position);
                }
                return cursor;
            }
            return null;
        }

        private Intent getDictIntent(String lang, String hz) {
            String link = DB.getDictLink(lang);
            if (TextUtils.isEmpty(link)) return null;
            String big5 = null;
            String hex = HanZi.toUnicodeHex(hz);
            try {
                big5 = URLEncoder.encode(hz, "big5");
            } catch (Exception ignored) {
            }
            if (Objects.requireNonNull(big5).equals("%3F")) big5 = null;    // Unsupported character
            link = String.format(link, hz, hex, big5);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }

        @Override
        public void onClick(View v) {
            Cursor cursor = getCursor();
            if (cursor == null) return;
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.item_menu, menu);
            MenuCompat.setGroupDividerEnabled(menu, true);
            MenuItem item;
            String hz = cursor.getString(COL_HZ);
            String lang = cursor.getString(COL_LANG);
            String language = DB.getLanguageByLabel(lang);
            item = menu.findItem(R.id.menu_item_goto_info);
            item.setTitle(String.format(Objects.requireNonNull(item.getTitle()).toString(), language));
            item.setOnMenuItemClickListener(i -> {
                Utils.info(v.getContext(), lang);
                return true;
            });
            item = menu.findItem(R.id.menu_item_custom_language);
            boolean isCustom = Utils.isCustomLanguage(language);
            item.setTitle(Pref.getString(isCustom ? R.string.rm_from_custom_language : R.string.add_to_custom_language, lang));
            item.setOnMenuItemClickListener(i -> {
                DictFragment dictFragment = ((MainActivity) v.getContext()).getDictionaryFragment();
                dictFragment.updateCustomLanguage(language);
                Toast.makeText(v.getContext(), Pref.getString(Utils.isCustomLanguage(language) ? R.string.add_to_custom_language_done : R.string.rm_from_custom_language_done, language), Toast.LENGTH_SHORT).show();
                return true;
            });
            item = menu.findItem(R.id.menu_item_copy_readings);
            item.setTitle(Pref.getString(R.string.copy_one_reading, hz, lang));
            String ipa = cursor.getString(COL_IPA);
            item.setOnMenuItemClickListener(i -> {
                String zs = cursor.getString(COL_ZS);
                String reading = String.format("[%s] %s %s%s", lang, hz, DisplayHelper.getIPA(lang, ipa), DisplayHelper.formatJS(zs));
                copyText(reading);
                return true;
            });
            item = menu.findItem(R.id.menu_item_search_homophone);
            item.setTitle(Pref.getString(R.string.search_homophone, DisplayHelper.getIPA(lang, ipa).toString().replaceAll("[ /].*$",""), lang));
            item.setOnMenuItemClickListener(i->{
                String query = ipa.replaceAll("/.*$","");
                if (lang.contentEquals(BA)) query = BaiSha.display(ipa);
                DictFragment dictFragment = ((MainActivity) v.getContext()).getDictionaryFragment();
                dictFragment.setType(1);
                dictFragment.refresh(query, lang);
                return true;
            });
            item = menu.findItem(R.id.menu_item_copy_hz);
            item.setTitle(Pref.getString(R.string.copy_hz, hz));
            item.setOnMenuItemClickListener(i -> {
                copyText(hz);
                return true;
            });
            String dict = DB.getDictName(lang);
            item = menu.findItem(R.id.menu_item_dict_links);
            if (!TextUtils.isEmpty(dict)) {
                item.setTitle(Pref.getString(R.string.one_dict_links, hz, dict));
                item.setIntent(getDictIntent(lang, hz));
                item.setVisible(true);
            } else {
                item.setVisible(false);
            }
            String comment = getResult(String.format("select comment from user.favorite where hz = '%s'", hz));
            item = menu.findItem(R.id.menu_item_favorite);
            if (comment != null) {
                item.setTitle(R.string.favorite_view_or_edit);
            } else {
                item.setTitle(R.string.favorite_add);
            }
            item.setTitle(String.format(Objects.requireNonNull(item.getTitle()).toString(), hz));
            item.setOnMenuItemClickListener(i -> {
                showFavorite(hz, comment);
                return true;
            });
            popupMenu.show();
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

    public Cursor getCursor() {
        return mCursor;
    }
}