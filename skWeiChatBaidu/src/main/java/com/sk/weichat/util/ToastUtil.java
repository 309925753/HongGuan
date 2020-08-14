package com.sk.weichat.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.sk.weichat.R;

import java.lang.reflect.Field;

/**
 *
 */
public class ToastUtil {

    public static void showErrorNet(Context context) {
        showToast(context, R.string.net_exception);
    }

    public static void showToast(Context context, int res) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(res), Toast.LENGTH_SHORT);
    }

    public static void showErrorData(Context context) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(R.string.data_exception), Toast.LENGTH_SHORT);
    }

    /**
     * onError回调内有多种情况，不能提示网络异常，模糊一点，提示请求异常好了
     *
     * @param context
     */
    public static void showNetError(Context context) {
        if (context == null) {
            return;
        }
        // realShowToast(context, context.getString(R.string.net_exception), Toast.LENGTH_SHORT);
        realShowToast(context, context.getString(R.string.request_exception), Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message) {
        if (context == null) {
            return;
        }
        realShowToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(Context context, String message) {
        if (context == null) {
            return;
        }
        realShowToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showLongToast(Context context, int res) {
        if (context == null) {
            return;
        }
        realShowToast(context, context.getString(res), Toast.LENGTH_LONG);
    }

    public static void showUnkownError(Context ctx, Throwable t) {
        String message = "null";
        if (t != null) {
            message = t.getMessage();
        }
        showToast(ctx, ctx.getString(R.string.tip_unkown_error_place_holder, message));
    }

    private static void realShowToast(Context context, int res, int duration) {
        realShowToast(context, context.getString(res), duration);
    }

    private static void realShowToast(Context context, CharSequence text, int duration) {
        // 安卓7弹toast有可能因ui线程阻塞而崩溃，hook加个try,
        hookToast(Toast.makeText(context, text, duration)).show();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static Toast hookToast(Toast toast) {
        Class<Toast> cToast = Toast.class;
        try {
            //TN是private的
            Field fTn = cToast.getDeclaredField("mTN");
            fTn.setAccessible(true);

            //获取tn对象
            Object oTn = fTn.get(toast);
            //获取TN的class，也可以直接通过Field.getType()获取。
            Class<?> cTn = oTn.getClass();
            Field fHandle = cTn.getDeclaredField("mHandler");

            //重新set->mHandler
            fHandle.setAccessible(true);
            fHandle.set(oTn, new HandlerProxy((Handler) fHandle.get(oTn)));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return toast;
    }

    private static class HandlerProxy extends Handler {

        private Handler mHandler;

        HandlerProxy(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException ignored) {
                // 安卓8源码中handleShow方法就是直接加了try catch避免崩溃，所以这里也只try catch避免崩溃，
            }
        }
    }
}
