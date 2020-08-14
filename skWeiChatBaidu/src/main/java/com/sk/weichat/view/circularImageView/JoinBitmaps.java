//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sk.weichat.view.circularImageView;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

import java.util.List;

public class JoinBitmaps {
    public JoinBitmaps() {
    }

    public static void join(Canvas canvas, int dimension, List<Bitmap> bitmaps) {
        if (bitmaps != null) {
            int count = Math.min(bitmaps.size(), JoinLayout.max());
            float[] size = JoinLayout.size(count);
            join(canvas, dimension, bitmaps, count, size);
        }
    }

    public static void join(Canvas canvas, int dimension, List<Bitmap> bitmaps, int count, float[] size) {
        join(canvas, dimension, bitmaps, count, size, 0.15F);
    }

    public static void join(Canvas canvas, int dimension, List<Bitmap> bitmaps, float gapSize) {
        if (bitmaps != null) {
            int count = Math.min(bitmaps.size(), JoinLayout.max());
            float[] size = JoinLayout.size(count);
            join(canvas, dimension, bitmaps, count, size, gapSize);
        }
    }

    public static void join(Canvas canvas, int dimension, List<Bitmap> bitmaps, int count, float[] size, float gapSize) {
        if (bitmaps != null) {
            float[] rotation = JoinLayout.rotation(count);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Matrix matrixJoin = new Matrix();
            matrixJoin.postScale(size[0], size[0]);
            canvas.save();
            try {
                for (int index = 0; index < bitmaps.size(); index++) {
                    Bitmap bitmap = (Bitmap) bitmaps.get(index);

                    Matrix matrix = new Matrix();
                    matrix.postScale((float) dimension / (float) bitmap.getWidth(), (float) dimension / (float) bitmap.getHeight());
                    canvas.save();
                    matrix.postConcat(matrixJoin);
                    float[] offset = JoinLayout.offset(count, index, (float) dimension, size);
                    canvas.translate(offset[0], offset[1]);
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    Bitmap bitmapOk = createMaskBitmap(newBitmap, newBitmap.getWidth(), newBitmap.getHeight(), (int) rotation[index], gapSize);
                    canvas.drawBitmap(bitmapOk, 0.0F, 0.0F, paint);
                    canvas.restore();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.restore();
        }
    }

    public static Bitmap createMaskBitmap(Bitmap bitmap, int viewBoxW, int viewBoxH, int rotation, float gapSize) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            int center = Math.round((float) viewBoxW / 2.0F);
            canvas.drawCircle((float) center, (float) center, (float) center, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint);
            if (rotation != 360) {
                Matrix matrix = new Matrix();
                matrix.setRotate((float) rotation, (float) (viewBoxW / 2), (float) (viewBoxH / 2));
                canvas.setMatrix(matrix);
                paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                canvas.drawCircle((float) viewBoxW * (1.5F - gapSize), (float) center, (float) center, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps) {
        int count = Math.min(bitmaps.size(), JoinLayout.max());
        float[] size = JoinLayout.size(count);
        return createBitmap(width, height, bitmaps, count, size, 0.15F);
    }

    public static Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps, int count, float[] size) {
        return createBitmap(width, height, bitmaps, count, size, 0.15F);
    }

    public static Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps, int count, float[] size, float gapSize) {
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int dimen = Math.min(width, height);
        join(canvas, dimen, bitmaps, count, size, gapSize);
        return output;
    }
}
