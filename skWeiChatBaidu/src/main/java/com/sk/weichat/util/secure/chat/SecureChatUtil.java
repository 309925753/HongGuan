package com.sk.weichat.util.secure.chat;

import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.secure.MD5;

public class SecureChatUtil {
    /**
     * --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * aes encrypt  dh/rsa privateKey upload Serve
     * ase decrypt encrypted dh/rsa  privateKey from Serve.
     * key is login password
     */
    /**
     * 明文密码加密dh私钥-aes
     */
    public static String aesEncryptDHPrivateKey(String key, String value) {
        return "";
    }

    /**
     * 明文密码加密rsa私钥-aes
     */
    public static String aesEncryptRSAPrivateKey(String key, String value) {
        return "";
    }

    /**
     * 明文密码解密dh私钥-aes
     */
    public static String aesDecryptDHPrivateKey(String key, String value) {
        return "";
    }

    /**
     * 明文密码解密rsa私钥-aes
     */
    public static String aesDecryptRSAPrivateKey(String key, String value) {
        return "";
    }

    /**
     * 忘记密码验签
     */
    public static String signatureUploadKeys(String password, String telephone) {
        return "";
    }

    /**
     * 修改密码验签
     */
    public static String signatureUpdateKeys(String password, String checkCode) {
        return "";
    }
    /**
     * aes encrypt  dh/rsa privateKey upload Serve
     * ase decrypt encrypted dh/rsa  privateKey from Serve.
     * key is login password
     * --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * save/take dh/rsa private from local
     */
    /**
     * userId加密dh私钥-aes set
     */
    public static void setDHPrivateKey(String key, String value) {
    }

    /**
     * userId加密rsa公钥-aes set
     */
    public static void setRSAPublicKey(String key, String value) {
    }

    /**
     * userId加密rsa私钥-aes set
     */
    public static void setRSAPrivateKey(String key, String value) {
    }

    /**
     * userId解密dh私钥-aes get
     */
    public static String getDHPrivateKey(String key) {
        return "";
    }

    /**
     * userId解密dh公钥-aes get
     */
    public static String getRSAPublicKey(String key) {
        return "";
    }

    /**
     * userId解密dh私钥-aes get
     */
    public static String getRSAPrivateKey(String key) {
        return "";
    }
    /**
     * save/take dh/rsa private from local
     * --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * single/multi chat des
     */
    public static String getSymmetricKey(long timeSend, String msgId) {
        return MD5.encryptHex(AppConfig.apiKey + timeSend + msgId);
    }

    /**
     * single/multi chat aes && db message
     */
    public static String getSymmetricKey(String msgId) {
        return Base64.encode(MD5.encrypt(AppConfig.apiKey + msgId));
    }

    /**
     * single chat dh&&aes
     */
    public static String getSingleSymmetricKey(String msgId, String dhSymmetricKey) {
        return "";
    }

    /**
     * multi save local chatKey
     *
     * @param roomJid
     * @param chatKey
     * @return
     */
    public static String encryptChatKey(String roomJid, String chatKey) {
        return "";
    }

    /**
     * multi get local chatKey
     *
     * @param roomJid
     * @param chatKeyGroup
     * @return
     */
    public static String decryptChatKey(String roomJid, String chatKeyGroup) {
        return "";
    }

    /**
     * single chat signature
     */
    public static String getSignatureSingle(String fromUserId, String toUserId, int isEncrypt, String msgId, String sKey, String value) {
        return "";
    }

    /**
     * multi chat signature
     */
    public static String getSignatureMulti(String fromUserId, String toUserId, int isEncrypt, String msgId, String sKey, String value) {
        return "";
    }

    /**
     * set findPasswordStatus for DataDownloadActivity downloadRoom
     *
     * @param key
     * @param value
     */
    public static void setFindPasswordStatus(String key, boolean value) {
        PreferenceUtils.putBoolean(MyApplication.getContext(), AppConstant.FIND_PASSWORD_STATUS + key, value);
    }

    /**
     * get findPasswordStatus for DataDownloadActivity downloadRoom
     *
     * @param key
     * @return
     */
    public static boolean getFindPasswordStatus(String key) {
        return PreferenceUtils.getBoolean(MyApplication.getContext(), AppConstant.FIND_PASSWORD_STATUS + key, false);
    }
}
