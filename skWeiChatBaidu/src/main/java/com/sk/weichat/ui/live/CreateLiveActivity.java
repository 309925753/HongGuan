package com.sk.weichat.ui.live;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.live.bean.LiveRoom;
import com.sk.weichat.util.FastBlurUtil;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 创建直播间
 */
public class CreateLiveActivity extends BaseActivity implements View.OnClickListener {
    private String mAccessToken;
    private String mLoginUserId;
    private String mLoginNickName;
    private AutoCompleteTextView mTvName, mTvContent;
    private Button btn;
    private RelativeLayout mHideSoftware;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_live);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        mAccessToken = coreManager.getSelfStatus().accessToken;
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        ImageView ivMenuBg = (ImageView) findViewById(R.id.iv_start_live_bg);
        // 为背景添加虚化效果
        int scaleRatio = 10;// blurry multiple
        Bitmap scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.live_backgroud3);
        Bitmap blurBitmap = FastBlurUtil.toBlur(scaledBitmap, scaleRatio);
        ivMenuBg.setImageBitmap(blurBitmap);

        findViewById(R.id.iv_start_live_back).setOnClickListener(this);
        // 头像
        ImageView ivHead = (ImageView) findViewById(R.id.iv_live_head);
        AvatarHelper.getInstance().displayAvatar(mLoginUserId, ivHead, true);
        mTvName = (AutoCompleteTextView) findViewById(R.id.tv_live_name);
        mTvContent = (AutoCompleteTextView) findViewById(R.id.tv_live_content);
        mTvName.setCompletionHint(getString(R.string.live_inputroomname));
        mTvContent.setCompletionHint(getString(R.string.live_inputroomnotice));
        btn = (Button) findViewById(R.id.start);
        btn.setText(getString(R.string.live_start_live));
        btn.setOnClickListener(this);
        mHideSoftware = (RelativeLayout) findViewById(R.id.hide_software);
        mHideSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mHideSoftware.getWindowToken(), 0); // 强制隐藏键盘
            }
        });

        mTvName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {// 失去焦点，判断公告是否为空，为空则赋值房间名
                if (TextUtils.isEmpty(mTvContent.getText().toString())
                        && !TextUtils.isEmpty(mTvName.getText().toString())) {
                    mTvContent.setText(mTvName.getText().toString());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_start_live_back:
                finish();
                break;
            case R.id.start:
                if (TextUtils.isEmpty(mTvContent.getText().toString())
                        && !TextUtils.isEmpty(mTvName.getText().toString())) {
                    mTvContent.setText(mTvName.getText().toString());
                }
                btn.setOnClickListener(null);// 防止用户频繁点击创建按钮而创建多个直播间
                String name = mTvName.getText().toString();
                String content = mTvContent.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    mTvName.setError(getString(R.string.name_cannot_ull));
                    btn.setOnClickListener(this);// 恢复点击事件
                    return;
                }
                if (TextUtils.isEmpty(content)) {
                    mTvContent.setError(getString(R.string.notice_cannot_null));
                    btn.setOnClickListener(this);// 恢复点击事件
                    return;
                }
                DialogHelper.showDefaulteMessageProgressDialog(this);
                showCreateGroupChatDialog(name, content);
                break;
        }
    }

    private void showCreateGroupChatDialog(String name, String content) {
        // 聊天室名称&聊天室描述
        createGroupChat(name, null, content);
    }

    private void createGroupChat(String roomName, String roomSubject, final String roomDesc) {
        final String roomJid = coreManager.createMucRoom(roomName);
        if (TextUtils.isEmpty(roomJid)) {
            DialogHelper.dismissProgressDialog();
            Toast.makeText(mContext, getString(R.string.create_room_failed), Toast.LENGTH_SHORT).show();
            btn.setOnClickListener(this);// 恢复点击事件
            return;
        }
        // 去服务器创建直播间
        openLive(roomJid, roomName, roomDesc);
    }

    private void openLive(String roomJid, String roomName, String roomDesc) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("userId", mLoginUserId);
        params.put("nickName", mLoginNickName);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("notice", roomDesc);
        HttpUtils.get().url(coreManager.getConfig().CREATE_LIVE_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<LiveRoom>(LiveRoom.class) { // 创建直播间成功，进入推流界面
                    @Override
                    public void onResponse(ObjectResult<LiveRoom> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            LiveRoom room = result.getData();
                            Intent intent = new Intent(CreateLiveActivity.this, PushFlowActivity.class);
                            intent.putExtra(LiveConstants.LIVE_PUSH_FLOW_URL, room.getUrl());
                            intent.putExtra(LiveConstants.LIVE_ROOM_ID, room.getRoomId());
                            intent.putExtra(LiveConstants.LIVE_CHAT_ROOM_ID, room.getJid());
                            intent.putExtra(LiveConstants.LIVE_ROOM_NAME, room.getName());
                            intent.putExtra(LiveConstants.LIVE_ROOM_PERSON_ID, String.valueOf(room.getUserId()));
                            intent.putExtra(LiveConstants.LIVE_ROOM_NOTICE, String.valueOf(room.getNotice()));
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        btn.setOnClickListener(CreateLiveActivity.this);// 恢复点击事件
                        ToastUtil.showNetError(CreateLiveActivity.this);
                    }
                });
    }
}
