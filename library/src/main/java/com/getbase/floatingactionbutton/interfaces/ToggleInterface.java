package com.getbase.floatingactionbutton.interfaces;


/**
 * Created by liurongchan on 14/11/11.
 */
public interface ToggleInterface {

    public void toggle(boolean visible, boolean animate, boolean force);

    public void show(boolean animate);

    public void hide(boolean animate);

    public int getMarginBottom();

}
