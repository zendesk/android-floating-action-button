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

    private View v;
    private static final int TRANSLATE_DURATION_MILLIS = 200;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public ToggleImpl(View v) {
        this.v = v;
    }

    @Override
    public void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = v.getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = v.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = v.getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                v.animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                v.setTranslationY(translationY);
            }
        }
    }

    @Override
    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    @Override
    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    public int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

}
