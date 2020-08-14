package com.sk.weichat.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.util.Random;

public class AnimationUtil {
    private static final int rela1 = Animation.RELATIVE_TO_SELF;
    private static final int rela2 = Animation.RELATIVE_TO_PARENT;

    private static final int IN_FAED = 1;
    private static final int IN_SLIDE = 2;
    private static final int IN_SCALE = 3;
    private static final int IN_ROTATE = 4;
    private static final int IN_SCALE_ROTATE = 5;
    private static final int IN_SLIDE_FADE = 6;

    private static final int OUT_FAED = 11;
    private static final int OUT_SLIDE = 12;
    private static final int OUT_SCALE = 13;
    private static final int OUT_ROTATE = 14;
    private static final int OUT_SCALE_ROTATE = 15;
    private static final int OUT_SLIDE_FADE = 16;

    private static final int OUT_RIGHT_SLIDE = 17;

    private static Random random = new Random();

    public static int randomAnimationInIndex() {
        return random.nextInt(IN_SLIDE_FADE) + 1;
    }

    public static int getNextInAnimationIndex() {
        return IN_SLIDE;
    }

    public static int getNextOutAnimationIndex() {
        return OUT_FAED;
    }

    public static int getPreviousOutAnimationIndex() {
        return OUT_RIGHT_SLIDE;
    }

    public static int getPreviousInAnimationIndex() {
        return IN_FAED;
    }

    /**
     * 反转动画的Index，比如传进来的是in，返回对应的out
     *
     * @return
     */
    public static int reversalAnimationIndex(int index) {
        if (index > 10) {
            return index - 10;
        } else {
            return index + 10;
        }
    }

    public static Animation getAnimation(int index) {
        Animation animation = null;
        switch (index) {
            case IN_FAED:
                animation = new AlphaAnimation(0, 1);
                break;
            case IN_SLIDE:
                animation = new TranslateAnimation(rela2, 1, rela2, 0, rela2, 0, rela2, 0);
                break;
            case IN_SCALE:
                animation = new ScaleAnimation(0, 1, 0, 1, rela2, 0.5f, rela2, 0.5f);
                break;
            case IN_ROTATE:
                animation = new RotateAnimation(-90, 0, rela1, 0, rela1, 1);
                break;
            case IN_SCALE_ROTATE:
                animation = getScaleRotateIn();
                break;
            case IN_SLIDE_FADE:
                animation = getSlideFadeIn();
                break;
            //
            case OUT_FAED:
                animation = new AlphaAnimation(1, 0);
                break;
            case OUT_SLIDE:
                animation = new TranslateAnimation(rela2, 0, rela2, -1, rela2, 0, rela2, 0);
                break;
            case OUT_SCALE:
                animation = new ScaleAnimation(1, 0, 1, 0, rela2, 0.5f, rela2, 0.5f);
                break;
            case OUT_ROTATE:
                animation = new RotateAnimation(0, 90, rela1, 0, rela1, 1);
                break;
            case OUT_SCALE_ROTATE:
                animation = getScaleRotateOut();
                break;
            case OUT_SLIDE_FADE:
                animation = getSlideFadeOut();
                break;
            case OUT_RIGHT_SLIDE:
                animation = new TranslateAnimation(rela2, 0, rela2, 1, rela2, 0, rela2, 0);
                break;
        }

        if (animation != null) {
            animation.setDuration(1500);
        }
        return animation;
    }

    private static Animation getScaleRotateIn() {
        ScaleAnimation animation1 = new ScaleAnimation(0, 1, 0, 1, rela1, 0.5f, rela1, 0.5f);
        RotateAnimation animation2 = new RotateAnimation(0, 360, rela1, 0.5f, rela1, 0.5f);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(animation1);
        animation.addAnimation(animation2);
        return animation;
    }

    private static Animation getScaleRotateOut() {
        ScaleAnimation animation1 = new ScaleAnimation(1, 0, 1, 0, rela1, 0.5f, rela1, 0.5f);
        RotateAnimation animation2 = new RotateAnimation(0, 360, rela1, 0.5f, rela1, 0.5f);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(animation1);
        animation.addAnimation(animation2);
        return animation;
    }

    private static Animation getSlideFadeIn() {
        TranslateAnimation animation1 = new TranslateAnimation(rela2, 1, rela2, 0, rela2, 0, rela2, 0);
        AlphaAnimation animation2 = new AlphaAnimation(0, 1);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(animation1);
        animation.addAnimation(animation2);
        return animation;
    }

    private static Animation getSlideFadeOut() {
        TranslateAnimation animation1 = new TranslateAnimation(rela2, 0, rela2, -1, rela2, 0, rela2, 0);
        AlphaAnimation animation2 = new AlphaAnimation(1, 0);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(animation1);
        animation.addAnimation(animation2);
        return animation;
    }

    public static void setVisible(View view) {
        TranslateAnimation showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(500);
        view.startAnimation(showAnim);
        view.setVisibility(View.VISIBLE);
    }

    public static void setGone(View view) {
        TranslateAnimation hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnim.setDuration(500);
        view.startAnimation(hideAnim);
        view.setVisibility(View.GONE);
    }
}
