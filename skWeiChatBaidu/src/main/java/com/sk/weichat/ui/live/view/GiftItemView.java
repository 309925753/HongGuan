package com.sk.weichat.ui.live.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.live.bean.GiftItem;

public class GiftItemView extends LinearLayout {
    Handler handler = new Handler();
    private ImageView avatar;
    private TextView name;
    private TextView giftName;
    private TextView giftNumTv;
    private ImageView giftIv;
    private GiftItem gift;
    private int giftNum = 1;
    private boolean isShow = false;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isShow = false;
            giftNum = 0;
            setVisibility(INVISIBLE);
        }
    };
    private OnAnimatorListener onAnimatorListener;

    public GiftItemView(Context context) {
        this(context, null);
    }

    public GiftItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnAnimatorListener(OnAnimatorListener onAnimatorListener) {
        this.onAnimatorListener = onAnimatorListener;
    }

    private void init() {
        setOrientation(VERTICAL);
        setVisibility(INVISIBLE);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_gift_message, null, false);
        avatar = (ImageView) convertView.findViewById(R.id.avatar);
        name = (TextView) convertView.findViewById(R.id.name);
        giftName = (TextView) convertView.findViewById(R.id.gift_name);
        giftIv = (ImageView) convertView.findViewById(R.id.gift_type);
        giftNumTv = (TextView) convertView.findViewById(R.id.gift_num);
        addView(convertView);
    }

    public void setGift(GiftItem gift) {
        this.gift = gift;
        refreshView();
    }

    /**
     * click one gift,if gifts not contain this gift,
     * refreshView():init giftView(now giftView is inVisible,giftNum is 0)
     * after init,
     * addNum():giftNum+,begin scale,removeItemMessage handler
     * giftView is isShow?if(not) show isShow = true
     * giftView visible,visible 3s postHandler
     * in 3s,click this gift again,refreshVie(not go)
     * giftNum++,reset 3s
     */
    public void refreshView() {
        if (gift == null) {
            return;
        }
        giftNum = gift.getGiftNum();// 0
        if (!TextUtils.isEmpty(gift.getGiftUi())) {
            AvatarHelper.getInstance().displayAvatar(gift.getGiftUi(), avatar, false);
        } else {
            avatar.setImageResource(R.drawable.default_head);
        }
        name.setText(gift.getGiftUn());
        giftName.setText(gift.getName());
        if (TextUtils.isEmpty(gift.getPhoto())) {
            giftIv.setVisibility(INVISIBLE);
        } else {
            giftIv.setVisibility(VISIBLE);
            AvatarHelper.getInstance().displayUrl(gift.getPhoto(), giftIv);
        }
        giftNumTv.setText("x" + gift.getGiftNum());
        scaleView(giftNumTv, 200);
    }

    public void addNum(int num) {
        giftNum += num;
        giftNumTv.setText("x" + giftNum);
        scaleView(giftNumTv, 200);
        // removeItemMessage handler,not to be invisible
        handler.removeCallbacks(runnable);
        if (!isShow()) {
            show();
        }
        // loading 3s,if not add,post handler,to be visible
        handler.postDelayed(runnable, 3000);
    }

    /**
     * @param view
     * @param duration
     */
    public void scaleView(View view, long duration) {
        // 组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 2f, 1f);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new LinearInterpolator());
        // 两个动画同时开始
        animatorSet.play(scaleY).with(scaleX);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (onAnimatorListener != null) {
                    onAnimatorListener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (onAnimatorListener != null) {
                    onAnimatorListener.onAnimationEnd(gift);
                }
            }
        });
    }

    public void show() {
        isShow = true;
        setVisibility(VISIBLE);
        handler.postDelayed(runnable, 3000);
    }

    public boolean isShow() {
        return isShow;
    }

    public interface OnAnimatorListener {
        void onAnimationStart(Animator animation);

        void onAnimationEnd(GiftItem gift);
    }
}
