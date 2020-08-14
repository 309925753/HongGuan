package com.sk.weichat.view.chatHolder;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.HtmlUtils;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.StringUtils;
import com.sk.weichat.util.link.HttpTextView;

public class TextReplayViewHolder extends AChatHolderInterface {

    public HttpTextView mTvContent;
    public HttpTextView mTvReplayContent;
    public TextView tvFireTime;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_text_replay : R.layout.chat_to_item_text_replay;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mTvReplayContent = view.findViewById(R.id.chat_text_replay);
        mRootView = view.findViewById(R.id.chat_warp_view);
        if (!isMysend) {
            tvFireTime = view.findViewById(R.id.tv_fire_time);
        }
    }

    @Override
    public void fillData(ChatMessage message) {
        // 修改字体功能
        int size = PreferenceUtils.getInt(mContext, Constants.FONT_SIZE) + 14;
        mTvContent.setTextSize(size);
        mTvContent.setTextColor(mContext.getResources().getColor(R.color.black));

        String content = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(content, true);
        if (message.getIsReadDel() && !isMysend) {// 阅后即焚
            if (!message.isGroup() && !message.isSendRead()) {
                mTvContent.setText(R.string.tip_click_to_read);
                mTvContent.setTextColor(mContext.getResources().getColor(R.color.redpacket_bg));
            } else {
                // 已经查看了，当适配器再次刷新的时候，不需要重新赋值
                mTvContent.setText(charSequence);
            }
        } else {
            mTvContent.setText(charSequence);
        }

        if (!TextUtils.isEmpty(message.getObjectId())) {
            mTvReplayContent.setVisibility(View.VISIBLE);
            ChatMessage replayMessage = new ChatMessage(message.getObjectId());
            /**
             * 在此处对消息进行解密
             */
            if (replayMessage.getIsEncrypt() == 1) {
                try {
                    String decryptKey = Md5Util.toMD5(AppConfig.apiKey + replayMessage.getTimeSend() + replayMessage.getPacketId());
                    String decryptContent = DES.decryptDES(replayMessage.getContent(), decryptKey);
                    // 为chatMessage重新设值
                    replayMessage.setContent(decryptContent);
                } catch (Exception e) {
                    LogUtils.log(replayMessage.toJsonString());
                    Reporter.post("解密失败<" + replayMessage.getPacketId() + ">", e);
                }
            }
            SpannableStringBuilder sb = new SpannableStringBuilder()
                    .append(replayMessage.getFromUserName())
                    .append(": ")
                    .append(HtmlUtils.addSmileysToMessage(replayMessage.getSimpleContent(mContext), false));
            mTvReplayContent.setText(sb);
        } else {
            mTvReplayContent.setVisibility(View.GONE);
        }

        mTvReplayContent.setOnClickListener(v -> {
            mHolderListener.onReplayClick(v, this, mdata);
        });

        mTvContent.setUrlText(mTvContent.getText());
        mTvContent.setOnClickListener(v -> mHolderListener.onItemClick(mRootView, TextReplayViewHolder.this, mdata));
        mTvContent.setOnLongClickListener(v -> {
            mHolderListener.onItemLongClick(v, TextReplayViewHolder.this, mdata);
            return true;
        });
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableFire() {
        return true;
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    public void showFireTime(boolean show) {
        if (tvFireTime != null) {
            tvFireTime.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
