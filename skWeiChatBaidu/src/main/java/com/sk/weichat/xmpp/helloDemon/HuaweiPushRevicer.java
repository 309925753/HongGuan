package com.sk.weichat.xmpp.helloDemon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.support.api.push.PushReceiver;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.notification.NotificationProxyActivity;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 华为推送
 */

public class HuaweiPushRevicer extends PushReceiver {
    private static final String TAG = "Huawei PushReceiver";

    public static Map<String, String> extrasToMap(String extras) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONArray jsonArray = JSON.parseArray(extras);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    if (entry.getValue() != null) {
                        map.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        } catch (Exception e) {
            Reporter.post("华为通知参数解析失败", e);
        }
        return map;
    }

    /**
     * 连接上华为服务时会调用,可以获取token值
     */
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        Log.e("push", "华为推送绑定成功");
        String belongId = extras.getString("belongId");
        String content = "get token and belongId successful, token = " + token + ",belongId = " + belongId;
        Log.e("push", content);

        /**
         * 得到token,上传至服务器
         */
        String area = PreferenceUtils.getString(context, AppConstant.EXTRA_CLUSTER_AREA);
        if (TextUtils.isEmpty(area)) {
            area = "CN";
        } else {
            if (!area.endsWith(",")) {
                area += ",";
            }
            String[] split = area.split(",");
            if (split.length > 0) {
                area = split[0];
            } else {
                area = "CN";
            }
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(context).accessToken);
        params.put("token", token);
        params.put("adress", area);
        params.put("deviceId", "3");

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).configHw)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        Log.e("push", "上传成功");
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("push", "上传失败");
                    }
                });
    }

    /**
     * 连接状态的回调方法
     */
    @Override
    public void onPushState(Context context, boolean pushState) {
        try {
            String content = "---------The current push status： " + (pushState ? "Connected" : "Disconnected");
            Log.e("push", content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知
     * 1.标题
     * 2.消息内容
     * 3.自定义内容{键，值}(不会显示在通知栏上，如自定义内容为空，点击通知不会回调onEvent方法)
     */
    @Override
    public void onEvent(Context context, PushReceiver.Event event, Bundle extras) {
        /*if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {// 通知栏中的通知被点击打开 || 通知栏中通知上的按钮被点击  }*/
        String message = extras.getString(BOUND_KEY.pushMsgKey);
        Log.e("push", "自定义内容:" + message);
        Map<String, String> params = extrasToMap(message);
        LogUtils.log("push", "通知参数：" + params);
        NotificationProxyActivity.start(context, params);
    }

    /**
     * 透传消息的回调方法
     */
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            // 可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            Log.e("push", "收到华为推送透传消息,消息内容为:" + content);

            Intent intent;
            if (AppUtils.isAppRunning(context)) {// APP进程存在
                intent = new Intent(context, MainActivity.class);
            } else {// APP进程不存在
                intent = new Intent(context, SplashActivity.class);
            }
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
