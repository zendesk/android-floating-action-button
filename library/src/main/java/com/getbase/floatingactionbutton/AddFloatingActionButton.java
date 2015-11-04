package com.getbase.floatingactionbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;

public class AddFloatingActionButton extends FloatingActionButton {
  int mPlusColor;

  public AddFloatingActionButton(Context context) {
    this(context, null);
  }

  public AddFloatingActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AddFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  void init(Context context, AttributeSet attributeSet) {
    TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.AddFloatingActionButton, 0, 0);
    mPlusColor = attr.getColor(R.styleable.AddFloatingActionButton_fab_plusIconColor, getColor(android.R.color.white));
    attr.recycle();

    super.init(context, attributeSet);
  }

  /**
   * @return the current Color of plus icon.
   */
  public int getPlusColor() {
    return mPlusColor;
  }

  public void setPlusColorResId(@ColorRes int plusColor) {
    setPlusColor(getColor(plusColor));
  }

  public void setPlusColor(int color) {
    if (mPlusColor != color) {
      mPlusColor = color;
      updateBackground();
    }
  }

 //Not Needed because we check to see if there is a drawable if not we use default (fab_add)
 //    @Override
//    public void setIcon(@DrawableRes int icon) {
//        if (mIcon != icon) {
//            mIcon = icon;
//            mAddIconDrawable = null;
//            updateBackground();
//        }
//    }
//
//    @Override
//    Drawable getIconDrawable() {
//
//        final float iconSize = getDimension(R.dimen.fab_icon_size);
//        final float iconHalfSize = iconSize / 2f;
//
//        final float plusSize = getDimension(R.dimen.fab_plus_icon_size);
//        final float plusHalfStroke = getDimension(R.dimen.fab_plus_icon_stroke) / 2f;
//        final float plusOffset = (iconSize - plusSize) / 2f;
//
//        final Shape shape = new Shape() {
//            @Override
//            public void draw(Canvas canvas, Paint paint) {
//                canvas.drawRect(plusOffset, iconHalfSize - plusHalfStroke, iconSize - plusOffset, iconHalfSize + plusHalfStroke, paint);
//                canvas.drawRect(iconHalfSize - plusHalfStroke, plusOffset, iconHalfSize + plusHalfStroke, iconSize - plusOffset, paint);
//            }
//        };
//
//        ShapeDrawable drawable = new ShapeDrawable(shape);
//
//        final Paint paint = drawable.getPaint();
//        paint.setColor(mPlusColor);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setAntiAlias(true);
//
//
//        return drawable;
//    }
}
