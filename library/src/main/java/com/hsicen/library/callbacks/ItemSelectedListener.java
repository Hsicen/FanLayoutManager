package com.hsicen.library.callbacks;

import android.view.View;

/**
 * 作者：hsicen  2020/10/2 13:28
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：FanLayoutManager
 */
public interface ItemSelectedListener {

    /*** 当前选中Item*/
    void onItemSelected(int position, View selectedView);
}
