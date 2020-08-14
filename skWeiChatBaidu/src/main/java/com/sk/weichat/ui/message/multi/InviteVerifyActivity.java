package com.sk.weichat.ui.message.multi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.view.CircleImageView;
import com.sk.weichat.view.MyGridView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 群聊邀请验证页面
 */
public class InviteVerifyActivity extends BaseActivity {
    String text, isInvite, reason;
    String id;
    private String mLoginUserId;
    // 邀请者发送的邀请消息，所有的内容都在里面
    private ChatMessage message;
    private String friendId;
    private String packet;
    private String mRoomId;
    private CircleImageView mInviteAvatarIv;
    private TextView mInveiteNameTv;
    private TextView mInveiteCountTv;
    private TextView mInveitReasonTv;
    private MyGridView mVerifyGridView;
    private VerifyAdapter mVerifyAdapter;
    private List<Invite> mInviteList;
    private TextView mSureTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_verify);
        mLoginUserId = coreManager.getSelf().getUserId();

        if (getIntent() != null) {
            friendId = getIntent().getStringExtra("VERIFY_MESSAGE_FRIEND_ID");
            packet = getIntent().getStringExtra("VERIFY_MESSAGE_PACKET");
            mRoomId = getIntent().getStringExtra("VERIFY_MESSAGE_ROOM_ID");
            if (!TextUtils.isEmpty(packet)) {
                message = ChatMessageDao.getInstance().findMsgById(mLoginUserId, friendId, packet);
            }
            if (TextUtils.isEmpty(packet) || message == null) {
                Toast.makeText(this, R.string.tip_get_detail_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        initAction();
        initData();
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
        tvTitle.setText(R.string.invite_detail);
    }

    private void initData() {
        mInviteList = new ArrayList<>();
        List<String> lIst = new ArrayList<>();
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(message.getObjectId());
            id = jsonObject.getString("userIds");
            String name = jsonObject.getString("userNames");
            String[] ids = id.split(",");
            String[] names = name.split(",");
            for (int i = 0; i < ids.length; i++) {
                Invite invite = new Invite();
                invite.setInvitedId(ids[i]);
                invite.setInvitedName(names[i]);
                mInviteList.add(invite);
                lIst.add(ids[i]);
            }

            text = JSON.toJSONString(lIst);
            isInvite = jsonObject.getString("isInvite");
            if (TextUtils.isEmpty(isInvite)) {
                isInvite = "0";
            }
            reason = jsonObject.getString("reason");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mInviteAvatarIv = (CircleImageView) findViewById(R.id.invite_iv);
        mInveiteNameTv = (TextView) findViewById(R.id.invite_name);
        mInveiteCountTv = (TextView) findViewById(R.id.invite_number);
        mInveitReasonTv = (TextView) findViewById(R.id.invite_reasonr);

        AvatarHelper.getInstance().displayAvatar(message.getFromUserName(), message.getFromUserId(), mInviteAvatarIv, false);
        mInveiteNameTv.setText(message.getFromUserName());
        if (isInvite.equals("0")) {
            mInveiteCountTv.setText(getString(R.string.tip_invite_count_place_holder, mInviteList.size()));
        } else {
            mInveiteCountTv.setText(mInviteList.get(0).getInvitedName() + getString(R.string.wanna_in));
        }
        mInveitReasonTv.setText(reason);
        mVerifyGridView = findViewById(R.id.verify_gd);
        mVerifyAdapter = new VerifyAdapter(this, mInviteList);
        mVerifyGridView.setAdapter(mVerifyAdapter);
        mSureTv = (TextView) findViewById(R.id.sure_tv);
        if (message.isDownload()) {// 已确认
            mSureTv.setText(R.string.has_confirm);
            mSureTv.setBackgroundResource(R.drawable.bg_verify_sure_grey);
        } else {// 待确认
            mSureTv.setBackgroundResource(R.drawable.bg_verify_sure);
            mSureTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inviteFriend();
                }
            });
        }
    }

    /**
     * 邀请好友
     */
    private void inviteFriend() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("text", text);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            mSureTv.setBackgroundResource(R.drawable.bg_verify_sure_grey);
                            mSureTv.setText(R.string.has_confirm);

                            List<ChatMessage> mNeedUpdateVerifyMessage = new ArrayList<>();
                            mNeedUpdateVerifyMessage.add(message);

                            // 因为该用户可能请求了很多次，其余请求消息也需要更新
                            List<ChatMessage> verifyMessages = ChatMessageDao.getInstance().getAllVerifyMessage(mLoginUserId, friendId, message.getFromUserId());
                            for (int i = 0; i < verifyMessages.size(); i++) {
                                ChatMessage verifyMessage = verifyMessages.get(i);
                                if (!TextUtils.isEmpty(verifyMessage.getObjectId())
                                        && verifyMessage.getObjectId().contains("isInvite")
                                        && verifyMessage.getObjectId().contains("reason")) {// 基本可以确定为群聊邀请确认
                                    try {
                                        org.json.JSONObject jsonObject = new org.json.JSONObject(verifyMessage.getObjectId());
                                        String isInvite2 = jsonObject.getString("isInvite");
                                        if (TextUtils.isEmpty(isInvite2)) {
                                            isInvite2 = "0";
                                        }
                                        String id2 = jsonObject.getString("userIds");
                                        if (isInvite2.equals(isInvite)) {
                                            if (isInvite.equals("0")) {
                                                if (!TextUtils.isEmpty(id2)
                                                        && id.equals(id2)
                                                        && !verifyMessage.isDownload()) {// 基本可以确定为同一条
                                                    mNeedUpdateVerifyMessage.add(verifyMessage);
                                                }
                                            } else {
                                                if (!verifyMessage.isDownload()) {// 未确认
                                                    mNeedUpdateVerifyMessage.add(verifyMessage);
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            for (int i = 0; i < mNeedUpdateVerifyMessage.size(); i++) {// 更新消息内容 与 确认状态
                                String str = message.getContent().replace(getString(R.string.to_confirm), getString(R.string.has_confirm));
                                ChatMessageDao.getInstance().updateMessageContent(mLoginUserId, friendId, mNeedUpdateVerifyMessage.get(i).getPacketId(), str);
                                ChatMessageDao.getInstance().updateGroupVerifyMessageStatus(mLoginUserId, friendId, mNeedUpdateVerifyMessage.get(i).getPacketId(), true);
                            }
                            setResult(Activity.RESULT_OK, getIntent());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(InviteVerifyActivity.this, getString(R.string.net_exception), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    class VerifyAdapter extends CommonAdapter<Invite> {

        VerifyAdapter(Context context, List<Invite> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.item_verify, position);
            CircleImageView mAvatarIv = viewHolder.getView(R.id.verify_iv);
            TextView mNameTv = viewHolder.getView(R.id.verify_tv);
            AvatarHelper.getInstance().displayAvatar(data.get(position).getInvitedName(), data.get(position).getInvitedId(), mAvatarIv, false);
            mNameTv.setText(data.get(position).getInvitedName());
            return viewHolder.getConvertView();
        }
    }

    class Invite {
        private String invitedId;
        private String invitedName;

        public String getInvitedId() {
            return invitedId;
        }

        public void setInvitedId(String invitedId) {
            this.invitedId = invitedId;
        }

        public String getInvitedName() {
            return invitedName;
        }

        public void setInvitedName(String invitedName) {
            this.invitedName = invitedName;
        }
    }
}
