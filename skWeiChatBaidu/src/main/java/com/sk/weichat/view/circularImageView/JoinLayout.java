//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sk.weichat.view.circularImageView;

public class JoinLayout {
    public static final String TAG = JoinLayout.class.getSimpleName();
    private static final float[][] rotations = new float[][]{{360.0F}, {45.0F, 360.0F}, {120.0F, 0.0F, -120.0F}, {90.0F, 180.0F, -90.0F, 0.0F}, {144.0F, 72.0F, 0.0F, -72.0F, -144.0F}};
    private static final float[][] sizes = new float[][]{{0.9F, 0.9F}, {0.5F, 0.65F}, {0.45F, 0.8F}, {0.45F, 0.91F}, {0.38F, 0.8F}};

    public JoinLayout() {
    }

    public static int max() {
        return 5;
    }

    public static float[] rotation(int count) {
        return count > 0 && count <= rotations.length ? rotations[count - 1] : null;
    }

    public static float[] size(int count) {
        return count > 0 && count <= sizes.length ? sizes[count - 1] : null;
    }

    public static float[] offset(int count, int index, float dimension, float[] size) {
        switch (count) {
            case 1:
                return offset1(index, dimension, size);
            case 2:
                return offset2(index, dimension, size);
            case 3:
                return offset3(index, dimension, size);
            case 4:
                return offset4(index, dimension, size);
            case 5:
                return offset5(index, dimension, size);
            default:
                return new float[]{0.0F, 0.0F};
        }
    }

    private static float[] offset5(int index, float dimension, float[] size) {
        float cd = dimension * size[0];
        float s1 = -cd * size[1];
        float x1 = 0.0F;
        float x2 = (float) ((double) s1 * Math.cos(0.3316125578789226D));
        float y2 = (float) ((double) s1 * Math.sin(0.3141592653589793D));
        float x3 = (float) ((double) s1 * Math.cos(0.9424777960769379D));
        float y3 = (float) ((double) (-s1) * Math.sin(0.9424777960769379D));
        float x4 = (float) ((double) (-s1) * Math.cos(0.9424777960769379D));
        float y4 = (float) ((double) (-s1) * Math.sin(0.9424777960769379D));
        float x5 = (float) ((double) (-s1) * Math.cos(0.3316125578789226D));
        float y5 = (float) ((double) s1 * Math.sin(0.3141592653589793D));
        float xx1 = (dimension - cd - y3 - s1) / 2.0F;
        float xxc1 = (dimension - cd) / 2.0F;
        switch (index) {
            case 0:
                return new float[]{x1 + xxc1, s1 + xx1};
            case 1:
                return new float[]{x2 + xxc1, y2 + xx1};
            case 2:
                return new float[]{x3 + xxc1, y3 + xx1};
            case 3:
                return new float[]{x4 + xxc1, y4 + xx1};
            case 4:
                return new float[]{x5 + xxc1, y5 + xx1};
            default:
                return new float[]{0.0F, 0.0F};
        }
    }

    private static float[] offset4(int index, float dimension, float[] size) {
        float cd = dimension * size[0];
        float s1 = cd * size[1];
        float x1 = 0.0F;
        float y1 = 0.0F;
        float xx1 = (dimension - cd - s1) / 2.0F;
        switch (index) {
            case 0:
                return new float[]{x1 + xx1, y1 + xx1};
            case 1:
                return new float[]{s1 + xx1, y1 + xx1};
            case 2:
                return new float[]{s1 + xx1, s1 + xx1};
            case 3:
                return new float[]{x1 + xx1, s1 + xx1};
            default:
                return new float[]{0.0F, 0.0F};
        }
    }

    private static float[] offset3(int index, float dimension, float[] size) {
        float cd = dimension * size[0];
        float s1 = cd * size[1];
        float y2 = s1 * 1.0F;
        float x2 = s1 - y2 / 1.73205F;
        float x3 = s1 * 2.0F - x2;
        float xx1 = (dimension - cd - y2) / 2.0F;
        float xxc1 = (dimension - cd) / 2.0F - s1;
        switch (index) {
            case 0:
                return new float[]{s1 + xxc1, xx1};
            case 1:
                return new float[]{x2 + xxc1, y2 + xx1};
            case 2:
                return new float[]{x3 + xxc1, y2 + xx1};
            default:
                return new float[]{0.0F, 0.0F};
        }
    }

    private static float[] offset2(int index, float dimension, float[] size) {
        float cd = dimension * size[0];
        float s1 = cd * size[1];
        float x1 = 0.0F;
        float y1 = 0.0F;
        float xx1 = (dimension - cd - s1) / 2.0F;
        switch (index) {
            case 0:
                return new float[]{x1 + xx1, y1 + xx1};
            case 1:
                return new float[]{s1 + xx1, s1 + xx1};
            default:
                return new float[]{0.0F, 0.0F};
        }
    }

    private static float[] offset1(int index, float dimension, float[] size) {
        float cd = dimension * size[0];
        float offset = (dimension - cd) / 2.0F;
        return new float[]{offset, offset};
    }
}
