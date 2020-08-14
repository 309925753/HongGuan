package com.redchamber.bean;

import java.util.List;

public class CertificateBean {


    /**
     * currentTime : 1589783620591
     * data : [{"addTime":1589783344863,"autoAuditTime":1589785144863,"certificateInfo":"http://file57.quyangapp.com/u/41/10000041/202005/o/a05245889c6c46e993412fdab819e416.png","flag":0,"id":"5ec22b3093778409603ac389","type":1,"userId":10000041}]
     * resultCode : 1
     */

    private long currentTime;
    private int resultCode;
    private List<DataBean> data;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * addTime : 1589783344863
         * autoAuditTime : 1589785144863
         * certificateInfo : http://file57.quyangapp.com/u/41/10000041/202005/o/a05245889c6c46e993412fdab819e416.png
         * flag : 0
         * id : 5ec22b3093778409603ac389
         * type : 1
         * userId : 10000041
         */

        private long addTime;
        private long autoAuditTime;
        private String certificateInfo;
        private int flag;
        private String id;
        private int type;
        private int userId;

        public long getAddTime() {
            return addTime;
        }

        public void setAddTime(long addTime) {
            this.addTime = addTime;
        }

        public long getAutoAuditTime() {
            return autoAuditTime;
        }

        public void setAutoAuditTime(long autoAuditTime) {
            this.autoAuditTime = autoAuditTime;
        }

        public String getCertificateInfo() {
            return certificateInfo;
        }

        public void setCertificateInfo(String certificateInfo) {
            this.certificateInfo = certificateInfo;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
