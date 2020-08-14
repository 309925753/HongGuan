package com.sk.weichat.ui.live;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.sk.weichat.util.ScreenUtil;

/**
 * 软键盘的监听
 */
public class KeyBoardShowListener {
    OnKeyboardVisibilityListener keyboardListener;
    private Context mContext;
    private View mEditLayout;
    private int mCount;

    public KeyBoardShowListener(Context context, View editLayout) {
        this.mContext = context;
        this.mEditLayout = editLayout;
    }

    public void setKeyBoardCount(int count) {
        this.mCount = count;
    }

    public void setKeyboardListener(final OnKeyboardVisibilityListener listener, Activity activity) {
        final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            // 软键盘高度
            private final int DefaultKeyboardDP = 100;
            // From @nathanielwolf answer... Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
            private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect mRect = new Rect();
            // 软键盘是否弹出
            private boolean wasOpened;

            @Override
            public void onGlobalLayout() {
                // Convert the dp to pixels.
                int estimatedKeyboardHeight = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, activityRootView.getResources().getDisplayMetrics());
                // 获取activityRootView在窗体的可视区域
                activityRootView.getWindowVisibleDisplayFrame(mRect);
                // 获取activityRootView在窗体的不可视区域高度(被其他View遮挡的区域高度,例如软键盘(软键盘实质为一个Dialog))
                int heightDiff = activityRootView.getRootView().getHeight() - (mRect.bottom - mRect.top);
                // 如果activityRootView在窗体的不可视区域高度大于或等于软键盘高度，则视为软键盘弹起
                boolean isShown = heightDiff >= estimatedKeyboardHeight;
                if (isShown == wasOpened) {
                    Log.e("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                wasOpened = isShown;
/*
                if (mCount != 1) {
                    // 软键盘第一次弹起，还是像之前一样挡住一半吧,如果用下面的方法，会发生很诡异的事情
                    if (isShown) {
                        int[] location = new int[2];
                        // 获取mEditLayout在窗体的坐标
                        mEditLayout.getLocationInWindow(location);
                        // 计算activityRootView滚动高度，使editLayout在可见区域
                        int scrollHeight = (location[1] + mEditLayout.getHeight()) - mRect.bottom;
                        activityRootView.scrollTo(0, scrollHeight);
                    } else {
                        activityRootView.scrollTo(0, 0);
                    }
                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ScreenUtil.dip2px(mContext, 48));
                    layoutParams.setMargins(0, 0, 0, heightDiff - mRect.top);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    mEditLayout.setLayoutParams(layoutParams);
                }
*/
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ScreenUtil.dip2px(mContext, 48));
                layoutParams.setMargins(0, 0, 0, heightDiff - mRect.top);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mEditLayout.setLayoutParams(layoutParams);
                listener.onVisibilityChanged(isShown);
            }
        });
    }

    public interface OnKeyboardVisibilityListener {

        void onVisibilityChanged(boolean visible);
    }
}
