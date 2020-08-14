package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MD5Test {
    private static final String content = "123456";
    private static final String encrypt = "e10adc3949ba59abbe56e057f20f883e";
    private static final String encryptBase64 = "4QrcOUm6Wau+VuBX8g+IPg==";

    @Test
    public void encrypt() {
        assertEquals(encrypt, (MD5.encryptHex(content.getBytes())));
        assertEquals(encryptBase64, Base64.encode(MD5.encrypt(content.getBytes())));
    }
}