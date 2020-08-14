package com.sk.weichat.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.EncryptedData;
import com.sk.weichat.bean.LoginAuto;
import com.sk.weichat.bean.LoginCode;
import com.sk.weichat.bean.LoginRegisterResult;
import com.sk.weichat.bean.PayPrivateKey;
import com.sk.weichat.bean.User;
import com.sk.weichat.db.dao.UserDao;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.LoginPassword;
import com.sk.weichat.util.secure.MAC;
import com.sk.weichat.util.secure.MD5;
import com.sk.weichat.util.secure.Parameter;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.wxapi.WXHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

/**
 * 登录加固工具类，
 * <p>
 * 线程比较混乱，主要是http回调在主线程，因此可能存在部分加密操作在主线程执行，应改为异步线程，
 */
public class LoginSecureHelper {
    private static final String TAG = "LoginSecureHelper";
    // 多点登录相关，一种设备只能有一个在登录，
    private static final String DEVICE_ID = "android";
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static boolean logged = false;

    /**
     * 封装普通接口mac验参，
     */
    public static void generateHttpParam(
            Context ctx,
            Map<String, String> params,
            Boolean beforeLogin
    ) {
        if (params.containsKey("secret")) {
            UserSp sp = UserSp.getInstance(MyApplication.getContext());
            String accessToken = sp.getAccessToken();
            if (!TextUtils.isEmpty(accessToken)) {
                params.put("access_token", accessToken);
            }
            return;
        }
        if (beforeLogin) {
            generateBeforeLoginParam(ctx, params);
            return;
        }
        CoreManager coreManager = CoreManager.getInstance(ctx);
        if (coreManager.getSelf() == null) {
            generateBeforeLoginParam(ctx, params);
            return;
        }
        String userId = coreManager.getSelf().getUserId();
        UserSp sp = UserSp.getInstance(ctx);
        String accessToken = sp.getAccessToken();
        if (accessToken == null) {
            generateBeforeLoginParam(ctx, params);
            return;
        }
        String httpKey = sp.getHttpKey();
        if (httpKey == null) {
            generateBeforeLoginParam(ctx, params);
            return;
        }
        String salt = params.remove("salt");
        if (salt == null) {
            salt = String.valueOf(System.currentTimeMillis());
        }
        // 旧代码手动添加的accessToken无视，
        params.remove("access_token");
        String macContent = AppConfig.apiKey + userId + accessToken + Parameter.joinValues(params) + salt;
        String mac = MAC.encodeBase64(macContent.getBytes(), Base64.decode(httpKey));
        params.put("access_token", accessToken);
        params.put("salt", salt);
        params.put("secret", mac);
    }

    private static void generateBeforeLoginParam(Context ctx, Map<String, String> params) {
        String salt = params.remove("salt");
        if (salt == null) {
            salt = String.valueOf(System.currentTimeMillis());
        }
        String macContent = AppConfig.apiKey + Parameter.joinValues(params) + salt;
        byte[] key = MD5.encrypt(AppConfig.apiKey);
        String mac = MAC.encodeBase64(macContent.getBytes(), key);
        params.put("salt", salt);
        params.put("secret", mac);
    }

    /**
     * 只用于登录后免调用自动登录接口，
     * 密码登录后有拿到自动登录返回的token和key就可以不调自动登录了，
     */
    public static void setLogged() {
        logged = true;
    }

