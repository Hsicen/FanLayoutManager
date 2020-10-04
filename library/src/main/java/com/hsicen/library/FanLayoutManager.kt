@file:Suppress("unused", "UNUSED_PARAMETER")

package com.hsicen.library

import android.animation.Animator
import android.content.Context
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.hsicen.library.FanLayoutManagerSettings.Companion.newBuilder
import com.hsicen.library.animation.AnimationHelper
import com.hsicen.library.animation.AnimationHelperImpl
import com.hsicen.library.animation.SimpleAnimatorListener
import com.hsicen.library.animation.ViewAnimationInfoGenerator.generate
import com.hsicen.library.scroller.FanCardScroller
import com.hsicen.library.scroller.ShiftToCenterCardScroller
import java.util.*
import kotlin.math.*

/**
 * 作者：hsicen  2020/10/3 14:17
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：自定义LayoutManager，实现弧形布局
 * FanLayoutManager change view's position, rotation and translation to create effect fan scrolling
 */
class FanLayoutManager @JvmOverloads constructor(
  context: Context, settings: FanLayoutManagerSettings? = null
) : RecyclerView.LayoutManager() {

  /*** 设置默认配置*/
  private val mSettings: FanLayoutManagerSettings = settings
    ?: newBuilder(context)
      .withFanRadius(false)
      .withAngleItemBounce(0f)
      .build()

  private var mAnimationHelper: AnimationHelper = AnimationHelperImpl()
  private val mShiftToCenterCardScroller = ShiftToCenterCardScroller(context)
  private val mFanCardScroller = FanCardScroller(context) { position, time ->
    selectItem(position, time) //滑动结束后选中Item
  }

  private val mRandom = Random() //用来产生默认偏移角度
  private val mViewCache = SparseArray<View>() //View缓存
  private var mViewRotationsMap: SparseArray<Float> = SparseArray() //缓存View偏移角度

  var selectedItemPosition = RecyclerView.NO_POSITION //当前选中Item位置
  private var mScrollToPosition = RecyclerView.NO_POSITION //将要滑动到的位置

  private var mIsSelectAnimationInProcess = false   //是否在滑动过程中执行选中动画
  private var mIsDeselectAnimationInProcess = false //是否在滑动过程中执行取消选中动画

  private var mIsWaitingToSelectAnimation = false   //是否等待选中动画完成
  private var mIsWaitingToDeselectAnimation = false //是否等待取消选中动画完成

  var isSelectedItemStraightened = false //标记选中Item没有角度偏移
  private var mIsSelectedItemStraightenedInProcess = false //标记选中Item在滑动过程中没有角度偏移

  private var mIsViewCollapsing = false //标记Item是否展开
  private var mIsCollapsed = false //标记Item是否折叠
  private var mIsSelected = false  //标记是否选中

  private var mPendingSavedState: SavedState? = null
  private var mCenterView: View? = null
  private var mSelectedListener: ((position: Int, selectedView: View?) -> Unit)? = null

  val isItemSelected: Boolean //当前是否有Item选中
    get() = selectedItemPosition != RecyclerView.NO_POSITION

  fun addOnItemSelectedListener(listener: (position: Int, selectedView: View?) -> Unit) {
    mSelectedListener = listener
  }

  /*** 设置自定义的动画实现逻辑*/
  fun setAnimationHelper(animationHelper: AnimationHelper?) {
    mAnimationHelper = animationHelper ?: AnimationHelperImpl()
  }

  /*** 状态保存*/
  private fun saveState() {
    mPendingSavedState = SavedState()
    mPendingSavedState?.let {
      it.mCenterItemPosition = findCurrentCenterViewPos() // save center view position
      it.isSelected = mIsSelected   // save selected state for center view
      it.isCollapsed = mIsCollapsed // save collapsed state for views
      it.mRotation = mViewRotationsMap // center view position
    }
  }

  override fun onSaveInstanceState(): Parcelable? {
    saveState()
    return mPendingSavedState
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is SavedState) {
      mPendingSavedState = state
      mPendingSavedState?.let {
        mScrollToPosition = it.mCenterItemPosition  // center view position
        selectedItemPosition = if (it.isSelected) mScrollToPosition else RecyclerView.NO_POSITION // position for selected item
        mIsSelected = it.isSelected  // selected
        mIsCollapsed = it.isCollapsed // collapsed
        mViewRotationsMap = it.mRotation ?: SparseArray()  // rotation
      }
    }
  }

  override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
    return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
  }

  override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
    // 在分离或回收所有视图之前找到中心视图
    mCenterView = findCurrentCenterView()
    if (itemCount == 0) {
      detachAndScrapAttachedViews(recycler)
      return
    }
    detachAndScrapAttachedViews(recycler)
    fill(recycler, true)
  }

  /*** 处理View的创建和复用*/
  private fun fill(recycler: Recycler, notify: Boolean) {
    //缓存View
    mViewCache.clear()
    for (index in 0 until childCount) {
      val childView = getChildAt(index) ?: continue
      val childPosition = getPosition(childView)
      mViewCache.put(childPosition, childView)
    }

    //暂时Detach View
    for (i in 0 until mViewCache.size()) {
      detachView(mViewCache.valueAt(i))
    }

    //中心视图的位置(默认第一个Item)
    val centerViewPosition = mCenterView?.let { getPosition(it) } ?: 0
    //中心视图的左←偏移位置
    val centerViewOffset = mCenterView?.let { getDecoratedLeft(it) } ?: (width / 2f - mSettings.viewWidthPx / 2f).toInt()

    //主要的布局逻辑
    if (mScrollToPosition != RecyclerView.NO_POSITION) {
      //第一个Item不在中心位置
      fillRightFromCenter(mScrollToPosition, centerViewOffset, recycler)
    } else {
      //第一个Item(0)在中心位置
      fillRightFromCenter(centerViewPosition, centerViewOffset, recycler)
    }

    //回收所有视图后更新中心视图
    if (childCount != 0) {
      mCenterView = findCurrentCenterView()
      /*if (notify) { //依次回调滑动过程中的每个Item
        mSelectedListener?.onItemSelected(findCurrentCenterViewPos(), mCenterView);
      }*/
    }

    //回收缓存View到缓存池
    for (i in 0 until mViewCache.size()) {
      recycler.recycleView(mViewCache.valueAt(i))
    }

    // 更新View的偏移位置，实现弧形布局效果
    updateArcViewPositions()
  }

  /***
   * ItemView的测量
   * Measure view with margins and specs
   * @param child view to measure
   * @param ws spec for width
   * @param hs spec for height */
  private fun measureChildWithDecorationsAndMargin(child: View, ws: Int, hs: Int) {
    var widthSpec = ws
    var heightSpec = hs
    val decorRect = Rect()
    calculateItemDecorationsForChild(child, decorRect)

    val lp = child.layoutParams as RecyclerView.LayoutParams
    widthSpec = updateSpecWithExtra(
      widthSpec, lp.leftMargin + decorRect.left,
      lp.rightMargin + decorRect.right
    )
    heightSpec = updateSpecWithExtra(
      heightSpec, lp.topMargin + decorRect.top,
      lp.bottomMargin + decorRect.bottom
    )

    child.measure(widthSpec, heightSpec)
  }

  /***
   * 计算满足要求的 MeasureSpec值
   * @param spec 给定的MeasureSpec
   * @param startInset 起始间距
   * @param endInset 结束间距
   * @return 实际的MeasureSpec */
  private fun updateSpecWithExtra(spec: Int, startInset: Int, endInset: Int): Int {
    if (startInset == 0 && endInset == 0) {
      return spec
    }

    val mode = View.MeasureSpec.getMode(spec)
    return if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
      View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(spec) - startInset - endInset, mode)
    } else spec
  }

  /*** 是否支持水平滑动*/
  override fun canScrollHorizontally(): Boolean = true

  /*** 设定不支持垂直滑动*/
  override fun canScrollVertically(): Boolean = false

  /*** 水平滑动逻辑处理*/
  override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
    // after fillRightFromCenter(...) we don't need those params.
    mScrollToPosition = RecyclerView.NO_POSITION
    mPendingSavedState = null

    if (dx == RecyclerView.NO_POSITION) {
      val delta = scrollHorizontallyInternal(dx)
      offsetChildrenHorizontal(-delta)
      fill(recycler, false)
      return delta
    }

    // if item selected and any animation isn't in progress
    if (selectedItemPosition != RecyclerView.NO_POSITION && !mIsSelectAnimationInProcess
      && !mIsDeselectAnimationInProcess && !mIsWaitingToDeselectAnimation && !mIsWaitingToSelectAnimation
    ) {
      deselectItem(selectedItemPosition)
    }

    // if animation in progress block scroll
    if (mIsDeselectAnimationInProcess || mIsSelectAnimationInProcess || mIsViewCollapsing) {
      return 0
    }

    val delta = scrollHorizontallyInternal(dx)
    offsetChildrenHorizontal(-delta)
    fill(recycler, true)
    return delta
  }

  /***
   * 水平滑动距离计算
   * @param dx fling (user scroll gesture) delta x
   * @return delta x for views */
  private fun scrollHorizontallyInternal(dx: Int): Int {
    // check child count
    if (childCount == 0) {
      return 0
    }

    // items count in the adapter
    val itemCount = itemCount
    var leftView = getChildAt(0) ?: return 0
    var rightView = getChildAt(childCount - 1) ?: return 0

    // search left and right views.
    for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      if (getDecoratedLeft(leftView) > getDecoratedLeft(childView)) {
        leftView = childView
      }
      if (getDecoratedRight(rightView) < getDecoratedRight(childView)) {
        rightView = childView
      }
    }

    // area with filling views. need to find borders
    val viewSpan = if (getDecoratedRight(rightView) > width) {
      getDecoratedRight(rightView)
    } else {
      width - min(getDecoratedLeft(leftView), 0)
    }

    // check left and right borders
    if (viewSpan < width) {
      return 0
    }

    var delta = 0
    if (dx < 0) { // move views left
      // position for left item in the adapter
      val firstViewAdapterPos = getPosition(leftView)
      delta = if (firstViewAdapterPos > 0) { // if item isn't first in the adapter
        dx
      } else { // if item first in the adapter
        // stop scrolling if item in the middle.
        val viewLeft = getDecoratedLeft(leftView) - width / 2 + getDecoratedMeasuredWidth(leftView) / 2
        max(viewLeft, dx)
      }
    } else if (dx > 0) {  // move views right
      // position for right item in the adapter
      val lastViewAdapterPos = getPosition(rightView)
      delta = if (lastViewAdapterPos < itemCount - 1) { // if item isn't last in the adapter
        dx
      } else { // if item last in the adapter
        // stop scrolling if item in the middle.
        val viewRight = getDecoratedRight(rightView) + width / 2 - getDecoratedMeasuredWidth(rightView) / 2
        val parentRight = width
        min(viewRight - parentRight, dx)
      }
    }

    return delta
  }

  /*** 当前RecyclerView的宽高测量逻辑*/
  override fun onMeasure(recycler: Recycler, state: RecyclerView.State, widthSpec: Int, hs: Int) {
    var heightSpec = hs
    val heightMode = View.MeasureSpec.getMode(heightSpec)
    val scaledHeight = mSettings.viewHeightPx * mAnimationHelper.viewScaleFactor
    val scaledWidth = mSettings.viewWidthPx * mAnimationHelper.viewScaleFactor
    val height = if (heightMode == View.MeasureSpec.EXACTLY) View.MeasureSpec.getSize(heightSpec) else
      sqrt(scaledHeight * scaledHeight + scaledWidth * scaledWidth).toInt()
    heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

    updateArcViewPositions()
    super.onMeasure(recycler, state, widthSpec, heightSpec)
  }

  /***
   * 更新当前ItemView的pivot，rotation，translation，实现Fan效果
   * Change rotation to create bounce effect. */
  private fun updateArcViewPositions() {
    val halfWidth = width / 2f
    val radius = width * 2.0  // minimal radius is recyclerView width * 2
    val powRadius = radius * radius

    for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      var rotation = 0.0

      // need to show views in "fan" style
      val halfViewWidth = childView.width / 2f

      // change pivot point to center bottom of the view
      childView.pivotX = halfViewWidth
      childView.pivotY = childView.height * 1f

      if (mSettings.isFanRadiusEnable) { //弧形布局参数计算
        // distance between center of screen to center of view in x-axis
        val deltaX = halfWidth - getDecoratedLeft(childView) - halfViewWidth

        // distance in which need to move view in y-axis. Low accuracy
        val deltaY = radius - sqrt(powRadius - deltaX * deltaX)
        childView.translationY = deltaY.toFloat()

        // calculate view rotation
        rotation = (Math.toDegrees(asin((radius - deltaY) / radius)) - 90) * sign(deltaX)
      }

      //设置角度偏移
      val viewPosition = getPosition(childView)
      val baseViewRotation = mViewRotationsMap.get(viewPosition) ?: generateBaseViewRotation(viewPosition)
      childView.rotation = (rotation.toFloat()
          + if (selectedItemPosition == viewPosition && isSelectedItemStraightened) 0f else baseViewRotation)
    }
  }

  /*** 随机产生基础偏移角度*/
  private fun generateBaseViewRotation(position: Int): Float {
    val randomRotation = mRandom.nextFloat() * mSettings.angleItemBounce * 2 - mSettings.angleItemBounce
    mViewRotationsMap.put(position, randomRotation)

    return randomRotation
  }

  /*** 使用中心视图位置进行绘制.
   * @param centerViewPosition 中心视图（锚点）的位置。该视图将居中
   * @param recycler Recycler from the recyclerView */
  private fun fillRightFromCenter(centerViewPosition: Int, centerViewOffset: Int, recycler: Recycler) {
    // 左边界
    val leftBorder = -(mSettings.viewWidthPx + if (mIsCollapsed) mSettings.viewWidthPx else 0)
    // 右边界
    val rightBorder = width + (mSettings.viewWidthPx + if (mIsCollapsed) mSettings.viewWidthPx else 0)
    var leftViewOffset = centerViewOffset
    var leftViewPosition = centerViewPosition

    // 计算 Margin Top的值，调整(mSettings.viewWidthPx / 4)可以控制Item距离底部的间隔
    val baseTopMargin = max(0, height - mSettings.viewHeightPx - mSettings.viewWidthPx / 4)
    //设置Item之间的间隔
    val overlapDistance = if (mIsCollapsed) {
      -mSettings.viewWidthPx / 4 //展开效果
    } else mSettings.viewWidthPx / 4 //重叠效果

    // 计算Item的宽高尺寸(具体值)
    var fillRight = true
    val widthSpec = View.MeasureSpec.makeMeasureSpec(mSettings.viewWidthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(mSettings.viewHeightPx, View.MeasureSpec.EXACTLY)

    // 判断是否需要恢复数据
    val hasPendingStateSelectedItem = mPendingSavedState?.let {
      it.isSelected && it.mCenterItemPosition != RecyclerView.NO_POSITION
    } ?: false

    //弧形偏移量
    val deltaOffset = mSettings.viewWidthPx / 2.toFloat()

    // 找到左边的第一个显示View的位置
    while (leftViewOffset > leftBorder && leftViewPosition >= 0) {
      leftViewOffset -= if (mIsCollapsed) {
        mSettings.viewWidthPx + abs(overlapDistance) // offset for collapsed views
      } else {
        mSettings.viewWidthPx - abs(overlapDistance) // offset for NOT collapsed views
      }

      leftViewPosition--
    }

    if (leftViewPosition < 0) {
      leftViewOffset += if (mIsCollapsed) {
        (mSettings.viewWidthPx + abs(overlapDistance)) * abs(leftViewPosition) // offset for collapsed views
      } else {
        (mSettings.viewWidthPx - abs(overlapDistance)) * abs(leftViewPosition) // offset for NOT collapsed views
      }

      leftViewPosition = 0
    }

    // offset for left views if we restore state and have selected item
    if (hasPendingStateSelectedItem && leftViewPosition != mPendingSavedState?.mCenterItemPosition) {
      leftViewOffset += -deltaOffset.toInt()
    }

    var selectedView: View? = null
    while (fillRight && leftViewPosition < itemCount) {
      // offset for current view if we restore state and have selected item
      if (hasPendingStateSelectedItem && leftViewPosition == mPendingSavedState?.mCenterItemPosition && leftViewPosition != 0) {
        leftViewOffset += deltaOffset.toInt()
      }

      // get view from local cache
      var view = mViewCache[leftViewPosition]
      if (view == null) {
        // get view from recycler
        view = recycler.getViewForPosition(leftViewPosition)
        // optimization for view rotation
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        // add view to the recyclerView
        addView(view)
        // 测量子View
        measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec)
        // set offsets, width and height in the recyclerView
        layoutDecorated(
          view, leftViewOffset, baseTopMargin,
          leftViewOffset + mSettings.viewWidthPx, baseTopMargin + mSettings.viewHeightPx
        )
      } else {
        attachView(view, leftViewPosition)
        mViewCache.remove(leftViewPosition)
      }

      view.scaleX = 1f
      view.scaleY = 1f
      if (mIsSelected && centerViewPosition == leftViewPosition) {
        selectedView = view
      }

      // calculate position for next view. last position + view height - overlap between views.
      leftViewOffset = leftViewOffset + mSettings.viewWidthPx - overlapDistance

      // check right border. stop loop if next view is > then right border.
      fillRight = leftViewOffset < rightBorder

      // offset for right views if we restore state and have selected item
      if (hasPendingStateSelectedItem && leftViewPosition == mPendingSavedState?.mCenterItemPosition) {
        leftViewOffset += deltaOffset.toInt()
      }

      leftViewPosition++
    }

    // if we have to restore state with selected item
    // this part need to scale center selected view
    if (hasPendingStateSelectedItem && selectedView != null) {
      //设置选中View为放大状态
      selectedView.scaleX = mAnimationHelper.viewScaleFactor
      selectedView.scaleY = mAnimationHelper.viewScaleFactor
    }
  }

  /** ItemView选中逻辑实现
   * @param recyclerView current recycler view. Need to smooth scroll.
   * @param selectedViewPosition item view position */
  fun switchItem(recyclerView: RecyclerView?, selectedViewPosition: Int) {
    // block event if any animation in progress
    if (mIsDeselectAnimationInProcess || mIsSelectAnimationInProcess || mIsViewCollapsing
      || mIsWaitingToDeselectAnimation || mIsWaitingToSelectAnimation || mIsSelectedItemStraightenedInProcess
    ) {
      return
    }

    // if item selected
    recyclerView ?: return
    if (selectedItemPosition != RecyclerView.NO_POSITION
      && selectedItemPosition != selectedViewPosition
    ) {
      deselectItem(recyclerView, selectedItemPosition, selectedViewPosition)
      return
    }

    // if item not selected need to smooth scroll and then select item
    smoothScrollToPosition(recyclerView, null, selectedViewPosition)
  }

  /***
   * ItemView选中逻辑实现
   * @param position 选中位置
   * @param delay 动画延时时间 */
  private fun selectItem(position: Int, delay: Int) {
    // if select already selected item
    if (selectedItemPosition == position) {
      deselectItem(selectedItemPosition)
      return
    }

    // search view by position
    var viewToSelect: View? = null
    loop@ for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      if (position == getPosition(childView)) {
        viewToSelect = childView
        break@loop
      }
    }

    viewToSelect ?: return
    // save position of view which will be selected
    selectedItemPosition = position
    // save selected stay... no way back...
    mIsSelected = true
    // open item animation wait for start but not in process.
    // select item animation prepare and wait until smooth scroll is finished
    mIsWaitingToSelectAnimation = true
    mAnimationHelper.openItem(viewToSelect, delay * 3, object : SimpleAnimatorListener() {
      override fun onAnimationStart(animator: Animator) {
        super.onAnimationStart(animator)
        // change state of select animation progress
        mIsSelectAnimationInProcess = true
        mIsWaitingToSelectAnimation = false

        // shift distance between center view and left, right views.
        val delta = mSettings.viewWidthPx / 2
        // generate data for animation helper. (calculate final positions for all views)
        val infoViews = generate(
          delta, true, this@FanLayoutManager, selectedItemPosition, false
        )

        // animate shifting left and right views
        mAnimationHelper.shiftSideViews(
          infoViews, 0, this@FanLayoutManager,
          animatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
          }
        ) {
          // update rotation and translation for all views
          updateArcViewPositions()
        }
      }

      override fun onAnimationEnd(animator: Animator) {
        mIsSelectAnimationInProcess = false
      }

      override fun onAnimationCancel(animator: Animator) {
        mIsSelectAnimationInProcess = false
      }
    })
  }

  /*** 取消选中效果(放大)*/
  fun deselectItem() {
    deselectItem(selectedItemPosition)
  }

  /*** 取消选中效果， 并设置默认参数*/
  private fun deselectItem(position: Int) {
    deselectItem(null, position, RecyclerView.NO_POSITION)
  }

  /** 取消选中效果逻辑实现
   * @param recyclerView RecyclerView for this LayoutManager
   * @param position position item for deselect
   * @param scrollToPosition position to scroll after deselect
   * @param delay waiting duration before start deselect */
  private fun deselectItem(recyclerView: RecyclerView?, position: Int, scrollToPosition: Int, delay: Int = 0) {
    if (position == RecyclerView.NO_POSITION) {
      // if position is default non selected value
      return
    }

    if (isSelectedItemStraightened) {
      restoreBaseRotationSelectedItem(object : SimpleAnimatorListener() {
        override fun onAnimationEnd(animator: Animator) {
          closeItem(recyclerView, position, scrollToPosition, delay)
        }
      })
    } else {
      closeItem(recyclerView, position, scrollToPosition, delay)
    }
  }

  /** 取消选中效果逻辑实现
   * @param recyclerView RecyclerView for this LayoutManager
   * @param position position item for deselect
   * @param scrollToPosition position to scroll after deselect
   * @param delay waiting duration before start deselect */
  private fun closeItem(recyclerView: RecyclerView?, position: Int, scrollToPosition: Int, delay: Int) {
    // wait for start deselect animation
    mIsWaitingToDeselectAnimation = true

    // search view by position
    var viewToDeselect: View? = null
    outLoop@ for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      if (position == getPosition(childView)) {
        viewToDeselect = childView
        break@outLoop
      }
    }

    // remove selected state... no way back...
    selectedItemPosition = RecyclerView.NO_POSITION
    mIsSelected = false
    viewToDeselect ?: return

    // close item animation
    mAnimationHelper.closeItem(viewToDeselect, delay, object : SimpleAnimatorListener() {
      override fun onAnimationStart(animator: Animator) {
        // change states
        mIsDeselectAnimationInProcess = true
        mIsWaitingToDeselectAnimation = false

        //设置左右View与中心View的间隔
        val delta = mSettings.viewWidthPx / 2

        // generate data for animation helper. (calculate final positions for all views)
        val infoViews = generate(delta, false, this@FanLayoutManager, position, false)

        // animate shifting left and right views
        mAnimationHelper.shiftSideViews(
          infoViews,
          0,
          this@FanLayoutManager,
          animatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
          }) {
          // update rotation and translation for all views
          updateArcViewPositions()
        }
      }

      override fun onAnimationEnd(animator: Animator) {
        mIsDeselectAnimationInProcess = false
        if (recyclerView != null && scrollToPosition != RecyclerView.NO_POSITION) {
          // scroll to new position after deselect animation end
          smoothScrollToPosition(recyclerView, null, scrollToPosition)
        }
      }

      override fun onAnimationCancel(animator: Animator) {
        mIsDeselectAnimationInProcess = false
        if (recyclerView != null && scrollToPosition != RecyclerView.NO_POSITION) {
          // scroll to new position after deselect animation cancel
          smoothScrollToPosition(recyclerView, null, scrollToPosition)
        }
      }
    })
  }

  override fun onScrollStateChanged(state: Int) {
    super.onScrollStateChanged(state)
    // when user stop scrolling
    if (state == RecyclerView.SCROLL_STATE_IDLE) {
      // show view in the middle of screen
      scrollToCenter()
    }
  }

  /*** 滑动到中心View位置*/
  private fun scrollToCenter() {
    val nearestToCenterView = findCurrentCenterView()
    if (nearestToCenterView != null) {
      mShiftToCenterCardScroller.targetPosition = getPosition(nearestToCenterView)
      startSmoothScroll(mShiftToCenterCardScroller)
      mSelectedListener?.invoke(getPosition(nearestToCenterView), nearestToCenterView)
    }
  }

  override fun scrollToPosition(position: Int) {
    mScrollToPosition = position
    requestLayout()
  }

  override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
    if (position >= itemCount) {
      // if position is not in range
      return
    }

    // smooth scroll to position
    mFanCardScroller.targetPosition = position
    startSmoothScroll(mFanCardScroller)
  }

  /***
   * 取消ItemView的角度偏移效果
   * @param listener straighten function listener */
  fun straightenSelectedItem(listener: Animator.AnimatorListener?) {
    // check all animations, block if any animation in progress
    if (selectedItemPosition == RecyclerView.NO_POSITION || mIsSelectAnimationInProcess
      || mIsDeselectAnimationInProcess || mIsSelectedItemStraightenedInProcess
      || mIsWaitingToDeselectAnimation || mIsWaitingToSelectAnimation || mIsViewCollapsing
      || isSelectedItemStraightened
    ) return

    //find target view to rotate to 0
    var viewToRotate: View? = null
    loop@ for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      if (selectedItemPosition == getPosition(childView)) {
        viewToRotate = childView
        break@loop
      }
    }

    // start straight animation
    viewToRotate ?: return
    mAnimationHelper.straightenView(viewToRotate, object : Animator.AnimatorListener {
      override fun onAnimationStart(animation: Animator) {
        listener?.onAnimationStart(animation)
      }

      override fun onAnimationEnd(animation: Animator) {
        listener?.onAnimationEnd(animation)
        isSelectedItemStraightened = true
        mIsSelectedItemStraightenedInProcess = false
      }

      override fun onAnimationCancel(animation: Animator) {
        listener?.onAnimationCancel(animation)
        isSelectedItemStraightened = true
        mIsSelectedItemStraightenedInProcess = false
      }

      override fun onAnimationRepeat(animation: Animator) {
        listener?.onAnimationRepeat(animation)
      }
    })

    // save state
    mIsSelectedItemStraightenedInProcess = true
  }

  /***
   * 恢复选中Item之前的偏移角度
   * @param listener 动画监听 */
  private fun restoreBaseRotationSelectedItem(listener: Animator.AnimatorListener?) {
    // block if any animation in progress
    if (selectedItemPosition == RecyclerView.NO_POSITION || mIsSelectAnimationInProcess
      || mIsDeselectAnimationInProcess || mIsSelectedItemStraightenedInProcess
      || mIsWaitingToDeselectAnimation || mIsWaitingToSelectAnimation
      || mIsViewCollapsing || !isSelectedItemStraightened
    ) return

    // search selected view
    var viewToRotate: View? = null
    loop@ for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      if (selectedItemPosition == getPosition(childView)) {
        viewToRotate = childView
        break@loop
      }
    }

    viewToRotate ?: return
    val baseViewRotation = mViewRotationsMap.get(selectedItemPosition)
      ?: generateBaseViewRotation(selectedItemPosition)
    mIsSelectedItemStraightenedInProcess = true  // save state

    // start straight animation
    mAnimationHelper.rotateView(viewToRotate, baseViewRotation, object : Animator.AnimatorListener {
      override fun onAnimationStart(animation: Animator) {
        listener?.onAnimationStart(animation)
      }

      override fun onAnimationEnd(animation: Animator) {
        listener?.onAnimationEnd(animation)
        isSelectedItemStraightened = false
        mIsSelectedItemStraightenedInProcess = false
      }

      override fun onAnimationCancel(animation: Animator) {
        listener?.onAnimationCancel(animation)
        isSelectedItemStraightened = false
        mIsSelectedItemStraightenedInProcess = false
      }

      override fun onAnimationRepeat(animation: Animator) {
        listener?.onAnimationRepeat(animation)
      }
    })
  }


  /***
   * 展开或折叠ItemView：
   * 1) Lock screen (Stop scrolling)
   * 2) Collapse all cards
   * 3) Unlock screen
   * 4) Scroll to center nearest card if not selected */
  fun collapseViews() {
    // check all animations
    if (mIsSelectAnimationInProcess || mIsWaitingToSelectAnimation ||
      mIsDeselectAnimationInProcess || mIsWaitingToDeselectAnimation ||
      mIsSelectedItemStraightenedInProcess || mIsViewCollapsing
    ) return

    // 1) lock screen
    mIsViewCollapsing = true

    // 2) Collapse all cards
    // collapse distance
    val delta = mSettings.viewWidthPx / 2
    // generate data for collapse animation
    val infoViews = generate(
      delta, !mIsCollapsed.also { mIsCollapsed = it },
      this@FanLayoutManager,
      findCurrentCenterViewPos(), true
    )

    // collapse views
    mAnimationHelper.shiftSideViews(infoViews, 0, this@FanLayoutManager,
      object : SimpleAnimatorListener() {
        override fun onAnimationEnd(animator: Animator) {
          // 3) Unlock screen
          mIsViewCollapsing = !mIsViewCollapsing
          // 4) Scroll to center nearest card
          scrollToCenter()
        }
      }) {
      // update rotation and translation for all views
      updateArcViewPositions()
    }
  }

  /*** 找到当前中心位置的ItemView*/
  private fun findCurrentCenterView(): View? {
    val centerX = width / 2f
    val viewHalfWidth = mSettings.viewWidthPx / 2f
    var nearestToCenterView: View? = null
    var nearestDeltaX = 0f

    for (i in 0 until childCount) {
      val childView = getChildAt(i) ?: continue
      val centerXView = getDecoratedLeft(childView) + viewHalfWidth

      if (nearestToCenterView == null || abs(nearestDeltaX) > abs(centerX - centerXView)) {
        nearestToCenterView = childView
        nearestDeltaX = centerX - centerXView
      }
    }

    return nearestToCenterView
  }

  /*** 找到当前中心View的Position*/
  private fun findCurrentCenterViewPos(): Int {
    val view = mCenterView
    return view?.let { getPosition(it) } ?: RecyclerView.NO_POSITION
  }

  override fun onItemsChanged(recyclerView: RecyclerView) {
    super.onItemsChanged(recyclerView)
    recyclerView.stopScroll()
    saveState()
    if (itemCount <= selectedItemPosition) {
      selectedItemPosition = RecyclerView.NO_POSITION
      // save selected state for center view
      mPendingSavedState!!.isSelected = false
      mIsSelected = false
    }
  }

  override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
    super.onItemsAdded(recyclerView, positionStart, itemCount)
    recyclerView.stopScroll()
    saveState()
  }

  override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
    super.onItemsUpdated(recyclerView, positionStart, itemCount)
    recyclerView.stopScroll()
    saveState()
  }

  override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int, payload: Any?) {
    super.onItemsUpdated(recyclerView, positionStart, itemCount, payload)
    recyclerView.stopScroll()
    saveState()
  }

  override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
    super.onItemsRemoved(recyclerView, positionStart, itemCount)
    recyclerView.stopScroll()
    saveState()

    if (selectedItemPosition >= positionStart && selectedItemPosition < positionStart + itemCount) {
      selectedItemPosition = RecyclerView.NO_POSITION
      // save selected state for center view
      mPendingSavedState?.isSelected = false
    }
  }

  data class SavedState(
    var mCenterItemPosition: Int = RecyclerView.NO_POSITION,
    var isCollapsed: Boolean = false,
    var isSelected: Boolean = false,
    var mRotation: SparseArray<Float>? = SparseArray()
  ) : Parcelable {

    constructor(parcel: Parcel) : this(
      parcel.readInt(),
      parcel.readByte() != 0.toByte(),
      parcel.readByte() != 0.toByte(),
      parcel.readSparseArray(SparseArray::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
      parcel.writeInt(mCenterItemPosition)
      parcel.writeByte(if (isCollapsed) 1 else 0)
      parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SavedState> {
      override fun createFromParcel(parcel: Parcel): SavedState? = SavedState(parcel)
      override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
    }
  }
}