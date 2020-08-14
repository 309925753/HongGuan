package com.sk.weichat.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sk.weichat.ui.base.ActivityStack;
import com.sk.weichat.ui.base.BaseActivity;

public class ShakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), OtherBroadcast.ACTION_SHAKE_MSG_NOTIFY)) {
            Activity activity = ActivityStack.getInstance().getActivity(ActivityStack.getInstance().size() - 1);
            if (activity instanceof BaseActivity) {
                ((BaseActivity) activity).shake(intent.getIntExtra(OtherBroadcast.ACTION_SHAKE_MSG_NOTIFY, 1));
            }
        }
    }
}
