package com.sk.weichat.api;

import com.sk.weichat.AppConfig;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.secure.MAC;
import com.sk.weichat.util.secure.Parameter;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiTest {
    private String userId = "10017133";
    private String accessToken = "ef2586e5ea884e49a3845ef4b5addd15";
    private String httpKey = "OUXaujnKy/JoROqDkVPOMg==";

    @Test
    public void testFriendList() throws Exception {
        test("http://test.shiku.co/friends/attention/list", new HashMap<>(), 1);
    }

    public void test(String apiUrl, Map<String, String> params, int times) throws Exception {
        int success = 0;
        int failed = 0;
        for (int i = 0; i < times; i++) {
            params = build(params);
            HttpUrl.Builder builder = HttpUrl.parse(apiUrl).newBuilder();
            params.forEach(builder::addQueryParameter);
            String body = new OkHttpClient.Builder()
                    .build()
                    .newCall(
                            new Request.Builder()
                                    .url(builder.build())
                                    .get()
                                    .build()
                    )
                    .execute()
                    .body().string();
            if (body.contains("\"resultCode\":1")) {
                success++;
            } else {
                failed++;
            }
            System.out.println(body);
            TimeUnit.MILLISECONDS.sleep(10);
        }
        System.out.println("成功次数：" + success);
        System.out.println("失败次数：" + failed);
    }

    public Map<String, String> build(Map<String, String> params) {
        params.put("language", "zh");
        String salt = params.remove("salt");
        if (salt == null) {
            salt = String.valueOf(System.currentTimeMillis());
        }
        params.remove("access_token");
        String macContent = AppConfig.apiKey + userId + accessToken + Parameter.joinValues(params) + salt;
        String mac = MAC.encodeBase64(macContent.getBytes(), Base64.decode(httpKey));
        params.put("access_token", accessToken);
        params.put("salt", salt);
        params.put("secret", mac);
        return params;
    }
}
