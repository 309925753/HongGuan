package com.sk.weichat.ui.base;

import android.os.Bundle;

import com.sk.weichat.MyApplication;

/**
 * 加入了堆栈的Activity
 *
 * @author Dean Tao
 */
/*
  靠不住，activity栈里的activity不一定是stop状态，可能存在destroy状态，
  比如 A -> B -> C, C崩溃，安卓自动恢复activity栈，出现A -> B，然后初始化B，而A就保持destroy,
  模拟可以使用开发者选项里的“不保留活动”，
  或者kill命令杀进程模拟崩溃，
 */
public abstract class StackActivity extends SetActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityStack.getInstance().push(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.getInstance().pop(this);
//        if (isFinishing()) {
//            if (!ActivityStack.getInstance().has()) {
//                MyApplication.getInstance().destory();
//            }
//        }
    }
}
