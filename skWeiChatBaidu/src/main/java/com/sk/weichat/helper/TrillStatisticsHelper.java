package com.sk.weichat.helper;

import android.content.Context;
import android.text.TextUtils;

import com.sk.weichat.AppConstant;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class TrillStatisticsHelper {
    /**
     * fromUserId为AppConstant.TRILL_INSTANT_ID的就是短视频分享的消息，
     * {@see com.sk.weichat.ui.trill.TriListActivity#onShare(java.lang.String, long, int)}
     *
     * @return true表示是短视频分享的消息，
     */
    private static boolean checkTrillShare(String originalUserId) {
        return TextUtils.equals(originalUserId, AppConstant.TRILL_INSTANT_ID);
    }

    private static void share(Context ctx, CoreManager coreManager, String trillId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", trillId);
        HttpUtils.get().url(coreManager.getConfig().TRILL_ADD_FORWARD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        Result.checkSuccess(ctx, result);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ctx);
                    }
                });

    }

    public static void share(Context ctx, CoreManager coreManager, ChatMessage message) {
        if (checkTrillShare(message.getFromUserId())) {
            share(ctx, coreManager, message.getPacketId());
        }
    }

    public static void play(Context ctx, CoreManager coreManager, PublicMessage message) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());
        HttpUtils.get().url(coreManager.getConfig().TRILL_ADD_PLAY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        Result.checkSuccess(ctx, result);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ctx);
                    }
                });
    }
}
