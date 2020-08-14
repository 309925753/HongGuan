package com.sk.weichat.video;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.sk.weichat.R;
import com.sk.weichat.helper.CutoutHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.JCameraView;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.ClickListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.ErrorListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.JCameraListener;
import com.sk.weichat.view.imageedit.IMGEditActivity;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 只支持拍照的Activity
 */
public class EasyCameraActivity extends Activity {
    public static final int REQUEST_IMAGE_EDIT = 1;

    private JCameraView jCameraView;
    private String editedPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CutoutHelper.setWindowOut(getWindow());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_camera);

        // 全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }

        initView();
    }

    private void initView() {
        CutoutHelper.initCutoutHolderTop(getWindow(), findViewById(R.id.vCutoutHolder));
        jCameraView = findViewById(R.id.jcameraview);

        // 设置只能录像或只能拍照或两种都可以（默认两种都可以）
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);

        // 设置视频保存路径
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");

        // 设置视频质量
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        // JCameraView监听
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                // 打开Camera失败回调
                Log.i("CJT", "open camera error");
            }

            @Override
            public void AudioPermissionError() {
                // 没有录取权限回调
                Log.i("CJT", "AudioPermissionError");
            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                // 获取图片bitmap
                Log.i("CJT", "bitmap = " + bitmap.getWidth());
                String path = FileUtil.saveBitmap(bitmap);
                if (!TextUtils.isEmpty(path)) {
                    EventBus.getDefault().post(new MessageEventGpu(path));
                } else {
                    DialogHelper.tip(EasyCameraActivity.this, "图片存储失败");
                }
                finish();
            }

            @Override
            public void onEditClick(Bitmap bitmap) {
                String path = FileUtil.saveBitmap(bitmap);
                if (!TextUtils.isEmpty(path)) {
                    editedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                    IMGEditActivity.startForResult(EasyCameraActivity.this, Uri.fromFile(new File(path)), editedPath, REQUEST_IMAGE_EDIT);
                } else {
                    DialogHelper.tip(EasyCameraActivity.this, "图片存储失败");
                }
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                // 获取视频路径
                Log.i("CJT", "url = " + url);
            }
        });

        // 左边按钮点击事件
        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });

        // 右边按钮点击事件
        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    Bitmap bitmap = BitmapFactory.decodeFile(editedPath);
                    jCameraView.showPicture(bitmap);
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
        }
    }
}
