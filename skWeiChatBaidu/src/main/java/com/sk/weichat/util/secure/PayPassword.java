package com.sk.weichat.util.secure;

/**
 * 所有参与的6字节支付密码明文都通过位运算转换成128位16字节的不可打印字节数组，
 * 要求是把6字节可打印字符串转成16字节不可打印字节数组，
 */
public class PayPassword {
    public static byte[] encode(String payPassword) {
        return MD5.encrypt(payPassword);
    }

    /**
     * 在服务器保存的支付密码md5,
     *
     * @param userId 用户自己的userId,
     * @param key    {@link PayPassword#encode(String)}不可打印的支付密码,
     */
    public static String md5(String userId, byte[] key) {
        return MD5.encryptHex(AES.encrypt(userId.getBytes(), key));
    }

    public static String encodeMd5(String userId, String payPassword) {
        return md5(userId, encode(payPassword));
    }

    /**
     * 通过老算法的密码计算新算法的密码，
     *
     * @param oldPassword 服务器数据库保存的老算法的支付密码，
     * @return 新算法下的支付密码密文，
     */
    public static String encodeFromOldPassword(String userId, String oldPassword) {
        return md5(userId, HEX.decode(oldPassword));
    }
}
