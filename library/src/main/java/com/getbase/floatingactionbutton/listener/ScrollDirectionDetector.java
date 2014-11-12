package com.getbase.floatingactionbutton.listener;

import android.view.View;
import android.widget.AbsListView;

import com.getbase.floatingactionbutton.R;

/**
 * Detects which direction list view was scrolled.
 * <p/>
 * Set {@link ScrollDirectionListener} to get callbacks
 * {@link ScrollDirectionListener#onScrollDown()} or
 * {@link ScrollDirectionListener#onScrollUp()}
 *
 * @author Vilius Kraujutis
 */
public abstract class ScrollDirectionDetector implements AbsListView.OnScrollListener {
    private ScrollDirectionListener mScrollDirectionListener;
    private int mPreviousScrollY;
    private int mPreviousFirstVisibleItem;
    public int mLastChangeY;
    private AbsListView mListView;
    private int mMinSignificantScroll;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mMinSignificantScroll = view.getContext().getResources().getDimensionPixelOffset(R.dimen.fab_min_significant_scroll);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int newScrollY = estimateScrollY();
        if (mScrollDirectionListener != null && isSameRow(firstVisibleItem) && isSignificantDelta(newScrollY)) {
            if (isScrollUp(newScrollY)) {
                mScrollDirectionListener.onScrollUp();
            } else {
                mScrollDirectionListener.onScrollDown();
            }
        }
    }

    public ScrollDirectionListener getScrollDirectionListener() {
        return mScrollDirectionListener;
    }

    public void setScrollDirectionListener(ScrollDirectionListener mScrollDirectionListener) {
        this.mScrollDirectionListener = mScrollDirectionListener;
    }

    /**
     * @return true if scrolled up or false otherwise
     * @see #isSignificantDelta(int) which ensures, that events are not fired it there was no scrolling
     */
    private boolean isScrollUp(int newScrollY) {
        boolean scrollUp = newScrollY > mPreviousScrollY;
        mPreviousScrollY = newScrollY;
        return scrollUp;
    }

    /**
     * Make sure wrong direction method is not called when stopping scrolling
     * and finger moved a little to opposite direction.
     *
     * @see #isScrollUp(int)
     */
    private boolean isSignificantDelta(int newScrollY) {
        boolean isSignificantDelta = Math.abs(mLastChangeY - newScrollY) > mMinSignificantScroll;
        if (isSignificantDelta)
            mLastChangeY = newScrollY;
        return isSignificantDelta;
    }

    /**
     * <code>newScrollY</code> position might not be correct if:
     * <ul>
     * <li><code>firstVisibleItem</code> is different than <code>mPreviousFirstVisibleItem</code></li>
     * <li>list has rows of different height</li>
     * </ul>
     * <p/>
     * It's necessary to track if row did not change, so events
     * {@link ScrollDirectionListener#onScrollUp()} or {@link ScrollDirectionListener#onScrollDown()} could be fired with confidence
     *
     * @see #estimateScrollY()
     */
    private boolean isSameRow(int firstVisibleItem) {
        boolean rowsChanged = firstVisibleItem == mPreviousFirstVisibleItem;
        mPreviousFirstVisibleItem = firstVisibleItem;
        return rowsChanged;
    }

    /**
     * Will be incorrect if rows has changed and if list has rows of different heights
     * <p/>
     * So when measuring scroll direction, it's necessary to ignore this value
     * if first visible row is different than previously calculated.
     *
     * @deprecated because it should be used with caution
     */
    private int estimateScrollY() {
        if (mListView == null || mListView.getChildAt(0) == null) return 0;
        View topChild = mListView.getChildAt(0);
        return mListView.getFirstVisiblePosition() * topChild.getHeight() - topChild.getTop();
    }

    public void setListView(AbsListView listView) {
        mListView = listView;
    }
}