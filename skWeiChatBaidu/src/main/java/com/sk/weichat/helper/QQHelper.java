package com.sk.weichat.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.QQLoginResult;
import com.sk.weichat.bean.QQUserInfo;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.event.EventUpdateBandQqAccount;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.ToastUtil;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class QQHelper {
    public static final boolean ENABLE = true;
    private static final String TAG = "QQHelper";
    private static final Object lock = new Object();
    private static Tencent mTencent;
    private static IUiListener loginListener;
    private static IUiListener bandListener;

    private static Tencent getTencent(Context ctx) {
        if (mTencent != null) {
            return mTencent;
        }
        synchronized (lock) {
            if (mTencent != null) {
                return mTencent;
            }
            mTencent = Tencent.createInstance(BuildConfig.QQ_APP_ID, ctx);
        }
        return mTencent;
    }

    public static String parseOpenId(String json) {
        QQLoginResult result = JSON.parseObject(json, QQLoginResult.class);
        return result.getOpenid();
    }

    public static boolean qqInstalled(Context ctx) {
        return getTencent(ctx).isQQInstalled(ctx);
    }

    public static IUiListener getLoginListener(Context ctx) {
        if (loginListener != null) {
            return loginListener;
        }
        synchronized (lock) {
            if (loginListener != null) {
                return loginListener;
            }
            loginListener = new IUiListener() {
                @Override
                public void onComplete(Object response) {
                    Log.d(TAG, "onComplete() called with: response = [" + response + "]");
                    if (null == response) {
                        ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                        return;
                    }
                    JSONObject jsonResponse = (JSONObject) response;
                    if (jsonResponse.length() == 0) {
                        ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                        return;
                    }
                    QQLoginResult loginResult = JSON.parseObject(response.toString(), QQLoginResult.class);
                    mTencent.setAccessToken(loginResult.getAccessToken(), String.valueOf(loginResult.getExpiresIn()));
                    mTencent.setOpenId(loginResult.getOpenid());
                    LoginActivity.bindThird(ctx, loginResult);
                }

                @Override
                public void onError(UiError uiError) {
                    Log.d(TAG, "onError() called with: uiError = [" + uiError + "]");
                    ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "onCancel() called");
                    ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                }
            };
        }
        return loginListener;
    }

    public static IUiListener getBandListener(Context ctx) {
        if (bandListener != null) {
            return bandListener;
        }
        synchronized (lock) {
            if (bandListener != null) {
                return bandListener;
            }
            bandListener = new IUiListener() {
                @Override
                public void onComplete(Object response) {
                    Log.d(TAG, "onComplete() called with: response = [" + response + "]");
                    if (null == response) {
                        ToastUtil.showToast(ctx, R.string.tip_bind_qq_failed);
                        return;
                    }
                    JSONObject jsonResponse = (JSONObject) response;
                    if (jsonResponse.length() == 0) {
                        ToastUtil.showToast(ctx, R.string.tip_bind_qq_failed);
                        return;
                    }
                    QQLoginResult loginResult = JSON.parseObject(response.toString(), QQLoginResult.class);
                    mTencent.setAccessToken(loginResult.getAccessToken(), String.valueOf(loginResult.getExpiresIn()));
                    mTencent.setOpenId(loginResult.getOpenid());
                    bandOpenId(ctx, loginResult.getOpenid());
                }

                @Override
                public void onError(UiError uiError) {
                    Log.d(TAG, "onError() called with: uiError = [" + uiError + "]");
                    ToastUtil.showToast(ctx, R.string.tip_bind_qq_failed);
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "onCancel() called");
                    ToastUtil.showToast(ctx, R.string.tip_bind_qq_failed);
                }
            };
        }
        return bandListener;
    }

    public static void qqLogin(Activity ctx) {
        getTencent(ctx).login(ctx, "get_simple_userinfo", getLoginListener(ctx));
    }

    public static void qqBand(Activity ctx) {
        getTencent(ctx).login(ctx, "get_simple_userinfo", getBandListener(ctx));
    }

    public static void requestUserInfo(Context ctx, String thirdToken, OnCompleteListener onComplete) {
        UserInfo userInfo = new UserInfo(ctx, getTencent(ctx).getQQToken());
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object response) {
                Log.d(TAG, "onComplete() called with: response = [" + response + "]");
                if (null == response) {
                    ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                    return;
                }
                JSONObject jsonResponse = (JSONObject) response;
                if (jsonResponse.length() == 0) {
                    ToastUtil.showToast(ctx, R.string.tip_auth_login_failed);
                    return;
                }
                QQUserInfo result = JSON.parseObject(jsonResponse.toString(), QQUserInfo.class);
                onComplete.onComplete(result);
            }

            @Override
            public void onError(UiError uiError) {
                Log.d(TAG, "onError() called with: uiError = [" + uiError + "]");
                LogUtils.log(TAG, "获取QQ个人资料失败" + thirdToken);
                Reporter.post("获取QQ个人资料失败，" + uiError.toString());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel() called");
                LogUtils.log(TAG, "用户取消了操作" + thirdToken);
            }
        });
    }


    private static void bandOpenId(Context ctx, String openId) {
        CoreManager coreManager = CoreManager.getInstance(ctx);
        User user = coreManager.getSelf();
        DialogHelper.showDefaulteMessageProgressDialog(ctx);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("telephone", user.getTelephone());
        params.put("type", LoginActivity.THIRD_TYPE_QQ);
        params.put("loginInfo", openId);
        params.put("password", user.getPassword());

        HttpUtils.get().url(coreManager.getConfig().USER_THIRD_BIND)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ctx, result)) {
                            Toast.makeText(ctx, ctx.getString(R.string.tip_bind_qq_success), Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new EventUpdateBandQqAccount("result", "ok"));
                        } else {
                            EventBus.getDefault().post(new EventUpdateBandQqAccount("result", "err"));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    public interface OnCompleteListener {
        void onComplete(QQUserInfo userInfo);
    }
}
