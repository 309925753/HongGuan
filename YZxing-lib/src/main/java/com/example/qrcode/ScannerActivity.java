package com.example.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrcode.camera.CameraManager;
import com.example.qrcode.decode.InactivityTimer;
import com.example.qrcode.decode.ScannerHandler;
import com.example.qrcode.utils.DecodeUtils;
import com.example.qrcode.utils.NetUtil;
import com.example.qrcode.utils.UriUtils;
import com.example.qrcode.view.ScannerView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;


/**
 * Created by yangyu on 17/10/18.
 */

public class ScannerActivity extends SetActionBarActivity implements SurfaceHolder.Callback {
    public static final String BARCODE_FORMAT = "support_barcode_format";
    private static final String TAG = "ScannerActivity";
    public final int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0X11;
    public final int REQUEST_CODE_GET_PIC_URI = 0X12;
    private final int MESSAGE_DECODE_FROM_BITMAP = 0;
    Bitmap bitmap;
    private ScannerView mScannerView;
    private SurfaceView mSurfaceView;
    private InactivityTimer mInactivityTimer;
    private BeepManager beepManager;
    private com.example.qrcode.camera.CameraManager cameraManager;
    private ScannerHandler handler;
    private Collection<BarcodeFormat> decodeFormats;
    private int mScanFocusWidth;
    private int mScanFocusHeight;
    private int mScanFocusTopPadding;
    private boolean isEnableScanFromPicture;
    private boolean hasSurface;
    private MyHandler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_scanner);
        initView();
        hasSurface = false;
        Intent intent = getIntent();
        if (intent != null) {
            mScanFocusWidth = intent.getIntExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, -1);
            mScanFocusHeight = intent.getIntExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, -1);
            mScanFocusTopPadding = intent.getIntExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, -1);
            isEnableScanFromPicture = intent.getBooleanExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, false);
            Bundle b = intent.getExtras();
            if (b != null) {
                HashMap<String, Set> formats = (HashMap<String, Set>) b.getSerializable(Constant.EXTRA_SCAN_CODE_TYPE);
                if (formats != null) {
                    decodeFormats = formats.get(BARCODE_FORMAT);
                } else {
                    decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE
                            , BarcodeFormat.CODE_128);
                }
            } else {
                decodeFormats = EnumSet.of(BarcodeFormat.QR_CODE
                        , BarcodeFormat.CODE_128);
            }
        }
        Log.e(TAG, "onCreate:decodeFormats :" + decodeFormats.size() + "--" + decodeFormats.toString());
        mInactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        mHandler = new MyHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(this);
        cameraManager.setManualFramingRect(mScanFocusWidth, mScanFocusHeight, mScanFocusTopPadding);
        mScannerView.setCameraManager(cameraManager);
        SurfaceHolder holder = mSurfaceView.getHolder();

        if (hasSurface) {
            initCamera(holder);
        } else {
            holder.addCallback(this);
        }
        mInactivityTimer.onResume();
        beepManager.updatePrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        mInactivityTimer.onPause();
        beepManager.close();
    }

    @Override
    protected void onDestroy() {
        cameraManager.clearFramingRect();
        mInactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //关闭灯光
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                //开启闪光灯
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GET_PIC_URI);
    }

    private void initView() {
        TextView tv_title_center = findViewById(R.id.tv_title_center);
        tv_title_center.setText(getString(R.string.title_qr_code_or_banner_code));
        tv_title_center.setTextColor(Color.BLACK);
        findViewById(R.id.tv_title_right).setOnClickListener(new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                     int checked = ContextCompat.checkSelfPermission(ScannerActivity.this
                                                                             , Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                                                     if (checked == PackageManager.PERMISSION_GRANTED) {
                                                                         goPicture();
                                                                     } else {
                                                                         ActivityCompat.requestPermissions(ScannerActivity.this
                                                                                 , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                                                                     }
                                                                 }
                                                             }
        );

        ImageView iv_title_left_first = findViewById(R.id.iv_title_left_first);
        iv_title_left_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_title_left_first.setImageDrawable(getResources().getDrawable(R.mipmap.return_icon));
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScannerView = (ScannerView) findViewById(R.id.scan_view);
        if (getIntent() != null) {
            byte[] bytes = getIntent().getByteArrayExtra(Constant.EXTRA_SELF_QR_CODE_BITMAP);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ImageView imageView = findViewById(R.id.code_iv);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new ScannerHandler(this, decodeFormats, "utf-8", cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    // 在这里处理扫码结果
    public void handDecode(final Result result) {
        if (!NetUtil.isGprsOrWifiConnected(this)) {
            Toast.makeText(this, getString(R.string.check_net), Toast.LENGTH_SHORT).show();
            return;
        }
        mInactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        Intent data = new Intent();
        BarcodeFormat format = result.getBarcodeFormat();
        String type = format.toString();
        data.putExtra(Constant.EXTRA_RESULT_CODE_TYPE, type);
        data.putExtra(Constant.EXTRA_RESULT_CONTENT, result.getText());
        setResult(RESULT_OK, data);
        finish();
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GET_PIC_URI:
                    Uri uri = data.getData();
                    Log.e(TAG, "onActivityResult: uri:" + Objects.requireNonNull(uri).toString());
                    String imagePath = UriUtils.getPicturePathFromUri(ScannerActivity.this, uri);
                    // 对获取到的二维码照片进行压缩
                    // 必须压缩，否则可能因图片过大内存不够崩溃，
                    try {
                        Bitmap bitmap = DecodeUtils.compressPicture(this, Uri.fromFile(new File(imagePath)));
                        Message message = mHandler.obtainMessage(MESSAGE_DECODE_FROM_BITMAP, bitmap);
                        mHandler.sendMessage(message);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goPicture();
                return;
            }
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<ScannerActivity> activity;

        MyHandler(ScannerActivity mainActivityWeakReference) {
            activity = new WeakReference<ScannerActivity>(mainActivityWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScannerActivity activity = this.activity.get();
            if (activity != null) {
                if (msg.what == activity.MESSAGE_DECODE_FROM_BITMAP) {
                    Bitmap bm = (Bitmap) msg.obj;
                    DecodeUtils.DecodeAsyncTask decodeAsyncTask = new DecodeUtils.DecodeAsyncTask(activity);
                    decodeAsyncTask.execute(bm);
                }
            }
        }
    }
}
