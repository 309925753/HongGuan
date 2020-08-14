package com.sk.weichat.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.PayCode;
import com.sk.weichat.bean.PayPrivateKey;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.me.redpacket.PayPasswordVerifyDialog;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.MAC;
import com.sk.weichat.util.secure.PayPassword;
import com.sk.weichat.util.secure.RSA;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

/**
 * 支付加密工具类，
 * <p>
 * 线程比较混乱，主要是http回调在主线程，因此可能存在部分加密操作在主线程执行，应改为异步线程，
 */
public class PaySecureHelper {
    private static final String KEY_PRIVATE_KEY = "pay_private_key";
    private static final String TAG = "PaySecureHelper";
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * @param money 金额字符串，直接显示，单位元，
     */
    @MainThread
    public static Dialog inputPayPassword(Context ctx, String action, String money, Function<String> onSuccess) {
        PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(ctx);
        dialog.setAction(action);
        dialog.setMoney(money);
        dialog.setOnInputFinishListener(password -> {
            onSuccess.apply(password);
        });
        try {
            dialog.show();
        } catch (Exception ignored) {
            // 线程切换可能导致弹对话框时activity已经关闭，show会抛异常，
        }
        return dialog;
    }

    /**
     * 配置支付加密的参数，
     * <p>
     * 成功失败的回调都是在主线程调用的，
     *
     * @param onError 确保失败时回调，不能出现既不成功也不失败的情况，因为外面可能有对话框等着关闭，
     */
    public static void generateParam(
            Context ctx, String payPassword,
            Map<String, String> params, String valueString,
            Function<Throwable> onError,
            Function2<Map<String, String>, byte[]> onSuccess) {
        AsyncUtils.doAsync(ctx, t -> {
            Reporter.post("生成支付参数失败", t);
            AsyncUtils.runOnUiThread(ctx, c -> {
                onError.apply(t);
            });
        }, executor, c -> {
            CoreManager coreManager = CoreManager.getInstance(ctx);
            byte[] key = PayPassword.encode(payPassword);
            getCode(ctx, coreManager, key, t -> {
                Log.i(TAG, "获取临时密码失败", t);
                c.uiThread(r -> {
                    onError.apply(t);
                });
            }, (encryptedCode, codeId) -> {
                // 到这里说明支付密码正确，
                getRsaPrivateKey(ctx, coreManager, key, t -> {
                    Log.i(TAG, "获取支付私钥失败", t);
                    c.uiThread(r -> {
                        onError.apply(t);
                    });
                }, new Function<byte[]>() {
                    @Override
                    public void apply(byte[] privateKey) {
                        byte[] code;
                        try {
                            code = RSA.decryptFromBase64(encryptedCode, privateKey);
                        } catch (Exception e) {
                            Log.i(TAG, "私钥解密临时密码失败", e);
                            requestPrivateKey(ctx, coreManager, key, onError, this);
                            return;
                        }
                        String payPasswordMd5 = PayPassword.md5(coreManager.getSelf().getUserId(), key);
                        String time = String.valueOf(System.currentTimeMillis());
                        String mac = RSA.signBase64(AppConfig.apiKey + coreManager.getSelf().getUserId() + UserSp.getInstance(ctx).getAccessToken() + valueString + time + payPasswordMd5, privateKey);
                        JSONObject json = new JSONObject();
                        json.putAll(params);
                        json.put("mac", mac);
                        json.put("time", time);
                        String data = AES.encryptBase64(json.toJSONString(), code);
                        Map<String, String> p = new HashMap<>();
                        p.put("data", data);
                        p.put("codeId", codeId);
                        c.uiThread(r -> {
                            onSuccess.apply(p, code);
                        });
                    }
                });
            });
        });
    }

