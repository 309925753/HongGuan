package com.sk.weichat.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.dialog.base.BaseDialog;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.ToastUtil;

/**
 * Created by Administrator on 2016/4/21.
 * 创建课程的提示框
 */
public class CreateCourseDialog extends BaseDialog {
    private TextView mTitleTv;
    private EditText mContentEt;
    private Button mCommitBtn;

    private CoureseDialogConfirmListener mOnClickListener;

    {
        RID = R.layout.dialog_single_input;
    }

    public CreateCourseDialog(Activity activity, CoureseDialogConfirmListener listener) {
        mActivity = activity;
        initView();
        mOnClickListener = listener;
    }

    protected void initView() {
        super.initView();
        mTitleTv = (TextView) mView.findViewById(R.id.title);
        mContentEt = mView.findViewById(R.id.content);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mContentEt.requestFocus();
                InputMethodManager mInputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 0);
        mView.findViewById(R.id.public_rl).setVisibility(View.GONE);
        mCommitBtn = (Button) mView.findViewById(R.id.sure_btn);
        ButtonColorChange.colorChange(mActivity, mCommitBtn);
        mCommitBtn.setText(mActivity.getString(R.string.sure));

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    String content = mContentEt.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.showToast(mActivity, mActivity.getString(R.string.name_course_error));
                    } else {
                        CreateCourseDialog.this.mDialog.dismiss();
                        mOnClickListener.onClick(content);
                    }
                }
            }
        });

        mTitleTv.setText(mActivity.getString(R.string.coursename));
        mContentEt.setHint(mActivity.getString(R.string.input_course_name));
    }

    @Override
    public BaseDialog show() {
        mContentEt.setFocusable(true);
        mContentEt.setFocusableInTouchMode(true);
        return super.show();
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setHint(String hint) {
        mContentEt.setHint(hint);
    }

    public void setMaxLines(int maxLines) {
        mContentEt.setMaxLines(maxLines);
    }

    public String getContent() {
        return mContentEt.getText().toString();
    }

    // 外面需要对两个EditText做操作，给获取方法
    public EditText getE1() {
        return mContentEt;
    }

    public interface CoureseDialogConfirmListener {
        void onClick(String content);
    }
}
