package com.sk.weichat.call;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sk.weichat.R;

/**
 * Created by Administrator on 2017/9/21 0021.
 */
public class JitsiFloatService extends Service {
    RefreshBroadcastReceiverTimer refreshBroadcastReceiverTimer = new RefreshBroadcastReceiverTimer();
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private int mFloatWinWidth, mFloatWinHeight;
    private int mStartX, mLastX, mStartY, mLastY;
    private View mFloatView;
    private TextView timer;

    @Override
    public void onCreate() {
        super.onCreate();
        JitsistateMachine.isFloating = true;
        createWindowManager();
        createFloatView();
        registerFloatingCast();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeFloatView();
    }

    private void createWindowManager() {
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;
        mFloatWinWidth = (int) (screenWidth * 0.8 / 4);
        mFloatWinHeight = mFloatWinWidth * 4 / 3;

        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;// TYPE_TOAST(7.0上使用TYPE_TOAST可能会有问题)
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mLayoutParams.gravity = Gravity.TOP | Gravity.END;
        mLayoutParams.x = 10;
        mLayoutParams.y = 10;
        mLayoutParams.width = mFloatWinWidth;
        mLayoutParams.height = mFloatWinHeight;
    }

    private void createFloatView() {
        LayoutInflater inflater = LayoutInflater.from(JitsiFloatService.this);
        mFloatView = inflater.inflate(R.layout.activity_main_03, null);
        timer = (TextView) mFloatView.findViewById(R.id.time_for_me);
        mWindowManager.addView(mFloatView, mLayoutParams);
        moveFloatView();

        mFloatView.setOnClickListener(v -> {
            Intent intent = new Intent(getBaseContext(), Jitsi_connecting_second.class);
            // 部分设备报错提示要加这个，
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            destroy();
            removeFloatView();
        });
    }

    private void registerFloatingCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CallConstants.REFRESH_FLOATING);
        intentFilter.addAction(CallConstants.CLOSE_FLOATING);
        registerReceiver(refreshBroadcastReceiverTimer, intentFilter);
    }

    private void moveFloatView() {// 移动悬浮窗
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (MotionEvent.ACTION_DOWN == action) {
                    mStartX = mLastX = (int) event.getRawX();
                    mStartY = mLastY = (int) event.getRawY();
                } else if (MotionEvent.ACTION_UP == action) {
                    int dx = (int) event.getRawX() - mStartX;
                    int dy = (int) event.getRawY() - mStartY;
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        return true;
                    }
                } else if (MotionEvent.ACTION_MOVE == action) {
                    int dx = (int) event.getRawX() - mLastX;
                    int dy = (int) event.getRawY() - mLastY;
                    mLayoutParams.x = mLayoutParams.x - dx;
                    mLayoutParams.y = mLayoutParams.y + dy;
                    mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
                    mLastX = (int) event.getRawX();
                    mLastY = (int) event.getRawY();
                }
                return false;
            }
        });
    }

    private void destroy() {
        if (refreshBroadcastReceiverTimer != null) {
            unregisterReceiver(refreshBroadcastReceiverTimer);
        }
        JitsiFloatService.this.stopSelf();
    }

    private void removeFloatView() {
        if (mWindowManager != null && mFloatView != null) {
            JitsistateMachine.isFloating = false;
            try {
                mWindowManager.removeView(mFloatView);
            } catch (Exception e) {

            }
        }
    }

    public class RefreshBroadcastReceiverTimer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CallConstants.REFRESH_FLOATING)) {
                timer.setText(Jitsi_connecting_second.time);
            } else if (intent.getAction().equals(CallConstants.CLOSE_FLOATING)) {// 挂断的广播
                destroy();
            }
        }
    }
}
