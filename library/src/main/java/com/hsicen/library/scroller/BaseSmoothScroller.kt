package com.hsicen.library.scroller

import android.content.Context
import android.graphics.PointF
import androidx.recyclerview.widget.LinearSmoothScroller
import com.hsicen.library.FanLayoutManager

/**
 * 作者：hsicen  2020/10/2 16:28
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：布局滑动相关
 */
abstract class BaseSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

  override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {

    val layoutManager = layoutManager
    if (layoutManager is FanLayoutManager) {
      if (childCount == 0) {
        return null
      }

      val firstChildPos = layoutManager.getPosition(layoutManager.getChildAt(0)!!)
      val direction = if (targetPosition < firstChildPos) -1 else 1
      return PointF(direction.toFloat(), 0f)
    }

    return PointF()
  }
}