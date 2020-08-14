package com.sk.weichat.view.chatHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.bumptech.glide.Glide;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.bean.PrivacySetting;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.broadcast.OtherBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.view.HeadView;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

import java.util.List;

public abstract class AChatHolderInterface implements View.OnLongClickListener, View.OnClickListener {

    public Context mContext;

    public boolean isMysend;
    public List<ChatMessage> chatMessages;
    public boolean isGounp; // 是否是群聊
    public boolean isDevice;// 是否为我的设备
    public boolean isMultiple; // 多选
    public boolean showPerson;
    public int position, mouseX, mouseY;
    public ChatHolderFactory.ChatHolderType mHolderType;

    public String mLoginUserId;
    public String mLoginNickName;
    public String mToUserId;
    public ChatMessage mdata;
    // 在群里的身份，为null表示没有身份，比如单聊，
    @Nullable
    public Integer selfGroupRole;

    public TextView mTvTime; // 时间
    public HeadView mIvHead; // 头像
    public TextView mTvName; // 名字
    public View mRootView; // 根布局
    public ImageView mIvFire; // 消息阅后即焚
    public TextView mTvSendState; // 消息发送状态
    public ImageView mIvFailed; // 消息发送失败感叹号
    public ProgressBar mSendingBar; // 发送中的转圈
    public CheckBox mCboxSelect; // 多选
    public ImageView ivUnRead; // 未读消息红点

    public ChatHolderListener mHolderListener;

    /**
     * 分析聊天holder的共性
     * 每一个holder都要去 findviewbyid
     * 90%的holder都要加载头像，处理时间控件,显示名字，消息发送状态
     */

    protected abstract int itemLayoutId(boolean isMysend);

    protected abstract void initView(View view);

    protected abstract void fillData(ChatMessage message);

    protected abstract void onRootClick(View v); // 重写此方法获得包裹布局的子view

    public int getLayoutId(boolean isMysend) {
        this.isMysend = isMysend;
        return itemLayoutId(isMysend);
    }

    public void findView(View convertView) {
        // 初始化 公共的布局
        if (enableNormal()) {
            inflateNormal(convertView);
        }

        // 初始化 子类view 自己的布局
        initView(convertView);

        if (enableUnRead()) {
            ivUnRead = convertView.findViewById(R.id.unread_img_view);
        }

        if (enableFire()) {
            mIvFire = convertView.findViewById(R.id.iv_fire);
        }
    }

    public void prepare(ChatMessage message, @Nullable Integer role, boolean secret) {
        mdata = message;
        if (enableNormal()) {
            // 显示消息状态
            changeMessageState(message);
            if (isDevice) {
                mIvHead.setRound(false);// 我的设备不裁剪为圆形
            }
            // 管理员角标
           // mIvHead.setGroupRole(role);

            String toId = message.getToId();
            String types = !TextUtils.isEmpty(toId) && toId.length() < 8 ? message.getFromId() : message.getFromUserId();
            AvatarHelper.getInstance().displayAvatar(message.getFromUserName(), types, mIvHead.getHeadImage(), false);
          // Glide.with(mContext).load(message.getFromUserName()).into(mIvHead.getHeadImage());
            // 显示昵称
            changeNickName(message, secret);

            mCboxSelect.setChecked(message.isMoreSelected);
        }

        fillData(message);

        if (mRootView != null) {
            if (isOnClick()) {
                mRootView.setOnClickListener(this);
            }

            if (isLongClick()) {
                mRootView.setOnTouchListener((v, event) -> {
                    mouseX = (int) event.getX();
                    mouseY = (int) event.getY();
                    return false;
                });
                mRootView.setOnLongClickListener(this);
            } else {
                mRootView.setOnLongClickListener(null);
            }
        }

        // 显示阅后即焚
        if (enableFire()) {
            mIvFire.setVisibility(message.getIsReadDel() ? View.VISIBLE : View.GONE);
        }

        // 开启自动发回执
        if (enableSendRead() && !message.getIsReadDel() && !isMysend) {
            sendReadMessage(message);
        }
    }

    private void changeNickName(ChatMessage message, boolean secret) {
        if (isGounp) {
            mTvName.setVisibility(isMysend ? View.GONE : View.VISIBLE);

            // Todo 有点多此一举，反而会造成其他昵称显示问题，已经在ChatContentView的chagneNameRemark方法内处理好了
/*
            if (!isMysend && !message.isLoadRemark()) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, message.getFromUserId());
                if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                    message.setFromUserName(friend.getRemarkName());
                }
                message.setLoadRemark(true);
            }
*/
            String name = message.getFromUserName();
            if (!TextUtils.isEmpty(name) && secret) {
                name = name.substring(0, name.length() - 1) + "*";
            }
            mTvName.setText(name);
        }
    }

    private void changeMessageState(ChatMessage message) {
        if (!isMysend && message.getMessageState() != ChatMessageListener.MESSAGE_SEND_SUCCESS) {
            message.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
        }

        int state = message.getMessageState();

        boolean read = false;
        if (ChatMessageListener.MESSAGE_SEND_SUCCESS == state) {
            // 单聊，我发的显示已读与未读 || 群聊，显示群已读人数
            if ((!isGounp && isMysend) || (isGounp && showPerson)) {
                read = true;
            }
            // 单聊还需要判断隐私设置内的设置
            if (!isGounp && read) {
                PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(mContext);
                read = privacySetting.getIsShowMsgState() == 1;
            }

            // 未读消息显示小红点
            if (enableUnRead()) {
                boolean show = message.isSendRead() || isMysend || message.getIsReadDel();
                ivUnRead.setVisibility(show ? View.GONE : View.VISIBLE);
            }

        } else {
            if (enableUnRead()) {
                ivUnRead.setVisibility(View.GONE);
            }
        }

        // todo 开关控制已读/未读显示与否
        changeVisible(mTvSendState, read);
        changeVisible(mIvFailed, state == ChatMessageListener.MESSAGE_SEND_FAILED);
        changeVisible(mSendingBar, state == ChatMessageListener.MESSAGE_SEND_ING);
        changeSendText(message);
    }

