package com.getbase.floatingactionbutton.implement;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.getbase.floatingactionbutton.interfaces.ToggleInterface;

/**
 * Created by liurongchan on 14/11/11.
 */
public class ToggleImpl implements ToggleInterface {

    private boolean mVisible = true;

    private static final int TRANSLATE_DURATION_MILLIS = 200;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    @Override
    public void toggle(final View view, final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = view.getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = view.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = view.getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(view, visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom(view);
            if (animate) {
                view.animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                view.setTranslationY(translationY);
            }
        }
    }

    @Override
    public void show(boolean animate) {

    }

    @Override
    public void hide(boolean animate) {

    }

    public int getMarginBottom(View view) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }
}
