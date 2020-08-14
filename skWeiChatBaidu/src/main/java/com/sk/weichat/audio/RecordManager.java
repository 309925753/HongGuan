package com.sk.weichat.audio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.audio.record.AmrEncoder;
import com.sk.weichat.audio.record.AudioFileUtils;
import com.sk.weichat.audio.record.AudioRecorder;

public class RecordManager {
    private static final String TAG = "RecordManager";

    private static final int MSG_VOICE_CHANGE = 1;
    private static RecordManager instance;

    private RecordStateListener listener;
    private AudioRecorder mr;
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
    private boolean running = false;
    private boolean mAudioFocus;
    private AudioManager mAudioManager;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                default:
                    Log.i(TAG, "AudioFocusChange focus = " + focusChange);
                    break;
            }
        }
    };

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
            requestAudioFocus();
            notifyStartLoading();
            AudioFileUtils.setFolder(MyApplication.getContext().getFilesDir().getAbsolutePath());
            mr = new AudioRecorder(new AudioRecorder.CallBack() {
                @Override
                public void recordProgress(int progress) {
                    if (listener != null) {
                        int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
                        notifyVoiceSecondsChange(seconds);
                    }
                }

                @Override
                public void volumn(int volumn) {
                    if (listener != null) {
                        notifyVoiceChange(volumn);
                    }
                }
            }, new AmrEncoder());
            notifyStart();
            // 做些准备工作
            mr.startRecord();
            startTime = System.currentTimeMillis();
            running = true;
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

    public synchronized String stop() {
        if (mr != null) {
            try {
                mr.stop();
                name = mr.getVoiceFilePath();
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
        abandonAudioFocus();
        return name;
    }

    public synchronized void cancel() {
        if (mr != null) {
            try {
                mr.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyCancal();
        }
        running = false;
        abandonAudioFocus();
    }

    public void setVoiceVolumeListener(RecordStateListener listener) {
        this.listener = listener;
    }

    private void requestAudioFocus() {
        Log.v(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);
        if (!mAudioFocus) {
            int result = getAudioManager().requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_MUSIC, // Use the music stream.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = true;
            } else {
                Log.e(TAG, "AudioManager request Audio Focus result = " + result);
            }
        }
    }

    private void abandonAudioFocus() {
        Log.v(TAG, "abandonAudioFocus mAudioFocus = " + mAudioFocus);
        if (mAudioFocus) {
            getAudioManager().abandonAudioFocus(afChangeListener);
            mAudioFocus = false;
        }
    }

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            synchronized (this) {
                if (mAudioManager == null) {
                    mAudioManager = (AudioManager) MyApplication.getContext()
                            .getSystemService(Context.AUDIO_SERVICE);
                }
            }
        }
        return mAudioManager;
    }

}
