package com.sk.weichat.downloader;

import java.util.concurrent.locks.ReentrantLock;

public final class DownLoadingInfo {

    final String uri;
    final ViewAware viewAware;
    final ReentrantLock loadFromUriLock;
    final DownloadListener listener;
    final DownloadProgressListener progressListener;

    public DownLoadingInfo(String uri, ViewAware viewAware, ReentrantLock loadFromUriLock, DownloadListener listener, DownloadProgressListener progressListener) {
        this.uri = uri;
        this.viewAware = viewAware;
        this.loadFromUriLock = loadFromUriLock;
        this.listener = listener;
        this.progressListener = progressListener;
    }

}
