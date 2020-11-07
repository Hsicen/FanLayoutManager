package com.hsicen.library.widget

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.library.helper.GravitySnapHelper
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * 作者：hsicen  2020/11/7 15:29
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager
 */
class CustomLayoutManager @JvmOverloads constructor(
    val context: Context,
    orientation: Int = RecyclerView.HORIZONTAL,
    reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {
    private val snapHelper = GravitySnapHelper(Gravity.CENTER)
    var isEnableFan = true //是否启用弧形布局
    var itemWidthPx = 0  //item的宽度
    var itemHeightPx = 0 //item的高度

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        snapHelper.attachToRecyclerView(view)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        updateArcViewPositions()
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int, heightSpec: Int
    ) {
        updateArcViewPositions()
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    override fun canScrollHorizontally(): Boolean = true

    override fun scrollHorizontallyBy(
        dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?
    ): Int {
        val delta = super.scrollHorizontallyBy(dx, recycler, state)
        updateArcViewPositions()

        return delta
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        //当前选中的View
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            //获取当前View的位置
            val position = getCenterIndex()
            //Toast.makeText(context, "当前选中： $position", Toast.LENGTH_SHORT).show()
        }
    }

    /*** 设置View旋转角度逻辑 */
    private fun updateArcViewPositions() {
        val halfWidth = width / 2.toFloat()
        val radius = width * 2.toDouble()
        val powRadius = radius * radius

        val centerIndex = getCenterIndex()
        for (pos in 0 until childCount) {
            val itemView = getChildAt(pos) ?: return
            var rotation = 0.0
            val halfViewWidth = itemView.width / 2f

            //设置偏移间距
            itemView.pivotX = halfViewWidth
            itemView.pivotY = itemView.height.toFloat()
            val offset = (screenWidth() - itemWidthPx) / 2

            //设置偏移角度
            if (isEnableFan) {
                var decoratedLeft = getDecoratedLeft(itemView)
                if (0 == centerIndex && centerIndex == pos) {
                    decoratedLeft = offset
                }

                var deltaX = halfWidth - decoratedLeft - halfViewWidth
                var deltaY = (radius - sqrt(powRadius - deltaX * deltaX)).toFloat()
                if (centerIndex == pos) {
                    deltaX = 0f
                    deltaY = 0f
                }

                itemView.translationY = deltaY

                Log.d(
                    "hsc",
                    "" +
                            "位置:$pos  " +
                            "deltaX:$deltaX  " +
                            "deLeft:${getDecoratedLeft(itemView)}  "
                )
                rotation = (Math.toDegrees(asin((radius - deltaY) / radius)) - 90) * sign(deltaX)
            }

            //设置偏移
            itemView.rotation = rotation.toFloat()
        }
    }

    /*** 找到当前中心View*/
    private fun findCurrentCenterView(): View? {
        val centerX = width / 2f
        val viewHalfWidth: Float = itemWidthPx / 2f

        var nearestToCenterView: View? = null
        var nearestDeltaX = 0

        for (pos in 0 until childCount) {
            val childItem = getChildAt(pos) ?: continue
            val centerXView = (getDecoratedLeft(childItem) + viewHalfWidth).toInt()
            if (nearestToCenterView == null || abs(nearestDeltaX) > abs(centerX - centerXView)) {
                nearestToCenterView = childItem
                nearestDeltaX = (centerX - centerXView).toInt()
            }
        }

        return nearestToCenterView
    }

    /*** 找到当前中心View的位置*/
    private fun getCenterIndex(): Int {
        val centerView = findCurrentCenterView() ?: return 0
        return getPosition(centerView)
    }
}