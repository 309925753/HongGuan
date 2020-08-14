package com.redchamber.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

public class PageDataBean implements MultiItemEntity,Serializable {

        public static final int TYPE_A = 0;
        public static final int TYPE_B = 1;


    public int programType;
    public PageDataBean() {

    }
      public PageDataBean(int itemType) {
        this.programType = itemType;
    }

        @Override
        public int getItemType() {
        return programType;
    }

        /**
         * cityId : 10020
         * content : 爱你一万年
         * delFlag : 0
         * discussFlag : 1
         * discussNum : 1
         * discusses : [{"content":"我是一只小小小小鸟","joinTime":1588727453660,"nickName":"测试用户","userId":10000007}]
         * expectFriend : 有趣;关爱我
         * images : https://daydayloan.oss-cn-shenzhen.aliyuncs.com/image/1.png
         * likeNum : 1
         * likes : [{"joinTime":1588727469928,"nickName":"测试用户","userId":10000007}]
         * loc : {"lat":31.2351,"lng":121.5276}
         * nickName : 测试用户
         * placeName : 黄焖鸡米饭
         * programDate : 1598176000000
         * programFlag : 1
         * programId : 5eaaa335fe259804603350f7
         * programTime : 下午
         * programType : 0
         * pubTime : 1588241205899
         * registerCityId : 10020
         * sex : 0
         * signUpNums : 1
         * signUps : [{"joinImage":"http://111.jpg","joinTime":1588241264833,"nickName":"测试用户","userId":10000007}]
         * title : 约旅游
         * userId : 10000007
         * userLevel : 10010
         */

        private int cityId;
        private String content;
        private int delFlag;
        private int discussFlag;//是否可以评论 0禁止评论 1开启评论"
        private int discussNum;
        private String expectFriend;
        private String images;
        private int likeNum;
        private LocBean loc;
        private String nickName;
        private String placeName;
        private long programDate;
        private int programFlag;
        private String programId;
        private String programTime;
        private long pubTime;
        private int registerCityId;
        private int sex;
        private int signUpNums;
        private String title;
        private int userId;
        private String userLevel;//用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章
        private List<DiscussesBean> discusses;
        private List<LikesBean> likes;
        private List<SignUpsBean> signUps;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    private String  cityName;
        public int getCityId() {
        return cityId;
    }

        public void setCityId(int cityId) {
        this.cityId = cityId;
    }

        public String getContent() {
        return content;
    }

        public void setContent(String content) {
        this.content = content;
    }

        public int getDelFlag() {
        return delFlag;
    }

        public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

        public int getDiscussFlag() {
        return discussFlag;
    }

        public void setDiscussFlag(int discussFlag) {
        this.discussFlag = discussFlag;
    }

        public int getDiscussNum() {
        return discussNum;
    }

        public void setDiscussNum(int discussNum) {
        this.discussNum = discussNum;
    }

        public String getExpectFriend() {
        return expectFriend;
    }

        public void setExpectFriend(String expectFriend) {
        this.expectFriend = expectFriend;
    }

        public String getImages() {
        return images;
    }

        public void setImages(String images) {
        this.images = images;
    }

        public int getLikeNum() {
        return likeNum;
    }

        public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

        public LocBean getLoc() {
        return loc;
    }

        public void setLoc(LocBean loc) {
        this.loc = loc;
    }

        public String getNickName() {
        return nickName;
    }

        public void setNickName(String nickName) {
        this.nickName = nickName;
    }

        public String getPlaceName() {
        return placeName;
    }

        public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

        public long getProgramDate() {
        return programDate;
    }

        public void setProgramDate(long programDate) {
        this.programDate = programDate;
    }

        public int getProgramFlag() {
        return programFlag;
    }

        public void setProgramFlag(int programFlag) {
        this.programFlag = programFlag;
    }

        public String getProgramId() {
        return programId;
    }

        public void setProgramId(String programId) {
        this.programId = programId;
    }

        public String getProgramTime() {
        return programTime;
    }

