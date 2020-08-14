package com.sk.weichat.view.chatHolder;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.sk.weichat.R;
import com.sk.weichat.bean.event.MessageEventClickable;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.ChatMessageDao;

import de.greenrobot.event.EventBus;

class ShakeViewHolder extends AChatHolderInterface {

    ImageView mImageView;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_shake : R.layout.chat_to_item_shake;
    }

    @Override
    public void initView(View view) {
        mImageView = view.findViewById(R.id.chat_image);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        mImageView.setBackgroundResource(message.isMySend() ? R.drawable.shake_frame : R.drawable.shake_frame_f);

        if (!message.isDownload()) {
            message.setDownload(true);
            AnimationDrawable animationDrawable = (AnimationDrawable) mImageView.getBackground();
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            animationDrawable.start();
            EventBus.getDefault().post(new MessageEventClickable(mdata));
        }

        if (message.isMySend()) {
            ChatMessageDao.getInstance().updateMessageShakeState(mLoginUserId, message.getToUserId(), message.getPacketId());
        } else {
            ChatMessageDao.getInstance().updateMessageShakeState(mLoginUserId, message.getFromUserId(), message.getPacketId());
        }
    }

    @Override
    protected void onRootClick(View v) {
        EventBus.getDefault().post(new MessageEventClickable(mdata));
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
