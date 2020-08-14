package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

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
 * 发布时间、地区
 */
public class BarTimeAreaSelectDialog extends Dialog implements SelectAreaAdapter.onProvinceClickListener,
        SelectCityAdapter.onCityClickListener {

    private Unbinder mBinder;

    @BindView(R.id.tv_time)
    TextView mTvTime;
    @BindView(R.id.tv_area)
    TextView mTvArea;
    @BindView(R.id.ll_area)
    LinearLayout mLlArea;
    @BindView(R.id.rv_province)
    RecyclerView mRvProvince;
    @BindView(R.id.rv_city)
    RecyclerView mRvCity;
    @BindView(R.id.ll_time)
    LinearLayout mLlTime;
    @BindView(R.id.tv_release_time)
    TextView mTvReleaseTime;
    @BindView(R.id.tv_activity_time)
    TextView mTvActivityTime;

    public final static int TYPE_TIME = 0;
    public final static int TYPE_AREA = 1;

    private SelectAreaAdapter mProvinceAdapter;
    private SelectCityAdapter mCityAdapter;
    private ArrayList<ProvinceCityBean> mProvinceCityBeans = new ArrayList<>();
    private int mProvincePosition;
    private String mSelectCity;

    private int mType;
    private BtnReleaseOnClick btnOnClick;

    public interface BtnReleaseOnClick {
        void btnOnClick(String data);
    }

    public void setBtnOnClice(BtnReleaseOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }

    private BtnCityOnClick btnCityOnClick;

    public interface BtnCityOnClick {
        void btnCityOnClick(String data);
    }

    public void setBtnOnClice(BtnCityOnClick btnCityOnClick) {
        this.btnCityOnClick = btnCityOnClick;

    }


    public BarTimeAreaSelectDialog(Context context, int type) {
        super(context, R.style.BaseDialogStyle);
        this.mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_release_time_area_select);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(true);

        showType(mType);
        initProvinceCityData();
    }

    @OnClick({R.id.tv_time, R.id.tv_area, R.id.tv_release_time, R.id.tv_activity_time})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_time:
//                showType(TYPE_TIME);
                dismiss();
                break;
            case R.id.tv_area:
//                showType(TYPE_AREA);
                dismiss();
                break;
            case R.id.tv_release_time:
                //   ToastUtils.showToast("发布时间");
                btnOnClick.btnOnClick(String.valueOf(0));
                dismiss();
                break;
            case R.id.tv_activity_time:
                //    ToastUtils.showToast("活动时间");
                btnOnClick.btnOnClick(String.valueOf(1));
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

    @Override
    public void onProvinceItemClick(int position) {
        mProvincePosition = position;
        mCityAdapter.setNewData(mProvinceCityBeans.get(position).city);
    }

    @Override
    public void onCityItemClick(int position) {
        mSelectCity = mProvinceCityBeans.get(mProvincePosition).city.get(position).name;
        //  ToastUtils.showToast(mSelectCity);
        btnCityOnClick.btnCityOnClick(mSelectCity);
        dismiss();
    }

    private void showType(int type) {
        if (TYPE_TIME == type) {
            mLlArea.setVisibility(View.GONE);
            mLlTime.setVisibility(View.VISIBLE);
        } else if (TYPE_AREA == type) {
            mLlArea.setVisibility(View.VISIBLE);
            mLlTime.setVisibility(View.GONE);
        }
    }


    private void initProvinceCityData() {
        ProvinceCityBean provinceCityBeanNear = new ProvinceCityBean();
        provinceCityBeanNear.name = "不限地区";
        ProvinceCityBean.CityBean cityBean = new ProvinceCityBean.CityBean();
        cityBean.name = "不限地区";
        provinceCityBeanNear.city = new ArrayList<>();
        provinceCityBeanNear.city.add(cityBean);
        mProvinceCityBeans.add(provinceCityBeanNear);

        mProvinceAdapter = new SelectAreaAdapter(null);
        mRvProvince.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvProvince.setAdapter(mProvinceAdapter);
        mProvinceAdapter.setOnProvinceClickListener(this);

        mCityAdapter = new SelectCityAdapter(null);
        mRvCity.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvCity.setAdapter(mCityAdapter);
        mCityAdapter.setOnCityClickListener(this);

        String JsonData = GetJsonDataUtil.getJson(getContext(), "province.json");
        mProvinceCityBeans.addAll(GetJsonDataUtil.parseProvinceCityBean(JsonData));

        mProvinceAdapter.setNewData(mProvinceCityBeans);
        mCityAdapter.setNewData(mProvinceCityBeans.get(0).city);
    }


}
