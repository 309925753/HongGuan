package com.sk.weichat.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {

    public static boolean saveBitmapToSDCard(Bitmap bmp, String strPath) {
        if (bmp == null) {
            return false;
        }
        if (TextUtils.isEmpty(strPath)) {
            return false;
        }
        try {
            File file = new File(strPath.substring(0, strPath.lastIndexOf("/")));
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(strPath);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = BitmapUtil.bitampToByteArray(bmp);
            fos.write(buffer);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static byte[] bitampToByteArray(Bitmap bitmap) {
        byte[] array = null;
        try {
            if (null != bitmap) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                array = os.toByteArray();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }

    /**
     * xuan  2018-12-19 19:30:51
     * 安全的加载一个bitmap
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight, int inSampleSize) {
        // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    /**
     * 返回图片的高宽
     *
     * @return
     */
    public static int[] getImageParamByIntsFile(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        int[] param;
        if (bitmap != null) {
            param = new int[]{bitmap.getWidth(), bitmap.getHeight()};
            bitmap.recycle();
        } else {// 容错
            param = new int[]{100, 100};
        }
        return param;
    }

    public static boolean getImageIsQRcode(Context context, String filePath) {
        Bitmap srcBitmap = BitmapFactory.decodeFile(filePath);
        if (srcBitmap == null) {
            return false;
        }
        // 保证图片的宽高不大于屏幕的宽高
        if (srcBitmap.getWidth() > ScreenUtil.getScreenWidth(context) || srcBitmap.getHeight() > ScreenUtil.getScreenHeight(context)) {
            //先计算原始的宽高比
            float ratio = srcBitmap.getWidth() * 1.0f / (srcBitmap.getHeight());
            srcBitmap = zoomImg(srcBitmap, (int) (ScreenUtil.getScreenHeight(context) * ratio), ScreenUtil.getScreenHeight(context));
        }
        Rect[] rects = QrCodeUtils.parsesMultiFromBitmap(srcBitmap);
        return rects != null;
    }

    public static Bitmap getImageIsQRcode(Context context, Activity activity, String filePath) {
        Bitmap srcBitmap = BitmapFactory.decodeFile(filePath);
        // 保证图片的宽高不大于屏幕的宽高
        if (srcBitmap.getWidth() != ScreenUtil.getScreenWidth(context) || srcBitmap.getHeight() != ScreenUtil.getScreenHeight(context)) {
            //先计算原始的宽高比
            float ratio = srcBitmap.getWidth() * 1.0f / (srcBitmap.getHeight());
            srcBitmap = zoomImg(srcBitmap, (int) (ScreenUtil.getScreenHeight(context) * ratio), ScreenUtil.getScreenHeight(context));
        }
        Rect[] rects = QrCodeUtils.parsesMultiFromBitmap(srcBitmap);
        Log.e("zx", "getImageIsQRcode: " + rects[0].left + "  " + rects[0].top + "　" + rects[0].right + "  " + rects[0].bottom);
        Bitmap bitmap = getBitmap(srcBitmap, rects[0].left, rects[0].top, rects[0].right, rects[0].bottom);
        return bitmap;
    }

    public static Bitmap getBitmap(Bitmap b, int x, int y, int w, int h) {
        Bitmap bitmap = b;
        try {
            bitmap = (Bitmap) Bitmap.createBitmap(bitmap, x < 0 ? 0 : x, y < 0 ? 0 : y, w - (x < 0 ? 10 : x), h - (y < 0 ? 10 : y));
            FileOutputStream fout = new FileOutputStream("mnt/sdcard/test.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }
}
