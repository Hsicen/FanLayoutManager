package com.hsicen.library;

import android.content.Context;
import android.graphics.PointF;

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：hsicen  2020/11/6 17:04
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：滑动处理基础类
 */
abstract class BaseSmoothScroller extends LinearSmoothScroller {
    BaseSmoothScroller(Context context) {
        super(context);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();

        if (layoutManager instanceof FanLayoutManager) {
            if (getChildCount() == 0) {
                return null;
            }
            final int firstChildPos = layoutManager.getPosition(layoutManager.getChildAt(0));
            final int direction = targetPosition < firstChildPos ? -1 : 1;
            return new PointF(direction, 0);
        }
        return new PointF();
    }
}
