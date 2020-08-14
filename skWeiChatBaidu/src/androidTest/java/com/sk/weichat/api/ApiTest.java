package com.sk.weichat.api;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sk.weichat.helper.PaySecureHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.builder.BaseBuilder;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;

@RunWith(AndroidJUnit4.class)
public class ApiTest {
    private Context mContext = ApplicationProvider.getApplicationContext();
    private String payPassword = "111111";
    private String toUserId = "10016664";
    private String money = "1";

    public void buildPay(String apiUrl, Map<String, String> params, String value,
                         String payPassword, PaySecureHelper.Function<Throwable> onError,
                         PaySecureHelper.Function<BaseBuilder.BaseCall> onSuccess) {
        PaySecureHelper.generateParam(
                mContext, payPassword, params,
                value,
                t -> {
                    onError.apply(t);
                }, (p, code) -> {
                    BaseBuilder.BaseCall call = HttpUtils.get().url(apiUrl)
                            .params(p)
                            .build();
                    onSuccess.apply(call);
                });
    }

    public void buildRed(PaySecureHelper.Function<Throwable> onError,
                         PaySecureHelper.Function<BaseBuilder.BaseCall> onSuccess) throws Exception {
        Map<String, String> params = new HashMap<>();
        String type = "1";
        String count = "1";
        String words = "财源滚滚，心想事成";
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", toUserId);
        String value = "" + type + money + count + words + toUserId;
        buildPay(CoreManager.requireConfig(mContext).REDPACKET_SEND, params, value, payPassword, onError, onSuccess);
    }

    public void buildTran(PaySecureHelper.Function<Throwable> onError,
                          PaySecureHelper.Function<BaseBuilder.BaseCall> onSuccess) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("toUserId", toUserId);
        params.put("money", money);
        String value = "" + toUserId + money + "";
        buildPay(CoreManager.requireConfig(mContext).SKTRANSFER_SEND_TRANSFER, params, value, payPassword, onError, onSuccess);
    }

    @Test
    public void testMulti() throws Exception {
        Thread thread = Thread.currentThread();
        AtomicReference<BaseBuilder.BaseCall> redCall = new AtomicReference<>();
        AtomicReference<BaseBuilder.BaseCall> tranCall = new AtomicReference<>();
        buildRed(t -> {
            System.out.println("失败");
            thread.interrupt();
        }, call -> {
            redCall.set(call);
            if (tranCall.get() != null) {
                thread.interrupt();
            }
        });
        buildTran(t -> {
            System.out.println("失败");
            thread.interrupt();
        }, call -> {
            tranCall.set(call);
            if (redCall.get() != null) {
                thread.interrupt();
            }
        });
        try {
            Thread.sleep(8000);
        } catch (InterruptedException ignored) {
        }
        Objects.requireNonNull(redCall.get());
        Objects.requireNonNull(tranCall.get());
        final int[] success = {0};
        final int[] failed = {0};
        for (BaseBuilder.BaseCall call : Arrays.asList(redCall.get(), tranCall.get())) {
            Log.i(HttpUtils.TAG, "实际请求时间：" + System.currentTimeMillis());
            call.execute(new BaseCallback<Void>(Void.class) {
                @Override
                public void onResponse(ObjectResult<Void> result) {
                    if (Result.checkSuccess(mContext, result)) {
                        success[0]++;
                    } else {
                        failed[0]++;
                    }
                }

                @Override
                public void onError(Call call, Exception e) {
                    failed[0]++;
                }
            });
        }
        try {
            Thread.sleep(6000);
        } catch (InterruptedException ignored) {
        }
        System.out.println("成功次数：" + success[0]);
        System.out.println("失败次数：" + failed[0]);
    }

    @Test
    public void testRed() throws Exception {
        Thread thread = Thread.currentThread();
        buildRed(t -> {
            System.out.println("失败");
            thread.interrupt();
        }, call -> {
            call.execute(new BaseCallback<Void>(Void.class) {
                @Override
                public void onResponse(ObjectResult<Void> result) {
                    if (Result.checkSuccess(mContext, result)) {
                        System.out.println("成功");
                    } else {
                        System.out.println("失败");
                    }
                    thread.interrupt();
                }

                @Override
                public void onError(Call call, Exception e) {
                    System.out.println("失败");
                    thread.interrupt();
                }
            });
        });
        try {
            Thread.sleep(8000);
        } catch (InterruptedException ignored) {
        }
    }
}
