package com.sk.im.audio;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.ImageViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.audio.RecordStateListener;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.UiUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 上一个录像类写的太乱了，重写
 * Create 2019.1.17  by zq
 * 朋友圈发布录音
 */
public class VoiceRecordActivity2 extends BaseActivity implements View.OnClickListener {

    private boolean isRecording;
    private boolean isPlaying;
    private int mMaxRecordTime = 60;// 最大录制时长 default == 60
    private int mLastRecordTime;// 剩余录制时长

    private RecordManager mRecordManager; // 音频录制管理类
    private List<String> mVoicePathList = new ArrayList<>();
    private String mVoiceFinalPath; // 最终合并的文件路径
    private VoicePlayer mVoicePlayer; // 音频播放器
    private CountDownTimer playingCountDownTimer;

    private ImageView mBackIv;

    private TextView tv1, tv2, tv3, tv4;

    private RelativeLayout mRecordTimeRl;
    private TextView mTimeTv;

    private RelativeLayout mStartRl;
    private ImageView mStartIv;

    private LinearLayout mOperatingLl;
    private TextView mLeftTv;
    private TextView mRightTv;

    private RecordStateListener recordStateListener = new RecordStateListener() {
        @Override
        public void onRecordStarting() {
            Log.e("zq", "onRecordStarting");
        }

        @Override
        public void onRecordStart() {
            Log.e("zq", "onRecordStart");
            changeSomething(true);
        }

        @Override
        public void onRecordFinish(String filePath) {
            Log.e("zq", "onRecordFinish");
            changeSomething(false);

            File file = new File(filePath);
            if (file.exists()) {
                mVoicePathList.add(filePath);
            }

            if (mLastRecordTime == 0) {
                finishRecord();
            }
        }

        @Override
        public void onRecordCancel() {
            Log.e("zq", "onRecordCancel");
            changeSomething(false);
        }

        @Override
        public void onRecordVolumeChange(int v) {
            Log.e("zq", "onRecordVolumeChange");
        }

        @Override
        public void onRecordTimeChange(int seconds) {
            Log.e("zq", "onRecordTimeChange：" + seconds);
            mLastRecordTime = mMaxRecordTime - seconds;
            if (mLastRecordTime <= 0) {
                mRecordManager.stop();
            } else {
                mTimeTv.setText(String.valueOf(mLastRecordTime));
            }
        }

        @Override
        public void onRecordTooShoot() {
            Log.e("zq", "onRecordTooShoot");
            changeSomething(false);
            Toast.makeText(VoiceRecordActivity2.this, getString(R.string.tip_record_time_too_short), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRecordError() {
            Log.e("zq", "onRecordError");
            changeSomething(false);
            Toast.makeText(VoiceRecordActivity2.this, getString(R.string.tip_voice_record_error), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        init();
        initView();
        initEvent();
    }

    private void init() {
        mRecordManager = RecordManager.getInstance();
        mRecordManager.setVoiceVolumeListener(recordStateListener);
    }

    private void initView() {
        mBackIv = findViewById(R.id.back_iv);

        tv1 = findViewById(R.id.title_tv1);
        tv2 = findViewById(R.id.title_tv2);
        tv3 = findViewById(R.id.title_tv3);
        tv4 = findViewById(R.id.title_tv4);
        tv1.setText(getString(R.string.jxaudiorecorder_recordertip1));
        tv2.setText(getString(R.string.jxaudiorecorder_recordertip2));
        tv3.setText(getString(R.string.jxaudiorecorder_recordertip3));
        tv4.setText(getString(R.string.jxaudiorecorder_recordertip4));

        mRecordTimeRl = findViewById(R.id.record_time_rl);
        mTimeTv = findViewById(R.id.record_time_tv);

        mStartRl = findViewById(R.id.start_rl);
        mStartIv = findViewById(R.id.start_iv);

        mOperatingLl = findViewById(R.id.operating_ll);
        mLeftTv = findViewById(R.id.left_tv);
        mRightTv = findViewById(R.id.right_tv);
    }

    public void initEvent() {
        mBackIv.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mLeftTv.setOnClickListener(this);
        mRightTv.setOnClickListener(this);
    }

    private void changeSomething(boolean flag) {
        isRecording = flag;
        if (flag) {
            mStartIv.setImageResource(R.mipmap.tounded1_normal);
            ImageViewCompat.setImageTintList(mStartIv, ColorStateList.valueOf(getResources().getColor(R.color.voice)));
            mRecordTimeRl.setVisibility(View.VISIBLE);
            mOperatingLl.setVisibility(View.INVISIBLE);
        } else {
            mStartIv.setImageResource(R.mipmap.triangle1_normal);
            ImageViewCompat.setImageTintList(mStartIv, ColorStateList.valueOf(getResources().getColor(R.color.voice)));
            mOperatingLl.setVisibility(View.VISIBLE);
            mMaxRecordTime = mLastRecordTime;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_iv:
                onBackPressed();
                break;
            case R.id.start_iv:
                // 防止连续点击，
                if (!UiUtils.isNormalClick(v)) {
                    return;
                }
                if (isRecording) {
                    mRecordManager.stop();
                } else {
                    mRecordManager.startRecord();
                }
                break;
            case R.id.left_tv:
                if (isPlaying) {
                    mVoicePlayer.stop();
                    if (playingCountDownTimer != null) {
                        playingCountDownTimer.cancel();
                        mTimeTv.setText(String.valueOf(mLastRecordTime));
                    }
                } else {
                    playVoice();
                }
                break;
            case R.id.right_tv:
                finishRecord();
                break;
        }
    }

    private void playVoice() {
        isPlaying = true;
        linkVoice();
        mStartRl.setVisibility(View.INVISIBLE);
        mLeftTv.setText(getString(R.string.stop));

        mVoicePlayer = null;
        mVoicePlayer = new VoicePlayer();
        mVoicePlayer.play(mVoiceFinalPath);
        mVoicePlayer.setOnFinishPlayListener(new VoicePlayer.OnFinishPlayListener() {
            @Override
            public void onFinishPlay() {
                isPlaying = false;
                mStartRl.setVisibility(View.VISIBLE);
                mLeftTv.setText(getString(R.string.test_listen_voice));
                if (playingCountDownTimer != null) {
                    playingCountDownTimer.cancel();
                    mTimeTv.setText(String.valueOf(mLastRecordTime));
                }
            }
        });

        if (playingCountDownTimer != null) {
            playingCountDownTimer.cancel();
        }
        long timeLength = (61 - mLastRecordTime) * 1000;
        mTimeTv.setText(String.valueOf(timeLength / 1000));
        playingCountDownTimer = new CountDownTimer(timeLength, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeTv.setText(String.valueOf((millisUntilFinished) / 1000));
            }

            @Override
            public void onFinish() {
                mTimeTv.setText(String.valueOf(0));
            }
        };
        playingCountDownTimer.start();
    }

    private void finishRecord() {
        linkVoice();
        File file = new File(mVoiceFinalPath);
        if (file.exists()) {
            EventBus.getDefault().post(new MessageEventVoice(mVoiceFinalPath, (60 - mLastRecordTime) * 1000));
            finish();
        }
    }

    @Override
    public void finish() {
        if (isRecording) {
            mRecordManager.cancel();
        }
        if (isPlaying) {
            mVoicePlayer.stop();
        }
        super.finish();
    }

    /**
     * 连接各个录音片段
     */
    public void linkVoice() {
        if (mVoicePathList.size() == 0) {
            return;
        } else if (mVoicePathList.size() == 1) {
            mVoiceFinalPath = mVoicePathList.get(0);
            return;
        }

        int end = mVoicePathList.get(0).lastIndexOf("/");
        String path = mVoicePathList.get(0).substring(0, end);
        mVoiceFinalPath = path + "/" + coreManager.getSelf().getUserId() + System.currentTimeMillis() + "_voice.amr";

        if (uniteAMRFile(mVoicePathList, mVoiceFinalPath)) {// 合并成功
            // 删除每个语音片段文件
            for (int i = 0; i < mVoicePathList.size(); i++) {
                File file = new File(mVoicePathList.get(i));
                if (file.exists()) {
                    file.delete();
                }
            }
            // 清空list
            mVoicePathList.clear();
            // 将生成的文件路径放入list内
            mVoicePathList.add(mVoiceFinalPath);
        } else {
            Toast.makeText(this, R.string.merger_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 需求:将两个amr格式音频文件合并为1个
     * 注意:amr格式的头文件为6个字节的长度
     *
     * @param partsPaths     各部分路径
     * @param unitedFilePath 合并后路径
     */
    public boolean uniteAMRFile(List<String> partsPaths, String unitedFilePath) {
        try {
            File unitedFile = new File(unitedFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(unitedFile);
            RandomAccessFile randomAccessFile = null;
            for (int i = 0; i < partsPaths.size(); i++) {
                randomAccessFile = new RandomAccessFile(partsPaths.get(i), "r");
                if (i != 0) {
                    randomAccessFile.seek(6);
                }
                byte[] buffer = new byte[1024 * 8];
                int len = 0;
                while ((len = randomAccessFile.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                }
            }
            randomAccessFile.close();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}





