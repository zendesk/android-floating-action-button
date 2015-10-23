package com.getbase.floatingactionbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class FloatingActionsMenu extends ViewGroup {
  public static final int EXPAND_UP = 0;
  public static final int EXPAND_DOWN = 1;
  public static final int EXPAND_LEFT = 2;
  public static final int EXPAND_RIGHT = 3;

  public static final int LABELS_ON_LEFT_SIDE = 0;
  public static final int LABELS_ON_RIGHT_SIDE = 1;

  private static final int ANIMATION_DURATION = 300;
  private static final float COLLAPSED_PLUS_ROTATION = 0f;
  private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;

  private int mAddButtonPlusColor;
  private int mAddButtonColorNormal;
  private int mAddButtonColorPressed;
  private int mAddButtonSize;
  private boolean mAddButtonStrokeVisible;
  private int mExpandDirection;

  private int mButtonSpacing;
  private int mLabelsMargin;
  private int mLabelsVerticalOffset;

  private boolean mExpanded;

  private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AddFloatingActionButton mAddButton;
  private RotatingDrawable mRotatingDrawable;
  private int mMaxButtonWidth;
  private int mMaxButtonHeight;
  private int mLabelsStyle;
  private int mLabelsPosition;
  private int mButtonsCount;

  private TouchDelegateGroup mTouchDelegateGroup;

  private OnFloatingActionsMenuUpdateListener mListener;

  public interface OnFloatingActionsMenuUpdateListener {
    void onMenuExpanded();
    void onMenuCollapsed();
  }

  public FloatingActionsMenu(Context context) {
    this(context, null);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {
    mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));
    mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
    mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);

    mTouchDelegateGroup = new TouchDelegateGroup(this);
    setTouchDelegate(mTouchDelegateGroup);

    TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
    mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
    mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
    mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
    mAddButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, FloatingActionButton.SIZE_NORMAL);
    mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
    mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
    mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
    mLabelsPosition = attr.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, LABELS_ON_LEFT_SIDE);
    attr.recycle();

    if (mLabelsStyle != 0 && expandsHorizontally()) {
      throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
    }

    createAddButton(context);
  }

  public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
    mListener = listener;
  }

  private boolean expandsHorizontally() {
    return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
  }

  private static class RotatingDrawable extends LayerDrawable {
    public RotatingDrawable(Drawable drawable) {
      super(new Drawable[] { drawable });
    }

    private float mRotation;

    @SuppressWarnings("UnusedDeclaration")
    public float getRotation() {
      return mRotation;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRotation(float rotation) {
      mRotation = rotation;
      invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.save();
      canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
      super.draw(canvas);
      canvas.restore();
    }
  }

  private void createAddButton(Context context) {
    mAddButton = new AddFloatingActionButton(context) {
      @Override
      void updateBackground() {
        mPlusColor = mAddButtonPlusColor;
        mColorNormal = mAddButtonColorNormal;
        mColorPressed = mAddButtonColorPressed;
        mStrokeVisible = mAddButtonStrokeVisible;
        super.updateBackground();
      }

      @Override
      Drawable getIconDrawable() {
        final RotatingDrawable rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
        mRotatingDrawable = rotatingDrawable;

        final OvershootInterpolator interpolator = new OvershootInterpolator();

        final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", EXPANDED_PLUS_ROTATION, COLLAPSED_PLUS_ROTATION);
        final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", COLLAPSED_PLUS_ROTATION, EXPANDED_PLUS_ROTATION);

        collapseAnimator.setInterpolator(interpolator);
        expandAnimator.setInterpolator(interpolator);

        mExpandAnimation.play(expandAnimator);
        mCollapseAnimation.play(collapseAnimator);

        return rotatingDrawable;
      }
    };

    mAddButton.setId(R.id.fab_expand_menu_button);
    mAddButton.setSize(mAddButtonSize);
    mAddButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggle();
      }
    });

    addView(mAddButton, super.generateDefaultLayoutParams());
    mButtonsCount++;
  }

  public void addButton(FloatingActionButton button) {
    addView(button, mButtonsCount - 1);
    mButtonsCount++;

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  public void removeButton(FloatingActionButton button) {
    removeView(button.getLabelView());
    removeView(button);
    button.setTag(R.id.fab_label, null);
    mButtonsCount--;
  }

  private int getColor(@ColorRes int id) {
    return getResources().getColor(id);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int width = 0;
    int height = 0;

    mMaxButtonWidth = 0;
    mMaxButtonHeight = 0;
    int maxLabelWidth = 0;

    for (int i = 0; i < mButtonsCount; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      switch (mExpandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
        height += child.getMeasuredHeight();
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        width += child.getMeasuredWidth();
        mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
        break;
      }

      if (!expandsHorizontally()) {
        TextView label = (TextView) child.getTag(R.id.fab_label);
        if (label != null) {
          maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
        }
      }
    }

    if (!expandsHorizontally()) {
      width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0);
    } else {
      height = mMaxButtonHeight;
    }

    switch (mExpandDirection) {
    case EXPAND_UP:
    case EXPAND_DOWN:
      height += mButtonSpacing * (mButtonsCount - 1);
      height = adjustForOvershoot(height);
      break;
    case EXPAND_LEFT:
    case EXPAND_RIGHT:
      width += mButtonSpacing * (mButtonsCount - 1);
      width = adjustForOvershoot(width);
      break;
    }

    setMeasuredDimension(width, height);
  }

  private int adjustForOvershoot(int dimension) {
    return dimension * 12 / 10;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    switch (mExpandDirection) {
    case EXPAND_UP:
    case EXPAND_DOWN:
      boolean expandUp = mExpandDirection == EXPAND_UP;

      if (changed) {
        mTouchDelegateGroup.clearTouchDelegates();
      }

      int addButtonY = expandUp ? b - t - mAddButton.getMeasuredHeight() : 0;
      // Ensure mAddButton is centered on the line where the buttons should be
      int buttonsHorizontalCenter = mLabelsPosition == LABELS_ON_LEFT_SIDE
          ? r - l - mMaxButtonWidth / 2
          : mMaxButtonWidth / 2;
      int addButtonLeft = buttonsHorizontalCenter - mAddButton.getMeasuredWidth() / 2;
      mAddButton.layout(addButtonLeft, addButtonY, addButtonLeft + mAddButton.getMeasuredWidth(), addButtonY + mAddButton.getMeasuredHeight());

      int labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin;
      int labelsXNearButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
          ? buttonsHorizontalCenter - labelsOffset
          : buttonsHorizontalCenter + labelsOffset;

      int nextY = expandUp ?
          addButtonY - mButtonSpacing :
          addButtonY + mAddButton.getMeasuredHeight() + mButtonSpacing;

      for (int i = mButtonsCount - 1; i >= 0; i--) {
        final View child = getChildAt(i);

        if (child == mAddButton || child.getVisibility() == GONE) continue;

        int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
        int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
        child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

        float collapsedTranslation = addButtonY - childY;
        float expandedTranslation = 0f;

        child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
        child.setAlpha(mExpanded ? 1f : 0f);

        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
        params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
        params.setAnimationsTarget(child);

        View label = (View) child.getTag(R.id.fab_label);
        if (label != null) {
          int labelXAwayFromButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelsXNearButton - label.getMeasuredWidth()
              : labelsXNearButton + label.getMeasuredWidth();

          int labelLeft = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelXAwayFromButton
              : labelsXNearButton;

          int labelRight = mLabelsPosition == LABELS_ON_LEFT_SIDE
              ? labelsXNearButton
              : labelXAwayFromButton;

          int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

          label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

          Rect touchArea = new Rect(
              Math.min(childX, labelLeft),
              childY - mButtonSpacing / 2,
              Math.max(childX + child.getMeasuredWidth(), labelRight),
              childY + child.getMeasuredHeight() + mButtonSpacing / 2);
          mTouchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

          label.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
          label.setAlpha(mExpanded ? 1f : 0f);

          LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
          labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
          labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
          labelParams.setAnimationsTarget(label);
        }

        nextY = expandUp ?
            childY - mButtonSpacing :
            childY + child.getMeasuredHeight() + mButtonSpacing;
      }
      break;

    case EXPAND_LEFT:
    case EXPAND_RIGHT:
      boolean expandLeft = mExpandDirection == EXPAND_LEFT;

      int addButtonX = expandLeft ? r - l - mAddButton.getMeasuredWidth() : 0;
      // Ensure mAddButton is centered on the line where the buttons should be
      int addButtonTop = b - t - mMaxButtonHeight + (mMaxButtonHeight - mAddButton.getMeasuredHeight()) / 2;
      mAddButton.layout(addButtonX, addButtonTop, addButtonX + mAddButton.getMeasuredWidth(), addButtonTop + mAddButton.getMeasuredHeight());

      int nextX = expandLeft ?
          addButtonX - mButtonSpacing :
          addButtonX + mAddButton.getMeasuredWidth() + mButtonSpacing;

      for (int i = mButtonsCount - 1; i >= 0; i--) {
        final View child = getChildAt(i);

        if (child == mAddButton || child.getVisibility() == GONE) continue;

        int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
        int childY = addButtonTop + (mAddButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
        child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

        float collapsedTranslation = addButtonX - childX;
        float expandedTranslation = 0f;

        child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
        child.setAlpha(mExpanded ? 1f : 0f);

        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
        params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
        params.setAnimationsTarget(child);

        nextX = expandLeft ?
            childX - mButtonSpacing :
            childX + child.getMeasuredWidth() + mButtonSpacing;
      }

      break;
    }
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(super.generateDefaultLayoutParams());
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(super.generateLayoutParams(attrs));
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(super.generateLayoutParams(p));
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p);
  }

  private static Interpolator sExpandInterpolator = new OvershootInterpolator();
  private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
  private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

  private class LayoutParams extends ViewGroup.LayoutParams {

    private ObjectAnimator mExpandDir = new ObjectAnimator();
    private ObjectAnimator mExpandAlpha = new ObjectAnimator();
    private ObjectAnimator mCollapseDir = new ObjectAnimator();
    private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
    private boolean animationsSetToPlay;

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);

      mExpandDir.setInterpolator(sExpandInterpolator);
      mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
      mCollapseDir.setInterpolator(sCollapseInterpolator);
      mCollapseAlpha.setInterpolator(sCollapseInterpolator);

      mCollapseAlpha.setProperty(View.ALPHA);
      mCollapseAlpha.setFloatValues(1f, 0f);

      mExpandAlpha.setProperty(View.ALPHA);
      mExpandAlpha.setFloatValues(0f, 1f);

      switch (mExpandDirection) {
      case EXPAND_UP:
      case EXPAND_DOWN:
        mCollapseDir.setProperty(View.TRANSLATION_Y);
        mExpandDir.setProperty(View.TRANSLATION_Y);
        break;
      case EXPAND_LEFT:
      case EXPAND_RIGHT:
        mCollapseDir.setProperty(View.TRANSLATION_X);
        mExpandDir.setProperty(View.TRANSLATION_X);
        break;
      }
    }

    public void setAnimationsTarget(View view) {
      mCollapseAlpha.setTarget(view);
      mCollapseDir.setTarget(view);
      mExpandAlpha.setTarget(view);
      mExpandDir.setTarget(view);

      // Now that the animations have targets, set them to be played
      if (!animationsSetToPlay) {
        addLayerTypeListener(mExpandDir, view);
        addLayerTypeListener(mCollapseDir, view);

        mCollapseAnimation.play(mCollapseAlpha);
        mCollapseAnimation.play(mCollapseDir);
        mExpandAnimation.play(mExpandAlpha);
        mExpandAnimation.play(mExpandDir);
        animationsSetToPlay = true;
      }
    }

    private void addLayerTypeListener(Animator animator, final View view) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          view.setLayerType(LAYER_TYPE_NONE, null);
        }

        @Override
        public void onAnimationStart(Animator animation) {
          view.setLayerType(LAYER_TYPE_HARDWARE, null);
        }
      });
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    bringChildToFront(mAddButton);
    mButtonsCount = getChildCount();

    if (mLabelsStyle != 0) {
      createLabels();
    }
  }

  private void createLabels() {
    Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

    for (int i = 0; i < mButtonsCount; i++) {
      FloatingActionButton button = (FloatingActionButton) getChildAt(i);
      String title = button.getTitle();

      if (button == mAddButton || title == null ||
          button.getTag(R.id.fab_label) != null) continue;

      TextView label = new TextView(context);
      label.setTextAppearance(getContext(), mLabelsStyle);
      label.setText(button.getTitle());
      addView(label);

      button.setTag(R.id.fab_label, label);
    }
  }

  public void collapse() {
    collapse(false);
  }

  public void collapseImmediately() {
    collapse(true);
  }

  private void collapse(boolean immediately) {
    if (mExpanded) {
      mExpanded = false;
      mTouchDelegateGroup.setEnabled(false);
      mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
      mCollapseAnimation.start();
      mExpandAnimation.cancel();

      if (mListener != null) {
        mListener.onMenuCollapsed();
      }
    }
  }

  public void toggle() {
    if (mExpanded) {
      collapse();
    } else {
      expand();
    }
  }

  public void expand() {
    if (!mExpanded) {
      mExpanded = true;
      mTouchDelegateGroup.setEnabled(true);
      mCollapseAnimation.cancel();
      mExpandAnimation.start();

      if (mListener != null) {
        mListener.onMenuExpanded();
      }
    }
  }

  public boolean isExpanded() {
    return mExpanded;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    mAddButton.setEnabled(enabled);
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.mExpanded = mExpanded;

    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      mExpanded = savedState.mExpanded;
      mTouchDelegateGroup.setEnabled(mExpanded);

      if (mRotatingDrawable != null) {
        mRotatingDrawable.setRotation(mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
      }

      super.onRestoreInstanceState(savedState.getSuperState());
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  public static class SavedState extends BaseSavedState {
    public boolean mExpanded;

    public SavedState(Parcelable parcel) {
      super(parcel);
    }

    private SavedState(Parcel in) {
      super(in);
      mExpanded = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(mExpanded ? 1 : 0);
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
