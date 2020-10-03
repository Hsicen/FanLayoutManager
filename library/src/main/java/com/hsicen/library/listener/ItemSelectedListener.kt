package com.hsicen.library.listener

import android.view.View

/**
 * 作者：hsicen  2020/10/2 13:28
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager选中回调
 */
interface ItemSelectedListener {

  /*** 当前选中Item */
  fun onItemSelected(position: Int, selectedView: View?)
}