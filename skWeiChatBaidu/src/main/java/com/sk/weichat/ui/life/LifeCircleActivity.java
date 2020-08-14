package com.sk.weichat.ui.life;

import android.os.Bundle;

import com.sk.weichat.R;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.ui.base.BaseActivity;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;

public class LifeCircleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_circle);
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        // 点返回键退出全屏视频，
        // 如果DiscoverFragment用在其他activity, 也要加上，
        if (JVCideoPlayerStandardSecond.backPress()) {
            JCMediaManager.instance().recoverMediaPlayer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        VoicePlayer.instance().stop();
        super.finish();
    }
}
