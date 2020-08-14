package com.sk.weichat.ui.me;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.PrivacySetting;
import com.sk.weichat.broadcast.OtherBroadcast;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.AllowTypeDialog;
import com.sk.weichat.view.MsgSyncDaysDialog;
import com.sk.weichat.view.SwitchButton;
import com.sk.weichat.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 隐私设置
 **/
public class PrivacySettingActivity extends BaseActivity implements View.OnClickListener {
    private TextView mMsgSyncDays;

    private SwitchButton mSbVerify;    // 需要朋友验证
    private SwitchButton mSbAuthLogin;    // 旧设备授权登录，

    private SwitchButton mSbEncrypt;   // 消息加密传输
    private SwitchButton mSbVibration; // 消息来时振动
    private SwitchButton mSbInputState;// 让对方知道我正在输入...
    private SwitchButton mSbSlideClearServer;// 首页侧滑删除服务器聊天记录
    private SwitchButton mSbShowMsgState;// 显示消息阅读状态
    private SwitchButton mSbPrivacyPosition;// 是否公开个人位置

    private SwitchButton mSbUseGoogleMap;// 使用谷歌地图
    private SwitchButton mSbSupport;     // 支持多设备登录
    private SwitchButton mSbKeepLive;

    private TextView addFriendTv, isEncryptTv, inputStateTv;
    private TextView mShowLastLoginTime;
    private TextView mShowTelephone;
    private TextView mAllowMessage;
    private TextView mAllowCall;
    private TextView mAllowJoinRoom;

    private SwitchButton sbNameSearch;
    private SwitchButton sbPhoneSearch;

    private TextView tvFriendFrom;

