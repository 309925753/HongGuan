package com.sk.weichat.util;

import android.content.Context;
import android.content.res.ColorStateList;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.User;

import java.text.DecimalFormat;

public class DisplayUtil {

    public static final int voice_view_max_width = 145;// dp
    public static final int voice_view_min_width = 40;// dp
    // 声音最长可以表现为多少毫秒（实际本程序是60s,但是如果这里是60s的话，当时间很短，就没啥差别
    public static final float voice_max_length = 30;
    private static final int ENABLE_ATTR = android.R.attr.state_enabled;
    private static final int CHECKED_ATTR = android.R.attr.state_checked;
    private static final int PRESSED_ATTR = android.R.attr.state_pressed;

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getVoiceViewWidth(Context context, int seconds) {
        if (seconds >= voice_max_length) {
            return dip2px(context, voice_view_max_width);
        }
        final int dpLen = (int) ((seconds / voice_max_length) * (voice_view_max_width - voice_view_min_width)) + voice_view_min_width;
        return dip2px(context, dpLen);
    }

    public static String getDistance(double latitude, double longitude, User data) {
        String value = MyApplication.getInstance().getString(R.string.tip_private_location); // 国际化
        String met = "m"; // 国际化

        LatLng point_start = new LatLng(latitude, longitude);

        double sLat;
        double sLng;
        if (data.getLoc() != null) {
            sLat = data.getLoc().getLat();
            sLng = data.getLoc().getLng();
        } else {
            sLat = 0;
            sLng = 0;
        }

        if (sLat > 1 && sLng > 1) {
            LatLng point_end = new LatLng(sLat, sLng);

            double distance = DistanceUtil.getDistance(point_start, point_end);

            if (distance > 1000) {
                met = "km";
                distance = distance / 1000;
            }

            value = "0 m";
            if (distance > 0) {
                DecimalFormat df = new DecimalFormat(".##");
                value = df.format(distance) + " " + met;
            }

        }
        if (value.startsWith(".")) {
            return "0" + value;
        }
        return value;
    }

    public static ColorStateList generateThumbColorWithTintColor(final int tintColor) {
        int[][] states = new int[][]{
                {-ENABLE_ATTR, CHECKED_ATTR},
                {-ENABLE_ATTR},
                {PRESSED_ATTR, -CHECKED_ATTR},
                {PRESSED_ATTR, CHECKED_ATTR},
                {CHECKED_ATTR},
                {-CHECKED_ATTR}
        };

        int[] colors = new int[]{
                tintColor - 0xAA000000,
                0xFFBABABA,
                tintColor - 0x99000000,
                tintColor - 0x99000000,
                tintColor | 0xFF000000,
                0xFFEEEEEE
        };
        return new ColorStateList(states, colors);
    }

    public static ColorStateList generateBackColorWithTintColor(final int tintColor) {
        int[][] states = new int[][]{
                {-ENABLE_ATTR, CHECKED_ATTR},
                {-ENABLE_ATTR},
                {CHECKED_ATTR, PRESSED_ATTR},
                {-CHECKED_ATTR, PRESSED_ATTR},
                {CHECKED_ATTR},
                {-CHECKED_ATTR}
        };

        int[] colors = new int[]{
                tintColor - 0xE1000000,
                0x10000000,
                tintColor - 0xD0000000,
                0x20000000,
                tintColor - 0xD0000000,
                0x20000000
        };
        return new ColorStateList(states, colors);
    }
}
