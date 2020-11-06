package com.hsicen.library.java;

import android.content.Context;

/**
 * @author alex yarovoi
 * @version 1.0
 */
public class FanLayoutManagerSettings {

    private static final float DEFAULT_VIEW_WIDTH_DP = 120F;
    private static final float DEFAULT_VIEW_HEIGHT_DP = 160F;

    private float mViewWidthDp;
    private float mViewHeightDp;
    private int mViewWidthPx;
    private int mViewHeightPx;
    private boolean mIsFanRadiusEnable;
    private int mItemMargin;
    private boolean mClickScale;

    public int getItemMargin() {
        return mItemMargin;
    }

    public boolean isClickScale() {
        return mClickScale;
    }

    public void setClickScale(boolean mClickScale) {
        this.mClickScale = mClickScale;
    }

    public void setItemMargin(int mItemMargin) {
        this.mItemMargin = mItemMargin;
    }

    public int getTopMargin() {
        return mTopMargin;
    }

    public void setTopMargin(int mTopMargin) {
        this.mTopMargin = mTopMargin;
    }

    public int getBottomMargin() {
        return mBottomMargin;
    }

    public void setBottomMargin(int mBottomMargin) {
        this.mBottomMargin = mBottomMargin;
    }

    private int mTopMargin;
    private int mBottomMargin;

    private FanLayoutManagerSettings(Builder builder) {
        mViewWidthDp = builder.mViewWidthDp;
        mViewHeightDp = builder.mViewHeightDp;
        mIsFanRadiusEnable = builder.mIsFanRadiusEnable;
        mViewWidthPx = builder.mViewWidthPx;
        mViewHeightPx = builder.mViewHeightPx;
        mItemMargin = builder.mItemMargin;
        mTopMargin = builder.mTopMargin;
        mBottomMargin = builder.mBottomMargin;
        mClickScale = builder.mClickScale;
    }

    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    float getViewWidthDp() {
        return mViewWidthDp;
    }

    float getViewHeightDp() {
        return mViewHeightDp;
    }

    boolean isFanRadiusEnable() {
        return mIsFanRadiusEnable;
    }

    int getViewWidthPx() {
        return mViewWidthPx;
    }

    int getViewHeightPx() {
        return mViewHeightPx;
    }

    /**
     * {@code FanLayoutManagerSettings} builder static inner class.
     */
    public static final class Builder {
        private static final float BOUNCE_MAX = 10F;
        private Context mContext;
        private float mViewWidthDp;
        private float mViewHeightDp;
        private boolean mIsFanRadiusEnable;
        private int mViewWidthPx;
        private int mViewHeightPx;
        private int mItemMargin;
        private int mTopMargin;
        private int mBottomMargin;
        private boolean mClickScale;

        private Builder(Context context) {
            mContext = context;
        }

        /**
         * Sets the {@code mViewWidthDp} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param viewWidthDp the {@code mViewWidthDp} to set
         * @return a reference to this Builder
         */
        public Builder withViewWidthDp(float viewWidthDp) {
            mViewWidthDp = viewWidthDp;
            mViewWidthPx = Math.round(mContext.getResources().getDisplayMetrics().density * viewWidthDp);
            mViewWidthPx = Math.min(mContext.getResources().getDisplayMetrics().widthPixels, mViewWidthPx);
            return this;
        }

        /**
         * Sets the {@code mViewHeightDp} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param viewHeightDp the {@code mViewHeightDp} to set
         * @return a reference to this Builder
         */
        public Builder withViewHeightDp(float viewHeightDp) {
            mViewHeightDp = viewHeightDp;
            mViewHeightPx = Math.round(mContext.getResources().getDisplayMetrics().density * viewHeightDp);
            mViewHeightPx = Math.min(mContext.getResources().getDisplayMetrics().heightPixels, mViewHeightPx);
            return this;
        }

        /**
         * Sets the {@code fanRadius} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param isFanRadiusEnable the {@code mIsFanRadiusEnable} to set
         * @return a reference to this Builder
         */
        public Builder withFanRadius(boolean isFanRadiusEnable) {
            mIsFanRadiusEnable = isFanRadiusEnable;
            return this;
        }


        public Builder withItemMargin(int itemMargin) {
            if (itemMargin <= 0F) {
                return this;
            }

            mItemMargin = itemMargin;
            return this;
        }

        public Builder withTopMargin(int topMargin) {
            if (topMargin <= 0F) {
                return this;
            }

            mTopMargin = topMargin;
            return this;
        }

        public Builder withBottomMargin(int bottomMargin) {
            if (bottomMargin <= 0F) {
                return this;
            }

            mBottomMargin = bottomMargin;
            return this;
        }

        public Builder withClickScale(boolean clickScale) {
            mClickScale = clickScale;
            return this;
        }

        /**
         * Returns a {@code FanLayoutManagerSettings} built from the parameters previously set.
         *
         * @return a {@code FanLayoutManagerSettings} built with parameters of this {@code FanLayoutManagerSettings.Builder}
         */
        public FanLayoutManagerSettings build() {
            if (Float.compare(mViewWidthDp, 0F) == 0) {
                withViewWidthDp(DEFAULT_VIEW_WIDTH_DP);
            }
            if (Float.compare(mViewHeightDp, 0F) == 0) {
                withViewHeightDp(DEFAULT_VIEW_HEIGHT_DP);
            }
            return new FanLayoutManagerSettings(this);
        }
    }
}
