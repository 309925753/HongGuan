package com.redchamber.util;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LongClickUtils {

    public static void setLongClick(final Handler handler, final View longClickView, final long delayMillis, onPressListener onPressListener) {

        longClickView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                longClickView.setOnTouchListener(new View.OnTouchListener() {
                    private int TOUCH_MAX = 1000;
                    private int mLastMotionX;
                    private int mLastMotionY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                // 抬起时,移除已有Runnable回调,抬起就算长按了(不需要考虑用户是否长按了超过预设的时间)
                                handler.removeCallbacksAndMessages(null);
                                handler.post(runnableEnd);
                                Log.d("aaaa", "=============ACTION_UP");
                                break;
//                    case MotionEv`ent.ACTION_MOVE:
////                        if (Math.abs(mLastMotionX - x) > TOUCH_MAX
////                                || Math.abs(mLastMotionY - y) > TOUCH_MAX) {
////                            // 移动误差阈值
////                            // xy方向判断
////                            // 移动超过阈值，则表示移动了,就不是长按(看需求),移除 已有的Runnable回调
////                            handler.removeCallbacksAndMessages(null);
////                        }
////                        break;`
                            case MotionEvent.ACTION_DOWN:
                                // 每次按下重新计时
                                // 按下前,先移除 已有的Runnable回调,防止用户多次单击导致多次回调长按事件的bug
                                Log.d("aaaa", "=============ACTION_DOWN");
                                handler.removeCallbacksAndMessages(null);
                                mLastMotionX = x;
                                mLastMotionY = y;
                                // 按下时,开始计时
                                handler.post(runnableStart);
                                handler.postDelayed(runnableEnd, delayMillis);
                                break;
                        }
                        return true;//onclick等其他事件不能用请改这里
                    }

                    private Runnable runnableEnd = new Runnable() {
                        @Override
                        public void run() {
                            if (onPressListener != null) {// 回调给用户,用户可能传null,需要判断null
                                onPressListener.onPressEnd();
                            }
                        }
                    };

                    private Runnable runnableStart = new Runnable() {
                        @Override
                        public void run() {
                            if (onPressListener != null) {
                                onPressListener.onPressStart();
                            }
                        }
                    };
                });
            }
        });

    }

    public interface onPressListener {

        void onPressStart();

        void onPressEnd();

    }


}
