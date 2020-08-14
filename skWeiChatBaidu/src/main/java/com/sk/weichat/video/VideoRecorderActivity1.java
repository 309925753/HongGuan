package com.sk.weichat.video;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.joe.camera2recorddemo.Entity.SizeInfo;
import com.joe.camera2recorddemo.OpenGL.CameraRecorder;
import com.joe.camera2recorddemo.OpenGL.Filter.Mp4EditFilter;
import com.joe.camera2recorddemo.OpenGL.Renderer;
import com.joe.camera2recorddemo.Utils.MatrixUtils;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.event.MessageLocalVideoFile;
import com.sk.weichat.bean.event.MessageVideoFile;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.me.LocalVideoActivity;
import com.sk.weichat.util.CameraUtil;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.RecorderUtils;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.VideoCompressUtil;
import com.sk.weichat.view.MyVideoView;
import com.sk.weichat.view.cjt2325.cameralibrary.CameraInterface;
import com.sk.weichat.view.cjt2325.cameralibrary.CaptureLayout;
import com.sk.weichat.view.cjt2325.cameralibrary.FoucsView;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.CaptureListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.ClickListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.TypeListener;
import com.sk.weichat.view.cjt2325.cameralibrary.util.CameraParamUtil;
import com.sk.weichat.view.imageedit.IMGEditActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Jni.VideoUitls;
import VideoHandle.OnEditorListener;
import de.greenrobot.event.EventBus;

/**
 * 聊天界面视频录制类
 * create by TAG
 * update time 2018-11-21 19:43:13
 */

public class VideoRecorderActivity1 extends BaseActivity implements View.OnClickListener, Renderer {
    public static final int REQUEST_IMAGE_EDIT = 1;
    private static final String TAG = "VideoRecorderActivity";
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    // 录制时长限制
    private static final int mRecordMaxTime = 10 * 1000;
    private static final int mRecordMinTime = 1000;
    public int mCameraWidth, mCameraHeight;
    int handlerTime = 0;
    // 控件
    private TextureView mTextureView;
    private ImageView mPhotoView;
    private MyVideoView mVideoView;
    private RelativeLayout mSetRelativeLayout;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    // 变量
    private Camera mCamera;
    private Camera.Parameters mParams;
    private float screenProp;
    private int mCurrentCameraState;
    private boolean isTakePhoto;// 当前为 拍照 || 录像
    private boolean isRecord;
    // 拍照显示的bitmap
    private Bitmap mCurrentBitmap;
    // 编辑图片之后的图片路径
    private String mEditedImagePath;
    // 录制视频的路径
    private String mCurrentVideoPath;
    // 录制视频的时长
    private int mCurrentTime;
    private CameraRecorder mCameraRecord;
    private Mp4EditFilter mFilter;
    FilterPreviewDialog.OnUpdateFilterListener mFilterListener = new FilterPreviewDialog.OnUpdateFilterListener() {
        @Override
        public void select(int type) {
            mFilter.getChooseFilter().setChangeType(type);
        }

        @Override
        public void dismiss() {
        }
    };
    private FilterPreviewDialog mFilterDialog;
    private AlbumOrientationEventListener mAlbumOrientationEventListener;
    // 初始状态不旋转，
    private int mOrientation = 0;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        initView();
        initRecorder();
        initEvent();

        mTextureView.postDelayed(() -> setFocusViewAnimation(ScreenUtil.getScreenWidth(mContext) / 2, ScreenUtil.getScreenHeight(mContext) / 2), 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumOrientationEventListener.disable();
    }

    private void initView() {
        mAlbumOrientationEventListener = new AlbumOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
        if (mAlbumOrientationEventListener.canDetectOrientation()) {
            mAlbumOrientationEventListener.enable();
        } else {
            Log.e("zx", "不能获取Orientation");
        }

        mTextureView = findViewById(R.id.mTexture);

        mPhotoView = findViewById(R.id.image_photo);
        mVideoView = findViewById(R.id.video_preview);

        mSetRelativeLayout = findViewById(R.id.set_rl);

        mCaptureLayout = findViewById(R.id.capture_layout);
        mCaptureLayout.setIconSrc(0, R.drawable.ic_sel_local_video);
        mFoucsView = findViewById(R.id.fouce_view);
    }