    private String mLoginUserId;
    MsgSyncDaysDialog.OnMsgSaveDaysDialogClickListener onMsgSyncDaysDialogClickListener = new MsgSyncDaysDialog.OnMsgSaveDaysDialogClickListener() {
        @Override
        public void tv1Click() {
            updateMsgSyncTimeLen(-1);
        }

        @Override
        public void tv8Click() {
            updateMsgSyncTimeLen(-2);
        }

        @Override
        public void tv2Click() {
            updateMsgSyncTimeLen(0.04);
        }

        @Override
        public void tv3Click() {
            updateMsgSyncTimeLen(1);
        }

        @Override
        public void tv4Click() {
            updateMsgSyncTimeLen(7);
        }

        @Override
        public void tv5Click() {
            updateMsgSyncTimeLen(30);
        }

        @Override
        public void tv6Click() {
            updateMsgSyncTimeLen(90);
        }

        @Override
        public void tv7Click() {
            updateMsgSyncTimeLen(365);
        }
    };
    SwitchButton.OnCheckedChangeListener onCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.mSbVerify:
                    submitPrivacySetting(1, isChecked);
                    break;
                case R.id.mSbEncrypt:
                    submitPrivacySetting(2, isChecked);
                    break;
                case R.id.mSbzhendong:
                    submitPrivacySetting(3, isChecked);
                    break;
                case R.id.mSbInputState:
                    submitPrivacySetting(4, isChecked);
                    break;
                case R.id.sb_google_map:
                    submitPrivacySetting(5, isChecked);
                    break;
                case R.id.mSbSupport:
                    submitPrivacySetting(6, isChecked);
                    break;
                case R.id.mSbKeepLive:
                    submitPrivacySetting(7, isChecked);
                    break;
                case R.id.sbPhoneSearch:
                    submitPrivacySetting(8, isChecked);
                    break;
                case R.id.sbNameSearch:
                    submitPrivacySetting(9, isChecked);
                    break;
                case R.id.sbAuthLogin:
                    submitPrivacySetting(10, isChecked);
                    break;
                case R.id.mSbPrivacyPosition:
                    submitPrivacySetting(11, isChecked);
                    break;
                case R.id.mSbSlideClearServer:
                    submitPrivacySetting(12, isChecked);
                    break;
                case R.id.mSbShowMsgState:
                    submitPrivacySetting(13, isChecked);
                    break;
            }
        }
    };
    private int friendsVerify = 0;// 是否开启验证

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_setting);

        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
        getPrivacySetting();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.private_settings));
    }

    private void initView() {
        mMsgSyncDays = (TextView) findViewById(R.id.msg_sync_days_tv);

        mSbVerify = (SwitchButton) findViewById(R.id.mSbVerify);
        mSbAuthLogin = findViewById(R.id.sbAuthLogin);

        mSbEncrypt = (SwitchButton) findViewById(R.id.mSbEncrypt);
        mSbVibration = (SwitchButton) findViewById(R.id.mSbzhendong);
        mSbInputState = (SwitchButton) findViewById(R.id.mSbInputState);
        mSbSlideClearServer = (SwitchButton) findViewById(R.id.mSbSlideClearServer);
        mSbShowMsgState = (SwitchButton) findViewById(R.id.mSbShowMsgState);
        mSbPrivacyPosition = (SwitchButton) findViewById(R.id.mSbPrivacyPosition);
        findViewById(R.id.rl_privacy_position).setVisibility(!coreManager.getConfig().disableLocationServer ? View.VISIBLE : View.GONE);
        mSbUseGoogleMap = (SwitchButton) findViewById(R.id.sb_google_map);
        mSbSupport = (SwitchButton) findViewById(R.id.mSbSupport);
        mSbKeepLive = (SwitchButton) findViewById(R.id.mSbKeepLive);
        addFriendTv = (TextView) findViewById(R.id.addFriend_text);
        addFriendTv.setText(getString(R.string.new_friend_verify));
        isEncryptTv = (TextView) findViewById(R.id.isEncrypt_text);
        isEncryptTv.setText(getString(R.string.encrypt_message));
        inputStateTv = (TextView) findViewById(R.id.tv_input_state);
        inputStateTv.setText(getString(R.string.know_typing));

        View.OnClickListener allowTypeListener = new AllowTypeOnClickListener();
        findViewById(R.id.show_last_login_time_rl).setOnClickListener(allowTypeListener);
        if (coreManager.getConfig().registerUsername) {
            findViewById(R.id.show_telephone_rl).setVisibility(View.GONE);
        } else {
            findViewById(R.id.show_telephone_rl).setOnClickListener(allowTypeListener);
        }
        findViewById(R.id.allow_message_rl).setOnClickListener(allowTypeListener);
        findViewById(R.id.allow_call_rl).setOnClickListener(allowTypeListener);
        findViewById(R.id.allow_join_room_rl).setOnClickListener(allowTypeListener);
        mShowLastLoginTime = (TextView) findViewById(R.id.show_last_login_time_tv);
        mShowTelephone = (TextView) findViewById(R.id.show_telephone_tv);
        mAllowMessage = (TextView) findViewById(R.id.allow_message_tv);
        mAllowCall = (TextView) findViewById(R.id.allow_call_tv);
        mAllowJoinRoom = (TextView) findViewById(R.id.allow_join_room_tv);

        sbPhoneSearch = (SwitchButton) findViewById(R.id.sbPhoneSearch);
        sbNameSearch = (SwitchButton) findViewById(R.id.sbNameSearch);

        findViewById(R.id.friend_from_rl).setOnClickListener(new FriendFromOnClickListener());
        tvFriendFrom = (TextView) findViewById(R.id.friend_from_tv);
    }

    // 获取用户的设置状态
    private void getPrivacySetting() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<PrivacySetting>(PrivacySetting.class) {

                    @Override
                    public void onResponse(ObjectResult<PrivacySetting> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(PrivacySettingActivity.this, result)) {
                            PrivacySetting settings = result.getData();
                            PrivacySettingHelper.setPrivacySettings(PrivacySettingActivity.this, settings);

                            friendsVerify = settings.getFriendsVerify();
                        }

                        initStatus();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(PrivacySettingActivity.this);

                        initStatus();
                    }
                });
    }

    private void initStatus() {
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        mShowLastLoginTime.setText(allowTypeValueToString(privacySetting.getShowLastLoginTime()));
        mShowTelephone.setText(allowTypeValueToString(privacySetting.getShowTelephone()));
        mAllowMessage.setText(allowTypeValueToString(privacySetting.getAllowMsg()));
        mAllowCall.setText(allowTypeValueToString(privacySetting.getAllowCall()));
        mAllowJoinRoom.setText(allowTypeValueToString(privacySetting.getAllowJoinRoom()));

        String chatSyncTimeLen = String.valueOf(privacySetting.getChatSyncTimeLen());
        mMsgSyncDays.setText(conversion(Double.parseDouble(chatSyncTimeLen)));

        mSbVerify.setChecked(friendsVerify == 1);
        mSbAuthLogin.setChecked(privacySetting.getAuthSwitch() == 1);

        sbPhoneSearch.setChecked(privacySetting.getPhoneSearch() == 1);
        sbNameSearch.setChecked(privacySetting.getNameSearch() == 1);

        setFriendFromListText(privacySetting.getFriendFromListArray());

        // 获得加密状态
        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        mSbEncrypt.setChecked(isEncrypt);
        // 获得振动状态
        boolean vibration = privacySetting.getIsVibration() == 1;
        mSbVibration.setChecked(vibration);
        // 获得输入状态
        boolean inputStatus = privacySetting.getIsTyping() == 1;
        mSbInputState.setChecked(inputStatus);
        // 首页侧滑删除服务器聊天记录
        boolean isSkidRemoveHistoryMsg = privacySetting.getIsSkidRemoveHistoryMsg() == 1;
        mSbSlideClearServer.setChecked(isSkidRemoveHistoryMsg);
        // 显示消息阅读状态
        boolean isShowMsgState = privacySetting.getIsShowMsgState() == 1;
        mSbShowMsgState.setChecked(isShowMsgState);
        // 获得位置公开状态
        boolean openPrivacyPosition = privacySetting.getIsOpenPrivacyPosition() == 1;
        mSbPrivacyPosition.setChecked(openPrivacyPosition);

        // 获得使用状态
        boolean iSGoogleMap = privacySetting.getIsUseGoogleMap() == 1;
        mSbUseGoogleMap.setChecked(iSGoogleMap);

        // 获得支持状态
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        mSbSupport.setChecked(isSupport);
        boolean isKeep = privacySetting.getIsKeepalive() == 1;
        mSbKeepLive.setChecked(isKeep);
        findViewById(R.id.msg_sync_days_rl).setOnClickListener(this);

        mSbSupport.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSbVerify.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbAuthLogin.setOnCheckedChangeListener(onCheckedChangeListener);

                sbPhoneSearch.setOnCheckedChangeListener(onCheckedChangeListener);
                sbNameSearch.setOnCheckedChangeListener(onCheckedChangeListener);

                mSbEncrypt.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbVibration.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbInputState.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbSlideClearServer.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbShowMsgState.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbPrivacyPosition.setOnCheckedChangeListener(onCheckedChangeListener);

                mSbUseGoogleMap.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbSupport.setOnCheckedChangeListener(onCheckedChangeListener);
                mSbKeepLive.setOnCheckedChangeListener(onCheckedChangeListener);
            }
        }, 200);
    }

    @Override
    public void onClick(View v) {
        MsgSyncDaysDialog msgSyncDaysDialog = new MsgSyncDaysDialog(this, onMsgSyncDaysDialogClickListener);
        msgSyncDaysDialog.show();
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

    // 更新消息漫游时长
    private void updateMsgSyncTimeLen(final double syncTime) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put("chatSyncTimeLen", String.valueOf(syncTime));

        HttpUtils.get().url(coreManager.getConfig().USER_SET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(PrivacySettingActivity.this, result)) {
                            Toast.makeText(PrivacySettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(PrivacySettingActivity.this);
                            privacySetting.setChatSyncTimeLen(syncTime);
                            PrivacySettingHelper.setPrivacySettings(PrivacySettingActivity.this, privacySetting);
                            mMsgSyncDays.setText(conversion(syncTime));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 更新允许相关的设置，
    private void updateAllowType(String type, int value, Field field, TextView resultView) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put(type, String.valueOf(value));

        HttpUtils.get().url(coreManager.getConfig().USER_SET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(PrivacySettingActivity.this, result)) {
                            Toast.makeText(PrivacySettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(PrivacySettingActivity.this);
                            try {
                                field.setAccessible(true);
                                field.set(privacySetting, value);
                            } catch (Exception e) {
                                Reporter.unreachable(e);
                                return;
                            }
                            PrivacySettingHelper.setPrivacySettings(PrivacySettingActivity.this, privacySetting);
                            resultView.setText(allowTypeValueToString(value));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private int allowTypeValueToString(int value) {
        int ret;
        switch (value) {
            case -1:
                ret = R.string.privacy_allow_none;
                break;
            case 1:
                ret = R.string.privacy_allow_all;
                break;
            case 2:
                ret = R.string.privacy_allow_friend;
                break;
            case 3:
                ret = R.string.privacy_allow_contact;
                break;
            default:
                Reporter.unreachable();
                ret = R.string.unknown;
        }
        return ret;
    }

    // 提交设置用户的状态
    private void submitPrivacySetting(final int index, final boolean isChecked) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        String status = isChecked ? "1" : "0";
        if (index == 1) {
            params.put("friendsVerify", status);
        } else if (index == 2) {
            params.put("isEncrypt", status);
        } else if (index == 3) {
            params.put("isVibration", status);
        } else if (index == 4) {
            params.put("isTyping", status);
        } else if (index == 5) {
            params.put("isUseGoogleMap", status);
        } else if (index == 6) {
            params.put("multipleDevices", status);
        } else if (index == 7) {
            params.put("isKeepalive", status);
        } else if (index == 8) {
            params.put("phoneSearch", status);
        } else if (index == 9) {
            params.put("nameSearch", status);
        } else if (index == 10) {
            params.put("authSwitch", status);
        } else if (index == 11) {
            params.put("isOpenPrivacyPosition", status);
        } else if (index == 12) {
            params.put("isSkidRemoveHistoryMsg", status);
        } else if (index == 13) {
            params.put("isShowMsgState", status);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        TipDialog tipDialog = new TipDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_SET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(PrivacySettingActivity.this, result)) {
                            Toast.makeText(PrivacySettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(PrivacySettingActivity.this);

                            int value = Integer.parseInt(status);
                            if (index == 2) {
                                privacySetting.setIsEncrypt(value);
                            } else if (index == 3) {
                                privacySetting.setIsVibration(value);
                            } else if (index == 4) {
                                privacySetting.setIsTyping(value);
                            } else if (index == 5) {
                                privacySetting.setIsUseGoogleMap(value);
                                if (isChecked) {
                                    MapHelper.setMapType(MapHelper.MapType.GOOGLE);
                                } else {
                                    MapHelper.setMapType(MapHelper.MapType.BAIDU);
                                }
                            } else if (index == 6) {
                                privacySetting.setMultipleDevices(value);
                                tipDialog.setmConfirmOnClickListener(getString(R.string.multi_login_need_reboot), new TipDialog.ConfirmOnClickListener() {
                                    @Override
                                    public void confirm() {
                                        // todo
                                    }
                                });
                                tipDialog.show();
                            } else if (index == 7) {
                                privacySetting.setIsKeepalive(value);
                                tipDialog.setmConfirmOnClickListener(getString(R.string.update_success_restart), new TipDialog.ConfirmOnClickListener() {
                                    @Override
                                    public void confirm() {
                                        Intent intent = new Intent(OtherBroadcast.BROADCASTTEST_ACTION);
                                        intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.myBroadcastReceiverClass));
                                        sendBroadcast(intent);
                                    }
                                });
                                tipDialog.show();
                            } else if (index == 8) {
                                privacySetting.setPhoneSearch(value);
                            } else if (index == 9) {
                                privacySetting.setNameSearch(value);
                            } else if (index == 11) {
                                privacySetting.setIsOpenPrivacyPosition(value);
                            } else if (index == 12) {
                                privacySetting.setIsSkidRemoveHistoryMsg(value);
                            } else if (index == 13) {
                                privacySetting.setIsShowMsgState(value);
                            }
                            PrivacySettingHelper.setPrivacySettings(PrivacySettingActivity.this, privacySetting);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(PrivacySettingActivity.this);
                    }
                });
    }

    private void submitFriendFromList(List<Integer> friendFromList) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);
        params.put("friendFromList", TextUtils.join(",", friendFromList));

        HttpUtils.get().url(coreManager.getConfig().USER_SET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(PrivacySettingActivity.this, result)) {
                            Toast.makeText(PrivacySettingActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(PrivacySettingActivity.this);
                            privacySetting.setFriendFromListArray(friendFromList);
                            PrivacySettingHelper.setPrivacySettings(mContext, privacySetting);
                            setFriendFromListText(friendFromList);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void setFriendFromListText(List<Integer> friendFromList) {
        if (friendFromList == null || friendFromList.isEmpty()) {
            tvFriendFrom.setText(R.string.friend_from_allow_none);
            return;
        }
        String[] stringArray = mContext.getResources().getStringArray(R.array.friend_from_type);
        // 0是系统添加，不能设置，
        if (friendFromList.containsAll(Arrays.asList(1, 2, 3, 4, 5, 6))) {
            tvFriendFrom.setText(R.string.friend_from_allow_all);
            return;
        }
        List<String> ret = new LinkedList<>();
        for (Integer integer : friendFromList) {
            // 0是系统添加，不能设置，
            if (integer == 0) {
                continue;
            }
            --integer;
            ret.add(stringArray[integer]);
        }
        tvFriendFrom.setText(TextUtils.join(",", ret));
    }

    private class FriendFromOnClickListener implements View.OnClickListener {
        private List<Integer> friendFromList;

        @Override
        public void onClick(View v) {
            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(PrivacySettingActivity.this);
            friendFromList = privacySetting.getFriendFromListArray();
            if (friendFromList == null) {
                friendFromList = (new ArrayList<>());
            }
            String[] stringArray = mContext.getResources().getStringArray(R.array.friend_from_type);
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.tip_friend_from)
                    .setMultiChoiceItems(
                            stringArray,
                            getCheckedItems(stringArray.length),
                            (dialog, which, isChecked) -> {
                                Log.e(TAG, "" + which + ", " + isChecked);
                                // 0是系统添加，不能设置，
                                ++which;
                                if (isChecked) {
                                    if (!friendFromList.contains(which)) {
                                        friendFromList.add(which);
                                    }
                                } else {
                                    // 删除的是等于which的元素，不是第几个，
                                    //noinspection SuspiciousMethodCalls
                                    friendFromList.remove((Object) which);
                                }
                            }
                    )
                    .setPositiveButton(R.string.sure, (dialog, which) -> {
                        Log.e(TAG, "" + which + ", " + friendFromList);
                        // 从小到大排个序，好看点，
                        Collections.sort(friendFromList);
                        submitFriendFromList(friendFromList);
                    })
                    .show();
        }

        private boolean[] getCheckedItems(int length) {
            boolean[] ret = new boolean[length];
            if (friendFromList == null || friendFromList.isEmpty()) {
                return ret;
            }
            for (Integer integer : friendFromList) {
                // 0是系统添加，不能设置，
                --integer;
                if (integer < 0 || integer >= length) {
                    Reporter.unreachable();
                    continue;
                }
                ret[integer] = true;
            }
            return ret;
        }
    }

    private class AllowTypeOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String type;
            String whiteListType;
            String blackListType;
            TextView resultView;
            switch (v.getId()) {
                case R.id.show_last_login_time_rl:
                    type = "showLastLoginTime";
                    whiteListType = "throughoutShowLastLoginTimeList";
                    blackListType = "noShowLastLoginTimeList";
                    resultView = mShowLastLoginTime;
                    break;
                case R.id.show_telephone_rl:
                    type = "showTelephone";
                    whiteListType = "throughoutShowTelephoneList";
                    blackListType = "noShowTelephoneList";
                    resultView = mShowTelephone;
                    break;
                case R.id.allow_message_rl:
                    type = "allowMsg";
                    whiteListType = "throughoutAllowMsgList";
                    blackListType = "noAllowMsgList";
                    resultView = mAllowMessage;
                    break;
                case R.id.allow_call_rl:
                    type = "allowCall";
                    whiteListType = "throughoutAllowCallList";
                    blackListType = "noAllowCallList";
                    resultView = mAllowCall;
                    break;
                case R.id.allow_join_room_rl:
                    type = "allowJoinRoom";
                    whiteListType = "throughoutAllowJoinRoomList";
                    blackListType = "noAllowJoinRoomList";
                    resultView = mAllowJoinRoom;
                    break;
                default:
                    Reporter.unreachable();
                    return;
            }
            AllowTypeDialog dialog = new AllowTypeDialog(mContext, new AllowTypeDialog.OnAllowTypeClickListener() {
                @Override
                public void onNewValueClick(int value) {
                    Field field;
                    try {
                        field = PrivacySetting.class.getDeclaredField(type);
                    } catch (Exception e) {
                        Reporter.unreachable(e);
                        return;
                    }
                    updateAllowType(type, value, field, resultView);
                }

                @Override
                public void onWhitelistClick() {
                    PrivacyWhitelistActivity.start(mContext, whiteListType);
                }

                @Override
                public void onBlacklistClick() {
                    PrivacyWhitelistActivity.start(mContext, blackListType);
                }
            });
            dialog.show();
        }
    }
}
