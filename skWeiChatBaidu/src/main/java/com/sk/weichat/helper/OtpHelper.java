package com.sk.weichat.helper;

import com.sk.weichat.AppConfig;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.secure.MAC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * one time password一次性离线口令，用于付款码，
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class OtpHelper {
    private static long sTimeMinute = 0;
    private static byte sRandByte = 0;

    public static QrCode generate(int userId, byte[] key) {
        long timeSeconds = TimeUtils.sk_time_current_time();
        long timeMinute = TimeUnit.SECONDS.toMinutes(timeSeconds);
        if (sTimeMinute != timeMinute) {
            sTimeMinute = timeMinute;
            sRandByte = 0;
        }
        byte randByte = sRandByte++;
        return generate(userId, timeMinute, key, randByte);
    }

    public static QrCode generate(int userId, long timeMinute, byte[] key, byte randByte) {
        long otp = otp(userId, timeMinute, key, randByte);
        return new QrCode(userId, otp);
    }

    public static long otp(int userId, long timeMinute, byte[] key, byte randByte) {
        String content = AppConfig.apiKey + userId + (randByte & 0xff) + timeMinute;
        byte[] hash = MAC.encode(content, key);
        return byteArrayToLong(hash, randByte);
    }

    public static QrCode parse(long qrCode) {
        int userId = (int) (qrCode >> 32);
        long otp = qrCode & 0xffffffffL;
        return new QrCode(userId, otp);
    }

    public static QrCode parse(String qrCode) {
        return parse(Long.valueOf(qrCode));
    }

    /**
     * 4字节数组转成无符号整数，
     * 低字节序, 即低位字节在前，高位字节在后，
     */
    public static long byteArrayToLong(byte[] hash, byte randByte) {
        if (hash.length != 4) {
            hash = Arrays.copyOf(hash, 4);
        }
        hash[3] = randByte;
        int i = ByteBuffer.wrap(hash)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        return i & 0xffffffffL;
    }

    public static class QrCode {
        private int userId;
        private long otp;
        private byte randByte;

        public QrCode(int userId, long otp) {
            this.userId = userId;
            this.otp = otp;
            this.randByte = (byte) (otp >> 24);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QrCode qrCode = (QrCode) o;
            return userId == qrCode.userId &&
                    otp == qrCode.otp &&
                    randByte == qrCode.randByte;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, otp, randByte);
        }

        public long getQrCode() {
            return ((long) userId << 32) + otp;
        }

        public String getQrCodeString() {
            return String.format(Locale.ENGLISH, "%019d", getQrCode());
        }

        public int getUserId() {
            return userId;
        }

        public long getOtp() {
            return otp;
        }

        public byte getRandByte() {
            return randByte;
        }
    }
}
