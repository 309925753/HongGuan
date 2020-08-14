package com.sk.weichat.util.secure;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 统一金额字符串的格式，
 * 小数末尾没有0，单位是元，整元没有小数点，如为大数不能转成科学计数法，
 */
@SuppressWarnings("WeakerAccess")
public class Money {
    public static String fromYuan(String money) {
        return toString(new BigDecimal(money));
    }

    public static String fromCent(String money) {
        return fromCent(Integer.valueOf(money));
    }

    public static String fromCent(int money) {
        BigDecimal ret = new BigDecimal(money);
        ret = ret.divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY);
        return toString(ret);
    }

    private static String toString(BigDecimal bigDecimal) {
        if (BigDecimal.ZERO.compareTo(bigDecimal) == 0) {
            // stripTrailingZeros不会把0.0处理成0，所以手动处理，
            // 0.0 equals 0 为 false，compare又相等，简直，
            return "0";
        }
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    //stripped.toPlainString()
    public static String fromBigDecimaltoString(String s) {
        return new BigDecimal(s).stripTrailingZeros().toPlainString();
    }
}
