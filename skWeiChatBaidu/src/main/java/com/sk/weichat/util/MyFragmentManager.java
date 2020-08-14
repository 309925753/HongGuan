package com.sk.weichat.util;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sk.weichat.ui.base.EasyFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/14.
 *
 * @作者： 陈晓威
 * @时间： 2016/5/14
 * @说明：
 */
public class MyFragmentManager {
    private FragmentManager mFragmentManager;
    private int mRID;
    private List<EasyFragment> mFragmentList;
    private int mCurrentIndex = -1;

    public MyFragmentManager(FragmentActivity activity, int RID) {
        mFragmentManager = activity.getSupportFragmentManager();
        mRID = RID;
        mFragmentList = new ArrayList<>();
    }

    public void add(EasyFragment... baseFragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        // for (int i = baseFragment.length - 1; i >= 0 ; i --) {
        for (int i = 0; i < baseFragment.length; i++) {
            fragmentTransaction.add(mRID, baseFragment[i]);
            mFragmentList.add(baseFragment[i]);
        }
        fragmentTransaction.commit();
    }

    public void remove(int position) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(mFragmentList.get(position));
        fragmentTransaction.commit();
    }

    public void removeAll() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        int size = mFragmentList.size();
        for (int i = 0; i < size; i++) {
            fragmentTransaction.remove(mFragmentList.get(i));
        }
        fragmentTransaction.commit();
    }

    public void show(int position) {
        if (position == mCurrentIndex)
            return;
        mCurrentIndex = position;
        showFragment(mFragmentList.get(position));
    }

    private void showFragment(EasyFragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
        TanX.Log("显示fragment:" + fragment.getClass().getSimpleName());
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        int size = mFragmentList.size();
        for (int i = 0; i < size; i++) {
            fragmentTransaction.hide(mFragmentList.get(i));
        }
    }

    public EasyFragment getShowFragment() {
        return mFragmentList.get(mCurrentIndex);
    }
}
