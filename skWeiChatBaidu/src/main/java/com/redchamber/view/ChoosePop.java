package com.redchamber.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sk.weichat.R;


/**
 * Created by cjh on 2016/10/28.
 */

public class ChoosePop extends PopupWindow {
 private  Button btn_cancel;
    /**拍照*/
    private TextView takepic;
    /**从手机中选择*/
    private TextView choosepic;

    private View hor;

    private View mPopVive;
    public ChoosePop(Activity context, View.OnClickListener itemsOnClick, int type) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopVive = inflater.inflate(R.layout.popchoose, null);

        btn_cancel =  mPopVive.findViewById(R.id.btn_cancle);
        hor =  mPopVive.findViewById(R.id.hor);
        takepic = mPopVive.findViewById(R.id.select_video);
        choosepic= mPopVive.findViewById(R.id.select_pic);


        if (type==1){
            takepic.setVisibility(View.GONE);
            hor.setVisibility(View.GONE);
            choosepic.setVisibility(View.VISIBLE);
        }else if (type==2){
            hor.setVisibility(View.GONE);
            takepic.setVisibility(View.VISIBLE);
            choosepic.setVisibility(View.GONE);
        }else{
            hor.setVisibility(View.VISIBLE);
            takepic.setVisibility(View.VISIBLE);
            choosepic.setVisibility(View.VISIBLE);
        }
        //取消按钮
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        takepic.setOnClickListener(itemsOnClick);
        choosepic.setOnClickListener(itemsOnClick);

        //设置SelectPicPopupWindow的View
        this.setContentView(mPopVive);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高

//        this.setHeight(500);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.anim.activity_bottom_out);
//        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
//        this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gray));
        //mPopVive添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mPopVive.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mPopVive.findViewById(R.id.poppic).getTop();
                int y=(int) event.getY();
                if(event.getAction()== MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}