    @MainThread
    public static void autoLogin(
            Context ctx,
            CoreManager coreManager,
            Function<Throwable> onError,
            Runnable onSuccess) {
        if (logged) {
            LogUtils.log("HTTP", "============跳过自动登录");
            onSuccess.run();
            return;
        }
        AsyncUtils.doAsync(ctx, t -> {
            Reporter.post("===============自动登录失败", t);
            AsyncUtils.runOnUiThread(ctx, c -> {
                onError.apply(t);
            });
        }, executor, c -> {
            User user = coreManager.getSelf();
            String userId = user.getUserId();
            Map<String, String> params = new HashMap<>();
            params.put("serial", DeviceInfoUtil.getDeviceId(ctx));

            // 地址信息
            double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
            double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
            if (latitude != 0)
                params.put("latitude", String.valueOf(latitude));
            if (longitude != 0)
                params.put("longitude", String.valueOf(longitude));

            if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
                String area = PreferenceUtils.getString(MyApplication.getContext(), AppConstant.EXTRA_CLUSTER_AREA);
                if (!TextUtils.isEmpty(area)) {
                    params.put("area", area);
                }
            }
            LoginSecureHelper.generateAutoLoginParam(
                    ctx, userId,
                    params, t -> {
                        c.uiThread(r -> {
                            onError.apply(t);
                        });
                    },
                    (data, loginToken, loginKeyData, salt) -> {
                        Map<String, String> p = new HashMap<>();
                        p.put("salt", salt);
                        p.put("loginToken", loginToken);
                        p.put("data", data);
                        HttpUtils.get().url(coreManager.getConfig().USER_LOGIN_AUTO)
                                .params(p)
                                .build(true, true)
                                .executeSync(new BaseCallback<EncryptedData>(EncryptedData.class, false) {
                                    @Override
                                    public void onResponse(ObjectResult<EncryptedData> result) {
                                        if (Result.checkSuccess(ctx, result, false) && result.getData() != null && result.getData().getData() != null) {
                                            String realData = LoginSecureHelper.decodeAutoLoginResult(loginKeyData, result.getData().getData());
                                            LoginAuto loginAuto = JSON.parseObject(realData, LoginAuto.class);
                                            UserSp.getInstance(ctx).saveAutoLoginResult(loginAuto);
                                            user.setRole(loginAuto.getRole());
                                            user.setMyInviteCode(loginAuto.getMyInviteCode());
                                            UserDao.getInstance().saveUserLogin(user);
                                            MyApplication.getInstance().initPayPassword(user.getUserId(), loginAuto.getPayPassword());
                                            YeepayHelper.saveOpened(ctx, loginAuto.getWalletUserNo() == 1);
                                            PrivacySettingHelper.setPrivacySettings(MyApplication.getContext(), loginAuto.getSettings());
                                            MyApplication.getInstance().initMulti();

                                            c.uiThread(r -> {
                                                onSuccess.run();
                                            });
                                        } else {
                                            c.uiThread(r -> {
                                                if (Result.checkError(result, Result.CODE_LOGIN_TOKEN_INVALID)) {
                                                    onError.apply(new LoginTokenOvertimeException(Result.getErrorMessage(ctx, result)));
                                                } else {
                                                    onError.apply(new IllegalStateException(Result.getErrorMessage(ctx, result)));
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onError(Call call, Exception e) {
                                        onError.apply(e);
                                    }
                                });
                    });
        });
    }

    @WorkerThread
    public static void generateAutoLoginParam(
            Context ctx, String userId,
            Map<String, String> params,
            Function<Throwable> onError,
            Function4<String, String, byte[], String> onSuccess) {
        UserSp sp = UserSp.getInstance(ctx);
        String loginToken = sp.getLoginToken();
        String loginKey = sp.getLoginKey();
        if (TextUtils.isEmpty(loginToken) || TextUtils.isEmpty(loginKey)) {
            onError.apply(new IllegalStateException("本地没有登录信息"));
            return;
        }
        byte[] loginKeyData = Base64.decode(loginKey);
        String salt = createSalt();
        String mac = MAC.encodeBase64((AppConfig.apiKey + userId + loginToken + Parameter.joinValues(params) + salt).getBytes(), loginKeyData);
        JSONObject json = new JSONObject();
        json.putAll(params);
        json.put("mac", mac);
        String data = AES.encryptBase64(json.toJSONString(), Base64.decode(loginKey));
        onSuccess.apply(data, loginToken, loginKeyData, salt);
    }

    public static String decodeAutoLoginResult(byte[] loginKeyData, String data) {
        try {
            String ret = AES.decryptStringFromBase64(data, loginKeyData);
            LogUtils.log("HTTP", "===================autoLogin data: " + ret);
            return ret;
        } catch (Exception e) {
            Reporter.post("===================登录结果解密失败", e);
            return data;
        }
    }

    private static String decodeLoginResult(byte[] code, String data) {
        try {
            String ret = AES.decryptStringFromBase64(data, code);
            LogUtils.log("HTTP", "login data: " + ret);
            return ret;
        } catch (Exception e) {
            Reporter.post("===================登录结果解密失败", e);
            return data;
        }
    }

    private static void thirdLogin(
            Context ctx, CoreManager coreManager,
            Map<String, String> params,
            Function<Throwable> onError,
            Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        AsyncUtils.doAsync(ctx, t -> {
            Reporter.post("第三方登录失败", t);
            AsyncUtils.runOnUiThread(ctx, c -> {
                onError.apply(t);
            });
        }, executor, c -> {
            String url = coreManager.getConfig().USER_THIRD_LOGIN;
            String salt = createSalt();
            byte[] code = MD5.encrypt(AppConfig.apiKey);
            String mac = MAC.encodeBase64((AppConfig.apiKey + Parameter.joinValues(params) + salt).getBytes(), code);
            JSONObject json = new JSONObject();
            json.putAll(params);
            json.put("mac", mac);
            String data = AES.encryptBase64(json.toJSONString(), code);
            Map<String, String> p = new HashMap<>();
            p.put("data", data);
            p.put("salt", salt);
            login(ctx, url, code, p, t -> {
                Log.i(TAG, "登录失败", t);
                c.uiThread(r -> {
                    onError.apply(t);
                });
            }, result -> {
                c.uiThread(r -> {
                    onSuccess.apply(result);
                });
            });
        });
    }

    public static void secureRegister(
            Context ctx, CoreManager coreManager,
            String thirdToken, String thirdTokenType,
            Map<String, String> params,
            Function<Throwable> onError,
            Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        String tUrl = coreManager.getConfig().USER_REGISTER;
        if (!TextUtils.isEmpty(thirdToken)) {
            params.put("type", thirdTokenType);
            params.put("loginInfo", WXHelper.parseOpenId(thirdToken));
            tUrl = coreManager.getConfig().USER_THIRD_REGISTER;
        }
        final String url = tUrl;
        AsyncUtils.doAsync(ctx, t -> {
            Reporter.post("第三方登录失败", t);
            AsyncUtils.runOnUiThread(ctx, c -> {
                onError.apply(t);
            });
        }, executor, c -> {
            String salt = createSalt();
            byte[] code = MD5.encrypt(AppConfig.apiKey);
            String mac = MAC.encodeBase64((AppConfig.apiKey + Parameter.joinValues(params) + salt).getBytes(), code);
            JSONObject json = new JSONObject();
            json.putAll(params);
            json.put("mac", mac);
            String data = AES.encryptBase64(json.toJSONString(), code);
            Map<String, String> p = new HashMap<>();
            p.put("data", data);
            p.put("salt", salt);
            login(ctx, url, code, p, t -> {
                Log.i(TAG, "登录失败", t);
                c.uiThread(r -> {
                    onError.apply(t);
                });
            }, result -> {
                c.uiThread(r -> {
                    onSuccess.apply(result);
                });
            });
        });
    }

    /**
     * 配置登录加密的参数，
     * <p>
     * 成功失败的回调都是在主线程调用的，
     *
     * @param account 不带区号的手机号，或者其他，手机号输入框里输入的其他登录号，
     * @param onError 确保失败时回调，不能出现既不成功也不失败的情况，因为外面可能有对话框等着关闭，
     */
    public static void secureLogin(
            Context ctx, CoreManager coreManager, String areaCode, String account, String loginPassword,
            Map<String, String> params,
            Function<Throwable> onError,
            Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        secureLogin(ctx, coreManager, areaCode, account, loginPassword, null, null, false, params, onError, onSuccess);
    }

    /**
     * 配置登录加密的参数，
     * <p>
     * 成功失败的回调都是在主线程调用的，
     *
     * @param account 不带区号的手机号，或者其他，手机号输入框里输入的其他登录号，
     * @param onError 确保失败时回调，不能出现既不成功也不失败的情况，因为外面可能有对话框等着关闭，
     */
    public static void secureLogin(
            Context ctx, CoreManager coreManager, String areaCode, String account, String loginPassword,
            String thirdToken, String thirdTokenType, boolean thirdAutoLogin,
            Map<String, String> params,
            Function<Throwable> onError,
            Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        String tUrl = coreManager.getConfig().USER_LOGIN;
        if (!TextUtils.isEmpty(thirdToken)) {
            params.put("type", thirdTokenType);
            if (TextUtils.equals(LoginActivity.THIRD_TYPE_WECHAT, thirdTokenType)) {
                params.put("loginInfo", WXHelper.parseOpenId(thirdToken));
            } else if (TextUtils.equals(LoginActivity.THIRD_TYPE_QQ, thirdTokenType)) {
                params.put("loginInfo", QQHelper.parseOpenId(thirdToken));
            } else {
                throw new IllegalStateException("unknown type: " + thirdTokenType);
            }
            tUrl = coreManager.getConfig().USER_THIRD_BIND_LOGIN;
        }
        if (thirdAutoLogin) {
            thirdLogin(ctx, coreManager,
                    params, onError, onSuccess);
            return;
        }
        final String url = tUrl;
        AsyncUtils.doAsync(ctx, t -> {
            Reporter.post("登录失败", t);
            AsyncUtils.runOnUiThread(ctx, c -> {
                onError.apply(t);
            });
        }, executor, c -> {
            byte[] key = LoginPassword.encode(loginPassword);
            getCode(ctx, coreManager, areaCode, account, key, t -> {
                Log.i(TAG, "获取code失败", t);
                c.uiThread(r -> {
                    onError.apply(t);
                });
            }, (encryptedCode, userId) -> {
                // 到这里说明登录密码正确，
                getRsaPrivateKey(ctx, coreManager, userId, key, t -> {
                    Log.i(TAG, "获取登录私钥失败", t);
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
                            Log.i(TAG, "私钥解密code失败", e);
                            requestPrivateKey(ctx, coreManager, userId, key, onError, this);
                            return;
                        }
                        String loginPasswordMd5 = LoginPassword.md5(key);
                        String salt = createSalt();
                        String mac = MAC.encodeBase64((AppConfig.apiKey + userId + Parameter.joinValues(params) + salt + loginPasswordMd5).getBytes(), code);
                        JSONObject json = new JSONObject();
                        json.putAll(params);
                        json.put("mac", mac);
                        String data = AES.encryptBase64(json.toJSONString(), code);
                        Map<String, String> p = new HashMap<>();
                        p.put("data", data);
                        p.put("userId", userId);
                        p.put("salt", salt);
                        login(ctx, url, code, p, t -> {
                            Log.i(TAG, "登录失败", t);
                            c.uiThread(r -> {
                                onError.apply(t);
                            });
                        }, result -> {
                            c.uiThread(r -> {
                                onSuccess.apply(result);
                            });
                        });
                    }
                });
            });
        });
    }

    public static void smsLogin(Context ctx, CoreManager coreManager, String smsCode, String areaCode, String telephone, Map<String, String> params, Function<Throwable> onError, Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        byte[] smsKey = MD5.encrypt(smsCode);
        String salt = createSalt();
        String mac = MAC.encodeBase64((AppConfig.apiKey + areaCode + telephone + Parameter.joinValues(params) + salt).getBytes(), smsKey);
        params.put("mac", mac);
        String data = AES.encryptBase64(JSON.toJSONString(params), smsKey);
        params = new HashMap<>();
        params.put("salt", salt);
        params.put("data", data);
        params.put("deviceId", DEVICE_ID);
        params.put("areaCode", areaCode);
        params.put("account", telephone);
        HttpUtils.get().url(coreManager.getConfig().USER_SMS_LOGIN)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<EncryptedData>(EncryptedData.class) {
                    @Override
                    public void onResponse(ObjectResult<EncryptedData> result) {
                        ObjectResult<LoginRegisterResult> objectResult = new ObjectResult<>();
                        objectResult.setCurrentTime(result.getCurrentTime());
                        objectResult.setResultCode(result.getResultCode());
                        objectResult.setResultMsg(result.getResultMsg());
                        if (Result.checkSuccess(ctx, result, false) && result.getData() != null && result.getData().getData() != null) {
                            String realData = LoginSecureHelper.decodeLoginResult(smsKey, result.getData().getData());
                            if (realData != null) {
                                LoginRegisterResult realResult = JSON.parseObject(realData, LoginRegisterResult.class);
                                objectResult.setData(realResult);
                            }
                        }
                        onSuccess.apply(objectResult);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });
    }

    private static void login(Context ctx, String url, byte[] code, Map<String, String> p, Function<Throwable> onError, Function<ObjectResult<LoginRegisterResult>> onSuccess) {
        p.put("deviceId", DEVICE_ID);
        HttpUtils.get().url(url)
                .params(p)
                .build(true, true)
                .executeSync(new BaseCallback<EncryptedData>(EncryptedData.class, false) {
                    @Override
                    public void onResponse(ObjectResult<EncryptedData> result) {
                        ObjectResult<LoginRegisterResult> objectResult = new ObjectResult<>();
                        objectResult.setCurrentTime(result.getCurrentTime());
                        objectResult.setResultCode(result.getResultCode());
                        objectResult.setResultMsg(result.getResultMsg());
                        if (Result.checkSuccess(ctx, result, false) && result.getData() != null && result.getData().getData() != null) {
                            String realData = LoginSecureHelper.decodeLoginResult(code, result.getData().getData());
                            if (realData != null) {
                                LoginRegisterResult realResult = JSON.parseObject(realData, LoginRegisterResult.class);
                                objectResult.setData(realResult);
                            }
                        }
                        onSuccess.apply(objectResult);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onError.apply(e);
                    }
                });
    }

    @NonNull
    private static String createSalt() {
        return String.valueOf(System.currentTimeMillis());
    }

    @WorkerThread
    private static void getCode(Context ctx, CoreManager coreManager, String areaCode, String account, byte[] key, Function<Throwable> onError, Function2<String, String> onSuccess) {
        String loginPasswordMd5 = LoginPassword.md5(key);
        String salt = createSalt();
        String mac = MAC.encodeBase64((AppConfig.apiKey + areaCode + account + salt).getBytes(), loginPasswordMd5);
        final Map<String, String> params = new HashMap<>();
        params.put("areaCode", areaCode);
        params.put("account", account);
        params.put("mac", mac);
        params.put("salt", salt);
        params.put("deviceId", DEVICE_ID);

        HttpUtils.post().url(coreManager.getConfig().LOGIN_SECURE_GET_CODE)
                .params(params)
                .build(true, true)
                .executeSync(new BaseCallback<LoginCode>(LoginCode.class, false) {

                    @Override
                    public void onResponse(ObjectResult<LoginCode> result) {
                        if (Result.checkSuccess(ctx, result, false) && result.getData() != null) {
                            String userId = result.getData().getUserId();
                            if (result.getData() == null || TextUtils.isEmpty(result.getData().getCode())) {
                                // 服务器没有公钥，创建一对上传后从新调用getCode,
                                makeRsaKeyPair(ctx, coreManager, userId, key, onError, privateKey -> {
                                    getCode(ctx, coreManager, areaCode, account, key, onError, onSuccess);
                                });
                            } else {
                                onSuccess.apply(result.getData().getCode(), userId);
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

    private static void getRsaPrivateKey(Context ctx, CoreManager coreManager, String userId, byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        // 本地不保存密码登录私钥，每次都通过接口获取，
        requestPrivateKey(ctx, coreManager, userId, key, onError, onSuccess);
    }

    @WorkerThread
    private static void requestPrivateKey(Context ctx, CoreManager coreManager, String userId, byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        String loginPasswordMd5 = LoginPassword.md5(key);
        String salt = createSalt();
        String mac = MAC.encodeBase64((AppConfig.apiKey + userId + salt).getBytes(), loginPasswordMd5);
        final Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("mac", mac);
        params.put("salt", salt);

        HttpUtils.post().url(coreManager.getConfig().LOGIN_SECURE_GET_PRIVATE_KEY)
                .params(params)
                .build(true, true)
                .executeSync(new BaseCallback<PayPrivateKey>(PayPrivateKey.class, false) {

                    @Override
                    public void onResponse(ObjectResult<PayPrivateKey> result) {
                        if (Result.checkSuccess(ctx, result, false)) {
                            String encryptedPrivateKey;
                            if (result.getData() != null && !TextUtils.isEmpty(encryptedPrivateKey = result.getData().getPrivateKey())) {
                                byte[] privateKey;
                                try {
                                    privateKey = AES.decryptFromBase64(encryptedPrivateKey, key);
                                } catch (Exception e) {
                                    // 解密失败，登录密码错误，
                                    onError.apply(new IllegalArgumentException(ctx.getString(R.string.tip_wrong_pay_password)));
                                    return;
                                }
                                onSuccess.apply(privateKey);
                            } else {
                                // 走到这里说明服务器返回了公钥加密的code, 不可能没有私钥，
                                onError.apply(new IllegalStateException(ctx.getString(R.string.tip_server_error)));
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

    @WorkerThread
    private static void makeRsaKeyPair(Context ctx, CoreManager coreManager, String userId,
                                       byte[] key, Function<Throwable> onError, Function<byte[]> onSuccess) {
        String salt = createSalt();
        RSA.RsaKeyPair rsaKeyPair = RSA.genKeyPair();
        String encryptedPrivateKeyBase64 = AES.encryptBase64(rsaKeyPair.getPrivateKey(), key);
        String publicKeyBase64 = rsaKeyPair.getPublicKeyBase64();
        String macKey = LoginPassword.md5(key);
        String macContent = AppConfig.apiKey + userId + encryptedPrivateKeyBase64 + publicKeyBase64 + salt;
        String mac = MAC.encodeBase64(macContent.getBytes(), macKey);
        final Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("publicKey", publicKeyBase64);
        params.put("privateKey", encryptedPrivateKeyBase64);
        params.put("salt", salt);
        params.put("mac", mac);

        HttpUtils.post().url(coreManager.getConfig().LOGIN_SECURE_UPLOAD_KEY)
                .params(params)
                .build(true, true)
                .executeSync(new BaseCallback<Void>(Void.class, false) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(ctx, result, false)) {
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

    public interface Function4<T, R, E, W> {
        void apply(T t, R r, E e, W w);
    }

    public static class LoginTokenOvertimeException extends IllegalStateException {
        public LoginTokenOvertimeException(String s) {
            super(s);
        }
    }
}
