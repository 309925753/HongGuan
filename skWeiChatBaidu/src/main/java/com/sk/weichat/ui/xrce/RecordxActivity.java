package com.sk.weichat.ui.xrce;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.joe.camera2recorddemo.View.CameraRecordView;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.audio_x.VoiceManager;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.helper.CutoutHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.me.LocalVideoActivity;
import com.sk.weichat.ui.trill.MarqueTextView;
import com.sk.weichat.util.RecorderUtils;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.video.FilterPreviewDialog;
import com.sk.weichat.view.cjt2325.cameralibrary.CameraInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class RecordxActivity extends BaseActivity implements View.OnClickListener {

    private static final int STATE_INIT = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_PAUSE = 2;
    private static final int REQUEST_CODE_SELECT_VIDEO = 10;
    BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private Xrecprogressbar xbar;
    private CameraRecordView mRecordView;
    private List<EpVideo> videos;
    private int mRecorderState;
    private String mCurrBgmPath;
    private String mCurrBgmName;
    private String mCurrPath;
    private SelectMusicDialog mSelectDialog;
    private RecordButton mRecordBtn;
    private RelativeLayout rlMore;
    FilterPreviewDialog.OnUpdateFilterListener listener = new FilterPreviewDialog.OnUpdateFilterListener() {
        @Override
        public void select(int type) {
            mRecordView.switchFilter(type);
        }

        @Override
        public void dismiss() {
            rlMore.setVisibility(View.VISIBLE);
            mRecordBtn.setVisibility(View.VISIBLE);
        }
    };
    private FilterPreviewDialog mDialog;
    private FrameLayout waitPar;
    private MarqueTextView tvBgName;
    private LinearLayout llMusic;
    private View ivDel;
    private View ivLocal;
    private boolean isStop = false;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            waitPar.setVisibility(View.GONE);
            switch (msg.what) {
                case RecorderUtils.ACTIVATE_BTN: // 激活按钮
                    mRecordBtn.setEnabled(true);
                    break;
                case RecorderUtils.MUSIC_SUCCESS: // 音乐拼合成功
                    intentPreview(mCurrPath);
                    break;
                case RecorderUtils.MERGE_FAILURE: // 视频合并失败
                    showToast(getString(R.string.flatten_failure));
                    break;
            }
            return false;
        }
    });
    private int handlerTime = 0;// 连续对焦

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        CutoutHelper.setWindowOut(getWindow());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recx);

        CutoutHelper.initCutoutHolderTop(getWindow(), findViewById(R.id.vCutoutHolder));
        xbar = findViewById(R.id.xpbar);
        xbar.setTotalTime(coreManager.getConfig().videoLength);
        mRecordView = findViewById(R.id.surfaceView);
        rlMore = findViewById(R.id.rl_more);
        mRecordBtn = findViewById(R.id.btn_rec);
        waitPar = findViewById(R.id.progress_ing);
        tvBgName = findViewById(R.id.tv_bgname);
        llMusic = findViewById(R.id.ll_select_music);
        ivDel = findViewById(R.id.iv_del);
        ivLocal = findViewById(R.id.iv_local);

        findViewById(R.id.btn_rec).setOnClickListener(this);
        findViewById(R.id.ll_filter).setOnClickListener(this);
        findViewById(R.id.ll_swith).setOnClickListener(this);
        findViewById(R.id.iv_comp).setOnClickListener(this);
        findViewById(R.id.iv_del).setOnClickListener(this);
        findViewById(R.id.iv_local).setOnClickListener(this);
        findViewById(R.id.ll_back).setOnClickListener(this);
        llMusic.setOnClickListener(this);
        waitPar.setOnClickListener(this);

        videos = new ArrayList<>();
        mDialog = new FilterPreviewDialog(this, listener);
        xbar.addOnComptListener(() -> {
            Log.e("xuan", "onCompte: ");
            stopRecord();
            mRecorderState = STATE_INIT;
            refreshControlUI();
            compteRecord();
        });

        mSelectDialog = new SelectMusicDialog(this, info -> {
            mCurrBgmPath = info.path;
            mCurrBgmName = info.getName();
            tvBgName.setText(info.getName() + "  " + info.getName() + "   " + info.getName());
            tvBgName.setTextColor(getResources().getColor(R.color.white));
        }, getToken(), getAppConfig().GET_MUSIC_LIST, getAppConfig().downloadUrl);

        broadcast();
        // 对焦
        mRecordView.post(() -> handleFocus(ScreenUtil.getScreenWidth(mContext) / 2, ScreenUtil.getScreenHeight(mContext) / 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isStop) {
            VoiceManager.instance().pause();
        }
        stopRecord();
        mRecorderState = STATE_INIT;
        //  refreshControlUI();
    }

    @Override
    public void onBackPressed() {
        if (waitPar.getVisibility() == View.VISIBLE) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReceiver);
    }

    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MsgBroadcast.ACTION_MSG_CLOSE_TRILL);
        registerReceiver(closeReceiver, intentFilter);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ll_filter:
                mDialog.show();
                rlMore.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.GONE);
                break;
            case R.id.btn_rec:
                if (mRecorderState == STATE_INIT) {
                    if (!xbar.isNotOver()) {
                        showToast(getString(R.string.delete_some));
                        return;
                    }

                    if (!TextUtils.isEmpty(mCurrBgmPath)) {
                        VoiceManager.instance().play(mCurrBgmPath);
                        int msec = xbar.getCurrentPro() - 1800;
                        if (msec < 0) {
                            msec = 0;
                        }
                        VoiceManager.instance().seek(msec);
                    }

                    //开始录制视频
                    if (startRecord(RecorderUtils.getVideoFileByTime())) {
                        mRecorderState = STATE_RECORDING;
                        refreshControlUI();
                    }

                } else if (mRecorderState == STATE_RECORDING) {
                    //停止视频录制
                    VoiceManager.instance().pause();
                    stopRecord();
                    mRecorderState = STATE_INIT;
                    refreshControlUI();
                }
                break;
            case R.id.ll_swith:
                if (UiUtils.isNormalClick(v)) {
                    // 这个切换摄像头连续切换会在部分设备崩溃，测试没法根治，只能限制点击频率了，
                    mRecordView.switchCamera();
                }
                break;
            case R.id.iv_comp:
                compteRecord();
                break;
            case R.id.iv_del:
                // 删除一段视频
                popDelVideo();
                refreshControlUI();
                break;
            case R.id.iv_local:
                // 选择本地视频，
                Intent intent = new Intent(this, LocalVideoActivity.class);
                intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                break;
            case R.id.ll_back: // 退出录制
                VoiceManager.instance().pause();
                mRecorderState = STATE_INIT;
                refreshControlUI();
                stopRecord();
                exitRecord();
                break;
            case R.id.ll_select_music: // 退出录制
                mSelectDialog.show();
                break;
        }
    }

    /**
     * 开始录制
     *
     * @return
     */
    private boolean startRecord(String path) {
        try {
            Log.e("xuan", "开始录制：" + path);
            mRecordView.startRecord(path);
            videos.add(new EpVideo(path));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 结束录制
     *
     * @return
     */
    private boolean stopRecord() {
        try {
            mRecordView.stopRecord();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void exitRecord() {
        for (int i = videos.size() - 1; i > -1; i--) {
            EpVideo video = videos.get(i);
            RecorderUtils.delVideoFile(video.getVideoPath());
        }
        finish();
    }

    /**
     * 删除上一段视频
     */
    private void popDelVideo() {
        if (videos.size() > 0) {
            EpVideo video = videos.get(videos.size() - 1);
            RecorderUtils.delVideoFile(video.getVideoPath());
            videos.remove(videos.size() - 1);
        }

        if (videos.size() == 0) {
            llMusic.setVisibility(View.VISIBLE);
        }

        xbar.popTask();

        Log.e("xuan", "popDelVideo: " + videos.size());
    }

    /**
     * 完成录制
     */
    public void compteRecord() {
        VoiceManager.instance().stop();
        int length = videos == null ? 0 : videos.size();
        if (length == 0) {
            showToast(getString(R.string.record_frist_video));
        } else if (length == 1) { // 直接去拼合音乐
            showWaitDialog();
            EpVideo video = videos.get(0);
            appendBgm(video.getVideoPath());
        } else {
            VoiceManager.instance().stop();
            showWaitDialog();
            // 先去拼合视频
            Log.e("xuan", "即将要拼合: " + videos.size() + "个视频");
            final String outFile = RecorderUtils.getVideoFileByTime();
            EpEditor.mergeByLc(this, videos, new EpEditor.OutputOption(outFile), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    appendBgm(outFile);
                }

                @Override
                public void onFailure() {
                    Log.e("xuan", "合并失败");
                    handler.sendEmptyMessage(RecorderUtils.MERGE_FAILURE);
                }

                @Override
                public void onProgress(float progress) {
                    //这里获取处理进度
                    Log.e("xuan", "正在合并" + progress);
                }
            });
        }
    }

    private void appendBgm(final String filePath) {
        if (TextUtils.isEmpty(mCurrBgmPath)) {
            mCurrPath = filePath;
            handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            return;
        }

        final String outfilePath = RecorderUtils.getVideoFileByTime();
        mCurrPath = outfilePath;

        // 合并音效
        EpEditor.music(filePath, mCurrBgmPath, outfilePath, 0f, 1f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onFailure() {
                mCurrPath = filePath;
                handler.sendEmptyMessage(RecorderUtils.MUSIC_SUCCESS);
            }

            @Override
            public void onProgress(float progress) {
                //这里获取处理进度
                Log.e("xuan", "music正在合并" + progress);
            }
        });
    }

    private void refreshControlUI() {
        if (mRecorderState == STATE_RECORDING) {
            //1s后才能按停止录制按钮
            mRecordBtn.setEnabled(false);
            handler.sendEmptyMessageDelayed(RecorderUtils.ACTIVATE_BTN, 1000);
            mRecordBtn.record();
            rlMore.setVisibility(View.GONE);
            llMusic.setVisibility(View.INVISIBLE);
            xbar.record();
        } else if (mRecorderState == STATE_INIT) {
            mRecordBtn.pause();
            xbar.pause();
            rlMore.setVisibility(View.VISIBLE);
            if (videos.size() == 0) {
                ivDel.setVisibility(View.GONE);
                ivLocal.setVisibility(View.VISIBLE);
            } else {
                ivDel.setVisibility(View.VISIBLE);
                ivLocal.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 跳转到视频预览界面
     */
    private void intentPreview(String filePath) {
        isStop = true;

        videos.clear();
        xbar.reset();
        llMusic.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, PreviewxActivity.class);
        intent.putExtra("file_path", filePath);
        if (!TextUtils.isEmpty(mCurrBgmName)) {
            intent.putExtra("music_name", mCurrBgmName);
        }
        startActivity(intent);
    }

    public void showWaitDialog() {
        waitPar.setVisibility(View.VISIBLE);
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void handleFocus(float x, float y) {
        Camera mCamera = mRecordView.getCamera();
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = CameraInterface.calculateTapArea(x, y, 1f, this);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            return;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus((success, camera) -> {
                if (success || handlerTime > 10) {
                    Camera.Parameters params1 = camera.getParameters();
                    params1.setFocusMode(currentFocusMode);
                    camera.setParameters(params1);
                    handlerTime = 0;
                } else {
                    handlerTime++;
                    handleFocus(x, y);
                }
            });
        } catch (Exception e) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_VIDEO:
                    // 选择视频返回
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // 不可到达，列表里有做判断，
                        Reporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // 不可到达，列表里有做过滤，
                                Reporter.unreachable();
                            } else {
                                videos.clear();
                                videos.add(new EpVideo(filePath));
                                handler.postDelayed(() -> {
                                    // 立即调用结束录制跳到预览会出现播放失败的情况，原因不明，可能是什么资源没来得及释放导致冲突，总之post一段时间就好，
                                    // 测试下来延迟100ms不行，延迟500ms正常，
                                    compteRecord();
                                }, 500);
                            }
                        }
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
