package com.sk.weichat.util.link;

import com.sk.weichat.util.HttpUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpTextViewTest {

    @Test
    public void setUrlText() {
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 10000; i++) {
            sb.append('1');
        }
        String text = sb.toString();
        Pattern p = Pattern.compile(HttpUtil.REGEX_URL);
        long start = System.currentTimeMillis();
        Matcher m = p.matcher(text);
        while (m.find()) {
            System.out.println(m.group());
        }
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("time: " + time);
    }

    @Test
    public void urlTest() {
        Pattern p = Pattern.compile(HttpUtil.REGEX_URL);
        List<String> matchList = Arrays.asList(
                "http://39.156.69.79",
                "https://libs.baidu.com/jquery/1.7.2/jquery.min.js",
                "https://developer.mozilla.org/en-US/search?q=URL",
                "http://39.156.69.79:8092/liveRoom/giftlist?pageSize=50&pageIndex=0&language=zh&access_token=e9476c040cca4747b2858c4459b73b88&salt=1570852145044&secret=Oip0lWoqer94E8SIF2fo7w%3D%3D",
                "https://www.baidu.com/",
                "https://www.baidu.com/s?wd=%E4%B8%AD%E6%96%87"
        );
        List<String> noMatchList = Arrays.asList(
                "39.156.69.79",
                "11111111111111"
        );
        for (String url : matchList) {
            Matcher m = p.matcher(url);
            assertTrue(m.find());
            assertEquals(url, m.group());
        }
        for (String url : noMatchList) {
            Matcher m = p.matcher(url);
            assertFalse(m.find());
        }
    }
}