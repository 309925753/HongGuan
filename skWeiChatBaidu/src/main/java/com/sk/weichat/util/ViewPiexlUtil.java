package com.sk.weichat.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

/**
 * Created by Administrator on 2016/4/11.
 */
public class ViewPiexlUtil {

    /**
     * 获取decorview的高度
     *
     * @param activity
     * @return
     */
    public static int getDecorViewHeight(Activity activity) {
        return activity.getWindow().getDecorView().getHeight();
    }

    public static int getContentBottomHeight(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        return ViewPiexlUtil.getDecorViewHeight(activity) - rect.top;//整体高度减去状态栏高度 减去actionbar高度
    }

    public static int px2dp(Context context, int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
