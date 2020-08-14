package com.sk.weichat.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.example.qrcode.utils.DecodeUtils;
import com.google.zxing.Result;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.adapter.ChatOverviewAdapter;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.BitmapUtil;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.view.SaveWindow;
import com.sk.weichat.view.imageedit.IMGEditActivity;

import java.util.List;

public class ChatOverviewActivity extends BaseActivity {
    public static final int REQUEST_IMAGE_EDIT = 1;
    public static String imageChatMessageListStr;
    private ViewPager mViewPager;
    private ChatOverviewAdapter mChatOverviewAdapter;
    private List<ChatMessage> mChatMessages;
    private int mFirstShowPosition;
    private String mCurrentShowUrl;
    private String mEditedPath;
    private SaveWindow mSaveWindow;
    private My_BroadcastReceivers my_broadcastReceiver = new My_BroadcastReceivers();
    private Rect[] rects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);
        // imageChatMessageListStr = getIntent().getStringExtra("imageChatMessageList");
        mChatMessages = JSON.parseArray(imageChatMessageListStr, ChatMessage.class);
        imageChatMessageListStr = "";
        if (mChatMessages == null) {
            finish();
            return;
        }
        mFirstShowPosition = getIntent().getIntExtra("imageChatMessageList_current_position", 0);
        getCurrentShowUrl(mFirstShowPosition);

        initView();
        register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (my_broadcastReceiver != null) {
            unregisterReceiver(my_broadcastReceiver);
        }
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mViewPager = findViewById(R.id.chat_overview_vp);
        mChatOverviewAdapter = new ChatOverviewAdapter(this, mChatMessages);
        mViewPager.setAdapter(mChatOverviewAdapter);
        mViewPager.setCurrentItem(mFirstShowPosition);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                getCurrentShowUrl(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });


    }


    private void getCurrentShowUrl(int position) {
        if (position >= mChatMessages.size()) {
            // 以防万一，静态变量可能导致各种无法预料的崩溃，
            return;
        }
        ChatMessage chatMessage = mChatMessages.get(position);
        if (!TextUtils.isEmpty(chatMessage.getFilePath()) && FileUtil.isExist(chatMessage.getFilePath())) {
            mCurrentShowUrl = chatMessage.getFilePath();
        } else {
            mCurrentShowUrl = chatMessage.getContent();
        }
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.sk.weichat.broadcast.OtherBroadcast.singledown);
        filter.addAction(com.sk.weichat.broadcast.OtherBroadcast.longpress);
        registerReceiver(my_broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    mCurrentShowUrl = mEditedPath;
                    ChatMessage chatMessage = mChatMessages.get(mViewPager.getCurrentItem());
                    chatMessage.setFilePath(mCurrentShowUrl);
                    mChatMessages.set(mViewPager.getCurrentItem(), chatMessage);
                    mChatOverviewAdapter.refreshItem(mCurrentShowUrl, mViewPager.getCurrentItem());
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class My_BroadcastReceivers extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(com.sk.weichat.broadcast.OtherBroadcast.singledown)) {
                finish();
            } else if (intent.getAction().equals(com.sk.weichat.broadcast.OtherBroadcast.longpress)) {
                // 长按屏幕，弹出菜单
                mSaveWindow = new SaveWindow(ChatOverviewActivity.this,
                        BitmapUtil.getImageIsQRcode(ChatOverviewActivity.this, mCurrentShowUrl), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();
                        switch (v.getId()) {
                            case R.id.save_image:
                                FileUtil.downImageToGallery(ChatOverviewActivity.this, mCurrentShowUrl);
                                break;
                            case R.id.edit_image:
                                ImageLoadHelper.loadFile(
                                        ChatOverviewActivity.this,
                                        mCurrentShowUrl,
                                        f -> {
                                            mEditedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                                            IMGEditActivity.startForResult(ChatOverviewActivity.this, Uri.fromFile(f), mEditedPath, REQUEST_IMAGE_EDIT);
                                        });
                                break;
                            case R.id.identification_qr_code:
                                // 识别图中二维码
                                ImageLoadHelper.loadFile(
                                        ChatOverviewActivity.this,
                                        mCurrentShowUrl,
                                        f -> {
                                            AsyncUtils.doAsync(mContext, t -> {
                                                Reporter.post("二维码解码失败，" + f.getCanonicalPath(), t);
                                                runOnUiThread(() -> {
                                                    Toast.makeText(ChatOverviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                });
                                            }, t -> {
                                                // 做些预处理提升扫码成功率，
                                                // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
                                                Uri decodeUri = Uri.fromFile(f);
                                                Bitmap bitmap = BitmapUtil.getImageIsQRcode(ChatOverviewActivity.this, null, mCurrentShowUrl);
                                                Log.e("zx", "onClick: " + bitmap);
                                                final Result result = DecodeUtils.decodeFromPicture(
                                                        bitmap == null ? DecodeUtils.compressPicture(t.getRef(), decodeUri) : bitmap
                                                );
                                                t.uiThread(c -> {
                                                    if (result != null && !TextUtils.isEmpty(result.getText())) {
                                                        HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                                                    } else {
                                                        Toast.makeText(ChatOverviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            });
                                        }
                                );
                                break;
                        }
                    }
                });
                mSaveWindow.show();
            }
        }
    }
}
