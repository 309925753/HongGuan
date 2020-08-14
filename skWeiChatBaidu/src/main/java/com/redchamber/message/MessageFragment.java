package com.redchamber.message;


import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.redchamber.lib.base.BaseFragment;
import com.redchamber.nocsroll.MessagePagerAdapter;
import com.redchamber.nocsroll.MyNavigationLayoutContainer;
import com.redchamber.nocsroll.NoScrollViewPager;
import com.sk.weichat.R;
import com.sk.weichat.view.SkinImageView;
import com.sk.weichat.view.SkinTextView;
import com.sk.weichat.view.cjt2325.cameralibrary.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 消息
 */
public class MessageFragment extends BaseFragment {
    List<Fragment> listFragments = new ArrayList<>();
    List<Integer> listIds = new ArrayList<>();

    int radioButtonWith;
    int indicatorHeight;
    @BindView(R.id.rb_unread)
    RadioButton rbUnread;
    @BindView(R.id.rb_readmessage)
    RadioButton rbReadmessage;
    @BindView(R.id.rg_choice)
    RadioGroup rgChoice;
    @BindView(R.id.myNavigationView)
    View myNavigationView;
    @BindView(R.id.myNavigationLayoutContainer)
    MyNavigationLayoutContainer myNavigationLayoutContainer;
    @BindView(R.id.vp_message_tab)
    NoScrollViewPager vpMessageTab;
    @BindView(R.id.message_fragment)
    RelativeLayout messageFragment;

    ChatNewFragment chatNewFragment;
    SystemNewFragment systemNewFragment;



    @Override
    protected int setLayout() {
        return R.layout.fragment_message;
    }

    @Override
    protected void initView() {
        setIndicatorWidth();
        setOnListener();
        addFragment();
    }

    private void addFragment() {
        listFragments.add(chatNewFragment = new ChatNewFragment());
        listFragments.add(systemNewFragment = new SystemNewFragment());

        FragmentManager fm = getActivity().getSupportFragmentManager();
        vpMessageTab.setAdapter(new MessagePagerAdapter(fm, listFragments, listIds));
        vpMessageTab.setOffscreenPageLimit(listFragments.size() - 1);
    }

    public void setOnListener() {

        rgChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                vpMessageTab.setAnimation(false);
                switch (checkedId) {
                    case R.id.rb_unread:
                        rbUnread.setTextSize(16);
                        rbReadmessage.setTextSize(14);
                        vpMessageTab.setCurrentItem(0);
                        break;

                    case R.id.rb_readmessage:
                        rbUnread.setTextSize(14);
                        rbReadmessage.setTextSize(16);
                        vpMessageTab.setCurrentItem(1);
                        break;
                }
            }
        });


        vpMessageTab.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float fraction, int positionOffsetPixels) {
                myNavigationLayoutContainer.scrollTo(-(Math.round(ScreenUtils.getScreenWidth(getContext()) / 2 - radioButtonWith - radioButtonWith / 2)), 0);
                if (position == 0) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myNavigationView.getLayoutParams();
                    params.width = radioButtonWith / 2;
                    params.height = indicatorHeight;
                    params.setMargins(ScreenUtils.getScreenWidth(getActivity()) / 2 - radioButtonWith, 0, 0, 0);
                    myNavigationView.setLayoutParams(params);
                } else {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myNavigationView.getLayoutParams();
                    params.width = radioButtonWith / 2;
                    params.height = indicatorHeight;
                    params.setMargins(ScreenUtils.getScreenWidth(getActivity()) / 3, 0, 0, 0);

                    myNavigationView.setLayoutParams(params);

                    myNavigationLayoutContainer.scrollTo(-(Math.round(ScreenUtils.getScreenWidth(getActivity()) / 3)), 0);
                }
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        rbUnread.setChecked(true);
                        break;

                    case 1:
                        rbReadmessage.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private void setIndicatorWidth() {
        radioButtonWith = getResources().getDimensionPixelSize(R.dimen.fragment_message_radio_button_width);
        indicatorHeight = getResources().getDimensionPixelSize(R.dimen.fragment_message_radio_button_height);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myNavigationView.getLayoutParams();
        params.width = radioButtonWith / 2;
        params.height = indicatorHeight;
        params.setMargins(ScreenUtils.getScreenWidth(getActivity()) / 2 - radioButtonWith, 0, 0, 0);
        params.addRule(Gravity.CENTER_VERTICAL);
        myNavigationView.setLayoutParams(params);
    }

}
