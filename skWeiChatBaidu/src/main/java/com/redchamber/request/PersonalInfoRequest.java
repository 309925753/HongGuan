package com.redchamber.request;

import android.content.Context;

import com.redchamber.bean.MyHomepageBean;
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
 * 个人信息查询
 */
public class PersonalInfoRequest {

    private volatile static PersonalInfoRequest instance;

    private PersonalInfoRequest() {
    }

    public static PersonalInfoRequest getInstance() {
        if (instance == null) {
            synchronized (PersonalInfoRequest.class) {
                if (instance == null) {
                    instance = new PersonalInfoRequest();
                }
            }
        }
        return instance;
    }

    public void getMyHomepage(Context context, PersonalInfoCallBack callBack) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_USER_INFO_MY_HOMEPAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<MyHomepageBean>(MyHomepageBean.class) {

                    @Override
                    public void onResponse(ObjectResult<MyHomepageBean> result) {
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


    public interface PersonalInfoCallBack {

        void onSuccess(MyHomepageBean myHomepageBean);

        void onFail(String error);

    }

}
