package com.redchamber.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class BarRadioBean implements MultiItemEntity {

    public static final int TYPE_A = 1;
    public static final int TYPE_B = 2;

    private int itemType;

    public BarRadioBean(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

}
