package com.hsicen.library.animation

import android.view.View

/**
 * 作者：hsicen  2020/10/2 16:58
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：动画参数
 */

data class ViewAnimationInfo(
  val startLeft: Int, val finishLeft: Int,
  val startRight: Int, val finishRight: Int,
  val top: Int, val bottom: Int, val view: View
)
