package com.hsicen.library.scroller

import android.content.Context
import android.util.DisplayMetrics
import android.view.View

/**
 * 作者：hsicen  2020/10/3 12:51
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：卡片滑动时，展示中心Item逻辑处理
 */
internal class ShiftToCenterCardScroller(context: Context) : BaseSmoothScroller(context) {

  override fun getHorizontalSnapPreference(): Int {
    return SNAP_TO_START
  }

  override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
    val layoutManager = layoutManager
    return if (layoutManager != null) {
      // add to calculated dx offset. Need to scroll to center of RecyclerView.
      super.calculateDxToMakeVisible(view, snapPreference) + layoutManager.width / 2 - view.width / 2
    } else {
      // no layoutManager detected - not expected case.
      super.calculateDxToMakeVisible(view, snapPreference)
    }
  }

  override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
  }

  companion object {
    private const val MILLISECONDS_PER_INCH = 400f
  }
}