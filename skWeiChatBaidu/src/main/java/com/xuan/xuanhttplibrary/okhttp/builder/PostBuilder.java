package com.xuan.xuanhttplibrary.okhttp.builder;

import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.Reporter;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import java.net.URLEncoder;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * @author Administrator
 * @time 2017/3/30 0:11
 * @des ${TODO}
 */

public class PostBuilder extends BaseBuilder {

    @Override
    public PostBuilder url(String url) {
        if (!TextUtils.isEmpty(url)) {
            this.url = url;
        }
        return this;
    }

    @Override
    public PostBuilder tag(Object tag) {
        return this;
    }

    @Override
    public BaseCall abstractBuild() {
        FormBody.Builder builder = appenParams(new FormBody.Builder());

        build = new Request.Builder()
                .header("User-Agent", getUserAgent())
                .url(url).post(builder.build())
                .build();

        return new BaseCall();
    }

    private FormBody.Builder appenParams(FormBody.Builder builder) {
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        if (!params.isEmpty()) {
            sb.append("?");
            for (String key : params.keySet()) {
                String v = params.get(key);
                if (v == null) {
                    continue;
                }
                try {
                    // url安全，部分字符不能直接放进url, 要改成百分号开头%的，
                    v = URLEncoder.encode(v, "UTF-8");
                } catch (Exception e) {
                    // 不可到达，UTF-8不可能不支持，
                    Reporter.unreachable(e);
                }
                // 不能用FormBody封装的urlEncode, 服务器收到的加号还是空格，原因不明，
                builder.addEncoded(key, v);
                sb.append(key).append("=").append(v).append("&");
            }
            sb = sb.deleteCharAt(sb.length() - 1); // 去掉后面的&
        }

        Log.i(HttpUtils.TAG, "网络请求参数：" + sb.toString());
        return builder;
    }

    @Override
    public PostBuilder params(String k, String v) {
        params.put(k, v);
        return this;
    }

    public PostBuilder params(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

}
