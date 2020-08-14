package com.sk.weichat.xmpp.helloDemon;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.PushManager;
import com.meizu.cloud.pushsdk.handler.MzPushMessage;
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.UserStatus;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.LogUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import okhttp3.Call;

/**
 * 魅族推送的广播接收器，
 * 参考官方demo,
 * https://github.com/MEIZUPUSH/PushDemo/blob/master/PushdemoInternal/src/main/java/com/meizu/pushdemo/PushMsgReceiver.java
 */
public class MeizuPushMsgReceiver extends MzPushMessageReceiver {
    private static final String TAG = "MeizuPushMsgReceiver";
    private static final String APP_ID = BuildConfig.MEIZU_APP_ID;
    private static final String APP_KEY = BuildConfig.MEIZU_APP_KEY;

    public static void init(Context ctx) {
        PushManager.register(ctx, APP_ID, APP_KEY);
    }

    public static void uploadPushId(String accessToken, String pushId) {
        Log.i(TAG, "uploadPushId() called with: accessToken = [" + accessToken + "], regId = [" + pushId + "]");
        if (accessToken == null) {
            Reporter.post("access token is null");
        } else {
            HttpUtils.post()
                    .url(CoreManager.requireConfig(MyApplication.getInstance()).configMz)
                    .params("pushId", pushId)
                    .params("access_token", accessToken)
                    // devicesId后端没有用上，但是沿用旧接口的参数列表带上这个，实际没用，
                    .params("deviceId", "4")
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {
                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            Log.i("push", "上传魅族推送Id成功");
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            Reporter.post("上传谷魅族推送Id失败，", e);
                        }
                    });
        }
    }

    private static void uploadPushId(Context context, String pushId) {
        if (!TextUtils.isEmpty(pushId)) {
            // 打开通知功能，
            PushManager.switchPush(context, APP_ID, APP_KEY, pushId, 0, true);
            // 上传pushId,
            // 这里的accessToken有可能为空，
            // 在登录并进入MainActivity前收到广播的话这里没有accessToken,
            // 没有实际效果影响，因为进入MainActivity后还会上传一次regId,
            UserStatus selfStatus = CoreManager.getSelfStatus(context);
            if (selfStatus != null) {
                String accessToken = selfStatus.accessToken;
                if (!TextUtils.isEmpty(accessToken)) {
                    uploadPushId(CoreManager.requireSelfStatus(context).accessToken, pushId);
                }
            }
        }
    }

    /**
     * @deprecated 连官方demo都在使用deprecated方法，应该是改成了onRegisterStatus，
     */
    @Override
    @Deprecated
    public void onRegister(Context context, String s) {
        Log.i(TAG, "onRegister pushID " + s);
        uploadPushId(context, s);
    }

    @Override
    public void onMessage(Context context, String s) {
        Log.i(TAG, "onMessage " + s);
    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Log.i(TAG, "flyme3 onMessage ");
        LogUtils.log(intent);
    }

    @Override
    public void onMessage(Context context, String message, String platformExtra) {
        Log.i(TAG, "onMessage " + message + " platformExtra " + platformExtra);
    }

    /**
     * @deprecated 连官方demo都在使用deprecated方法，应该是改成了onRegisterStatus，
     */
    @Override
    @Deprecated
    public void onUnRegister(Context context, boolean b) {
        Log.i(TAG, "onUnRegister " + b);
    }

    @Override
    public void onPushStatus(Context context, PushSwitchStatus pushSwitchStatus) {
    }

    @Override
    public void onRegisterStatus(Context context, RegisterStatus registerStatus) {
        Log.i(TAG, "onRegisterStatus " + registerStatus + " " + context.getPackageName());
        String pushId = registerStatus.getPushId();
        uploadPushId(context, pushId);
    }

    @Override
    public void onUnRegisterStatus(Context context, UnRegisterStatus unRegisterStatus) {
        Log.i(TAG, "onUnRegisterStatus " + unRegisterStatus + " " + context.getPackageName());
    }

    @Override
    public void onSubTagsStatus(Context context, SubTagsStatus subTagsStatus) {
        Log.i(TAG, "onSubTagsStatus " + subTagsStatus + " " + context.getPackageName());
    }

    @Override
    public void onSubAliasStatus(Context context, SubAliasStatus subAliasStatus) {
        Log.i(TAG, "onSubAliasStatus " + subAliasStatus + " " + context.getPackageName());
    }

    @Override
    public void onUpdateNotificationBuilder(PushNotificationBuilder pushNotificationBuilder) {
        pushNotificationBuilder.setmStatusbarIcon(R.mipmap.ic_logo);
        Log.e(TAG, "current clickpacakge " + pushNotificationBuilder.getClickPackageName());
    }

    @Override
    public void onNotificationArrived(Context context, MzPushMessage mzPushMessage) {
        Log.i(TAG, "onNotificationArrived title " + mzPushMessage.getTitle() + "content "
                + mzPushMessage.getContent() + " selfDefineContentString " + mzPushMessage.getSelfDefineContentString() + " notifyId " + mzPushMessage.getNotifyId());
    }

    @Override
    public void onNotificationClicked(Context context, MzPushMessage mzPushMessage) {
        Log.i(TAG, "onNotificationClicked title " + mzPushMessage.getTitle() + "content "
                + mzPushMessage.getContent() + " selfDefineContentString " + mzPushMessage.getSelfDefineContentString() + " notifyId " + mzPushMessage.getNotifyId());
    }

    @Override
    public void onNotificationDeleted(Context context, MzPushMessage mzPushMessage) {
        Log.i(TAG, "onNotificationDeleted title " + mzPushMessage.getTitle() + "content "
                + mzPushMessage.getContent() + " selfDefineContentString " + mzPushMessage.getSelfDefineContentString() + " notifyId " + mzPushMessage.getNotifyId());
    }

    @Override
    public void onNotifyMessageArrived(Context context, String message) {
        Log.i(TAG, "onNotifyMessageArrived messsage " + message);
    }
}

