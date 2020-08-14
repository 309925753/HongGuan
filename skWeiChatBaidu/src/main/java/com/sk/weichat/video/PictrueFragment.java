package com.sk.weichat.video;


import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.event.MessageLocalVideoFile;
import com.sk.weichat.helper.CutoutHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.luo.camfilter.FilterTypeHelper;
import com.sk.weichat.luo.camfilter.GPUCamImgOperator;
import com.sk.weichat.luo.camfilter.widget.LuoGLCameraView;
import com.sk.weichat.ui.me.LocalVideoActivity;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.view.cjt2325.cameralibrary.CameraInterface;
import com.sk.weichat.view.cjt2325.cameralibrary.CaptureLayoutPictrue;
import com.sk.weichat.view.cjt2325.cameralibrary.FoucsView;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.CaptureListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.ClickListener;
import com.sk.weichat.view.cjt2325.cameralibrary.listener.TypeListener;
import com.sk.weichat.view.imageedit.IMGEditActivity;
import com.xiaojigou.luo.xjgarsdk.XJGArSdkApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictrueFragment extends Fragment implements View.OnClickListener {
    public static final int REQUEST_IMAGE_EDIT = 1;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    // 录制时长限制
    private static final int REQUEST_PERMISSION = 233;
    private final int MODE_PIC = 1;

    int handlerTime = 0;
    PictrueFilterPreviewDialog.OnUpdateFilterListener mFilterListener = new PictrueFilterPreviewDialog.OnUpdateFilterListener() {
        @Override
        public void select(GPUCamImgOperator.GPUImgFilterType type) {
            String filterName = FilterTypeHelper.FilterType2FilterName(type);
            XJGArSdkApi.XJGARSDKChangeFilter(filterName);
        }

        @Override
        public void dismiss() {
        }
    };

    PictrueFilterPreviewDialog.SeekBarFilterListener seekBarFilterListener = new PictrueFilterPreviewDialog.SeekBarFilterListener() {
        @Override
        public void FaceShapeSeek(int i) {
            XJGArSdkApi.XJGARSDKSetThinChinParam(i);
        }

        @Override
        public void BigEyeSeek(int i) {
            XJGArSdkApi.XJGARSDKSetBigEyeParam(i);
        }

        @Override
        public void skinSmoothValueBar(int i) {
            XJGArSdkApi.XJGARSDKSetSkinSmoothParam(i);
        }

        @Override
        public void skinWhitenValueBar(int i) {
            XJGArSdkApi.XJGARSDKSetWhiteSkinParam(i);
        }

        @Override
        public void redFaceValueBar(int i) {
            XJGArSdkApi.XJGARSDKSetRedFaceParam(i);
        }
    };
    boolean isClicked = false;// 标志位，已点击confirm，防止发送多张图片
    // 控件
    private ImageView mPhotoView;
    private RelativeLayout mSetRelativeLayout;
    private CaptureLayoutPictrue mCaptureLayout;
    private FoucsView mFoucsView;
    // 变量
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isTakePhoto;// 当前为 拍照 || 录像
    // 拍照显示的bitmap
    private Bitmap mCurrentBitmap;
    // 录制视频的路径
    // 录制视频的时长
    // 编辑图片之后的图片路径
    private String mEditedImagePath;
    private AlbumOrientationEventListener mAlbumOrientationEventListener;
    // 初始状态不旋转，
    private int mOrientation = 0;
    private GPUCamImgOperator GPUCamImgOperator;
    private int mode = MODE_PIC;
    private ViewGroup rlCamera;
    private LuoGLCameraView luoGLCameraView;
    private GestureDetector mGestureDetector;
    private SVCGestureListener mGestureListener = new SVCGestureListener();
    private VideoRecorderActivity.MyOnTouchListener myOnTouchListener;
    private PictrueFilterPreviewDialog dialog;
    private Bitmap bitmap;

    public PictrueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.iv_filter:
                if (UiUtils.isNormalClick(v)) {
                    dialog.show();
                    mCaptureLayout.setVisibility(View.INVISIBLE);
                    dialog.setOnDismissListener(dialog1 -> {
                        mCaptureLayout.setVisibility(View.VISIBLE);
                    });
                }
                break;
            case R.id.iv_swith:
                if (UiUtils.isNormalClick(v)) {
                    GPUCamImgOperator.switchCamera();
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pictrue, container, false);
        if (checkPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION)) {
            if (getUserVisibleHint()) {
                initView(view);
                initCamareView();
                initEvent(view);
            }
        }

        mGestureDetector = new GestureDetector(getActivity(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);

        myOnTouchListener = new VideoRecorderActivity.MyOnTouchListener() {

            @Override
            public boolean onTouch(MotionEvent ev) {
                return mGestureDetector.onTouchEvent(ev);
            }
        };
        ((VideoRecorderActivity) getActivity()).registerMyOnTouchListener(myOnTouchListener);

        luoGLCameraView.postDelayed(() -> {
            // 有可能页面已经回收了，
            if (getContext() == null) {
                return;
            }
            setFocusViewAnimation(ScreenUtil.getScreenWidth(requireContext()) / 2, ScreenUtil.getScreenHeight(requireContext()) / 2);
        }, 1000);

        return view;
    }

    public boolean setFocusViewAnimation(float x, float y) {
        if (getContext() == null) {
            return false;
        }
        if (y < mSetRelativeLayout.getBottom()) {
            return false;
        }
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mFoucsView.setVisibility(View.INVISIBLE);
            }
        }, 2000);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > ScreenUtil.getScreenWidth(requireContext()) - mFoucsView.getWidth() / 2) {
            x = ScreenUtil.getScreenWidth(requireContext()) - mFoucsView.getWidth() / 2;
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
        Rect focusRect = CameraInterface.calculateTapArea(x, y, 1f, requireContext());
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                rebuild();
            }
        } else {
            if (getView() != null) {
                release();
            }
        }
    }

    private void rebuild() {
        if (rlCamera.getChildCount() == 0) {
            luoGLCameraView = new LuoGLCameraView(requireContext());
            luoGLCameraView.setId(R.id.glsurfaceview_camera);
            rlCamera.addView(luoGLCameraView);
        }
        initCamareView();
    }

    public void setClick(boolean isClick) {
        coverClickState();
    }

    private void coverClickState() {
        // 延时200ms恢复isClick状态
        mCaptureLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 200);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageXY message) {
        Log.e("zx", "helloEventBus: X: " + message.getX() + " Y: " + message.getY());
        /*
        延时50ms执行聚焦，50ms足够触发到其他控件的onClick事件，修改isClick的状态，
        在到setFocusViewAnimation方法内根据isClick状态判断是否执行对焦
         */
        mCaptureLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 有可能页面已经回收了，
                if (getContext() == null) {
                    return;
                }
                setFocusViewAnimation(message.getX(), message.getY());
            }
        }, 50);

        coverClickState();
    }

    private void release() {
        rlCamera.removeView(luoGLCameraView);
    }

    private void initEvent(View view) {
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                Log.e("zx", "takePictures: ");
                takePhoto();
            }

            @Override
            public void recordStart() {
                isTakePhoto = false;

            }

            @Override
            public void recordShort(long time) {

            }

            @Override
            public void recordEnd(long time) {

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
                mCaptureLayout.resetCaptureLayout();
                mSetRelativeLayout.setVisibility(View.VISIBLE);
                mPhotoView.setVisibility(View.GONE);
            }

            @Override
            public void confirm() {
                if (isClicked) {
                    return;
                }
                isClicked = true;
                String path = FileUtil.saveBitmap(bitmap);
//                readPictureDegree(path);
                EventBus.getDefault().post(new MessageEventGpu(path));
                getActivity().finish();
            }
        });

        mCaptureLayout.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                getActivity().finish();
            }
        });

        mCaptureLayout.setMiddleClickListener(new ClickListener() {
            @Override
            public void onClick() {// 进行图片编辑
                String path = FileUtil.saveBitmap(mCurrentBitmap);
                if (!TextUtils.isEmpty(path)) {
                    mEditedImagePath = FileUtil.createImageFileForEdit().getAbsolutePath();
                    IMGEditActivity.startForResult(requireActivity(), Uri.fromFile(new File(path)), mEditedImagePath, REQUEST_IMAGE_EDIT);
                } else {
                    DialogHelper.tip(requireContext(), "图片编辑失败");
                }
            }
        });

        mCaptureLayout.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {// 选择本地视频
                Intent intent = new Intent(requireContext(), LocalVideoActivity.class);
                intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
                intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
                startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
            }
        });

        view.findViewById(R.id.iv_swith).setOnClickListener(this);
        view.findViewById(R.id.iv_filter).setOnClickListener(this);
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
        Log.e("zx", "readPictureDegree: " + degree);
        return degree;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        getActivity().finish();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private boolean checkPermission(String permission, int requestCode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                Log.d("home", "checkPermission:no pass!");
                requestPermission();
            } else {
                Log.d("home", "checkPermission:pass!");
            }
        }
        return true;
    }

    private boolean checkPermission() {
        Log.d("home", "checkPermission");
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("home", "CAMERA");
            return false;
        }
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("home", "WRITE_EXTERNAL_STORAGE");
            return false;
        }

        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION);

        ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.READ_CONTACTS);
    }

    private void initView(View view) {
        CutoutHelper.initCutoutHolderTop(requireActivity().getWindow(), view.findViewById(R.id.vCutoutHolder));
        EventBus.getDefault().register(this);
        mAlbumOrientationEventListener = new AlbumOrientationEventListener(requireContext(), SensorManager.SENSOR_DELAY_NORMAL);
        if (mAlbumOrientationEventListener.canDetectOrientation()) {
            mAlbumOrientationEventListener.enable();
        } else {
            Log.e("zx", "不能获取Orientation");
        }

        mPhotoView = view.findViewById(R.id.image_photo);
        mSetRelativeLayout = view.findViewById(R.id.set_rl);

        mCaptureLayout = view.findViewById(R.id.capture_layout_pictrue);
        mCaptureLayout.setButtonFeatures(0x101);  //只能拍照
        mCaptureLayout.setIconSrc(0, 0);
        mFoucsView = view.findViewById(R.id.fouce_view);
        rlCamera = view.findViewById(R.id.rlCamera);
        luoGLCameraView = view.findViewById(R.id.glsurfaceview_camera);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    private void initCamareView() {
        GPUCamImgOperator = new GPUCamImgOperator();
        GPUCamImgOperator.context = luoGLCameraView.getContext();
        GPUCamImgOperator.luoGLBaseView = luoGLCameraView;
        XJGArSdkApi.XJGARSDKSetOptimizationMode(0);
        XJGArSdkApi.XJGARSDKSetShowStickerPapers(false);
//        String licenseText = "hMPthC0oBIbtMp515TWb9jZvrLAKWIMvA4Dhf03n51QvnJr7jZowVe86d0WwU0NK9QGRFaXQn628fRu941qyr3FtsI5R7Y6v1XEpL6YvQNWQCkFEt1SAb0hyawimOYf1tfG2lIaNE63c5e+OxXssOVUWvw8tOr2glVwWVzh79NmZMahrnS8l69SoeoXLMKCYlvAt/qJFFk4+6Aq3QvOv3o72fq5p90yty+YWg7o0HirZpMSP9P5/DHYPFqR/ud7twTJ+Yo2+ZzYvodqRQbGG0HseZn8Xpt7fZdFuZbc2HGRMVk56vNDMRlcGZZXAjENk7m2UMhi1ohhuSf4WmIgXCZFiJXvYFByaY625gXKtEI7+b7t81nWQYHP9BEbzURwL";
//        XJGArSdkApi.XJGARSDKInitialization(this, licenseText, "DoctorLuoInvitedUser:teacherluo", "LuoInvitedCompany:www.xiaojigou.cn");
        String licenseText = "hMPthC0oBIbtMp515TWb9jZvrLAKWIMvA4Dhf03n51QvnJr7jZowVe86d0WwU0NK9QGRFaXQn628fRu941qyr3FtsI5R7Y6v1XEpL6YvQNWQCkFEt1SAb0hyawimOYf1tfG2lIaNE63c5e+OxXssOVUWvw8tOr2glVwWVzh79NmZMahrnS8l69SoeoXLMKCYlvAt/qJFFk4+6Aq3QvOv3o72fq5p90yty+YWg7o0HirZpMSP9P5/DHYPFqR/ud7twTJ+Yo2+ZzYvodqRQbGG0HseZn8Xpt7fZdFuZbc2HGRMVk56vNDMRlcGZZXAjENk7m2UMhi1ohhuSf4WmIgXCZFiJXvYFByaY625gXKtEI7+b7t81nWQYHP9BEbzURwL";
        XJGArSdkApi.XJGARSDKInitialization(requireContext(), licenseText, "DoctorLuoInvitedUser:user0423", "LuoInvitedCompany:xiaojigou");
        dialog = new PictrueFilterPreviewDialog(requireContext(), mFilterListener, seekBarFilterListener);

        Point screenSize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) luoGLCameraView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.y;  //screenSize.x * 4 / 3;
        luoGLCameraView.setLayoutParams(params);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.e("home", "相机权限申请被拒绝");
                getActivity().finish();
            }
        } else if (grantResults.length != 1 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mode == MODE_PIC)
                takePhoto();
//            else
//                takeVideo();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void takePhoto() {
        GPUCamImgOperator.savePicture();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessagePhoto message) {
        bitmap = message.getBitmap();
        if (bitmap != null) {
            playPhoto(bitmap);
//            Log.e("zx", "helloEventBus: "+readPictureDegree() );
        }
    }

    /**
     * 预览图片
     *
     * @param bitmap
     */
    private void playPhoto(Bitmap bitmap) {
        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator();
        mSetRelativeLayout.setVisibility(View.GONE);
        mPhotoView.setImageBitmap(bitmap);
        mPhotoView.setVisibility(View.VISIBLE);
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

    public class SVCGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            EventBus.getDefault().post(new MessageXY(e.getX(), e.getY()));
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }
}