    private void initRecorder() {
        mFilter = new Mp4EditFilter(getResources());
        mFilterDialog = new FilterPreviewDialog(this, mFilterListener);

        mCameraRecord = new CameraRecorder();
        mCurrentVideoPath = RecorderUtils.getVideoFileByTime();
        mCameraRecord.setOutputPath(mCurrentVideoPath);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

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
                mCameraRecord.setRenderer(VideoRecorderActivity1.this);
                mCameraRecord.setPreviewSize(width, height);
                mCameraRecord.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mCameraRecord.setPreviewSize(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (isRecord) {
                    isRecord = false;
                    try {
                        mCameraRecord.stopRecord();
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

    private void initEvent() {
        mCaptureLayout.setDuration(mRecordMaxTime);
        mCaptureLayout.setMinDuration(mRecordMinTime);
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                isTakePhoto = true;
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        mCurrentBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Camera.CameraInfo info = new Camera.CameraInfo();
                        Camera.getCameraInfo(mCurrentCameraState, info);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // 后置摄像头拍摄出来的照片需要旋转90'
                            mCurrentBitmap = CameraUtil.restoreRotatedImage(info.orientation, mCurrentBitmap);
                        } else {// 前置摄像头拍出的图片需要先旋转270',在左右翻转一次
                            mCurrentBitmap = CameraUtil.restoreRotatedImage(info.orientation, mCurrentBitmap);
                            mCurrentBitmap = CameraUtil.turnCurrentLayer(mCurrentBitmap, -1, 1);
                        }
                        mCurrentBitmap = CameraUtil.restoreRotatedImage(mOrientation, mCurrentBitmap);
                        playPhoto();
                        // 继续进行预览
                        mCamera.startPreview();
                    }
                });
            }

            @Override
            public void recordStart() {
                isTakePhoto = false;
                // 开始录制视频
                if (startRecord(mCurrentVideoPath)) {
                    isRecord = true;
                    mCurrentTime = 0;
                }
            }

