package com.sk.weichat.util.secure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoginPasswordTest {
    private static String password = "123456";
    private static String oldEncryptedPassword = MD5.encryptHex(password);
    private static String encryptedPassword = "7ba1bd982b33ac731c2c3bca90e77be9";

    @Test
    public void encode() {
        assertEquals(encryptedPassword, LoginPassword.encodeMd5(password));
    }

    @Test
    public void encodeFromOldPassword() {
        assertEquals(encryptedPassword, LoginPassword.encodeFromOldPassword(oldEncryptedPassword));
    }
}