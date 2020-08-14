package com.sk.weichat.ui.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.SKShareBean;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.InstantMessageConfirm;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadFrame;
import com.sk.weichat.view.MessageAvatar;
import com.sk.weichat.xmpp.ListenerManager;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;

/**
 * 分享 最近联系人
 */
public class ShareNearChatFriend extends BaseActivity implements OnClickListener, ChatMessageListener {
    private ListView mShareLv;
    private List<Friend> mFriends;

    private InstantMessageConfirm menuWindow;
    private LoadFrame mLoadFrame;

    private String mShareContent;
    private SKShareBean mSKShareBean;
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
        setContentView(R.layout.activity_messageinstant);

        // 已进入分享界面
        ShareConstant.IS_SHARE_S_COME = true;

        Intent intent = getIntent();
        LogUtils.log(TAG, intent);
        if (intent.getData() != null) {
            // data中的参数转移到extra，方便后续统一读取，
            try {
                Uri data = intent.getData();
                for (String key : data.getQueryParameterNames()) {
                    String value = data.getQueryParameter(key);
                    // 参数统一存在intent.extras里，有的推送不支持，所以要提前处理一下，
                    intent.putExtra(key, value);
                }
            } catch (Exception e) {
                Reporter.post("通知点击intent.data解析失败", e);
            }
        }

        mShareContent = getIntent().getStringExtra(ShareConstant.EXTRA_SHARE_CONTENT);
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }

        mSKShareBean = JSON.parseObject(mShareContent, SKShareBean.class);

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
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }

        coreManager.relogin();// 连接xmpp 发消息需要

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
            if (mFriends.get(i).getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)
                    || mFriends.get(i).getUserId().equals(Friend.ID_SK_PAY)) {
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
        if (mShareChatMessage != null && mShareChatMessage.getPacketId().equals(msgId)) {
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
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(AppConfig.apiKey + mSKShareBean.getAppId() + coreManager.getSelf().getUserId() +
                Md5Util.toMD5(coreManager.getSelfStatus().accessToken + time) + Md5Util.toMD5(mSKShareBean.getAppSecret()));
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", String.valueOf(2));// 1.授权 2.分享 3.支付
        params.put("appId", mSKShareBean.getAppId());
        params.put("appSecret", mSKShareBean.getAppSecret());
        params.put("time", time);
        params.put("secret", secret);

        HttpUtils.get().url(coreManager.getConfig().SDK_OPEN_AUTH_INTERFACE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (type == 1) {// 选择好友
                                Intent intent = new Intent(ShareNearChatFriend.this, ShareNewFriend.class);
                                intent.putExtra(ShareConstant.EXTRA_SHARE_CONTENT, mShareContent);
                                startActivity(intent);
                            } else if (type == 2) {// 生活圈
                                Intent intent = new Intent(ShareNearChatFriend.this, ShareLifeCircleActivity.class);
                                intent.putExtra(ShareConstant.EXTRA_SHARE_CONTENT, mShareContent);
                                startActivity(intent);
                            } else {// 直接发送
                                share(friend);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 网络异常
                        ToastUtil.showNetError(mContext);
                    }
                });
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
        mLoadFrame.setSomething(getString(R.string.back_app, mSKShareBean.getAppName()), new LoadFrame.OnLoadFrameClickListener() {
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

        mShareChatMessage = new ChatMessage();
        if (mSKShareBean.getShareType() == 0) {
            mShareChatMessage.setType(XmppMessage.TYPE_SHARE_LINK);
            mShareChatMessage.setContent(getString(R.string.msg_link));
            mShareChatMessage.setObjectId(mShareContent);
        } else if (mSKShareBean.getShareType() == 1) {
            mShareChatMessage.setType(XmppMessage.TYPE_TEXT);
            mShareChatMessage.setContent(mSKShareBean.getTitle());
        } else if (mSKShareBean.getShareType() == 2) {
            mShareChatMessage.setType(XmppMessage.TYPE_IMAGE);
            mShareChatMessage.setContent(mSKShareBean.getImageUrl());
            mShareChatMessage.setUpload(true);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.tip_share_type_not_supported));
            return;
        }
        mShareChatMessage.setFromUserId(coreManager.getSelf().getUserId());
        mShareChatMessage.setFromUserName(coreManager.getSelf().getNickName());
        mShareChatMessage.setToUserId(friend.getUserId());
        mShareChatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        mShareChatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage);
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
