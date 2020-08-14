package com.sk.weichat.ui.message.single;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.CameraUtil;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.photopicker.PhotoPickerActivity;
import com.sk.weichat.view.photopicker.SelectModel;
import com.sk.weichat.view.photopicker.intent.PhotoPickerIntent;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/5 0005.
 * 设置聊天背景
 */

public class SelectSetTypeActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_PICK_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private String mFriendId;
    private String mLoginUserId;
    private Uri mNewPhotoUri;
    private FinishBroadcastReceiver mReceiver = new FinishBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_select_set_type);
        mFriendId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.set_chat_bg));
    }

    private void initView() {
        findViewById(R.id.select_from_phone_album_rl).setOnClickListener(this);
        findViewById(R.id.take_picture_rl).setOnClickListener(this);
        findViewById(R.id.restore_default_background_rl).setOnClickListener(this);
        register();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_from_phone_album_rl:
                selectPhoto();
                break;
            case R.id.take_picture_rl:
                takePhoto();
                break;
            case R.id.restore_default_background_rl:
                restore();
                break;
        }
    }

    private void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH);
        registerReceiver(mReceiver, intentFilter);
    }

    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(this);
        intent.setSelectModel(SelectModel.MULTI);
        intent.setShowCarema(false);
        intent.setMaxTotal(1);
        intent.setSelectedPaths(imagePaths);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    private void takePhoto() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_TAKE_PHOTO);
    }

    private void restore() {
        PreferenceUtils.putString(this, Constants.SET_CHAT_BACKGROUND_PATH
                + mFriendId + mLoginUserId, "reset");

        PreferenceUtils.putString(this, Constants.SET_CHAT_BACKGROUND
                + mFriendId + mLoginUserId, "reset");

        Intent intent = new Intent();
        intent.putExtra("Operation_Code", 1);
        intent.setAction(com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH);
        sendBroadcast(intent); // 恢复聊天背景成功，发送广播更新单聊界面
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                ArrayList<String> stringArrayListExtra = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                SetChatBackActivity.start(mContext, mFriendId, stringArrayListExtra.get(0));
            } else {
                ToastUtil.showToast(this, R.string.c_photo_album_failed);
            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (mNewPhotoUri != null) {
                SetChatBackActivity.start(mContext, mFriendId, mNewPhotoUri.getPath());
            } else {
                ToastUtil.showToast(this, R.string.c_take_picture_failed);
            }
        }
    }

    class FinishBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH)) {
                finish();
            }
        }
    }
}
