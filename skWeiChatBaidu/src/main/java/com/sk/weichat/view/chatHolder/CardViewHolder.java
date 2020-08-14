package com.sk.weichat.view.chatHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.other.BasicInfoActivity;

class CardViewHolder extends AChatHolderInterface {

    ImageView ivCardImage;
    TextView tvPersonName;
    TextView tvPersonSex;
    ImageView ivUnRead;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_card : R.layout.chat_to_item_card;
    }

    @Override
    public void initView(View view) {
        ivCardImage = view.findViewById(R.id.iv_card_head);
        tvPersonName = view.findViewById(R.id.person_name);
        tvPersonSex = view.findViewById(R.id.person_sex);
        ivUnRead = view.findViewById(R.id.unread_img_view);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        AvatarHelper.getInstance().displayAvatar(message.getContent(), message.getObjectId(), ivCardImage, true);
        tvPersonName.setText(String.valueOf(message.getContent()));

        if (!isMysend) {
            ivUnRead.setVisibility(message.isSendRead() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onRootClick(View v) {
        sendReadMessage(mdata);
        ivUnRead.setVisibility(View.GONE);
        BasicInfoActivity.start(mContext, mdata.getObjectId(), BasicInfoActivity.FROM_ADD_TYPE_CARD);
    }

    /**
     * 重写该方法，return true 表示显示红点
     *
     * @return
     */
    @Override
    public boolean enableUnRead() {
        return true;
    }
}
