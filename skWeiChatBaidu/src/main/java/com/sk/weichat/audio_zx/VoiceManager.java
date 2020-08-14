package com.sk.weichat.audio_zx;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xuan on 2017/9/13.
 */

public class VoiceManager {

    /*############### 状态 ###############*/
    public static final int STATE_UNLL = 0;                                 // 无状态
    public static final int STATE_READY = 1;                                // 准备好了
    public static final int STATE_PLAY = 2;                                 // 播放中
    public static final int STATE_PAUSE = 3;                                // 暂停中
    public static final int STATE_FINISH = 4;                               // 播放完成
    public static final int STATE_ERROR = -1;                               // 错误
    public static int mCurrtState;
    private volatile static VoiceManager instance;
    public String mCurrtPath;
    private MediaPlayer mMediaPlayer;
    private VoicePlayListenerSet mListener;
    private int duration;

    /*############### 单例 ###############*/
    private VoiceManager() {
        mListener = new VoicePlayListenerSet();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mCurrtState = STATE_FINISH;
                mMediaPlayer.reset();
                if (mListener != null) {
                    mListener.onFinishPlay(mCurrtPath);
                }
            }
        });

        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {

            mCurrtState = STATE_ERROR;
            if (mListener != null) {
                mListener.onErrorPlay();
            }
            return false;
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mCurrtState = STATE_ERROR;
                if (mListener != null) {
                    mListener.onErrorPlay();
                }
                return false;
            }
        });
        mMediaPlayer.setLooping(false);
    }

    public static VoiceManager instance() {
        if (instance == null) {
            synchronized (VoiceManager.class) {
                if (instance == null) {
                    instance = new VoiceManager();
                }
            }
        }
        return instance;
    }

    /**
     * 跳转到某一个进度播放
     *
     * @param msec
     */
    public void seek(double msec) {
        if (mCurrtState == STATE_PLAY) {
            double d = Double.valueOf(getDuration());
            int i = (int) Math.round(d * msec);
            mMediaPlayer.seekTo(i);
            Log.e("zxzx", "seek: " + i + " d: " + d);
        }
    }

    public void seekInt(int msec) {
        if (mCurrtState == STATE_PLAY) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void play(String path) {
        File file = new File(path);
        if (file.exists()) { // 文件不存在
            play(file);
        } else {
            Log.e("zxzx", "播放音乐文件不存在  path :" + path);
        }
    }

    public void play(File file) {
        if (mCurrtState == STATE_PLAY) { //  播放中，我们先停止
            mMediaPlayer.stop();

            if (mListener != null) {
                mListener.onStopPlay(mCurrtPath);
            }

        } else if (mCurrtState == STATE_PAUSE) { // 暂停中,继续播放
            mMediaPlayer.start();
            mCurrtState = STATE_PLAY;
            return;
        }

        mCurrtState = STATE_READY;
        try {

            mCurrtPath = file.getAbsolutePath();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mCurrtPath);
            mMediaPlayer.prepare();

            mMediaPlayer.start();
            mCurrtState = STATE_PLAY;

        } catch (IOException e) {
            mCurrtState = STATE_ERROR;
            if (mListener != null) {
                mListener.onErrorPlay();
            }
            e.printStackTrace();
        }
        setDuration(mMediaPlayer.getDuration());
    }

    public void pause() {
        mMediaPlayer.pause();
        duration = mMediaPlayer.getDuration();
        mCurrtState = STATE_PAUSE;
    }

    public void stop() {
        mMediaPlayer.stop();
        mCurrtState = STATE_FINISH;
        if (mListener != null) {
            mListener.onStopPlay(mCurrtPath);
        }
    }

    public int getState() {
        return mCurrtState;
    }

    /**
     * 得到mediaplayer的播放进度
     *
     * @return
     */
    public int getProgress() {
        int pro = 0;
        if (mCurrtState == STATE_PLAY) { // 播放中才有播放进度
            pro = (int) Math.round(mMediaPlayer.getCurrentPosition() / 1000.0); // 四舍五入
        }
        return pro;
    }

    public int getCurrentPosition() {
        int pro = 0;
        pro = (int) Math.round(mMediaPlayer.getCurrentPosition()); // 四舍五入
        return pro;
    }

    /**
     * 得到mediaplayer的大小
     *
     * @return
     */
    public int getMeSize() {
        int pro = 0;
        if (mCurrtState == STATE_PLAY) { // 播放中才有播放进度
        }
        return pro;
    }

    public void addVoicePlayListener(VoicePlayListener listener) {
        mListener.addListener(listener);
    }

    public void removeVoicePlayListener(VoicePlayListener listener) {
        mListener.removeListener(listener);
    }

    public interface VoicePlayListener {
        void onFinishPlay(String path);

        void onStopPlay(String path);

        void onErrorPlay();
    }

    private static class VoicePlayListenerSet implements VoicePlayListener {
        private Set<VoicePlayListener> listeners = new HashSet<>();

        @Override
        public void onFinishPlay(String path) {
            for (VoicePlayListener listener : listeners) {
                listener.onFinishPlay(path);
            }
        }

        @Override
        public void onStopPlay(String path) {
            for (VoicePlayListener listener : listeners) {
                listener.onStopPlay(path);
            }
        }

        @Override
        public void onErrorPlay() {
            for (VoicePlayListener listener : listeners) {
                listener.onErrorPlay();
            }
        }

        public void addListener(VoicePlayListener listener) {
            if (listener != null) {
                listeners.add(listener);
            }
        }

        public void removeListener(VoicePlayListener listener) {
            listeners.remove(listener);
        }
    }
}
