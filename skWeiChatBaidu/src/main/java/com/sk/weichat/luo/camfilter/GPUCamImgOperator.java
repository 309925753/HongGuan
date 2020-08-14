package com.sk.weichat.luo.camfilter;

import android.content.Context;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.sk.weichat.luo.camfilter.widget.LuoGLBaseView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class GPUCamImgOperator {

    public static Context context;
    public static LuoGLBaseView luoGLBaseView;

    public GPUCamImgOperator() {
    }

    public void savePicture() {
        SavePictureTask savePictureTask = new SavePictureTask(getOutputMediaFile(), null);
        luoGLBaseView.savePicture(savePictureTask);
    }

    public void startRecord() {

    }

    public void stopRecord() {

    }

    public void switchCamera() {
        CameraEngine.switchCamera();
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "XJGArSdkDemo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        readPictureDegree(String.valueOf(mediaFile));
        return mediaFile;
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.i("zx", "读取角度-" + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public enum GPUImgFilterType {
        NONE,
        COOL,
        HEALTHY,
        EMERALD,
        NOSTALGIA,
        CRAYON,
        EVERGREEN
    }

}
