package com.sk.weichat.ui.mucfile;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.TanX;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.apache.http.Header;

import java.io.File;

/**
 * Created by Administrator on 2017/7/10.
 */

public class UploadingHelper {

    public static void upfile(String accessToken, String userId, final File file, final OnUpFileListener onUpFileListener) {
        try {
            if (!file.exists()) {
                onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.alert_not_have_file), file.getAbsolutePath());
            }

            if (TextUtils.isEmpty(userId)) {
                onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.tip_user_id_empty), file.getAbsolutePath());
            }

            if (onUpFileListener == null) {
                onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.tip_upload_listener_empty), file.getAbsolutePath());
            }

            AsyncHttpClient client = new AsyncHttpClient();
            TanX.Log("上传文件：" + file.getAbsolutePath());
            RequestParams params = new RequestParams();
            params.put("access_token", accessToken);
            params.put("userId", userId);
            params.put("file1", file);
            params.put("validTime", "-1");// 文件有效期

            client.post(CoreManager.requireConfig(MyApplication.getInstance()).UPLOAD_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {//数据发送成功，并且有返回值<不是上传成功。。。>
                    TanX.Log("相应码：" + arg0);
                    String url = null;
                    if (arg0 == 200) {
                        UploadFileResult result = JSON.parseObject(new String(arg2), UploadFileResult.class);
                        UploadFileResult.Data data = result.getData();
                        //上传成功
                        if (result == null || result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null
                                || result.getSuccess() != result.getTotal()) {
                            onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.upload_failed), file.getAbsolutePath());
                        } else {

                            if (!TextUtils.isEmpty(url = getImagesUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getVideosUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getAudiosUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getFilesUrl(data))) {

                            } else if (!TextUtils.isEmpty(url = getOthersUrl(data))) {
                                url = getOthersUrl(data);
                            }
                        }
                    }
                    if (TextUtils.isEmpty(url)) {
                        // 返回成功，但是却获取不到对应的URL，服务器返回值异常<概率极小>
                        Log.i("roamer", "上传文件成功了 但是URL 是空的");
                        onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.tip_upload_result_empty), file.getAbsolutePath());
                    } else {
                        Log.i("roamer", "上传文件成功了");
                        onUpFileListener.onSuccess(url, file.getAbsolutePath());
                    }
                }

                @Override
                public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            onUpFileListener.onFailure(MyApplication.getContext().getString(R.string.upload_failed), file.getAbsolutePath());
        }
    }

    private static String getAudiosUrl(UploadFileResult.Data data) {
        TanX.Log("语音格式");
        if (data.getAudios() != null && data.getAudios().size() > 0) {
            return data.getAudios().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getFilesUrl(UploadFileResult.Data data) {
        TanX.Log("文件格式");
        if (data.getFiles() != null && data.getFiles().size() > 0) {
            return data.getFiles().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getVideosUrl(UploadFileResult.Data data) {
        TanX.Log("视频格式");
        if (data.getVideos() != null && data.getVideos().size() > 0) {
            return data.getVideos().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getImagesUrl(UploadFileResult.Data data) {
        TanX.Log("图片格式");
        if (data.getImages() != null && data.getImages().size() > 0) {
            return data.getImages().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    private static String getOthersUrl(UploadFileResult.Data data) {
        TanX.Log("其他格式");
        if (data.getOthers() != null && data.getOthers().size() > 0) {
            return data.getOthers().get(0).getOriginalUrl();
        } else {
            return "";
        }
    }

    public interface OnUpFileListener {
        void onSuccess(String url, String filePath);

        void onFailure(String err, String filePath);
    }
}
