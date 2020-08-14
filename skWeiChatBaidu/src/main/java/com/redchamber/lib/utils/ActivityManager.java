package com.redchamber.lib.utils;

import com.redchamber.lib.base.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

/**
 * 页面管理类
 */
public class ActivityManager {

    private ActivityManager() {
    }

    private static ActivityManager sManager;
    private Stack<WeakReference<BaseActivity>> mActivityStack;

    public static ActivityManager getManager() {
        if (sManager == null) {
            synchronized (ActivityManager.class) {
                if (sManager == null) {
                    sManager = new ActivityManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 添加Activity到栈
     *
     * @param activity
     */
    public void addActivity(BaseActivity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }

    /**
     * 检查弱引用是否释放，若释放，则从栈中清理掉该元素
     */
    public void checkWeakReference() {
        try {
            if (mActivityStack != null) {
                // 使用迭代器进行安全删除
                for (Iterator<WeakReference<BaseActivity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                    WeakReference<BaseActivity> activityReference = it.next();
                    BaseActivity temp = activityReference.get();
                    if (temp == null) {
                        it.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前Activity（栈中最后一个压入的）
     *
     * @return
     */
    public BaseActivity currentActivity() {
        checkWeakReference();
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            return mActivityStack.lastElement().get();
        }
        return null;
    }

    /**
     * 关闭当前Activity（栈中最后一个压入的）
     */
    public void finishActivity() {
        BaseActivity activity = currentActivity();
        if (activity != null) {
            finishActivity(activity);
        }
    }

    /**
     * 关闭指定的Activity
     *
     * @param activity
     */
    public void finishActivity(BaseActivity activity) {
        try {
            if (activity != null && mActivityStack != null) {
                // 使用迭代器进行安全删除
                for (Iterator<WeakReference<BaseActivity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                    WeakReference<BaseActivity> activityReference = it.next();
                    BaseActivity temp = activityReference.get();
                    // 清理掉已经释放的activity
                    if (temp == null) {
                        it.remove();
                        continue;
                    }
                    if (temp == activity) {
                        it.remove();
                    }
                }
                activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭指定类名的所有Activity
     *
     * @param cls
     */
    public void finishActivity(Class<?> cls) {
        try {
            if (mActivityStack != null) {
                // 使用迭代器进行安全删除
                for (Iterator<WeakReference<BaseActivity>> it = mActivityStack.iterator(); it.hasNext(); ) {
                    WeakReference<BaseActivity> activityReference = it.next();
                    BaseActivity activity = activityReference.get();
                    // 清理掉已经释放的activity
                    if (activity == null) {
                        it.remove();
                        continue;
                    }
                    if (activity.getClass().equals(cls)) {
                        it.remove();
                        activity.finish();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        try {
            if (mActivityStack != null) {
                for (WeakReference<BaseActivity> activityReference : mActivityStack) {
                    BaseActivity activity = activityReference.get();
                    if (activity != null) {
                        activity.finish();
                    }
                }
                mActivityStack.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出应用程序
     */
    public void exitApp() {
        try {
            finishAllActivity();
            // 退出JVM,释放所占内存资源,0表示正常退出
            System.exit(0);
            // 从系统中kill掉应用程序
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
