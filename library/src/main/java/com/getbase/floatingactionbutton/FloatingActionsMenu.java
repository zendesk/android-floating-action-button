package com.getbase.floatingactionbutton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class FloatingActionsMenu extends ViewGroup {
    /**
     * The constant EXPAND_UP.
     */
    public static final int EXPAND_UP = 0;
    /**
     * The constant EXPAND_DOWN.
     */
    public static final int EXPAND_DOWN = 1;
    /**
     * The constant EXPAND_LEFT.
     */
    public static final int EXPAND_LEFT = 2;
    /**
     * The constant EXPAND_RIGHT.
     */
    public static final int EXPAND_RIGHT = 3;
    /**
     * The constant ANIMATION_DURATION.
     */
    private static final int ANIMATION_DURATION = 300;
    /**
     * The constant COLLAPSED_PLUS_ROTATION.
     */
    private static final float COLLAPSED_PLUS_ROTATION = 0f;
    /**
     * The constant EXPANDED_PLUS_ROTATION.
     */
    private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;
    /**
     * M add button color normal.
     */
    private int mAddButtonColorNormal;
    /**
     * M add button color pressed.
     */
    private int mAddButtonColorPressed;
    /**
     * M add button size.
     */
    private int mAddButtonSize;
    /**
     * M add button stroke visible.
     */
    private boolean mAddButtonStrokeVisible;
    /**
     * M expand direction.
     */
    private int mExpandDirection;
    /**
     * M button spacing.
     */
    private int mButtonSpacing;
    /**
     * M labels margin.
     */
    private int mLabelsMargin;
    /**
     * M labels vertical offset.
     */
    private int mLabelsVerticalOffset;
    /**
     * M expanded.
     */
    private boolean mExpanded;

    /**
     * M expand animation.
     */
    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    /**
     * M collapse animation.
     */
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    /**
     * M add button.
     */
    private FloatingActionButton mAddButton;
    /**
     * M rotating drawable.
     */
    private Drawable mRotatingDrawable;
    /**
     * M max button width.
     */
    private int mMaxButtonWidth;
    /**
     * M max button height.
     */
    private int mMaxButtonHeight;
    /**
     * M labels style.
     */
    private int mLabelsStyle;
    /**
     * M buttons count.
     */
    private int mButtonsCount;
    /**
     * M menu icon.
     */
    @DrawableRes
    private int mMenuIcon;
    /**
     * M icon drawable.
     */
    private Drawable mIconDrawable;
    /**
     * M rotate icon.
     */
    private boolean mRotateIcon;
    /**
     * M listener.
     */
    private OnFloatingActionsMenuUpdateListener mListener;

    /**
     * The interface On floating actions menu update listener.
     */
    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();

        void onMenuCollapsed();
    }

    /**
     * Instantiates a new Floating actions menu.
     *
     * @param context
     *         the context
     */
    public FloatingActionsMenu(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Floating actions menu.
     *
     * @param context
     *         the context
     * @param attrs
     *         the attrs
     */
    public FloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Instantiates a new Floating actions menu.
     *
     * @param context
     *         the context
     * @param attrs
     *         the attrs
     * @param defStyle
     *         the def style
     */
    public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Init void.
     *
     * @param context
     *         context
     * @param attributeSet
     *         attribute set
     */
    private void init(Context context, AttributeSet attributeSet) {
        mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));
        mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
        mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
