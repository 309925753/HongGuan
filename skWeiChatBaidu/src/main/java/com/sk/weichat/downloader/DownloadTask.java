package com.sk.weichat.downloader;

import android.os.Handler;
import android.util.Log;

import com.sk.weichat.downloader.FailReason.FailType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class DownloadTask implements Runnable {

    final String uri;
    final ViewAware viewAware;
    final DownloadListener listener;
    final DownloadProgressListener progressListener;
    private final DownloaderEngine engine;
    private final DownLoadingInfo imageLoadingInfo;
    private final Handler handler;

    public DownloadTask(DownloaderEngine engine, DownLoadingInfo imageLoadingInfo, Handler handler) {
        this.engine = engine;
        this.imageLoadingInfo = imageLoadingInfo;
        this.handler = handler;

        uri = imageLoadingInfo.uri;
        viewAware = imageLoadingInfo.viewAware;
        listener = imageLoadingInfo.listener;
        progressListener = imageLoadingInfo.progressListener;
    }

    static void runTask(Runnable r, Handler handler) {
        handler.post(r);
    }

    @Override
    public void run() {
        if (waitIfPaused())
            return;
        ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
        loadFromUriLock.lock();

        File file = null;
        try {
            checkTaskNotActual();
            file = Downloader.getInstance().getFile(uri);
            if (!file.exists()) {
                file = tryDownloadFile();
            }

            if (file == null || !file.exists()) {
                return;
            }

            checkTaskNotActual();
            checkTaskInterrupted();

        } catch (TaskCancelledException e) {
            fireCancelEvent();
            e.printStackTrace();
            return;
        } finally {
            loadFromUriLock.unlock();
        }

        if (isViewCollected()) {
            fireCancelEvent();
        } else if (isViewReused()) {
            fireCancelEvent();
        } else {
            engine.cancelDisplayTaskFor(viewAware);
            fireCompleteEvent(file.getAbsolutePath());
        }

    }

    private File tryDownloadFile() throws TaskCancelledException {
        File tempFile = null;
        boolean success = false;

        HttpURLConnection connection = null;
        FileOutputStream os = null;
        InputStream is = null;

        try {
            tempFile = Downloader.getInstance().getTempFile(uri);
            if (!tempFile.getParentFile().exists()) {
                boolean createSuccess = tempFile.getParentFile().mkdirs();
                if (!createSuccess) {
                    Log.e("PART_TEST", "1");
                    fireFailEvent(FailType.IO_ERROR, new IOException());
                    return null;
                }
            }

            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Snowdream Mobile");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);

            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                Log.e("PART_TEST", "2");
                fireFailEvent(FailType.IO_ERROR, new IOException());
                return null;
            }

            is = url.openStream();
            int total = connection.getContentLength();
            int current = 0;
            os = new FileOutputStream(tempFile);

            checkTaskNotActual();

            if (is != null && os != null) {
                byte[] buffer = new byte[1024 * 5];
                int readLen = 0;
                while ((readLen = is.read(buffer, 0, buffer.length)) > 0) {
                    os.write(buffer, 0, readLen);
                    current += readLen;
                    fireProgressEvent(current, total);
                }
                success = true;
            }

            if (total != -1) {
                if (tempFile.length() != total) {
                    success = false;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("PART_TEST", "3" + e.getMessage());
            fireFailEvent(FailType.IO_ERROR, e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("PART_TEST", "4" + e.getMessage());
            fireFailEvent(FailType.IO_ERROR, e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PART_TEST", "5" + e.getMessage());
            fireFailEvent(FailType.IO_ERROR, e);
        } catch (TaskCancelledException e) {
            throw e;
        } catch (Throwable e) {
            Log.e("PART_TEST", "6" + e.getMessage());
            fireFailEvent(FailType.UNKNOWN, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (os != null) {
                    os.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // reName
        if (!success) {
            return null;
        }
        File file = Downloader.getInstance().getFile(uri);
        success = tempFile.renameTo(file);
        if (success) {
            return file;
        } else {
            tempFile.delete();
            return null;
        }

    }

    /**
     * @return <b>true</b> - if task should be interrupted; <b>false</b> -
     * otherwise
     */
    private boolean waitIfPaused() {
        AtomicBoolean pause = engine.getPause();
        if (pause.get()) {
            synchronized (engine.getPauseLock()) {
                if (pause.get()) {
                    try {
                        engine.getPauseLock().wait();
                    } catch (InterruptedException e) {
                        return true;
                    }
                }
            }
        }
        return isTaskNotActual();
    }

    /**
     * @return <b>true</b> - if task is not actual (target ImageAware is
     * collected by GC or the image URI of this task doesn't match to
     * image URI which is actual for current ImageAware at this
     * moment)); <b>false</b> - otherwise
     */
    private boolean isTaskNotActual() {
        return isViewCollected() || isViewReused();
    }

    /**
     * @return <b>true</b> - if target ImageAware is collected by GC;
     * <b>false</b> - otherwise
     */
    private boolean isViewCollected() {
        if (viewAware.isCollected()) {
            return true;
        }
        return false;
    }

    /**
     * @return <b>true</b> - if current ImageAware is reused for displaying
     * another image; <b>false</b> - otherwise
     */
    private boolean isViewReused() {
        String currentCacheKey = engine.getLoadingUriForView(viewAware);
        // Check whether memory cache key (image URI) for current ImageAware is
        // actual.
        // If ImageAware is reused for another task then current task should be
        // cancelled.
        boolean imageAwareWasReused = !uri.equals(currentCacheKey);
        if (imageAwareWasReused) {
            return true;
        }
        return false;
    }

    /**
     * @throws TaskCancelledException if task is not actual (target ImageAware is collected by GC
     *                                or the image URI of this task doesn't match to image URI
     *                                which is actual for current ImageAware at this moment)
     */


    private void checkTaskNotActual() throws TaskCancelledException {
        checkViewCollected();
        checkViewReused();
    }

    /**
     * @throws TaskCancelledException if target ImageAware is collected
     */
    private void checkViewCollected() throws TaskCancelledException {
        if (isViewCollected()) {
            throw new TaskCancelledException();
        }
    }

    /**
     * @throws TaskCancelledException if target ImageAware is collected by GC
     */
    private void checkViewReused() throws TaskCancelledException {
        if (isViewReused()) {
            throw new TaskCancelledException();
        }
    }

    /**
     * @throws TaskCancelledException if current task was interrupted
     */
    private void checkTaskInterrupted() throws TaskCancelledException {
        if (isTaskInterrupted()) {
            throw new TaskCancelledException();
        }
    }

    /**
     * @return <b>true</b> - if current task was interrupted; <b>false</b> -
     * otherwise
     */
    private boolean isTaskInterrupted() {
        if (Thread.interrupted()) {
            return true;
        }
        return false;
    }

    String getLoadingUri() {
        return uri;
    }

    /**
     * @return <b>true</b> - if loading should be continued; <b>false</b> - if
     * loading should be interrupted
     */
    private boolean fireProgressEvent(final int current, final int total) {
        if (isTaskInterrupted() || isTaskNotActual())
            return false;
        if (progressListener != null) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    progressListener.onProgressUpdate(uri, viewAware.getWrappedView(), current, total);
                }
            };
            runTask(r, handler);
        }
        return true;
    }

    // /////////////

    private void fireFailEvent(final FailType failType, final Throwable failCause) {
        if (isTaskInterrupted() || isTaskNotActual())
            return;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onFailed(uri, new FailReason(failType, failCause), viewAware.getWrappedView());
            }
        };
        runTask(r, handler);
    }

    private void fireCancelEvent() {
        if (isTaskInterrupted())
            return;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onCancelled(uri, viewAware.getWrappedView());
            }
        };
        runTask(r, handler);
    }

    private void fireCompleteEvent(final String filePath) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onComplete(uri, filePath, viewAware.getWrappedView());
            }
        };
        runTask(r, handler);
    }

    class TaskCancelledException extends Exception {
        private static final long serialVersionUID = 648537347121358898L;
    }

}
