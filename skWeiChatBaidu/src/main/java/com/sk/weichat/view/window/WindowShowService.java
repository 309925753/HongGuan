package com.sk.weichat.view.window;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class WindowShowService extends Service implements WindowUtil.OnPermissionListener {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WindowUtil.getInstance().showPermissionWindow(this, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowUtil.getInstance().dismissWindow();
    }

    @Override
    public void showPermissionDialog() {

    }
}
