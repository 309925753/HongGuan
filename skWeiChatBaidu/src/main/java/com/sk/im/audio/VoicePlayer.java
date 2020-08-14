package com.sk.im.audio;

import android.media.MediaPlayer;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/*
 *
 * 2017.02.10 wzw
 * 添加暂停方法pause()
 *
 * 只停止播放，不释放播放器资源方法
 * onlyStop();
 *
 * */
public class VoicePlayer {

    private MediaPlayer player;

    public interface OnMediaStateChange {
        public void onFinishPlay(MediaPlayer player);

        public void onErrorPlay();

        public void onSecondsChange(int seconds);

    }

    private OnMediaStateChange listener = new OnMediaStateChange() {
        public void onFinishPlay(MediaPlayer player) {
            if (mOnFinishPlayListener != null) {
                mOnFinishPlayListener.onFinishPlay();
            }

        }

        public void onErrorPlay() {

        }

        public void onSecondsChange(int seconds) {

        }
    };

    //影音播放结束的监听
    /*start*/
    private OnFinishPlayListener mOnFinishPlayListener;

    public void setOnFinishPlayListener(OnFinishPlayListener listener) {
        if (listener != null) {
            mOnFinishPlayListener = listener;
        }
    }

    public interface OnFinishPlayListener {
        void onFinishPlay();
    }
    /*end*/

    public void setOnMediaStateChangeListener(OnMediaStateChange listener) {
        this.listener = listener;
    }

    private Timer timer = new Timer();

    public void stop() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            if (player != null) {
                player.reset();
                player.release();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.onFinishPlay(player);
            player = null;
        }
    }

    public void keepStop() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            if (player != null) {
                player.reset();
                player.release();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            player = null;
        }
    }

    public void play(String path) {
        try {
            player = new MediaPlayer();
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    try {
                        timer.cancel();
                        listener.onFinishPlay(mp);
                        player.reset();
                        player.release();
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                    return false;
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {

                    try {
                        timer.cancel();
                        listener.onFinishPlay(mp);
                        player.reset();
                        player.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            player.setLooping(false);
            player.setDataSource(path);
            player.prepare();
            player.start();

            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                listener.onSecondsChange(player
                                        .getCurrentPosition());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }, 0, 500);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onErrorPlay();
        }

    }

    /**
     * 暂停
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }

    }

    /**
     * 只停止播放，不释放资源
     */
    public void onlyStop() {
        if (player != null) {
            player.stop();
        }
    }

    private Handler handler = new Handler();

}