            @Override
            public void recordShort(long time) {
                mCaptureLayout.setTextWithAnimation(getString(R.string.tip_record_too_short));
                mTextureView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (stopRecord()) {
                            isRecord = false;
                            mCurrentTime = 0;
                            mCaptureLayout.resetCaptureLayout();
                        }
                    }
                }, mRecordMinTime - time);
            }

            @Override
            public void recordEnd(long time) {
                if (stopRecord()) {
                    isRecord = false;
                    mCurrentTime = (int) (time / 1000);
                    playVideo();
                }
            }

            @Override
            public void recordZoom(float zoom) {
                // 摄像头缩放
            }

            @Override
            public void recordError() {

            }
        });

        //确认 取消
        mCaptureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {
                reset();
            }

            @Override
            public void confirm() {
                complete();
            }
        });

        mCaptureLayout.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });

        mCaptureLayout.setMiddleClickListener(new ClickListener() {
            @Override
            public void onClick() {// 进行图片编辑
                String path = FileUtil.saveBitmap(mCurrentBitmap);
                if (!TextUtils.isEmpty(path)) {
                    mEditedImagePath = FileUtil.createImageFileForEdit().getAbsolutePath();
                    IMGEditActivity.startForResult(VideoRecorderActivity1.this, Uri.fromFile(new File(path)), mEditedImagePath, REQUEST_IMAGE_EDIT);
                } else {
                    DialogHelper.tip(VideoRecorderActivity1.this, "图片编辑失败");
                }
            }
        });

        mCaptureLayout.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {// 选择本地视频
                Intent intent = new Intent(VideoRecorderActivity1.this, LocalVideoActivity.class);
                intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
                intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
            }
        });

        findViewById(R.id.iv_swith).setOnClickListener(this);
        findViewById(R.id.iv_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.iv_filter:
                Toast.makeText(this, getString(R.string.tip_photo_filter_not_supported), Toast.LENGTH_SHORT).show();
                mFilterDialog.show();
                break;
            case R.id.iv_swith:
                changeCamera();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    // 图片编辑返回
                    mCurrentBitmap = BitmapFactory.decodeFile(mEditedImagePath);
                    mPhotoView.setImageBitmap(mCurrentBitmap);
                    break;
                case REQUEST_CODE_SELECT_VIDEO:
                    // 选择视频返回
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // 不可到达，列表里有做判断，
                        Reporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // 不可到达，列表里有做过滤，
                                Reporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // 不可到达，列表里有做过滤，
                                    Reporter.unreachable();
                                } else {
                                    EventBus.getDefault().post(new MessageLocalVideoFile(file));
                                }
                            }
                        }
                        finish();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
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

    /**
     * 停止摄像头预览
     */
    private void stopPreview() {
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
    private boolean startRecord(String path) {
        try {
            Log.e(TAG, "开始录制：" + path);
            mCameraRecord.startRecord();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 结束录制
     */
    private boolean stopRecord() {
        try {
            mCameraRecord.stopRecord();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    预览图片
     */
    private void playPhoto() {
        mSetRelativeLayout.setVisibility(View.GONE);
        mPhotoView.setImageBitmap(mCurrentBitmap);
        mPhotoView.setVisibility(View.VISIBLE);

        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator();
    }

    /*
    预览视频
     */
    private void playVideo() {
        mSetRelativeLayout.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoPath(mCurrentVideoPath);
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {// 循环播放
                mVideoView.start();
            }
        });
        mVideoView.start();
    }

    private void reset() {
        mSetRelativeLayout.setVisibility(View.VISIBLE);
        if (isTakePhoto) {
            mPhotoView.setVisibility(View.GONE);
        } else {
            mVideoView.stopPlayback();// 停止播放 并释放资源
            mVideoView.setVisibility(View.GONE);
        }

        mCaptureLayout.resetCaptureLayout();
    }

    private void complete() {
        if (isTakePhoto) {
            String path = FileUtil.saveBitmap(mCurrentBitmap);
            EventBus.getDefault().post(new MessageEventGpu(path));
            finish();
        } else {
            compress(mCurrentVideoPath);
        }
    }

    private void compress(String path) {
        DialogHelper.showMessageProgressDialog(this, MyApplication.getContext().getString(R.string.compressed));
        final String out = RecorderUtils.getVideoFileByTime();
        String[] cmds = RecorderUtils.ffmpegComprerssCmd(path, out);
        long duration = VideoUitls.getDuration(path);

        VideoCompressUtil.exec(cmds, duration, new OnEditorListener() {
            public void onSuccess() {
                DialogHelper.dismissProgressDialog();
                mCurrentVideoPath = out;
                EventBus.getDefault().post(new MessageVideoFile(mCurrentTime,
                        new File(mCurrentVideoPath).length(), mCurrentVideoPath));
                finish();
            }

            public void onFailure() {
                DialogHelper.dismissProgressDialog();
                finish();
            }

            public void onProgress(float progress) {
            }
        });
    }

    /*****
     * 拓展 手动聚焦
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    // 显示对焦指示器
                    setFocusViewAnimation(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public boolean setFocusViewAnimation(float x, float y) {
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(View.VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > ScreenUtil.getScreenWidth(this) - mFoucsView.getWidth() / 2) {
            x = ScreenUtil.getScreenWidth(this) - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();

        handleFocus(x, y);
        return true;
    }

    public void handleFocus(final float x, final float y) {
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = CameraInterface.calculateTapArea(x, y, 1f, this);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            mFoucsView.setVisibility(View.INVISIBLE);
            return;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus((success, camera) -> {
                if (success || handlerTime > 10) {
                    Camera.Parameters params1 = camera.getParameters();
                    params1.setFocusMode(currentFocusMode);
                    camera.setParameters(params1);
                    handlerTime = 0;
                    mFoucsView.setVisibility(View.INVISIBLE);
                } else {
                    handlerTime++;
                    handleFocus(x, y);
                }
            });
        } catch (Exception e) {

        }
    }

    private class AlbumOrientationEventListener extends OrientationEventListener {
        public AlbumOrientationEventListener(Context context) {
            super(context);
        }

        public AlbumOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            //保证只返回四个方向
            int newOrientation = ((orientation + 45) / 90 * 90) % 360;

            if (newOrientation != mOrientation) {
                mOrientation = newOrientation;
                Log.e("zx", "onOrientationChanged: " + mOrientation);
                //返回的mOrientation就是手机方向，为0°、90°、180°和270°中的一个

            }
        }
    }
}
