package com.sk.weichat.util.secure;

import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import okio.ByteString;

import static org.junit.Assert.assertEquals;

public class PayPasswordTest {
    private static String password = "123456";
    private static String userId = "10000";
    private static String oldEncryptedPassword = MD5.encryptHex(password);
    private static String encryptedPassword = "a993866630d8905ebc5cb5931b39b840";

    @Test
    public void encode() {
        assertEquals(encryptedPassword, PayPassword.encodeMd5(userId, password));
    }

    @Test
    public void encodeFromOldPassword() {
        assertEquals(encryptedPassword, PayPassword.encodeFromOldPassword(userId, oldEncryptedPassword));
    }

    @Test
    public void testRandom() {
        int[] times = new int[128];
        for (int t = 0; t < 5000; t++) {
            Random r = new Random();
            char[] buf = new char[6];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (char) ('0' + r.nextInt(10));
            }
            String payPassword = new String(buf);
            byte[] key = PayPassword.encode(payPassword);
            BitSet bitSet = BitSet.valueOf(key);
            for (int i = 0; i < (key.length * 8); i++) {
                if (bitSet.get(i)) {
                    times[i]++;
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int i1 = 0; i1 < 16; i1++) {
                int time = times[16 * i + i1];
                System.out.print(String.format("%5s", time));
            }
            System.out.println();
        }
        System.out.println("标准差：" + standardDeviation(times));
    }

    @Test
    public void test123456() {
        printResult("123456");
    }

    @Test
    public void testInt() {
        System.out.println(toBinaryString(999999));
        int rate = (int) ((1L << 32) / 999999);
        assertEquals(4294, rate);
        assertEquals("00000000000000000001000011000101", toBinaryString(4293));
        System.out.println(toBinaryString(999999 * 4293));
    }

    private String toBinaryString(int i) {
        return String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
    }

    private double standardDeviation(int[] array) {
        int sum = Arrays.stream(array).sum();
        double average = 1.0 * sum / array.length;
        double total = 0;
        for (int t : array) {
            total += (t - average) * (t - average);
        }
        return Math.sqrt(total / array.length);
    }

    private void printHex(byte[] key) {
        System.out.println("hex: " + ByteString.of(key).hex());
    }

    private void printByteArray(byte[] key) {
        StringBuilder sb = new StringBuilder(key.length * 8);
        for (byte b : key) {
            sb.append(byteToString(b));
        }
        System.out.println("bin: " + sb);
    }

    public String byteToString(byte b) {
        byte[] masks = {-128, 64, 32, 16, 8, 4, 2, 1};
        StringBuilder builder = new StringBuilder();
        for (byte m : masks) {
            if ((b & m) == m) {
                builder.append('1');
            } else {
                builder.append('0');
            }
        }
        return builder.toString();
    }

    private void printResult(String payPassword) {
        System.out.println(payPassword);
        byte[] key = PayPassword.encode(payPassword);
        printHex(key);
        printByteArray(key);
    }
}