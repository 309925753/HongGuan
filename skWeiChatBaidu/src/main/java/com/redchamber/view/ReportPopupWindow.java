package com.redchamber.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.sk.weichat.R;

public class ReportPopupWindow extends PopupWindow {

    private   BtnReportOnClick btnReportOnClick;

    public interface BtnReportOnClick {
        void btnReportOnClick(int type);
    }
    public void setBtnOnClice(BtnReportOnClick btnReportOnClick) {
        this.btnReportOnClick = btnReportOnClick;

    }
    public ReportPopupWindow( FragmentActivity context,String  comm,String  dynamic,View v) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.red_popupwindow_release_select, null);

        TextView tv_comm = (TextView) mMenuView.findViewById(R.id.tv_comm);
        TextView tv_dynamic = (TextView) mMenuView.findViewById(R.id.tv_dynamic);
        tv_comm.setText(comm);
        tv_dynamic.setText(dynamic);

        this.setFocusable(true);// 取得焦点
        this.setContentView(mMenuView);
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(context.getDrawable(R.drawable.dialog_style_bg));
        //点击外部消失
        this.setOutsideTouchable(true);
        this.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);
        this.showAsDropDown(v);
        tv_comm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReportOnClick.btnReportOnClick(1);
                dismiss();
            }
        });
        tv_dynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReportOnClick.btnReportOnClick(2);
                dismiss();
            }
        });



    }
}
