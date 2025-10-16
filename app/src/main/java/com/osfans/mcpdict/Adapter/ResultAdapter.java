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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.osfans.mcpdict.Orth.DisplayHelper;
import com.osfans.mcpdict.Favorite.FavoriteDialogs;
import com.osfans.mcpdict.MainActivity;
import com.osfans.mcpdict.Orth.BaiSha;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Util.OpenCC;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.UI.MapView;
import com.osfans.mcpdict.UI.PopupSpan;
import com.osfans.mcpdict.UI.TextDrawable;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.App;

import java.net.URLEncoder;
import java.util.Objects;

import me.zhanghai.android.fastscroll.PopupTextProvider;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> implements PopupTextProvider {

    private Cursor mCursor = null;
    boolean isMainPage;

    @NonNull
    @Override
    public CharSequence getPopupText(@NonNull View view, int position) {
        if (mCursor == null || position == 0) return "";
        return mCursor.getString(COL_HZ);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView, mTvHead;
        TextDrawable.IBuilder builder;
        String lastLang, lastHz;
        View mView, mViewLang;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            // Define click listener for the ViewHolder's View
            mTvHead = view.findViewById(R.id.head);
            mTvHead.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(mTvHead);
            mTvHead.setMovementMethod(LinkMovementMethod.getInstance());
            mTvHead.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NONE);
            mViewLang = view.findViewById(R.id.textLang);
            mViewLang.setOnClickListener(this);
            mTextView = view.findViewById(R.id.text);
            mTextView.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(mTextView);
            mTextView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP && !((TextView) v).hasSelection()) {
                    onClick(v);
                    v.performClick();
                }
                return false;
            });
            int width = view.getResources().getDimensionPixelOffset(R.dimen.label_width);
            int height = view.getResources().getDimensionPixelOffset(R.dimen.label_height);
            builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(3)
                    .width(width)  // width in px
                    .height(height) // height in px
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

        public String normLabel(String lang) {
            String label = lang.replace("－", "-").replace("（", "(").replace("）", ")");
            return OpenCC.convertToOld(label);
        }

        public void set(Cursor cursor, boolean isMainPage) {
            mTvHead.setVisibility(View.GONE);
            mViewLang.setVisibility(View.GONE);
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
                    mViewLang.setVisibility(View.INVISIBLE);
                } else {
                    Drawable drawable = builder.build(normLabel(lang), getColor(lang), getSubColor(lang));
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mViewLang.setBackground(drawable);
                    mViewLang.setVisibility(View.VISIBLE);
                }
                CharSequence cs = HtmlCompat.fromHtml(ipa, HtmlCompat.FROM_HTML_MODE_COMPACT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ssb.append(cs, new TypefaceSpan(FontUtil.getIPATypeface()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    ssb.append(cs);
                }
                String zs = cursor.getString(COL_ZS);
                if (!TextUtils.isEmpty(zs)) {
                    zs = DisplayHelper.formatZS(hz, zs);
                    cs = HtmlCompat.fromHtml(zs, HtmlCompat.FROM_HTML_MODE_COMPACT);
                    ssb.append(cs);
                }
                mTextView.setText(ssb);
                mTextView.setVisibility(View.VISIBLE);
            }
        }

        public void copyText(String text) {
            Context context = App.getContext();
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

        private Intent getLinkIntent(String link, String hz) {
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

        private Intent getDictIntent(String lang, String hz) {
            String link = DB.getDictLink(lang);
            return getLinkIntent(link, hz);
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
                App.info(v.getContext(), lang);
                return true;
            });
            item = menu.findItem(R.id.menu_item_custom_language);
            boolean isCustom = App.isCustomLanguage(language);
            item.setTitle(Pref.getString(isCustom ? R.string.rm_from_custom_language : R.string.add_to_custom_language));
            item.setOnMenuItemClickListener(i -> {
                DictFragment dictFragment = ((MainActivity) v.getContext()).getDictionaryFragment();
                dictFragment.updateCustomLanguage(language);
                Toast.makeText(v.getContext(), Pref.getString(App.isCustomLanguage(language) ? R.string.add_to_custom_language_done : R.string.rm_from_custom_language_done, language), Toast.LENGTH_SHORT).show();
                return true;
            });
            item = menu.findItem(R.id.menu_item_copy_readings);
            item.setTitle(Pref.getString(R.string.copy_one_reading, hz));
            String ipa = cursor.getString(COL_IPA);
            item.setOnMenuItemClickListener(i -> {
                String zs = cursor.getString(COL_ZS);
                String reading = String.format("[%s] %s %s%s", lang, hz, DisplayHelper.getIPA(lang, ipa), DisplayHelper.formatJS(hz, zs));
                copyText(reading);
                return true;
            });
            item = menu.findItem(R.id.menu_item_search_homophone);
            item.setTitle(Pref.getString(R.string.search_homophone, DisplayHelper.getIPA(lang, ipa).toString().replaceAll("[ /].*$","")));
            item.setOnMenuItemClickListener(i->{
                String query = ipa.replaceAll("/.*$","").replace("-", " ").replace("=", " ").trim();
                if (lang.contentEquals(BA)) query = BaiSha.display(ipa.replaceAll("\\([^()]*?\\)$", "").trim());
                else query = query.replace("*", "");
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
                item.setTitle(dict);
                item.setIntent(getDictIntent(lang, hz));
                item.setVisible(true);
            } else {
                item.setVisible(false);
            }
            item = menu.findItem(R.id.menu_item_dict_zdic);
            item.setIntent(getLinkIntent("https://zdic.net/hans/%s", hz));
            item = menu.findItem(R.id.menu_item_dict_zisea);
            item.setIntent(getLinkIntent("http://zisea.com/zscontent.asp?uni=%2$s", hz));
            item = menu.findItem(R.id.menu_item_dict_moedict);
            item.setIntent(getLinkIntent("https://www.moedict.tw/%s", hz));
            item = menu.findItem(R.id.menu_item_dict_zitools);
            item.setIntent(getLinkIntent("https://zi.tools/zi/%s", hz));
            item = menu.findItem(R.id.menu_item_dict_unihan);
            item.setIntent(getLinkIntent("https://www.unicode.org/cgi-bin/GetUnihanData.pl?codepoint=%s", hz));
            item = menu.findItem(R.id.menu_item_dict_chise);
            item.setIntent(getLinkIntent("https://www.chise.org/est/view/character/%s", hz));
            item = menu.findItem(R.id.menu_item_dict_kangxi);
            item.setIntent(getLinkIntent("https://kangxizidian.com/kxhans/%s", hz));
            String comment = getResult(String.format("select comment from user.favorite where hz = '%s'", hz));
            item = menu.findItem(R.id.menu_item_favorite);
            if (comment != null) {
                item.setTitle(R.string.favorite_view_or_edit);
            } else {
                item.setTitle(R.string.favorite_add);
            }
            item.setTitle(String.format(Objects.requireNonNull(item.getTitle()).toString(), hz));
            item.setOnMenuItemClickListener(i -> {
                if (TextUtils.isEmpty(comment)) {
                    FavoriteDialogs.add(hz, lang + ":");
                } else {
                    FavoriteDialogs.view(hz, comment);
                }
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