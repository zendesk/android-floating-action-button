package com.getbase.floatingactionbutton;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;

public class Label extends AppCompatTextView {

    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private int mShadowRadius;
    private int mShadowXOffset;
    private int mShadowYOffset;
    private int mShadowColor;
    private Drawable mBackgroundDrawable;
    private boolean mShowShadow = true;
    private int mRawWidth;
    private int mRawHeight;
    private int mColorNormal;
    private int mColorPressed;
    private int mColorRipple;
    private int mCornerRadius;
    private FloatingActionButton mFab;
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private boolean mUsingStyle;
    private boolean mHandleVisibilityChanges = true;

    public Label(Context context) {
        super(context);
    }

    public Label(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Label(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    private int calculateMeasuredWidth() {
        if (mRawWidth == 0) {
            mRawWidth = getMeasuredWidth();
        }
        return getMeasuredWidth() + calculateShadowWidth();
    }

    private int calculateMeasuredHeight() {
        if (mRawHeight == 0) {
            mRawHeight = getMeasuredHeight();
        }
        return getMeasuredHeight() + calculateShadowHeight();
    }

    int calculateShadowWidth() {
        return mShowShadow ? (mShadowRadius + Math.abs(mShadowXOffset)) : 0;
    }

    int calculateShadowHeight() {
        return mShowShadow ? (mShadowRadius + Math.abs(mShadowYOffset)) : 0;
    }

    void updateBackground() {
        LayerDrawable layerDrawable;
        if (mShowShadow) {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    new Shadow(),
                    createFillDrawable()
            });

            int leftInset = mShadowRadius + Math.abs(mShadowXOffset);
            int topInset = mShadowRadius + Math.abs(mShadowYOffset);
            int rightInset = (mShadowRadius + Math.abs(mShadowXOffset));
            int bottomInset = (mShadowRadius + Math.abs(mShadowYOffset));

            layerDrawable.setLayerInset(
                    1,
                    leftInset,
                    topInset,
                    rightInset,
                    bottomInset
            );
        } else {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    createFillDrawable()
            });
        }

        setBackgroundCompat(layerDrawable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createRectDrawable(mColorPressed));
        drawable.addState(new int[]{}, createRectDrawable(mColorNormal));

        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][]{{}},
                    new int[]{mColorRipple}), drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
            mBackgroundDrawable = ripple;
            return ripple;
        }

        mBackgroundDrawable = drawable;
        return drawable;
    }

    private Drawable createRectDrawable(int color) {
        RoundRectShape shape = new RoundRectShape(
                new float[]{
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius
                },
                null,
                null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private void setShadow(FloatingActionButton fab) {
        mShadowColor = fab.getShadowColor();
        mShadowRadius = fab.getShadowRadius();
        mShadowXOffset = fab.getShadowXOffset();
        mShadowYOffset = fab.getShadowYOffset();
        mShowShadow = fab.hasShadow();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    private void playShowAnimation() {
        if (mShowAnimation != null) {
            mHideAnimation.cancel();
            startAnimation(mShowAnimation);
        }
    }

    private void playHideAnimation() {
        if (mHideAnimation != null) {
            mShowAnimation.cancel();
            startAnimation(mHideAnimation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_pressed});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
//        setPressed(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
//        setPressed(false);
    }

    void setFab(FloatingActionButton fab) {
        mFab = fab;
        setShadow(fab);
    }

    void setShowShadow(boolean show) {
        mShowShadow = show;
    }

    void setCornerRadius(int cornerRadius) {
        mCornerRadius = cornerRadius;
    }

    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mColorNormal = colorNormal;
        mColorPressed = colorPressed;
        mColorRipple = colorRipple;
    }

    void show(boolean animate) {
        if (animate) {
            playShowAnimation();
        }
        setVisibility(VISIBLE);
    }

    void hide(boolean animate) {
        if (animate) {
            playHideAnimation();
        }
        setVisibility(INVISIBLE);
    }

    void setShowAnimation(Animation showAnimation) {
        mShowAnimation = showAnimation;
    }

    void setHideAnimation(Animation hideAnimation) {
        mHideAnimation = hideAnimation;
    }

    void setUsingStyle(boolean usingStyle) {
        mUsingStyle = usingStyle;
    }

    void setHandleVisibilityChanges(boolean handle) {
        mHandleVisibilityChanges = handle;
    }

    boolean isHandleVisibilityChanges() {
        return mHandleVisibilityChanges;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mFab == null || mFab.getOnClickListener() == null || !mFab.isEnabled()) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                onActionUp();
                mFab.onActionUp();
                break;

            case MotionEvent.ACTION_CANCEL:
                onActionUp();
                mFab.onActionUp();
                break;
        }

        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown();
            if (mFab != null) {
                mFab.onActionDown();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onActionUp();
            if (mFab != null) {
                mFab.onActionUp();
            }
            return super.onSingleTapUp(e);
        }
    });

    private class Shadow extends Drawable {

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);

        private Shadow() {
            this.init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColorNormal);

            mErase.setXfermode(PORTER_DUFF_CLEAR);

            if (!isInEditMode()) {
                mPaint.setShadowLayer(mShadowRadius, mShadowXOffset, mShadowYOffset, mShadowColor);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            RectF shadowRect = new RectF(
                    mShadowRadius + Math.abs(mShadowXOffset),
                    mShadowRadius + Math.abs(mShadowYOffset),
                    mRawWidth,
                    mRawHeight
            );

            canvas.drawRoundRect(shadowRect, mCornerRadius, mCornerRadius, mPaint);
            canvas.drawRoundRect(shadowRect, mCornerRadius, mCornerRadius, mErase);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}