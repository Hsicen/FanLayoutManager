package com.hsicen.library.scroller

import android.content.Context
import android.graphics.PointF
import androidx.recyclerview.widget.LinearSmoothScroller
import com.hsicen.library.CustomLayoutManager

/**
 * 作者：hsicen  2020/11/7 20:32
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：滑动器
 */
abstract class BaseSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val layoutManager = layoutManager
        if (layoutManager is CustomLayoutManager) {
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