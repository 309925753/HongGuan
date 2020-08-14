package com.sk.weichat.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sk.weichat.R;
import com.sk.weichat.call.ScreenModeHelper;
import com.sk.weichat.util.ScreenUtil;

public class GroupVideoChatToolDialog extends Dialog implements View.OnClickListener {
    private OnVideoChatToolDialogClickListener clickListener;
    private Context VContext;

    public GroupVideoChatToolDialog(@NonNull Context context, OnVideoChatToolDialogClickListener onVideoChatToolDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.VContext = context;
        this.clickListener = onVideoChatToolDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_chat_tool);
        setCanceledOnTouchOutside(true);
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ViewGroup root = findViewById(R.id.llRoot);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof LinearLayout) {
                child.setOnClickListener(this);
            }
        }

        TextView tvTalkAlign = findViewById(R.id.tvTalkAlign);
        if (tvTalkAlign.getText().length() == 3) {
            // 针对中文情况，对齐，
            tvTalkAlign.setText(tvTalkAlign.getText() + "　");
        }

        if (!ScreenModeHelper.isEnable()) {
           findViewById(R.id.llScreen).setVisibility(View.GONE);
        }

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.llVideo:
                clickListener.videoClick();
                break;
            case R.id.llAudio:
                clickListener.voiceClick();
                break;
            case R.id.llScreen:
                clickListener.screenClick();
                break;
            case R.id.llTalk:
                clickListener.talkClick();
                break;
            case R.id.llCancel:
                clickListener.cancleClick();
                break;

        }
    }

    public interface OnVideoChatToolDialogClickListener {
        void videoClick();

        void voiceClick();

        void screenClick();

        void talkClick();

        void cancleClick();

    }
}
