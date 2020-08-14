package com.sk.weichat.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sk.weichat.AppConfig;

/**
 * 我的朋友
 */
public class CardcastUiUpdateUtil {

    public static final String ACTION_UPDATE_UI = AppConfig.sPackageName + ".action.cardcast.update_ui";

    public static IntentFilter getUpdateActionFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_UI);
        return intentFilter;
    }

    public static void broadcastUpdateUi(Context context) {
        if (context != null) {
            context.sendBroadcast(new Intent(ACTION_UPDATE_UI));
        }
    }
}
