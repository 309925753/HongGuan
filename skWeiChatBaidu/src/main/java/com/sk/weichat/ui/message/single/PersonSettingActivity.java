package com.sk.weichat.ui.message.single;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrcode.Constant;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.Label;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.pay.TransferRecordActivity;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.groupchat.SelectContactsActivity;
import com.sk.weichat.ui.message.search.SearchChatHistoryActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.view.MsgSaveDaysDialog;
import com.sk.weichat.view.SelectChatModeDialog;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.view.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/4/18 0018.
 */

public class PersonSettingActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mFriendAvatarIv;
    private TextView mFriendNameTv;
    private TextView mRemarkNameTv;
    private TextView mLabelNameTv;
    private SwitchButton mIsReadFireSb;
    private SwitchButton mTopSb;
    private SwitchButton mIsDisturbSb;
    private TextView mMsgSaveDays;

    private String mLoginUserId;
    private String mFriendId;
    MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener onMsgSaveDaysDialogClickListener = new MsgSaveDaysDialog.OnMsgSaveDaysDialogClickListener() {
        @Override
        public void tv1Click() {
            updateChatRecordTimeOut(-1);
        }

        @Override
        public void tv2Click() {
            updateChatRecordTimeOut(0.04);
            // updateChatRecordTimeOut(0.00347); // 五分钟过期
        }

        @Override
        public void tv3Click() {
            updateChatRecordTimeOut(1);
        }

        @Override
        public void tv4Click() {
            updateChatRecordTimeOut(7);
        }

        @Override
        public void tv5Click() {
            updateChatRecordTimeOut(30);
        }

        @Override
        public void tv6Click() {
            updateChatRecordTimeOut(90);
        }

        @Override
        public void tv7Click() {
            updateChatRecordTimeOut(365);
        }
    };
    private Friend mFriend;
    private String mFriendName;
    private RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    // SecureFlag
    private SwitchButton sb1, sb2, sb3, sb4;
    private List<com.sk.weichat.view.SwitchButton> buttons = new ArrayList<>();
    private int mCurrentCheck = -1;
    private int mLastType;// 网络请求失败之后，还原为上一次type
    // todo changed ui
    private SelectChatModeDialog mSelectChatModeDialog;
    private TextView mSelectTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_setting);

        mLoginUserId = coreManager.getSelf().getUserId();
        mFriendId = getIntent().getStringExtra("ChatObjectId");
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriendId);

        if (mFriend == null) {
            LogUtils.log(getIntent());
            Reporter.unreachable();
            ToastUtil.showToast(this, R.string.tip_friend_not_found);
            finish();
            return;
        }

        initActionBar();
        initView();
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriendId);// Friend也更新下
        if (mFriend == null) {
            Toast.makeText(this, R.string.tip_friend_removed, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            mFriendName = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
            mFriendNameTv.setText(mFriendName);
            if (mFriend.getRemarkName() != null) {
                mRemarkNameTv.setText(mFriend.getRemarkName());
            }
            List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mFriendId);
            String labelNames = "";
            if (friendLabelList != null && friendLabelList.size() > 0) {
                for (int i = 0; i < friendLabelList.size(); i++) {
                    if (i == friendLabelList.size() - 1) {
                        labelNames += friendLabelList.get(i).getGroupName();
                    } else {
                        labelNames += friendLabelList.get(i).getGroupName() + "，";
                    }
                }
            }
            mLabelNameTv.setText(labelNames);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            // 无论如何不应该在destroy崩溃，
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.chat_settings));
    }

    private void initView() {
        mFriendAvatarIv = (ImageView) findViewById(R.id.avatar);
        AvatarHelper.getInstance().displayAvatar(mFriendId, mFriendAvatarIv, true);
        mFriendNameTv = (TextView) findViewById(R.id.name);
        mRemarkNameTv = (TextView) findViewById(R.id.remark_name);
        mLabelNameTv = (TextView) findViewById(R.id.label_name);
        TextView mNoDisturbTv = (TextView) findViewById(R.id.no_disturb_tv);
        mNoDisturbTv.setText(getString(R.string.message_not_disturb));
        // 阅后即焚 && 置顶 && 消息免打扰
        mIsReadFireSb = (SwitchButton) findViewById(R.id.sb_read_fire);
        int isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId, 0);
        mIsReadFireSb.setChecked(isReadDel == 1);
        mIsReadFireSb.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                updateDisturbStatus(1, isChecked);
            }
        });

        mTopSb = (SwitchButton) findViewById(R.id.sb_top_chat);
        mTopSb.setChecked(mFriend.getTopTime() != 0);// TopTime不为0，当前状态为置顶
        mTopSb.setOnCheckedChangeListener((view, isChecked) -> updateDisturbStatus(2, isChecked));

        mIsDisturbSb = (SwitchButton) findViewById(R.id.sb_no_disturb);
        mIsDisturbSb.setChecked(mFriend.getOfflineNoPushMsg() == 1);
        mIsDisturbSb.setOnCheckedChangeListener((view, isChecked) -> updateDisturbStatus(0, isChecked));

        initSecureChatCheck();

        mMsgSaveDays = (TextView) findViewById(R.id.msg_save_days_tv);
        mMsgSaveDays.setText(conversion(mFriend.getChatRecordTimeOut()));

        findViewById(R.id.avatar).setOnClickListener(this);
        if (coreManager.getLimit().cannotCreateGroup() || mFriend.getStatus() == Friend.STATUS_SYSTEM) {
            findViewById(R.id.add_contacts).setVisibility(View.GONE);
        } else {
            findViewById(R.id.add_contacts).setOnClickListener(this);
        }
        // 关闭支付功能，隐藏交易记录
        if (!coreManager.getConfig().enablePayModule) {
            findViewById(R.id.rl_transfer).setVisibility(View.GONE);
        }

        findViewById(R.id.chat_history_search).setOnClickListener(this);
        findViewById(R.id.remark_rl).setOnClickListener(this);
        findViewById(R.id.label_rl).setOnClickListener(this);
        findViewById(R.id.msg_save_days_rl).setOnClickListener(this);
        findViewById(R.id.set_background_rl).setOnClickListener(this);
        findViewById(R.id.chat_history_empty).setOnClickListener(this);
        findViewById(R.id.sync_chat_history_empty).setOnClickListener(this);
        findViewById(R.id.rl_transfer).setOnClickListener(this);

        findViewById(R.id.rl_transmission_public).setOnClickListener(this);
        findViewById(R.id.rl_transmission_desed).setOnClickListener(this);
        findViewById(R.id.rl_transmission_aes).setOnClickListener(this);
        findViewById(R.id.rl_transmission_asymmetric_aes).setOnClickListener(this);
        findViewById(R.id.rl_transmission_select).setOnClickListener(this);

        if (mFriend.getStatus() == Friend.STATUS_SYSTEM) {
            findViewById(R.id.remark_rl).setVisibility(View.GONE);
            findViewById(R.id.label_rl).setVisibility(View.GONE);
        }
    }

    private void initSecureChatCheck() {
        sb1 = findViewById(R.id.sb_transmission_public);
        sb2 = findViewById(R.id.sb_transmission_desed);
        sb3 = findViewById(R.id.sb_transmission_aes);
        sb4 = findViewById(R.id.sb_transmission_asymmetric_aes);
        buttons.add(sb1);
        buttons.add(sb2);
        buttons.add(sb3);
        buttons.add(sb4);
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setEnableTouch(false);
        }

        // todo changed ui
        mSelectTv = findViewById(R.id.rl_transmission_select_tv);

        changeCheck(mFriend.getEncryptType(), false);
    }

    private void changeCheck(int encryptType, boolean isUpdate) {
        boolean isSameClick = mCurrentCheck == encryptType;
        mLastType = mCurrentCheck;
        mCurrentCheck = encryptType;

        if (!isUpdate) {
            for (int i = 0; i < buttons.size(); i++) {
                if (encryptType == i) {
                    buttons.get(i).setChecked(true);
                } else {
                    buttons.get(i).setChecked(false);
                }
            }

            // todo changed ui
            if (encryptType == 0) {
                mSelectTv.setText(getString(R.string.msg_transmission_public));
            } else if (encryptType == 1) {
                mSelectTv.setText(getString(R.string.msg_transmission_desed));
            } else if (encryptType == 2) {
                mSelectTv.setText(getString(R.string.msg_transmission_aes));
            } else if (encryptType == 3) {
                mSelectTv.setText(getString(R.string.msg_transmission_asymmetric_aes));
            }
        }

        if (isUpdate && !isSameClick) {
            if (encryptType == 2 || encryptType == 3) {// 兼容老版本与其它端，自己与好友都有dh公钥，才能开启端到端聊天和AES加密
                String key = SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId());
                if (TextUtils.isEmpty(key)) {
                    ToastUtil.showToast(mContext, getString(encryptType == 2 ? R.string.you_are_not_eligible_for_encrypt_aes : R.string.you_are_not_eligible_for_encrypt));
                    return;
                }
                if (TextUtils.isEmpty(mFriend.getPublicKeyDH())) {
                    ToastUtil.showToast(mContext, getString(encryptType == 2 ? R.string.friend_are_not_eligible_for_encrypt_aes : R.string.friend_are_not_eligible_for_encrypt));
                    return;
                }
            }
            DialogHelper.showDefaulteMessageProgressDialog(this);
            updateEncryptType(encryptType);
        }
    }

    private void updateEncryptType(int type) {
        Map<String, String> params = new HashMap<>();
        params.put("toUserId", mFriendId);
        params.put("encryptType", String.valueOf(type));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_FRIENDS_MODIFY_ENCRYPT_TYPE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            FriendDao.getInstance().updateEncryptType(mFriendId, type);
                            for (int i = 0; i < buttons.size(); i++) {
                                if (type == i) {
                                    buttons.get(i).setChecked(true);
                                } else {
                                    buttons.get(i).setChecked(false);
                                }
                            }

                            // todo changed ui
                            if (mSelectChatModeDialog != null) {
                                mSelectChatModeDialog.dismiss();
                            }
                            if (type == 0) {
                                mSelectTv.setText(getString(R.string.msg_transmission_public));
                            } else if (type == 1) {
                                mSelectTv.setText(getString(R.string.msg_transmission_desed));
                            } else if (type == 2) {
                                mSelectTv.setText(getString(R.string.msg_transmission_aes));
                            } else if (type == 3) {
                                mSelectTv.setText(getString(R.string.msg_transmission_asymmetric_aes));
                            }
                        } else {
                            mCurrentCheck = mLastType;// 还原type
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                        mCurrentCheck = mLastType;// 还原type
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.avatar:
                Intent intentBasic = new Intent(this, BasicInfoActivity.class);
                intentBasic.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentBasic);
                break;
            case R.id.add_contacts:
                Intent intentAdd = new Intent(this, SelectContactsActivity.class);
                intentAdd.putExtra("QuicklyCreateGroup", true);
                intentAdd.putExtra("ChatObjectId", mFriendId);
                intentAdd.putExtra("ChatObjectName", mFriendName);
                startActivity(intentAdd);
                break;
            case R.id.chat_history_search:
                Intent intentChat = new Intent(this, SearchChatHistoryActivity.class);
                intentChat.putExtra("isSearchSingle", true);
                intentChat.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentChat);
                break;
            case R.id.remark_rl:
                SetRemarkActivity.start(this, mFriendId);
                break;
            case R.id.label_rl:
                Intent intentLabel = new Intent(this, SetLabelActivity.class);
                intentLabel.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentLabel);
                break;
            case R.id.msg_save_days_rl:
                MsgSaveDaysDialog msgSaveDaysDialog = new MsgSaveDaysDialog(this, onMsgSaveDaysDialogClickListener);
                msgSaveDaysDialog.show();
                break;
            case R.id.set_background_rl:
                Intent intentBackground = new Intent(this, SelectSetTypeActivity.class);
                intentBackground.putExtra(AppConstant.EXTRA_USER_ID, mFriendId);
                startActivity(intentBackground);
                break;
            case R.id.chat_history_empty:
                clean(false);
                break;
            case R.id.sync_chat_history_empty:
                clean(true);
                break;
            case R.id.rl_transfer:
                Intent intentTransfer = new Intent(this, TransferRecordActivity.class);
                intentTransfer.putExtra(Constant.TRANSFE_RRECORD, mFriendId);
                startActivity(intentTransfer);
                break;
            case R.id.rl_transmission_public:
                changeCheck(0, true);
                break;
            case R.id.rl_transmission_desed:
                changeCheck(1, true);
                break;
            case R.id.rl_transmission_aes:
                changeCheck(2, true);
                break;
            case R.id.rl_transmission_asymmetric_aes:
                changeCheck(3, true);
                break;
            case R.id.rl_transmission_select:
                mSelectChatModeDialog = new SelectChatModeDialog(mContext, new SelectChatModeDialog.OnBannedDialogClickListener() {
                    @Override
                    public void tv1Click() {
                        changeCheck(0, true);
                    }

                    @Override
                    public void tv2Click() {
                        changeCheck(1, true);
                    }

                    @Override
                    public void tv3Click() {
                        changeCheck(2, true);
                    }

                    @Override
                    public void tv4Click() {
                        changeCheck(3, true);
                    }
                });
                mSelectChatModeDialog.show();
                break;
        }
    }

    private void clean(boolean isSync) {
        String tittle = isSync ? getString(R.string.sync_chat_history_clean) : getString(R.string.clean_chat_history);
        String tip = isSync ? getString(R.string.tip_sync_chat_history_clean) : getString(R.string.tip_confirm_clean_history);

        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(tittle, tip, new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                if (isSync) {
                    // 发送一条双向清除的消息给对方，对方收到消息后也将本地消息删除
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY);
                    chatMessage.setFromUserId(mLoginUserId);
                    chatMessage.setFromUserName(coreManager.getSelf().getNickName());
                    chatMessage.setToUserId(mFriendId);
                    chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
                    coreManager.sendChatMessage(mFriendId, chatMessage);
                }
                emptyServerMessage();

                FriendDao.getInstance().resetFriendMessage(mLoginUserId, mFriendId);
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mFriendId);
                sendBroadcast(new Intent(Constants.CHAT_HISTORY_EMPTY));// 清空聊天界面
                MsgBroadcast.broadcastMsgUiUpdate(mContext);
                Toast.makeText(PersonSettingActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            }
        });
        selectionFrame.show();
    }

    // 更新消息免打扰状态
    private void updateDisturbStatus(final int type, final boolean isChecked) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put("toUserId", mFriendId);
        params.put("type", String.valueOf(type));
        params.put("offlineNoPushMsg", isChecked ? String.valueOf(1) : String.valueOf(0));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_NOPULL_MSG)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (type == 0) {// 消息免打扰
                                FriendDao.getInstance().updateOfflineNoPushMsgStatus(mFriendId, isChecked ? 1 : 0);
                            } else if (type == 1) {// 阅后即焚
                                PreferenceUtils.putInt(mContext, Constants.MESSAGE_READ_FIRE + mFriendId + mLoginUserId, isChecked ? 1 : 0);
                                if (isChecked) {
                                    ToastUtil.showToast(PersonSettingActivity.this, R.string.tip_status_burn);
                                }
                            } else {// 置顶聊天
                                if (isChecked) {
                                    FriendDao.getInstance().updateTopFriend(mFriendId, mFriend.getTimeSend());
                                } else {
                                    FriendDao.getInstance().resetTopFriend(mFriendId);
                                }
                            }
                        } else {
                            Toast.makeText(PersonSettingActivity.this, R.string.tip_edit_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(PersonSettingActivity.this);
                    }
                });
    }

    // 更新消息保存天数
    private void updateChatRecordTimeOut(final double outTime) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", mFriendId);
        params.put("chatRecordTimeOut", String.valueOf(outTime));

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(PersonSettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            mMsgSaveDays.setText(conversion(outTime));
                            FriendDao.getInstance().updateChatRecordTimeOut(mFriendId, outTime);
                            sendBroadcast(new Intent(com.sk.weichat.broadcast.OtherBroadcast.NAME_CHANGE));// 刷新聊天界面
                        } else {
                            Toast.makeText(PersonSettingActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 服务器上与该人的聊天记录也需要删除
    private void emptyServerMessage() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(0));// 0 清空单人 1 清空所有
        params.put("toUserId", mFriendId);

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private String conversion(double outTime) {
        String outTimeStr;
        if (outTime == -1 || outTime == 0) {
            outTimeStr = getString(R.string.permanent);
        } else if (outTime == -2) {
            outTimeStr = getString(R.string.no_sync);
        } else if (outTime == 0.04) {
            outTimeStr = getString(R.string.one_hour);
        } else if (outTime == 1) {
            outTimeStr = getString(R.string.one_day);
        } else if (outTime == 7) {
            outTimeStr = getString(R.string.one_week);
        } else if (outTime == 30) {
            outTimeStr = getString(R.string.one_month);
        } else if (outTime == 90) {
            outTimeStr = getString(R.string.one_season);
        } else {
            outTimeStr = getString(R.string.one_year);
        }
        return outTimeStr;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH);
        registerReceiver(receiver, intentFilter);
    }

    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH)) {
                // 快速创建群组 || 更换聊天背景 成功，接收到该广播结束当前界面
                finish();
            }
        }
    }
}
