package com.sk.weichat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.ActivityStack;

public class RestartService extends Service {
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Intent intent = new Intent(RestartService.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                ActivityStack.getInstance().exit();
                MyApplication.getInstance().destoryRestart();
                stopSelf();
            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = new Message();
        message.what = 1;

        handler.sendMessageDelayed(message, 100);
        return super.onStartCommand(intent, flags, startId);
    }

}
