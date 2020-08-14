package com.sk.weichat.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;

public class AudioModeManger {
    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Sensor mProximiny;
    private onSpeakerListener mOnSpeakerListener;

    /**
     * 在识别扬声器与听筒时，mDistanceSensorListener会连续回调多次，我们取第一次作为有效回调
     */
    private long mLastSpeakerSwitchingTime;// 上一次扬声器切换时间
    private long mLastEarpieceSwitchingTime;// 上一次听筒时间
    /**
     * 距离传感器监听者
     */
    private SensorEventListener mDistanceSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float f_proximiny = event.values[0];
            //扬声器模式
            //魅蓝E传感器得到的值竟然比最大值都要大？what fuck ？
            if (f_proximiny >= mProximiny.getMaximumRange()) {
                if (mOnSpeakerListener != null) {
                    if (System.currentTimeMillis() - mLastSpeakerSwitchingTime >= 1000) {
                        mLastSpeakerSwitchingTime = System.currentTimeMillis();
                        mOnSpeakerListener.onSpeakerChanged(true);
                    }
                }
            } else {//听筒模式
                if (mOnSpeakerListener != null) {
                    if (System.currentTimeMillis() - mLastEarpieceSwitchingTime >= 1000) {
                        mLastEarpieceSwitchingTime = System.currentTimeMillis();
                        mOnSpeakerListener.onSpeakerChanged(false);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public AudioModeManger() {

    }

    public void setOnSpeakerListener(onSpeakerListener listener) {
        if (listener != null) {
            mOnSpeakerListener = listener;
        }
    }

    /**
     * 听筒、扬声器切换
     * <p>
     * 注释： 敬那些年踩过的坑和那些网上各种千奇百怪坑比方案！！
     * <p>
     * AudioManager设置声音类型有以下几种类型（调节音量用的是这个）:
     * <p>
     * STREAM_ALARM 警报
     * STREAM_MUSIC 音乐回放即媒体音量
     * STREAM_NOTIFICATION 窗口顶部状态栏Notification,
     * STREAM_RING 铃声
     * STREAM_SYSTEM 系统
     * STREAM_VOICE_CALL 通话
     * STREAM_DTMF 双音多频,不是很明白什么东西
     * <p>
     * ------------------------------------------
     * <p>
     * AudioManager设置声音模式有以下几个模式（切换听筒和扬声器时setMode用的是这个）
     * <p>
     * MODE_NORMAL 正常模式，即在没有铃音与电话的情况
     * MODE_RINGTONE 铃响模式
     * MODE_IN_CALL 通话模式 5.0以下
     * MODE_IN_COMMUNICATION 通话模式 5.0及其以上
     *
     * @param speakerPhoneOn
     */
    public void setSpeakerPhoneOn(boolean speakerPhoneOn) {
        audioManager.setSpeakerphoneOn(speakerPhoneOn);
        if (speakerPhoneOn) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), AudioManager.FX_KEY_CLICK);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0及其以上
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
        }
    }

    /**
     * 注册距离传感器监听
     */
    public void register(Context context) {
        audioManager = (AudioManager) context.getSystemService(android.app.Service.AUDIO_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(android.app.Service.SENSOR_SERVICE);
        if (sensorManager != null && mDistanceSensorListener != null) {
            mProximiny = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorManager.registerListener(mDistanceSensorListener, mProximiny,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 取消注册距离传感器监听
     */
    public void unregister() {
        if (sensorManager != null && mDistanceSensorListener != null) {
            sensorManager.unregisterListener(mDistanceSensorListener);
        }
    }

    /**
     * 扬声器状态监听器
     * 如果要做成类似微信那种切换后重新播放音频的效果，需要这个监听回调
     * isSpeakerOn 扬声器是否打开
     */
    public interface onSpeakerListener {
        void onSpeakerChanged(boolean isSpeakerOn);
    }

}
