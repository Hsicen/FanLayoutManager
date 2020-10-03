package com.hsicen.library.animation

import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * 作者：hsicen  2020/10/3 12:10
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：动画数据生成
 */
object ViewAnimationInfoGenerator {

  /** 生成动画数据
   * @param delta delta x (shift distance) for views
   * @param isSelected flag if have selected item
   * @param layoutManager the layout manager
   * @param centerViewPos the center view position
   * @param isCollapsed flag if have collapsed items
   * @return collection of view data
   */
  @JvmStatic
  fun generate(
    delta: Int, isSelected: Boolean, layoutManager: RecyclerView.LayoutManager,
    centerViewPos: Int, isCollapsed: Boolean
  ): List<ViewAnimationInfo> {
    val infoViews: MutableList<ViewAnimationInfo> = ArrayList()
    if (centerViewPos == RecyclerView.NO_POSITION) {
      return infoViews
    }

    for (i in 0 until layoutManager.childCount) {
      val childView = layoutManager.getChildAt(i) ?: continue
      val viewPosition = layoutManager.getPosition(childView)
      if (centerViewPos == viewPosition) continue

      val startLeft = layoutManager.getDecoratedLeft(childView)
      val startRight = layoutManager.getDecoratedRight(childView)
      val top = layoutManager.getDecoratedTop(childView)
      val bottom = layoutManager.getDecoratedBottom(childView)

      //show views with overlapping if have selected item
      val makeSelect = if (viewPosition < centerViewPos) {
        if (isSelected) -1 else 1
      } else {
        if (isSelected) 1 else -1
      }

      //make distance between each item if isCollapsed = true
      val makeCollapse = if (viewPosition < centerViewPos) {
        if (isCollapsed) centerViewPos - viewPosition else 1
      } else {
        if (isCollapsed) viewPosition - centerViewPos else 1
      }

      val finishLeft = startLeft + delta * makeSelect * makeCollapse
      val finishRight = startRight + delta * makeSelect * makeCollapse

      infoViews.add(ViewAnimationInfo(startLeft, finishLeft, startRight, finishRight, top, bottom, childView))
    }

    return infoViews
  }
}