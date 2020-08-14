package com.sk.weichat.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class AudioPalyer {
    private static final int IDLE = 0;// 空闲状态
    private static final int INITIALIZED = 1;// 初始化状态，即设置了数据源
    private static final int PREPARING = 2;  // 正在准备
    private static final int PREPARED = 3;   // 准备完毕
    private static final int STARED = 4;// 开始播放
    private static final int PAUSED = 5;// 暂停
    private static final int STOPED = 6;// 停止
    private int mState = IDLE;
    private MediaPlayer mMediaPlayer;
    private PlayerActionHandler mHandler;
    private AudioPlayListener mAudioPlayListener;

    private String mPlayingUrl;
    private boolean isLooping;
    private boolean mPauseAccident = false;// 意外事件导致音频停止播放，比如Home到后台，来电等等

    public AudioPalyer() {
        mMediaPlayer = new MediaPlayer();
        mHandler = new PlayerActionHandler();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 设置回调
        mMediaPlayer.setOnPreparedListener(mInnerOnPreparedListener);
        mMediaPlayer.setOnBufferingUpdateListener(mInnerOnBufferingUpdateListener);
        mMediaPlayer.setOnCompletionListener(mInnerOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mInnerOnErrorListener);
        mMediaPlayer.setOnSeekCompleteListener(mInnerOnSeekCompleteListener);
    }

    public void setAudioPlayListener(AudioPlayListener listener) {
        mAudioPlayListener = listener;
    }

    public void play(String url) {
        play(url, false);
    }

    public void play(String url, boolean looping) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (isLooping != looping) {
            mMediaPlayer.setLooping(looping);
            isLooping = looping;
        }
        try {
            if (mState == IDLE || mPlayingUrl == null || !mPlayingUrl.equals(url)) {
                mMediaPlayer.reset();
                mState = IDLE;
                mPlayingUrl = url;
                mMediaPlayer.setDataSource(url);
                mState = INITIALIZED;
                mMediaPlayer.prepareAsync();
                if (mAudioPlayListener != null) {
                    mAudioPlayListener.onPreparing();
                }
                mState = PREPARING;
            } else {
                reuse();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reuse() {
        Log.d("roamer", "AudioPlayer reuse：" + mState);
        if (mState == INITIALIZED || mState == STOPED) {
            mMediaPlayer.prepareAsync();
            if (mAudioPlayListener != null) {
                mAudioPlayListener.onPreparing();
            }
            mState = PREPARING;
        } else if (mState == PREPARING || mState == STARED) {
            return;
        } else if (mState == PREPARED || mState == PAUSED) {
            mMediaPlayer.start();
            mState = STARED;
        }
    }

    /**
     * 时候可以直接播放或者正在播放
     *
     * @return
     */
    private boolean canPlay() {
        if (isPlaying() || mState == PREPARED || mState == PAUSED) {
            return true;
        }
        return false;
    }

    public boolean needPrepare(String url) {
        if (mState == IDLE || mPlayingUrl == null || !mPlayingUrl.equals(url)) {
            return true;
        } else {
            if (mState == INITIALIZED || mState == STOPED) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isPlaying() {
        return mState == STARED && mMediaPlayer.isPlaying();
    }

    public void stop() {
        if (canPlay()) {
            mMediaPlayer.stop();
            mState = STOPED;
        }
    }

    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            mState = PAUSED;
        }
    }

    public int getCurrentPosition() {
        if (canPlay()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (canPlay()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (canPlay()) {
            mMediaPlayer.seekTo(position);
        }
    }

    /**
     * 在onPuase方法中调用
     *
     * @return true表示确实意外暂停了，UI需要更新界面
     */
    public boolean accidentPause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            mState = PAUSED;
            mPauseAccident = true;
            return true;
        }
        return false;
    }

    /**
     * 在onResume方法中调用
     *
     * @return true表示确实回复了上次的意外暂停，UI需要更新界面
     */
    public boolean accidentResume() {
        if (mState == PAUSED && mPauseAccident) {
            mMediaPlayer.start();
            mState = STARED;
            return true;
        }
        return false;
    }

    public void release() {
        mMediaPlayer.release();
        mState = IDLE;
    }

    @SuppressWarnings("unused")
    private boolean mTimeChangeEnable = false;

    public void enableTimeChange(boolean enable) {
        mTimeChangeEnable = enable;
    }

    private static final int MESSAGE_ON_PREPARED = 0x1;
    private static final int MESSAGE_ON_BUFFERING_UPDATE = 0x2;
    private static final int MESSAGE_ON_ERROE = 0x3;
    private static final int MESSAGE_ON_COMPLETION = 0x4;
    private static final int MESSAGE_ON_SEEK_COOMPLETE = 0x5;

    private class PlayerActionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mAudioPlayListener == null) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_ON_PREPARED:
                    mAudioPlayListener.onPrepared();
                    break;
                case MESSAGE_ON_BUFFERING_UPDATE:
                    mAudioPlayListener.onBufferingUpdate(msg.arg1);
                    break;
                case MESSAGE_ON_ERROE:
                    mAudioPlayListener.onError();
                    break;
                case MESSAGE_ON_COMPLETION:
                    mAudioPlayListener.onCompletion();
                    break;
                case MESSAGE_ON_SEEK_COOMPLETE:
                    mAudioPlayListener.onSeekComplete();
                    break;
                default:
                    break;
            }
        }
    }

    public static interface AudioPlayListener {
        void onPreparing();

        void onPrepared();

        void onBufferingUpdate(int percent);

        void onError();

        void onCompletion();

        void onSeekComplete();
    }

    private OnPreparedListener mInnerOnPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mState = PREPARED;
            mMediaPlayer.start();
            mState = STARED;
            if (mAudioPlayListener != null) {
                mHandler.obtainMessage(MESSAGE_ON_PREPARED).sendToTarget();
            }

        }
    };

    private OnBufferingUpdateListener mInnerOnBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mAudioPlayListener != null) {
                Message message = mHandler.obtainMessage(MESSAGE_ON_BUFFERING_UPDATE);
                message.arg1 = percent;
                message.sendToTarget();
            }
        }
    };

    private OnErrorListener mInnerOnErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mState = IDLE;
            if (mAudioPlayListener != null) {
                mHandler.obtainMessage(MESSAGE_ON_ERROE).sendToTarget();
            }
            return false;
        }
    };

    private OnCompletionListener mInnerOnCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d("roamer", "AudioPlayer onCompletion");
            mState = PAUSED;// 实际上应该是PlaybackCompleted 但是这个状态和Paused差不多 当做同样的处理
            if (mAudioPlayListener != null) {
                mHandler.obtainMessage(MESSAGE_ON_COMPLETION).sendToTarget();
            }
        }
    };

    private OnSeekCompleteListener mInnerOnSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (mAudioPlayListener != null) {
                mHandler.obtainMessage(MESSAGE_ON_SEEK_COOMPLETE).sendToTarget();
            }
        }
    };

}
