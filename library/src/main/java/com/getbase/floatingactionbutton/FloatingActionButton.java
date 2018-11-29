package com.getbase.floatingactionbutton;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getbase.floatingactionbutton.R;

public class FloatingActionButton extends AppCompatImageButton {

    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;

    int mFabSize;
    boolean mShowShadow;
    int mShadowColor;
    int mShadowRadius = Util.dpToPx(getContext(), 4f);
    int mShadowXOffset = Util.dpToPx(getContext(), 1f);
    int mShadowYOffset = Util.dpToPx(getContext(), 3f);

    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static final long PAUSE_GROWING_TIME = 200;
    private static final double BAR_SPIN_CYCLE_TIME = 500;
    private static final int BAR_MAX_LENGTH = 270;

    private int mColorNormal;
    private int mColorPressed;
    private int mColorDisabled;
    private int mColorRipple;
    private Drawable mIcon;
    private int mIconSize = Util.dpToPx(getContext(), 24f);
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private String mLabelText;
    private OnClickListener mClickListener;
    private Drawable mBackgroundDrawable;
    private boolean mUsingElevation;
    private boolean mUsingElevationCompat;

    // Progress
    private boolean mProgressBarEnabled;
    private int mProgressWidth = Util.dpToPx(getContext(), 6f);
    private int mProgressColor;
    private int mProgressBackgroundColor;
    private boolean mShouldUpdateButtonPosition;
    private float mOriginalX = -1;
    private float mOriginalY = -1;
    private boolean mButtonPositionSaved;
    private RectF mProgressCircleBounds = new RectF();
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mProgressIndeterminate;
    private long mLastTimeAnimated;
    private float mSpinSpeed = 195.0f; //The amount of degrees per second
    private long mPausedTimeWithoutGrowing = 0;
    private double mTimeStartGrowing;
    private boolean mBarGrowingFromFront = true;
    private int mBarLength = 16;
    private float mBarExtraLength;
    private float mCurrentProgress;
    private float mTargetProgress;
    private int mProgress;
    private boolean mAnimateProgress;
    private boolean mShouldProgressIndeterminate;
    private boolean mShouldSetProgress;
    private int mProgressMax = 100;
    private boolean mShowProgressBackground;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
        mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, 0xFFDA4336);
        mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, 0xFFE75043);
        mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, 0xFFAAAAAA);
        mColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple, 0x99FFFFFF);
        mShowShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true);
        mShadowColor = attr.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000);
        mShadowRadius = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowRadius, mShadowRadius);
        mShadowXOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowXOffset, mShadowXOffset);
        mShadowYOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowYOffset, mShadowYOffset);
        mFabSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
        mLabelText = attr.getString(R.styleable.FloatingActionButton_fab_label);
        mShouldProgressIndeterminate = attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_indeterminate, false);
        mProgressColor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_color, 0xFF009688);
        mProgressBackgroundColor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_backgroundColor, 0x4D000000);
        mProgressMax = attr.getInt(R.styleable.FloatingActionButton_fab_progress_max, mProgressMax);
        mShowProgressBackground = attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_showBackground, true);

        if (attr.hasValue(R.styleable.FloatingActionButton_fab_progress)) {
            mProgress = attr.getInt(R.styleable.FloatingActionButton_fab_progress, 0);
            mShouldSetProgress = true;
        }

        if (attr.hasValue(R.styleable.FloatingActionButton_fab_elevationCompat)) {
            float elevation = attr.getDimensionPixelOffset(R.styleable.FloatingActionButton_fab_elevationCompat, 0);
            if (isInEditMode()) {
                setElevation(elevation);
            } else {
                setElevationCompat(elevation);
            }
        }

        initShowAnimation(attr);
        initHideAnimation(attr);
        attr.recycle();

        if (isInEditMode()) {
            if (mShouldProgressIndeterminate) {
                setIndeterminate(true);
            } else if (mShouldSetProgress) {
                saveButtonOriginalPosition();
                setProgress(mProgress, false);
            }
        }

