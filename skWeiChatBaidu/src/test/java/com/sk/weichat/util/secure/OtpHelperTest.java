package com.sk.weichat.util.secure;

import com.sk.weichat.helper.OtpHelper;
import com.sk.weichat.util.Base64;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class OtpHelperTest {
    @Test
    public void generate() throws Exception {
        int userId = 10070133;
        byte[] key = Base64.decode("bvOKcP6SFo/jdGCL/NK5kw==");
        testQrCode(userId, key);
        userId = Integer.MAX_VALUE;
        testQrCode(userId, key);
        userId = 1;
        testQrCode(userId, key);
        userId = 10000;
        testQrCode(userId, key);
    }

    private void testQrCode(int userId, byte[] key) {
        long timeMinute = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
        byte randByte = (byte) (Math.random() * 256);
        OtpHelper.QrCode qrCode = OtpHelper.generate(userId, timeMinute, key, randByte);
        System.out.println("" + (int) qrCode.getRandByte());
        String qrCodeString = qrCode.getQrCodeString();
        System.out.println(qrCodeString);
        assertEquals(qrCode, OtpHelper.parse(qrCodeString));

        assertEquals(qrCode.getOtp(), OtpHelper.otp(userId, timeMinute, key, qrCode.getRandByte()));
    }

    @Test
    public void testByteArrayToInt() throws Exception {
        assertEquals(Long.parseLong("03020100", 16), OtpHelper.byteArrayToLong(new byte[]{0x00, 0x01, 0x02}, (byte) 0x03));
        assertEquals(Long.parseLong("98badcfe", 16), OtpHelper.byteArrayToLong(new byte[]{(byte) 0xfe, (byte) 0xdc, (byte) 0xba}, (byte) 0x98));
    }
}