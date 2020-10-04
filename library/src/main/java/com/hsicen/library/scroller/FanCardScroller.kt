package com.hsicen.library.scroller

import android.content.Context
import android.util.DisplayMetrics
import android.view.View

/**
 * 作者：hsicen  2020/10/2 16:42
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：惯性滑动处理类
 */
internal class FanCardScroller(
  context: Context,
  private val cardTimeCallback: ((targetPosition: Int, time: Int) -> Unit)? = null
) : BaseSmoothScroller(context) {

  override fun getHorizontalSnapPreference(): Int = SNAP_TO_START

  override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
    val layoutManager = layoutManager
    return if (layoutManager != null) {
      super.calculateDxToMakeVisible(view, snapPreference) + layoutManager.width / 2 - view.width / 2
    } else {
      super.calculateDxToMakeVisible(view, snapPreference)
    }
  }

  override fun calculateTimeForScrolling(dx: Int): Int {
    val time = super.calculateTimeForScrolling(dx)
    cardTimeCallback?.invoke(targetPosition, time)

    return time
  }

  override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
  }

  companion object {
    //每英寸滑动需要的时间
    private const val MILLISECONDS_PER_INCH = 200f
  }
}