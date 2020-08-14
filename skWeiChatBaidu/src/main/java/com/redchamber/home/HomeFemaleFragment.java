package com.redchamber.home;

import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.redchamber.home.adapter.MyFragmentPagerAdapter;
import com.redchamber.home.fragment.DebutanteFragment;
import com.redchamber.home.fragment.GirlFragment;
import com.redchamber.home.fragment.NearFragment;
import com.redchamber.home.fragment.NewerFragment;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.search.SearchActivity;
import com.redchamber.view.SelectAreaDialog;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 首页-红馆
 */
public class HomeFemaleFragment extends BaseFragment {

    @BindView(R.id.rg_type)
    RadioGroup mRgType;
    @BindView(R.id.rb_near)
    RadioButton mRbNear;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.tv_location)
    TextView mTvLocation;
    @BindView(R.id.tv_online)
    TextView mTvOnLine;

    private int position;
    private String mIsOnline = "1";
    private String mCityName = "上海市";

    private NearFragment mNearFragment = new NearFragment();
    private NewerFragment mNewerFragment = new NewerFragment();
    private GirlFragment mGirlFragment = new GirlFragment();
    private DebutanteFragment mDebutanteFragment = new DebutanteFragment();

    @Override
    protected int setLayout() {
        return R.layout.fragment_home_female;
    }

    @Override
    protected void initView() {
        setListener();
        initFragment();
    }

    @OnClick({R.id.iv_search, R.id.tv_location, R.id.tv_online})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                if (getContext() != null) {
                    getContext().startActivity(new Intent(getContext(), SearchActivity.class));
                }
                break;
            case R.id.tv_location:
                SelectAreaDialog selectAreaDialog = new SelectAreaDialog(getContext());
                selectAreaDialog.show();
                selectAreaDialog.setOnConfirmListener(new SelectAreaDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String city) {
                        mCityName = city;
                        mTvLocation.setText(city);
                        updateUserList();
                    }
                });
                break;
            case R.id.tv_online:
                if ("1".equals(mIsOnline)) {
                    mIsOnline = "0";
                    mTvOnLine.setTextColor(getResources().getColor(R.color.color_666666));
                    mTvOnLine.setBackground(getResources().getDrawable(R.drawable.shape_bg_online_unchecked));
                } else {
                    mTvOnLine.setTextColor(getResources().getColor(R.color.color_FB719A));
                    mTvOnLine.setBackground(getResources().getDrawable(R.drawable.shape_bg_online_checked));
                    mIsOnline = "1";
                }

                updateUserList();
                break;
        }
    }

    private void initFragment() {
        List<BaseFragment> alFragment = new ArrayList<>(4);
        alFragment.add(mNearFragment);
        alFragment.add(mNewerFragment);
        alFragment.add(mGirlFragment);
        alFragment.add(mDebutanteFragment);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), alFragment));
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mRgType.check(R.id.rb_near);
                        break;
                    case 1:
                        mRgType.check(R.id.rb_newer);
                        break;
                    case 2:
                        mRgType.check(R.id.rb_girl);
                        break;
                    case 3:
                        mRgType.check(R.id.rb_debutante);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setListener() {
        mRgType.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        mRbNear.performClick();
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_near://附近
                    position = 0;
                    break;
                case R.id.rb_newer://新注册类
                    position = 1;
                    break;
                case R.id.rb_girl://女神
                    position = 2;
                    break;
                case R.id.rb_debutante://名媛
                    position = 3;
                    break;
                default:
            }
            mViewPager.setCurrentItem(position, true);
        }
    }

    private void updateUserList() {
        mNearFragment.getNearIndexUser(mIsOnline, mCityName);
        mNewerFragment.getNewerIndexUser(mIsOnline, mCityName);
        mGirlFragment.getGirlIndexUser(mIsOnline, mCityName);
        mDebutanteFragment.getDebutanteIndexUser(mIsOnline, mCityName);
    }

}
