package com.roamer.slidelistview.wrap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.roamer.slidelistview.wrap
 * @作者:王阳
 * @创建时间: 2015年10月15日 下午4:58:42
 * @描述: wrap the front view ,so we can handle motion event more simple,拦截时间分发处理
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容:
 */
public class FrontViewWrapLayout extends LinearLayout {
    private boolean isOpend;// whether the front view is opend

    public FrontViewWrapLayout(Context context) {
        super(context);
    }

    public FrontViewWrapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrontViewWrapLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // if the front view is opend,drop all motion event(include sub view)
        if (isOpend) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOpend(boolean isOpend) {
        this.isOpend = isOpend;
    }
}
