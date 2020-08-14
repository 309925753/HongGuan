package com.sk.weichat.audio;

public interface RecordStateListener {
    public void onRecordStarting();

    public void onRecordStart();

    public void onRecordFinish(String file);

    public void onRecordCancel();

    public void onRecordVolumeChange(int v);

    public void onRecordTimeChange(int seconds);

    public void onRecordError();

    public void onRecordTooShoot();// 录音时间太短
}

