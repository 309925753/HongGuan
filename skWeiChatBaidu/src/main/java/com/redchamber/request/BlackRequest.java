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
 * 黑名单
 */
public class BlackRequest {

    private volatile static BlackRequest blackRequest;

    private BlackRequest() {
    }

    public static BlackRequest getInstance() {
        if (blackRequest == null) {
            synchronized (BlackRequest.class) {
                if (blackRequest == null) {
                    blackRequest = new BlackRequest();
                }
            }
        }
        return blackRequest;
    }

    //判断是否加入黑名单
    public void isMyBlackList(Context
                                      context, String friendId, IsMyBlackListCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("friendId", friendId);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_IS_MY_BLACK_LIST)
                .params(params)
                .build()
                .execute(new BaseCallback<Boolean>(Boolean.class) {

                    @Override
                    public void onResponse(ObjectResult<Boolean> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            callBack.onSuccess(result.getData());
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

    /**
     * 添加或删除黑名单
     *
     * @param stats 状态0：添加1：删除
     */
    public void addBlackList(Context
                                     context, String friendId, String stats, AddBlackListCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("friendId", friendId);
        params.put("stats", stats);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_ADD_BLACK_LIST)
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


    public interface IsMyBlackListCallBack {

        void onSuccess(boolean isBlack);

        void onFail(String error);

    }

    public interface AddBlackListCallBack {

        void onSuccess();

        void onFail(String error);

    }


}
