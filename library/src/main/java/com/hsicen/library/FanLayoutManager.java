package com.hsicen.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hsicen.library.callbacks.ItemSelectedListener;
import com.hsicen.library.widget.CustomSnapHelper;

import java.util.Collection;

/**
 * 作者：hsicen  2020/11/7 13:04
 * 邮箱：codinghuang@163.com
 * 功能：
 * 描述：支持弧形布局和居中布局
 */
public class FanLayoutManager extends RecyclerView.LayoutManager {
    /**
     * Settings for fan layout manager. {@link FanLayoutManagerSettings.Builder}
     */
    private final FanLayoutManagerSettings mSettings;

    /**
     * Map with view cache.
     */
    private final SparseArray<View> mViewCache = new SparseArray<>();
    /**
     * LinearSmoothScroller for switch views.
     */
    private final FanCardScroller mFanCardScroller;

    /**
     * Helper module need to implement 'open','close', 'shift' views functionality.
     * By default using {@link AnimationHelperImpl}
     * Can be changed {@link #setAnimationHelper(AnimationHelper)}
     */
    @NonNull
    private AnimationHelper mAnimationHelper;
    /**
     * Position of selected item in adapter. ADAPTER!!
     */
    private int mSelectedItemPosition = RecyclerView.NO_POSITION;
    /**
     * Position of item we need to scroll to right now.
     */
    private int mScrollToPosition = RecyclerView.NO_POSITION;
    /**
     * Need to block some events between smooth scroll and select item animation.
     * true before start smoothScroll to selected item
     * false after smooth scroll finished and after select animation is started.
     */
    private boolean mIsWaitingToSelectAnimation = false;
    /**
     * Need to block some events while scaling view.
     * true right after smooth scroll finished scrolling.
     */
    private boolean mIsSelectAnimationInProcess = false;

    /**
     * Need to block some events while deselecting item is preparing.
     */
    private boolean mIsWaitingToDeselectAnimation = false;
    /**
     * Need to block some events.
     */
    private boolean mIsDeselectAnimationInProcess = false;

    /**
     * Flag using to change bounce radius.
     */
    private boolean mIsSelectedItemStraightened = false;

    /**
     * Need to block some events.
     */
    private boolean mIsSelectedItemStraightenedInProcess = false;

    /**
     * Need to block some events while collapsing views.
     */
    private boolean mIsViewCollapsing = false;

    /**
     * Saved state for layout manager.
     */
    private SavedState mPendingSavedState;
    /**
     * true if item selected
     */
    private boolean mIsSelected = false;

    /**
     * true if views collapsed
     */
    private boolean mIsCollapsed = false;

    private View mCenterView = null;
    private ItemSelectedListener mSelectedListener = null;
    private CustomSnapHelper snapHelper = new CustomSnapHelper();

    public FanLayoutManager(@NonNull Context context) {
        this(context, null);
        mAnimationHelper = new AnimationHelperImpl();
    }

    public FanLayoutManager(@NonNull Context context, @Nullable FanLayoutManagerSettings settings) {
        // create default settings
        mSettings = settings == null ? FanLayoutManagerSettings.newBuilder(context).build() : settings;
        // create default animation helper
        mAnimationHelper = new AnimationHelperImpl();
        // create default FanCardScroller
        mFanCardScroller = new FanCardScroller(context);
        // set callback which return calculated scroll time
        mFanCardScroller.setCardTimeCallback(new FanCardScroller.FanCardTimeCallback() {
            @Override
            public void onTimeForScrollingCalculated(int targetPosition, int time) {
                // select item after scroll to item
                if (settings.isClickScale()) {
                    selectItem(targetPosition, time);
                }
            }
        });
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        snapHelper.attachToRecyclerView(view);
    }

    public void addOnItemSelectedListener(ItemSelectedListener listener) {
        mSelectedListener = listener;
    }

