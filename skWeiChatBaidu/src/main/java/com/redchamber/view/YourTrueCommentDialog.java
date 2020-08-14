package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.YourCommentBean;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.CommentRequest;
import com.redchamber.view.adapter.YourCommentAdapter;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 你的真实评价
 */
public class YourTrueCommentDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.rv)
    RecyclerView mRv;

    private YourCommentAdapter mAdapter;

    public YourTrueCommentDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_your_true_comment);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);

        mAdapter = new YourCommentAdapter(getContext(), null);
        mRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRv.setAdapter(mAdapter);
        getMyComment();
    }

    @OnClick(R.id.iv_close)
    void onClick(View view) {
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

    private void getMyComment() {
        CommentRequest.getInstance().getUserComment(getContext(), "", new CommentRequest.UserCommentCallBack() {
            @Override
            public void onSuccess(List<YourCommentBean> commentBeanList) {
                mAdapter.setNewData(commentBeanList);
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

}
