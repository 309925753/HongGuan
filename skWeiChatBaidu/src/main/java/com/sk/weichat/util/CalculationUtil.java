package com.sk.weichat.util;

public class CalculationUtil {

    /**
     * 取数组中最大值
     *
     * @param lastPositions
     * @return
     */
    public static int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
