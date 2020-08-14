package com.redchamber.lib.net.rx;

import android.net.ParseException;

import com.google.gson.JsonParseException;
import com.redchamber.lib.utils.ToastUtils;

import org.json.JSONException;

import java.net.SocketTimeoutException;

import okhttp3.Connection;
import retrofit2.HttpException;

public class NetWorkCodeException {
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    /**
     * 自定义的code
     */

    //未知错误
    public static final int UNKNOWN = 1000;
    //解析错误
    public static final int PARSE_ERROR = 1001;
    //网络错误
    public static final int NETWORK_ERROR = 1002;
    //协议出错
    public static final int HTTP_ERROR = 1003;
    //证书出错
    public static final int SSL_ERROR = 1005;
    //无网络
    public static final int NO_NETWORK = 2000;

    public static ResponseThrowable getResponseThrowable(Throwable e) {
        e.printStackTrace();
        ResponseThrowable ex;

        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponseThrowable();
            ex.code = httpException.code();
            switch (httpException.code()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    ex.message = "请检查权限";
                    break;
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                    ex.message = "网络超时";
                    break;
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex.message = "服务器错误";
                    break;
            }
            try {
                ToastUtils.showToast("code:" + ex.code + " " + httpException.message());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new ResponseThrowable();
            ex.code = resultException.code;
            ex.message = resultException.message;
            try {
                ToastUtils.showToast("code:" + ex.code + " " + ex.message);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            ex = new ResponseThrowable();
            ex.code = PARSE_ERROR;
            ex.message = "解析错误";
            return ex;
        } else if (e instanceof SocketTimeoutException) {
            ex = new ResponseThrowable();
            ex.code = NETWORK_ERROR;
            ex.message = "网络超时";
            return ex;
        } else if (e instanceof Connection) {
            ex = new ResponseThrowable();
            ex.code = NETWORK_ERROR;
            ex.message = "连接失败";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponseThrowable();
            ex.code = SSL_ERROR;
            ex.message = "证书验证失败";
            return ex;
        } else {
            ex = new ResponseThrowable();
            ex.code = NETWORK_ERROR;
            ex.message = null;
            return ex;
        }
    }


    public static class ResponseThrowable extends Exception {
        public int code;
        public String message;

        public ResponseThrowable() {
        }

        public ResponseThrowable(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            return "ResponseThrowable{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public class ServerException extends RuntimeException {
        public int code;
        public String message;

        @Override
        public String toString() {
            return "ServerException{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
