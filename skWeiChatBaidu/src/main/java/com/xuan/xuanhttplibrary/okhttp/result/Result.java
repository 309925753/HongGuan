package com.xuan.xuanhttplibrary.okhttp.result;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.ToastUtil;


public class Result {
    /**
     * 通用的Http Result Code http 请求返回的结果码 <br/>
     * 0表示一般性错误</br> 1-100表示成功</br> 大于100000表示一些详细的错误</br>
     */
    public final static int CODE_ERROE = 0;// 未知的错误 或者系统内部错误
    public final static int CODE_SUCCESS = 1;// 正确的Http请求返回状态码
    public final static int CODE_ARGUMENT_ERROR1 = 1010101;// 请求参数验证失败，缺少必填参数或参数错误
    public final static int CODE_ARGUMENT_ERROR2 = 1010102;// 缺少请求参数：%1$s

    public final static int CODE_INTERNAL_ERROR = 1020101;// 接口内部异常
    public final static int CODE_NO_TOKEN = 1030101;// 缺少访问令牌
    public final static int CODE_TOKEN_ERROR = 1030102;// 访问令牌过期或无效

    /* 登陆接口的Http Result Code */
    public final static int CODE_ACCOUNT_INEXISTENCE = 1040101;// 帐号不存在
    public final static int CODE_ACCOUNT_ERROE = 1040102;// 帐号或密码错误

    // 这个是第三方登录时出错这个第三方账号没有绑定到已有账号，
    public final static int CODE_THIRD_NO_PHONE = 1040305;// 第三方登录未绑定手机号码
    // 这个是绑定已有账号时出错这个IM账号不存在，
    public final static int CODE_THIRD_NO_EXISTS = 1040306;// 第三方登录时账号不存在
    public final static int CODE_LOGIN_TOKEN_INVALID = 1030112;// 自动登录时的loginToken过期，
    public final static int CODE_QR_KEY_INVALID = 1040204;// 生成付款码的qrKey过期，
    public final static int CODE_AUTH_LOGIN_SCUESS = 101987;// 授权成功
    public final static int CODE_AUTH_LOGIN_FAILED_1 = 101988;// 无授权
    public final static int CODE_AUTH_LOGIN_FAILED_2 = 101986;// 登入超时
    public final static int CODE_AUTH_LOGIN_FAILED_3 = 101982;// 验证码失效

    public static final String RESULT_CODE = "resultCode";
    public static final String RESULT_MSG = "resultMsg";
    public static final String RESULT_CURRENT_TIME = "currentTime";
    public static final String DATA = "data";

    private int resultCode;
    private String resultMsg;
    private long currentTime;

    public static boolean checkSuccess(Context context, Result result) {
        return checkSuccess(context, result, true);
    }

    public static boolean checkSuccess(Context context, Result result, boolean toast) {
        boolean success = result != null && result.resultCode == CODE_SUCCESS;
        if (!success) {
            if (toast) {
                ToastUtil.showToast(context, getErrorMessage(context, result));
            }
        }
        return success;
    }

    public static String getErrorMessage(Context ctx, Result result) {
        if (result == null || TextUtils.isEmpty(result.resultMsg)) {
            // 这里是即没成功也没错误提示的情况，
            // 一定是服务器端的问题，
            Reporter.post("内部服务器错误");
            return ctx.getString(R.string.tip_server_error);
        } else {
            return result.resultMsg;
        }
    }

    public static boolean checkError(Result result, int errorCode) {
        return result != null && result.resultCode == errorCode;
    }

    public static boolean defaultParser(Context context, Result result, boolean showToast) {
        if (result == null) {
            if (showToast) {
                toast(context, context.getString(R.string.data_exception));
            }
            return false;
        }
        if (result.resultCode == CODE_SUCCESS) {// 成功
            return true;
        } else if (result.resultCode == CODE_NO_TOKEN) {
            // 缺少参数Token,需要重新登录
            LoginHelper.broadcastConflict(context);
            showResultToast(context, result);
            return false;
        } else if (result.resultCode == CODE_TOKEN_ERROR) {
            // Token过期或错误,需要重新登录
            LoginHelper.broadcastConflict(context);
            showResultToast(context, result);
            return false;
        } else {
            if (showToast)
                showResultToast(context, result);
            return false;
        }
    }

    private static void showResultToast(Context context, com.xuan.xuanhttplibrary.okhttp.result.Result result) {
        if (TextUtils.isEmpty(result.getResultMsg())) {
            toast(context, context.getString(R.string.data_exception));
        } else {
            toast(context, result.getResultMsg());
        }
    }

    private static void toast(Context context, String string) {
        // 个别情况比如朋友圈上传，会在异步线程调用相关方法，toast要切到主线程，
        AsyncUtils.runOnUiThread(context, c -> {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        });
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public String toString() {
        return JSON.toJSON(this).toString();
    }
}
