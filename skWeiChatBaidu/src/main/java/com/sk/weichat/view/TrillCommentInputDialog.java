package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.sk.weichat.R;

/**
 * 生活圈 | 短视频模块 评论&回复 输入框
 */
public class TrillCommentInputDialog extends Dialog {

    private EditText mEditText;
    private ImageView mImageView;
    private String mReplyStr;
    private OnSendCommentListener mOnSendCommentListener;

    public TrillCommentInputDialog(Context context, String text, OnSendCommentListener onSendCommentListener) {
        super(context, R.style.BottomDialog);
        this.mReplyStr = text;
        this.mOnSendCommentListener = onSendCommentListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_dialog_for_trill_comment_input);
        setCanceledOnTouchOutside(true);
        initView();
    }

    public void initView() {
        mImageView = findViewById(R.id.send_iv);
        mEditText = findViewById(R.id.comment_et);
        if (!TextUtils.isEmpty(mReplyStr)) {
            mEditText.setHint(mReplyStr);
        }
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mCommentStr = mEditText.getText().toString();
                if (TextUtils.isEmpty(mCommentStr)) {
                    return;
                }
                if (mOnSendCommentListener != null) {
                    dismiss();
                    mOnSendCommentListener.sendComment(mCommentStr);
                }
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(mEditText.getText().toString())) {
                    mImageView.setImageResource(R.drawable.ic_tiktok_send_ing);
                } else {
                    mImageView.setImageResource(R.drawable.ic_tiktok_send_nor);
                }
            }
        });
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        // window.setWindowAnimations(R.style.BottomDialog_Animation);
    }

    public interface OnSendCommentListener {
        void sendComment(String str);
    }
}
