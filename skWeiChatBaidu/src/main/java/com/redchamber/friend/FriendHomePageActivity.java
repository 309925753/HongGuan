package com.redchamber.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.bean.RedIndexUser;
import com.redchamber.bean.UserHomePageBean;
import com.redchamber.bean.YourCommentBean;
import com.redchamber.friend.adapter.FriendPhotoAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.report.AnonymousReportActivity;
import com.redchamber.request.BlackRequest;
import com.redchamber.request.BlackRequest.AddBlackListCallBack;
import com.redchamber.request.BlackRequest.IsMyBlackListCallBack;
import com.redchamber.request.CollectRequest;
import com.redchamber.request.CommentRequest;
import com.redchamber.request.FreeAuthTimesRequest;
import com.redchamber.request.UnlockPhotoRequest;
import com.redchamber.request.UserLevelRequest;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.util.SplitUtils;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.view.CommentFriendDialog;
import com.redchamber.view.CommonHintDoubleDialog;
import com.redchamber.view.CommonHintSingleDialog;
import com.redchamber.view.EvenWheatCodeDialog;
import com.redchamber.view.PullBlackDialog;
import com.redchamber.vip.VipCenterActivity;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.bean.assistant.GroupAssistantDetail;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.call.Jitsi_pre;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.tool.SingleImagePreviewActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.ChatBottomView;
import com.sk.weichat.view.ChatContentView;
import com.sk.weichat.view.photopicker.PhotoPickerActivity;
import com.sk.weichat.view.photopicker.SelectModel;
import com.sk.weichat.view.photopicker.intent.PhotoPickerIntent;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 他人主页
 */
