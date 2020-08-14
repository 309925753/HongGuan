package com.sk.weichat.xmpp.helloDemon;

import android.content.Context;
import android.util.Log;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.util.LogUtils;

/**
 * Created by Administrator on 2018/1/11 0011.
 */

public class HuaweiClient implements HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener {
    private static final String TAG = "Huawei Client";
    private HuaweiApiClient mClient;
    private Context mContext;

    public HuaweiClient(Context context) {
        this.mContext = context;
    }

    public void clientConnect() {
        // 创建华为移动服务client实例用以使用华为push服务
        // 需要指定api为HuaweiPush.PUSH_API
        // 连接回调以及连接失败监听
        mClient = new HuaweiApiClient.Builder(mContext)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // 建议在onCreate的时候连接华为移动服务
        // 业务可以根据自己业务的形态来确定client的连接和断开的时机，但是确保connect和disconnect必须成对出现
        mClient.connect();
    }

    @Override
    public void onConnected() {
        // 华为移动服务client连接成功，在这边处理业务自己的事件
        Log.i(TAG, "HuaweiApiClient 连接成功");
        getTokenAsyn();// 连接成功后在去获取Token
    }

    @Override
    public void onConnectionSuspended(int i) {
        // HuaweiApiClient异常断开连接
        MainActivity activity = (MainActivity) mContext;
        if (!activity.isDestroyed() || !activity.isFinishing()) {// 主界面未销毁，client重新连接
            mClient.connect();
        }
        Log.i(TAG, "HuaweiApiClient 连接断开");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "HuaweiApiClient连接失败，错误码：" + connectionResult.getErrorCode());
    }

    /**
     * 申请token会触发启动Push服务，token申请成功后，结果会通过广播的方式返回token给应用。
     * 调用getToken方法发起请求，返回申请token的PendingResult对象，根据对象可以获取接口调用是否成功，但是不直接返回token 结果。
     */
    public void getTokenAsyn() {
        if (!mClient.isConnected()) {
            Log.i(TAG, "获取token失败，原因：HuaweiApiClient未连接");
            mClient.connect();
            return;
        }

        Log.i(TAG, "异步接口获取push token");
        PendingResult<TokenResult> tokenResult = HuaweiPush.HuaweiPushApi.getToken(mClient);
        tokenResult.setResultCallback(new ResultCallback<TokenResult>() {
            @Override
            public void onResult(TokenResult result) {// 可至HuaweiPushRevicer内的onToken回调内查看Token结果，上传至服务器
                Log.e(TAG, "onResult() called with: result = [" + result + "]");
                LogUtils.log(result);
            }
        });
    }
}
