package com.example.qrcode.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.qrcode.R;
import com.example.qrcode.ScannerActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by yangyu on 2017/11/27.
 */

public class DecodeUtils {
    private static final String TAG = "DecodeUtils";

    /**
     * 从开源扫码app复制来的压缩图片方法，
     */
    public static Bitmap compressPicture(Context ctx, Uri decodeUri) throws FileNotFoundException {
        // 做些预处理提升扫码成功率，
        // 预读一遍获取图片比例，使用inSampleSize压缩图片分辨率到恰到好处，
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        ContentResolver resolver = ctx.getContentResolver();

        InputStream in = null;
        try {
            in = resolver.openInputStream(decodeUri);
            BitmapFactory.decodeStream(in, null, options);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }

        int height = options.outHeight;
        int width = options.outWidth;
        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) Math.round(Math.sqrt(height * width / (double) (320 * 240)));

        in = null;
        Bitmap bitmap;
        try {
            in = resolver.openInputStream(decodeUri);
            bitmap = BitmapFactory.decodeStream(in, null, options);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.w(TAG, ioe);
                }
            }
        }
        return bitmap;
    }

    public static Result decodeFromPicture(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();
        int[] pix = new int[picWidth * picHeight];
        Log.e(TAG, "decodeFromPicture:图片大小： " + bitmap.getByteCount() / 1024 / 1024 + "M");
        bitmap.getPixels(pix, 0, picWidth, 0, 0, picWidth, picHeight);
        //构造LuminanceSource对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(picWidth
                , picHeight, pix);
        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //因为解析的条码类型是二维码，所以这边用QRCodeReader最合适。
        QRCodeReader qrCodeReader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, true);
        Result result;
        try {
            result = qrCodeReader.decode(bb, hints);
            return result;
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class DecodeAsyncTask extends AsyncTask<Bitmap, Integer, Result> {

        private WeakReference<ScannerActivity> activity;
        private Result result;

        public DecodeAsyncTask(ScannerActivity activity) {
            this.activity = new WeakReference<ScannerActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Result doInBackground(Bitmap... bitmaps) {
            result = decodeFromPicture(bitmaps[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                if (activity.get() != null) {
                    activity.get().handDecode(result);
                }
            } else {
                if (activity.get() != null) {
                    Toast.makeText(activity.get(), activity.get().getString(R.string.tip_decode_failed), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}


