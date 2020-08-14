package com.redchamber.home;

import android.content.Context;
import android.content.Intent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.FragmentTransaction;

import com.redchamber.bar.BarFragment;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.message.MessageFragment;
import com.redchamber.mine.MineFemaleFragment;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 首页
 */
public class HomeActivity extends BaseActivity {

    @BindView(R.id.rg_main)
    RadioGroup mRgMain;
    @BindView(R.id.rb_home)
    RadioButton mRbHome;

    private List<BaseFragment> mBaseFragment;
    private int position;
    private BaseFragment mContent;

    @Override
    protected int setLayout() {
        return R.layout.activity_home;
    }

    @Override
    protected void initView() {
        initFragment();
        setListener();
    }

    private void setListener() {
        mRgMain.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        mRbHome.performClick();
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_home://红馆
                    position = 0;
                    break;
                case R.id.rb_bar://约吧类
                    position = 1;
                    break;
                case R.id.rb_message://消息
                    position = 2;
                    break;
                case R.id.rb_mine://个人
                    position = 3;
                    break;
                default:
            }
            BaseFragment to = getFragment();
            switchFragment(mContent, to);
        }
    }

    private void switchFragment(BaseFragment from, BaseFragment to) {
        if (from != to) {
            mContent = to;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (from != null) {
                ft.hide(from);
            }
            if (!to.isAdded()) {
                ft.add(R.id.fl_content, to).commit();
            } else {
                ft.show(to).commit();
            }
        }
    }

    private BaseFragment getFragment() {
        return mBaseFragment.get(position);
    }

    private void initFragment() {
        mBaseFragment = new ArrayList<>();
//        mBaseFragment.add(new HomeFemaleFragment());
        mBaseFragment.add(new HomeFragment());
        mBaseFragment.add(new BarFragment());
        mBaseFragment.add(new MessageFragment());
        mBaseFragment.add(new MineFemaleFragment());
    }

    public static void startHomeActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, HomeActivity.class));
    }

}
