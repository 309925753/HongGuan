package com.sk.weichat.helper;

import android.content.Context;
import android.text.TextUtils;

import com.sk.weichat.R;
import com.sk.weichat.bean.YeepayId;
import com.sk.weichat.bean.YeepayOrder;
import com.sk.weichat.bean.YeepayUrl;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.yeepay.YeepayOpenActivity;
import com.sk.weichat.ui.yeepay.YeepayWebViewActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.Money;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class YeepayHelper {
    private static final String SP_NAME = "sk_yeepay";
    public static boolean ENABLE;

    public static boolean isOpened(Context ctx) {
        return ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getBoolean("opened", false);
    }

    public static void saveOpened(Context ctx, boolean isOpened) {
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("opened", isOpened)
                .apply();
    }

    public static boolean checkOpened(Context ctx) {
        if (!isOpened(ctx)) {
            YeepayOpenActivity.start(ctx);
            return false;
        }
        return true;
    }

    public static boolean checkOpenedOrAsk(Context ctx) {
        if (!isOpened(ctx)) {
            SelectionFrame dialog = new SelectionFrame(ctx);
            dialog.setSomething(ctx.getString(R.string.app_name), ctx.getString(R.string.tip_yeepay_ask_open), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    YeepayOpenActivity.start(ctx);
                }
            });
            dialog.show();

            return false;
        }
        return true;
    }

    public static void sendRed(Context ctx, CoreManager coreManager, String toUserId,
                               final String type, String pMoney, String count,
                               final String words) {
        String money = Money.fromYuan(pMoney);
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", toUserId);
        request(ctx, coreManager.getConfig().YOP_SEND_RED, params, YeepayId.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl(), y.getId());
        });
    }

    public static void sendMucRed(Context ctx, CoreManager coreManager, String toUserId,
                                  final String type, String pMoney, String count,
                                  final String words) {
        String money = Money.fromYuan(pMoney);
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("roomJid", toUserId);
        request(ctx, coreManager.getConfig().YOP_SEND_RED, params, YeepayId.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl(), y.getId());
        });
    }

    public static void queryRed(Context ctx, CoreManager coreManager, String yeepayRedId, Runnable callback) {
        Map<String, String> params = new HashMap<>();
        params.put("id", yeepayRedId);
        request(ctx, coreManager.getConfig().YOP_QUERY_RED, params, Void.class, y -> {
            callback.run();
        });
    }

    public static void queryTransfer(Context ctx, CoreManager coreManager, String tradeNo, Runnable callback) {
        Map<String, String> params = new HashMap<>();
        params.put("id", tradeNo);
        request(ctx, coreManager.getConfig().YOP_QUERY_TRANSFER, params, Void.class, y -> {
            callback.run();
        });
    }

    public static void queryRecharge(Context ctx, CoreManager coreManager, String yeepayRedId, Runnable callback) {
        Map<String, String> params = new HashMap<>();
        params.put("tradeNo", yeepayRedId);
        request(ctx, coreManager.getConfig().YOP_QUERY_RECHARGE, params, Void.class, y -> {
            callback.run();
        });
    }

    public static void queryWithdraw(Context ctx, CoreManager coreManager, String yeepayRedId, Runnable callback) {
        Map<String, String> params = new HashMap<>();
        params.put("tradeNo", yeepayRedId);
        request(ctx, coreManager.getConfig().YOP_QUERY_WITHDRAW, params, Void.class, y -> {
            callback.run();
        });
    }

    public static void transfer(Context ctx, CoreManager coreManager,
                                String mTransferredUserId, String money, String words) {
        Map<String, String> params = new HashMap<>();
        params.put("toUserId", mTransferredUserId);
        params.put("amount", money);
        params.put("money", money);
        if (!TextUtils.isEmpty(words)) {
            params.put("remark", words);
        }
        request(ctx, coreManager.getConfig().YOP_TRANSFER, params, YeepayId.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl(), y.getId());
        });
    }

    public static void recharge(Context ctx, CoreManager coreManager, String amount) {
        HashMap<String, String> params = new HashMap<>();
        params.put("amount", amount);
        request(ctx, coreManager.getConfig().YOP_RECHARGE, params, YeepayOrder.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl(), y.getTradeNo());
        });
    }

    public static void withdraw(Context ctx, CoreManager coreManager, String amount, String withdrawType) {
        HashMap<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("withdrawType", withdrawType);
        request(ctx, coreManager.getConfig().YOP_WITHDRAW, params, YeepayOrder.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl(), y.getTradeNo());
        });
    }

    public static void bind(Context ctx, CoreManager coreManager) {
        HashMap<String, String> params = new HashMap<>();
        requestUrl(ctx, coreManager.getConfig().YOP_BIND, params);
    }

    public static void secure(Context ctx, CoreManager coreManager) {
        HashMap<String, String> params = new HashMap<>();
        requestUrl(ctx, coreManager.getConfig().YOP_SECURE, params);
    }

    public static void requestUrl(Context ctx, String url, Map<String, String> params) {
        request(ctx, url, params, YeepayUrl.class, y -> {
            YeepayWebViewActivity.start(ctx, y.getUrl());
        });
    }

    public static <T> void request(Context ctx, String url, Map<String, String> params, Class<T> clazz, RequestCallBack<T> callBack) {
        if (!checkOpened(ctx)) return;
        DialogHelper.showDefaulteMessageProgressDialog(ctx);
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<T>(clazz) {

                    @Override
                    public void onResponse(ObjectResult<T> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ctx, result)) {
                            callBack.result(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(ctx);
                    }
                });
    }

    private interface RequestCallBack<T> {
        void result(T t);
    }
}
