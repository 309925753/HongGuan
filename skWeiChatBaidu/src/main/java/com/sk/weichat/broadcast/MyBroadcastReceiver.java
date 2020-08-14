package com.sk.weichat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sk.weichat.RestartService;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(OtherBroadcast.BROADCASTTEST_ACTION, intent.getAction())) {
            context.startService(new Intent(context, RestartService.class));
        }
    }
}
