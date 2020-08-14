package com.sk.weichat.xmpp.helloDemon;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.UserStatus;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.LogUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import okhttp3.Call;

public class FirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "FcmPush";
    // 默认禁用谷歌推送，毕竟要求服务器在墙外，
    // 禁用时fcm自动初始化后不上传
    private static boolean enabled = false;

    private static void sendRegistrationToServer(Context ctx) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "sendRegistrationToServer: " + refreshedToken);
        if (TextUtils.isEmpty(refreshedToken)) {
            Log.i(TAG, "还没拿到token，不上传token");
            return;
        }

        UserStatus status = CoreManager.getSelfStatus(ctx);
        if (status == null || TextUtils.isEmpty(status.accessToken)) {
            // 登录后会在MainActivity调用上传，
            Log.i(TAG, "还没登录，不上传token");
            return;
        }

        HttpUtils.post()
                .url(CoreManager.requireConfig(ctx).configFcm)
                .params("token", refreshedToken)
                .params("access_token", status.accessToken)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        Log.i("push", "上传谷歌推送fcm token成功");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Reporter.post("上传谷歌推送fcm token失败，", e);
                    }
                });
    }

    public static void init(Context ctx) {
        enabled = true;
        sendRegistrationToServer(ctx);
    }

    @Override
    public void onNewToken(String s) {
        Log.i(TAG, "onNewToken() called with: s = [" + s + "]");
        if (!enabled) {
            Log.i(TAG, "onNewToken: 谷歌推送fcm还没启用");
            return;
        }
        sendRegistrationToServer(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived() called with: remoteMessage = [" + remoteMessage + "]");
        LogUtils.log(TAG, remoteMessage);

        // 谷歌推送不支持前台通知，所有前台通知会走这个方法，可以自己弹，

    }
}
