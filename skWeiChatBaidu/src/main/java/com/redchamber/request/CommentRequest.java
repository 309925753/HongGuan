package com.redchamber.request;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.redchamber.bean.YourCommentBean;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

/**
 * 获取个人真实评价
 */
public class CommentRequest {

    private volatile static CommentRequest instance;

    private CommentRequest() {
    }

    public static CommentRequest getInstance() {
        if (instance == null) {
            synchronized (CommentRequest.class) {
                if (instance == null) {
                    instance = new CommentRequest();
                }
            }
        }
        return instance;
    }

    public void getUserComment(Context context, String userId, UserCommentCallBack commentCallBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        if (!TextUtils.isEmpty(userId)) {
            params.put("othersId", userId);
        }

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_GET_COMMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            JSONArray array = JSON.parseArray(result.getData());
                            List<YourCommentBean> beanList = new ArrayList<>();
                            for (int i = 0; i < array.size(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                Set<String> keys = object.keySet();
                                YourCommentBean commentBean = new YourCommentBean();
                                for (String key : keys) {
                                    commentBean.type = key;
                                    commentBean.num = object.getIntValue(key);
                                }
                                beanList.add(commentBean);
                            }
                            commentCallBack.onSuccess(beanList);
                        } else {
                            commentCallBack.onFail(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        commentCallBack.onFail(e.getMessage());
                    }
                });
    }

    public void addUserComment(Context context, String userId, String comment, AddCommentCallBack commentCallBack) {
        DialogHelper.showDefaulteMessageProgressDialog(context);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(context).getSelfStatus().accessToken);
        params.put("othersId", userId);
        params.put("comment", comment);

        HttpUtils.post().url(CoreManager.getInstance(context).getConfig().RED_ADD_COMMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            commentCallBack.onSuccess();
                        } else {
                            commentCallBack.onFail(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        commentCallBack.onFail(e.getMessage());
                    }
                });
    }


    public interface UserCommentCallBack {

        void onSuccess(List<YourCommentBean> commentBeanList);

        void onFail(String error);

    }

    public interface AddCommentCallBack {

        void onSuccess();

        void onFail(String error);

    }

}
