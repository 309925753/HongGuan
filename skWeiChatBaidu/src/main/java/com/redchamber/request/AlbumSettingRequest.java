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
 * 相册隐私设置
 */
public class AlbumSettingRequest {

    private volatile static AlbumSettingRequest instance;

    private AlbumSettingRequest() {
    }

    public static AlbumSettingRequest getInstance() {
        if (instance == null) {
            synchronized (AlbumSettingRequest.class) {
                if (instance == null) {
                    instance = new AlbumSettingRequest();
                }
            }
        }
        return instance;
    }

    public void setAlbum(Context context, int type, String coin, AlbumSetCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("type", String.valueOf(type));
        if (!TextUtils.isEmpty(coin)) {
            params.put("coin", String.valueOf(coin));
        }

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_ALBUM_SETTING)
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


    public interface AlbumSetCallBack {

        void onSuccess();

        void onFail(String error);

    }

}