//        updateBackground();
        setClickable(true);
    }

    private void initShowAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up);
        mShowAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private void initHideAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down);
        mHideAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private int getCircleSize() {
        return getResources().getDimensionPixelSize(mFabSize == SIZE_NORMAL
                ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    private int calculateMeasuredWidth() {
        int width = getCircleSize() + calculateShadowWidth();
        if (mProgressBarEnabled) {
            width += mProgressWidth * 2;
        }
        return width;
    }

    private int calculateMeasuredHeight() {
        int height = getCircleSize() + calculateShadowHeight();
        if (mProgressBarEnabled) {
            height += mProgressWidth * 2;
        }
        return height;
    }

    int calculateShadowWidth() {
        return hasShadow() ? getShadowX() * 2 : 0;
    }

    int calculateShadowHeight() {
        return hasShadow() ? getShadowY() * 2 : 0;
    }

    private int getShadowX() {
        return mShadowRadius + Math.abs(mShadowXOffset);
    }

    private int getShadowY() {
        return mShadowRadius + Math.abs(mShadowYOffset);
    }

    private float calculateCenterX() {
        return (float) (getMeasuredWidth() / 2);
    }

    private float calculateCenterY() {
        return (float) (getMeasuredHeight() / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProgressBarEnabled) {
            if (mShowProgressBackground) {
                canvas.drawArc(mProgressCircleBounds, 360, 360, false, mBackgroundPaint);
            }

            boolean shouldInvalidate = false;

            if (mProgressIndeterminate) {
                shouldInvalidate = true;

                long deltaTime = SystemClock.uptimeMillis() - mLastTimeAnimated;
                float deltaNormalized = deltaTime * mSpinSpeed / 1000.0f;

                updateProgressLength(deltaTime);

                mCurrentProgress += deltaNormalized;
                if (mCurrentProgress > 360f) {
                    mCurrentProgress -= 360f;
                }

                mLastTimeAnimated = SystemClock.uptimeMillis();
                float from = mCurrentProgress - 90;
                float to = mBarLength + mBarExtraLength;

                if (isInEditMode()) {
                    from = 0;
                    to = 135;
                }

                canvas.drawArc(mProgressCircleBounds, from, to, false, mProgressPaint);
            } else {
                if (mCurrentProgress != mTargetProgress) {
                    shouldInvalidate = true;
                    float deltaTime = (float) (SystemClock.uptimeMillis() - mLastTimeAnimated) / 1000;
                    float deltaNormalized = deltaTime * mSpinSpeed;

                    if (mCurrentProgress > mTargetProgress) {
                        mCurrentProgress = Math.max(mCurrentProgress - deltaNormalized, mTargetProgress);
                    } else {
                        mCurrentProgress = Math.min(mCurrentProgress + deltaNormalized, mTargetProgress);
                    }
                    mLastTimeAnimated = SystemClock.uptimeMillis();
                }

                canvas.drawArc(mProgressCircleBounds, -90, mCurrentProgress, false, mProgressPaint);
            }

            if (shouldInvalidate) {
                invalidate();
            }
        }
    }

    private void updateProgressLength(long deltaTimeInMillis) {
        if (mPausedTimeWithoutGrowing >= PAUSE_GROWING_TIME) {
            mTimeStartGrowing += deltaTimeInMillis;

            if (mTimeStartGrowing > BAR_SPIN_CYCLE_TIME) {
                mTimeStartGrowing -= BAR_SPIN_CYCLE_TIME;
                mPausedTimeWithoutGrowing = 0;
                mBarGrowingFromFront = !mBarGrowingFromFront;
            }

            float distance = (float) Math.cos((mTimeStartGrowing / BAR_SPIN_CYCLE_TIME + 1) * Math.PI) / 2 + 0.5f;
            float length = BAR_MAX_LENGTH - mBarLength;

            if (mBarGrowingFromFront) {
                mBarExtraLength = distance * length;
            } else {
                float newLength = length * (1 - distance);
                mCurrentProgress += (mBarExtraLength - newLength);
                mBarExtraLength = newLength;
            }
        } else {
            mPausedTimeWithoutGrowing += deltaTimeInMillis;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        saveButtonOriginalPosition();

        if (mShouldProgressIndeterminate) {
            setIndeterminate(true);
            mShouldProgressIndeterminate = false;
        } else if (mShouldSetProgress) {
            setProgress(mProgress, mAnimateProgress);
            mShouldSetProgress = false;
        } else if (mShouldUpdateButtonPosition) {
            updateButtonPosition();
            mShouldUpdateButtonPosition = false;
        }
        super.onSizeChanged(w, h, oldw, oldh);

        setupProgressBounds();
        setupProgressBarPaints();
        updateBackground();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof ViewGroup.MarginLayoutParams && mUsingElevationCompat) {
            ((ViewGroup.MarginLayoutParams) params).leftMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).topMargin += getShadowY();
            ((ViewGroup.MarginLayoutParams) params).rightMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).bottomMargin += getShadowY();
        }
        super.setLayoutParams(params);
    }

    void updateBackground() {
        LayerDrawable layerDrawable;
        if (hasShadow()) {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    new Shadow(),
                    createFillDrawable(),
                    getIconDrawable()
            });
        } else {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    createFillDrawable(),
                    getIconDrawable()
            });
        }

        int iconSize = -1;
        if (getIconDrawable() != null) {
            iconSize = Math.max(getIconDrawable().getIntrinsicWidth(), getIconDrawable().getIntrinsicHeight());
        }
        int iconOffset = (getCircleSize() - (iconSize > 0 ? iconSize : mIconSize)) / 2;
        int circleInsetHorizontal = hasShadow() ? mShadowRadius + Math.abs(mShadowXOffset) : 0;
        int circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;

        if (mProgressBarEnabled) {
            circleInsetHorizontal += mProgressWidth;
            circleInsetVertical += mProgressWidth;
        }

        /*layerDrawable.setLayerInset(
                mShowShadow ? 1 : 0,
                circleInsetHorizontal,
                circleInsetVertical,
                circleInsetHorizontal,
                circleInsetVertical
        );*/
        layerDrawable.setLayerInset(
                hasShadow() ? 2 : 1,
                circleInsetHorizontal + iconOffset,
                circleInsetVertical + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetVertical + iconOffset
        );

        setBackgroundCompat(layerDrawable);
    }

    protected Drawable getIconDrawable() {
        if (mIcon != null) {
            return mIcon;
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createCircleDrawable(mColorDisabled));
        drawable.addState(new int[]{android.R.attr.state_pressed}, createCircleDrawable(mColorPressed));
        drawable.addState(new int[]{}, createCircleDrawable(mColorNormal));

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

    private Drawable createCircleDrawable(int color) {
        CircleDrawable shapeDrawable = new CircleDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    private void saveButtonOriginalPosition() {
        if (!mButtonPositionSaved) {
            if (mOriginalX == -1) {
                mOriginalX = getX();
            }

            if (mOriginalY == -1) {
                mOriginalY = getY();
            }

            mButtonPositionSaved = true;
        }
    }

    private void updateButtonPosition() {
        float x;
        float y;
        if (mProgressBarEnabled) {
            x = mOriginalX > getX() ? getX() + mProgressWidth : getX() - mProgressWidth;
            y = mOriginalY > getY() ? getY() + mProgressWidth : getY() - mProgressWidth;
        } else {
            x = mOriginalX;
            y = mOriginalY;
        }
        setX(x);
        setY(y);
    }

    private void setupProgressBarPaints() {
        mBackgroundPaint.setColor(mProgressBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mProgressWidth);

        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    private void setupProgressBounds() {
        int circleInsetHorizontal = hasShadow() ? getShadowX() : 0;
        int circleInsetVertical = hasShadow() ? getShadowY() : 0;
        mProgressCircleBounds = new RectF(
                circleInsetHorizontal + mProgressWidth / 2,
                circleInsetVertical + mProgressWidth / 2,
                calculateMeasuredWidth() - circleInsetHorizontal - mProgressWidth / 2,
                calculateMeasuredHeight() - circleInsetVertical - mProgressWidth / 2
        );
    }

    Animation getShowAnimation() {
        return mShowAnimation;
    }

    Animation getHideAnimation() {
        return mHideAnimation;
    }

    void playShowAnimation() {
        mHideAnimation.cancel();
        startAnimation(mShowAnimation);
    }

    void playHideAnimation() {
        mShowAnimation.cancel();
        startAnimation(mHideAnimation);
    }

    OnClickListener getOnClickListener() {
        return mClickListener;
    }

    Label getLabelView() {
        return (Label) getTag(R.id.fab_label);
    }

    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mColorNormal = colorNormal;
        mColorPressed = colorPressed;
        mColorRipple = colorRipple;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_enabled});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClickListener != null && isEnabled()) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label == null) return super.onTouchEvent(event);

            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;
            }
            mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionDown();
            }
            onActionDown();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionUp();
            }
            onActionUp();
            return super.onSingleTapUp(e);
        }
    });

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        ProgressSavedState ss = new ProgressSavedState(superState);

        ss.mCurrentProgress = this.mCurrentProgress;
        ss.mTargetProgress = this.mTargetProgress;
        ss.mSpinSpeed = this.mSpinSpeed;
        ss.mProgressWidth = this.mProgressWidth;
        ss.mProgressColor = this.mProgressColor;
        ss.mProgressBackgroundColor = this.mProgressBackgroundColor;
        ss.mShouldProgressIndeterminate = this.mProgressIndeterminate;
        ss.mShouldSetProgress = this.mProgressBarEnabled && mProgress > 0 && !this.mProgressIndeterminate;
        ss.mProgress = this.mProgress;
        ss.mAnimateProgress = this.mAnimateProgress;
        ss.mShowProgressBackground = this.mShowProgressBackground;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ProgressSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        ProgressSavedState ss = (ProgressSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.mCurrentProgress = ss.mCurrentProgress;
        this.mTargetProgress = ss.mTargetProgress;
        this.mSpinSpeed = ss.mSpinSpeed;
        this.mProgressWidth = ss.mProgressWidth;
        this.mProgressColor = ss.mProgressColor;
        this.mProgressBackgroundColor = ss.mProgressBackgroundColor;
        this.mShouldProgressIndeterminate = ss.mShouldProgressIndeterminate;
        this.mShouldSetProgress = ss.mShouldSetProgress;
        this.mProgress = ss.mProgress;
        this.mAnimateProgress = ss.mAnimateProgress;
        this.mShowProgressBackground = ss.mShowProgressBackground;

        this.mLastTimeAnimated = SystemClock.uptimeMillis();
    }

    private class CircleDrawable extends ShapeDrawable {

        private int circleInsetHorizontal;
        private int circleInsetVertical;

        private CircleDrawable() {
        }

        private CircleDrawable(Shape s) {
            super(s);
            circleInsetHorizontal = hasShadow() ? mShadowRadius + Math.abs(mShadowXOffset) : 0;
            circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;

            if (mProgressBarEnabled) {
                circleInsetHorizontal += mProgressWidth;
                circleInsetVertical += mProgressWidth;
            }
        }

        @Override
        public void draw(Canvas canvas) {
            setBounds(circleInsetHorizontal, circleInsetVertical, calculateMeasuredWidth()
                    - circleInsetHorizontal, calculateMeasuredHeight() - circleInsetVertical);
            super.draw(canvas);
        }
    }

    private class Shadow extends Drawable {

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mRadius;

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

            mRadius = getCircleSize() / 2;

            if (mProgressBarEnabled && mShowProgressBackground) {
                mRadius += mProgressWidth;
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mPaint);
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mErase);
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

    static class ProgressSavedState extends BaseSavedState {

        float mCurrentProgress;
        float mTargetProgress;
        float mSpinSpeed;
        int mProgress;
        int mProgressWidth;
        int mProgressColor;
        int mProgressBackgroundColor;
        boolean mProgressBarEnabled;
        boolean mProgressBarVisibilityChanged;
        boolean mProgressIndeterminate;
        boolean mShouldProgressIndeterminate;
        boolean mShouldSetProgress;
        boolean mAnimateProgress;
        boolean mShowProgressBackground;

        ProgressSavedState(Parcelable superState) {
            super(superState);
        }

        private ProgressSavedState(Parcel in) {
            super(in);
            this.mCurrentProgress = in.readFloat();
            this.mTargetProgress = in.readFloat();
            this.mProgressBarEnabled = in.readInt() != 0;
            this.mSpinSpeed = in.readFloat();
            this.mProgress = in.readInt();
            this.mProgressWidth = in.readInt();
            this.mProgressColor = in.readInt();
            this.mProgressBackgroundColor = in.readInt();
            this.mProgressBarVisibilityChanged = in.readInt() != 0;
            this.mProgressIndeterminate = in.readInt() != 0;
            this.mShouldProgressIndeterminate = in.readInt() != 0;
            this.mShouldSetProgress = in.readInt() != 0;
            this.mAnimateProgress = in.readInt() != 0;
            this.mShowProgressBackground = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(this.mCurrentProgress);
            out.writeFloat(this.mTargetProgress);
            out.writeInt((mProgressBarEnabled ? 1 : 0));
            out.writeFloat(this.mSpinSpeed);
            out.writeInt(this.mProgress);
            out.writeInt(this.mProgressWidth);
            out.writeInt(this.mProgressColor);
            out.writeInt(this.mProgressBackgroundColor);
            out.writeInt(this.mProgressBarVisibilityChanged ? 1 : 0);
            out.writeInt(this.mProgressIndeterminate ? 1 : 0);
            out.writeInt(this.mShouldProgressIndeterminate ? 1 : 0);
            out.writeInt(this.mShouldSetProgress ? 1 : 0);
            out.writeInt(this.mAnimateProgress ? 1 : 0);
            out.writeInt(this.mShowProgressBackground ? 1 : 0);
        }

        public static final Parcelable.Creator<ProgressSavedState> CREATOR =
                new Parcelable.Creator<ProgressSavedState>() {
                    public ProgressSavedState createFromParcel(Parcel in) {
                        return new ProgressSavedState(in);
                    }

                    public ProgressSavedState[] newArray(int size) {
                        return new ProgressSavedState[size];
                    }
                };
    }

    /* ===== API methods ===== */

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
        View label = (View) getTag(R.id.fab_label);
        if (label != null) {
            label.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onClick(FloatingActionButton.this);
                    }
                }
            });
        }
    }

    /**
     * Sets the size of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param size size of the <b>FloatingActionButton</b>. Accepted values: SIZE_NORMAL, SIZE_MINI.
     */
    public void setButtonSize(int size) {
        if (size != SIZE_NORMAL && size != SIZE_MINI) {
            throw new IllegalArgumentException("Use @FabSize constants only!");
        }

        if (mFabSize != size) {
            mFabSize = size;
            updateBackground();
        }
    }

    public int getButtonSize() {
        return mFabSize;
    }

    public void setColorNormal(int color) {
        if (mColorNormal != color) {
            mColorNormal = color;
            updateBackground();
        }
    }

    public void setColorNormalResId(int colorResId) {
        setColorNormal(getResources().getColor(colorResId));
    }

    public int getColorNormal() {
        return mColorNormal;
    }

    public void setColorPressed(int color) {
        if (color != mColorPressed) {
            mColorPressed = color;
            updateBackground();
        }
    }

    public void setColorPressedResId(int colorResId) {
        setColorPressed(getResources().getColor(colorResId));
    }

    public int getColorPressed() {
        return mColorPressed;
    }

    public void setColorRipple(int color) {
        if (color != mColorRipple) {
            mColorRipple = color;
            updateBackground();
        }
    }

    public void setColorRippleResId(int colorResId) {
        setColorRipple(getResources().getColor(colorResId));
    }

    public int getColorRipple() {
        return mColorRipple;
    }

    public void setColorDisabled(int color) {
        if (color != mColorDisabled) {
            mColorDisabled = color;
            updateBackground();
        }
    }

    public void setColorDisabledResId(int colorResId) {
        setColorDisabled(getResources().getColor(colorResId));
    }

    public int getColorDisabled() {
        return mColorDisabled;
    }

    public void setShowShadow(boolean show) {
        if (mShowShadow != show) {
            mShowShadow = show;
            updateBackground();
        }
    }

    public boolean hasShadow() {
        return !mUsingElevation && mShowShadow;
    }

    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowRadius(int dimenResId) {
        int shadowRadius = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowRadius != shadowRadius) {
            mShadowRadius = shadowRadius;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowRadiusDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowRadius(float shadowRadiusDp) {
        mShadowRadius = Util.dpToPx(getContext(), shadowRadiusDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowRadius() {
        return mShadowRadius;
    }

    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowXOffset(int dimenResId) {
        int shadowXOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowXOffset != shadowXOffset) {
            mShadowXOffset = shadowXOffset;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowXOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowXOffset(float shadowXOffsetDp) {
        mShadowXOffset = Util.dpToPx(getContext(), shadowXOffsetDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowXOffset() {
        return mShadowXOffset;
    }

    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowYOffset(int dimenResId) {
        int shadowYOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowYOffset != shadowYOffset) {
            mShadowYOffset = shadowYOffset;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowYOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowYOffset(float shadowYOffsetDp) {
        mShadowYOffset = Util.dpToPx(getContext(), shadowYOffsetDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowYOffset() {
        return mShadowYOffset;
    }

    public void setShadowColorResource(int colorResId) {
        int shadowColor = getResources().getColor(colorResId);
        if (mShadowColor != shadowColor) {
            mShadowColor = shadowColor;
            updateBackground();
        }
    }

    public void setShadowColor(int color) {
        if (mShadowColor != color) {
            mShadowColor = color;
            updateBackground();
        }
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    /**
     * Checks whether <b>FloatingActionButton</b> is hidden
     *
     * @return true if <b>FloatingActionButton</b> is hidden, false otherwise
     */
    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }

    /**
     * Makes the <b>FloatingActionButton</b> to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void show(boolean animate) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation();
            }
            super.setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the <b>FloatingActionButton</b> to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hide(boolean animate) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation();
            }
            super.setVisibility(INVISIBLE);
        }
    }

    public void toggle(boolean animate) {
        if (isHidden()) {
            show(animate);
        } else {
            hide(animate);
        }
    }

    public void setLabelText(String text) {
        mLabelText = text;
        TextView labelView = getLabelView();
        if (labelView != null) {
            labelView.setText(text);
        }
    }

    public String getLabelText() {
        return mLabelText;
    }

    public void setShowAnimation(Animation showAnimation) {
        mShowAnimation = showAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        mHideAnimation = hideAnimation;
    }

    public void setLabelVisibility(int visibility) {
        Label labelView = getLabelView();
        if (labelView != null) {
            labelView.setVisibility(visibility);
            labelView.setHandleVisibilityChanges(visibility == VISIBLE);
        }
    }

    public int getLabelVisibility() {
        TextView labelView = getLabelView();
        if (labelView != null) {
            return labelView.getVisibility();
        }

        return -1;
    }

    @Override
    public void setElevation(float elevation) {
        if (Util.hasLollipop() && elevation > 0) {
            super.setElevation(elevation);
            if (!isInEditMode()) {
                mUsingElevation = true;
                mShowShadow = false;
            }
            updateBackground();
        }
    }

    /**
     * Sets the shadow color and radius to mimic the native elevation.
     * <p>
     * <p><b>API 21+</b>: Sets the native elevation of this view, in pixels. Updates margins to
     * make the view hold its position in layout across different platform versions.</p>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setElevationCompat(float elevation) {
        mShadowColor = 0x26000000;
        mShadowRadius = Math.round(elevation / 2);
        mShadowXOffset = 0;
        mShadowYOffset = Math.round(mFabSize == SIZE_NORMAL ? elevation : elevation / 2);

        if (Util.hasLollipop()) {
            super.setElevation(elevation);
            mUsingElevationCompat = true;
            mShowShadow = false;
            updateBackground();

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        } else {
            mShowShadow = true;
            updateBackground();
        }
    }

    /**
     * <p>Change the indeterminate mode for the progress bar. In indeterminate
     * mode, the progress is ignored and the progress bar shows an infinite
     * animation instead.</p>
     *
     * @param indeterminate true to enable the indeterminate mode
     */
    public synchronized void setIndeterminate(boolean indeterminate) {
        if (!indeterminate) {
            mCurrentProgress = 0.0f;
        }

        mProgressBarEnabled = indeterminate;
        mShouldUpdateButtonPosition = true;
        mProgressIndeterminate = indeterminate;
        mLastTimeAnimated = SystemClock.uptimeMillis();
        setupProgressBounds();
//        saveButtonOriginalPosition();
        updateBackground();
    }

    public synchronized void setMax(int max) {
        mProgressMax = max;
    }

    public synchronized int getMax() {
        return mProgressMax;
    }

    public synchronized void setProgress(int progress, boolean animate) {
        if (mProgressIndeterminate) return;

        mProgress = progress;
        mAnimateProgress = animate;

        if (!mButtonPositionSaved) {
            mShouldSetProgress = true;
            return;
        }

        mProgressBarEnabled = true;
        mShouldUpdateButtonPosition = true;
        setupProgressBounds();
        saveButtonOriginalPosition();
        updateBackground();

        if (progress < 0) {
            progress = 0;
        } else if (progress > mProgressMax) {
            progress = mProgressMax;
        }

        if (progress == mTargetProgress) {
            return;
        }

        mTargetProgress = mProgressMax > 0 ? (progress / (float) mProgressMax) * 360 : 0;
        mLastTimeAnimated = SystemClock.uptimeMillis();

        if (!animate) {
            mCurrentProgress = mTargetProgress;
        }

        invalidate();
    }

    public synchronized int getProgress() {
        return mProgressIndeterminate ? 0 : mProgress;
    }

    public synchronized void hideProgress() {
        mProgressBarEnabled = false;
        mShouldUpdateButtonPosition = true;
        updateBackground();
    }

    public synchronized void setShowProgressBackground(boolean show) {
        mShowProgressBackground = show;
    }

    public synchronized boolean isProgressBackgroundShown() {
        return mShowProgressBackground;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Label label = (Label) getTag(R.id.fab_label);
        if (label != null) {
            label.setEnabled(enabled);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Label label = (Label) getTag(R.id.fab_label);
        if (label != null) {
            label.setVisibility(visibility);
        }
    }

    /**
     * <b>This will clear all AnimationListeners.</b>
     */
    public void hideButtonInMenu(boolean animate) {
        if (!isHidden() && getVisibility() != GONE) {
            hide(animate);

            Label label = getLabelView();
            if (label != null) {
                label.hide(animate);
            }

            getHideAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                    getHideAnimation().setAnimationListener(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    public void showButtonInMenu(boolean animate) {
        if (getVisibility() == VISIBLE) return;

        setVisibility(INVISIBLE);
        show(animate);
        Label label = getLabelView();
        if (label != null) {
            label.show(animate);
        }
    }

    /**
     * Set the label's background colors
     */
    public void setLabelColors(int colorNormal, int colorPressed, int colorRipple) {
        Label label = getLabelView();

        int left = label.getPaddingLeft();
        int top = label.getPaddingTop();
        int right = label.getPaddingRight();
        int bottom = label.getPaddingBottom();

        label.setColors(colorNormal, colorPressed, colorRipple);
        label.updateBackground();
        label.setPadding(left, top, right, bottom);
    }

    public void setLabelTextColor(int color) {
        getLabelView().setTextColor(color);
    }

    public void setLabelTextColor(ColorStateList colors) {
        getLabelView().setTextColor(colors);
    }
}