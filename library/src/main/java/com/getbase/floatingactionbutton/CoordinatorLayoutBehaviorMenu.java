package com.getbase.floatingactionbutton;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by nilesh on 4/2/17.
 */

public class CoordinatorLayoutBehaviorMenu extends CoordinatorLayout.Behavior<FloatingActionsMenu> {


    public CoordinatorLayoutBehaviorMenu() {
        super();
    }

    public CoordinatorLayoutBehaviorMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionsMenu child, View dependency) {
        super.layoutDependsOn(parent, child, dependency);
        return dependency instanceof Snackbar.SnackbarLayout;
    }



    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionsMenu child, View dependency) {
        super.onDependentViewChanged(parent, child, dependency);
        int offset = (int) Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(offset);
        return true;
    }


}
