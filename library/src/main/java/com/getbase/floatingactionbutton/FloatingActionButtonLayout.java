package com.getbase.floatingactionbutton;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;


/**
 * Created by Alorma on 26/08/2014.
 */
public class FloatingActionButtonLayout extends RelativeLayout implements ViewTreeObserver.OnScrollChangedListener {

	private static final long FOLD_DURATION = 500;
	private FABScrollContentListener fabScrollContentListener;
	private FloatingActionButton fabView;
	private int topId;
	private OnClickListener fabClickListener;
	private boolean fabVisible;
	private String fabTag;
	private View scrolledChild;
	private boolean forceVisbility;
	private int scrollableId;
	private boolean isFold = true;
	private View topView;
	private int topFoldedSize;
	private int topUnfoldedSize;

	public FloatingActionButtonLayout(Context context) {
		super(context);
		init(null, 0);
	}

	public FloatingActionButtonLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public FloatingActionButtonLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		isInEditMode();

		topFoldedSize = (int) (56 * getResources().getDisplayMetrics().density);

		topUnfoldedSize = topFoldedSize * 2;

		if (attrs != null) {
			TypedArray attr = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButtonLayout, defStyle, 0);

			if (attr.hasValue(R.styleable.FloatingActionButtonLayout_fab_ly_top_id)) {
				topId = attr.getResourceId(R.styleable.FloatingActionButtonLayout_fab_ly_top_id, 0);
				if (topId != 0) {
					fabVisible = true;
					createFabView();
				}
			}

			if (attr.hasValue(R.styleable.FloatingActionButtonLayout_fab_ly_scrollable_id)) {
				scrollableId = attr.getResourceId(R.styleable.FloatingActionButtonLayout_fab_ly_scrollable_id, 0);
			}

			if (attr.hasValue(R.styleable.FloatingActionButtonLayout_fab_ly_folded_size)) {
				topFoldedSize = attr.getDimensionPixelOffset(R.styleable.FloatingActionButtonLayout_fab_ly_folded_size, topFoldedSize);
			}

			if (attr.hasValue(R.styleable.FloatingActionButtonLayout_fab_ly_unfolded_size)) {
				topUnfoldedSize = attr.getDimensionPixelOffset(R.styleable.FloatingActionButtonLayout_fab_ly_unfolded_size, topUnfoldedSize);
			}
		}
	}

	private void addChildScrollListener(View child) {
		if (child != null && fabView != null && child != fabView && child.getId() != topId && child.getId() == scrollableId) {
			scrolledChild = child;
			child.getViewTreeObserver().addOnScrollChangedListener(this);
		}
	}

	private void createFabView() {
		fabView = new FloatingActionButton(getContext());

		fabView.setOnClickListener(fabClickListener);
		setFabTag();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (topView != null && fabVisible && fabView != null) {

			int bottom = topView.getBottom();

			if (bottom > 0) {
				int int16 = getResources().getDimensionPixelOffset(R.dimen.fab_gap_large);
				fabView.layout(r - fabView.getWidth() - int16, bottom - fabView.getHeight() / 2, r - int16, bottom + fabView.getHeight() / 2);
				removeView(fabView);
				addView(fabView);

				ViewCompat.setElevation(fabView, 6f);
			}
		}
	}

	public void setFabIcon(Drawable drawable) {
		if (fabView != null) {
			fabView.setIconDrawable(drawable);
		}
	}

	public void setFabColor(int color) {
		if (fabView != null) {
			fabView.setColorNormal(color);
		}
	}

	public void setFabColorPressed(int color) {
		if (fabView != null) {
			fabView.setColorPressed(color);
		}
	}

	public void setFabClickListener(OnClickListener fabClickListener, final String tag) {
		this.fabClickListener = fabClickListener;
		this.fabTag = tag;
		if (fabView != null) {
			fabView.setOnClickListener(fabClickListener);
			setFabTag();
		}
	}

	private void setFabTag() {
		if (fabView != null && fabTag != null) {
			fabView.setTag(fabTag);
			fabView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Toast.makeText(v.getContext(), String.valueOf(v.getTag()), Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}
	}

	public void setFabViewVisibility(int visibility, boolean forced) {
		forceVisbility = forced;
		if (fabView != null) {
			fabView.setVisibility(visibility);
		}
	}

	@Override
	public void onScrollChanged() {

		if (!forceVisbility && scrolledChild != null) {
			int scrollY = scrolledChild.getScrollY();

			int fabViewHeight = fabView != null ? fabView.getHeight() : 0;
			int minimScroll = fabViewHeight / 2;

			setFabClickListener(scrollY < minimScroll ? fabClickListener : null, "");

			float alpha = ((float) (255 - scrollY)) / 255f;

			if (scrollY < minimScroll) {
				if (fabView != null) {
					ViewCompat.setAlpha(fabView, alpha);
				}
				setFabViewVisibility(View.VISIBLE, false);
			} else {
				setFabViewVisibility(View.INVISIBLE, false);
			}

			if (fabScrollContentListener != null) {
				fabScrollContentListener.onScrollFactor(scrollY, alpha);
			}
		}
	}

	public void setFabScrollContentListener(FABScrollContentListener fabScrollContentListener) {
		this.fabScrollContentListener = fabScrollContentListener;
	}

	public void removeFab() {
		if (fabView != null) {
			removeView(fabView);
			fabView = null;
		}
	}

	public int getFabId() {
		return fabView != null ? fabView.getId() : 0;
	}

	public void setFold(boolean fold) {
		this.isFold = fold;
		if (fold) {
			fold();
		} else {
			unfold();
		}
	}

	public boolean isFold() {
		return isFold;
	}

	public void fold() {
		animFold(topFoldedSize);
	}

	public void unfold() {
		animFold(topUnfoldedSize);
	}

	private void animFold(int finalValue) {
		ValueAnimator animator = ValueAnimator.ofInt(topView.getMeasuredHeight(), finalValue);
		animator.addUpdateListener(new TopFoldAnimatorListener(topView));
		animator.setDuration(FOLD_DURATION);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();

	}

	public interface FABScrollContentListener {
		void onScrollFactor(int alpha, float factor);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		if (child.getId() == scrollableId) {
			scrolledChild = child;
			addChildScrollListener(scrolledChild);
		} else if (child.getId() == topId) {
			topView = child;
		} else {
			View viewTop = child.findViewById(topId);
			if (viewTop != null) {
				topView = viewTop;
			}
			View viewScroll = child.findViewById(scrollableId);
			if (viewScroll != null) {
				scrolledChild = viewScroll;
			}
		}
	}

	private class TopFoldAnimatorListener implements ValueAnimator.AnimatorUpdateListener {
		private View animatedView;

		private TopFoldAnimatorListener(View animatedView) {
			this.animatedView = animatedView;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int height = (Integer) animation.getAnimatedValue();
			ViewGroup.LayoutParams params = animatedView.getLayoutParams();
			params.height = height;
			animatedView.setLayoutParams(params);
		}
	}
}
