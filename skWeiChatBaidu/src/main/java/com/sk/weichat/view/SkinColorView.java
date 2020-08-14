package com.sk.weichat.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.sk.weichat.util.SkinUtils;

public class SkinColorView extends View {

    public SkinColorView(Context context) {
        super(context);
        init();
    }

    public SkinColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkinColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SkinColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        SkinUtils.Skin skin = SkinUtils.getSkin(getContext());
        setBackgroundColor(skin.getAccentColor());
    }
}
