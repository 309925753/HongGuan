package com.redchamber.nocsroll;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MyNavigationLayoutContainer extends RelativeLayout {

    private Scroller scroll;

    public MyNavigationLayoutContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroll = new Scroller(context);
    }

    public void startScrollView(int width, int target) {
        int dx = -(target * width + getScrollX());
        scroll.startScroll(getScrollX(), 0, dx, 0);
        postInvalidate();
    }

    public void startScrollView(String str, int left) {
        if ("left".equals(str)) {
            scroll.startScroll(getScrollX(), 0, -getScrollX(), 0);
        } else if ("right".equals(str)) {
            scroll.startScroll(getScrollX(), 0, -getScrollX() - left, 0);
        }
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (scroll.computeScrollOffset()) {
            scrollTo(scroll.getCurrX(), scroll.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

}
