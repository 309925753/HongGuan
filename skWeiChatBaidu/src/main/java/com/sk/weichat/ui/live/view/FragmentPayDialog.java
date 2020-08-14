package com.sk.weichat.ui.live.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.sk.weichat.R;

public class FragmentPayDialog extends DialogFragment {
    public OnClickBottomListener onClickBottomListener;
    /**
     * 取消和确认按钮
     */
    private Button negtiveBn, positiveBn;
    /**
     * 按钮之间的分割线
     */
    private View columnLineView;
    /**
     * 底部是否只有一个按钮
     */
    private boolean isSingle = false;
    private Dialog dialog;

    public static final FragmentPayDialog newInstance(OnClickBottomListener onClickBottomListener) {
        FragmentPayDialog fragment = new FragmentPayDialog();
        fragment.onClickBottomListener = onClickBottomListener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.common_pay_dialog_layout, null, false);
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙
        initDialogStyle(rootView);
        initView(rootView);
        initEvent();
        return dialog;
    }

    private void initDialogStyle(View view) {
        dialog = new Dialog(getActivity(), R.style.CustomDialog);
        // 设置Content前设定,(自定义标题,当需要自定义标题时必须指定)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        // 外部点击取消
        dialog.setCanceledOnTouchOutside(true);
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.Buttom_Popwindow);
        WindowManager.LayoutParams lp = window.getAttributes();
        // 中间显示
        lp.gravity = Gravity.BOTTOM;
        // 宽度持平
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    private void initView(View rootView) {
        positiveBn = (Button) rootView.findViewById(R.id.positive);
        columnLineView = rootView.findViewById(R.id.column_line);
        negtiveBn = (Button) rootView.findViewById(R.id.negtive);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView message = (TextView) rootView.findViewById(R.id.message);
        title.setText(getString(R.string.title_balance_not_enough));
        message.setText(getString(R.string.hint_balance_not_enough));
        positiveBn.setText(getString(R.string.recharge));
        negtiveBn.setText(getString(R.string.near_cancel));
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onClickBottomListener != null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        // 设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onClickBottomListener != null) {
                    onClickBottomListener.onNegativeClick();
                }
            }
        });
    }

    public interface OnClickBottomListener {
        void onPositiveClick();

        void onNegativeClick();
    }
}
