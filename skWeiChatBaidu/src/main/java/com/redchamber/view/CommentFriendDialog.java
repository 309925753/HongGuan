package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.YourCommentBean;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.CommentRequest;
import com.redchamber.view.adapter.FriendCommentAdapter;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 他的真实评论
 */
public class CommentFriendDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.rv)
    RecyclerView mRv;

    private String mUserId;
    private List<YourCommentBean> mCommentList;
    private FriendCommentAdapter mAdapter;

    public CommentFriendDialog(Context context, String userId, List<YourCommentBean> commentBeanList) {
        super(context, R.style.BaseDialogStyle);
        this.mUserId = userId;
        this.mCommentList = commentBeanList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_comment_friend);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);

        mAdapter = new FriendCommentAdapter(mCommentList);
        mRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRv.setAdapter(mAdapter);
    }

    @OnClick({R.id.btn_comment, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_comment:
                commentRequest();
                break;
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

    private void commentRequest() {
        if (mCommentList == null || mCommentList.size() <= 0) {
            return;
        }
        String commentStr = "";
        for (YourCommentBean commentBean : mCommentList) {
            if (commentBean.isChecked) {
                commentStr += commentBean.type + ";";
            }
        }
        if (TextUtils.isEmpty(commentStr)) {
            ToastUtils.showToast("请选择评价");
            return;
        }
        commentStr = commentStr.substring(0, commentStr.length() - 1);
        CommentRequest.getInstance().addUserComment(getContext(), mUserId, commentStr, new CommentRequest.AddCommentCallBack() {
            @Override
            public void onSuccess() {
                ToastUtils.showToast("评论成功");
                dismiss();
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

}
