package com.hsicen.library;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：hsicen  2020/11/6 16:58
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：滑动处理
 */
class FanCardScroller extends BaseSmoothScroller {
    private static final float MILLISECONDS_PER_INCH = 80F;

    @Nullable
    private FanCardTimeCallback mCardTimeCallback;

    /**
     * LinearSmoothScroller for switch views.
     *
     * @param context Context
     */
    FanCardScroller(Context context) {
        super(context);
    }

    @Override
    protected int getHorizontalSnapPreference() {
        return SNAP_TO_START;
    }

    @Override
    public int calculateDxToMakeVisible(View view, int snapPreference) {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null) {
            // add to calculated dx offset. Need to scroll to center of RecyclerView.
            return super.calculateDxToMakeVisible(view, snapPreference) + layoutManager.getWidth() / 2 - view.getWidth() / 2;
        } else {
            // no layoutManager detected - not expected case. can be magic or end of the world...
            return super.calculateDxToMakeVisible(view, snapPreference);
        }
    }

    @Override
    protected int calculateTimeForScrolling(int dx) {
        int time = super.calculateTimeForScrolling(dx);
        if (mCardTimeCallback != null) {
            mCardTimeCallback.onTimeForScrollingCalculated(getTargetPosition(), time);
        }
        return time;
    }

    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }

    @Nullable
    public FanCardTimeCallback getCardTimeCallback() {
        return mCardTimeCallback;
    }

    void setCardTimeCallback(@Nullable FanCardTimeCallback cardTimeCallback) {
        mCardTimeCallback = cardTimeCallback;
    }

    interface FanCardTimeCallback {
        /**
         * @param targetPosition item position to scroll to
         * @param time           scroll duration
         */
        void onTimeForScrollingCalculated(int targetPosition, int time);
    }
}
