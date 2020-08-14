package com.sk.weichat.luo.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.luo.camfilter.FilterRecyclerViewAdapter;
import com.sk.weichat.luo.camfilter.FilterTypeHelper;
import com.sk.weichat.luo.camfilter.GPUCamImgOperator;
import com.sk.weichat.luo.camfilter.widget.LuoGLCameraView;
import com.xiaojigou.luo.xjgarsdk.XJGArSdkApi;

//import com.xiaojigou.luo.faceEff.R;

/**
 * Created by jianxin luo on 2017/10/7.
 */
public class CameraWithFilterActivity extends Activity {
    private static final int REQUEST_PERMISSION = 233;
    static boolean bShowFaceSurgery = false;
    static boolean bShowImgFilters = false;
    private final int MODE_PIC = 1;
    private final int MODE_VIDEO = 2;
    private final com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType[] types = new GPUCamImgOperator.GPUImgFilterType[]{
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.NONE,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.HEALTHY,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.NOSTALGIA,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.COOL,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.EMERALD,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.EVERGREEN,
            com.sk.weichat.luo.camfilter.GPUCamImgOperator.GPUImgFilterType.CRAYON
    };
    protected SeekBar mFaceSurgeryFaceShapeSeek;
    protected SeekBar mFaceSurgeryBigEyeSeek;
    protected SeekBar mSkinSmoothSeek;
    protected SeekBar mSkinWihtenSeek;
    protected SeekBar mRedFaceSeek;
    private LinearLayout mFilterLayout;
    private LinearLayout mFaceSurgeryLayout;
    private RecyclerView mMenuView;
    private RecyclerView mFilterListView;
    private FilterRecyclerViewAdapter mAdapter;
    private GPUCamImgOperator GPUCamImgOperator;
    private boolean isRecording = false;
    private int mode = MODE_PIC;
    private ImageView btn_shutter;
    private ImageView btn_mode;
    private ObjectAnimator animator;
    private FilterRecyclerViewAdapter.onFilterChangeListener onFilterChangeListener = (filterType) -> {

        String filterName = FilterTypeHelper.FilterType2FilterName(filterType);
        XJGArSdkApi.XJGARSDKChangeFilter(filterName);
    };
    private View.OnClickListener btn_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int buttonId = v.getId();
            if (buttonId == R.id.btn_camera_mode) {
                switchMode();
            } else if (buttonId == R.id.btn_camera_shutter) {
                if (PermissionChecker.checkSelfPermission(CameraWithFilterActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(CameraWithFilterActivity.this, new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            v.getId());
                } else {
                    if (mode == MODE_PIC)
                        takePhoto();
                    else
                        takeVideo();
                }
            } else if (buttonId == R.id.btn_camera_filter) {
                bShowImgFilters = !bShowImgFilters;
                if (bShowImgFilters)
                    showFilters();
                else
                    hideFilters();
            } else if (buttonId == R.id.btn_camera_switch) {
                GPUCamImgOperator.switchCamera();
            } else if (buttonId == R.id.btn_camera_beauty) {
                bShowFaceSurgery = !bShowFaceSurgery;
                if (bShowFaceSurgery)
                    showFaceSurgery();
                else
                    hideFaceSurgery();
            } else if (buttonId == R.id.btn_camera_closefilter) {
                if (bShowImgFilters) {
                    hideFilters();
                    bShowImgFilters = false;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION)) {
            initView();
        } else {
            finish();
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("home", "CAMERA");
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("home", "WRITE_EXTERNAL_STORAGE");
            return false;
        }

        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION);

        ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS);
    }

    private void initView() {
        setContentView(R.layout.activity_camera_with_filter);
        GPUCamImgOperator = new GPUCamImgOperator();
        LuoGLCameraView luoGLCameraView = (LuoGLCameraView) findViewById(R.id.glsurfaceview_camera);
        GPUCamImgOperator.context = luoGLCameraView.getContext();
        GPUCamImgOperator.luoGLBaseView = luoGLCameraView;
        XJGArSdkApi.XJGARSDKSetOptimizationMode(2);
        XJGArSdkApi.XJGARSDKSetShowStickerPapers(false);
//        String licenseText = "hMPthC0oBIbtMp515TWb9jZvrLAKWIMvA4Dhf03n51QvnJr7jZowVe86d0WwU0NK9QGRFaXQn628fRu941qyr3FtsI5R7Y6v1XEpL6YvQNWQCkFEt1SAb0hyawimOYf1tfG2lIaNE63c5e+OxXssOVUWvw8tOr2glVwWVzh79NmZMahrnS8l69SoeoXLMKCYlvAt/qJFFk4+6Aq3QvOv3o72fq5p90yty+YWg7o0HirZpMSP9P5/DHYPFqR/ud7twTJ+Yo2+ZzYvodqRQbGG0HseZn8Xpt7fZdFuZbc2HGRMVk56vNDMRlcGZZXAjENk7m2UMhi1ohhuSf4WmIgXCZFiJXvYFByaY625gXKtEI7+b7t81nWQYHP9BEbzURwL";
//        XJGArSdkApi.XJGARSDKInitialization(this, licenseText, "DoctorLuoInvitedUser:teacherluo", "LuoInvitedCompany:www.xiaojigou.cn");
        String licenseText = "hMPthC0oBIbtMp515TWb9jZvrLAKWIMvA4Dhf03n51QvnJr7jZowVe86d0WwU0NK9QGRFaXQn628fRu941qyr3FtsI5R7Y6v1XEpL6YvQNWQCkFEt1SAb0hyawimOYf1tfG2lIaNE63c5e+OxXssOVUWvw8tOr2glVwWVzh79NmZMahrnS8l69SoeoXLMKCYlvAt/qJFFk4+6Aq3QvOv3o72fq5p90yty+YWg7o0HirZpMSP9P5/DHYPFqR/ud7twTJ+Yo2+ZzYvodqRQbGG0HseZn8Xpt7fZdFuZbc2HGRMVk56vNDMRlcGZZXAjENk7m2UMhi1ohhuSf4WmIgXCZFiJXvYFByaY625gXKtEI7+b7t81nWQYHP9BEbzURwL";
        XJGArSdkApi.XJGARSDKInitialization(this, licenseText, "DoctorLuoInvitedUser:user0423", "LuoInvitedCompany:xiaojigou");

        mFilterLayout = (LinearLayout) findViewById(R.id.layout_filter);

        mFaceSurgeryLayout = (LinearLayout) findViewById(R.id.layout_facesurgery);
        mFaceSurgeryFaceShapeSeek = (SeekBar) findViewById(R.id.faceShapeValueBar);
        mFaceSurgeryFaceShapeSeek.setProgress(20);
        mFaceSurgeryBigEyeSeek = (SeekBar) findViewById(R.id.bigeyeValueBar);
        mFaceSurgeryBigEyeSeek.setProgress(50);

        mSkinSmoothSeek = (SeekBar) findViewById(R.id.skinSmoothValueBar);
        mSkinSmoothSeek.setProgress(100);
        mSkinWihtenSeek = (SeekBar) findViewById(R.id.skinWhitenValueBar);
        mSkinWihtenSeek.setProgress(20);
        mRedFaceSeek = (SeekBar) findViewById(R.id.redFaceValueBar);
        mRedFaceSeek.setProgress(80);
        XJGArSdkApi.XJGARSDKSetSkinSmoothParam(100);
        XJGArSdkApi.XJGARSDKSetWhiteSkinParam(20);
        XJGArSdkApi.XJGARSDKSetRedFaceParam(80);
        mFaceSurgeryFaceShapeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int strength = value;//(int)(value*(float)1.0/100);
                XJGArSdkApi.XJGARSDKSetThinChinParam(strength);
            }
        });
        mFaceSurgeryBigEyeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int strength = value;//(int)(value*(float)1.0/100);
                XJGArSdkApi.XJGARSDKSetBigEyeParam(strength);
            }
        });
        mSkinSmoothSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int level = value;//(int)(value/18);
                XJGArSdkApi.XJGARSDKSetSkinSmoothParam(level);
