package com.sk.weichat.ui.mucfile;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sk.weichat.ui.mucfile.bean.DownBean;
import com.sk.weichat.ui.mucfile.bean.MucFileBean;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author liuxuan
 * @time 2017-7-7 14:39:11
 * @des 下载管理器，核心下载逻辑
 */
public class DownManager {
    /*############### 单例 ###############*/
    private DownManager() {
    }

    private volatile static DownManager instance;

    public static DownManager instance() {
        if (instance == null) {
            synchronized (DownManager.class) {
                if (instance == null) {
                    instance = new DownManager();
                }
            }
        }
        return instance;
    }

    /*############### 状态 ###############*/
    public static final int STATE_UNDOWNLOAD = 0;                                    // 未下载
    public static final int STATE_DOWNLOADING = 1;                                   // 下载中
    public static final int STATE_PAUSEDOWNLOAD = 2;                             // 暂停下载
    public static final int STATE_WAITINGDOWNLOAD = 3;                       // 等待下载
    public static final int STATE_DOWNLOADFAILED = 4;                           // 下载失败
    public static final int STATE_DOWNLOADED = 5;                                    // 下载完成

    // 记录正在下载的一些downLoadInfo
    public Map<String, DownBean> mDownLoadMaps = new HashMap<String, DownBean>();

    // 下载任务
    public void download(MucFileBean data) {  // 下载 和 断点继续
        // 1 封装成 downBean
        DownBean downBean;
        if (mDownLoadMaps.containsKey(data.getUrl())) {
            downBean = mDownLoadMaps.get(data.getUrl());
        } else {
            downBean = getDownBean(data);
            mDownLoadMaps.put(downBean.url, downBean);
        }

        downBean.state = STATE_WAITINGDOWNLOAD;

        // 2 保存数据库
        if (DownDao.instance().isExists(data.getUrl())) {         // 说明数据有数据
            DownBean q = DownDao.instance().query(data.getUrl()); // 校准我们的数据
            downBean.cur = q.cur;
            downBean.state = q.state;
            downBean.max = q.max;
        } else { // 第一次下载
            DownDao.instance().insert(downBean);
        }

        // 文件检查
        File file = new File(getFileDir(), downBean.name);

        if (!file.exists()) {
            if (downBean.cur != 0) {// 数据矫正过来
                Log.e("xuan", "文件出错");
                downBean.cur = 0;
                DownDao.instance().update(downBean);
            }
        } else {
        }
        // 3 去下载
        ThreadPoolProxy.getInstance().execute(downBean.task);
    }

    /**
     * 对外界暴露我们这个下载情况
     */
    public DownBean getDownloadState(MucFileBean data) {
        DownBean bean = null;
        if (mDownLoadMaps.containsKey(data.getUrl())) {     // 在队列中 直接返回
            bean = mDownLoadMaps.get(data.getUrl());
        } else {
            bean = DownDao.instance().query(data.getUrl()); // 去查询数据库
            if (bean == null) {
                bean = getDownBean(data); // 未下载
            }
        }

        /* 检查文件 */
        File file = new File(getFileDir(), data.getName());
        if (!file.exists()) {
            bean.state = STATE_UNDOWNLOAD;
            bean.cur = 0;
            DownDao.instance().delete(data.getUrl());
        }
        data.setState(bean.state);
        return bean;
    }

    private DownBean getDownBean(MucFileBean data) {
        DownBean bean = new DownBean();
        bean.state = STATE_UNDOWNLOAD;
        bean.max = data.getSize();
        bean.cur = data.getProgress();
        bean.url = data.getUrl();
        bean.name = data.getName();
        bean.task = new DownLoadTask(bean);
        return bean;
    }

    class DownLoadTask implements Runnable {
        DownBean mInfo;

        public DownLoadTask(DownBean info) {
            mInfo = info;
        }

