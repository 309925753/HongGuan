package com.sk.weichat.bean.redpacket;

import java.util.List;

/**
 * Created by Administrator on 2016/9/26.
 */
public class ConsumeRecordItem {
    /**
     * total : 7
     * pageCount : 1
     * start : 0
     * pageData : [{"id":"57b6d2085ce5b123bdfd6aa4","time":1471599112,"desc":"红包发送","tradeNo":"351471599112981","status":1,"userId":10006521,"money":50,"payType":3,"type":2},{"id":"57b6d1cf5ce5b123bdfd6aa2","time":1471599055,"desc":"红包发送","tradeNo":"991471599055344","status":1,"userId":10006521,"money":10,"payType":3,"type":2},{"id":"57b6d1aa5ce5b123bdfd6a9e","time":1471599018,"desc":"余额充值","tradeNo":"191471599018126","status":1,"userId":10006521,"money":100,"payType":2,"type":1}]
     * pageSize : 10
     * pageIndex : 0
     */
    private int total;
    private int pageCount;
    private int start;
    private List<PageDataEntity> pageData;
    private int pageSize;
    private int pageIndex;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<PageDataEntity> getPageData() {
        return pageData;
    }

    public void setPageData(List<PageDataEntity> pageData) {
        this.pageData = pageData;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public class PageDataEntity {
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
    }
}
