package com.sk.weichat.ui.tool;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.sk.weichat.R;
import com.sk.weichat.util.SkinUtils;

public class ButtonColorChange {
    public static void colorChange(Context context, View view) {
        SkinUtils.Skin skin = SkinUtils.getSkin(context);
        Drawable drawable = context.getResources().getDrawable(R.drawable.bg_btn_grey);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, skin.getButtonColorState());
        view.setBackground(drawable);
    }

    public static void rechargeChange(Context context, View view, int drawable1) {
        SkinUtils.Skin skin = SkinUtils.getSkin(context);
        Drawable drawable = context.getResources().getDrawable(drawable1);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, skin.getButtonColorState());
        view.setBackground(drawable);
    }

    public static void textChange(Context context, TextView view) {
        SkinUtils.Skin skin = SkinUtils.getSkin(context);
        view.setTextColor(skin.getAccentColor());
    }

    public static void checkChange(Context context, CheckBox view) {
        SkinUtils.Skin skin = SkinUtils.getSkin(context);
        ColorStateList colorStateList = skin.getButtonColorState();
        Drawable drawable = context.getResources().getDrawable(R.mipmap.choice_icon);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, colorStateList);
        view.setButtonDrawable(drawable);
    }

    public static void companyDrawable(TextView t) {
        Drawable drawable = DrawableCompat.wrap(t.getCompoundDrawables()[0]);
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{},
        };

        int[] colors = new int[]{
                0xffb1abab,
                0xff333333,
        };

        DrawableCompat.setTintList(drawable, new ColorStateList(states, colors));
        t.setCompoundDrawables(drawable, null, null, null);
    }
}
