package com.sk.weichat.util.secure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MACTest {
    private String content = "hello";
    private String encrypted = "nGmdevc6SSR6I5yw3S+BOQ==";
    private String key = "123456";

    @Test
    public void encode() throws Exception {
        assertEquals(encrypted, MAC.encodeBase64(content, key));
    }
}