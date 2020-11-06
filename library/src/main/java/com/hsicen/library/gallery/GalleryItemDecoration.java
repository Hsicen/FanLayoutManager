package com.hsicen.library.gallery;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * 作者：hsicen  2020/11/5 17:57
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：item布局设置
 */
public class GalleryItemDecoration extends RecyclerView.ItemDecoration {
    private final String TAG = "MainActivity_TAG";

    //默认px值
    /*** 每一个页面默认页边距*/
    public int mPageMargin = 0;
    public int mItemWidth = 0;
    public int mItemHeight = 0;

    public int mItemConsumeY = 0;
    public int mItemConsumeX = 0;

    private GalleryRecyclerView.OnItemClickListener onItemClickListener;

    private OnItemSizeMeasuredListener mOnItemSizeMeasuredListener;

    GalleryItemDecoration() {
    }

    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull final View view, @NotNull final RecyclerView parent,
                               @NotNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int position = parent.getChildAdapterPosition(view);
        final int itemCount = parent.getAdapter().getItemCount();

        parent.post(() -> {
            if (((GalleryRecyclerView) parent).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                onSetHorizontalParams(parent, view, position, itemCount);
            } else {
                onSetVerticalParams(parent, view, position, itemCount);
            }
        });

        view.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, position);
            }
        });
    }

    private void onSetVerticalParams(ViewGroup parent, View itemView, int position, int itemCount) {
        int itemNewWidth = mItemWidth;
        int itemNewHeight = mItemHeight;

        mItemConsumeY = itemNewHeight + (2 * mPageMargin);

        if (mOnItemSizeMeasuredListener != null) {
            mOnItemSizeMeasuredListener.onItemSizeMeasured(mItemConsumeY);
        }

        // 适配第0页和最后一页没有左页面和右页面，让他们保持左边距和右边距和其他项一样
        int topMargin = position == 0 ? (OsUtil.getScreenHeigth() - mItemWidth) / 2 : (mPageMargin);
        int bottomMargin = position == itemCount - 1 ? (OsUtil.getScreenHeigth() - mItemWidth) / 2 : (mPageMargin);

        setLayoutParams(itemView, 0, topMargin, 0, bottomMargin, itemNewWidth, itemNewHeight);
    }

    /**
     * 设置Item的宽高和间隔参数
     *
     * @param parent    ViewGroup
     * @param itemView  View
     * @param position  int
     * @param itemCount int
     */
    private void onSetHorizontalParams(ViewGroup parent, View itemView, int position, int itemCount) {
        int itemNewWidth = mItemWidth;
        int itemNewHeight = mItemHeight;

        mItemConsumeX = itemNewWidth + (2 * mPageMargin);

        if (mOnItemSizeMeasuredListener != null) {
            mOnItemSizeMeasuredListener.onItemSizeMeasured(mItemConsumeX);
        }

        // 适配第0页和最后一页没有左页面和右页面，让他们保持左边距和右边距和其他项一样
        int leftMargin = position == 0 ? (OsUtil.getScreenWidth() - mItemWidth) / 2 : (mPageMargin);
        int rightMargin = position == itemCount - 1 ? (OsUtil.getScreenWidth() - mItemWidth) / 2 : (mPageMargin);

        setLayoutParams(itemView, leftMargin, 0, rightMargin, 0, itemNewWidth, itemNewHeight);
    }

    /**
     * 设置参数
     *
     * @param itemView   View
     * @param left       int
     * @param top        int
     * @param right      int
     * @param bottom     int
     * @param itemWidth  int
     * @param itemHeight int
     */
    private void setLayoutParams(View itemView, int left, int top, int right, int bottom, int itemWidth, int itemHeight) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        boolean mMarginChange = false;
        boolean mWidthChange = false;
        boolean mHeightChange = false;

        if (lp.leftMargin != left || lp.topMargin != top || lp.rightMargin != right || lp.bottomMargin != bottom) {
            lp.setMargins(left, top, right, bottom);
            mMarginChange = true;
        }
        if (lp.width != itemWidth) {
            lp.width = itemWidth;
            mWidthChange = true;
        }
        if (lp.height != itemHeight) {
            lp.height = itemHeight;
            mHeightChange = true;

        }

        // 因为方法会不断调用，只有在真正变化了之后才调用
        if (mWidthChange || mMarginChange || mHeightChange) {
            itemView.setLayoutParams(lp);
        }
    }

    public void setOnItemClickListener(GalleryRecyclerView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSizeMeasuredListener(OnItemSizeMeasuredListener itemSizeMeasuredListener) {
        this.mOnItemSizeMeasuredListener = itemSizeMeasuredListener;
    }

    interface OnItemSizeMeasuredListener {
        /**
         * Item的大小测量完成
         *
         * @param size int
         */
        void onItemSizeMeasured(int size);
    }
}
