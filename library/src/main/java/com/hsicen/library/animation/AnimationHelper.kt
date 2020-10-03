package com.hsicen.library.animation

import android.animation.Animator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 作者：hsicen  2020/10/2 17:01
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：动画帮助类
 */
interface AnimationHelper {

  /** 选中动画
   * @param view view to scale (open)
   * @param delay start delay duration
   * @param animatorListener select view animation listener
   */
  fun openItem(view: View, delay: Int, animatorListener: Animator.AnimatorListener)


  /** 取消选中动画
   * @param view view to scale (close)
   * @param delay start delay
   * @param animatorListener deselect view animation listener
   */
  fun closeItem(view: View, delay: Int, animatorListener: Animator.AnimatorListener)

  /** View滑动时动画处理
   * @param views view data information for shift animation [ViewAnimationInfo]
   * @param delay start delay
   * @param layoutManager the layout manager
   * @param animatorListener animator listener to check start or end animation
   * @param animatorUpdateListener value animator listener to check updates
   */
  fun shiftSideViews(
    views: List<ViewAnimationInfo>,
    delay: Int,
    layoutManager: RecyclerView.LayoutManager,
    animatorListener: Animator.AnimatorListener,
    animatorUpdateListener: AnimatorUpdateListener
  )

  /**
   * @return Item选中或取消选中的缩放因子
   */
  val viewScaleFactor: Float

  /**
   * 旋转View的角度为0
   *
   * @param view view to rotate
   * @param listener animator listener to check start or end animation
   */
  fun straightenView(view: View, listener: Animator.AnimatorListener)

  /**
   * 旋转View到指定角度
   *
   * @param view view to rotate
   * @param angle rotate angle
   * @param listener animator listener to check start or end animation
   */
  fun rotateView(view: View, angle: Float, listener: Animator.AnimatorListener)
}