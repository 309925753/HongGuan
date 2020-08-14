package com.xuan.xuanhttplibrary.okhttp.callback;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.util.LogUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class AbstractCallback<T> implements Callback {
    // 切到主线程再回调子类方法，
    protected boolean mainThreadCallback;
    private Handler mDelivery;

    public AbstractCallback() {
        this(true);
    }

    /**
     * @param mainThreadCallback true表示切到主线程再回调子类方法，
     */
    public AbstractCallback(boolean mainThreadCallback) {
        this.mainThreadCallback = mainThreadCallback;
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public abstract void onResponse(T result);

    public abstract void onError(Call call, Exception e);

    @Override
    public void onFailure(Call call, IOException e) {
        Log.i(HttpUtils.TAG, "服务器请求失败", e);
        if (e instanceof ConnectException) {
            Log.i(HttpUtils.TAG, "ConnectException", e);
        }
        if (e instanceof SocketTimeoutException) {
            Log.i(HttpUtils.TAG, "SocketTimeoutException", e);
        }
        errorData(call, e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.code() == 200) {
            try {
                String body = response.body().string();
                Log.i(HttpUtils.TAG, "服务器数据包：" + body);
                successData(parseResponse(call, body));
            } catch (Exception e) {
                LogUtils.log(response);
                Reporter.post("json解析失败, ", e);
                Log.i(HttpUtils.TAG, "数据解析异常:" + e.getMessage());
                errorData(call, new Exception("数据解析异常"));
            }
        } else {
            Log.i(HttpUtils.TAG, "服务器请求异常");
            errorData(call, new Exception("服务器请求异常"));
        }
    }

    @NonNull
    abstract T parseResponse(Call call, String body);

    protected void successData(final T t) {
        if (mainThreadCallback) {
            mDelivery.post(() -> callOnResponse(t));
        } else {
            callOnResponse(t);
        }
    }

    private void callOnResponse(T t) {
        if (t instanceof Result) {
            int resultCode = ((Result) t).getResultCode();
            if (resultCode == Result.CODE_TOKEN_ERROR) {
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_OVERDUE;
                LoginHelper.broadcastLogout(MyApplication.getContext());
            }
        }
        onResponse(t);
    }

    protected void errorData(final Call call, final Exception e) {
        if (mainThreadCallback) {
            mDelivery.post(() -> onError(call, e));
        } else {
            onError(call, e);
        }
    }
}