        @Override
        public void run() {
            try {
                /*############### 当前状态: 下载中 ###############*/
                mInfo.state = STATE_DOWNLOADING;
                notifyObservers(mInfo);
                HttpURLConnection urlConnection = null;
                RandomAccessFile randomFile = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(mInfo.url);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestMethod("GET");
                    // 设置下载位置
                    urlConnection.setRequestProperty("Range", "bytes=" + mInfo.cur + "-" + mInfo.max);
                    // 设置文件写入位置
                    File file = new File(getFileDir(), mInfo.name);
                    randomFile = new RandomAccessFile(file, "rwd");
                    randomFile.seek(mInfo.cur);

                    long mTwoPercentSize = mInfo.max / 50;// 该文件2%的大小
                    long mCurrentDownSize = mInfo.cur;// 当前下载的文件大小

                    if (urlConnection.getResponseCode() == 206) {
                        // 获得文件流
                        inputStream = urlConnection.getInputStream();
                        byte[] buffer = new byte[1024 * 2];
                        int len = -1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            // 写入文件
                            randomFile.write(buffer, 0, len);
                            // 保存当前下载进度
                            mInfo.cur += len;
                            // 判断是否是暂停状态
                            if (mInfo.state == STATE_PAUSEDOWNLOAD) {
                                // 用户暂停了下载
                                notifyObservers(mInfo);
                                // 更新数据库
                                DownDao.instance().update(mInfo);
                                return; //结束循环
                            }
                            /*############### 当前状态: 下载中 ###############*/
                            mInfo.state = STATE_DOWNLOADING;

                            if (mInfo.cur - mCurrentDownSize >= mTwoPercentSize) {// 下载量大于等于2%，在通知页面刷新
                                mCurrentDownSize = mInfo.cur;
                                notifyObservers(mInfo);
                            }
                        }

                        /*############### 当前状态: 下载完成 ###############*/
                        mInfo.state = STATE_DOWNLOADED;
                        notifyObservers(mInfo);
                        DownDao.instance().update(mInfo);

                    } else {
                        /*############### 当前状态: 下载失败 ###############*/
                        mInfo.state = STATE_DOWNLOADFAILED;
                        notifyObservers(mInfo);
                        DownDao.instance().update(mInfo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    /*############### 当前状态: 下载失败 ###############*/
                    mInfo.state = STATE_DOWNLOADFAILED;
                    notifyObservers(mInfo);
                    DownDao.instance().update(mInfo);

                } finally {
                    // 回收工作
                    randomFile.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                /*############### 当前状态: 下载失败 ###############*/
                mInfo.state = STATE_DOWNLOADFAILED;
                notifyObservers(mInfo);
                DownDao.instance().update(mInfo);
            }
        }
    }

    public String getFileDir() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mucDown");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 暂停下载
     */
    public void pause(MucFileBean info) {
        /*############### 当前状态: 暂停 ###############*/
        DownBean data = mDownLoadMaps.get(info.getUrl());
        data.state = STATE_PAUSEDOWNLOAD;
        notifyObservers(data);
        /*#######################################*/
    }

    /**
     * 取消下载
     */
    public void cancel(MucFileBean info) {
        /*############### 当前状态: 未下载 ###############*/
        info.setState(STATE_UNDOWNLOAD);
        // 删除队列
        if (mDownLoadMaps.containsKey(info.getUrl())) {
            DownBean data = mDownLoadMaps.get(info.getUrl());
            // 找到线程池,移除任务
            ThreadPoolProxy.getInstance().removeTask(data.task);
        }
        // 删除数据
        detele(info);
    }

    /**
     * 删除文件
     */
    public void detele(MucFileBean data) {
        // 删除队列
        if (mDownLoadMaps.containsKey(data.getUrl())) {
            mDownLoadMaps.remove(data.getUrl());
        }

        // 去删除数据库
        if (DownDao.instance().isExists(data.getUrl())) {
            DownDao.instance().delete(data.getUrl());
        }

        // 删除文件
        File file = new File(getFileDir(), data.getName());
        if (file.exists()) {
            file.delete();
        }

        data.setState(STATE_UNDOWNLOAD);
        data.setProgress(0);
        notifyObservers(getDownBean(data));
    }

    /*=============== 自定义观察者设计模式  begin ===============*/
    public interface DownLoadObserver {
        void onDownLoadInfoChange(DownBean info);
    }

    List<DownLoadObserver> downLoadObservers = new LinkedList<DownLoadObserver>();

    /**
     * 添加观察者
     */
    public void addObserver(DownLoadObserver observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        synchronized (this) {
            if (!downLoadObservers.contains(observer))
                downLoadObservers.add(observer);
        }
    }

    /**
     * 删除观察者
     */
    public synchronized void deleteObserver(DownLoadObserver observer) {
        downLoadObservers.remove(observer);
    }

    /**
     * 通知观察者数据改变
     */
    public void notifyObservers(DownBean info) {
        Message msg = new Message();
        msg.obj = info;
        msg.what = 200;
        mHandler.sendMessage(msg);
    }

    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 200) {
                for (DownLoadObserver observer : downLoadObservers) {
                    DownBean info = (DownBean) msg.obj;
                    observer.onDownLoadInfoChange(info);
                }
            }
        }
    };
}
