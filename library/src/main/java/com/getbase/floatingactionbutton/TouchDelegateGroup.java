package com.getbase.floatingactionbutton;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import java.util.ArrayList;

public class TouchDelegateGroup extends TouchDelegate {
  private static final Rect USELESS_HACKY_RECT = new Rect();
  private final ArrayList<TouchDelegate> mTouchDelegates = new ArrayList<TouchDelegate>();
  private TouchDelegate mCurrentTouchDelegate;
  private boolean mEnabled;

  public TouchDelegateGroup(View uselessHackyView) {
    super(USELESS_HACKY_RECT, uselessHackyView);
  }

  public void addTouchDelegate(@NonNull TouchDelegate touchDelegate) {
    mTouchDelegates.add(touchDelegate);
  }

  public void removeTouchDelegate(TouchDelegate touchDelegate) {
    mTouchDelegates.remove(touchDelegate);
    if (mCurrentTouchDelegate == touchDelegate) {
      mCurrentTouchDelegate = null;
    }
  }

  public void clearTouchDelegates() {
    mTouchDelegates.clear();
    mCurrentTouchDelegate = null;
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (!mEnabled) return false;

    TouchDelegate delegate = null;

    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
      for (int i = 0; i < mTouchDelegates.size(); i++) {
        TouchDelegate touchDelegate = mTouchDelegates.get(i);
        if (touchDelegate.onTouchEvent(event)) {
          mCurrentTouchDelegate = touchDelegate;
          return true;
        }
      }
      break;

    case MotionEvent.ACTION_MOVE:
      delegate = mCurrentTouchDelegate;
      break;

    case MotionEvent.ACTION_CANCEL:
    case MotionEvent.ACTION_UP:
      delegate = mCurrentTouchDelegate;
      mCurrentTouchDelegate = null;
      break;
    }

    return delegate != null && delegate.onTouchEvent(event);
  }

  public void setEnabled(boolean enabled) {
    mEnabled = enabled;
  }
}