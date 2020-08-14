package com.sk.weichat.view.redDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.RedDialogBean;
import com.sk.weichat.helper.AvatarHelper;

public class RedDialog extends Dialog {

    private RelativeLayout mRedRl;
    private ImageView mAvatarIv, mOpenIv, mCloseIv;
    private TextView mNameTv, mContentTv;
    private RedDialogBean mRedDialogBean;
    private boolean isMySend;
    private Context mContext;
    private TextView tv_red_tail;
    private FrameAnimation mFrameAnimation;
    private int[] mImgResIds = new int[]{
            R.mipmap.icon_open_red_packet1,
            R.mipmap.icon_open_red_packet2,
            R.mipmap.icon_open_red_packet3,
            R.mipmap.icon_open_red_packet4,
            R.mipmap.icon_open_red_packet5,
            R.mipmap.icon_open_red_packet6,
            R.mipmap.icon_open_red_packet7,
            R.mipmap.icon_open_red_packet7,
            R.mipmap.icon_open_red_packet8,
            R.mipmap.icon_open_red_packet9,
            R.mipmap.icon_open_red_packet4,
            R.mipmap.icon_open_red_packet10,
            R.mipmap.icon_open_red_packet11,
    };

    private OnClickRedListener mOnClickRedListener;

    public RedDialog(Context context, RedDialogBean redDialogBean, OnClickRedListener onClickRedListener) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.mRedDialogBean = redDialogBean;
        this.mOnClickRedListener = onClickRedListener;
    }

    public RedDialog(Context context, RedDialogBean redDialogBean, OnClickRedListener onClickRedListener, boolean isWho) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.mRedDialogBean = redDialogBean;
        this.mOnClickRedListener = onClickRedListener;
        this.isMySend = isWho;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_red_packet);
        initView();
        initData();
        initEvent();

        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        // lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    private void initView() {
        mRedRl = findViewById(R.id.rl_red);
        mAvatarIv = findViewById(R.id.iv_avatar);
        mNameTv = findViewById(R.id.tv_name);
        mContentTv = findViewById(R.id.tv_msg);
        mOpenIv = findViewById(R.id.iv_open);
        mCloseIv = findViewById(R.id.iv_close);
        tv_red_tail = findViewById(R.id.tv_red_tail);
        Log.e("zx", "initView: " + (mContext.getClass().toString().contains("MucChatActivity") && isMySend));
        tv_red_tail.setVisibility(mContext.getClass().toString().contains("MucChatActivity") && isMySend ? View.VISIBLE : View.GONE);

    }

    private void initData() {
        AvatarHelper.getInstance().displayAvatar(mRedDialogBean.getUserName(), mRedDialogBean.getUserId(),
                mAvatarIv, true);
        mNameTv.setText(MyApplication.getContext().getString(R.string.red_someone, mRedDialogBean.getUserName()));
        mContentTv.setText(mRedDialogBean.getWords());

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_red);
        mRedRl.setAnimation(animation);
    }

    private void initEvent() {
        tv_red_tail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickRedListener.clickTail();
                stopAnim();
                dismiss();
            }
        });
        mOpenIv.setOnClickListener(v -> {
            if (mFrameAnimation != null) {
                return;
            }
            startAnim();
            if (mOnClickRedListener != null) {
                mOnClickRedListener.clickRed();
            }
        });

        mCloseIv.setOnClickListener(v -> {
            stopAnim();
            dismiss();
        });
    }

    private void startAnim() {
        mFrameAnimation = new FrameAnimation(mOpenIv, mImgResIds, 125, true);
        mFrameAnimation.setAnimationListener(new FrameAnimation.AnimationListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationRepeat() {

            }

            @Override
            public void onAnimationPause() {
                mOpenIv.setBackgroundResource(R.mipmap.icon_open_red_packet1);
            }
        });
    }

    private void stopAnim() {
        if (mFrameAnimation != null) {
            mFrameAnimation.release();
            mFrameAnimation = null;
        }
    }

    public interface OnClickRedListener {
        void clickRed();

        void clickTail();
    }
}
