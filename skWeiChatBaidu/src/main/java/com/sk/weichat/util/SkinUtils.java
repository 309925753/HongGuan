package com.sk.weichat.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.weichat.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by zq on 2017/10/23 0023.
 */

public class SkinUtils {
    // 添加皮肤在这里加一行就可以，
    // 然后字符串要国际化，改皮肤页面显示用，
    // 保存hashCode作本地持久化，所以hashCode必须唯一，并且添加字段也要更新hashCode方法，
    public static List<Skin> defaultSkins = Arrays.asList(
            // 绿色主题，白色标题绿色控件，
            new Skin(R.string.skin_qian_dou_green, 0xffffff, 0x61D999, true),
            // 蓝色主题，白色标题蓝色控件，
            new Skin(R.string.skin_Qing_Shui_blue, 0xffffff, 0x80BFFF, true),
            // 红色主题，白色标题红色控件，
            new Skin(R.string.skin_Shan_Hu_Hong, 0xffffff, 0xFF8080, true),
            // 粉色主题，白色标题粉色控件，
            new Skin(R.string.skin_Liu_Xia_Fen, 0xffffff, 0xFFA5C9, true),
            // 淡钴绿
            new Skin(R.string.skin_Dan_Gu_green, 0xffffff, 0x55BEB7, true),
            // 葡萄紫
            new Skin(R.string.skin_pu_tao_zi, 0xffffff, 0x6C53AB, true),
            // 商务蓝
            new Skin(R.string.skin_Shang_Wu_lan, 0xffffff, 0x3B5699, true),
            // 经典红
            new Skin(R.string.jing_dian_hong, 0xffffff, 0xfd504e, true)
    );
    // sk默认极简绿,
    private static final Skin DEFAULT_SKIN = defaultSkins.get(0);

    @Nullable
    private static Skin currentSkin = null;

    @NonNull
    private static Skin requireSkin(Context ctx) {
        if (currentSkin != null) {
            return currentSkin;
        }
        synchronized (SkinUtils.class) {
            if (currentSkin == null) {
                int savedSkinColor = PreferenceUtils.getInt(ctx, Constants.KEY_SKIN_NAME, DEFAULT_SKIN.hashCode());
                for (Skin skin : defaultSkins) {
                    if (skin.hashCode() == savedSkinColor) {
                        currentSkin = skin;
                        break;
                    }
                }
                if (currentSkin == null) {
                    // 本地保存的皮肤数据出了异常，比如高版本删除了低版本存在的皮肤，
                    currentSkin = DEFAULT_SKIN;
                }
            }
        }
        return currentSkin;
    }

    public static Skin getSkin(Context ctx) {
        return requireSkin(ctx);
    }

    public static void setSkin(Context ctx, Skin skin) {
        currentSkin = skin;
        PreferenceUtils.putInt(ctx, Constants.KEY_SKIN_NAME, skin.hashCode());
    }

    public static class Skin {
        // 表示未激活的灰色，
        private static final int normalColor = 0xff333333;
        private int colorName;
        // 主色，也就是标题栏的颜色，
        private int primaryColor;
        // 活跃的颜色，也就是各种按钮控件激活状态的颜色，
        private int accentColor;
        // 亮色主题，如果是，标题栏状态栏文字图标就要深色，
        private boolean light;

        Skin(int colorName, int primaryColor, int accentColor, boolean light) {
            this.colorName = colorName;
            this.primaryColor = 0xff000000 | primaryColor;
            this.accentColor = 0xff000000 | accentColor;
            this.light = light;
        }

        /**
         * 颜色叠加算法，
         *
         * @param color        叠加前的颜色RGB，
         * @param overlayColor 要叠加的颜色RGB，
         * @param alpha        叠加颜色的透明度，0-1，1是不透明，
         */
        public static int colorBlend(int color, int overlayColor, float alpha) {

            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            int ovR = Color.red(overlayColor);
            int ovG = Color.green(overlayColor);
            int ovB = Color.blue(overlayColor);

            int newR = singleColorBlend(r, ovR, alpha);
            int newG = singleColorBlend(g, ovG, alpha);
            int newB = singleColorBlend(b, ovB, alpha);

            return Color.rgb(newR, newG, newB);
        }

        /**
         * 计算红绿蓝其一叠加后的色值，
         */
        static int singleColorBlend(int color, int overlay, float alpha) {
            return (int) (color * (1 - alpha) + overlay * alpha);
        }

        public int getColorName() {
            return colorName;
        }

        public int getPrimaryColor() {
            return primaryColor;
        }

        public int getAccentColor() {
            return accentColor;
        }

        public boolean isLight() {
            return light;
        }

        public ColorStateList getTabColorState() {
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked}
            };

            int[] colors = new int[]{
                    normalColor,
                    getAccentColor()
            };

            return new ColorStateList(states, colors);
        }

        public ColorStateList getMainTabColorState() {
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked}
            };

            int[] colors = new int[]{
                    0xff333333,
                    getAccentColor()
            };

            return new ColorStateList(states, colors);
        }

        public ColorStateList getButtonColorState() {
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{android.R.attr.state_pressed},
                    new int[]{android.R.attr.state_focused},
                    new int[]{},
            };

            int[] colors = new int[]{
                    0xffa7a7a7,
                    colorBlend(getAccentColor(), 0x000000, 0.2f),
                    getAccentColor(),
                    getAccentColor(),
            };

            return new ColorStateList(states, colors);
        }

        /**
         * 用于那些选中时背景色变成皮肤色，未选中时背景镂空，文字色为皮肤色，的按钮，
         */
        public ColorStateList getHighlightColorState() {
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_checked},
                    new int[]{},
            };

            int[] colors = new int[]{
                    0xffffffff,
                    getAccentColor(),
            };

            return new ColorStateList(states, colors);
        }

        /**
         * copy from java.awt.Color.brighter,
         */
        public int getBrighterPrimaryColor() {
            int color = primaryColor;
            int var1 = Color.red(color);
            int var2 = Color.green(color);
            int var3 = Color.blue(color);
            int var4 = Color.alpha(color);
            byte var5 = 3;
            if (var1 == 0 && var2 == 0 && var3 == 0) {
                return Color.argb(var4, var5, var5, var5);
            } else {
                if (var1 > 0 && var1 < var5) {
                    var1 = var5;
                }

                if (var2 > 0 && var2 < var5) {
                    var2 = var5;
                }

                if (var3 > 0 && var3 < var5) {
                    var3 = var5;
                }

                return Color.argb(var4, Math.min((int) ((double) var1 / 0.7D), 255), Math.min((int) ((double) var2 / 0.7D), 255), Math.min((int) ((double) var3 / 0.7D), 255));
            }
        }

        /**
         * colorName是资源id，不保证每次编译都相同，不能参与计算，
         * 其他字段都要参与，
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Skin skin = (Skin) o;
            return primaryColor == skin.primaryColor &&
                    accentColor == skin.accentColor &&
                    light == skin.light;
        }

        /**
         * colorName是资源id，不保证每次编译都相同，不能参与计算，
         * 其他字段都要参与，
         */
        @Override
        public int hashCode() {
            return Objects.hash(primaryColor, accentColor, light);
        }
    }
}
