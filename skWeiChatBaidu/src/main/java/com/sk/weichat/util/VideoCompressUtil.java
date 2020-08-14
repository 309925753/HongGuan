package com.sk.weichat.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Jni.FFmpegCmd;
import VideoHandle.OnEditorListener;

public class VideoCompressUtil {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void exec(String[] cmds, long duration, OnEditorListener listener) {
        executorService.execute(() -> {
            Thread thread = Thread.currentThread();
            OnEditorListener l = new OnEditorListener() {
                @Override
                public void onSuccess() {
                    synchronized (thread) {
                        thread.notify();
                    }
                    listener.onSuccess();
                }

                @Override
                public void onFailure() {
                    synchronized (thread) {
                        thread.notify();
                    }
                    listener.onFailure();
                }

                @Override
                public void onProgress(float progress) {
                    listener.onProgress(progress);
                }
            };
            FFmpegCmd.exec(cmds, duration, l);
            synchronized (thread) {
                try {
                    thread.wait();
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}
