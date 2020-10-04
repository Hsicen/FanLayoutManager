package com.hsicen.library

import android.content.Context
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 作者：hsicen  2020/10/2 16:21
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：FanLayoutManager配置参数
 */
class FanLayoutManagerSettings private constructor(builder: Builder) {
  val viewWidthDp: Float
  val viewHeightDp: Float
  val viewWidthPx: Int
  val viewHeightPx: Int
  val isFanRadiusEnable: Boolean
  val angleItemBounce: Float

  class Builder constructor(private val mContext: Context) {
    var mViewWidthDp = 0f
    var mViewHeightDp = 0f
    var mIsFanRadiusEnable = false
    var mAngleItemBounce = 0f
    var mViewWidthPx = 0
    var mViewHeightPx = 0

    /**
     * 设置宽度
     * @param viewWidthDp dp值
     */
    fun withViewWidthDp(viewWidthDp: Float): Builder {
      mViewWidthDp = viewWidthDp
      mViewWidthPx = (mContext.resources.displayMetrics.density * viewWidthDp).roundToInt()
      mViewWidthPx = min(mContext.resources.displayMetrics.widthPixels, mViewWidthPx)
      return this
    }


    /**
     * 设置高度
     * @param viewHeightDp dp值
     */
    fun withViewHeightDp(viewHeightDp: Float): Builder {
      mViewHeightDp = viewHeightDp
      mViewHeightPx = (mContext.resources.displayMetrics.density * viewHeightDp).roundToInt()
      mViewHeightPx = min(mContext.resources.displayMetrics.heightPixels, mViewHeightPx)
      return this
    }

    /**
     * 设置是否启用扇形布局
     * @param isFanRadiusEnable 设置是否启用扇形布局
     */
    fun withFanRadius(isFanRadiusEnable: Boolean): Builder {
      mIsFanRadiusEnable = isFanRadiusEnable
      return this
    }

    /**
     * 设置扇形布局角度
     * @param angleItemBounce 扇形角度
     */
    fun withAngleItemBounce(angleItemBounce: Float): Builder {
      if (angleItemBounce <= 0f) {
        return this
      }
      mAngleItemBounce = min(BOUNCE_MAX, angleItemBounce)
      return this
    }


    fun build(): FanLayoutManagerSettings {
      if (mViewWidthDp.compareTo(0f) == 0) {
        withViewWidthDp(DEFAULT_VIEW_WIDTH_DP)
      }
      if (mViewHeightDp.compareTo(0f) == 0) {
        withViewHeightDp(DEFAULT_VIEW_HEIGHT_DP)
      }
      return FanLayoutManagerSettings(this)
    }

    companion object {
      private const val BOUNCE_MAX = 10f
    }
  }

  companion object {
    private const val DEFAULT_VIEW_WIDTH_DP = 120f
    private const val DEFAULT_VIEW_HEIGHT_DP = 160f

    fun newBuilder(context: Context): Builder {
      return Builder(context)
    }
  }

  init {
    viewWidthDp = builder.mViewWidthDp
    viewHeightDp = builder.mViewHeightDp
    isFanRadiusEnable = builder.mIsFanRadiusEnable
    angleItemBounce = builder.mAngleItemBounce
    viewWidthPx = builder.mViewWidthPx
    viewHeightPx = builder.mViewHeightPx
  }
}
