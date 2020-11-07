@file:Suppress("NOTHING_TO_INLINE")

package com.hsicen.library.widget

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

/**
 * 作者：黄思程  2020/9/30 9:18
 * 邮箱：huangsicheng@camera360.com
 * 功能：
 * 描述：dp and sp extension
 */

val Float.sp2px: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )

val Float.dp2px: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Float.px2sp: Float
    get() {
        val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
        return (this / scaledDensity + 0.5f)
    }

val Float.px2dp: Float
    get() {
        val scaledDensity = Resources.getSystem().displayMetrics.density
        return (this / scaledDensity + 0.5f)
    }

val Int.sp2px: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this * 1.0f, Resources.getSystem().displayMetrics
    ).toInt()

val Int.dp2px: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this * 1.0f, Resources.getSystem().displayMetrics
    ).toInt()

/***  获取屏幕高度*/
fun screenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

/*** 获取屏幕宽度*/
fun screenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels

inline fun Context.dp2px(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

inline fun Context.dp2px(dp: Int) = dp2px(dp.toFloat()).toInt()

inline fun Context.sp2px(sp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

inline fun Context.sp2px(sp: Int) = sp2px(sp.toFloat()).toInt()

inline fun Context.px2dp(px: Int) = px / resources.displayMetrics.density
inline fun Context.px2sp(px: Int) = px / resources.displayMetrics.scaledDensity
