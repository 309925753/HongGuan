package com.sk.weichat.fragment;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DiscoverFragmentTest {

    /**
     * 发送大量朋友圈评论，
     */
    @Test
    public void send() throws Exception {
        int success = 0;
        int failed = 0;
        for (int i = 0; i < 500; i++) {
            String content = String.valueOf(i);
            String url = "http://47.101.137.26:8092/b/circle/msg/comment/add?" +
                    "access_token=eafce76a9b2f4788807a66c1885df033&messageId=5cc6dcd903986872ef4bf850&body=" + content;
            String body = new OkHttpClient.Builder()
                    .build()
                    .newCall(
                            new Request.Builder()
                                    .url(url)
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

}