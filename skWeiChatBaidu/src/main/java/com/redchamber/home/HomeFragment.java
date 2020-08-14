package com.redchamber.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.HomeHeaderBean;
import com.redchamber.bean.MyHomepageBean;
import com.redchamber.event.UpdateCityOnlineEvent;
import com.redchamber.home.adapter.HomeHeaderAdapter;
import com.redchamber.home.adapter.MyFragmentPagerAdapter;
import com.redchamber.home.fragment.HomeBaseFragment;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.PersonalInfoRequest;
import com.redchamber.search.SearchActivity;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.view.SelectAreaDialog;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 红馆
 */
public class HomeFragment extends BaseFragment {

    @BindView(R.id.rv_head)
    RecyclerView mRvHead;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    @BindView(R.id.tv_location)
    TextView mTvLocation;
    @BindView(R.id.tv_online)
    TextView mTvOnLine;

    private HomeHeaderAdapter mHeaderAdapter;
    private List<HomeHeaderBean> mHeaderList = new ArrayList<>();
    private List<BaseFragment> fragments = new ArrayList<>();
    private int mCurrentPosition = 0;

    private String mOnlineFirst = "0";
    private String mCityName = ""; //附近

    @Override
    protected int setLayout() {
        return R.layout.red_fragment_home;
    }

    @Override
    protected void initView() {
        setHeaders();
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
                if ("1".equals(mOnlineFirst)) {
                    mOnlineFirst = "0";
                    mTvOnLine.setTextColor(getResources().getColor(R.color.color_666666));
                    mTvOnLine.setBackground(getResources().getDrawable(R.drawable.shape_bg_online_unchecked));
                } else {
                    mTvOnLine.setTextColor(getResources().getColor(R.color.color_FB719A));
                    mTvOnLine.setBackground(getResources().getDrawable(R.drawable.shape_bg_online_checked));
                    mOnlineFirst = "1";
                }

                updateUserList();
                break;
        }
    }

    private void setHeaders() {
        PersonalInfoRequest.getInstance().getMyHomepage(getContext(), new PersonalInfoRequest.PersonalInfoCallBack() {
            @Override
            public void onSuccess(MyHomepageBean myHomepageBean) {
                MyApplication.mMyHomepageBean = myHomepageBean;
                setFragmentList(Objects.requireNonNull(UserLevelUtils.getLevels(myHomepageBean.userLevel))[0]);
                mHeaderAdapter = new HomeHeaderAdapter(getContext(), mHeaderList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                mRvHead.setLayoutManager(layoutManager);
                mRvHead.setAdapter(mHeaderAdapter);
                mHeaderAdapter.setOnHeaderClickListener(new HomeHeaderAdapter.onHeaderClickListener() {
                    @Override
                    public void onHeaderClick(int position) {
                        mViewPager.setCurrentItem(position);
                    }
                });
                setViewPager();
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    private void setViewPager() {
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), fragments));
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mHeaderList.get(mCurrentPosition).isChecked = false;
                mHeaderAdapter.notifyItemChanged(mCurrentPosition);
                mCurrentPosition = position;
                mHeaderList.get(position).isChecked = true;
                mHeaderAdapter.notifyItemChanged(mCurrentPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setOffscreenPageLimit(fragments.size() - 1);
    }

    private void addFragment(String type, int position) {
        HomeBaseFragment fragment = new HomeBaseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(GlobalConstants.KEY_TYPE, type);
        fragment.setArguments(bundle);
        fragments.add(fragment);
    }

    private void updateUserList() {
        if (TextUtils.equals("附近", mCityName)) {
            mCityName = "";
        }else if(TextUtils.equals("常驻城市", mCityName)){
            mCityName = "0000";
        }
        EventBus.getDefault().postSticky(new UpdateCityOnlineEvent(mCityName, mOnlineFirst));
    }

    private void setFragmentList(boolean female) {
        if (female) { //女
            PreferenceUtils.saveSex(0);
            mHeaderList.add(new HomeHeaderBean("附近", true));
            addFragment("0", 0);
            mHeaderList.add(new HomeHeaderBean("会员", false));
            addFragment("4", 1);
        } else { //男
            PreferenceUtils.saveSex(1);
            mHeaderList.add(new HomeHeaderBean("附近", true));
            addFragment("0", 0);
            mHeaderList.add(new HomeHeaderBean("新注册", false));
            addFragment("1", 1);
            mHeaderList.add(new HomeHeaderBean("女神", false));
            addFragment("2", 2);
            mHeaderList.add(new HomeHeaderBean("名媛", false));
            addFragment("3", 3);
        }
    }

}
