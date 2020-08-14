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
 * 付费解锁照片
 */
public class UnlockPhotoRequest {

    private volatile static UnlockPhotoRequest instance;

    private UnlockPhotoRequest() {
    }

    public static UnlockPhotoRequest getInstance() {
        if (instance == null) {
            synchronized (UnlockPhotoRequest.class) {
                if (instance == null) {
                    instance = new UnlockPhotoRequest();
                }
            }
        }
        return instance;
    }

    public void unlockPhoto(Context context, String userId, String photoId, String gold, UnlockCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("photoId", photoId);
        params.put("gold", gold);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_PAY_UNLOCK_PHOTO)
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

    public void unlockAlbum(Context context, String userId, String type, UnlockCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("type", type);//解锁类别，0免费解锁 1付费解锁

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_PAY_UNLOCK_ALBUM)
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


    public interface UnlockCallBack {

        void onSuccess();

        void onFail(String error);

    }


}
