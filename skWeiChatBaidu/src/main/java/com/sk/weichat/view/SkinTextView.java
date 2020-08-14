package com.sk.weichat.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.sk.weichat.R;
import com.sk.weichat.util.SkinUtils;

/**
 * 抽象出标题栏上的控件以实现根据皮肤切换深色浅色，
 */
public class SkinTextView extends AppCompatTextView {
    public SkinTextView(Context context) {
        super(context);
        init();
    }

    public SkinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkinTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SkinUtils.Skin skin = SkinUtils.getSkin(getContext());
        if (skin.isLight()) {
            setTextColor(getContext().getResources().getColorStateList(R.color.black));
        } else {
            setTextColor(getContext().getResources().getColorStateList(R.color.white));
        }
    }
}
