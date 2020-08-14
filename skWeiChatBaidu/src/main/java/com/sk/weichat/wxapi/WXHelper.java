package com.sk.weichat.wxapi;

import androidx.annotation.WorkerThread;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.bean.WXUploadResult;
import com.sk.weichat.bean.WXUserInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WXHelper {
    private static final String SNS_URL = "https://api.weixin.qq.com/sns/userinfo";

    private WXHelper() {
    }

    public static WXUserInfo requestUserInfo(String json) throws IOException {
        return requestUserInfo(JSON.parseObject(json, WXUploadResult.class));
    }

    @WorkerThread
    private static WXUserInfo requestUserInfo(WXUploadResult request) throws IOException {
        HttpUrl url = HttpUrl.parse(SNS_URL).newBuilder()
                .addQueryParameter("access_token", request.getAccess_token())
                .addQueryParameter("openid", request.getOpenid())
                .build();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Call call = client.newCall(new Request.Builder()
                .url(url)
                .build());
        Response response = call.execute();
        String resultString = response.body().string();
        return JSON.parseObject(resultString, WXUserInfo.class);
    }

    public static String parseOpenId(String json) {
        WXUploadResult wxUploadResult = JSON.parseObject(json, WXUploadResult.class);
        return wxUploadResult.getOpenid();
    }
}
