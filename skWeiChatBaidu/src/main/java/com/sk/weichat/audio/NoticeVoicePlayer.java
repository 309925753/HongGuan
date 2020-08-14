package com.sk.weichat.audio;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.PrivacySetting;
import com.sk.weichat.helper.PrivacySettingHelper;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by wzw on 2017/3/22.
 * <p>
 * 提示音播放 + 手机振动
 */

public class NoticeVoicePlayer {
    private static NoticeVoicePlayer instance;
    // 当手机开启静音，部分手机的多媒体不会被管控，改为Ringtone播放
    /*private MediaPlayer mediaPlayer;*/
    private Ringtone ringtone;
    private Vibrator vibrator;// 振动

    public NoticeVoicePlayer() {
        /*mediaPlayer = MediaPlayer.create(MyApplication.getContext(), R.raw.msg);*/
        ringtone = initRingtone(MyApplication.getContext());
        vibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
    }

    public static NoticeVoicePlayer getInstance() {
        if (instance == null) {
            instance = new NoticeVoicePlayer();
        }
        return instance;
    }

    /**
     * init type of Ringtone
     *
     * @param context Activity
     * @return Ringtone
     */
    private Ringtone initRingtone(Context context) {
        // 系统默认通知提示音
        /*Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);*/
        Uri ringTone = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.msg);
        return RingtoneManager.getRingtone(context, ringTone);
    }

    public void start() {
        /*mediaPlayer.start();*/
        ringtone.play();
        // 停止 开启 停止 开启
        long[] pattern = {100, 400, 100, 400};
        // 重复两次上面的pattern 如果只想震动一次，index设为-1
        // vibrator.vibrate(pattern, 2);
        // 判断该用户是否开启振动
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(MyApplication.getContext());
        boolean input = privacySetting.getIsVibration() == 1;
        if (input) {
            vibrator.vibrate(pattern, -1);
        }
    }

    public void stop() {
        /*mediaPlayer.stop();*/
        ringtone.stop();
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(MyApplication.getContext());
        boolean input = privacySetting.getIsVibration() == 1;
        if (input) {
            vibrator.cancel();
        }
    }
}
