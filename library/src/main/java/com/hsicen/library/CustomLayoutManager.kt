package com.hsicen.library

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsicen.library.scroller.FanCardScroller
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
    private val mFanCardScroller = FanCardScroller(context)
    var isEnableFan = true //是否启用弧形布局

    var itemWidthPx = 0  //item的宽度
    var itemHeightPx = 0 //item的高度
    var itemMarginPx = 0 //item之间的间距
    var itemStartMarginPx = 0 //item刚开始的间距

    //当前选中Item回调
    private var onItemChange: ((position: Int) -> Unit)? = null

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
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        updateArcViewPositions()
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
            onItemChange?.invoke(position)
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?, position: Int
    ) {
        if (position >= itemCount) return
        mFanCardScroller.targetPosition = position
        startSmoothScroll(mFanCardScroller)
    }

    /*** 设置View旋转角度逻辑 */
    private fun updateArcViewPositions() {
        val halfWidth = width / 2f
        val radius = width * 2.0
        val powRadius = radius * radius
        val centerOffset = (screenWidth() - itemWidthPx) / 2

        val centerIndex = getCenterIndex()
        for (pos in 0 until childCount) {
            val itemView = getChildAt(pos) ?: return
            var rotation = 0.0
            val halfViewWidth = itemView.width / 2f

            //设置偏移间距
            itemView.pivotX = halfViewWidth
            itemView.pivotY = itemView.height.toFloat()

            //设置偏移角度
            if (isEnableFan) {
                var decoratedLeft = getDecoratedLeft(itemView)
                if (pos == 0 && centerIndex in 0..1) {
                    decoratedLeft += itemStartMarginPx
                }

                val deltaX = halfWidth - decoratedLeft - halfViewWidth
                val deltaY = (radius - sqrt(powRadius - deltaX * deltaX)).toFloat()

                itemView.translationY = deltaY
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

    /*** 当前选中Item回调*/
    fun onItemChange(onItemChange: ((position: Int) -> Unit)): CustomLayoutManager {
        this.onItemChange = onItemChange
        return this
    }

    /*** 设置Item的宽高参数和间距*/
    fun setItemInfo(widthPx: Int, heightPx: Int, marginPx: Int): CustomLayoutManager {
        itemWidthPx = widthPx
        itemHeightPx = heightPx
        itemMarginPx = marginPx
        return this
    }

    /*** 设置开始的间距*/
    fun setStartMargin(sizePx: Int): CustomLayoutManager {
        itemStartMarginPx = sizePx
        return this
    }

    /*** 是否使用弧形布局*/
    fun enableFan(isFan: Boolean): CustomLayoutManager {
        isEnableFan = isFan
        return this
    }

    /*** 返回当前SnapHelper*/
    fun getSnapHelper() = snapHelper

}