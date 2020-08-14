package com.redchamber.nocsroll;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

/**
 * Created by Administrator on 2018/9/20.
 */

public class MessagePagerAdapter extends MyFragmentPagerAdapter {

    private List<Fragment> list;
    List<Integer> listIds;

    public MessagePagerAdapter(FragmentManager fm, List<Fragment> list, List<Integer> listIds) {
        super(fm);
        this.list = list;
        this.listIds = listIds;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (listIds == null || listIds.size() == 0) {
            return position;
        }
        return listIds.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
