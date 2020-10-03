package com.hsicen.library.animation

import android.animation.Animator

/**
 * 作者：hsicen  2020/10/3 12:07
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：动画监听器
 */
open class SimpleAnimatorListener : Animator.AnimatorListener {
  override fun onAnimationStart(animator: Animator) {}
  override fun onAnimationEnd(animator: Animator) {}
  override fun onAnimationCancel(animator: Animator) {}
  override fun onAnimationRepeat(animator: Animator) {}
}