package com.sk.weichat.bean;

import java.util.List;

public class RoomMessage {

    /**
     * currentTime : 1559706082156
     * data : {"allowConference":1,"allowHostUpdate":1,"allowInviteFriend":1,"allowSendCard":1,"allowSpeakCourse":0,"allowUploadFile":1,"areaId":440307,"call":"300053","category":0,"chatRecordTimeOut":-1,"cityId":440300,"countryId":1,"createTime":1559706081,"desc":"发个","id":"5cf739e0d85c8f05d8a6ce35","isAttritionNotice":1,"isLook":1,"isNeedVerify":0,"jid":"94582751f4cc4c049e5487d64cf1af8b","latitude":22.609037,"longitude":114.066137,"maxUserSize":1000,"members":[],"modifyTime":1559706081,"name":"国产车","nickname":"比比划划","notice":{"modifyTime":0,"time":0},"notices":[],"provinceId":440000,"s":1,"showMember":1,"showRead":0,"subject":"","tags":[],"talkTime":0,"userId":10000018,"userSize":0,"videoMeetingNo":"350053"}
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
         * allowConference : 1
         * allowHostUpdate : 1
         * allowInviteFriend : 1
         * allowSendCard : 1
         * allowSpeakCourse : 0
         * allowUploadFile : 1
         * areaId : 440307
         * call : 300053
         * category : 0
         * chatRecordTimeOut : -1.0
         * cityId : 440300
         * countryId : 1
         * createTime : 1559706081
         * desc : 发个
         * id : 5cf739e0d85c8f05d8a6ce35
         * isAttritionNotice : 1
         * isLook : 1
         * isNeedVerify : 0
         * jid : 94582751f4cc4c049e5487d64cf1af8b
         * latitude : 22.609037
         * longitude : 114.066137
         * maxUserSize : 1000
         * members : []
         * modifyTime : 1559706081
         * name : 国产车
         * nickname : 比比划划
         * notice : {"modifyTime":0,"time":0}
         * notices : []
         * provinceId : 440000
         * s : 1
         * showMember : 1
         * showRead : 0
         * subject :
         * tags : []
         * talkTime : 0
         * userId : 10000018
         * userSize : 0
         * videoMeetingNo : 350053
         */

        private int allowConference;
        private int allowHostUpdate;
        private int allowInviteFriend;
        private int allowSendCard;
        private int allowSpeakCourse;
        private int allowUploadFile;
        private int areaId;
        private String call;
        private int category;
        private double chatRecordTimeOut;
        private int cityId;
        private int countryId;
        private int createTime;
        private String desc;
        private String id;
        private int isAttritionNotice;
        private int isLook;
        private int isNeedVerify;
        private String jid;
        private double latitude;
        private double longitude;
        private int maxUserSize;
        private int modifyTime;
        private String name;
        private String nickname;
        private NoticeBean notice;
        private int provinceId;
        private int s;
        private int showMember;
        private int showRead;
        private String subject;
        private int talkTime;
        private int userId;
        private int userSize;
        private String videoMeetingNo;
        private List<?> members;
        private List<?> notices;
        private List<?> tags;

        public int getAllowConference() {
            return allowConference;
        }

        public void setAllowConference(int allowConference) {
            this.allowConference = allowConference;
        }

        public int getAllowHostUpdate() {
            return allowHostUpdate;
        }

        public void setAllowHostUpdate(int allowHostUpdate) {
            this.allowHostUpdate = allowHostUpdate;
        }

        public int getAllowInviteFriend() {
            return allowInviteFriend;
        }

        public void setAllowInviteFriend(int allowInviteFriend) {
            this.allowInviteFriend = allowInviteFriend;
        }

        public int getAllowSendCard() {
            return allowSendCard;
        }

        public void setAllowSendCard(int allowSendCard) {
            this.allowSendCard = allowSendCard;
        }

        public int getAllowSpeakCourse() {
            return allowSpeakCourse;
        }

