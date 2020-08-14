package com.sk.weichat.bean.redpacket;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;

public class Recharge {
    private String id;
    private String userId; // 用户Id
    private double money;
    private int type; // 类型 1.微信支付 2.支付宝 3.银行卡
    private String orderNo; // 订单号
    private double serviceCharge; // 手续费
    private double actualMoney; // 实际金额
    private long createTime;
    private int status; // 状态 -1.忽略 1.申请中 2.已完成
    private long modifyTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getType() {
        if (type == 1) {
            return MyApplication.getContext().getString(R.string.wechat);
        } else if (type == 2) {
            return MyApplication.getContext().getString(R.string.alipay);
        } else {
            return MyApplication.getContext().getString(R.string.bank_card);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public double getActualMoney() {
        return actualMoney;
    }

    public void setActualMoney(double actualMoney) {
        this.actualMoney = actualMoney;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        if (status == -1) {
            return MyApplication.getContext().getString(R.string.recharge_failed);
        } else if (status == 1) {
            // 审核中...
            return "";
        } else {
            return MyApplication.getContext().getString(R.string.success);
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
}
