package com.sk.im.audio;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.sk.weichat.audio.RecordStateListener;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class RecordManager {

    private static final int MSG_VOICE_CHANGE = 1;
    private static RecordManager instance;

    private RecordStateListener listener;
    private MediaRecorder mr;
    private String name;
    // private Thread voiceVolumeListener;
    // private static ExecutorService pool;
    private Handler handler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_VOICE_CHANGE) {
                if (listener != null) {
                    listener.onRecordVolumeChange((Integer) msg.obj);
                }
            }
            return false;
        }
    });
    private long startTime = System.currentTimeMillis();
    private Timer timer = new Timer();
    private boolean running = false;

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        if (instance == null) {
            instance = new RecordManager();
        }
        return instance;
    }

    private void notifyStartLoading() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordStarting();
                }
            });
        }
    }

    private void notifyTooShoot() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordTooShoot();
                }
            });
        }
    }

    private void notifyStart() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordStart();
                }
            });
        }
    }

    private void notifyFinish(final String file) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordFinish(file);
                }
            });
        }
    }

    private void notifyCancal() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordCancel();
                }
            });
        }
    }

    private void notifyVoiceChange(int v) {
        Message message = new Message();
        message.what = MSG_VOICE_CHANGE;
        message.obj = v;
        handler.sendMessage(message);
    }

    public boolean isRunning() {
        return running;
    }

    @SuppressWarnings("deprecation")
    public void startRecord() {
        // Thread recordThread = new Thread(new Runnable() {

        // @Override
        // public void run() {
        try {
            notifyStartLoading();
            mr = new MediaRecorder();
            mr.setAudioSource(AudioSource.MIC);
            // 设置音源,这里是来自麦克风,虽然有VOICE_CALL,但经真机测试,不行
            mr.setOutputFormat(OutputFormat.RAW_AMR);
            // 输出格式
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            name = FileUtil.getRandomAudioAmrFilePath();

            if (TextUtils.isEmpty(name)) {
                notifyError();
                return;
            }
            // 编码
            mr.setOutputFile(name);
            mr.prepare();
            notifyStart();
            // 做些准备工作
            mr.start();
            startTime = System.currentTimeMillis();
            running = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int i = mr.getMaxAmplitude();
                    if (listener != null) {
                        int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
                        notifyVoiceSecondsChange(seconds);
                        notifyVoiceChange(i);
                    }
                }
            }, 0, 100);
        } catch (Exception e) {
            e.printStackTrace();
            notifyError();
        }
    }

    private void notifyError() {
        handler.post(new Runnable() {
            public void run() {
                listener.onRecordError();
            }
        });
    }

    private void notifyVoiceSecondsChange(final int seconds) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onRecordTimeChange(seconds);
            }
        });
    }

    private void stopVolumeListener() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void waitRunning() {
        /*
         * while (!running) { try { Thread.sleep(10); } catch
         * (InterruptedException e) { e.printStackTrace(); } }
         */
    }

    public synchronized void stop() {
        stopVolumeListener();
        if (mr != null) {
            try {
                mr.stop();
                mr.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long delay = System.currentTimeMillis() - startTime;

            if (delay <= 500) {
                notifyTooShoot();
            } else {
                notifyFinish(name);
            }
        } else {
            notifyCancal();
        }
        running = false;
    }

    public synchronized void cancel() {
        stopVolumeListener();
        if (mr != null) {
            try {
                mr.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            File file = new File(name);
            file.deleteOnExit();
            notifyCancal();
        }
        running = false;
    }

    public void setVoiceVolumeListener(RecordStateListener listener) {
        this.listener = listener;
    }

}
