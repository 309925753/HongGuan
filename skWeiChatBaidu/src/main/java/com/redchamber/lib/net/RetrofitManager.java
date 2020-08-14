package com.redchamber.lib.net;

import com.redchamber.lib.net.http.HttpConfig;
import com.redchamber.lib.net.http.HttpsUtils;
import com.redchamber.lib.utils.PreferenceUtils;
import com.sk.weichat.AppConfig;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static Retrofit mRetrofit;
    private static OkHttpClient.Builder okhttpClientBuilder;

    private static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            OkHttpClient okHttpClient = buildOkHttpClient(okhttpClientBuilder);
            mRetrofit = buildRetrofit(okHttpClient);
        }
        return mRetrofit;
    }

    private static Retrofit buildRetrofit(OkHttpClient okHttpClient) {
        return mRetrofit = new Retrofit.Builder()
                .baseUrl(HttpConfig.BASE_URL)
//                .baseUrl(AppConfig.CONFIG_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static void setOkhttpClientBuilder(OkHttpClient.Builder okhttpClientBuilder) {
        RetrofitManager.okhttpClientBuilder = okhttpClientBuilder;
    }

    private static OkHttpClient buildOkHttpClient(OkHttpClient.Builder builder) {
        try {
            if (builder == null) {
                builder = new OkHttpClient.Builder();
            }
//            //如果不是在正式包，添加拦截 打印响应json
//            builder.addInterceptor(new CommonRequestInterceptor());
//            if (HttpConfig.isDebug) {
//                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(HttpConfig.HttpLogTAG);
//                httpLoggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
//                httpLoggingInterceptor.setColorLevel(Level.INFO);
//                builder.addNetworkInterceptor(httpLoggingInterceptor);
//            }
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            builder.connectTimeout(HttpConfig.connectTimeout, TimeUnit.SECONDS);
            builder.readTimeout(HttpConfig.readTimeout, TimeUnit.SECONDS);
            builder.writeTimeout(HttpConfig.writeTimeout, TimeUnit.SECONDS);
            builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
            builder.addInterceptor(getAuth());
            //设置缓存
            File httpCacheDirectory = new File(HttpConfig.URL_CACHE, "red_chamber_cache");
            if (!httpCacheDirectory.exists()) {
                httpCacheDirectory.mkdirs();
            }
            builder.cache(new Cache(httpCacheDirectory, HttpConfig.MAX_MEMORY_SIZE));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return builder.build();
        }
    }

    public static <T> T getApiService(Class<T> clazz) {
        mRetrofit = getRetrofit();
        return mRetrofit.create(clazz);
    }

    /**
     * 通用鉴权方法
     */
    //FIXME ....
    private static Interceptor getAuth() {
        return chain -> {
            Request original = chain.request();
            long timestamp = System.currentTimeMillis();
            Request request = original.newBuilder()
                    .header("deviceId", "android")
                    .header("salt", String.valueOf(timestamp))
                    .header("access_token", PreferenceUtils.getAccessToken())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        };
    }

}
