package com.redchamber.photo;


import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.photo.adapter.PreviewPhotosAdapter;
import com.redchamber.request.PhotoSettingRequest;
import com.redchamber.view.widget.CountDownView;
import com.sk.weichat.R;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 相册
 */
public class BarPhotoActivity  extends BaseActivity {

    @BindView(R.id.countDownView)
    CountDownView mCountDownView;
    @BindView(R.id.vp_photo)
    ViewPager mVpPhoto;
    @BindView(R.id.tv_index)
    TextView mTvIndex;
    @BindView(R.id.cb_fire)
    CheckBox mCbFire;
    @BindView(R.id.ll_fire)
    LinearLayout mLlFire;

    private PreviewPhotosAdapter mAdapter;
    private int mIndex = 0; //当前图片下标

    private List<PhotoBean> mPhotoBeans;

    @Override
    protected int setLayout() {
        return R.layout.activity_bar_photo;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        if (getIntent() != null) {
            mIndex = getIntent().getIntExtra(GlobalConstants.KEY_INDEX, 0);
            mPhotoBeans = (List<PhotoBean>) getIntent().getSerializableExtra(GlobalConstants.KEY_PHOTO_LIST);
        }

        initViewPager();
        mCountDownView.setCountdownTime(5);
    }

    @OnClick({R.id.iv_back, R.id.tv_delete, R.id.ll_fire})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.ll_fire:
                int visitType = showIsFire() ? 0 : 1;
                PhotoSettingRequest.getInstance().setPhoto(this, mPhotoBeans.get(mIndex).photoId, String.valueOf(visitType),
                        "", new PhotoSettingRequest.SettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mCbFire.setChecked(!showIsFire());
                                mPhotoBeans.get(mIndex).visitType = visitType;
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
                break;
        }
    }

    public static void startActivity(Context context, List<PhotoBean> photoBeans, int index) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, BarPhotoActivity.class);
        intent.putExtra(GlobalConstants.KEY_INDEX, index);
        intent.putExtra(GlobalConstants.KEY_PHOTO_LIST, (Serializable) photoBeans);
        context.startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private void initViewPager() {

        mTvIndex.setText(String.format("1/%d", mPhotoBeans.size()));
        mAdapter = new PreviewPhotosAdapter(this, mPhotoBeans);
        mVpPhoto.setAdapter(mAdapter);
        mVpPhoto.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTvIndex.setVisibility(View.VISIBLE);
                mTvIndex.setText(position + 1 + "/" + mPhotoBeans.size());
                mIndex = position;
                showIsFire();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mVpPhoto.setCurrentItem(mIndex);
        showIsFire();
    }

    private boolean showIsFire() {
        if (mIndex < 0) {
            return false;
        }
     //   mCbFire.setChecked(1 == mPhotoBeans.get(mIndex).visitType);
        return mCbFire.isChecked();
    }



}

