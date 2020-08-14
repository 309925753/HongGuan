package com.sk.weichat.util.secure;

import com.sk.weichat.util.Base64;

import org.junit.Test;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RSATest {
    public static final RSA.RsaKeyPair keyPair = new RSA.RsaKeyPair(
            Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLjXCd0y8wucMlQDd9S9cFeCA0H" +
                    "/l/prnouwWgGOEzoaS1gBK4IK0AAiNd7mz8EP+4m9DqeaGW63ei3aws43qV1lDpsVepfJ2PPe/5" +
                    "VBx7uAKKGqPU+IlNP6EBWUWMMsrCS/oh6LHucCyLah5YhyXOju1cZTfqQ1VFWsbZupmUaQIDAQAB"),
            Base64.decode("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIuNcJ3TLzC5wyVAN31L" +
                    "1wV4IDQf+X+muei7BaAY4TOhpLWAErggrQACI13ubPwQ/7ib0Op5oZbrd6LdrCzjepXWUOmxV6l8" +
                    "nY897/lUHHu4Aooao9T4iU0/oQFZRYwyysJL+iHose5wLItqHliHJc6O7VxlN+pDVUVaxtm6mZRpA" +
                    "gMBAAECgYAKHDkodgBZO1wT+s8KWNA/KTDMFfTxdpbJcaM6shK+tttD+v9gL53Y/k6po3hp2qFsM" +
                    "n20PxOh53VHa1/p8KEU1j+DwLbNC5eIp7/5ZNWwftQTSHBCqSyr+7rE0i6Gcst1qT0ioKUS1fOHI" +
                    "ZSt0gfBOf1eEzhpLDT1o0QgY98cAQJBANrWFNml89xHZQAUmXvrcC/vzmbfktWuHpTP4gRoURp4U" +
                    "h7j07xD7dVN/gbk42K70VWCTWTRSARApA9IfjACuqECQQCjQH4hh/2H70b23h3OUfiGUSnhupoNU" +
                    "z93xTsaBYbwiTGYH81Sno5aQbO3j8H9gi8qZanSHRG24MUVeyQdRYzJAkBHJ0aeQgxZeklHzmrdV" +
                    "P8kRwfIgTdgDP5aioFFx5lfTvH8oz1MQJYLPhGzsiaRCtqUwApkFnwhDdeKNJr7B1ghAkEAm/knS" +
                    "TQbp/+VxpGK2q/4iaQMJs3ZF7gc4HrBL+ht92ysxJJF4pT4nwU9BrlD98ik9ZXyPXxmi1qPEin35" +
                    "Dup+QJBAMQsiQwjjTGoVJpNrXoxHbSwgrHhJrgP4HUX2XKmbjCfem8dWdU93G4/VDFUDcNJyd33x" +
                    "DOHispMoe+rHwgG0xQ=")
    );

    public static final RSA.RsaKeyPair keyPairPkcs1 = new RSA.RsaKeyPair(
            Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCjpncvOtMHIp4Bv9sX3JMoSlYKCWsaHdDZ5Oi+QybEDQQlk+MS0wDv+CodsbBFkFwkYcScJzXO/2tM7zVLJR71H761u/woIC5WiBivEMfF6paD0oUM/M440N6ek9ZVONd+W29tnsA+pRVPhN8JhIJaWpuB//UoROXp0PWMjfiZwIDAQAB"),
            Base64.decode("MIICXAIBAAKBgQCCjpncvOtMHIp4Bv9sX3JMoSlYKCWsaHdDZ5Oi+QybEDQQlk+MS0wDv+CodsbBFkFwkYcScJzXO/2tM7zVLJR71H761u/woIC5WiBivEMfF6paD0oUM/M440N6ek9ZVONd+W29tnsA+pRVPhN8JhIJaWpuB//UoROXp0PWMjfiZwIDAQABAoGAd/oYBzRNfzpTPY4guDTWUvlfhzYNuOyffP/4OrJoFS/EyOF45NJlXqS8DdRpPhP3uzzhRd7bIyhsLPj4tWYsZGuyA+GyOjF9Zj/rOWPU1rP4qWSFQ1p9pHvugoi3yt9I1bIqggvUcXk3hdnuVdfSjQE1fY5lpXZvGKB6zNpqZVECQQDuWimYnFgc/1BJtSfCwtKiN0eFMw8S4gTyzWttwOtFxBsHo7Q1l5Xvk564kwZXr2CuOXahrJaDjYm7vNzfoy6bAkEAjDk9QynP8YXQsISPB/X/PxYYpZbAti85sk3JPVO2jb3tAkxCYmIxUg1xgpogaOupqKxeQe83gD8742+5xSXSJQJASuFegghUEkAPjChyZlhobffp6ynASZFiNplcb62U/GUAjOTcH54Qx6Rbz+a4rmF1gSaiY2ZiHtAffjB2P3f3kwJASBx7k9mh1ZwyeUSCZd6tOB096ZJAYrCgpEB6eC5f2D7O7vqWvQ+wO3ksYbSvbCWdZ1/VTWUfDrX2L31adLeBfQJBALGYWVO6Ksv72k1vbSywhLYOKVe3JLZiZgFUNvKLh0g1Tfm1pK29veSSGey8HIkGtI04E6tgQVLx3adZSxjdnFI=")
    );
    public static final String content = "hello";
    public static final byte[] contentRaw = Base64.decode("kolOt/LYqkhf/RZu6aJcIA==");
    private static final String encryped = "ejOeerZ5MXrlVi6MyiFiPFqVGPb0hV8gaUqylAu8V+X5/BJMXzYwRNxp0SL0VM9aWGyqtUZbimY/VUB7/fxHHj4nf9bmLKoQkGGgjJ8NfdotyENFgLo01Xe4j58BOIrfre7rnXRYZPGkVCPgXXYPZ49L0yk+O7co+kOFaJbvM+A=";
    private static final String sign = "b1bE8VRWHu3Lwe+kGls6M8uHGQ4Ft/QeAHNpGaLP5tZvZy9CmVKvkNOtQauFq+ETLcynG/crmEhBe0nlx/0gZjyAXv5Ygzct62tPNLl36CIiM5h+YJI+8eym+mCGeSnJsWLxiI96gUQ/dNCyVPrpAY1Zs9Ivw1u8rHEJtDSA+pE=";
    private static final String encrypedRaw = "a6CIZzAPpzaDysCOE9X5FYp723lsTRia/GVDmU4yyhcKaFX2iBICfVwK5gakKK+NgTQ4veMu0l3wpIHM+eRA+Q6zrxCYjE8tkH1O4Jbxcvx4Nai4QP0JqCXDXNpxJMccKhqyNZ01uBq1RjJ++ATkMt66rt5DMW4pLtToh7nLjhg=";
    private static final String signRaw = "RvxmCkUxhtSPLss712C2vH7jpXaV82QXDe/e9EaclgWuVPEliDPmUkwg20PfG5d/xM0l3LAEexHAUWD3svg6HTWo9zw7/l+fYxtkbv59i8Uz7r5Y+j3HVaHKevFEw2Z34PHbiPXVNYBRE/4Qzl8wLT2ZSLzo50yBBFziD4LgvtU=";

    @Test
    public void genKeyPair() throws Exception {
        RSA.RsaKeyPair rsaKeyPair = RSA.genKeyPair();
        System.out.println(rsaKeyPair.getPublicKeyBase64());
        System.out.println(rsaKeyPair.getPrivateKeyBase64());

        byte[] publicKey = Base64.decode(rsaKeyPair.getPublicKeyBase64());
        assertArrayEquals(rsaKeyPair.getPublicKey(), publicKey);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
        assertArrayEquals(rsaKeyPair.getPublicKey(), rsaPublicKey.getEncoded());
    }

    @Test
    public void publicEncrypt() throws Exception {
        // 每次公钥加密得到的密文都是不同的，
        // 加解密只能是公钥加密私钥解密，
        String encrypted = RSA.encryptBase64(content.getBytes(), keyPair.getPublicKey());
        System.out.println(encrypted);
        byte[] decrypted = RSA.decryptFromBase64(encrypted, keyPair.getPrivateKey());
        assertEquals(content, new String(decrypted));
    }

    @Test
    public void testPkcs1() throws Exception {
        // 每次公钥加密得到的密文都是不同的，
        // 加解密只能是公钥加密私钥解密，
        String encrypted = RSA.encryptBase64(content.getBytes(), keyPairPkcs1.getPublicKey());
        System.out.println(encrypted);
        byte[] decrypted = RSA.decryptFromBase64(encrypted, keyPairPkcs1.getPrivateKey());
        assertEquals(content, new String(decrypted));
    }

    @Test
    public void privateDecrypt() throws Exception {
        assertEquals(content, new String(RSA.decryptFromBase64(encryped, keyPairPkcs1.getPrivateKey())));
    }

    @Test
    public void signTest() throws Exception {
        assertEquals(sign, RSA.signBase64(content, RSATest.keyPairPkcs1.getPrivateKey()));
    }

    @Test
    public void verifyTest() throws Exception {
        assertTrue(RSA.verifyFromBase64(content, RSATest.keyPairPkcs1.getPublicKey(), sign));
    }

    @Test
    public void testRaw() throws Exception {
        String encrypted = RSA.encryptBase64(contentRaw, keyPairPkcs1.getPublicKey());
        System.out.println(encrypted);
        byte[] decrypted = RSA.decryptFromBase64(encrypted, keyPairPkcs1.getPrivateKey());
        assertArrayEquals(contentRaw, decrypted);
        assertArrayEquals(contentRaw, RSA.decryptFromBase64(encrypedRaw, keyPairPkcs1.getPrivateKey()));
        assertArrayEquals(Base64.decode(signRaw), RSA.sign(contentRaw, RSATest.keyPairPkcs1.getPrivateKey()));
        assertTrue(RSA.verify(contentRaw, RSATest.keyPairPkcs1.getPublicKey(), Base64.decode(signRaw)));
    }
}