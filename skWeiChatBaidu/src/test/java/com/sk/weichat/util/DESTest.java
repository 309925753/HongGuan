package com.sk.weichat.util;

import org.junit.Test;

import okio.ByteString;

import static org.junit.Assert.assertEquals;

public class DESTest {
    private String content = "经济   ，   看看书  ， 看看书！";
    // TODO: 使用的base64库可能编码出空格，导致其他平台可能base64解码失败，
    private String encrypted = "WyyPfaBKM+/WsEMO0RE3QaEW8izfv8E8iwAJqNhMy8lfsC0Xnm4Bj9CeKA9m /vQI";
    private String apiKey = "";
    private String timeSend = "1556249183";
    private String messageId = "8d45b3033ec446ac8ab1df65d20b3e74";
    private String key = Md5Util.toMD5(apiKey + timeSend + messageId);

    @Test
    public void encryptDES() throws Exception {
        assertEquals(encrypted, DES.encryptDES(content, key));
    }

    @Test
    public void decryptDES() throws Exception {
        assertEquals(content, DES.decryptDES(encrypted, key));
    }

    @Test
    public void base64() throws Exception {
        // okio解码base64同样兼容空格，
        // Linux命令base64不兼容空格，
        ByteString byteString = ByteString.decodeBase64(encrypted);
        assertEquals("c0477c794e49276de6f8c698ddf6f104", byteString.md5().hex());
    }
}