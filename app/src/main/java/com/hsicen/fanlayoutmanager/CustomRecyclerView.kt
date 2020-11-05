package com.hsicen.fanlayoutmanager

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * 作者：黄思程  2020/11/5 14:59
 * 邮箱：huangsicheng@camera360.com
 * 功能：
 * 描述：自定义RecyclerView， 设置抛掷速度
 */
internal class CustomRecyclerView : RecyclerView {
    //设置抛掷因子
    var mScale = 0.2

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val newX = (velocityX * mScale).toInt()
        return super.fling(newX, velocityY)
    }

}