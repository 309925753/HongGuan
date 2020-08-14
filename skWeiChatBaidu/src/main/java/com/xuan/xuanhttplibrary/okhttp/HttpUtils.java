package com.xuan.xuanhttplibrary.okhttp;

import com.xuan.xuanhttplibrary.okhttp.builder.GetBuilder;
import com.xuan.xuanhttplibrary.okhttp.builder.PostBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author liuxan
 * @time 2017/3/29 23:36
 * @des
 */

public class HttpUtils {

    public static String TAG = "HTTP";

    private static HttpUtils instance = new HttpUtils();
    private OkHttpClient mOkHttpClient;

    private HttpUtils() {
    }

    public static HttpUtils getInstance() {
        return instance;
    }

    public static PostBuilder post() {
        return new PostBuilder();
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    // .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }
}
