package com.redchamber.request;

import android.content.Context;

import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.lib.utils.ToastUtils;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 获取免费权限剩余的次数
 */
public class FreeAuthTimesRequest {

    private volatile static FreeAuthTimesRequest instance;

    private FreeAuthTimesRequest() {
    }

    public static FreeAuthTimesRequest getInstance() {
        if (instance == null) {
            synchronized (FreeAuthTimesRequest.class) {
                if (instance == null) {
                    instance = new FreeAuthTimesRequest();
                }
            }
        }
        return instance;
    }

    /**
     * @param type 001发布 005私聊 006连麦 007解锁相册
     */
    public void queryFreeAuthTimes(Context context, String type, FreeAuthTimesCallBack callBack) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("type", type);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_MY_QUERY_FREEAUTH_TIMES)
                .params(params)
                .build()
                .execute(new BaseCallback<QueryFreeAuthBean>(QueryFreeAuthBean.class) {

                    @Override
                    public void onResponse(ObjectResult<QueryFreeAuthBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            callBack.onSuccess(result.getData());
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        callBack.onFail(e.getMessage());
                    }
                });
    }


    public interface FreeAuthTimesCallBack {

        void onSuccess(QueryFreeAuthBean freeAuthBean);

        void onFail(String error);

    }

}
