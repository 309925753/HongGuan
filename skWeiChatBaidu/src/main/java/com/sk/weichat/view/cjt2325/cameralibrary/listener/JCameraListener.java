package com.sk.weichat.view.cjt2325.cameralibrary.listener;

import android.graphics.Bitmap;

public interface JCameraListener {

    void captureSuccess(Bitmap bitmap);

    void onEditClick(Bitmap bitmap);

    void recordSuccess(String url, Bitmap firstFrame);

}