        public void setAllowSpeakCourse(int allowSpeakCourse) {
            this.allowSpeakCourse = allowSpeakCourse;
        }

        public int getAllowUploadFile() {
            return allowUploadFile;
        }

        public void setAllowUploadFile(int allowUploadFile) {
            this.allowUploadFile = allowUploadFile;
        }

        public int getAreaId() {
            return areaId;
        }

        public void setAreaId(int areaId) {
            this.areaId = areaId;
        }

        public String getCall() {
            return call;
        }

        public void setCall(String call) {
            this.call = call;
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public double getChatRecordTimeOut() {
            return chatRecordTimeOut;
        }

        public void setChatRecordTimeOut(double chatRecordTimeOut) {
            this.chatRecordTimeOut = chatRecordTimeOut;
        }

        public int getCityId() {
            return cityId;
        }

        public void setCityId(int cityId) {
            this.cityId = cityId;
        }

        public int getCountryId() {
            return countryId;
        }

        public void setCountryId(int countryId) {
            this.countryId = countryId;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
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

        public int getIsAttritionNotice() {
            return isAttritionNotice;
        }

        public void setIsAttritionNotice(int isAttritionNotice) {
            this.isAttritionNotice = isAttritionNotice;
        }

        public int getIsLook() {
            return isLook;
        }

        public void setIsLook(int isLook) {
            this.isLook = isLook;
        }

        public int getIsNeedVerify() {
            return isNeedVerify;
        }

        public void setIsNeedVerify(int isNeedVerify) {
            this.isNeedVerify = isNeedVerify;
        }

        public String getJid() {
            return jid;
        }

        public void setJid(String jid) {
            this.jid = jid;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public int getMaxUserSize() {
            return maxUserSize;
        }

        public void setMaxUserSize(int maxUserSize) {
            this.maxUserSize = maxUserSize;
        }

        public int getModifyTime() {
            return modifyTime;
        }

        public void setModifyTime(int modifyTime) {
            this.modifyTime = modifyTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public NoticeBean getNotice() {
            return notice;
        }

        public void setNotice(NoticeBean notice) {
            this.notice = notice;
        }

        public int getProvinceId() {
            return provinceId;
        }

        public void setProvinceId(int provinceId) {
            this.provinceId = provinceId;
        }

        public int getS() {
            return s;
        }

        public void setS(int s) {
            this.s = s;
        }

        public int getShowMember() {
            return showMember;
        }

        public void setShowMember(int showMember) {
            this.showMember = showMember;
        }

        public int getShowRead() {
            return showRead;
        }

        public void setShowRead(int showRead) {
            this.showRead = showRead;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public int getTalkTime() {
            return talkTime;
        }

        public void setTalkTime(int talkTime) {
            this.talkTime = talkTime;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getUserSize() {
            return userSize;
        }

        public void setUserSize(int userSize) {
            this.userSize = userSize;
        }

        public String getVideoMeetingNo() {
            return videoMeetingNo;
        }

        public void setVideoMeetingNo(String videoMeetingNo) {
            this.videoMeetingNo = videoMeetingNo;
        }

        public List<?> getMembers() {
            return members;
        }

        public void setMembers(List<?> members) {
            this.members = members;
        }

        public List<?> getNotices() {
            return notices;
        }

        public void setNotices(List<?> notices) {
            this.notices = notices;
        }

        public List<?> getTags() {
            return tags;
        }

        public void setTags(List<?> tags) {
            this.tags = tags;
        }

        public static class NoticeBean {
            /**
             * modifyTime : 0
             * time : 0
             */

            private int modifyTime;
            private int time;

            public int getModifyTime() {
                return modifyTime;
            }

            public void setModifyTime(int modifyTime) {
                this.modifyTime = modifyTime;
            }

            public int getTime() {
                return time;
            }

            public void setTime(int time) {
                this.time = time;
            }
        }
    }
}
