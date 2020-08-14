package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DH {
    private static final String KEY_ALGORITHM = "ECDH";
    // 安卓系统自带也有BC，而且算法有缺，所以不能通过名称"BC"指定provider, 改成直接指定provider对象，
    private static final Provider PROVIDER = new org.bouncycastle.jce.provider.BouncyCastleProvider();

    public static DHKeyPair genKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            keyPairGen.initialize(ecGenParameterSpec, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 公钥
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            // 私钥
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
            byte[] x = publicKey.getEncoded();
            byte[] y = convertPkcs8ToPkcs1(privateKey.getEncoded());
            return new DHKeyPair(x, y);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getCommonSecretKey(byte[] privateKey, byte[] receivedPublicKey) {
        try {
            //初始化ECDH，KeyFactory
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
            //处理私钥
            Key ecPriKey = parseEcdhPrivateKey(privateKey);

            //处理公钥
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(receivedPublicKey);
            PublicKey ecPubKey = keyFactory.generatePublic(pubX509);

            //密钥磋商生成新的密钥Byte数组
            KeyAgreement akeyAgree = KeyAgreement.getInstance(KEY_ALGORITHM, PROVIDER);
            akeyAgree.init(ecPriKey);
            akeyAgree.doPhase(ecPubKey, true);
            return akeyAgree.generateSecret();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getCommonSecretKey(String privateKey, String receivedPublicKey) {
        return (getCommonSecretKey(Base64.decode(privateKey), Base64.decode(receivedPublicKey)));
    }

    public static String getCommonSecretKeyBase64(byte[] privateKey, byte[] receivedPublicKey) {
        return Base64.encode(getCommonSecretKey((privateKey), (receivedPublicKey)));
    }

    public static String getCommonSecretKeyBase64(String privateKey, String receivedPublicKey) {
        return Base64.encode(getCommonSecretKey(Base64.decode(privateKey), Base64.decode(receivedPublicKey)));
    }

    private static byte[] convertPkcs8ToPkcs1(byte[] pkcs8Bytes) {
        return Arrays.copyOfRange(pkcs8Bytes, 26, pkcs8Bytes.length);
    }

    public static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) throws IOException {
        ASN1Primitive prim = ASN1Primitive.fromByteArray(pkcs1Bytes);
        PrivateKeyInfo keyInfo = new PrivateKeyInfo(new AlgorithmIdentifier(
                X9ObjectIdentifiers.id_ecPublicKey,
                SECObjectIdentifiers.secp256k1), prim); // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        return keyInfo.getEncoded();
    }

    private static Key parseEcdhPrivateKey(byte[] privateKey) throws Exception {
        Key priKey;
        try {
            // 兼容pkcs1格式私钥，
            priKey = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER).generatePrivate(new PKCS8EncodedKeySpec(convertPkcs1ToPkcs8(privateKey)));
        } catch (Exception e) {
            priKey = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER).generatePrivate(new PKCS8EncodedKeySpec((privateKey)));
        }
        return priKey;
    }

    public static class DHKeyPair {
        private byte[] publicKey;
        private byte[] privateKey;

        public DHKeyPair(byte[] publicKey, byte[] privateKey) {
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
