package com.redchamber.request;

import android.content.Context;

import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 相册隐私设置
 */
public class AppSettingRequest {

    private volatile static AppSettingRequest instance;

    private AppSettingRequest() {
    }

    public static AppSettingRequest getInstance() {
        if (instance == null) {
            synchronized (AppSettingRequest.class) {
                if (instance == null) {
                    instance = new AppSettingRequest();
                }
            }
        }
        return instance;
    }

    public void set(Context context, String type, int openFlag, AppSettingCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("type", type);
        params.put("openFlag", String.valueOf(openFlag));

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_APP_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            callBack.onSuccess();
                        } else {
                            callBack.onFail(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        callBack.onFail(e.getMessage());
                    }
                });
    }

    public interface AppSettingCallBack {

        void onSuccess();

        void onFail(String error);

    }

}
