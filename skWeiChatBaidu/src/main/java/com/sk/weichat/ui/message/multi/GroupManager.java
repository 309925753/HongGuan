package com.sk.weichat.ui.message.multi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
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

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 群管理
 */
public class GroupManager extends BaseActivity {
    /**
     * 更新群组 是否显示已读人数、私密群组、是否开启进群验证、是否对普通成员开放群成员列表、群成员是否可在群组内发送名片
     */
    String authority;
    private String mRoomId;
    private String mRoomJid;
    SwitchButton.OnCheckedChangeListener mOnCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.sb_read:
                    updateGroupHostAuthority(0, isChecked);
                    break;
                case R.id.sb_look:
                    updateGroupHostAuthority(1, isChecked);
                    break;
                case R.id.sb_verify:
                    updateGroupHostAuthority(2, isChecked);
                    break;
                case R.id.sb_show_member:
                    updateGroupHostAuthority(3, isChecked);
                    break;
                case R.id.sb_allow_chat:
                    updateGroupHostAuthority(4, isChecked);
                    break;
                case R.id.sb_allow_invite:
                    updateGroupHostAuthority(5, isChecked);
                    break;
                case R.id.sb_allow_upload:
                    updateGroupHostAuthority(6, isChecked);
                    break;
                case R.id.sb_allow_conference:
                    updateGroupHostAuthority(7, isChecked);
                    break;
                case R.id.sb_allow_send_course:
                    updateGroupHostAuthority(8, isChecked);
                    break;
                case R.id.sb_notify:
                    updateGroupHostAuthority(9, isChecked);
                    break;
            }
        }
    };
    private int[] status_lists;
    private SwitchButton mSbRead;
    private SwitchButton mSbLook;
    private SwitchButton mSbVerify;
    private SwitchButton mSbShowMember;
    private SwitchButton mSbAllowChat;
    private SwitchButton mSbAllowInvite;
    private SwitchButton mSbAllowUpload;
    private SwitchButton mSbAllowConference;
    private SwitchButton mSbAllowSendCourse;
    private SwitchButton mSbNotify;
    private int roomRole;
    private String mRoomName;
    private int mMemberSize;
    // SecureFlagGroup
    private Friend mFriend;
    private int mCurrentCheck = -1;
    private int mLastType;// 网络请求失败之后，还原为上一次type
    private SelectChatModeDialog mSelectChatModeDialog;
    private TextView mSelectTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manager);
        mRoomId = getIntent().getStringExtra("roomId");
        mRoomJid = getIntent().getStringExtra("roomJid");
        roomRole = getIntent().getIntExtra("roomRole", 0);
        status_lists = getIntent().getIntArrayExtra("GROUP_STATUS_LIST");
        mRoomName = getIntent().getStringExtra("copy_name");
        mMemberSize = getIntent().getIntExtra("copy_size", 0);

        mFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mRoomJid);
        initAction();
        initView();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.group_management));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        int isSecretGroup = getIntent().getIntExtra("isSecretGroup", 0);
        findViewById(R.id.copy_rl).setVisibility(isSecretGroup == 1 ? View.GONE : View.VISIBLE);

        mSbRead = (SwitchButton) findViewById(R.id.sb_read);
        mSbLook = (SwitchButton) findViewById(R.id.sb_look);
        mSbVerify = (SwitchButton) findViewById(R.id.sb_verify);
        mSbShowMember = (SwitchButton) findViewById(R.id.sb_show_member);
        mSbAllowChat = (SwitchButton) findViewById(R.id.sb_allow_chat);
        mSbAllowInvite = (SwitchButton) findViewById(R.id.sb_allow_invite);
        mSbAllowUpload = (SwitchButton) findViewById(R.id.sb_allow_upload);
        mSbAllowConference = (SwitchButton) findViewById(R.id.sb_allow_conference);
        mSbAllowSendCourse = (SwitchButton) findViewById(R.id.sb_allow_send_course);
        mSbNotify = (SwitchButton) findViewById(R.id.sb_notify);
        mSbRead.setChecked(status_lists[0] == 1);
        mSbLook.setChecked(status_lists[1] == 0);// 公开群组 开传0，关传1
        mSbVerify.setChecked(status_lists[2] == 1);
        mSbShowMember.setChecked(status_lists[3] == 1);
        mSbAllowChat.setChecked(status_lists[4] == 1);
        mSbAllowInvite.setChecked(status_lists[5] == 1);
        mSbAllowUpload.setChecked(status_lists[6] == 1);
        mSbAllowConference.setChecked(status_lists[7] == 1);
        mSbAllowSendCourse.setChecked(status_lists[8] == 1);
        mSbNotify.setChecked(status_lists[9] == 1);
        mSbRead.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbLook.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbVerify.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbShowMember.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowChat.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowInvite.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowUpload.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowConference.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowSendCourse.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbNotify.setOnCheckedChangeListener(mOnCheckedChangeListener);

        findViewById(R.id.set_remarks_rl).setOnClickListener(v -> {
            Intent intent = new Intent(GroupManager.this, GroupMoreFeaturesActivity.class);
            intent.putExtra("roomId", mRoomId);
            intent.putExtra("isSetRemark", true);
            startActivity(intent);
        });

        // 设置 &&  取消 管理员，隐身人，监控人的按钮设置监听，
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> setRoleIdMap = new HashMap<>();
        setRoleIdMap.put(R.id.set_manager_rl, RoomMember.ROLE_MANAGER);
        setRoleIdMap.put(R.id.set_invisible_rl, RoomMember.ROLE_INVISIBLE);
        setRoleIdMap.put(R.id.set_guardian_rl, RoomMember.ROLE_GUARDIAN);
        for (Integer id : setRoleIdMap.keySet()) {
            findViewById(id).setOnClickListener(v -> {
                SetManagerActivity.start(this, mRoomId, mRoomJid, setRoleIdMap.get(id));
            });
        }

        // SecureFlagGroup
        if (mFriend.getIsSecretGroup() == 1) {
            findViewById(R.id.rl_look).setVisibility(View.GONE);
            findViewById(R.id.rl_look_summer).setVisibility(View.GONE);
        }
        findViewById(R.id.rl_transmission_select).setVisibility(mFriend.getIsSecretGroup() != 1 ? View.VISIBLE : View.GONE);
        mSelectTv = findViewById(R.id.rl_transmission_select_tv);
        changeCheck(mFriend.getEncryptType(), false);
        findViewById(R.id.rl_transmission_select).setOnClickListener(v -> {
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
            mSelectChatModeDialog.setGoneSecret(true);
            mSelectChatModeDialog.show();
        });
        if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
            findViewById(R.id.rl_reset_group_chat_key).setVisibility(mFriend.getIsSecretGroup() != 1 ? View.GONE : View.VISIBLE);
            findViewById(R.id.rl_reset_group_chat_key).setOnClickListener(v -> {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(getString(R.string.reset_group_chat_key), getString(R.string.tip_reset_group_chat_key), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        getEveryMemberRsaPublicKey();
                    }
                });
                selectionFrame.show();
            });
        }

        findViewById(R.id.transfer_group_rl).setOnClickListener(v -> {
            Intent intent = new Intent(GroupManager.this, GroupTransferActivity.class);
            intent.putExtra("roomId", mRoomId);
            intent.putExtra("roomJid", mRoomJid);
            startActivity(intent);
            finish();
        });

        // 群复制
        findViewById(R.id.copy_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRoomId == null) {
                    return;
                }
                if (roomRole == 1 || roomRole == 2) {
                    Intent intent = new Intent(GroupManager.this, RoomCopyActivity.class);
                    intent.putExtra("roomId", mRoomId);
                    intent.putExtra("copy_name", mRoomName);
                    intent.putExtra("copy_size", mMemberSize);
                    startActivity(intent);
                    finish();
                } else tip(getString(R.string.copy_group_manager));
            }
        });
    }

    private void updateGroupHostAuthority(final int type, final boolean isChecked) {
        authority = isChecked ? "1" : "0";
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mRoomId);
        if (type == 0) {
            params.put("showRead", authority);
        } else if (type == 1) {
            params.put("isLook", authority = isChecked ? "0" : "1");// 公开群组 开传0，关传1
        } else if (type == 2) {
            params.put("isNeedVerify", authority);
        } else if (type == 3) {
            params.put("showMember", authority);
        } else if (type == 4) {
            params.put("allowSendCard", authority);
        } else if (type == 5) {
            params.put("allowInviteFriend", authority);
        } else if (type == 6) {
            params.put("allowUploadFile", authority);
        } else if (type == 7) {
            params.put("allowConference", authority);
        } else if (type == 8) {
            params.put("allowSpeakCourse", authority);
        } else if (type == 9) {
            params.put("isAttritionNotice", authority);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            EventBus.getDefault().post(new EventGroupStatus(type, Integer.valueOf(authority)));// 更新群组信息页面
                            String str;
                            if (isChecked) {
                                str = getString(R.string.is_open);
                            } else {
                                str = getString(R.string.is_close);
                            }
                            if (type == 0) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + mRoomJid, isChecked);
                                MsgBroadcast.broadcastMsgRoomUpdate(mContext);// 服务端不会给调用接口者推送对应的XMPP协议，所以需要通知聊天界面刷新
                            } else if (type == 4) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + mRoomJid, isChecked);
                            } else if (type == 7) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_CONFERENCE + mRoomJid, isChecked);
                            } else if (type == 8) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_SEND_COURSE + mRoomJid, isChecked);
                            }
                            tip(str);
                        } else {
                            ToastUtil.showErrorData(mContext);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void changeCheck(int encryptType, boolean isUpdate) {
        boolean isSameClick = mCurrentCheck == encryptType;
        mLastType = mCurrentCheck;
        mCurrentCheck = encryptType;

        if (!isUpdate) {
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
            if (encryptType == 3) {// 兼容老版本，自己与好友都有dh公钥，才能开启端到端聊天
                String key = SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId());
                if (TextUtils.isEmpty(key)) {
                    ToastUtil.showToast(mContext, getString(R.string.you_are_not_eligible_for_encrypt));
                    return;
                }
                if (TextUtils.isEmpty(mFriend.getPublicKeyDH())) {
                    ToastUtil.showToast(mContext, getString(R.string.friend_are_not_eligible_for_encrypt));
                    return;
                }
            }
            DialogHelper.showDefaulteMessageProgressDialog(this);
            updateEncryptType(encryptType);
        }
    }

    /**
     * 更新消息传输方式
     *
     * @param type
     */
    private void updateEncryptType(int type) {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mRoomId);
        params.put("encryptType", String.valueOf(type));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE_ENCRYPT_TYPE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            FriendDao.getInstance().updateEncryptType(mRoomJid, type);
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

    private void getEveryMemberRsaPublicKey() {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mRoomId);
        if (mMemberSize > 100) {
            DialogHelper.showMessageProgressDialog(this, getString(R.string.tip_group_more_wait_long));
        } else {
            DialogHelper.showDefaulteMessageProgressDialog(this);
        }

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET_ALL_MEMBER_RSA_PUBLIC_KEY)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Map<String, String> data = (Map<String, String>) JSON.parse(result.getData());
                            resetChatKeyGroup(data);
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void resetChatKeyGroup(Map<String, String> data) {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mRoomId);

        Map<String, String> keys = new HashMap<>();
        String keysStr;
        String chatKey = UUID.randomUUID().toString().replaceAll("-", "");
        List<String> list = new ArrayList<>(data.keySet());
        for (int i = 0; i < list.size(); i++) {
            String key = data.get(list.get(i));
            String chatKeyGroup = RSA.encryptBase64(chatKey.getBytes(), Base64.decode(key));
            keys.put(list.get(i), chatKeyGroup);
        }
        keysStr = JSON.toJSONString(keys);
        params.put("keys", keysStr);

        HttpUtils.post().url(coreManager.getConfig().ROOM_RESET_GROUP_CHAT_KEY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, getString(R.string.success));
                            // 将chatKey保存在本地
                            FriendDao.getInstance().updateChatKeyGroup(mRoomJid, SecureChatUtil.encryptChatKey(mRoomJid, chatKey));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });

    }

    public void tip(String tip) {
        ToastUtil.showToast(getBaseContext(), tip);
    }
}
