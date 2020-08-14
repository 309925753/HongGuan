package com.sk.weichat.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import com.sk.weichat.util.filter.EmojiInputFilter;

// 无下划线超链接，使用textColorLink、textColorHighlight分别修改超链接前景色和按下时的颜色
public class MyImageSpan extends DynamicDrawableSpan {

    private Drawable mDrawable;
    private Context mContext;
    private int mResourceId;

    public MyImageSpan(Context context, int resourceId) {
        this.mContext = context;
        this.mResourceId = resourceId;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = null;

        if (mDrawable != null) {
            drawable = mDrawable;
        } else {
            try {
                drawable = mContext.getResources().getDrawable(mResourceId);
                drawable.setBounds(0, 0,
                        DisplayUtil.dip2px(mContext, EmojiInputFilter.EMOJI_DRAWABLE_BOUND_SIZE_DP),
                        DisplayUtil.dip2px(mContext, EmojiInputFilter.EMOJI_DRAWABLE_BOUND_SIZE_DP));
            } catch (Exception e) {
                Log.e("sms", "Unable to find resource: " + mResourceId);
            }
        }
        return drawable;
    }
}
