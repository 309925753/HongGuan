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
 * 创建订单
 */
public class CreateOrderRequest {

    private volatile static CreateOrderRequest instance;

    private CreateOrderRequest() {
    }

    public static CreateOrderRequest getInstance() {
        if (instance == null) {
            synchronized (CreateOrderRequest.class) {
                if (instance == null) {
                    instance = new CreateOrderRequest();
                }
            }
        }
        return instance;
    }

    public void create(Context context, String type, String configId, String money, String sex, CreateOrderCallBack callBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("payType", "1");//支付方式 1支付宝支付 2微信支付 3苹果内购
        params.put("type", type);//充值类型 1开通会员 2充值红豆
        params.put("configId", configId);//配置的ID，会员套餐的ID或充值金额配置的ID
        params.put("money", money);//配置的ID，会员套餐的ID或充值金额配置的ID
        params.put("sex", sex);//性别 0女 1男
        params.put("deviceType", "1");//设备类型，区分苹果和安卓
        params.put("userId", CoreManager.getInstance(context).getSelf().getUserId());

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_PAY_CREATE_ORDER)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
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


    public interface CreateOrderCallBack {

        void onSuccess(String aliPayOrderId);

        void onFail(String error);

    }


}
