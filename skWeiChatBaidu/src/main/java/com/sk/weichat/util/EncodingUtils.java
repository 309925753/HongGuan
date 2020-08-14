package com.sk.weichat.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.qrcode.utils.CommonUtils;

/**
 * Created by Administrator on 2018/4/2 0002.
 */

public class EncodingUtils {
    /**
     * 生成二维码
     */
    public static Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBitmap) {
        Bitmap bitmap = CommonUtils.createQRCode(content, widthPix, heightPix);
        if (logoBitmap != null) {
            bitmap = addLogo(bitmap, logoBitmap);
        }
        return bitmap;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    public static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        // 获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        // logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save(); // Canvas.ALL_SAVE_FLAG

            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }
}
