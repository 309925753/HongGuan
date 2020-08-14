package com.sk.weichat.ui.message.single;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by Administrator on 2017/12/5 0005.
 * 聊天背景
 */

public class SetChatBackActivity extends BaseActivity implements View.OnClickListener {
    private ImageView mChaIv;
    private String mFriendId;
    private String mLoginUserId;
    private String mChatBackgroundPath;
    private String mChatBackground;

    public static void start(Context ctx, String friendId, String path) {
        Intent intent = new Intent(ctx, SetChatBackActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, friendId);
        intent.putExtra(AppConstant.EXTRA_IMAGE_FILE_PATH, path);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_chang_chatbg);
        mFriendId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mChatBackgroundPath = getIntent().getStringExtra(AppConstant.EXTRA_IMAGE_FILE_PATH);
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.preview));
        TextView tvRight = findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.finish));
        tvRight.setOnClickListener(this);
    }

    private void initView() {
        mChaIv = findViewById(R.id.chat_bg);
        File file = new File(mChatBackgroundPath);
        if (file.exists()) { // 加载本地
            if (mChatBackgroundPath.toLowerCase().endsWith("gif")) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(file);
                    mChaIv.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ImageLoadHelper.showFileWithError(
                        SetChatBackActivity.this,
                        file,
                        R.drawable.fez,
                        mChaIv
                );
            }
        } else { // 加载网络
            ImageLoadHelper.showImageWithError(
                    this,
                    mChatBackgroundPath,
                    R.drawable.fez,
                    mChaIv
            );
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title_right:
                DialogHelper.showDefaulteMessageProgressDialog(SetChatBackActivity.this);
                UploadUrl uploadUrl = new UploadUrl();
                uploadUrl.execute(mChatBackgroundPath);
                break;
        }
    }

    private void sureSet() {
        PreferenceUtils.putString(SetChatBackActivity.this, Constants.SET_CHAT_BACKGROUND_PATH
                + mFriendId + mLoginUserId, mChatBackgroundPath);

        PreferenceUtils.putString(SetChatBackActivity.this, Constants.SET_CHAT_BACKGROUND
                + mFriendId + mLoginUserId, mChatBackground);
        Intent intent = new Intent();
        intent.putExtra("Operation_Code", 1);
        intent.setAction(com.sk.weichat.broadcast.OtherBroadcast.QC_FINISH);
        sendBroadcast(intent); // 设置聊天背景成功，发送广播更新单聊界面
        finish();
    }

    class UploadUrl extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("userId", coreManager.getSelf().getUserId());
            params.put("validTime", "-1");// 文件有效期

            List<String> filePathList = new ArrayList<>();
            filePathList.add(strings[0]);
            return new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, params, filePathList);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            DialogHelper.dismissProgressDialog();
            UploadFileResult recordResult = JSON.parseObject(s, UploadFileResult.class);
            List<UploadFileResult.Sources> images = recordResult.getData().getImages();
            mChatBackground = images.get(0).getOriginalUrl();
            sureSet();
        }
    }
}
