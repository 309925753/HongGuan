package com.sk.weichat.audio;

import java.util.ArrayList;

public interface RecordListener {
    public void onRecordStart();

    public void onRecordCancel();

    public void onRecordSuccess(String filePath, int timeLen, ArrayList<String> strings);
}
