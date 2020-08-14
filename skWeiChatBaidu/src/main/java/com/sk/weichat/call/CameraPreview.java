package com.sk.weichat.call;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2016/5/9.
 */
public class CameraPreview implements SurfaceHolder.Callback {
    private Camera mCamera;
    private int mCameraId = 0;
    private SurfaceHolder mHolder;

    public CameraPreview(SurfaceView surfaceView) {
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
    }

    private void openCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount > 1) {
            mCameraId = 1;// 前置摄像头
        }

        try {
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            mCamera = null;
        }

        if (mCamera == null) {
            return;
        }

        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("jitsi", "holder created");
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            Log.e("jitsi", "holder destroyed");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
