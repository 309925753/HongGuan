package com.sk.weichat.audio;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;

public class RecordPopWindow {
    private Context mContext;
    private PopupWindow mPopup;
    private View mRootView;

    private TextView mVoiceSecondsTv;

    private LinearLayout mMicrophoneLinerLayout;
    private ImageView mMicrophoneImageView;
    private ImageView mMicrophoneLevelImageView;

    private ImageView mRubishVoiceImg;

    private TextView mVoiceTipTv;

    @SuppressWarnings("deprecation")
    public RecordPopWindow(Context context) {
        mContext = context;
        // mPopup.setFocusable(true);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.chat_voice_window, null);

        mVoiceSecondsTv = (TextView) mRootView.findViewById(R.id.voice_seconds);

        mMicrophoneLinerLayout = (LinearLayout) mRootView.findViewById(R.id.microphone_ll);
        mMicrophoneImageView = (ImageView) mRootView.findViewById(R.id.microphone_image_view);
        mMicrophoneLevelImageView = (ImageView) mRootView.findViewById(R.id.microphone_level_image_view);

        mRubishVoiceImg = (ImageView) mRootView.findViewById(R.id.rubish_voice);

        mVoiceTipTv = (TextView) mRootView.findViewById(R.id.voice_tip);

        // 推出声音窗口
        mPopup = new PopupWindow(mRootView);
        // 这样可以点击空白不消失窗口，
        mPopup.setFocusable(false);
        mPopup.setOutsideTouchable(false);
        mPopup.setAnimationStyle(android.R.style.Animation_Dialog);

        mPopup.setHeight(LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(LayoutParams.WRAP_CONTENT);
    }

    public void startRecord() {
        mMicrophoneLinerLayout.setVisibility(View.VISIBLE);
        mRubishVoiceImg.setVisibility(View.GONE);
        mVoiceTipTv.setText(R.string.motalk_voice_chat_tip_3);
        mVoiceSecondsTv.setText("0''");
    }

    // 音量等级
    public void setVoicePercent(int level) {
        int resId = mContext.getResources().getIdentifier("v" + level, "drawable", mContext.getPackageName());
        mMicrophoneLevelImageView.setImageResource(resId);
    }

    // 录制时间
    public void setVoiceSecond(int seconds) {
        mVoiceSecondsTv.setText(seconds + "s");
    }

    public boolean isRubishVoiceImgShow() {
        return mRubishVoiceImg.getVisibility() == View.VISIBLE;
    }

    /**
     * 手指上滑，取消发送
     */
    public void hideRubishTip() {
        mMicrophoneLinerLayout.setVisibility(View.VISIBLE);
        mRubishVoiceImg.setVisibility(View.GONE);
        mVoiceTipTv.setText(R.string.motalk_voice_chat_tip_3);
    }

    /**
     * 松开手指，取消发送
     */
    public void setRubishTip() {
        mMicrophoneLinerLayout.setVisibility(View.GONE);
        mRubishVoiceImg.setVisibility(View.VISIBLE);
        mVoiceTipTv.setText(R.string.motalk_voice_chat_tip_4);
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    public void show() {
        if (!mPopup.isShowing()) {
            try {
                mPopup.showAtLocation(((Activity) mContext).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            } catch (Exception e) {
                // 可能意外activity已经关闭之类的情况，
                Reporter.post("弹窗崩溃", e);
            }
        }
    }

    public void dismiss() {
        if (mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }
}
