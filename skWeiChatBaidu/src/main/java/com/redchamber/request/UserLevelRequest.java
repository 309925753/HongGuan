package com.redchamber.request;

import android.content.Context;

import com.redchamber.bean.UserLevelBean;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 获取当前用户的userLevel
 */
public class UserLevelRequest {

    private volatile static UserLevelRequest instance;

    private UserLevelRequest() {
    }

    public static UserLevelRequest getInstance() {
        if (instance == null) {
            synchronized (UserLevelRequest.class) {
                if (instance == null) {
                    instance = new UserLevelRequest();
                }
            }
        }
        return instance;
    }

    public void queryUserLevel(Context context, UserLevelCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_USER_LEVEL)
                .params(params)
                .build()
                .execute(new BaseCallback<UserLevelBean>(UserLevelBean.class) {

                    @Override
                    public void onResponse(ObjectResult<UserLevelBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            callBack.onSuccess(result.getData().userLevel);
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

    public interface UserLevelCallBack {

        void onSuccess(String userLevel);

        void onFail(String error);

    }

}