    public void saveState() {
        mPendingSavedState = new SavedState();
        // save center view position
        mPendingSavedState.mCenterItemPosition = findCurrentCenterViewPos();
        // save selected state for center view
        mPendingSavedState.isSelected = mIsSelected;
        // save collapsed state for views
        mPendingSavedState.isCollapsed = mIsCollapsed;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        saveState();
        return mPendingSavedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = (SavedState) state;
            // center view position
            mScrollToPosition = mPendingSavedState.mCenterItemPosition;
            // position for selected item
            mSelectedItemPosition = mPendingSavedState.isSelected ? mScrollToPosition : RecyclerView.NO_POSITION;
            // selected state
            mIsSelected = mPendingSavedState.isSelected;
            // collapsed state
            mIsCollapsed = mPendingSavedState.isCollapsed;
        }
    }

    /**
     * @return selected item position
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * @return is selected item straightened or has base (bounce) rotation
     */
    public boolean isSelectedItemStraightened() {
        return mIsSelectedItemStraightened;
    }

    /**
     * Setter for custom animation helper
     *
     * @param animationHelper custom animation helper.
     */
    @Deprecated
    void setAnimationHelper(@Nullable AnimationHelper animationHelper) {
        mAnimationHelper = animationHelper == null ? new AnimationHelperImpl() : animationHelper;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // find center view before detach or recycle all views
        mCenterView = findCurrentCenterView();
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        detachAndScrapAttachedViews(recycler);
        fill(recycler, true);
    }

    /**
     * Method create or reuse views for recyclerView.
     *
     * @param recycler recycler from the recyclerView
     */
    private void fill(RecyclerView.Recycler recycler, Boolean notify) {
        mViewCache.clear();

        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            int pos = getPosition(view);
            mViewCache.put(pos, view);
        }

        for (int i = 0; i < mViewCache.size(); i++) {
            detachView(mViewCache.valueAt(i));
        }
        // position for center view
        int centerViewPosition = mCenterView == null ? 0 : getPosition(mCenterView);

        // left offset for center view
        int centerViewOffset = mCenterView == null ? (int) (getWidth() / 2F - mSettings.getViewWidthPx() / 2F) :
                getDecoratedLeft(mCenterView);

        // main fill logic
        if (mScrollToPosition != RecyclerView.NO_POSITION) {
            // fill views if start position not in the middle of screen (restore state)
            fillRightFromCenter(mScrollToPosition, centerViewOffset, recycler);
        } else {
            // fill views if start position in the middle of the screen
            fillRightFromCenter(centerViewPosition, centerViewOffset, recycler);
        }

        //update center view after recycle all views
        if (getChildCount() != 0) {
            mCenterView = findCurrentCenterView();
      /*if (notify) {
        mSelectedListener.onItemSelected(findCurrentCenterViewPos(), mCenterView);
      }*/
        }

        for (int i = 0; i < mViewCache.size(); i++) {
            recycler.recycleView(mViewCache.valueAt(i));
        }
        // update rotations.
        updateArcViewPositions();
    }

    /**
     * Measure view with margins and specs
     *
     * @param child      view to measure
     * @param widthSpec  spec for width
     * @param heightSpec spec for height
     */
    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec) {

        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(child, decorRect);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + decorRect.left,
                lp.rightMargin + decorRect.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + decorRect.top,
                lp.bottomMargin + decorRect.bottom);
        child.measure(widthSpec, heightSpec);

    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        mScrollToPosition = RecyclerView.NO_POSITION;
        mPendingSavedState = null;

        if (dx == RecyclerView.NO_POSITION) {
            int delta = scrollHorizontallyInternal(dx);
            offsetChildrenHorizontal(-delta);
            fill(recycler, false);
            return delta;
        }

        int delta = scrollHorizontallyInternal(dx);
        offsetChildrenHorizontal(-delta);
        fill(recycler, true);
        return delta;
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int heightMode = View.MeasureSpec.getMode(heightSpec);
        long scaledHeight = (long) (mSettings.getViewHeightPx() * mAnimationHelper.getViewScaleFactor());
        long scaledWidth = (long) (mSettings.getViewWidthPx() * mAnimationHelper.getViewScaleFactor());
        //这里可以设置RecyclerView的高度
        int height = heightMode == View.MeasureSpec.EXACTLY ? View.MeasureSpec.getSize(heightSpec) :
                (int) (scaledHeight + mSettings.getTopMargin() + mSettings.getBottomMargin());

        //noinspection Range
        heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        updateArcViewPositions();
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    /*** 计算每次滑动的距离*/
    private int scrollHorizontallyInternal(int dx) {
        int childCount = getChildCount();
        // check child count
        if (childCount == 0) {
            return 0;
        }
        // items count in the adapter
        int itemCount = getItemCount();

        View leftView = getChildAt(0);
        View rightView = getChildAt(childCount - 1);
        // search left and right views.
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (getDecoratedLeft(leftView) > getDecoratedLeft(view)) {
                leftView = view;
            }
            if (getDecoratedRight(rightView) < getDecoratedRight(view)) {
                rightView = view;
            }
        }
        // area with filling views. need to find borders
        int viewSpan = getDecoratedRight(rightView) > getWidth() ? getDecoratedRight(rightView) : getWidth() -
                (Math.min(getDecoratedLeft(leftView), 0));

        // check left and right borders
        if (viewSpan < getWidth()) {
            return 0;
        }

        int delta = 0;
        if (dx < 0) {
            // move views left

            // position for left item in the adapter
            int firstViewAdapterPos = getPosition(leftView);

            if (firstViewAdapterPos > 0) {
                // if item isn't first in the adapter
                delta = dx;
            } else {
                // if item first in the adapter

                // stop scrolling if item in the middle.
                int viewLeft = getDecoratedLeft(leftView) - getWidth() / 2 + getDecoratedMeasuredWidth(leftView) / 2;
                delta = Math.max(viewLeft, dx);
            }
        } else if (dx > 0) {
            // move views right

            // position for right item in the adapter
            int lastViewAdapterPos = getPosition(rightView);

            if (lastViewAdapterPos < itemCount - 1) {
                // if item isn't last in the adapter
                delta = dx;
            } else {
                // if item last in the adapter

                // stop scrolling if item in the middle.
                int viewRight = getDecoratedRight(rightView) + getWidth() / 2 - getDecoratedMeasuredWidth(rightView) / 2;
                int parentRight = getWidth();
                delta = Math.min(viewRight - parentRight, dx);
            }
        }
        return delta;
    }

    /**
     * Change pivot, rotation, translation for view to create fan effect.
     * Change rotation to create bounce effect.
     */
    private void updateArcViewPositions() {

        // +++++ init params +++++
        float halfWidth = getWidth() / 2;
        // minimal radius is recyclerView width * 2
        double radius = getWidth() * 2;
        double powRadius = radius * radius;
        double rotation;
        float halfViewWidth;
        double deltaX;
        double deltaY;
        int viewPosition;
        // ----- init params -----
        for (int count = getChildCount(), i = 0; i < count; i++) {
            View view = getChildAt(i);
            rotation = 0;
            // need to show views in "fan" style

            halfViewWidth = view.getWidth() / 2;

            // change pivot point to center bottom of the view
            view.setPivotX(halfViewWidth);
            view.setPivotY(view.getHeight());

            if (mSettings.isFanRadiusEnable()) {

                // distance between center of screen to center of view in x-axis
                deltaX = halfWidth - getDecoratedLeft(view) - halfViewWidth;

                // distance in which need to move view in y-axis. Low accuracy
                deltaY = radius - Math.sqrt(powRadius - deltaX * deltaX);
                view.setTranslationY((float) deltaY);
                // calculate view rotation
                rotation = (Math.toDegrees(Math.asin((radius - deltaY) / radius)) - 90) * Math.signum(deltaX);

            }

            viewPosition = getPosition(view);
            view.setRotation((float) (rotation + (mSelectedItemPosition == viewPosition && mIsSelectedItemStraightened ? 0 : 0)));
        }
    }

    /**
     * Method draw view using center view position.
     *
     * @param centerViewPosition position of center view (anchor). This view will be in center
     * @param recycler           Recycler from the recyclerView
     */
    private void fillRightFromCenter(int centerViewPosition, int centerViewOffset, RecyclerView.Recycler recycler) {
        // +++++++++++ Prepare data +++++++++++

        // left limit. need to prepare with before they will be show to user.
        int leftBorder = -(mSettings.getViewWidthPx() + (mIsCollapsed ? mSettings.getViewWidthPx() : 0));

        // right limit.
        int rightBorder = getWidth() + (mSettings.getViewWidthPx() + (mIsCollapsed ? mSettings.getViewWidthPx() : 0));
        int leftViewOffset = centerViewOffset;
        int leftViewPosition = centerViewPosition;

        // margin to draw cards in bottom  设置Item距离底部的高度 (mSettings.getViewWidthPx() / 4)
        final int baseTopMargin = Math.max(0, getHeight() - mSettings.getViewHeightPx() - mSettings.getBottomMargin());
        //设置Item的水平间隔
        int overlapDistance;
        if (mIsCollapsed) {

            // overlap distance if views are collapsed
            overlapDistance = -mSettings.getItemMargin();
        } else {

            // overlap distance if views aren't collapsed
            overlapDistance = mSettings.getItemMargin();
        }

        boolean fillRight = true;

        // specs for item views
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(mSettings.getViewWidthPx(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(mSettings.getViewHeightPx(), View.MeasureSpec.EXACTLY);

        // if have to restore state with selected item
        boolean hasPendingStateSelectedItem = mPendingSavedState != null && mPendingSavedState.isSelected &&
                mPendingSavedState.mCenterItemPosition != RecyclerView.NO_POSITION;

        // offset for left and right views in case we have to restore pending state with selected view.
        // this is delta distance between overlap cards state and collapse (selected card) card state
        // need to use ones for all left view and right views
        float deltaOffset = mSettings.getViewWidthPx() / 2;

        // --------- Prepare data ---------

        // search left position for first view
        while (leftViewOffset > leftBorder && leftViewPosition >= 0) {
            if (mIsCollapsed) {
                // offset for collapsed views
                leftViewOffset -= (mSettings.getViewWidthPx() + Math.abs(overlapDistance));
            } else {
                // offset for NOT collapsed views
                leftViewOffset -= (mSettings.getViewWidthPx() - Math.abs(overlapDistance));
            }
            leftViewPosition--;
        }

        if (leftViewPosition < 0) {
            // if theoretically position for left view is less than left view.
            if (mIsCollapsed) {
                // offset for collapsed views
                leftViewOffset += (mSettings.getViewWidthPx() + Math.abs(overlapDistance)) * Math.abs(leftViewPosition);
            } else {
                // offset for NOT collapsed views
                leftViewOffset += (mSettings.getViewWidthPx() - Math.abs(overlapDistance)) * Math.abs(leftViewPosition);
            }
            leftViewPosition = 0;
        }

        // offset for left views if we restore state and have selected item
        if (hasPendingStateSelectedItem && leftViewPosition != mPendingSavedState.mCenterItemPosition) {
            leftViewOffset += -deltaOffset;
        }

        View selectedView = null;
        while (fillRight && leftViewPosition < getItemCount()) {

            // offset for current view if we restore state and have selected item
            if (hasPendingStateSelectedItem && leftViewPosition == mPendingSavedState.mCenterItemPosition && leftViewPosition != 0) {
                leftViewOffset += deltaOffset;
            }

            // get view from local cache
            View view = mViewCache.get(leftViewPosition);

            if (view == null) {
                // get view from recycler
                view = recycler.getViewForPosition(leftViewPosition);
                // optimization for view rotation
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                // add vew to the recyclerView
                addView(view);
                // measuring view
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                // set offsets, with and height in the recyclerView
                layoutDecorated(view, leftViewOffset, baseTopMargin,
                        leftViewOffset + mSettings.getViewWidthPx(), baseTopMargin + mSettings.getViewHeightPx());
            } else {
                attachView(view, leftViewPosition);
                mViewCache.remove(leftViewPosition);
            }

            view.setScaleX(1F);
            view.setScaleY(1F);

            if (mIsSelected && centerViewPosition == leftViewPosition) {
                selectedView = view;
            }

            // calculate position for next view. last position + view height - overlap between views.
            leftViewOffset = leftViewOffset + mSettings.getViewWidthPx() - overlapDistance;

            // check right border. stop loop if next view is > then right border.
            fillRight = leftViewOffset < rightBorder;

            // offset for right views if we restore state and have selected item
            if (hasPendingStateSelectedItem && leftViewPosition == mPendingSavedState.mCenterItemPosition) {
                leftViewOffset += deltaOffset;
            }

            leftViewPosition++;
        }

        // if we have to restore state with selected item
        // this part need to scale center selected view
        if (hasPendingStateSelectedItem) {
            //            View view = findCurrentCenterView();
            if (selectedView != null) {
                selectedView.setScaleX(mAnimationHelper.getViewScaleFactor());
                selectedView.setScaleY(mAnimationHelper.getViewScaleFactor());
            }
        }
    }

    public boolean isItemSelected() {
        return mSelectedItemPosition != RecyclerView.NO_POSITION;
    }

    private void selectItem(final int position, int delay) {
        if (mSelectedItemPosition == position) {
            // if select already selected item
            deselectItem(mSelectedItemPosition);
            return;
        }

        // search view by position
        View viewToSelect = null;
        for (int count = getChildCount(), i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (position == getPosition(view)) {
                viewToSelect = view;
            }
        }

        if (viewToSelect == null) {
            // view to select not found!!!
            return;
        }
        // save position of view which will be selected
        mSelectedItemPosition = position;
        // save selected stay... no way back...
        mIsSelected = true;
        // open item animation wait for start but not in process.
        // select item animation prepare and wait until smooth scroll is finished
        mIsWaitingToSelectAnimation = true;

        mAnimationHelper.openItem(viewToSelect, delay * 3 /*need to finish scroll before start open*/,
                new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        super.onAnimationStart(animator);
                        // change state of select animation progress
                        mIsSelectAnimationInProcess = true;
                        mIsWaitingToSelectAnimation = false;
                        // shift distance between center view and left, right views.
                        final int delta = 0; //设置缩放的偏移量
                        // generate data for animation helper. (calculate final positions for all views)
                        final Collection<ViewAnimationInfo> infoViews = ViewAnimationInfoGenerator.generate(delta,
                                true,
                                FanLayoutManager.this,
                                mSelectedItemPosition,
                                false);

                        // animate shifting let and right views
                        mAnimationHelper.shiftSideViews(
                                infoViews,
                                0,
                                FanLayoutManager.this,
                                null,
                                new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        // update rotation and translation for all views
                                        updateArcViewPositions();
                                    }
                                });
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mIsSelectAnimationInProcess = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        mIsSelectAnimationInProcess = false;
                    }
                });
    }

    /**
     * Deselect selected item. {@link #deselectItem(int)}
     */
    public void deselectItem() {
        deselectItem(mSelectedItemPosition);
    }

    /**
     * Deselect item with default params. {@link #deselectItem(RecyclerView, int, int, int)}
     *
     * @param position selected item position
     */
    private void deselectItem(final int position) {
        deselectItem(null, position, RecyclerView.NO_POSITION, 0);
    }

    /**
     * Deselect item
     *
     * @param recyclerView     RecyclerView for this LayoutManager
     * @param position         position item for deselect
     * @param scrollToPosition position to scroll after deselect
     * @param delay            waiting duration before start deselect
     */

    private void deselectItem(final RecyclerView recyclerView, final int position, final int scrollToPosition, final int delay) {

        if (position == RecyclerView.NO_POSITION) {
            // if position is default non selected value
            return;
        }

        if (mIsSelectedItemStraightened) {
            restoreBaseRotationSelectedItem(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    closeItem(recyclerView, position, scrollToPosition, delay);
                }
            });
        } else {
            closeItem(recyclerView, position, scrollToPosition, delay);
        }

    }

    /**
     * Close item
     *
     * @param recyclerView     RecyclerView for this LayoutManager
     * @param position         position item for deselect
     * @param scrollToPosition position to scroll after deselect
     * @param delay            waiting duration before start deselect
     */

    private void closeItem(final RecyclerView recyclerView, final int position, final int scrollToPosition, final int delay) {
        // wait for start deselect animation
        mIsWaitingToDeselectAnimation = true;
        // search view by position
        View viewToDeselect = null;
        for (int count = getChildCount(), i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (position == getPosition(view)) {
                viewToDeselect = view;
            }
        }
        // remove selected item position
        mSelectedItemPosition = RecyclerView.NO_POSITION;

        // remove selected state... no way back...
        mIsSelected = false;

        if (viewToDeselect == null) {
            mSelectedItemPosition = RecyclerView.NO_POSITION;
            // search error!!! No view found!!!
            return;
        }

        // close item animation
        mAnimationHelper.closeItem(viewToDeselect, delay, new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

                // change states
                mIsDeselectAnimationInProcess = true;
                mIsWaitingToDeselectAnimation = false;

                // shift distance between center view and left, right views.
                final int delta = 0; //设置缩放的偏移量

                // generate data for animation helper. (calculate final positions for all views)
                final Collection<ViewAnimationInfo> infoViews =
                        ViewAnimationInfoGenerator.generate(delta,
                                false,
                                FanLayoutManager.this,
                                position,
                                false);

                // animate shifting let and right views
                mAnimationHelper.shiftSideViews(
                        infoViews,
                        0,
                        FanLayoutManager.this,
                        null,
                        new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                // update rotation and translation for all views
                                updateArcViewPositions();
                            }
                        });
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsDeselectAnimationInProcess = false;
                if (recyclerView != null && scrollToPosition != RecyclerView.NO_POSITION) {
                    // scroll to new position after deselect animation end
                    smoothScrollToPosition(recyclerView, null, scrollToPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mIsDeselectAnimationInProcess = false;
                if (recyclerView != null && scrollToPosition != RecyclerView.NO_POSITION) {
                    // scroll to new position after deselect animation cancel
                    smoothScrollToPosition(recyclerView, null, scrollToPosition);
                }
            }
        });

    }

    @Override
    public void scrollToPosition(int position) {
        mScrollToPosition = position;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        if (position >= getItemCount()) {
            // if position is not in range
            return;
        }
        // smooth scroll to position
        mFanCardScroller.setTargetPosition(position);
        startSmoothScroll(mFanCardScroller);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            scrollToCenter();
        }
    }

    /*** 滑动到中心Item*/
    private void scrollToCenter() {
        View nearestToCenterView = findCurrentCenterView();
        if (nearestToCenterView != null) {
            mSelectedListener.onItemSelected(getPosition(nearestToCenterView), nearestToCenterView);
        }
    }

    /**
     * Method need to restore base (bounce) rotation for item
     *
     * @param listener rotate function listener
     */
    public void restoreBaseRotationSelectedItem(final Animator.AnimatorListener listener) {
        // check all animations
        if (mSelectedItemPosition == RecyclerView.NO_POSITION || mIsSelectAnimationInProcess ||
                mIsDeselectAnimationInProcess || mIsSelectedItemStraightenedInProcess || mIsWaitingToDeselectAnimation ||
                mIsWaitingToSelectAnimation || mIsViewCollapsing || !mIsSelectedItemStraightened) {
            // block if any animation in progress
            return;
        }

        // +++++ prepare data +++++
        View viewToRotate = null;
        View view;
        // ----- prepare data -----

        // search selected view
        for (int count = getChildCount(), i = 0; i < count; i++) {
            view = getChildAt(i);

            if (mSelectedItemPosition == getPosition(view)) {
                viewToRotate = view;
            }
        }

        if (viewToRotate != null) {
            // save state
            mIsSelectedItemStraightenedInProcess = true;

            // start straight animation
            mAnimationHelper.rotateView(viewToRotate, 0, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                    mIsSelectedItemStraightened = false;
                    mIsSelectedItemStraightenedInProcess = false;

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationCancel(animation);
                    }
                    mIsSelectedItemStraightened = false;
                    mIsSelectedItemStraightenedInProcess = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
        }
    }

    /*** 折叠Item*/
    public void collapseViews() {
        // check all animations
        if (mIsSelectAnimationInProcess || mIsWaitingToSelectAnimation ||
                mIsDeselectAnimationInProcess || mIsWaitingToDeselectAnimation ||
                mIsSelectedItemStraightenedInProcess || mIsViewCollapsing) {
            return;
        }
        // steps:
        // 1) Lock screen (Stop scrolling)
        // 2) Collapse all cards
        // 3) Unlock screen
        // 4) Scroll to center nearest card if not selected

        // 1) lock screen
        mIsViewCollapsing = true;

        // 2) Collapse all cards
        updateItemsByMode();
    }

    /*** 折叠Item*/
    private void updateItemsByMode() {
        // collapse distance
        int delta = mSettings.getViewWidthPx() / 2;

        // generate data for collapse animation
        final Collection<ViewAnimationInfo> infoViews =
                ViewAnimationInfoGenerator.generate(delta,
                        mIsCollapsed = !mIsCollapsed,
                        FanLayoutManager.this,
                        findCurrentCenterViewPos(),
                        true);

        // collapse views
        mAnimationHelper.shiftSideViews(infoViews,
                0,
                FanLayoutManager.this,
                new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        // 3) Unlock screen
                        mIsViewCollapsing = !mIsViewCollapsing;

                        // 4) Scroll to center nearest card
                        scrollToCenter();
                    }
                }, valueAnimator -> {
                    // update rotation and translation for all views
                    updateArcViewPositions();
                });
    }

    /*** 找到当前的中心Item*/
    @Nullable
    private View findCurrentCenterView() {
        // +++++ prepare data +++++

        // center of the screen in x-axis
        float centerX = getWidth() / 2F;
        float viewHalfWidth = mSettings.getViewWidthPx() / 2F;
        View nearestToCenterView = null;
        int nearestDeltaX = 0;
        View item;
        int centerXView;
        // ----- prepare data -----

        // search nearest to center view
        for (int count = getChildCount(), i = 0; i < count; i++) {
            item = getChildAt(i);
            centerXView = (int) (getDecoratedLeft(item) + viewHalfWidth);
            if (nearestToCenterView == null || Math.abs(nearestDeltaX) > Math.abs(centerX - centerXView)) {
                nearestToCenterView = item;
                nearestDeltaX = (int) (centerX - centerXView);
            }
        }

        return nearestToCenterView;
    }

    /**
     * Find position of view in the middle of screen
     *
     * @return position of center view or {@link RecyclerView#NO_POSITION}
     */
    private int findCurrentCenterViewPos() {
        View view = mCenterView;
        return view == null ? RecyclerView.NO_POSITION : getPosition(view);
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        recyclerView.stopScroll();
        saveState();
        if (getItemCount() <= mSelectedItemPosition) {
            mSelectedItemPosition = RecyclerView.NO_POSITION;
            // save selected state for center view
            mPendingSavedState.isSelected = false;
            mIsSelected = false;
        }
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        recyclerView.stopScroll();
        saveState();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsUpdated(recyclerView, positionStart, itemCount);
        recyclerView.stopScroll();
        saveState();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        super.onItemsUpdated(recyclerView, positionStart, itemCount, payload);
        recyclerView.stopScroll();
        saveState();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
        recyclerView.stopScroll();
        saveState();
        if (mSelectedItemPosition >= positionStart && mSelectedItemPosition < positionStart + itemCount) {
            mSelectedItemPosition = RecyclerView.NO_POSITION;
            // save selected state for center view
            mPendingSavedState.isSelected = false;
        }
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        super.onItemsMoved(recyclerView, from, to, itemCount);
    }

    private static class SavedState implements Parcelable {
        int mCenterItemPosition = RecyclerView.NO_POSITION;
        boolean isCollapsed;
        boolean isSelected;
        SparseArray<Float> mRotation;

        public SavedState() {
        }

        public SavedState(SavedState other) {
            mCenterItemPosition = other.mCenterItemPosition;
            isCollapsed = other.isCollapsed;
            isSelected = other.isSelected;
            mRotation = other.mRotation;
        }

        protected SavedState(Parcel in) {
            mCenterItemPosition = in.readInt();
            isCollapsed = in.readByte() != 0;
            isSelected = in.readByte() != 0;
            mRotation = in.readSparseArray(SparseArray.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mCenterItemPosition);
            dest.writeByte((byte) (isCollapsed ? 1 : 0));
            dest.writeByte((byte) (isSelected ? 1 : 0));
            dest.writeSparseArray(mRotation);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}