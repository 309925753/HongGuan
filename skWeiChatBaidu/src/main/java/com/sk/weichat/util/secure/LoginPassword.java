package com.sk.weichat.util.secure;

/**
 * 登录密码
 * 使用时有两种形式的登录密码，
 * 1.	保存在服务器的登录密码加密后的md5(下称登录密码密文),
 * 具体算法是用登录密码计算md5得到16字节数据作为aes加密的key和被加密内容，
 * 得到密文数据计算md5得到16字节数据，16进制编码得到32位字符串，完毕，
 * 即hex(md5(aes(md5(password), md5(password)))),
 * 如密码为123456，计算出密码密文为7ba1bd982b33ac731c2c3bca90e77be9
 * 2.	不传输不保存只作为加解密key临时使用的只有用户本人知道的登录密码（下称登录密码明文），无法通过服务器保存的的登录密码密文转换得到，
 * 即md5(password),
 * 具体算法是用登录密码计算md5得到16字节数据，完毕，
 */
public class LoginPassword {
    /**
     * 登录密码明文，不传输，用于加密
     *
     * @param payPassword 用户输入的密码字符串，
     * @return 用于加密的16字节数据，
     */
    public static byte[] encode(String payPassword) {
        return MD5.encrypt(payPassword);
    }

    /**
     * 登录密码密文，在服务器保存的登录密码密文字符串,
     *
     * @param key {@link LoginPassword#encode(String)}不可打印的登录密码,
     */
    public static String md5(byte[] key) {
        return MD5.encryptHex(AES.encrypt(key, key));
    }

    /**
     * 登录密码密文，在服务器保存的登录密码密文字符串,
     *
     * @param payPassword 用户输入的密码字符串，
     */
    public static String encodeMd5(String payPassword) {
        return md5(encode(payPassword));
    }

    /**
     * 通过老算法的密码计算新算法的密码，
     *
     * @param oldPassword 服务器数据库保存的老算法的登录密码，
     * @return 新算法下的登录密码密文，
     */
    public static String encodeFromOldPassword(String oldPassword) {
        return md5(HEX.decode(oldPassword));
    }
}
