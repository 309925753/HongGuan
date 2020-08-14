package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.ProvinceCityBean;
import com.redchamber.util.GetJsonDataUtil;
import com.redchamber.view.adapter.SelectAreaAdapter;
import com.redchamber.view.adapter.SelectCityAdapter;
import com.sk.weichat.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 首页-红馆-范围选择
 */
public class SelectAreaDialog extends Dialog implements SelectAreaAdapter.onProvinceClickListener,
        SelectCityAdapter.onCityClickListener {

    private Unbinder mBinder;

    @BindView(R.id.rv_area)
    RecyclerView mRvArea;
    @BindView(R.id.rv_city)
    RecyclerView mRvCity;

    private SelectAreaAdapter mAreaAdapter;
    private SelectCityAdapter mCityAdapter;
    private ArrayList<ProvinceCityBean> mProvinceCityBeans = new ArrayList<>();
    private OnConfirmListener mOnConfirmListener;
    private int mProvincePosition;
    private String mSelectCity = "北京市";

    public SelectAreaDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_select_dialog);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);

        initData();
        initProvinceCityData();
    }

    @OnClick(R.id.tv_confirm)
    void onClick(View view) {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onConfirmClick(mSelectCity);
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

    private void initData() {
        mAreaAdapter = new SelectAreaAdapter(null);
        mRvArea.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvArea.setAdapter(mAreaAdapter);
        mAreaAdapter.setOnProvinceClickListener(this);

        mCityAdapter = new SelectCityAdapter(null);
        mRvCity.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvCity.setAdapter(mCityAdapter);
        mCityAdapter.setOnCityClickListener(this);
    }

    private void initProvinceCityData() {
        ProvinceCityBean provinceCityBeanNear = new ProvinceCityBean();
        provinceCityBeanNear.name = "附近";
        ProvinceCityBean.CityBean cityBean = new ProvinceCityBean.CityBean();
        cityBean.name = "附近";
        provinceCityBeanNear.city = new ArrayList<>();
        provinceCityBeanNear.city.add(cityBean);
        mProvinceCityBeans.add(provinceCityBeanNear);

        ProvinceCityBean provinceCityBeanResident = new ProvinceCityBean();
        provinceCityBeanResident.name = "常驻城市";
        ProvinceCityBean.CityBean cityBean2 = new ProvinceCityBean.CityBean();
        cityBean2.name = "常驻城市";
        provinceCityBeanResident.city = new ArrayList<>();
        provinceCityBeanResident.city.add(cityBean2);
        mProvinceCityBeans.add(provinceCityBeanResident);

        String JsonData = GetJsonDataUtil.getJson(getContext(), "province.json");
        mProvinceCityBeans.addAll(GetJsonDataUtil.parseProvinceCityBean(JsonData));

        mAreaAdapter.setNewData(mProvinceCityBeans);
        mCityAdapter.setNewData(mProvinceCityBeans.get(0).city);
    }


    @Override
    public void onProvinceItemClick(int position) {
        mProvincePosition = position;
        mCityAdapter.setNewData(mProvinceCityBeans.get(position).city);
    }

    @Override
    public void onCityItemClick(int position) {
        mSelectCity = mProvinceCityBeans.get(mProvincePosition).city.get(position).name;
    }

    public interface OnConfirmListener {
        void onConfirmClick(String city);
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }
    
    private void aa(){

    }

}
