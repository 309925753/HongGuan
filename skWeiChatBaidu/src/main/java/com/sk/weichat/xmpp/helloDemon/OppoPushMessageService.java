package com.sk.weichat.xmpp.helloDemon;

import android.content.Context;
import android.util.Log;

import com.coloros.mcssdk.PushManager;
import com.coloros.mcssdk.PushService;
import com.coloros.mcssdk.callback.PushAdapter;
import com.coloros.mcssdk.mode.AppMessage;
import com.coloros.mcssdk.mode.CommandMessage;
import com.coloros.mcssdk.mode.SptDataMessage;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import okhttp3.Call;

/**
 * <p>Title:${Title} </p>
 * <p>Description: PushMessageService</p>
 * <p>Copyright (c) 2016 www.oppo.com Inc. All rights reserved.</p>
 * <p>Company: OPPO</p>
 *
 * @author QuWanxin
 * @version 1.0
 * @date 2017/7/28
 */

/**
 * 如果应用需要解析和处理Push消息（如透传消息），则继承PushService来处理，并在Manifest文件中申明Service
 * 如果不需要处理Push消息，则不需要继承PushService，直接在Manifest文件申明PushService即可
 */
public class OppoPushMessageService extends PushService {
    private static final String TAG = "OppoPushMessageService";

    public static void init(Context ctx) {
        if (!PushManager.isSupportPush(ctx)) {
            return;
        }
        PushManager.getInstance().register(ctx, BuildConfig.OPPO_APP_KEY, BuildConfig.OPPO_APP_SECRET, new PushAdapter() {
            @Override
            public void onRegister(int i, String regId) {
                putRegId(CoreManager.requireSelfStatus(ctx).accessToken, regId);
            }
        });
    }

    public static void putRegId(String accessToken, String regId) {
        Log.d(TAG, "putRegId() called with: accessToken = [" + accessToken + "], regId = [" + regId + "]");
        String at = accessToken;
        if (at == null) {
            Reporter.post("access token is null");
        } else {
            HttpUtils.post()
                    .url(CoreManager.requireConfig(MyApplication.getInstance()).configOp)
                    .params("pushId", regId)
                    .params("access_token", at)
                    // devicesId后端没有用上，但是沿用旧接口的参数列表带上这个，实际没用，
                    .params("deviceId", "6")
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {
                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            Log.i("push", "上传oppo推送Id成功");
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            Reporter.post("上传谷oppo推送Id失败，", e);
                        }
                    });
        }
    }

    /**
     * 命令消息，主要是服务端对客户端调用的反馈，一般应用不需要重写此方法
     *
     * @param context
     * @param commandMessage
     */
    @Override
    public void processMessage(Context context, CommandMessage commandMessage) {
        super.processMessage(context, commandMessage);
    }

    /**
     * 普通应用消息，视情况看是否需要重写
     *
     * @param context
     * @param appMessage
     */
    @Override
    public void processMessage(Context context, AppMessage appMessage) {
        super.processMessage(context, appMessage);
    }


    /**
     * 透传消息处理，应用可以打开页面或者执行命令,如果应用不需要处理透传消息，则不需要重写此方法
     *
     * @param context
     * @param sptDataMessage
     */
    @Override
    public void processMessage(Context context, SptDataMessage sptDataMessage) {
        super.processMessage(context.getApplicationContext(), sptDataMessage);
    }
}
