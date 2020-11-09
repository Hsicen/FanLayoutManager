package com.hsicen.library

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

/**
 * 作者：hsicen  2020/11/7 14:22
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：设置Item的起始Padding，结束Padding和Item之间的Margin
 */
class LinearEdgeDecoration(
    @Px private val startPadding: Int,
    @Px private val endPadding: Int = startPadding,
    @Px private val itemMargin: Int = 0,
    private val orientation: Int = RecyclerView.VERTICAL,
    private val inverted: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutManager: RecyclerView.LayoutManager = parent.layoutManager ?: return
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        val position = layoutParams.viewAdapterPosition
        val itemCount = layoutManager.itemCount

        if (position == RecyclerView.NO_POSITION || itemCount == 0) return

        if (orientation == RecyclerView.HORIZONTAL) {
            if (position == 0) {
                if (!inverted) {
                    outRect.left = startPadding
                    outRect.right = itemMargin / 2
                } else {
                    outRect.right = startPadding
                    outRect.left = itemMargin / 2
                }
            } else if (position == itemCount - 1) {
                if (!inverted) {
                    outRect.right = endPadding
                    outRect.left = itemMargin / 2
                } else {
                    outRect.left = endPadding
                    outRect.right = itemMargin / 2
                }
            } else {
                outRect.left = itemMargin / 2
                outRect.right = itemMargin / 2
            }
        } else {
            if (position == 0) {
                if (!inverted) {
                    outRect.top = startPadding
                    outRect.bottom = itemMargin / 2
                } else {
                    outRect.bottom = startPadding
                    outRect.top = itemMargin / 2
                }
            } else if (position == itemCount - 1) {
                if (!inverted) {
                    outRect.bottom = endPadding
                    outRect.top = itemMargin / 2
                } else {
                    outRect.top = endPadding
                    outRect.bottom = itemMargin / 2
                }
            } else {
                outRect.top = itemMargin / 2
                outRect.bottom = itemMargin / 2
            }
        }
    }
}