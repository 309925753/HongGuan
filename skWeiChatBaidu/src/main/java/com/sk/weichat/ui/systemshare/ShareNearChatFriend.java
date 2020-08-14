package com.sk.weichat.ui.systemshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadEngine;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.InstantMessageConfirm;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadFrame;
import com.sk.weichat.view.MessageAvatar;
import com.sk.weichat.xmpp.ListenerManager;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

import java.util.List;
import java.util.UUID;

/**
 * 分享 最近联系人
 */
public class ShareNearChatFriend extends BaseActivity implements OnClickListener, ChatMessageListener {
    private ListView mShareLv;
    private List<Friend> mFriends;

    private InstantMessageConfirm menuWindow;
    private LoadFrame mLoadFrame;

    private ChatMessage mShareChatMessage;

    private boolean isNeedExecuteLogin;
    private BroadcastReceiver mShareBroadCast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getAction())
                    && intent.getAction().equals(ShareBroadCast.ACTION_FINISH_ACTIVITY)) {
                finish();
            }
        }
    };

    public ShareNearChatFriend() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_share);

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, SplashActivity.class));
            finish();
            return;
        }

        coreManager.relogin();// 连接xmpp 发消息需要

        mShareChatMessage = new ChatMessage();
        if (ShareUtil.shareInit(this, mShareChatMessage)) return;

        initActionBar();
        loadData();
        initView();

        ListenerManager.getInstance().addChatMessageListener(this);
        registerReceiver(mShareBroadCast, new IntentFilter(ShareBroadCast.ACTION_FINISH_ACTIVITY));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().removeChatMessageListener(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.most_recent_contact));
    }

    private void loadData() {
        mFriends = FriendDao.getInstance().getNearlyFriendMsg(coreManager.getSelf().getUserId());
        for (int i = 0; i < mFriends.size(); i++) {
            if (mFriends.get(i).getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {
                mFriends.remove(i);
            }
        }
    }

    private void initView() {
        findViewById(R.id.tv_create_newmessage).setOnClickListener(this);
        findViewById(R.id.ll_send_life_circle).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_send_life_circle).setOnClickListener(this);

        mShareLv = findViewById(R.id.lv_recently_message);
        mShareLv.setAdapter(new MessageRecentlyAdapter());
        mShareLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Friend friend = mFriends.get(position);
                showPopuWindow(view, friend);
            }
        });
    }

    private void showPopuWindow(View view, Friend friend) {
        if (menuWindow != null) {
            menuWindow.dismiss();
        }
        menuWindow = new InstantMessageConfirm(this, new ClickListener(friend), friend);
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        // 更新消息Fragment的广播
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
        if (mShareChatMessage != null && TextUtils.equals(mShareChatMessage.getPacketId(), msgId)) {
            if (messageState == ChatMessageListener.MESSAGE_SEND_SUCCESS) {// 发送成功
                if (mLoadFrame != null) {
                    mLoadFrame.change();
                }
            }
        }
    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        return false;
    }

    /**
     * 事件的监听
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_create_newmessage:
                verificationShare(1, null);
                break;
            case R.id.tv_send_life_circle:
                verificationShare(2, null);
                break;
            default:
                break;
        }
    }

    private void verificationShare(final int type, Friend friend) {
        if (type == 1) {// 选择好友
            Intent intent = getIntent();
            intent.setClass(ShareNearChatFriend.this, ShareNewFriend.class);
            startActivity(intent);
        } else if (type == 2) {// 生活圈
            Intent intent = getIntent();
            intent.setClass(ShareNearChatFriend.this, ShareLifeCircleProxyActivity.class);
            startActivity(intent);
        } else {// 直接发送
            share(friend);
        }
    }

    public void share(Friend friend) {
        if (friend.getRoomFlag() != 0) {
            if (friend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {// 禁言时间 > 当前时间 禁言还未结束
                DialogHelper.tip(mContext, getString(R.string.tip_forward_ban));
                return;
            } else if (friend.getGroupStatus() == 1) {
                DialogHelper.tip(mContext, getString(R.string.tip_forward_kick));
                return;
            } else if (friend.getGroupStatus() == 2) {
                DialogHelper.tip(mContext, getString(R.string.tip_forward_disbanded));
                return;
            } else if ((friend.getGroupStatus() == 3)) {
                DialogHelper.tip(mContext, getString(R.string.tip_group_disable_by_service));
                return;
            }
        }

        mLoadFrame = new LoadFrame(ShareNearChatFriend.this);
        mLoadFrame.setSomething(getString(R.string.back_last_page), getString(R.string.open_im, getString(R.string.app_name)), new LoadFrame.OnLoadFrameClickListener() {
            @Override
            public void cancelClick() {
                if (DeviceInfoUtil.isOppoRom()) {
                    // 调试发现OPPO手机被调起后当前界面不会自动回到后台，手动调一下
                    moveTaskToBack(true);
                }
                finish();
            }

            @Override
            public void confirmClick() {
                startActivity(new Intent(ShareNearChatFriend.this, MainActivity.class));
                finish();
            }
        });
        mLoadFrame.show();

        mShareChatMessage.setFromUserId(coreManager.getSelf().getUserId());
        mShareChatMessage.setFromUserName(coreManager.getSelf().getNickName());
        mShareChatMessage.setToUserId(friend.getUserId());
        mShareChatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        mShareChatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage);
        switch (mShareChatMessage.getType()) {
            case XmppMessage.TYPE_TEXT:
                sendMessage(friend);
                break;
            case XmppMessage.TYPE_IMAGE:
            case XmppMessage.TYPE_VIDEO:
            case XmppMessage.TYPE_FILE:
                if (!mShareChatMessage.isUpload()) {// 未上传
                    UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage, new UploadEngine.ImFileUploadResponse() {
                        @Override
                        public void onSuccess(String toUserId, ChatMessage message) {
                            sendMessage(friend);
                        }

                        @Override
                        public void onFailure(String toUserId, ChatMessage message) {
                            mLoadFrame.dismiss();
                            ToastUtil.showToast(ShareNearChatFriend.this, getString(R.string.upload_failed));
                        }
                    });
                } else {// 已上传 自定义表情默认为已上传
                    sendMessage(friend);
                }
                break;
            default:
                Reporter.unreachable();
        }
    }

    private void sendMessage(Friend friend) {
        if (friend.getRoomFlag() == 1) {
            coreManager.sendMucChatMessage(friend.getUserId(), mShareChatMessage);
        } else {
            coreManager.sendChatMessage(friend.getUserId(), mShareChatMessage);
        }
    }

    class ClickListener implements OnClickListener {
        private Friend friend;

        ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:
                    verificationShare(3, friend);
                    break;
                case R.id.btn_cancle:
                    break;
                default:
                    break;
            }
        }
    }

    class MessageRecentlyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mFriends != null) {
                return mFriends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mFriends != null) {
                return mFriends.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mFriends != null) {
                return position;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(ShareNearChatFriend.this, R.layout.item_recently_contacts, null);
                holder = new ViewHolder();
                holder.mIvHead = convertView.findViewById(R.id.iv_recently_contacts_head);
                holder.mTvName = convertView.findViewById(R.id.tv_recently_contacts_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Friend friend = mFriends.get(position);
            holder.mIvHead.fillData(friend);
            holder.mTvName.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
            return convertView;
        }
    }

    class ViewHolder {
        MessageAvatar mIvHead;
        TextView mTvName;
    }
}
