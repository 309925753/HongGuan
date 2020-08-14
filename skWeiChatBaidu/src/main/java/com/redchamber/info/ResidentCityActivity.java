package com.redchamber.info;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.info.adapter.ResidentCityAdapter;
import com.redchamber.info.adapter.ResidentProvinceAdapter;
import com.redchamber.info.adapter.SelectCityTagAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.CityUtils;
import com.sk.weichat.R;
import com.sk.weichat.bean.Area;
import com.sk.weichat.db.dao.AreasDao;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 常驻城市
 */
public class ResidentCityActivity extends BaseActivity implements ResidentProvinceAdapter.onProvinceClickListener,
        ResidentCityAdapter.onCityClickListener {

    @BindView(R.id.rv_province)
    RecyclerView mRvProvince;
    @BindView(R.id.rv_city)
    RecyclerView mRvCity;
    @BindView(R.id.tfl_city)
    TagFlowLayout mTLFCity;

    //    private ArrayList<ProvinceCityBean> mProvinceCityBeans;
    private List<Area> mProvinceList;
    private ResidentProvinceAdapter mProvinceAdapter;
    private ResidentCityAdapter mCityAdapter;
    private int mProvincePosition;
    private ArrayList<String> mSelectCity = new ArrayList<>();
    private SelectCityTagAdapter mCityTagAdapter;

    private int mMaxSelect = 4;

    @Override
    protected int setLayout() {
        return R.layout.activity_resident_city;
    }

    public ResidentCityActivity() {
        noLoginRequired();
    }

    @Override
    protected void initView() {
        mProvinceAdapter = new ResidentProvinceAdapter(null);
        mRvProvince.setLayoutManager(new LinearLayoutManager(this));
        mRvProvince.setAdapter(mProvinceAdapter);
        mProvinceAdapter.setOnProvinceClickListener(this);

        mCityAdapter = new ResidentCityAdapter(null);
        mRvCity.setLayoutManager(new LinearLayoutManager(this));
        mRvCity.setAdapter(mCityAdapter);
        mCityAdapter.setOnCityClickListener(this);

        mCityTagAdapter = new SelectCityTagAdapter(this, mSelectCity);
        mTLFCity.setAdapter(mCityTagAdapter);
        mTLFCity.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                mSelectCity.remove(position);
                mCityTagAdapter.notifyDataChanged();
                return false;
            }
        });

        if (getIntent() != null) {
            mMaxSelect = getIntent().getIntExtra(GlobalConstants.KEY_MAX_SELECT, 4);
        }

        initProvinceCityData();
    }

    @OnClick({R.id.iv_back, R.id.tv_confirm})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_confirm:
                Intent i = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(GlobalConstants.KEY_CITY, CityUtils.formatCity(mSelectCity));
                setResult(RequestCode.REQUEST_CODE_SELECT_CITY, i.putExtras(bundle));
                finish();
                break;
        }
    }

    private void initProvinceCityData() {
//        String JsonData = GetJsonDataUtil.getJson(this, "province.json");
//        mProvinceCityBeans = GetJsonDataUtil.parseProvinceCityBean(JsonData);
        mProvinceList = AreasDao.getInstance().getAreasByTypeAndParentId(Area.AREA_TYPE_PROVINCE, Area.AREA_DATA_CHINA_ID);
        mProvinceAdapter.setNewData(mProvinceList);
        List<Area> mCityList = AreasDao.getInstance().getAreasByTypeAndParentId(Area.AREA_TYPE_CITY, mProvinceList.get(0).getId());
        mCityAdapter.setNewData(mCityList);
    }

    @Override
    public void onProvinceItemClick(int position) {
        mProvincePosition = position;
        List<Area> mCityList = AreasDao.getInstance().getAreasByTypeAndParentId(Area.AREA_TYPE_CITY, mProvinceList.get(mProvincePosition).getId());
        mCityAdapter.setNewData(mCityList);
    }

    @Override
    public void onCityItemClick(String city) {
        if (mSelectCity.size() >= mMaxSelect) {
            ToastUtils.showToast("最多选择" + mMaxSelect + "个城市哦");
            return;
        }
        if (!mSelectCity.contains(city)) {
            mSelectCity.add(city);
            mCityTagAdapter.notifyDataChanged();
        }
    }

    public static void startActivityForResult(Activity activity, int maxSelect, int requestCode) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResidentCityActivity.class);
        intent.putExtra(GlobalConstants.KEY_MAX_SELECT, maxSelect);
        activity.startActivityForResult(intent, requestCode);
    }

}
