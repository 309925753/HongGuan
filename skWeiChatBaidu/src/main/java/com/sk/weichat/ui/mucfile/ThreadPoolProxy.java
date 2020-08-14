package com.sk.weichat.ui.mucfile;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liuxuan
 * @time 2017-7-7 14:39:11
 * @des 创建线程池, 执行任务, 提交任务
 */
public class ThreadPoolProxy {
    ThreadPoolExecutor mExecutor;

    int mCorePoolSize = 3;
    int mMaximumPoolSize = 3;

    long mKeepAliveTime = 1800;
    TimeUnit mUnit = TimeUnit.SECONDS;

    private ThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super();
        mCorePoolSize = corePoolSize;
        mMaximumPoolSize = maximumPoolSize;
        mKeepAliveTime = keepAliveTime;
        mUnit = unit;
    }

    private static volatile ThreadPoolProxy instance = null;

    public static ThreadPoolProxy getInstance() {
        synchronized (ThreadPoolProxy.class) {
            if (instance == null) {
                instance = new ThreadPoolProxy(3, 3, 3000, TimeUnit.MILLISECONDS);
            }
        }
        return instance;
    }

    private ThreadPoolExecutor initThreadPoolExecutor() {
        // 双重检查加锁
        if (mExecutor == null) {
            synchronized (ThreadPoolProxy.class) {
                if (mExecutor == null) {
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();// 无界队列
/*
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

                    mExecutor = new ThreadPoolExecutor(
                            mCorePoolSize,   // 核心线程数
                            mMaximumPoolSize,// 最大的线程数

                            mKeepAliveTime,  // 非核心线程超时时长(存活时长)
                            mUnit,           // 超时时间单位

                            workQueue,       // 线程池中的任务队列
                    );
*/

                    mExecutor = new ThreadPoolExecutor(
                            mCorePoolSize,   // 核心线程数
                            mMaximumPoolSize,// 最大的线程数

                            mKeepAliveTime,  // 非核心线程超时时长(存活时长)
                            mUnit,           // 超时时间单位

                            workQueue        // 线程池中的任务队列
                    );

                    // mExecutor.allowCoreThreadTimeOut(true);// 核心线程也存在超时策略
                }
            }
        }
        return mExecutor;
    }

    /**
     * 执行任务
     */
    public void execute(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }

    /**
     * 移除任务
     */
    public void removeTask(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.remove(task);
    }
}
