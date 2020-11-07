package com.hsicen.library.scroller

import android.content.Context
import android.util.DisplayMetrics
import android.view.View

/**
 * 作者：hsicen  2020/11/7 20:33
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：滑动器
 */
class FanCardScroller(context: Context?) : BaseSmoothScroller(context) {

    override fun getHorizontalSnapPreference() = SNAP_TO_START

    override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
        val layoutManager = layoutManager
        return if (layoutManager != null) {
            super.calculateDxToMakeVisible(view, snapPreference) +
                    layoutManager.width / 2 - view.width / 2
        } else {
            super.calculateDxToMakeVisible(view, snapPreference)
        }
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }

    companion object {
        private const val MILLISECONDS_PER_INCH = 80f
    }
}