//                GPUCamImgOperator.setBeautyLevel(level);
            }
        });

        mSkinWihtenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int level = value;//(int)(value/18);
                XJGArSdkApi.XJGARSDKSetWhiteSkinParam(level);
//                GPUCamImgOperator.setBeautyLevel(level);
            }
        });
        mRedFaceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public int value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int level = value;//(int)(value/18);
                XJGArSdkApi.XJGARSDKSetRedFaceParam(level);
//                GPUCamImgOperator.setBeautyLevel(level);
            }
        });

        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);

        btn_shutter = (ImageView) findViewById(R.id.btn_camera_shutter);
        btn_mode = (ImageView) findViewById(R.id.btn_camera_mode);

        findViewById(R.id.btn_camera_filter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_closefilter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_shutter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_switch).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_mode).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_beauty).setOnClickListener(btn_listener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterRecyclerViewAdapter(this, types);
        mFilterListView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(onFilterChangeListener);

        animator = ObjectAnimator.ofFloat(btn_shutter, "rotation", 0, 360);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        LuoGLCameraView cameraView = (LuoGLCameraView) findViewById(R.id.glsurfaceview_camera);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.y;//screenSize.x * 4 / 3;
        cameraView.setLayoutParams(params);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.e("home", "相机权限申请被拒绝");
                finish();
            }
        } else if (grantResults.length != 1 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mode == MODE_PIC)
                takePhoto();
            else
                takeVideo();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void switchMode() {
        if (mode == MODE_PIC) {
            mode = MODE_VIDEO;
            btn_mode.setImageResource(R.drawable.icon_camera);
        } else {
            mode = MODE_PIC;
            btn_mode.setImageResource(R.drawable.icon_video);
        }
    }

    private void takePhoto() {
        GPUCamImgOperator.savePicture();
    }

    private void takeVideo() {
        if (isRecording) {
            animator.end();
            GPUCamImgOperator.stopRecord();
        } else {
            animator.start();
            GPUCamImgOperator.startRecord();
        }
        isRecording = !isRecording;
    }

    //显示面部整形
    private void showFaceSurgery() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFaceSurgeryLayout, "translationY", mFaceSurgeryLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                findViewById(R.id.btn_camera_shutter).setClickable(false);
                mFaceSurgeryLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();

    }

    //隐藏面部整形
    private void hideFaceSurgery() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFaceSurgeryLayout, "translationY", 0, mFaceSurgeryLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mFaceSurgeryLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                mFaceSurgeryLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }
        });
        animator.start();
    }

    private void showFilters() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                findViewById(R.id.btn_camera_shutter).setClickable(false);
                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideFilters() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0, mFilterLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }
        });
        animator.start();
    }
}
