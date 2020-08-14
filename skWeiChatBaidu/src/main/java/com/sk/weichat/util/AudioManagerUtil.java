package com.sk.weichat.util;

/**
 * Created by Administrator on 2017/8/2 0002.
 */

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

/**
 * 系统声音模式设置工具类
 *
 * @author linzhiyong
 * @time 2017年1月9日15:30:47
 * @email wflinzhiyong@163.com
 * @desc
 */
public class AudioManagerUtil {

    private Context context;
    private AudioManager audioManager;

    public AudioManagerUtil(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 设置是否使用扬声器
     *
     * @param on
     */
    public void setSpeakerphoneOn(boolean on) {
        if (on) {
            if (this.audioManager.isSpeakerphoneOn()) {
                return;
            }
            this.audioManager.setSpeakerphoneOn(true);
        } else {
            this.audioManager.setSpeakerphoneOn(false);//关闭扬声器
            this.audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
            ((Activity) context).setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            //把声音设定成Earpiece（听筒）出来，设定为正在通话中
            this.audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    /**
     * 是否是扬声器模式
     *
     * @return
     */
    public boolean isSpeakerphoneOn() {
        return this.audioManager.isSpeakerphoneOn();
    }

    /**
     * 设置静音 true: 静音  false: 正常
     *
     * @param on
     */
    public void setSilentOn(boolean on) {
        if (on) {
            if (this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            this.audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else {
            this.audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            //            this.audioManager.setStreamVolume(AudioManager.RINGER_MODE_NORMAL, ringerVolume, 0);
        }
    }

    /**
     * 是否是静音模式
     *
     * @return
     */
    public boolean isSlientOn() {
        return this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
                || this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }
}