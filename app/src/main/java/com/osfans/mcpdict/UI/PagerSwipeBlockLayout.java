package com.osfans.mcpdict.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * A normal LinearLayout that prevents an ancestor ViewPager2 from stealing
 * gestures that start inside the matrix area.
 *
 * It does not consume the gesture itself: all child RecyclerViews, cells and
 * scrollbars continue to receive touch events normally.
 */
public class PagerSwipeBlockLayout extends LinearLayout {

    public PagerSwipeBlockLayout(Context context) {
        super(context);
    }

    public PagerSwipeBlockLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerSwipeBlockLayout(Context context,
                                 @Nullable AttributeSet attrs,
                                 int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        ViewParent parent = getParent();
        if (parent != null) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                parent.requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL) {
                parent.requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
