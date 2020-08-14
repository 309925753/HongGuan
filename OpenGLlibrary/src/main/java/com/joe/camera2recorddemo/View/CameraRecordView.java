package com.joe.camera2recorddemo.View;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.joe.camera2recorddemo.Entity.SizeInfo;
import com.joe.camera2recorddemo.OpenGL.CameraRecorder;
import com.joe.camera2recorddemo.OpenGL.Filter.Mp4EditFilter;
import com.joe.camera2recorddemo.OpenGL.Renderer;
import com.joe.camera2recorddemo.Utils.CameraParamUtil;
import com.joe.camera2recorddemo.Utils.MatrixUtils;

import java.io.IOException;

public class CameraRecordView extends TextureView implements Renderer {

    private static final int STATE_INIT = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_PAUSE = 2;
    private int mRecorderState;
    // 摄像头
    private Camera mCamera;
    private Camera.Parameters mParams;
    private float screenProp;
    private int mCurrentCameraState = 0;

    private CameraRecorder mCameraRecord;
    private Mp4EditFilter mFilter;
    private int mCurrentFilterIndex;// 当前滤镜

    private int mCameraWidth;
    private int mCameraHeight;
    // private SizeInfo recordSize;

    public CameraRecordView(Context context) {
        this(context, null);
    }

    public CameraRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mFilter = new Mp4EditFilter(getResources());
        mCameraRecord = new CameraRecorder();

        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCamera = Camera.open(0);
                screenProp = (float) height / (float) width;
                initCamera(screenProp);

                mCameraRecord.setOutputSurface(new Surface(surface));
                Camera.Size videoSize;
                if (mParams.getSupportedVideoSizes() == null) {
                    videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), 600,
                            screenProp);
                } else {
                    videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedVideoSizes(), 600,
                            screenProp);
                }
                SizeInfo sizeInfo;
                if (videoSize.width == videoSize.height) {
                    sizeInfo = new SizeInfo(720, 720);
                } else {
                    sizeInfo = new SizeInfo(videoSize.height, videoSize.width);
                }
                mCameraRecord.setOutputSize(sizeInfo);
                mCameraRecord.setRenderer(CameraRecordView.this);
                mCameraRecord.setPreviewSize(width, height);
                mCameraRecord.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mCameraRecord.setPreviewSize(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // 停止录制
                if (mRecorderState == STATE_RECORDING) {
                    try {
                        stopRecord();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopPreview();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public void create() {
        try {
            mCamera.setPreviewTexture(mCameraRecord.createInputSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Size mSize = mCamera.getParameters().getPreviewSize();
        mCameraWidth = mSize.height;
        mCameraHeight = mSize.width;

        mCamera.startPreview();
        mFilter.create();
    }

    @Override
    public void sizeChanged(int width, int height) {
        mFilter.sizeChanged(width, height);
        MatrixUtils.getMatrix(mFilter.getVertexMatrix(), MatrixUtils.TYPE_CENTERCROP,
                mCameraWidth, mCameraHeight, width, height);
        MatrixUtils.flip(mFilter.getVertexMatrix(), false, true);
    }

    @Override
    public void draw(int texture) {
        mFilter.draw(texture);
    }

    @Override
    public void destroy() {
        mFilter.destroy();
    }

    public void initCamera(float screenProp) {
        if (mCamera != null) {
            mParams = mCamera.getParameters();
            Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParams
                    .getSupportedPreviewSizes(), 1000, screenProp);
            Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParams
                    .getSupportedPictureSizes(), 1200, screenProp);
            mParams.setPreviewSize(previewSize.width, previewSize.height);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            if (CameraParamUtil.getInstance().isSupportedFocusMode(
                    mParams.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_AUTO)) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                    ImageFormat.JPEG)) {
                mParams.setPictureFormat(ImageFormat.JPEG);
                mParams.setJpegQuality(100);
            }
            mCamera.setParameters(mParams);
            mParams = mCamera.getParameters();
        }
    }

    public void switchFilter(int index) {
        if (mCurrentFilterIndex != index) {
            mCurrentFilterIndex = index;
            mFilter.getChooseFilter().setChangeType(mCurrentFilterIndex);
        }
    }

    public void switchCamera() {
        if (Camera.getNumberOfCameras() > 1) {
            stopPreview();
            mCurrentCameraState += 1;
            if (mCurrentCameraState > Camera.getNumberOfCameras() - 1)
                mCurrentCameraState = 0;
            mCamera = Camera.open(mCurrentCameraState);
            initCamera(screenProp);// 切换摄像头之后需要重新setParameters
/*
            if (mParams == null) {
                initCamera(screenProp);
            } else {
                // 部分机型 java.lang.RuntimeException: setParameters failed
               mCamera.setParameters(mParams);
            }
*/
            mCameraRecord.startPreview();
        }
    }

    public void stopPreview() {
        try {
            mCameraRecord.stopPreview();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开始录制
     */
    public void startRecord(String filePath) throws IOException {
        mCameraRecord.setOutputPath(filePath);
        mCameraRecord.startRecord();

        mRecorderState = STATE_RECORDING;
    }

    /**
     * 停止录制
     */
    public void stopRecord() throws InterruptedException {
        mCameraRecord.stopRecord();

        mRecorderState = STATE_INIT;
    }

    public Camera getCamera() {
        return mCamera;
    }
}
