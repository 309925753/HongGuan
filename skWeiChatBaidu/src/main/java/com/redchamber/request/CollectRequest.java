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
 * 加入收藏、取消收藏
 */
public class CollectRequest {

    public void request(Context context, String friendId, String state, CollectStatusCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("friendId", friendId);
        params.put("state", state);// 0：添加 1：取消

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_ADD_COLLECTION)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            callBack.onSuccess("0".equals(state) ? "1" : "0");
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

    public interface CollectStatusCallBack {

        void onSuccess(String result);

        void onFail(String error);

    }


}