        public void setProgramTime(String programTime) {
        this.programTime = programTime;
    }

        public int getProgramType() {
        return programType;
    }



        public long getPubTime() {
        return pubTime;
    }

        public void setPubTime(long pubTime) {
        this.pubTime = pubTime;
    }

        public int getRegisterCityId() {
        return registerCityId;
    }

        public void setRegisterCityId(int registerCityId) {
        this.registerCityId = registerCityId;
    }

        public int getSex() {
        return sex;
    }

        public void setSex(int sex) {
        this.sex = sex;
    }

        public int getSignUpNums() {
        return signUpNums;
    }

        public void setSignUpNums(int signUpNums) {
        this.signUpNums = signUpNums;
    }

        public String getTitle() {
        return title;
    }

        public void setTitle(String title) {
        this.title = title;
    }

        public int getUserId() {
        return userId;
    }

        public void setUserId(int userId) {
        this.userId = userId;
    }

        public String getUserLevel() {
        return userLevel;
    }

        public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

        public List<DiscussesBean> getDiscusses() {
        return discusses;
    }

        public void setDiscusses(List<DiscussesBean> discusses) {
        this.discusses = discusses;
    }

        public List<LikesBean> getLikes() {
        return likes;
    }

        public void setLikes(List<LikesBean> likes) {
        this.likes = likes;
    }

        public List<SignUpsBean> getSignUps() {
        return signUps;
    }

        public void setSignUps(List<SignUpsBean> signUps) {
        this.signUps = signUps;
    }

        public static class LocBean implements  Serializable{
            /**
             * lat : 31.2351
             * lng : 121.5276
             */

            private double lat;
            private double lng;

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }
        }

        public static class DiscussesBean implements  Serializable{
            /**
             * content : 我是一只小小小小鸟
             * joinTime : 1588727453660
             * nickName : 测试用户
             * userId : 10000007
             */

            private String content;
            private long joinTime;
            private String nickName;
            private int userId;

            public String getUserLevel() {
                return userLevel;
            }

            public void setUserLevel(String userLevel) {
                this.userLevel = userLevel;
            }

            private String userLevel;//用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public long getJoinTime() {
                return joinTime;
            }

            public void setJoinTime(long joinTime) {
                this.joinTime = joinTime;
            }

            public String getNickName() {
                return nickName;
            }

            public void setNickName(String nickName) {
                this.nickName = nickName;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }
        }

        public static class LikesBean implements  Serializable {
            /**
             * joinTime : 1588727469928
             * nickName : 测试用户
             * userId : 10000007
             */

            private long joinTime;
            private String nickName;
            private int userId;

            public String getUserLevel() {
                return userLevel;
            }

            public void setUserLevel(String userLevel) {
                this.userLevel = userLevel;
            }

            private String userLevel;//用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章

            public long getJoinTime() {
                return joinTime;
            }

            public void setJoinTime(long joinTime) {
                this.joinTime = joinTime;
            }

            public String getNickName() {
                return nickName;
            }

            public void setNickName(String nickName) {
                this.nickName = nickName;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }
        }

        public static class SignUpsBean implements  Serializable {
            /**
             * joinImage : http://111.jpg
             * joinTime : 1588241264833
             * nickName : 测试用户
             * userId : 10000007
             */

            private String joinImage;
            private long joinTime;
            private String nickName;
            private int userId;

            public String getUserLevel() {
                return userLevel;
            }

            public void setUserLevel(String userLevel) {
                this.userLevel = userLevel;
            }

            private String userLevel;//用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章
            public String getJoinImage() {
                return joinImage;
            }

            public void setJoinImage(String joinImage) {
                this.joinImage = joinImage;
            }

            public long getJoinTime() {
                return joinTime;
            }

            public void setJoinTime(long joinTime) {
                this.joinTime = joinTime;
            }

            public String getNickName() {
                return nickName;
            }

            public void setNickName(String nickName) {
                this.nickName = nickName;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }
        }
    }
