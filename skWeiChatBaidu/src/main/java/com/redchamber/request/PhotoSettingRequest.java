package com.redchamber.request;

import android.content.Context;
import android.text.TextUtils;

import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 照片设置
 */
public class PhotoSettingRequest {

    private volatile static PhotoSettingRequest instance;

    private PhotoSettingRequest() {
    }

    public static PhotoSettingRequest getInstance() {
        if (instance == null) {
            synchronized (PhotoSettingRequest.class) {
                if (instance == null) {
                    instance = new PhotoSettingRequest();
                }
            }
        }
        return instance;
    }

    public void setPhoto(Context context, String photoIds, String visitType, String coin, SettingCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("photoIds", photoIds);
        params.put("visitType", visitType);
        if (!TextUtils.isEmpty(coin)) {
            params.put("coin", coin);
        }

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_PHOTO_SETTING)
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

    public void deletePhoto(Context context, String photoIds, SettingCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("photoId", photoIds);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_PHOTO_DELETE)
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


    public interface SettingCallBack {

        void onSuccess();

        void onFail(String error);

    }


}