    @NonNull
    private static String createSalt() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static void getCode(Context ctx, CoreManager coreManager, byte[] key, Function<Throwable> onError, Function2<String, String> onSuccess) {
        String payPasswordMd5 = PayPassword.md5(coreManager.getSelf().getUserId(), key);
        String salt = createSalt();
        String mac = MAC.encodeBase64((AppConfig.apiKey + coreManager.getSelf().getUserId() + UserSp.getInstance(ctx).getAccessToken() + salt).getBytes(), payPasswordMd5);
        final Map<String, String> params = new HashMap<>();
        params.put("mac", mac);
        params.put("salt", salt);

        HttpUtils.post().url(coreManager.getConfig().PAY_SECURE_GET_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<PayCode>(PayCode.class) {

                    @Override
                    public void onResponse(ObjectResult<PayCode> result) {
                        if (Result.checkSuccess(ctx, result, false)) {
                            if (result.getData() == null || TextUtils.isEmpty(result.getData().getCode())) {
                                // 服务器没有公钥，创建一对上传后从新调用getCode,
                                makeRsaKeyPair(ctx, coreManager, key, onError, privateKey -> {
                                    getCode(ctx, coreManager, key, onError, onSuccess);
                                });
                            } else {
                                onSuccess.apply(result.getData().getCode(), result.getData().getCodeId());
                            }
                        } else {
                            onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });

    }

    private static void getRsaPrivateKey(Context ctx, CoreManager coreManager, byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        SharedPreferences sp = getPreferences(ctx);
        String encryptedPrivateKey = sp.getString(KEY_PRIVATE_KEY + coreManager.getSelf().getUserId(), null);
        if (TextUtils.isEmpty(encryptedPrivateKey)) {
            requestPrivateKey(ctx, coreManager, key, onError, onSuccess);
        } else {
            byte[] privateKey;
            try {
                privateKey = AES.decryptFromBase64(encryptedPrivateKey, key);
            } catch (Exception e) {
                // 解密失败，支付密码错误，是其他设备改过支付密码，重新获取私钥，
                requestPrivateKey(ctx, coreManager, key, onError, onSuccess);
                return;
            }
            onSuccess.apply(privateKey);
        }
    }

    private static void requestPrivateKey(Context ctx, CoreManager coreManager, byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        final Map<String, String> params = new HashMap<>();

        HttpUtils.post().url(coreManager.getConfig().PAY_SECURE_GET_PRIVATE_KEY)
                .params(params)
                .build()
                .execute(new BaseCallback<PayPrivateKey>(PayPrivateKey.class) {

                    @Override
                    public void onResponse(ObjectResult<PayPrivateKey> result) {
                        if (Result.checkSuccess(ctx, result, false)) {
                            String encryptedPrivateKey;
                            if (result.getData() != null && !TextUtils.isEmpty(encryptedPrivateKey = result.getData().getPrivateKey())) {
                                savePrivateKey(ctx, coreManager, encryptedPrivateKey);
                                byte[] privateKey;
                                try {
                                    privateKey = AES.decryptFromBase64(encryptedPrivateKey, key);
                                } catch (Exception e) {
                                    // 解密失败，支付密码错误，
                                    onError.apply(new IllegalArgumentException(ctx.getString(R.string.tip_wrong_pay_password)));
                                    return;
                                }
                                onSuccess.apply(privateKey);
                            } else {
                                makeRsaKeyPair(ctx, coreManager, key, onError, onSuccess);
                            }
                        } else {
                            onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });
    }

    private static void makeRsaKeyPair(Context ctx, CoreManager coreManager,
                                       byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        AsyncUtils.doAsync(ctx, t -> {
            onError.apply(new IllegalStateException(ctx.getString(R.string.tip_server_error)));
        }, c -> {
            RSA.RsaKeyPair rsaKeyPair = RSA.genKeyPair();
            byte[] encryptedPrivateKey = AES.encrypt(rsaKeyPair.getPrivateKey(), key);
            String encryptedPrivateKeyBase64 = AES.encryptBase64(rsaKeyPair.getPrivateKey(), key);
            String macKey = PayPassword.md5(coreManager.getSelf().getUserId(), key);
            byte[] macContent = Arrays.copyOf(encryptedPrivateKey, encryptedPrivateKey.length + rsaKeyPair.getPublicKey().length);
            System.arraycopy(rsaKeyPair.getPublicKey(), 0, macContent, encryptedPrivateKey.length, rsaKeyPair.getPublicKey().length);
            String mac = MAC.encodeBase64(macContent, macKey);
            final Map<String, String> params = new HashMap<>();
            params.put("publicKey", rsaKeyPair.getPublicKeyBase64());
            params.put("privateKey", encryptedPrivateKeyBase64);
            params.put("mac", mac);

            HttpUtils.post().url(coreManager.getConfig().PAY_SECURE_UPLOAD_KEY)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            if (Result.checkSuccess(ctx, result, false)) {
                                savePrivateKey(ctx, coreManager, encryptedPrivateKeyBase64);
                                onSuccess.apply(rsaKeyPair.getPrivateKey());
                            } else {
                                onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            onError.apply(e);
                        }
                    });
        });
    }

    private static void savePrivateKey(Context ctx, CoreManager coreManager, String encryptedPrivateKey) {
        getPreferences(ctx).edit()
                .putString(KEY_PRIVATE_KEY + coreManager.getSelf().getUserId(), encryptedPrivateKey)
                .apply();
    }

    private static SharedPreferences getPreferences(Context ctx) {
        return ctx.getSharedPreferences("sk_rsa_key_pair", Context.MODE_PRIVATE);
    }

    public interface Function<T> {
        void apply(T t);
    }

    public interface Function2<T, R> {
        void apply(T t, R r);
    }

    public interface Function3<T, R, E> {
        void apply(T t, R r, E e);
    }
}
