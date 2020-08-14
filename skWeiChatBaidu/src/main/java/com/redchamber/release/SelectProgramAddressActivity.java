package com.redchamber.release;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.info.ResidentCityActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.release.adapter.SelectProgramAddressAdapter;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 请选择节目地点
 */
public class SelectProgramAddressActivity extends BaseActivity implements SelectProgramAddressAdapter.onAddressClickListener {

    @BindView(R.id.tv_location)
    TextView mTvLocation;
    @BindView(R.id.rv)
    RecyclerView mRvAddress;

    private SelectProgramAddressAdapter mAdapter;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_select_program_address;
    }

    @Override
    protected void initView() {
        mAdapter = new SelectProgramAddressAdapter(null);
        mRvAddress.setLayoutManager(new LinearLayoutManager(this));
        mRvAddress.setAdapter(mAdapter);
        mAdapter.setOnAddressClickListener(this);

        fakeData();
    }

    @OnClick({R.id.iv_back, R.id.tv_location, R.id.tv_undetermined})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_location:
                ResidentCityActivity.startActivityForResult(this, 1, RequestCode.REQUEST_CODE_SELECT_CITY);
//                startActivityForResult(new Intent(this, ResidentCityActivity.class), RequestCode.REQUEST_CODE_SELECT_CITY);
                break;
            case R.id.tv_undetermined:
                setResult("待定");
                break;
        }
    }

    public static void startActivityForResult(Activity context, int requestCode) {
        if (context == null) {
            return;
        }
        context.startActivityForResult(new Intent(context, SelectProgramAddressActivity.class), requestCode);
    }

    private void fakeData() {
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add("村霸蹦床主题乐园" + i);
        }
        mAdapter.setNewData(data);
    }

    @Override
    public void onAddressItemClick(String address) {
        setResult(address);
    }

    private void setResult(String address) {
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(GlobalConstants.KEY_ADDRESS, address);
        setResult(RequestCode.REQUEST_CODE_SELECT_CITY, i.putExtras(bundle));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCode.REQUEST_CODE_SELECT_CITY:
                if (data != null) {
                    String address = data.getStringExtra(GlobalConstants.KEY_CITY);
                    mTvLocation.setText(address);
                }
                break;
        }
    }

}
