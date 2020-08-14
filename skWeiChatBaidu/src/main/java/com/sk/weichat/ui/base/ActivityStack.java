package com.sk.weichat.ui.base;

import android.app.Activity;

import java.util.Stack;

public class ActivityStack {
    private static ActivityStack instance;
    private Stack<Activity> stack;

    private ActivityStack() {
        stack = new Stack<Activity>();
    }

    public static ActivityStack getInstance() {
        if (instance == null) {
            synchronized (ActivityStack.class) {
                if (instance == null) {
                    instance = new ActivityStack();
                }
            }
        }
        return instance;
    }

    public void push(Activity activity) {
        stack.add(activity);
    }

    public void pop(Activity activity) {
        if (activity != null) {
            stack.remove(activity);
        }
    }

    public boolean has() {
        return stack.size() > 0;
    }

    /*
      靠不住，activity栈里的activity不一定是stop状态，可能存在destroy状态，
      比如 A -> B -> C, C崩溃，安卓自动恢复activity栈，出现A -> B，然后初始化B，而A就保持destroy,
      模拟可以使用开发者选项里的“不保留活动”，
      或者kill命令杀进程模拟崩溃，
     */
    @Deprecated
    public void exit() {
        for (int i = 0; i < stack.size(); i++) {
            Activity activity = stack.get(i);
            stack.remove(i);
            i--;
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }

    public Activity getActivity(int position) {
        return stack.get(position);
    }

    public int size() {
        return stack.size();
    }
}
