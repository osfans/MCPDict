package com.osfans.mcpdict.Adapter;

import static com.osfans.mcpdict.DB.COL_HZ;
import static com.osfans.mcpdict.DB.COL_IPA;
import static com.osfans.mcpdict.DB.COL_LANG;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.osfans.mcpdict.DB;
import com.osfans.mcpdict.DisplayHelper;
import com.osfans.mcpdict.Orth.HanZi;
import com.osfans.mcpdict.Pref;
import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.FontUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    public final static List<String> mHZs = new ArrayList<>();
    public final static Map<String, String> mIPAs = new HashMap<>();
    public final static List<Integer> mPositions = new ArrayList<>();
    public static RecyclerView mRecyclerView;
    public static String mCurrentLanguage;
    public static boolean mShowIPA = false;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHZ, tvIPA;
        View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            // Define click listener for the ViewHolder's View
            tvIPA = view.findViewById(R.id.ipa);
            FontUtil.setTypeface(tvIPA);
            tvIPA.setVisibility(mShowIPA ? View.VISIBLE : View.GONE);
            tvHZ = view.findViewById(R.id.hz);
            tvHZ.setTextAppearance(R.style.FontDetail);
            FontUtil.setTypeface(tvHZ);
            mView.setClickable(true);
            mView.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                int position = mPositions.get(index);
                mRecyclerView.scrollToPosition(position);
            });
        }

        public void set(int position) {
            String hz = mHZs.get(position);
            tvHZ.setText(hz);
            if (mShowIPA && !HanZi.isUnknown(hz)) {
                String ipa = mIPAs.getOrDefault(hz, "");
                tvIPA.setText(DisplayHelper.formatIPA(mCurrentLanguage, ipa));
            }
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param cursor String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public IndexAdapter(Cursor cursor, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        changeCursor(cursor);
    }

    public void changeCursor(Cursor cursor) {
        mHZs.clear();
        mIPAs.clear();
        mPositions.clear();
        String lastHz = "";
        mCurrentLanguage = Pref.getLabel();
        DB.FILTER filter = Pref.getFilter();
        if (cursor != null) {
            mShowIPA = (filter == DB.FILTER.CURRENT && Pref.getBool(R.string.pref_key_show_ipa, false));
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String hz = cursor.getString(COL_HZ);
                if (!hz.contentEquals(lastHz)) {
                    mHZs.add(hz);
                    mPositions.add(cursor.getPosition());
                }
                String lang = cursor.getString(COL_LANG);
                if (lang.contentEquals(mCurrentLanguage)) {
                    String ipa = cursor.getString(COL_IPA);
                    String lastIpa = mIPAs.getOrDefault(hz, "") + " ";
                    mIPAs.put(hz, (lastIpa + ipa).trim());
                }
                lastHz = hz;
            }
        }
        notifyDataSetChanged();
    }

    public IndexAdapter(RecyclerView recyclerView) {
        this(null, recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.index_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.set(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mHZs.size();
    }
}