    private void changeSendText(ChatMessage message) {
        mTvSendState.setOnClickListener(null);
        if (isGounp) {
            if (showPerson) {
                int count = message.getReadPersons();
                mTvSendState.setText(count + getString(R.string.people));
                mTvSendState.setOnClickListener(this);
            }
        } else {
            if (message.isSendRead()) {
                mTvSendState.setText(R.string.status_read);
                mTvSendState.setTextColor(mContext.getResources().getColor(R.color.Grey_400));
            } else {
                mTvSendState.setText(R.string.unread);
                mTvSendState.setTextColor(mContext.getResources().getColor(R.color.Grey_800));
            }
        }
    }

    /**
     * 如果是一条普通的消息，那么就会有发送状态和头像等，如果是特殊的消息需要重写此方法并清空方法体
     *
     * @param view
     * @see SystemViewHolder
     */
    private void inflateNormal(View view) {
        mTvTime = view.findViewById(R.id.time_tv);
        mIvHead = view.findViewById(R.id.chat_head_iv);
        mTvName = view.findViewById(R.id.nick_name);
        mSendingBar = view.findViewById(R.id.progress);
        mIvFailed = view.findViewById(R.id.iv_failed);
        mTvSendState = view.findViewById(R.id.tv_read);

        mCboxSelect = view.findViewById(R.id.chat_msc);

        mIvHead.setOnClickListener(this);
        mIvFailed.setOnClickListener(this);
        mCboxSelect.setOnClickListener(this);
        if (isGounp) {
            mIvHead.setOnLongClickListener(this);
        }
    }

    public void showTime(String time) {
        if (mTvTime == null) {
            return;
        }

        if (!TextUtils.isEmpty(time)) {
            mTvTime.setVisibility(View.VISIBLE);
            mTvTime.setText(time);
        } else {
            mTvTime.setVisibility(View.GONE);
        }
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
        if (enableNormal()) {
            mCboxSelect.setVisibility(isMultiple ? View.VISIBLE : View.GONE);
        }
    }

    public void setShowPerson(boolean showPerson) {
        this.showPerson = showPerson;
    }

    public void changeVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public String getString(@StringRes int sid) {
        return mContext.getResources().getString(sid);
    }

    public String getString(@StringRes int sid, String splice) {
        return mContext.getResources().getString(sid, splice);
    }

    public int dp2px(float dpValue) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    @Override
    public void onClick(View v) {
        if (v == mRootView && !isMultiple) {
            // 让子类获得点击事件
            onRootClick(v);
        }
        callOnItemClick(v);
    }

    private void callOnItemClick(View v) {
        if (mHolderListener != null) {
            // 让外界获得点击事件
            mHolderListener.onItemClick(v, this, mdata);
        }
    }

    protected void callOnReplayClick(View v) {
        if (mHolderListener != null) {
            // 让外界获得点击事件
            mHolderListener.onReplayClick(v, this, mdata);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mHolderListener != null) {
            mHolderListener.onItemLongClick(v, this, mdata);
        }
        return true;
    }

    public void setBoxSelect(boolean select) {
        if (mCboxSelect != null) {
            // mCboxSelect.setVisibility(visible ? View.VISIBLE : View.GONE);
            mCboxSelect.setChecked(select);
        }
    }

    public void sendReadMessage(ChatMessage message) {
        if (ivUnRead != null) {
            ivUnRead.setVisibility(View.GONE);
        }

        if (message.isMySend() && !isDevice) {// 自己发送的消息不发已读
            return;
        }

        if (message.isSendRead()) {
            return;
        }

        // 群里的隐身人不发已读，
        if (!RoomMember.shouldSendRead(selfGroupRole)) {
            return;
        }

        if (isGounp && !showPerson) {
            message.setSendRead(true);
            ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, mToUserId, message.getPacketId(), true);
            return;
        }

        Intent intent = new Intent();
        intent.setAction(OtherBroadcast.Read);
        Bundle bundle = new Bundle();
        bundle.putString("packetId", message.getPacketId());
        bundle.putBoolean("isGroup", isGounp);
        if (message.getFromUserId().equals(message.getToUserId())) {// 我的设备
            bundle.putString("friendId", mLoginUserId);
        } else {
            bundle.putString("friendId", mToUserId);
        }
        bundle.putString("fromUserName", mLoginNickName);
        intent.putExtras(bundle);
        mContext.sendBroadcast(intent);
        message.setSendRead(true); // 自动发送的已读消息，先给一个已读标志，等有消息回执确认发送成功后在去修改数据库
    }

    // 默认关闭阅后即焚消息, 需要子类自行开启
    public boolean enableFire() {
        return false;
    }

    // 默认关闭未读消息显示红点功能,
    public boolean enableUnRead() {
        return false;
    }

    // 是否是普通消息 普通消息有发送状态，头像，昵称
    public boolean enableNormal() {
        return true;
    }

    // 默认开启长按事件，如果不需要可以子类重写 返回false
    public boolean isLongClick() {
        return true;
    }

    // 默认开启长按事件，如果不需要可以子类重写 返回false
    public boolean isOnClick() {
        return true;
    }

    // 默认关闭自动发送已读消息
    public boolean enableSendRead() {
        return false;
    }

    public void addChatHolderListener(ChatHolderListener listener) {
        mHolderListener = listener;
    }
}
