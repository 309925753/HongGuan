package com.sk.weichat.downloader;

import android.annotation.SuppressLint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


@SuppressLint("UseSparseArrays")
class DownloaderEngine {
    public static final int DEFAULT_THREAD_POOL_SIZE = 3;
    public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private int threadPriority = DEFAULT_THREAD_PRIORITY;

    private Executor taskExecutor;
    private final Map<String, ReentrantLock> uriLocks = new WeakHashMap<String, ReentrantLock>();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();

    DownloaderEngine() {
        taskExecutor = createTaskExecutor();
    }

    private Executor createTaskExecutor() {
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
        return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue, new DefaultThreadFactory(threadPriority,
                "uil-pool-"));
    }

    /**
     * Pauses engine. All new "load&display" tasks won't be executed until
     * ImageLoader is {@link #resume() resumed}.<br
     * />
     * Already running tasks are not paused.
     */
    void pause() {
        paused.set(true);
    }

    /**
     * Resumes engine work. Paused "load&display" tasks will continue its work.
     */
    void resume() {
        paused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    /**
     * Stops engine, cancels all running and scheduled display image tasks.
     * Clears internal data. <br />
     * <b>NOTE:</b> This method doesn't shutdown
     * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#taskExecutor(Executor)
     * custom task executors} if you set them.
     */
    void stop() {
        ((ExecutorService) taskExecutor).shutdownNow();
        cacheKeysForImageAwares.clear();
        uriLocks.clear();
    }

    ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = uriLocks.get(uri);
        if (lock == null) {
            lock = new ReentrantLock();
            uriLocks.put(uri, lock);
        }
        return lock;
    }

    AtomicBoolean getPause() {
        return paused;
    }

    Object getPauseLock() {
        return pauseLock;
    }

    /**
     * Submits task to execution pool
     */
    void submit(final DownloadTask task) {
        taskExecutor.execute(task);
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final int threadPriority;

        DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
            this.threadPriority = threadPriority;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            t.setPriority(threadPriority);
            return t;
        }
    }


    private final Map<Integer, String> cacheKeysForImageAwares = Collections
            .synchronizedMap(new HashMap<Integer, String>());

    /**
     * Returns URI of image which is loading at this moment into passed {@link com.nostra13.universalimageloader.core.imageaware.ImageAware}
     */
    String getLoadingUriForView(ViewAware aware) {
        return cacheKeysForImageAwares.get(aware.getId());
    }

    /**
     * Associates <b>memoryCacheKey</b> with <b>imageAware</b>. Then it helps to define image URI is loaded into View at
     * exact moment.
     */
    void prepareDisplayTaskFor(ViewAware aware, String memoryCacheKey) {
        cacheKeysForImageAwares.put(aware.getId(), memoryCacheKey);
    }

    /**
     * Cancels the task of loading and displaying image for incoming <b>imageAware</b>.
     *
     * @param aware {@link com.nostra13.universalimageloader.core.imageaware.ImageAware} for which display task
     *              will be cancelled
     */
    void cancelDisplayTaskFor(ViewAware aware) {
        cacheKeysForImageAwares.remove(aware.getId());
    }
}
