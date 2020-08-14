package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AES {
    private static byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

    /**
     * AES加密字符串
     *
     * @param content  需要被加密的字符串
     * @param password 加密需要的密码
     * @return 密文
     */
    public static byte[] encrypt(byte[] content, byte[] password) {
        try {
            if (password.length != 16) {
                password = Arrays.copyOfRange(password, 0, 16);
            }
            KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));// 初始化为加密模式的密码器
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptBase64(byte[] content, byte[] password) {
        return Base64.encode(encrypt(content, password));
    }

    public static String encryptBase64(String content, byte[] password) {
        return Base64.encode(encrypt(content.getBytes(), password));
    }

    /**
     * 解密AES加密过的字符串
     *
     * @param content  AES加密过过的内容
     * @param password 加密时的密码
     * @return 明文
     */
    public static byte[] decrypt(byte[] content, byte[] password) {
        try {
            if (password.length != 16) {
                password = Arrays.copyOfRange(password, 0, 16);
            }
            KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));// 初始化为解密模式的密码器
            return cipher.doFinal(content); // 明文
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptFromBase64(String content, byte[] password) {
        return decrypt(Base64.decode(content), password);
    }

    public static String decryptString(byte[] content, byte[] password) {
        return new String(decrypt(content, password));
    }

    public static String decryptStringFromBase64(String content, byte[] password) {
        return new String(decrypt(Base64.decode(content), password));
    }

    public static String decryptStringFromBase64II(String content, byte[] password) {
        return Base64.encode(decrypt(Base64.decode(content), password));
    }
}
