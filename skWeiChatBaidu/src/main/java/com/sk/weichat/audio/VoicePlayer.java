package com.sk.weichat.audio;

import android.media.MediaPlayer;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class VoicePlayer {
    private MediaPlayer player;
    private Timer timer = new Timer();
    private Handler handler = new Handler();

    public interface OnMediaStateChange {
        void onFinishPlay(MediaPlayer player);

        void onErrorPlay();

        void onSecondsChange(int seconds);
    }

    private OnMediaStateChange listener = new OnMediaStateChange() {
        public void onFinishPlay(MediaPlayer player) {

        }

        public void onErrorPlay() {

        }

        public void onSecondsChange(int seconds) {

        }
    };

    public void setOnMediaStateChangeListener(OnMediaStateChange listener) {
        this.listener = listener;
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
                                listener.onSecondsChange(player.getCurrentPosition());
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
}
