package com.sk.weichat.bean;

import java.io.Serializable;
import java.util.List;

public class TransferRecord implements Serializable {

    /**
     * currentTime : 1555128563810
     * data : {"pageCount":1,"pageData":[{"desc":"红包发送","id":"5cb14d565266c2106cf1da03","money":0.1,"payType":3,"status":1,"time":1555123542,"toUserId":10008401,"tradeNo":"991555123542826","type":4,"userId":10009550},{"desc":"转账","id":"5cb14c8c5266c2106cf1d9f5","money":0.1,"payType":3,"status":1,"time":1555123340,"toUserId":10008401,"tradeNo":"861555123340830","type":7,"userId":10009550}],"pageIndex":0,"pageSize":20,"start":0,"total":2}
     * resultCode : 1
     */

    private long currentTime;
    private DataBean data;
    private int resultCode;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public static class DataBean {
        /**
         * pageCount : 1
         * pageData : [{"desc":"红包发送","id":"5cb14d565266c2106cf1da03","money":0.1,"payType":3,"status":1,"time":1555123542,"toUserId":10008401,"tradeNo":"991555123542826","type":4,"userId":10009550},{"desc":"转账","id":"5cb14c8c5266c2106cf1d9f5","money":0.1,"payType":3,"status":1,"time":1555123340,"toUserId":10008401,"tradeNo":"861555123340830","type":7,"userId":10009550}]
         * pageIndex : 0
         * pageSize : 20
         * start : 0
         * total : 2
         */

        private int pageCount;
        private int pageIndex;
        private int pageSize;
        private int start;
        private int total;
        private List<PageDataBean> pageData;

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<PageDataBean> getPageData() {
            return pageData;
        }

        public void setPageData(List<PageDataBean> pageData) {
            this.pageData = pageData;
        }

        public static class PageDataBean {
            /**
             * desc : 红包发送
             * id : 5cb14d565266c2106cf1da03
             * money : 0.1
             * payType : 3
             * status : 1
             * time : 1555123542
             * toUserId : 10008401
             * tradeNo : 991555123542826
             * type : 4
             * userId : 10009550
             */

            private String desc;
            private String id;
            private double money;
            private int payType;
            private int status;
            private int time;
            private int toUserId;
            private String tradeNo;
            private int type;
            private int userId;

            private boolean isTitle;
            private double totalOutMoney;
            private double totalInMoney;
            private int month;

            public boolean isTitle() {
                return isTitle;
            }

            public void setIsTitle(boolean title) {
                isTitle = title;
            }

            public double getTotalOutMoney() {
                return totalOutMoney;
            }

            public void setTotalOutMoney(double totalOutMoney) {
                this.totalOutMoney = totalOutMoney;
            }

            public double getTotalInMoney() {
                return totalInMoney;
            }

            public void setTotalInMoney(double totalInMoney) {
                this.totalInMoney = totalInMoney;
            }

            public int getMonth() {
                return month;
            }

            public void setMonth(int month) {
                this.month = month;
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
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

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public int getTime() {
                return time;
            }

            public void setTime(int time) {
                this.time = time;
            }

            public int getToUserId() {
                return toUserId;
            }

            public void setToUserId(int toUserId) {
                this.toUserId = toUserId;
            }

            public String getTradeNo() {
                return tradeNo;
            }

            public void setTradeNo(String tradeNo) {
                this.tradeNo = tradeNo;
            }

            public int getType() {
                return type;
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
        }
    }
}
