package com.sk.weichat.view;

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

public class SingleVideoChatToolDialog extends Dialog implements View.OnClickListener {
    private OnSingleVideoChatToolDialog clickListener;
    private Context VContext;
    private boolean isChatToolView;

    public SingleVideoChatToolDialog(@NonNull Context context, OnSingleVideoChatToolDialog onSingleVideoChatToolDialog, boolean isChatTool) {
        super(context, R.style.BottomDialog);
        this.VContext = context;
        this.clickListener = onSingleVideoChatToolDialog;
        this.isChatToolView = isChatTool;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_single_chat_tool);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        ViewGroup root = findViewById(R.id.llRoot);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof LinearLayout) {
                child.setOnClickListener(this);
            }
        }

        if (!isChatToolView) {
            TextView tv_video_dialog = (TextView) findViewById(R.id.tv_video_dialog);
            tv_video_dialog.setText(VContext.getResources().getString(R.string.chat_video_conference));
            TextView tv_vioce_dialog = (TextView) findViewById(R.id.tv_vioce_dialog);
            tv_vioce_dialog.setText(VContext.getResources().getString(R.string.meeting));
            findViewById(R.id.llScreen).setVisibility(View.GONE);
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
            case R.id.llCancel:
                clickListener.cancleClick();
                break;

        }
    }

    public interface OnSingleVideoChatToolDialog {
        void videoClick();

        void voiceClick();

        default void screenClick() {
        }

        void cancleClick();

    }
}
