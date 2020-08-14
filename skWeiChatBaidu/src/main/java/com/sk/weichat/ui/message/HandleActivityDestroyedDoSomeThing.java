package com.sk.weichat.ui.message;

import com.sk.weichat.MyApplication;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 处理一些界面onDestroy时需要执行的操作
 */
public class HandleActivityDestroyedDoSomeThing {

    /**
     * 面对面建群 退出当前界面需要退出群组
     */
    public static void handleFaceGroupDestroyed(String jid) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("jid", jid);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).ROOM_LOCATION_EXIT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
}
