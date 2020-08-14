package com.sk.weichat.bean.redpacket;

import java.util.List;

/**
 * Created by Administrator on 2016/9/26.
 */
public class ConsumeRecordItemNew {
    /**
     * expenses : 4.43
     * income : 102.13
     * pageData : [{"id":"57b6d2085ce5b123bdfd6aa4","time":1471599112,"desc":"红包发送","tradeNo":"351471599112981","status":1,"userId":10006521,"money":50,"payType":3,"type":2},{"id":"57b6d1cf5ce5b123bdfd6aa2","time":1471599055,"desc":"红包发送","tradeNo":"991471599055344","status":1,"userId":10006521,"money":10,"payType":3,"type":2},{"id":"57b6d1aa5ce5b123bdfd6a9e","time":1471599018,"desc":"余额充值","tradeNo":"191471599018126","status":1,"userId":10006521,"money":100,"payType":2,"type":1}]
     */

    private String expenses;
    private String income;
    private List<RecordDataEntity> recordList;

    public String getExpenses() {
        return expenses;
    }

    public void setExpenses(String expenses) {
        this.expenses = expenses;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public List<RecordDataEntity> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<RecordDataEntity> recordList) {
        this.recordList = recordList;
    }

    public static class RecordDataEntity {
        /**
         * id : 57b6d2085ce5b123bdfd6aa4
         * time : 1471599112
         * desc : 红包发送
         * tradeNo : 351471599112981
         * status : 1
         * manualPay_status : 1
         * userId : 10006521
         * money : 50
         * payType : 3
         * type : 2
         */
        private String id;
        private long time;
        private String desc;
        private String tradeNo;
        private int status;
        private int manualPay_status;
        private String userId;
        private double money;
        private int payType;
        private int type;
        private int changeType;// 服务端新增，代表+-金额
        private int itemType;// 0 账单信息类型 1 统计信息类型
        private String expenses;// 某年某月支出，仅itemType==1才有
        private String income;  // 某年某月收入，仅itemType==1才有
        private int year;
        private int month;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getTradeNo() {
            return tradeNo;
        }

        public void setTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getManualPay_status() {
            return manualPay_status;
        }

        public void setManualPay_status(int manualPay_status) {
            this.manualPay_status = manualPay_status;
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

        public int getPayType() {
            return payType;
        }

        public void setPayType(int payType) {
            this.payType = payType;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getChangeType() {
            return changeType;
        }

        public void setChangeType(int changeType) {
            this.changeType = changeType;
        }

        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        public String getExpenses() {
            return expenses;
        }

        public void setExpenses(String expenses) {
            this.expenses = expenses;
        }

        public String getIncome() {
            return income;
        }

        public void setIncome(String income) {
            this.income = income;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }
    }
}