//        mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
        mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
        mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_light));
        mAddButtonSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, FloatingActionButton.SIZE_NORMAL);
        mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
        mMenuIcon = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_menu_icon, 0);
        mRotateIcon = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_rotate_icon, true);
        attr.recycle();

        if (mLabelsStyle != 0 && expandsHorizontally()) {
            throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
        }

        createAddButton(context);
    }

    /**
     * Sets on floating actions menu update listener.
     *
     * @param listener
     *         listener
     */
    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        mListener = listener;
    }

    /**
     * Expands horizontally.
     *
     * @return the boolean
     */
    private boolean expandsHorizontally() {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
    }

    /**
     * The type Rotating drawable.
     */
    private static class RotatingDrawable extends LayerDrawable {
        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[]{drawable});
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

    /**
     * Create add button.
     *
     * @param context
     *         context
     */
    private void createAddButton(Context context) {
        mAddButton = new FloatingActionButton(context) {
            @Override
            void updateBackground() {
//                mPlusColor = mAddButtonPlusColor;
                mColorNormal = mAddButtonColorNormal;
                mColorPressed = mAddButtonColorPressed;
                mStrokeVisible = mAddButtonStrokeVisible;
                mIcon = mMenuIcon;
                super.updateBackground();
            }

            @Override
            Drawable getIconDrawable() {
                Drawable rotatingDrawable;
                if (mRotateIcon) {
                    rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
                    mRotatingDrawable = rotatingDrawable;
                } else {
                    rotatingDrawable = super.getIconDrawable();
                }
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
    }

    /**
     * Add button.
     *
     * @param button
     *         button
     */
    public void addButton(FloatingActionButton button) {
        addView(button, mButtonsCount - 1);
        mButtonsCount++;

        if (mLabelsStyle != 0) {
            createLabels();
        }
    }

    /**
     * Gets color.
     *
     * @param id
     *         id
     * @return color
     */
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
                height += mButtonSpacing * (getChildCount() - 1);
                height = adjustForOvershoot(height);
                break;
            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                width += mButtonSpacing * (getChildCount() - 1);
                width = adjustForOvershoot(width);
                break;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Adjust for overshoot.
     *
     * @param dimension
     *         dimension
     * @return the int
     */
    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                boolean expandUp = mExpandDirection == EXPAND_UP;

                int addButtonY = expandUp ? b - t - mAddButton.getMeasuredHeight() : 0;
                // Ensure mAddButton is centered on the line where the buttons should be
                int addButtonLeft = r - l - mMaxButtonWidth + (mMaxButtonWidth - mAddButton.getMeasuredWidth()) / 2;
                mAddButton.layout(addButtonLeft, addButtonY, addButtonLeft + mAddButton.getMeasuredWidth(), addButtonY + mAddButton.getMeasuredHeight());

                int labelsRight = addButtonLeft - mLabelsMargin;

                int nextY = expandUp ?
                        addButtonY - mButtonSpacing :
                        addButtonY + mAddButton.getMeasuredHeight() + mButtonSpacing;

                for (int i = mButtonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mAddButton || child.getVisibility() == GONE) continue;

                    int childX = addButtonLeft + (mAddButton.getMeasuredWidth() - child.getMeasuredWidth()) / 2;
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
                        int labelLeft = labelsRight - label.getMeasuredWidth();
                        int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                        label.layout(labelLeft, labelTop, labelsRight, labelTop + label.getMeasuredHeight());

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

    /**
     * The constant sExpandInterpolator.
     */
    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    /**
     * The constant sCollapseInterpolator.
     */
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    /**
     * The constant sAlphaExpandInterpolator.
     */
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    /**
     * The type Layout params.
     */
    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDir = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();

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

            mExpandAnimation.play(mExpandAlpha);
            mExpandAnimation.play(mExpandDir);

            mCollapseAnimation.play(mCollapseAlpha);
            mCollapseAnimation.play(mCollapseDir);
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDir.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDir.setTarget(view);
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

    /**
     * Create labels.
     */
    private void createLabels() {
        Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

        for (int i = 0; i < mButtonsCount; i++) {
            FloatingActionButton button = (FloatingActionButton) getChildAt(i);
            String title = button.getTitle();

            if (button == mAddButton || title == null ||
                    button.getTag(R.id.fab_label) != null) continue;

            TextView label = new TextView(context);
            label.setText(button.getTitle());
            addView(label);

            button.setTag(R.id.fab_label, label);
        }
    }

    /**
     * Collapse void.
     */
    public void collapse() {
        if (mExpanded) {
            mExpanded = false;
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    /**
     * Toggle void.
     */
    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * Expand void.
     */
    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }

    /**
     * Is expanded.
     *
     * @return the boolean
     */
    public boolean isExpanded() {
        return mExpanded;
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

            if (mRotatingDrawable != null && mRotatingDrawable instanceof RotatingDrawable) {
                ((RotatingDrawable) mRotatingDrawable).setRotation(mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
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
