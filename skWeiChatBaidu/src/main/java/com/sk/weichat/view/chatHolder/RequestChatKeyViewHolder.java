package com.sk.weichat.view.chatHolder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.message.HandleSecureChatMessage;
import com.sk.weichat.util.ToastUtil;

class RequestChatKeyViewHolder extends AChatHolderInterface {

    ImageView requestIv;
    TextView requestTv;
    TextView sendTv;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_request_chat_key : R.layout.chat_to_item_request_chat_key;
    }

    @Override
    public void initView(View view) {
        requestIv = view.findViewById(R.id.request_iv);
        requestTv = view.findViewById(R.id.request_tv);
        sendTv = view.findViewById(R.id.tv_bottom);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        AvatarHelper.getInstance().displayAvatar(message.getFromUserName(), message.getFromUserId(), requestIv, true);
        requestTv.setText(getString(R.string.request_chat_key_group, message.getFromUserName()));

        if (message.getFileSize() == 2) {// 已经发送过了
            sendTv.setTextColor(MyApplication.getContext().getResources().getColor(R.color.Grey_500));
            sendTv.setOnClickListener(null);
        } else {
            sendTv.setTextColor(MyApplication.getContext().getResources().getColor(R.color.app_black));
            sendTv.setOnClickListener(v -> {
                if (TextUtils.equals(message.getFromUserId(), mLoginUserId)) {
                    ToastUtil.showToast(mContext, getString(R.string.self_cannot_send_chat_key_to_self));
                    return;
                }
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, mToUserId);
                if (friend != null && friend.getIsLostChatKeyGroup() == 1) {
                    ToastUtil.showToast(mContext, getString(R.string.you_cannot_send_chat_key_to_self));
                    return;
                }
                message.setFileSize(2);
                HandleSecureChatMessage.sendChatKeyForRequestedMember(message);
            });
        }
    }

    @Override
    public boolean isOnClick() {
        return false;
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
