package com.sk.weichat.db.dao.login;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class Machine {

    // 设备名
    private String machineName;
    // 是否在线
    private boolean isOnLine;
    // 是否发送回执
    private boolean isSendReceipt = true;

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public boolean isOnLine() {
        return isOnLine;
    }

    public void setOnLine(boolean onLine) {
        isOnLine = onLine;
    }

    public boolean isSendReceipt() {
        return isSendReceipt;
    }

    public void setSendReceipt(boolean sendReceipt) {
        isSendReceipt = sendReceipt;
    }

    // 计时器的回调监听
    private TimerListener timerListener;

    public TimerListener getTimerListener() {
        return timerListener;
    }

    public void setTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
    }

    // 计时器
    private CountDownTimer countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (timerListener != null) {
                timerListener.onFinish(machineName);
                if (isOnLine) {
                    resetTimer();
                } else {
                    stopTimer();
                }
            }
        }
    };

    // 停止计时
    public void stopTimer() {
        Log.e("msg", machineName + "停止计时 ");
        countDownTimer.cancel();
    }

    // 重新计时
    public void resetTimer() {
        Log.e("msg", machineName + "重新计时 ");
        countDownTimer.cancel();
        countDownTimer.start();
    }
}
