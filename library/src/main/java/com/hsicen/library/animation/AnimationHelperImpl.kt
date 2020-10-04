package com.hsicen.library.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 作者：hsicen  2020/10/2 17:08
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：Item选中动画实现类
 */
class AnimationHelperImpl : AnimationHelper {

  //Item选中时的缩放因子
  override val viewScaleFactor = ANIMATION_VIEW_SCALE_FACTOR

  /*** Item选中时动效处理*/
  override fun openItem(view: View, delay: Int, animatorListener: Animator.AnimatorListener) {
    val valueAnimator = ValueAnimator.ofFloat(1f, viewScaleFactor + ANIMATION_VIEW_SCALE_FACTOR_THRESHOLD)
    valueAnimator.addUpdateListener {
      var value = it.animatedValue as Float
      if (value < 1f + ANIMATION_VIEW_SCALE_FACTOR_THRESHOLD / 2) {
        // make view less
        value = abs(value - 2f)
      } else {
        // make view bigger
        value -= ANIMATION_VIEW_SCALE_FACTOR_THRESHOLD
      }
      scaleView(view, value)
    }

    valueAnimator.startDelay = delay.toLong()
    valueAnimator.duration = ANIMATION_SINGLE_OPEN_DURATION.toLong()
    valueAnimator.addListener(animatorListener)
    valueAnimator.start()
  }

  /*** Item取消选中时动效处理*/
  override fun closeItem(view: View, delay: Int, animatorListener: Animator.AnimatorListener) {
    val valueAnimator = ValueAnimator.ofFloat(viewScaleFactor, 1f)
    valueAnimator.addUpdateListener {
      val value = it.animatedValue as Float
      scaleView(view, value)
    }

    valueAnimator.startDelay = delay.toLong()
    valueAnimator.duration = ANIMATION_SINGLE_CLOSE_DURATION.toLong()
    valueAnimator.addListener(animatorListener)
    valueAnimator.start()
  }

  /*** View滑动时动效实现*/
  override fun shiftSideViews(
    views: List<ViewAnimationInfo>,
    delay: Int,
    layoutManager: RecyclerView.LayoutManager,
    animatorListener: Animator.AnimatorListener,
    animatorUpdateListener: AnimatorUpdateListener
  ) {
    val bounceAnimator = ValueAnimator.ofFloat(0f, 1f)
    bounceAnimator.addUpdateListener {
      val value = it.animatedValue as Float
      for (item in views) {
        // left offset for view for current update value
        val left = (item.startLeft + value * (item.finishLeft - item.startLeft)).toInt()
        // right offset for view for current update value
        val right = (item.startRight + value * (item.finishRight - item.startRight)).toInt()

        // update view with new params
        layoutManager.layoutDecorated(item.view, left, item.top, right, item.bottom)
      }
      animatorUpdateListener.onAnimationUpdate(it)
    }

    bounceAnimator.duration = ANIMATION_SHIFT_VIEWS_DURATION.toLong()
    bounceAnimator.startDelay = delay + ANIMATION_SHIFT_VIEWS_DELAY_THRESHOLD.toLong()
    bounceAnimator.addListener(animatorListener)
    bounceAnimator.start()
  }

  override fun straightenView(view: View, listener: Animator.AnimatorListener) {
    val viewObjectAnimator = ObjectAnimator.ofFloat(view, "rotation", view.rotation, 0f)

    viewObjectAnimator.duration = 150
    viewObjectAnimator.interpolator = DecelerateInterpolator()
    viewObjectAnimator.addListener(listener)
    viewObjectAnimator.start()
  }

  override fun rotateView(view: View, angle: Float, listener: Animator.AnimatorListener) {
    val viewObjectAnimator = ObjectAnimator.ofFloat(view, "rotation", view.rotation, angle)

    viewObjectAnimator.duration = 150
    viewObjectAnimator.interpolator = DecelerateInterpolator()
    viewObjectAnimator.addListener(listener)
    viewObjectAnimator.start()
  }

  /*** 缩放View*/
  private fun scaleView(view: View, value: Float) {
    // change pivot point to the bottom middle
    view.pivotX = view.width / 2.toFloat()
    view.pivotY = view.height.toFloat()

    // scale view
    view.scaleX = value
    view.scaleY = value
  }

  companion object {
    // scale factor for view in open/close animations
    private const val ANIMATION_VIEW_SCALE_FACTOR = 1.5f

    // base duration for open animation
    private const val ANIMATION_SINGLE_OPEN_DURATION = 300

    // base duration for close animation
    private const val ANIMATION_SINGLE_CLOSE_DURATION = 300

    // base duration for shift animation
    private const val ANIMATION_SHIFT_VIEWS_DURATION = 200

    // base threshold duration for shift animation
    private const val ANIMATION_SHIFT_VIEWS_DELAY_THRESHOLD = 50

    // base threshold duration for open/close animation (bounce effect)
    private const val ANIMATION_VIEW_SCALE_FACTOR_THRESHOLD = 0.4f
  }
}