package com.sk.weichat.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.ui.live.bean.Member;

/**
 * Create Company pop
 */
public class ManagerPopupWindow extends PopupWindow {
    private TextView mSetManager, mShutUp, mKick, mCancel;
    private View mMenuView;

    public ManagerPopupWindow(FragmentActivity context, OnClickListener itemsOnClick, Member self, Member member) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.manager_liveroom_dialog, null);
        mSetManager = mMenuView.findViewById(R.id.set_manager);
        mShutUp = mMenuView.findViewById(R.id.shut_up);
        mKick = mMenuView.findViewById(R.id.kick_room);
        mCancel = mMenuView.findViewById(R.id.cancel);
        if (self.getType() == Member.TYPE_OWNER) {
            if (member.getType() == Member.TYPE_MANAGER) {
                mSetManager.setText(R.string.cancel_admin);
            } else {
                mSetManager.setText(R.string.design_admin);
            }
        } else {
            mSetManager.setVisibility(View.GONE);
        }
        if (member.getState() == 0) {
            mShutUp.setText(MyApplication.getInstance().getString(R.string.ban));
        } else {
            // 已经处于禁言状态
            mShutUp.setText(MyApplication.getInstance().getString(R.string.live_gag_cancel));
        }
        mKick.setText(MyApplication.getInstance().getString(R.string.live_kick));
        mCancel.setText(MyApplication.getInstance().getString(R.string.cancel));
        mSetManager.setOnClickListener(itemsOnClick);
        mShutUp.setOnClickListener(itemsOnClick);
        mKick.setOnClickListener(itemsOnClick);
        mCancel.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //	        this.setWidth(ViewPiexlUtil.dp2px(context,200));
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.alp_background));
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }
}