public class FriendHomePageActivity extends BaseActivity implements
        ChatContentView.MessageEventListener, ChatBottomView.ChatBottomListener, ChatMessageListener {

    @BindView(R.id.tv_nickname)
    TextView mTvNickName;
    @BindView(R.id.tv_vip)
    TextView mTvVip;
    @BindView(R.id.iv_like)
    ImageView mIvLike;
    @BindView(R.id.tv_like)
    TextView mTvLike;
    @BindView(R.id.iv_avatar)
    RoundedImageView mIvAvatar;
    @BindView(R.id.tv_city)
    TextView mTvCity;
    @BindView(R.id.tv_age)
    TextView mTvAge;
    @BindView(R.id.tv_job)
    TextView mTvJob;
    @BindView(R.id.tv_distance)
    TextView mTvDistance;
    @BindView(R.id.tv_online)
    TextView mTvOnline;
    @BindView(R.id.tv_auth_face)
    TextView mTvAuthFace;
    @BindView(R.id.tv_auth_girl)
    TextView mTvAuthGirl;
    @BindView(R.id.tv_status_radio)
    TextView mTvRadioStatus;
    @BindView(R.id.tv_height)
    TextView mTvHeight;
    @BindView(R.id.tv_weight)
    TextView mTvWeight;
    @BindView(R.id.tv_resident_city)
    TextView mTvResidentCity;
    @BindView(R.id.tv_program)
    TextView mTvProgram;
    @BindView(R.id.tv_expect)
    TextView mTvExpect;
    @BindView(R.id.tv_intro)
    TextView mTvIntro;
    @BindView(R.id.rv_photo)
    RecyclerView mRvPhoto;
    @BindView(R.id.ll_permission)
    LinearLayout mLlPermission;
    @BindView(R.id.tv_permission_hint)
    TextView mTvPermissionHint;
    @BindView(R.id.tv_photo_num)
    TextView mTvPhotoNum;
    @BindView(R.id.tv_unlock)
    TextView mTvLock;
    @BindView(R.id.rl_radio)
    RelativeLayout rlRadio;
    @BindView(R.id.tv_radio)
    TextView mTvRadio;
    @BindView(R.id.tv_album)
    TextView mTvAlbum;

    private FriendPhotoAdapter mPhotoAdapter;
    private UserHomePageBean mUserHomePageBean;
    private String mUserId;
    private static String PRIVATE_CHAT = "005";//private chat
    private static String EVEN_WHEAT = "006";//Even the wheat
    private int mAlbumType;
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";
    private RedIndexUser indexUser;

    @Override
    protected int setLayout() {
        return R.layout.red_friend_home_page;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mUserId = getIntent().getStringExtra(GlobalConstants.KEY_USER_ID);
            indexUser = (RedIndexUser) getIntent().getSerializableExtra("indexUser");
        }
        mPhotoAdapter = new FriendPhotoAdapter(this, mUserId, null);
        mRvPhoto.setLayoutManager(new GridLayoutManager(this, 4));
        mRvPhoto.setAdapter(mPhotoAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserHomepage();
    }

    @OnClick({R.id.iv_back, R.id.iv_more, R.id.tv_like, R.id.tv_status_radio, R.id.tv_comment, R.id.tv_chat,
            R.id.tv_call, R.id.tv_unlock, R.id.rl_radio, R.id.iv_avatar})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                BlackRequest.getInstance().isMyBlackList(this, mUserId, new IsMyBlackListCallBack() {
                    @Override
                    public void onSuccess(boolean isBlack) {
                        PullBlackDialog pullBlackDialog = new PullBlackDialog(FriendHomePageActivity.this, isBlack);
                        pullBlackDialog.show();
                        pullBlackDialog.setOnConfirmListener(new PullBlackDialog.OnConfirmListener() {
                            @Override
                            public void onConfirmClick(int type) {
                                if (0 == type) {
                                    String stats = isBlack ? "1" : "0";
                                    BlackRequest.getInstance().addBlackList(FriendHomePageActivity.this, mUserId, stats, new AddBlackListCallBack() {
                                        @Override
                                        public void onSuccess() {
                                            if (isBlack) {
                                                ToastUtils.showToast("已移除黑名单");
                                            } else {
                                                ToastUtils.showToast("已拉入黑名单");
                                            }
                                        }

                                        @Override
                                        public void onFail(String error) {
                                            ToastUtils.showToast(error);
                                        }
                                    });

                                } else {
                                    AnonymousReportActivity.startActivity(FriendHomePageActivity.this, mUserId);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(String error) {
                        ToastUtils.showToast(error);
                    }
                });
                break;
            case R.id.tv_like:
                if (mUserHomePageBean == null) {
                    return;
                }
                int state = mUserHomePageBean.collectStatus;
                CollectRequest collectRequest = new CollectRequest();
                collectRequest.request(this, mUserHomePageBean.userId, String.valueOf(state),
                        new CollectRequest.CollectStatusCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                mUserHomePageBean.collectStatus = state == 1 ? 0 : 1;
                                updateLike(mUserHomePageBean.collectStatus);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
                break;
            case R.id.tv_status_radio:
                HeProgramOnLineActivity.startActivity(FriendHomePageActivity.this, mUserId, mUserHomePageBean.programId);
                break;
            case R.id.tv_comment:
                getComment();
                break;
            case R.id.tv_chat:
                chatOrLianMaiCheck(PRIVATE_CHAT);
                break;
            case R.id.tv_call:
                chatOrLianMaiCheck(EVEN_WHEAT);
                break;
            case R.id.tv_unlock:
                //0公开 1申请访问 2付费
                if (1 == mAlbumType) {
                    CommonHintSingleDialog commonHintSingleDialog = new CommonHintSingleDialog(this,
                            mUserHomePageBean.nickname + "设置了限制", "申请查看需要给对方发一张你的照片", "选择照片");
                    commonHintSingleDialog.show();
                    commonHintSingleDialog.setOnConfirmListener(new CommonHintSingleDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            selectPhoto();
                        }
                    });
                } else if (2 == mAlbumType) {
                    unlockAlbum();
                }
                break;
            case R.id.rl_radio:
                HeMomentDetailsActivity.startActivity(FriendHomePageActivity.this, mUserId);
                break;
            case R.id.iv_avatar:
                if (!TextUtils.isEmpty(mUserId)) {
                    Intent intent = new Intent(this, SingleImagePreviewActivity.class);
                    intent.putExtra(AppConstant.EXTRA_IMAGE_URI, RedAvatarUtils.getAvatarUrl(this, mUserId)
                            + "?" + System.currentTimeMillis());
                    startActivity(intent);
                }
                break;
        }
    }

    /**
     * 连麦
     */
    private void realDial() {
        ChatMessage message = new ChatMessage();
        message.setFromUserId(MyApplication.mMyHomepageBean.userId);
        message.setFromUserName(MyApplication.mMyHomepageBean.nickname);
        message.setToUserId(mUserHomePageBean.userId);
        message.setType(XmppMessage.TYPE_IS_CONNECT_VOICE);
        message.setContent(getString(R.string.sip_invite) + " " + getString(R.string.voice_call));


        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        coreManager.sendChatMessage(mUserHomePageBean.userId, message);

        Intent intent = new Intent(this, Jitsi_pre.class);
        intent.putExtra("type", 1);
        intent.putExtra("fromuserid", MyApplication.mMyHomepageBean.userId);
        intent.putExtra("touserid", mUserHomePageBean.userId);
        intent.putExtra("username", mUserHomePageBean.nickname);
        startActivity(intent);


    }

    private void getComment() {
        CommentRequest.getInstance().getUserComment(this, mUserId, new CommentRequest.UserCommentCallBack() {
            @Override
            public void onSuccess(List<YourCommentBean> commentBeanList) {
                CommentFriendDialog commentFriendDialog = new CommentFriendDialog(FriendHomePageActivity.this, mUserId, commentBeanList);
                commentFriendDialog.show();
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    public static void startFriendHomePageActivity(Context context, String userId) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, FriendHomePageActivity.class);
        intent.putExtra(GlobalConstants.KEY_USER_ID, userId);
        context.startActivity(intent);
    }

    public static void startFriendHomePageActivity(Context context, RedIndexUser indexUser) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, FriendHomePageActivity.class);
        intent.putExtra(GlobalConstants.KEY_USER_ID, indexUser.userId);
        intent.putExtra("indexUser", indexUser);
        context.startActivity(intent);
    }

    private void getUserHomepage() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("userId", mUserId);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_USER_HOME_PAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<UserHomePageBean>(UserHomePageBean.class) {

                    @Override
                    public void onResponse(ObjectResult<UserHomePageBean> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            mUserHomePageBean = result.getData();
                            updateUserInfo(result.getData());
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(FriendHomePageActivity.this);
                    }
                });
    }

    private void updateUserInfo(UserHomePageBean homepageBean) {
        if (homepageBean == null) {
            return;
        }
        mTvNickName.setText(homepageBean.nickname);
        GlideUtils.loadAvatar(mContext, homepageBean.userId, mIvAvatar);
//        Glide.with(this).load(RedAvatarUtils.getAvatarUrl(this, homepageBean.userId)).into(mIvAvatar);
        if (indexUser != null) {
            mTvCity.setText(indexUser.cityName);
            mTvDistance.setText(indexUser.distance);
            mTvOnline.setText(indexUser.onlineStatus);
        }
        if (!TextUtils.isEmpty(homepageBean.ageConstellation) && homepageBean.ageConstellation.contains(";")) {
            mTvAge.setText(SplitUtils.splitAgeConstellation(homepageBean.ageConstellation));
        }
        if (TextUtils.isEmpty(homepageBean.position)) {
            mTvJob.setVisibility(View.GONE);
        } else {
            mTvJob.setVisibility(View.VISIBLE);
            mTvJob.setText(homepageBean.position);
        }
        mTvJob.setText(homepageBean.position);
        updateLike(homepageBean.collectStatus);
        setUserLevel(homepageBean.userLevel);
        if (TextUtils.isEmpty(homepageBean.programId)) {
            mTvRadioStatus.setVisibility(View.GONE);
        } else {
            mTvRadioStatus.setVisibility(View.VISIBLE);
        }
        if (homepageBean.height <= 0) {
            mTvHeight.setText("未填写");
        } else {
            mTvHeight.setText(homepageBean.height + "CM");
        }
        if (homepageBean.weight <= 0) {
            mTvWeight.setText("未填写");
        } else {
            mTvWeight.setText(homepageBean.weight + "KG");
        }
        if (TextUtils.isEmpty(homepageBean.description)) {
            mTvIntro.setText("未填写");
        } else {
            mTvIntro.setText(homepageBean.description);
        }
        if (TextUtils.isEmpty(homepageBean.residentCity)) {
            mTvResidentCity.setText("未填写");
        } else {
            mTvResidentCity.setText(homepageBean.residentCity);
        }
        if (TextUtils.isEmpty(homepageBean.program)) {
            mTvProgram.setText("未填写");
        } else {
            mTvProgram.setText(homepageBean.program);
        }
        if (TextUtils.isEmpty(homepageBean.expectFriend)) {
            mTvExpect.setText("未填写");
        } else {
            mTvExpect.setText(homepageBean.expectFriend);
        }

        updateAlbumStatus(homepageBean.userAlbumVo);

    }

    private void updateLike(int collectStatus) {
        if (1 == collectStatus) {
            mIvLike.setImageResource(R.mipmap.ic_star_checked);
            mTvLike.setText("取消喜欢");
        } else {
            mIvLike.setImageResource(R.mipmap.ic_star);
            mTvLike.setText("加入喜欢");
        }
    }


    /**
     * 私聊或连麦前权限检查
     */
    private void chatOrLianMaiCheck(String type) {
        if (mUserHomePageBean == null) {
            return;
        }
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", type);
        params.put("otherUserId", mUserHomePageBean.userId);
        HttpUtils.post().url(coreManager.getConfig().RED_MY_CHATORLIANMAI_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            if (type.equals(PRIVATE_CHAT)) {
                                Intent intent = new Intent(mContext, ChatActivity.class);
                                Friend friend = new Friend();
                                friend.setNickName(mUserHomePageBean.nickname);
                                friend.setUserId(String.valueOf(mUserId));
                                intent.putExtra(ChatActivity.FRIEND, friend);
                                mContext.startActivity(intent);
                            } else if (type.equals(EVEN_WHEAT)) {
                                realDial();
                            }

                        } else {
                            showBarCom(result.getResultMsg(), result.getResultCode(), type);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(FriendHomePageActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int REAL_PEOPLE_VERIFY_FIRST = 100439;
    private int HAVENOLEGAL_POWER = 100440;
    private int TIMES_HAVE_USED = 100441;

    private void showBarCom(String title, int resultCode, String type) {
        //请先进行真人认证100439;//没有权限 100440;//次数已用完 100441;//没有配置次数，提示升级到VIP或则余额购买 100445
        String confirm = null;
        String center = null;
        if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
            title = type.equals(EVEN_WHEAT) ? "认证你的真实性后，才能连麦！" : "认证你的真实性后，才能私聊！";
            confirm = "马上认证";
        } else if (resultCode == HAVENOLEGAL_POWER) {
            title = type.equals(EVEN_WHEAT) ? "是否与TA连麦？！" : "是否与TA私聊？";
            confirm = type.equals(EVEN_WHEAT) ? "成为会员，免费连麦" : "成为会员，免费私聊";
            center = type.equals(EVEN_WHEAT) ? "连麦（" + mUserHomePageBean.lianMaiGold + "红豆）" : "私聊（" + mUserHomePageBean.lianMaiGold + "红豆";

        } else if (resultCode == TIMES_HAVE_USED) {
            //  confirm = "次数已用完";
            if (MyApplication.mMyHomepageBean != null) {
                boolean[] userLevels = UserLevelUtils.getLevels(MyApplication.mMyHomepageBean.userLevel);
                if (userLevels[1]) {//VIP
                    if (type.equals("005")) {
                        title = "聊天次数已用尽，是否与TA私聊？";
                        center = type.equals(EVEN_WHEAT) ? "连麦（" + mUserHomePageBean.lianMaiGold + "红豆）" : "私聊（" + mUserHomePageBean.privateChatGold + "红豆)";
                    } else if (type.equals("006")) {
                        title = "连麦次数已用尽，是否与TA连麦？";
                        center = type.equals(EVEN_WHEAT) ? "连麦（" + mUserHomePageBean.lianMaiGold + "红豆）" : "私聊（" + mUserHomePageBean.privateChatGold + "红豆)";
                    }
                    EvenWheatCodeDialog commCodeDialog = new EvenWheatCodeDialog(FriendHomePageActivity.this, title, confirm, center);
                    commCodeDialog.show();
                    commCodeDialog.setOnCenterListener(new EvenWheatCodeDialog.OnCenterListener() {
                        @Override
                        public void onCenterClick() {
                            if (type.equals("005")) {
                                payChatPublish(mUserHomePageBean.privateChatGold);
                            } else if (type.equals("006")) {
                                payPublish(mUserHomePageBean.lianMaiGold);
                            }
                        }
                    });
                    return;
                } else {
                    title = type.equals(EVEN_WHEAT) ? "是否与TA连麦？！" : "是否与TA私聊？";
                    confirm = type.equals(EVEN_WHEAT) ? "成为会员，免费连麦" : "成为会员，免费私聊";
                    center = type.equals(EVEN_WHEAT) ? "连麦（" + mUserHomePageBean.lianMaiGold + "红豆）" : "私聊（" + mUserHomePageBean.privateChatGold + "红豆)";
                    EvenWheatCodeDialog commCodeDialog = new EvenWheatCodeDialog(FriendHomePageActivity.this, title, confirm, center);
                    commCodeDialog.show();
                    commCodeDialog.setOnCenterListener(new EvenWheatCodeDialog.OnCenterListener() {
                        @Override
                        public void onCenterClick() {
                            if (type.equals("005")) {
                                payChatPublish(mUserHomePageBean.privateChatGold);
                            } else if (type.equals("006")) {
                                payPublish(mUserHomePageBean.lianMaiGold);
                            }
                        }
                    });
                    commCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            if (type.equals("005")) {
                                VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
                            } else if (type.equals("006")) {
                                VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
                            }
                        }
                    });

                    return;

                }
            }

        } else if (resultCode == HAVENOLEGAL_POWER) {
            title = type.equals(EVEN_WHEAT) ? "是否与TA连麦？！" : "是否与TA私聊？";
            confirm = "成为会员，免费连麦";
            center = type.equals(EVEN_WHEAT) ? "连麦（" + mUserHomePageBean.lianMaiGold + "红豆）" : "私聊（" + mUserHomePageBean.privateChatGold + "红豆)";

        } else if (resultCode == 100445) {
            //没有配置次数，提示升级到VIP或则余额购买
            // confirm = "确认";
            if (type.equals("005")) {
                VipOrPay();
            } else if (type.equals("006")) {
                VipEvenOrPay();
            }
            return;
        } else {

            confirm = "确认";
        }

        EvenWheatCodeDialog commCodeDialog = new EvenWheatCodeDialog(FriendHomePageActivity.this, title, confirm, center);
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
                    AuthenticationCenterActivity.startAuthenticationCenterActivity(FriendHomePageActivity.this);
                } else if (resultCode == HAVENOLEGAL_POWER) {
                    VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
                } else if (resultCode == TIMES_HAVE_USED) {
                    if (type.equals("005")) {
                        payChatPublish(mUserHomePageBean.privateChatGold);
                    } else if (type.equals("006")) {
                        payPublish(mUserHomePageBean.lianMaiGold);
                    }
                } else if (resultCode == 100445) {
                    VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
                }
            }
        });

        //请先进行真人认证100439;//没有权限 100440;//次数已用完 100441;//没有配置次数，提示升级到VIP或则余额购买 100445
        commCodeDialog.setOnCenterListener(new EvenWheatCodeDialog.OnCenterListener() {
            @Override
            public void onCenterClick() {
                if (resultCode == TIMES_HAVE_USED) {
                    if (type.equals("005")) {
                        payChatPublish(mUserHomePageBean.privateChatGold);
                    } else if (type.equals("006")) {
                        payPublish(mUserHomePageBean.lianMaiGold);
                    }
                }
            }
        });
    }

    /**
     * 私聊
     */
    private void VipOrPay() {

        EvenWheatCodeDialog commCodeDialog = new EvenWheatCodeDialog(FriendHomePageActivity.this, "是否与TA私聊？", "成为会员，免费私聊", "私聊（" + mUserHomePageBean.privateChatGold + "红豆)");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                //私聊  买VIP
                VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
            }
        });
        commCodeDialog.setOnCenterListener(new EvenWheatCodeDialog.OnCenterListener() {
            @Override
            public void onCenterClick() {
                //连
                payChatPublish(mUserHomePageBean.privateChatGold);
            }
        });

    }

    /**
     * 连麦 或者支付
     */
    private void VipEvenOrPay() {
        EvenWheatCodeDialog commCodeDialog = new EvenWheatCodeDialog(FriendHomePageActivity.this, "是否与TA连麦？", "成为会员，免费连麦", "连麦（" + mUserHomePageBean.lianMaiGold + "红豆)");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                //买VIP
                VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
            }
        });
        commCodeDialog.setOnCenterListener(new EvenWheatCodeDialog.OnCenterListener() {
            @Override
            public void onCenterClick() {
                payPublish(mUserHomePageBean.lianMaiGold);
            }
        });

    }

    /**
     * 付费聊天
     */
    private void payChatPublish(int payPublish) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("joinUserId", mUserHomePageBean.userId);
        params.put("type", "005");
        params.put("gold", String.valueOf(payPublish));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PAY_FOR_INTERACTION)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            Intent intent = new Intent(mContext, ChatActivity.class);
                            Friend friend = new Friend();
                            friend.setNickName(mUserHomePageBean.nickname);
                            friend.setUserId(String.valueOf(mUserId));
                            intent.putExtra(ChatActivity.FRIEND, friend);
                            mContext.startActivity(intent);
                        } else {
                            ToastUtil.showLongToast(FriendHomePageActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(FriendHomePageActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 红豆支付
     */
    private void payRed(String resultCode) {
        int price = 0;
        if (resultCode.equals("005")) {
            price = mUserHomePageBean.privateChatGold;
        } else if (resultCode.equals("006")) {
            price = mUserHomePageBean.lianMaiGold;
        }
        CommCodeDialog commCodeDialog = new CommCodeDialog(FriendHomePageActivity.this, "次数已用完", "支付" + price + "红豆");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode.equals("005")) {
                    payChatPublish(mUserHomePageBean.privateChatGold);
                } else if (resultCode.equals("006")) {
                    payPublish(mUserHomePageBean.lianMaiGold);
                }

            }
        });
    }

    /**
     * 付费连
     */
    private void payPublish(int payPublish) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("joinUserId", mUserHomePageBean.userId);
        params.put("type", "006");
        params.put("gold", String.valueOf(payPublish));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PAY_FOR_INTERACTION)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            realDial();
                        } else {
                            ToastUtil.showLongToast(FriendHomePageActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(FriendHomePageActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setUserLevel(String userLevel) {
        boolean[] userLevels = UserLevelUtils.getLevels(userLevel);
        if (userLevels[0]) {//女性
            mTvRadioStatus.setText("她正在发起广播哦");
            mTvRadio.setText("她的动态");
            mTvAlbum.setText("她的相册");
        } else {
            mTvRadioStatus.setText("他正在发起广播哦");
            mTvRadio.setText("他的动态");
            mTvAlbum.setText("他的相册");
        }
        if (userLevels[1] && coreManager.getConfig().enablePayModule) {//VIP
            mTvVip.setVisibility(View.VISIBLE);
        } else {
            mTvVip.setVisibility(View.GONE);
        }
        if (userLevels[2]) {//女神
            mTvAuthGirl.setVisibility(View.VISIBLE);
            mTvAuthFace.setVisibility(View.VISIBLE);
        } else {
            mTvAuthGirl.setVisibility(View.GONE);
            if (userLevels[3]) {//真人
                mTvAuthFace.setVisibility(View.VISIBLE);
            } else {
                mTvAuthFace.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void sendAt() {

    }

    @Override
    public void sendAtMessage(String text) {

    }

    @Override
    public void sendText(String text) {

    }

    @Override
    public void sendGif(String text) {

    }

    @Override
    public void sendCollection(String collection) {

    }

    @Override
    public void sendVoice(String filePath, int timeLen, ArrayList<String> strings) {

    }

    @Override
    public void stopVoicePlay() {

    }

    @Override
    public void clickPhoto() {

    }

    @Override
    public void clickCamera() {

    }

    @Override
    public void clickAudio() {

    }

    @Override
    public void clickStartRecord() {

    }

    @Override
    public void clickLocalVideo() {

    }

    @Override
    public void clickVideoChat() {

    }

    @Override
    public void clickLocation() {

    }

    @Override
    public void clickRedpacket() {

    }

    @Override
    public void clickTransferMoney() {

    }

    @Override
    public void clickCollection() {

    }

    @Override
    public void clickCard() {

    }

    @Override
    public void clickFile() {

    }

    @Override
    public void clickContact() {

    }

    @Override
    public void clickShake() {

    }

    @Override
    public void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail) {

    }

    @Override
    public void onInputState() {

    }

    @Override
    public void onEmptyTouch() {

    }

    @Override
    public void onTipMessageClick(ChatMessage message) {

    }

    @Override
    public void onMyAvatarClick() {

    }

    @Override
    public void onFriendAvatarClick(String friendUserId) {

    }

    @Override
    public void LongAvatarClick(ChatMessage chatMessage) {

    }

    @Override
    public void onNickNameClick(String friendUserId) {

    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {

    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {

    }

    @Override
    public void onSendAgain(ChatMessage chatMessage) {

    }

    @Override
    public void onMessageBack(ChatMessage chatMessage, int position) {

    }

    @Override
    public void onCallListener(int type) {

    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {

    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        return false;
    }

    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(FriendHomePageActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(1);
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        // 是否加载视频，默认true
        intent.setLoadVideo(false);
        startActivityForResult(intent, RequestCode.REQUEST_CODE_PICK_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
//                    boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
//                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                } else {
                    ToastUtils.showToast(getString(R.string.c_photo_album_failed));
                }
            }
        }
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Log.e("zq", "原图上传，不压缩，选择原文件路径");
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            mPhotoList.add(stringArrayListExtra.get(i));
        }
        new UploadPhoto().execute();
    }

    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(FriendHomePageActivity.this);
        }

        /**
         * 上传的结果： <br/>
         * return 1 Token过期，请重新登陆 <br/>
         * return 2 上传出错<br/>
         * return 3 上传成功<br/>
         */
        @Override
        protected Integer doInBackground(Void... params) {
            if (!LoginHelper.isTokenValidation()) {
                return 1;
            }
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("access_token", coreManager.getSelfStatus().accessToken);
            mapParams.put("userId", coreManager.getSelf().getUserId() + "");
            mapParams.put("validTime", "-1");// 文件有效期

            String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, mapParams, mPhotoList);
            if (TextUtils.isEmpty(result)) {
                return 2;
            }

            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(FriendHomePageActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        mPhotoUrlList = "";
                        for (int i = 0; i < data.getImages().size(); i++) {
                            mPhotoUrlList += data.getImages().get(i).getOriginalUrl() + ";";
                        }
                        mPhotoUrlList = mPhotoUrlList.substring(0, mPhotoUrlList.length() - 1);
                    } else {
                        return 2;
                    }
                    return 3;
                } else {
                    // 没有文件数据源，失败
                    return 2;
                }
            } else {
                return 2;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(FriendHomePageActivity.this, getString(R.string.upload_failed));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(FriendHomePageActivity.this, getString(R.string.upload_failed));
            } else {
                applyVisitAlbum();
            }
        }
    }

    private void applyVisitAlbum() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(FriendHomePageActivity.this).getSelfStatus().accessToken);
        params.put("userId", mUserId);
        params.put("imageUrl", mPhotoUrlList);

        HttpUtils.post().url(CoreManager.getInstance(FriendHomePageActivity.this).getConfig().RED_PHOTO_APPLY_VISIT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            getUserHomepage();
                            ToastUtils.showToast("您的申请正在审核中");
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void unlockAlbum() {
        if (mUserHomePageBean == null || mUserHomePageBean.userAlbumVo == null) {
            return;
        }
        UserLevelRequest.getInstance().queryUserLevel(FriendHomePageActivity.this, new UserLevelRequest.UserLevelCallBack() {
            @Override
            public void onSuccess(String userLevel) {
                if (UserLevelUtils.getLevels(userLevel)[1]) { //VIP
                    vipUnlockAlbum(mUserHomePageBean.userAlbumVo.coin);
                } else {
                    nonVipUnlockAlbum();
                }
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    private void updateAlbumStatus(UserHomePageBean.UserAlbumVo userAlbumVo) {
        if (userAlbumVo != null) {
            mAlbumType = userAlbumVo.type;//0公开 1申请访问 2付费
            int lockFlag = userAlbumVo.lockFlag;//0未解锁 1已解锁
            if (1 == mAlbumType && 0 == lockFlag) {
                mLlPermission.setVisibility(View.VISIBLE);
                mTvPermissionHint.setText("她设置了限制，查看资料需要请求她的同意");
                mTvPhotoNum.setVisibility(View.INVISIBLE);
                mTvLock.setText("申请查看");
            } else if (2 == mAlbumType && 0 == lockFlag) {
                mLlPermission.setVisibility(View.VISIBLE);
                mTvPermissionHint.setText("她设置了相册锁");
                mTvPhotoNum.setVisibility(View.VISIBLE);
                mTvPhotoNum.setText("有" + userAlbumVo.photoNum + "张照片");
                mTvLock.setText("解锁她的相册(" + userAlbumVo.coin + "红豆)会员免费");
            } else if (2 == lockFlag) {
                mLlPermission.setVisibility(View.VISIBLE);
                mTvLock.setVisibility(View.INVISIBLE);
                mTvPhotoNum.setVisibility(View.INVISIBLE);
                mTvPermissionHint.setText("已发送申请，对方确认后你会收到消息提醒");
            } else {
                mLlPermission.setVisibility(View.GONE);
            }
            mPhotoAdapter.setPhotoNum(userAlbumVo.photoNum);
            mPhotoAdapter.setNewData(userAlbumVo.photos);
        } else {
            mRvPhoto.setVisibility(View.GONE);
        }
    }

    private void vipUnlockAlbum(int coin) {
        FreeAuthTimesRequest.getInstance().queryFreeAuthTimes(mContext, "007", new FreeAuthTimesRequest.FreeAuthTimesCallBack() {
            @Override
            public void onSuccess(QueryFreeAuthBean freeAuthBean) {
                if (freeAuthBean.getFreeTimes() > 0) {//还有次数
                    CommonHintSingleDialog singleDialog = new CommonHintSingleDialog(FriendHomePageActivity.this,
                            "是否使用一次机会解锁她的相册?(你今天还有" + freeAuthBean.getFreeTimes() + "次免费机会)", "免费解锁");
                    singleDialog.show();
                    singleDialog.setOnConfirmListener(new CommonHintSingleDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            UnlockPhotoRequest.getInstance().unlockAlbum(FriendHomePageActivity.this, mUserId, "0", new UnlockPhotoRequest.UnlockCallBack() {
                                @Override
                                public void onSuccess() {
                                    getUserHomepage();
                                }

                                @Override
                                public void onFail(String error) {
                                    ToastUtils.showToast(error);
                                }
                            });
                        }
                    });
                } else {//次数用尽
                    CommonHintSingleDialog singleDialog = new CommonHintSingleDialog(FriendHomePageActivity.this,
                            "你今天免费机会已用完", "解锁(" + coin + "红豆)");
                    singleDialog.show();
                    singleDialog.setOnConfirmListener(new CommonHintSingleDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            UnlockPhotoRequest.getInstance().unlockAlbum(FriendHomePageActivity.this, mUserId, "1",
                                    new UnlockPhotoRequest.UnlockCallBack() {
                                        @Override
                                        public void onSuccess() {
                                            getUserHomepage();
                                        }

                                        @Override
                                        public void onFail(String error) {
                                            ToastUtils.showToast(error);
                                        }
                                    });
                        }
                    });
                }
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    private void nonVipUnlockAlbum() {
        CommonHintDoubleDialog doubleDialog = new CommonHintDoubleDialog(FriendHomePageActivity.this,
                "解锁她的付费相册", "成为会员,免费解锁相册",
                "付费解锁(" + mUserHomePageBean.userAlbumVo.coin + "红豆)");
        doubleDialog.show();
        doubleDialog.setOnConfirmListener(new CommonHintDoubleDialog.OnConfirmListener() {
            @Override
            public void onFirstButtonClick() {
                VipCenterActivity.startVipCenterActivity(FriendHomePageActivity.this);
            }

            @Override
            public void onSecondButtonClick() {
                UnlockPhotoRequest.getInstance().unlockAlbum(FriendHomePageActivity.this, mUserId, "1",
                        new UnlockPhotoRequest.UnlockCallBack() {
                            @Override
                            public void onSuccess() {
                                getUserHomepage();
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });
    }

}
