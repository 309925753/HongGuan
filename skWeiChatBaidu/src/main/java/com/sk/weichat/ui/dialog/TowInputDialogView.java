package com.sk.weichat.ui.dialog;

import android.app.Activity;
import android.text.InputFilter;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.dialog.base.BaseDialog;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.view.SwitchButton;

/**
 * Created by Administrator on 2016/4/21.
 */
public class TowInputDialogView extends BaseDialog {

    private TextView mTitleTv;
    private AutoCompleteTextView mContentEt;
    private AutoCompleteTextView mSecondEt;
    private Button mCommitBtn;

    private SwitchButton mSbSecretGroup;
    private int isSecretGroup = 0;

    private int isRead = 0; // 0不显示 1显示(default - 不显示)
    private int isLook = 1;// 0公开 1不公开(default - 不公开)
    private int isNeedVerify = 0;    // 0不需要 1需要(default - 不需要)
    private int isShowMember = 1;    // 0不显示 1显示(default - 显示)
    private int isAllowSendCard = 1; // 0不允许 1允许(default - 公开)
    private onSureClickLinsenter mOnClickListener;

    {
        RID = R.layout.dialog_double_input;
    }

    public TowInputDialogView(Activity activity, String title, String hint, String hint2, onSureClickLinsenter onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, hint2);
        mOnClickListener = onClickListener;
    }

    public TowInputDialogView(Activity activity, String title,
                              String hint, String hint2, String text, String text2, onSureClickLinsenter onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, hint2, text, text2);
        mOnClickListener = onClickListener;
    }

    protected void initView() {
        super.initView();
        mCanceled = false;// 点击空白地方不取消

        mTitleTv = (TextView) mView.findViewById(R.id.title);
        mContentEt = (AutoCompleteTextView) mView.findViewById(R.id.content);
        mContentEt.setFilters(new InputFilter[]{DialogHelper.mExpressionFilter, DialogHelper.mChineseEnglishNumberFilter});
        mSecondEt = (AutoCompleteTextView) mView.findViewById(R.id.second_et);
        mSecondEt.setFilters(new InputFilter[]{DialogHelper.mExpressionFilter, DialogHelper.mChineseEnglishNumberFilter});
        mCommitBtn = (Button) mView.findViewById(R.id.sure_btn);
        ButtonColorChange.textChange(mActivity, mView.findViewById(R.id.tv_input_room_name));
        ButtonColorChange.textChange(mActivity, mView.findViewById(R.id.tv_input_room_desc));
        ButtonColorChange.colorChange(mActivity, mCommitBtn);

        if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
            // todo SecureFlaGroup 私密群组相关均未做IS_SUPPORT_SECURE_CHAT判断，只判断了isSecretGroup，此处为私密群组的源头...
            //  GroupManager判断了重置chatKey item
            mView.findViewById(R.id.secret_group_rl).setVisibility(View.VISIBLE);
            mSbSecretGroup = mView.findViewById(R.id.switch_secret_group);
            mSbSecretGroup.setOnCheckedChangeListener((view, isChecked) -> isSecretGroup = isChecked ? 1 : 0);
        }
    }

    private void setView(String title, String hint, String hint2) {
        mTitleTv.setText(title);
        mContentEt.setHint(hint);
        mSecondEt.setVisibility(View.VISIBLE);
        mSecondEt.setHint(hint2);

        mCommitBtn.setOnClickListener(v -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mContentEt, mSecondEt, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard, isSecretGroup);
            }
        });
    }

    private void setView(String title, String hint, String hint2, String text, String text2) {
        mTitleTv.setText(title);
        mContentEt.setHint(hint);
        mContentEt.setText(text);
        mSecondEt.setVisibility(View.VISIBLE);
        mSecondEt.setHint(hint2);
        mSecondEt.setText(text2);
        mCommitBtn.setOnClickListener(v -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mContentEt, mSecondEt, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard, isSecretGroup);
            }
        });
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    // 这里有两个EditText，比较特殊，所以单击事件监听器也需要传两个EditText过去
    public interface onSureClickLinsenter {
        void onClick(EditText e1, EditText e2, int isRead, int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard, int isSecretGroup);
    }
}
