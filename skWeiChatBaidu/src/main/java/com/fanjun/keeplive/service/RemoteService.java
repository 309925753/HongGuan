package com.fanjun.keeplive.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.content.ContextCompat;

import com.fanjun.keeplive.utils.ServiceUtils;

/**
 * 守护进程
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public final class RemoteService extends Service {
    private boolean mIsBoundLocalService;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (ServiceUtils.isRunningTaskExist(getApplicationContext(), getPackageName() + ":remote")) {
                Intent localService = new Intent(RemoteService.this,
                        LocalService.class);
                ContextCompat.startForegroundService(RemoteService.this, localService);
                mIsBoundLocalService = RemoteService.this.bindService(new Intent(RemoteService.this,
                        LocalService.class), connection, Context.BIND_ABOVE_CLIENT);
            }
            PowerManager pm = (PowerManager) RemoteService.this.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (isScreenOn) {
                sendBroadcast(new Intent("_ACTION_SCREEN_ON"));
            } else {
                sendBroadcast(new Intent("_ACTION_SCREEN_OFF"));
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mIsBoundLocalService = this.bindService(new Intent(RemoteService.this, LocalService.class),
                    connection, Context.BIND_ABOVE_CLIENT);
        } catch (Exception e) {
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            try {
                if (mIsBoundLocalService) {
                    unbindService(connection);
                }
            } catch (Exception e) {
            }
        }
    }

}
