package com.sk.im.audio;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.audio.RecordStateListener;
import com.sk.weichat.ui.base.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 朋友圈发布录音
 * 原录音界面，写的太乱，已废弃
 */
public class VoiceRecordActivity extends BaseActivity {
    private static final int STOP = 0;   // 停止
    private static final int RECORD = 1; // 正在录制
    private static final int PAUSE = 2;  // 暂停
    private static final int PREVIEW = 3;// 试听
    private static final int PLAY = 4; // 播放状态
    ImageView backIv;
    LinearLayout titleTv;
    TextView tv1, tv2, tv3, tv4;
    TextView timeTv;
    TextView linTv;
    ImageView startIv;
    ImageView controlIv;
    TextView startTv;
    TextView leftTv;
    TextView rightTv;
    long statrcurent;
    long stopcurent;

    private RecordManager recordManager; // 音频录制管理类

    private int status = STOP; //当前的状态   0表示停止录制  1表示正在录制  2表示暂停录制
    private int time = 0; // 最终录制的时间
    private int remainingTime = 60;// 剩余的时间
    private int minTime = 0; // 每次录音的最短的时间
    private List<String> voices;// 所有间断产生的音频文件的路径
    private String myVoice; // 最终合并的文件路径
    private boolean isCancel = false; // 标记
    RecordStateListener recordStateListener = new RecordStateListener() {
        @Override
        public void onRecordStarting() {
        }

        @Override
        public void onRecordCancel() {
        }

        @Override
        public void onRecordVolumeChange(int v) {
        }

        @Override
        public void onRecordError() {
        }

        @Override
        public void onRecordTooShoot() {
        }

        @Override
        public void onRecordTimeChange(int seconds) {
        }

        @Override
        public void onRecordStart() {
            statrcurent = System.currentTimeMillis();
            new RecordTime().start();
        }

        @Override
        public void onRecordFinish(String file) {
            stopcurent = System.currentTimeMillis();
            File voiceFile = new File(file);

            if (isCancel && voiceFile.exists()) {
                voiceFile.delete();
                isCancel = false;
                return;
            }

            if (voiceFile.exists()) {
                voices.add(file);
            }
        }
    };
    /*更新UI   Handler */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RECORD) {
                timeTv.setText(remainingTime + "");
            } else if (msg.what == STOP) {
                if (status == PAUSE) {
                    showPager();
                    stop();
                } else {
                    status = STOP;
                    showPager();
                    linkVoice();
                }
            } else if (msg.what == PAUSE) {
                remainingTime++; // 录音保存不成功，把减去的时间恢复
                showPager();
                isCancel = true;
                stop();
            }
        }
    };
    private VoicePlayer voicePlayer; // 音频播放器
    private int voicePlayStatus = STOP; // 音频的播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_act_voice_record);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        backIv = (ImageView) findViewById(R.id.back_iv);
        titleTv = (LinearLayout) findViewById(R.id.title_tv);
        timeTv = (TextView) findViewById(R.id.time_tv);
        linTv = (TextView) findViewById(R.id.lin_tv);
        startIv = (ImageView) findViewById(R.id.start_iv);
        controlIv = (ImageView) findViewById(R.id.control_iv);
        startTv = (TextView) findViewById(R.id.start_tv);
        leftTv = (TextView) findViewById(R.id.left_tv);
        rightTv = (TextView) findViewById(R.id.right_tv);
        tv1 = (TextView) findViewById(R.id.title_tv1);
        tv2 = (TextView) findViewById(R.id.title_tv2);
        tv3 = (TextView) findViewById(R.id.title_tv3);
        tv4 = (TextView) findViewById(R.id.title_tv4);
        tv1.setText(getString(R.string.jxaudiorecorder_recordertip1));
        tv2.setText(getString(R.string.jxaudiorecorder_recordertip2));
        tv3.setText(getString(R.string.jxaudiorecorder_recordertip3));
        tv4.setText(getString(R.string.jxaudiorecorder_recordertip4));
        leftTv.setText(getString(R.string.cancel));
        startTv.setText(getString(R.string.click_recode));
        rightTv.setText(getString(R.string.upload));
        recordManager = RecordManager.getInstance();
        showPager();
        initEvent();
        recordManager.setVoiceVolumeListener(recordStateListener);
        voices = new ArrayList<>(); // 储存录制的所有音频文件的路径
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (status == STOP) // 当前为停止状态，点击后进入正在录制界面
            {
                finish();
            } else if (status == RECORD) // 当前为正在录制界面，点击后进入暂停录制界面
            {
                status = PAUSE;
                showPager();
            } else if (status == PAUSE || status == PREVIEW)//当前为暂停录制界面，点击后恢复停止状态
            {
                status = STOP;
                time = 0;
                remainingTime = 60;
                showPager();
                cancel();
                finish();
            }

        }
        return true;
    }

    private void showPager() {
        switch (status) {
            case STOP: // 停止录制
            {
                controlIv.setBackgroundResource(R.mipmap.voice_complete2);
                startIv.setImageResource(R.mipmap.tape_normal);
                leftTv.setVisibility(View.GONE);
                rightTv.setVisibility(View.GONE);
                startTv.setText(getString(R.string.click_recode));
                linTv.setVisibility(View.GONE);
                timeTv.setVisibility(View.GONE);
                backIv.setVisibility(View.VISIBLE);
                backIv.setImageResource(R.mipmap.return_icon);
                titleTv.setVisibility(View.VISIBLE);
            }
            break;

            case RECORD: // 正在录制
            {
                controlIv.setBackgroundResource(R.mipmap.voice_complete1);
                startIv.setImageResource(R.mipmap.tounded1_normal);
                leftTv.setVisibility(View.GONE);
                rightTv.setVisibility(View.GONE);
                startTv.setText(getString(R.string.audiorecorder_pause_recorder));
                rightTv.setText(getString(R.string.test_listen_voice));
                linTv.setVisibility(View.VISIBLE);
                timeTv.setVisibility(View.VISIBLE);
                backIv.setVisibility(View.VISIBLE);
                backIv.setImageResource(R.mipmap.fork);
                titleTv.setVisibility(View.VISIBLE);
            }
            break;

            case PAUSE:// 暂停录制
            {
                controlIv.setBackgroundResource(R.mipmap.voice_complete1);
                startIv.setImageResource(R.mipmap.triangle1_normal);
                leftTv.setVisibility(View.VISIBLE);
                rightTv.setVisibility(View.VISIBLE);
                startTv.setText(getString(R.string.audiorecorder_continue_recorder));
                linTv.setVisibility(View.GONE);
                timeTv.setVisibility(View.VISIBLE);
                backIv.setVisibility(View.GONE);
                titleTv.setVisibility(View.GONE);
            }
            break;

            case PREVIEW: // 试听
            {
                controlIv.setBackgroundResource(R.mipmap.voice_complete1);
                startIv.setImageResource(R.mipmap.triangle1_normal);
                leftTv.setVisibility(View.VISIBLE);
                rightTv.setVisibility(View.VISIBLE);
                rightTv.setText(getString(R.string.upload));
                startTv.setText(getString(R.string.stop));
                linTv.setVisibility(View.GONE);
                timeTv.setVisibility(View.VISIBLE);
                backIv.setVisibility(View.GONE);
                titleTv.setVisibility(View.GONE);
            }
            break;
        }
    }

    public void start() {
        recordManager.startRecord();
    }

    public void stop() {
        recordManager.stop();
    }

    public void pause() {
        recordManager.stop();
    }

    public void cancel() {
        if (voicePlayer != null) {
            if (voicePlayStatus == PLAY) {
                voicePlayer.stop();
            }
        }
        startTv.setText(getString(R.string.click_recode));
        timeTv.setText("");
        startIv.setImageResource(R.mipmap.tape_normal);
        /*取消的时候删除录音的临时文件*/
        if (voices.isEmpty()) {
            return;
        }
        for (int i = 0; i < voices.size(); i++) {
            File file = new File(voices.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
        voices.clear(); // 清除路径
    }

    /**
     * 需求:将两个amr格式音频文件合并为1个
     * 注意:amr格式的头文件为6个字节的长度
     *
     * @param partsPaths     各部分路径
     * @param unitedFilePath 合并后路径
     */
    public void uniteAMRFile(List<String> partsPaths, String unitedFilePath) {
        try {
            File unitedFile = new File(unitedFilePath);
            FileOutputStream fos = new FileOutputStream(unitedFile);
            RandomAccessFile ra = null;
            for (int i = 0; i < partsPaths.size(); i++) {
                ra = new RandomAccessFile(partsPaths.get(i), "r");
                if (i != 0) {
                    ra.seek(6);
                }
                byte[] buffer = new byte[1024 * 8];
                int len = 0;
                while ((len = ra.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            ra.close();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(this, R.string.merger_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 连接各个录音片段
     */
    public void linkVoice() {
        if (voices.size() == 0)
            return;

        String path;
        int end = voices.get(0).lastIndexOf("/");
        path = voices.get(0).substring(0, end);
        myVoice = path + "/" + coreManager.getSelf().getUserId() + "_voice.amr";
        uniteAMRFile(voices, myVoice);

        //合并成功后就删除每个语音片段文件
        for (int i = 0; i < voices.size(); i++) {
            File file = new File(voices.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
        // 把本地的路径存起来
        //        File voiceFile = new File(myVoice);
        //        uploadVoiceFile(voiceFile);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancel();
    }

    public void initEvent() {
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == PAUSE) // 暂停状态就进入试听状态
                {
                    status = PREVIEW;
                    showPager();
                    linkVoice();
                    playVoice();
                } else if (status == PREVIEW) // 预览状态就上传音频
                {
                    if (myVoice != null) {
                        File file = new File(myVoice);
                        if (file != null) {
                            EventBus.getDefault().post(new MessageEventVoice(myVoice, stopcurent - statrcurent));
                            finish();
                        }
                    }
                }
            }
        });
        leftTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = STOP;
                time = 0;
                remainingTime = 60;
                showPager();
                cancel();
            }
        });
        startTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == STOP) // 当前为停止状态，点击后进入正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == RECORD) // 当前为正在录制界面，点击后进入暂停录制界面
                {
                    status = PAUSE;
                    showPager();
                } else if (status == PAUSE)//当前为暂停录制界面，点击后恢复正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == PREVIEW) // 试听状态就直接播放音频
                {
                    if (voicePlayStatus == STOP) {
                        playVoice();
                        startTv.setText(getString(R.string.stop));
                        startIv.setImageResource(R.mipmap.triangle1_normal);
                    } else if (voicePlayStatus == PLAY) {
                        voicePlayer.stop();
                        voicePlayStatus = STOP;
                        startTv.setText(getString(R.string.play));
                        startIv.setImageResource(R.mipmap.tounded1_normal);
                    }
                }
            }
        });
        startIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == STOP) // 当前为停止状态，点击后进入正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == RECORD) // 当前为正在录制界面，点击后进入暂停录制界面
                {
                    status = PAUSE;
                    showPager();
                } else if (status == PAUSE)//当前为暂停录制界面，点击后恢复正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == PREVIEW) // 试听状态就直接播放音频
                {
                    if (voicePlayStatus == STOP) {
                        playVoice();
                        startTv.setText(getString(R.string.stop));
                        startIv.setImageResource(R.mipmap.triangle1_normal);
                    } else if (voicePlayStatus == PLAY) {
                        voicePlayer.stop();
                        voicePlayStatus = STOP;
                        startTv.setText(getString(R.string.play));
                        startIv.setImageResource(R.mipmap.tounded1_normal);
                    }
                }
            }
        });
        startTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == STOP) // 当前为停止状态，点击后进入正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == RECORD) // 当前为正在录制界面，点击后进入暂停录制界面
                {
                    status = PAUSE;
                    showPager();
                } else if (status == PAUSE)//当前为暂停录制界面，点击后恢复正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == PREVIEW) // 试听状态就直接播放音频
                {
                    if (voicePlayStatus == STOP) {
                        playVoice();
                        startTv.setText(getString(R.string.stop));
                        startIv.setImageResource(R.mipmap.triangle1_normal);
                    } else if (voicePlayStatus == PLAY) {
                        voicePlayer.stop();
                        voicePlayStatus = STOP;
                        startTv.setText(getString(R.string.play));
                        startIv.setImageResource(R.mipmap.tounded1_normal);
                    }
                }
            }
        });
        controlIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == STOP) // 当前为停止状态，点击后进入正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == RECORD) // 当前为正在录制界面，点击后进入暂停录制界面
                {
                    status = PAUSE;
                    showPager();
                } else if (status == PAUSE)//当前为暂停录制界面，点击后恢复正在录制界面
                {
                    status = RECORD;
                    start();
                    showPager();
                } else if (status == PREVIEW) // 试听状态就直接播放音频
                {
                    if (voicePlayStatus == STOP) {
                        playVoice();
                        startTv.setText(getString(R.string.stop));
                        startIv.setImageResource(R.mipmap.triangle1_normal);
                    } else if (voicePlayStatus == PLAY) {
                        voicePlayer.stop();
                        voicePlayStatus = STOP;
                        startTv.setText(getString(R.string.play));
                        startIv.setImageResource(R.mipmap.tounded1_normal);
                    }
                }
            }
        });
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == RECORD) {
                    status = STOP;
                    showPager();
                    cancel();
                } else if (status == STOP) {
                    finish();
                }
            }
        });
    }

    // 播放录音
    private void playVoice() {
        voicePlayer = null;
        voicePlayer = new VoicePlayer();
        voicePlayer.play(myVoice);
        voicePlayStatus = PLAY;
        voicePlayer.setOnFinishPlayListener(new VoicePlayer.OnFinishPlayListener() {
            @Override
            public void onFinishPlay() {
                voicePlayStatus = STOP;
                startTv.setText(getString(R.string.play));
                startIv.setImageResource(R.mipmap.tounded1_normal);
            }
        });
    }

    private class RecordTime extends Thread {
        public void run() {
            minTime = 0;
            while (time < 60 && time >= 0 && status == RECORD) {
                time++;
                minTime++;
                remainingTime--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (remainingTime <= 0) {
                    finish();
                }
                Message msg = new Message();
                msg.what = RECORD;
                handler.sendMessage(msg);
            }

            if (minTime < 2) {
                Message msg = new Message();
                msg.what = PAUSE;
                handler.sendMessage(msg);
            } else {
                if (status != PAUSE) {
                    recordManager.stop();
                }
                Message msg = new Message();
                msg.what = STOP;

                handler.sendMessage(msg);
            }
        }
    }
}





