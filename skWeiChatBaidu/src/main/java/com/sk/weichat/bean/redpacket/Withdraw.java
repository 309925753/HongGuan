package com.sk.weichat.bean.redpacket;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;

public class Withdraw {

    /**
     * actualMoney : 0.0
     * serviceCharge : 0.0
     * modifyTime : 1575515227
     * withdrawAccount : {"cardName":"小敏","bankCardNo":"112233445566","createTime":1575452374,"bankBranchName":"","bankName":"中国银行","id":{"date":1575452374000,"machineIdentifier":5758427,"counter":8768298,"processIdentifier":12116,"time":1575452374000,"timeSecond":1575452374,"timestamp":1575452374},"type":2,"userId":10000076,"desc":"快点审核"}
     * money : 100.0
     * createTime : 1575515227
     * id : {"date":1575515227000,"machineIdentifier":5758427,"counter":3993868,"processIdentifier":8556,"time":1575515227000,"timeSecond":1575515227,"timestamp":1575515227}
     * userId : 10000076
     * status : 2
     * withdrawAccountId : 5de77ed657dddb2f5485cb2a
     */

    private double actualMoney;
    private double serviceCharge;
    private long modifyTime;
    private WithdrawAccountBean withdrawAccount;
    private double money;
    private long createTime;
    private String userId;
    private int status;
    private String withdrawAccountId;

    public double getActualMoney() {
        return actualMoney;
    }

    public void setActualMoney(double actualMoney) {
        this.actualMoney = actualMoney;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public WithdrawAccountBean getWithdrawAccount() {
        return withdrawAccount;
    }

    public void setWithdrawAccount(WithdrawAccountBean withdrawAccount) {
        this.withdrawAccount = withdrawAccount;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        if (status == -1) {
            return MyApplication.getContext().getString(R.string.scan_withdraw_fail);
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

    public String getWithdrawAccountId() {
        return withdrawAccountId;
    }

    public void setWithdrawAccountId(String withdrawAccountId) {
        this.withdrawAccountId = withdrawAccountId;
    }

    public static class WithdrawAccountBean {
        /**
         * cardName : 小敏
         * bankCardNo : 112233445566
         * createTime : 1575452374
         * bankBranchName :
         * bankName : 中国银行
         * id : {"date":1575452374000,"machineIdentifier":5758427,"counter":8768298,"processIdentifier":12116,"time":1575452374000,"timeSecond":1575452374,"timestamp":1575452374}
         * type : 2
         * userId : 10000076
         * desc : 快点审核
         */

        private String cardName;
        private String bankCardNo;
        private int createTime;
        private String bankBranchName;
        private String bankName;
        private int type;
        private int userId;
        private String desc;

        public String getCardName() {
            return cardName;
        }

        public void setCardName(String cardName) {
            this.cardName = cardName;
        }

        public String getBankCardNo() {
            return bankCardNo;
        }

        public void setBankCardNo(String bankCardNo) {
            this.bankCardNo = bankCardNo;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public String getBankBranchName() {
            return bankBranchName;
        }

        public void setBankBranchName(String bankBranchName) {
            this.bankBranchName = bankBranchName;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getType() {
            if (type == 1) {
                return MyApplication.getContext().getString(R.string.alipay);
            } else {
                return MyApplication.getContext().getString(R.string.bank_card);
            }
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
