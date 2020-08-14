package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class RSA {
    /**
     * 随机生成密钥对
     */
    public static RsaKeyPair genKeyPair() {
        try {
            // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            // 初始化密钥对生成器，密钥大小为96-1024位
            keyPairGen.initialize(1024, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
            return new RsaKeyPair(publicKey.getEncoded(), convertPkcs8ToPkcs1(privateKey.getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static byte[] encrypt(byte[] str, byte[] publicKey) {
        try {
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return cipher.doFinal(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptBase64(byte[] str, byte[] publicKey) {
        return Base64.encode(encrypt(str, publicKey));
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     */
    public static byte[] decrypt(byte[] str, byte[] privateKey) {
        try {
            //base64编码的私钥
            RSAPrivateKey priKey = parseRsaPrivateKey(privateKey);
            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return cipher.doFinal(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析rsa私钥，兼容pkcs1和pkcs8格式，
     */
    private static RSAPrivateKey parseRsaPrivateKey(byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPrivateKey priKey;
        try {
            // 兼容pkcs1格式私钥，
            priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(convertPkcs1ToPkcs8(privateKey)));
        } catch (Exception e) {
            priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec((privateKey)));
        }
        return priKey;
    }

    private static byte[] convertPkcs8ToPkcs1(byte[] pkcs8Bytes) {
        return Arrays.copyOfRange(pkcs8Bytes, 26, pkcs8Bytes.length);
    }

    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        return join(pkcs8Header, pkcs1Bytes);
    }

    private static byte[] join(byte[] byteArray1, byte[] byteArray2) {
        byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
    }

    public static byte[] decryptFromBase64(String str, byte[] privateKey) {
        return decrypt(Base64.decode(str), privateKey);
    }

    public static byte[] sign(byte[] data, byte[] privateKey) {
        try {
            RSAPrivateKey priKey = parseRsaPrivateKey(privateKey);
            // 用私钥对信息生成数字签名
            Signature signature = Signature.getInstance("Sha1withRSA");
            signature.initSign(priKey);
            signature.update(data);

            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sign(String data, byte[] privateKey) {
        return sign(data.getBytes(), privateKey);
    }

    public static String signBase64(String data, byte[] privateKey) {
        return Base64.encode(sign(data.getBytes(), privateKey));
    }

    public static boolean verify(byte[] data, byte[] publicKey, byte[] sign) {
        try {
            // 构造X509EncodedKeySpec对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);

            // KEY_ALGORITHM 指定的加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("Sha1withRSA");
            signature.initVerify(pubKey);
            signature.update(data);

            // 验证签名是否正常
            return signature.verify(sign);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String data, byte[] publicKey, byte[] sign) {
        return verify(data.getBytes(), publicKey, sign);
    }

    public static boolean verifyFromBase64(String data, byte[] publicKey, String sign) {
        return verify(data.getBytes(), publicKey, Base64.decode(sign));
    }

    public static class RsaKeyPair {
        private byte[] publicKey;
        private byte[] privateKey;

        public RsaKeyPair(byte[] publicKey, byte[] privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }

        public String getPublicKeyBase64() {
            return Base64.encode(publicKey);
        }

        public String getPrivateKeyBase64() {
            return Base64.encode(privateKey);
        }
    }

}
