package com.hsicen.library.java.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 作者：黄思程  2020/11/5 14:59
 * 邮箱：huangsicheng@camera360.com
 * 功能：
 * 描述：自定义RecyclerView， 设置抛掷速度
 */
class CustomRecyclerView : RecyclerView {
    //设置滑动速度（像素/s）
    var flySpeed = 4500

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val newX = balanceVelocity(velocityX)
        val newY = balanceVelocity(velocityY)

        return super.fling(newX, newY)
    }

    /*** 返回滑动速度值*/
    private fun balanceVelocity(velocity: Int): Int {
        return if (velocity > 0) {
            velocity.coerceAtMost(flySpeed)
        } else {
            velocity.coerceAtLeast(-flySpeed)
        }
    }
}