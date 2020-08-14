package com.sk.weichat.view.likeView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.weichat.R;

public class LikeAnimationView extends FrameLayout {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private boolean isChecked = true;
    private ImageView ivLike;
    private DotsView vDotsView;
    private CircleView vCircleView;
    private AnimatorSet animatorSet;

    public LikeAnimationView(@NonNull Context context) {
        super(context);
        init();
    }

    public LikeAnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LikeAnimationView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_like_button, this, true);
        ivLike = (ImageView) findViewById(R.id.ivLike);
        vCircleView = (CircleView) findViewById(R.id.vCircle);
        vDotsView = findViewById(R.id.vDotsView);
    }

    public void start() {
        ivLike.setImageResource(R.drawable.ic_likes1);
        ivLike.setEnabled(false);
        ivLike.animate().cancel();
        ivLike.setScaleX(0);
        ivLike.setScaleY(0);
        vCircleView.setInnerCircleRadiusProgress(0);
        vCircleView.setOuterCircleRadiusProgress(0);
        vDotsView.setCurrentProgress(0);

        animatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat
                (vCircleView, "outerCircleRadiusProgress", 0f, 1f);
        outerCircleAnimator.setDuration(550);
        outerCircleAnimator.setStartDelay(0);
        outerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);
        //目标属性的属性名、初始值或结束值
        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(vCircleView, "innerCircleRadiusProgress", 0f, 1f);
        innerCircleAnimator.setDuration(550);
        innerCircleAnimator.setStartDelay(0);
        innerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(ivLike, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(550);
        starScaleYAnimator.setStartDelay(0);
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(ivLike, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(550);
        starScaleXAnimator.setStartDelay(0);
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(vDotsView, "currentProgress", 0, 1f);
        dotsAnimator.setDuration(550);
        dotsAnimator.setStartDelay(100);
        dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                starScaleXAnimator,
                dotsAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivLike.setEnabled(true);
            }
        });

        animatorSet.start();
    }

    public void cancel() {
        ivLike.setImageResource(R.drawable.ic_likes);
        ivLike.setEnabled(false);
        ivLike.animate().cancel();
        ivLike.setScaleX(0);
        ivLike.setScaleY(0);
        vCircleView.setInnerCircleRadiusProgress(0);
        vCircleView.setOuterCircleRadiusProgress(0);
        vDotsView.setCurrentProgress(0);

        animatorSet = new AnimatorSet();

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(ivLike, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(550);
        starScaleYAnimator.setStartDelay(0);
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(ivLike, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(550);
        starScaleXAnimator.setStartDelay(0);
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        animatorSet.playTogether(
                starScaleYAnimator,
                starScaleXAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivLike.setEnabled(true);
            }
        });

        animatorSet.start();
    }

    public void setLikeStatus(int status) {
        if (status == 1) {// Like
            ivLike.setImageResource(R.drawable.ic_likes1);
        } else {
            ivLike.setImageResource(R.drawable.ic_likes);
        }
    }
}