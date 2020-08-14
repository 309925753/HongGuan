package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AESTest {
    private String content = "10005154";
    private String encrypted = "v8dUhK9k1+uBnFJjlNtcGg==";
    private byte[] key = MD5.encrypt("123456");

    @Test
    public void encryptBase64() {
        assertEquals(encrypted, AES.encryptBase64(content, key));
    }

    @Test
    public void decryptStringFromBase64() {
        assertEquals(content, AES.decryptStringFromBase64(encrypted, key));
    }

    @Test
    public void test() {
        assertEquals("7b0ec6dfb48e8c79e53e5a3f55df62cb", MD5.encryptHex(Base64.decode(encrypted)));
    }
}