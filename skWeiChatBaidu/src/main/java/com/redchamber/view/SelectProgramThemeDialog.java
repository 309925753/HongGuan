package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.redchamber.release.ReleaseProgramActivity;
import com.sk.weichat.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 选择节目主题
 */
public class SelectProgramThemeDialog extends Dialog {

    private Unbinder mBinder;

    public SelectProgramThemeDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_select_program_theme);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        setCanceledOnTouchOutside(true);
    }

    @OnClick({R.id.iv_ktv, R.id.iv_travel, R.id.iv_movie, R.id.iv_food, R.id.iv_game, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_ktv:
                ReleaseProgramActivity.startActivity(getContext(), "约K歌");
                break;
            case R.id.iv_travel:
                ReleaseProgramActivity.startActivity(getContext(), "约旅游");
                break;
            case R.id.iv_movie:
                ReleaseProgramActivity.startActivity(getContext(), "约电影");
                break;
            case R.id.iv_food:
                ReleaseProgramActivity.startActivity(getContext(), "约美食");
                break;
            case R.id.iv_game:
                ReleaseProgramActivity.startActivity(getContext(), "约游戏");
                break;
            case R.id.iv_close:
                break;
        }
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

}
