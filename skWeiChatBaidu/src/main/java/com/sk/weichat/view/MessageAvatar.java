//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sk.weichat.view;

import android.content.Context;
import android.util.AttributeSet;

import com.sk.weichat.bean.Friend;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.CoreManager;

public class MessageAvatar extends HeadView {

    public MessageAvatar(Context context) {
        super(context);
    }

    public MessageAvatar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageAvatar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();
        int dimen = Math.min(width, height);
        this.setMeasuredDimension(dimen, dimen);
    }

    public void fillData(Friend friend) {
        AvatarHelper.getInstance().displayAvatar(CoreManager.requireSelf(getContext()).getUserId(), friend, this);
    }
}
