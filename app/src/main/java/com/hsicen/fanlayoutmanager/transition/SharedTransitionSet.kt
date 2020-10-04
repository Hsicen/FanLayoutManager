package com.hsicen.fanlayoutmanager.transition

import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet

/**
 * 作者：hsicen  2020/10/4 16:57
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：自定义转场动画
 */
class SharedTransitionSet : TransitionSet() {

  init {
    ordering = ORDERING_TOGETHER
    addTransition(ChangeBounds())
      .addTransition(ChangeTransform())
      .addTransition(ChangeImageTransform())
  }
}