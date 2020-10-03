package com.hsicen.library.listener

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ChildDrawingOrderCallback
import java.lang.ref.WeakReference

/**
 * 作者：hsicen  2020/10/2 16:50
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：控制子Item的绘制顺序
 */
class FanChildDrawingOrderCallback(layoutManager: RecyclerView.LayoutManager) : ChildDrawingOrderCallback {
  private val mLayoutManager = WeakReference(layoutManager)

  override fun onGetChildDrawingOrder(childCount: Int, i: Int): Int {

    val layoutManager = mLayoutManager.get()
    if (layoutManager != null) {
      val startView = layoutManager.getChildAt(0) ?: return i
      val position = layoutManager.getPosition(startView)
      val isStartFromBelow = position % 2 == 0

      return if (isStartFromBelow) {
        if (i % 2 == 0) {
          if (i == 0) 0 else i - 1
        } else {
          if (i + 1 >= childCount) i else i + 1
        }
      } else {
        if (i % 2 == 0) {
          if (i + 1 >= childCount) i else i + 1
        } else {
          i - 1
        }
      }
    }

    return i
  }
}
