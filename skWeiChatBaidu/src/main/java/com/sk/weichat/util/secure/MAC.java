package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * MAC算法 (Message Authentication Codes) 带秘密密钥的Hash函数
 * 具体采用HmacMD5，
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MAC {
    public static byte[] encode(String data, String key) {
        return encode(data.getBytes(), key.getBytes());
    }

    public static byte[] encode(String data, byte[] key) {
        return encode(data.getBytes(), key);
    }

    public static byte[] encode(byte[] data, String key) {
        return encode(data, key.getBytes());
    }

    public static byte[] encode(byte[] data, byte[] key) {
        try {
            // 还原密钥
            SecretKey secretKey = new SecretKeySpec(key, "HmacMD5");
            // 实例化Mac
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            //初始化mac
            mac.init(secretKey);
            //执行消息摘要
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeBase64(String data, String key) {
        return encodeBase64(data.getBytes(), key.getBytes());
    }

    public static String encodeBase64(String data, byte[] key) {
        return encodeBase64(data.getBytes(), key);
    }

    public static String encodeBase64(byte[] data, String key) {
        return encodeBase64(data, key.getBytes());
    }

    public static String encodeBase64(byte[] data, byte[] key) {
        return Base64.encode(encode(data, key));
    }
}
