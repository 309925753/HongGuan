package com.sk.weichat.util.input;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.Nullable;

/**
 * https://blog.csdn.net/u012255016/article/details/50824766
 */
public class SoftKeyBoardListener {
    private View rootView;//activity的根视图
    private int rootViewFullHeight;//纪录根视图的初始化状态完整高度，
    private int rootViewVisibleHeight;//纪录根视图的显示高度
    @Nullable
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    private SoftKeyBoardListener(Window window) {
        //获取activity的根视图
        rootView = window.getDecorView();

        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int visibleHeight = r.height();
                if (rootViewVisibleHeight == 0) {
                    rootViewFullHeight = visibleHeight;
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight) {
                    return;
                }
                //可以看作软键盘显示了
                if (rootViewFullHeight - visibleHeight > 0) {
                    int keyboardHeight = rootViewFullHeight - visibleHeight;
                    KeyboardPanelUtil.setKeyboardHeight(rootView.getContext(), keyboardHeight);
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardShow(keyboardHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //可以看作软键盘隐藏了
                if (rootViewFullHeight == visibleHeight) {
                    int keyboardHeight = rootViewFullHeight - rootViewVisibleHeight;
                    KeyboardPanelUtil.setKeyboardHeight(rootView.getContext(), keyboardHeight);
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardHide(keyboardHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }
            }
        });
    }

    public static void setListener(Window window) {
        new SoftKeyBoardListener(window);
    }

    @SuppressWarnings("unused")
    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }
}