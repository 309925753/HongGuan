package com.redchamber.bean;

import java.util.List;

public class Yuba {

    /**
     * pageCount : 1
     * total : 9
     * pageIndex : 1
     * start : 10
     * pageSize : 10
     * pageData : [{"loc":{"lng":121.5207250807694,"lat":31.09107920514784},"signUps":[],"registerCityId":310100,"pubTime":1589364882397,"cityId":0,"delFlag":0,"title":"约电影","programTime":"下午","likeNum":0,"userLevel":"11010","discusses":[],"discussFlag":1,"likes":[],"images":"http://file57.quyangapp.com/u/34/10000034/202005/o/f29bb4e3c8974f3aa5287c96832e57bb.jpg;http://file57.quyangapp.com/u/34/10000034/202005/o/bc411bf7b295439eb8eecd15bd591476.jpg","programType":0,"discussNum":0,"programFlag":0,"nickName":"路上","programDate":1589969657,"sex":1,"expectFriend":"看脸;有趣;大方","userId":10000034,"signUpNums":0,"placeName":"中国工商银行(上海市浦江高科技园支行)","programId":"5ebbc892f2091378e94e89ed"},{"loc":{"lng":121.5192444616039,"lat":31.09249477945173},"programType":0,"signUps":[],"discussNum":0,"programFlag":0,"registerCityId":310100,"nickName":"路上","programDate":1589709587,"pubTime":1589363993369,"sex":1,"expectFriend":"大方;关爱我;有趣","cityId":0,"delFlag":0,"title":"约旅游","programTime":"下午","userId":10000034,"likeNum":0,"userLevel":"11010","signUpNums":0,"discusses":[],"placeName":"上海虎生电子电器有限公司","programId":"5ebbc519abe0a60584f94182","discussFlag":1,"likes":[]},{"loc":{"lng":121.5099777576675,"lat":31.0901122879155},"images":"http://file57.quyangapp.com/u/34/10000034/202005/o/a0a5b6b6bdc540fe86283f86ebc18f7e.jpg;http://file57.quyangapp.com/u/34/10000034/202005/o/f958b2e9924b4d858aa28ef9f95f199b.jpg","programType":1,"signUps":[],"discussNum":0,"programFlag":1,"registerCityId":310100,"nickName":"路上","pubTime":1589363875803,"sex":1,"cityId":0,"delFlag":0,"userId":10000034,"content":"啦啦啦","likeNum":0,"userLevel":"11010","signUpNums":0,"discusses":[],"programId":"5ebbc4a3c6427b0b1c803d9c","discussFlag":1,"likes":[]},{"loc":{"lng":121.522,"lat":31.095},"images":"","programType":1,"signUps":[],"discussNum":0,"programFlag":1,"registerCityId":310100,"nickName":"路上","pubTime":1589363646856,"sex":1,"cityId":0,"delFlag":0,"programTime":"","userId":10000034,"content":"哈哈哈","likeNum":0,"userLevel":"11010","signUpNums":0,"discusses":[],"programId":"5ebbc3bec6427b0b1c803d8c","discussFlag":1,"likes":[]},{"loc":{"lng":121.522002634996,"lat":31.09574096233177},"images":"","programType":1,"signUps":[],"discussNum":0,"programFlag":1,"registerCityId":310100,"nickName":"路上","pubTime":1589277554719,"sex":1,"cityId":0,"delFlag":0,"programTime":"中午","userId":10000034,"content":"123","likeNum":0,"userLevel":"11010","signUpNums":0,"discusses":[],"programId":"5eba7372d91b36034947a4a1","discussFlag":1,"likes":[]},{"loc":{"lng":121.522002634996,"lat":31.09574096233177},"signUps":[],"registerCityId":310100,"pubTime":1589277466425,"cityId":0,"delFlag":0,"title":"约旅游","programTime":"中午","content":"","likeNum":0,"userLevel":"11010","discusses":[],"discussFlag":1,"likes":[],"images":"","programType":0,"discussNum":0,"programFlag":0,"nickName":"路上","programDate":1589618079,"sex":1,"expectFriend":"大方;关爱我","userId":10000034,"signUpNums":0,"placeName":"吴家墙","programId":"5eba731ad91b36034947a493"},{"loc":{"lng":121.522,"lat":31.095},"signUps":[],"registerCityId":310100,"pubTime":1589276532138,"cityId":0,"delFlag":0,"title":"约旅游","programTime":"中午","content":"123456789","likeNum":0,"userLevel":"11010","discusses":[],"discussFlag":1,"likes":[],"images":"","programType":0,"discussNum":0,"programFlag":0,"nickName":"路上","programDate":1589618079,"sex":1,"expectFriend":"大方;关爱我","userId":10000034,"signUpNums":0,"placeName":"吴家墙","programId":"5eba6f74500c34036e52e90c"},{"loc":{"lng":121.522,"lat":31.095},"signUps":[],"registerCityId":310100,"pubTime":1589276133580,"cityId":0,"delFlag":0,"title":"约旅游","programTime":"中午","content":"123456789","likeNum":0,"userLevel":"11010","discusses":[],"discussFlag":1,"likes":[],"images":"http://file57.quyangapp.com/avatar/t/34/10000034.jpg","programType":0,"discussNum":0,"programFlag":0,"nickName":"路上","programDate":1589618079,"sex":1,"expectFriend":"大方;关爱我","userId":10000034,"signUpNums":0,"placeName":"吴家墙","programId":"5eba6de5500c34036e52e8fd"},{"loc":{"lng":121.522,"lat":31.095},"images":"http://file57.quyangapp.com/avatar/t/34/10000034.jpg","programType":1,"signUps":[],"discussNum":0,"programFlag":1,"registerCityId":310100,"nickName":"路上","pubTime":1589275555896,"sex":1,"cityId":0,"delFlag":0,"programTime":"中午","userId":10000034,"content":"123456789","likeNum":0,"userLevel":"11010","signUpNums":0,"discusses":[],"programId":"5eba6ba332f5386d17d74c11","discussFlag":1,"likes":[]}]
     */

    private int pageCount;
    private int total;
    private int pageIndex;
    private int start;
    private int pageSize;
    private List<PageDataBean> pageData;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<PageDataBean> getPageData() {
        return pageData;
    }

    public void setPageData(List<PageDataBean> pageData) {
        this.pageData = pageData;
    }

    public static class PageDataBean {
        /**
         * loc : {"lng":121.5207250807694,"lat":31.09107920514784}
         * signUps : []
         * registerCityId : 310100
         * pubTime : 1589364882397
         * cityId : 0
         * delFlag : 0
         * title : 约电影
         * programTime : 下午
         * likeNum : 0
         * userLevel : 11010
         * discusses : []
         * discussFlag : 1
         * likes : []
         * images : http://file57.quyangapp.com/u/34/10000034/202005/o/f29bb4e3c8974f3aa5287c96832e57bb.jpg;http://file57.quyangapp.com/u/34/10000034/202005/o/bc411bf7b295439eb8eecd15bd591476.jpg
         * programType : 0
         * discussNum : 0
         * programFlag : 0
         * nickName : 路上
         * programDate : 1589969657
         * sex : 1
         * expectFriend : 看脸;有趣;大方
         * userId : 10000034
         * signUpNums : 0
         * placeName : 中国工商银行(上海市浦江高科技园支行)
         * programId : 5ebbc892f2091378e94e89ed
         * content : 啦啦啦
         */

        private LocBean loc;
        private int registerCityId;
        private long pubTime;
        private int cityId;
        private int delFlag;
        private String title;
        private String programTime;
        private int likeNum;
        private String userLevel;
        private int discussFlag;
        private String images;
        private int programType;
        private int discussNum;
        private int programFlag;
        private String nickName;
        private int programDate;
        private int sex;
        private String expectFriend;
        private int userId;
        private int signUpNums;
        private String placeName;
        private String programId;
        private String content;
        private List<?> signUps;
        private List<?> discusses;
        private List<?> likes;

        public LocBean getLoc() {
            return loc;
        }

        public void setLoc(LocBean loc) {
            this.loc = loc;
        }

        public int getRegisterCityId() {
            return registerCityId;
        }

        public void setRegisterCityId(int registerCityId) {
            this.registerCityId = registerCityId;
        }

        public long getPubTime() {
            return pubTime;
        }

        public void setPubTime(long pubTime) {
            this.pubTime = pubTime;
        }

        public int getCityId() {
            return cityId;
        }

        public void setCityId(int cityId) {
            this.cityId = cityId;
        }

        public int getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(int delFlag) {
            this.delFlag = delFlag;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getProgramTime() {
            return programTime;
        }

        public void setProgramTime(String programTime) {
            this.programTime = programTime;
        }

        public int getLikeNum() {
            return likeNum;
        }

        public void setLikeNum(int likeNum) {
            this.likeNum = likeNum;
        }

        public String getUserLevel() {
            return userLevel;
        }

        public void setUserLevel(String userLevel) {
            this.userLevel = userLevel;
        }

        public int getDiscussFlag() {
            return discussFlag;
        }

        public void setDiscussFlag(int discussFlag) {
            this.discussFlag = discussFlag;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }

        public int getProgramType() {
            return programType;
        }

        public void setProgramType(int programType) {
            this.programType = programType;
        }

        public int getDiscussNum() {
            return discussNum;
        }

        public void setDiscussNum(int discussNum) {
            this.discussNum = discussNum;
        }

        public int getProgramFlag() {
            return programFlag;
        }

        public void setProgramFlag(int programFlag) {
            this.programFlag = programFlag;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public int getProgramDate() {
            return programDate;
        }

        public void setProgramDate(int programDate) {
            this.programDate = programDate;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getExpectFriend() {
            return expectFriend;
        }

        public void setExpectFriend(String expectFriend) {
            this.expectFriend = expectFriend;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getSignUpNums() {
            return signUpNums;
        }

        public void setSignUpNums(int signUpNums) {
            this.signUpNums = signUpNums;
        }

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public String getProgramId() {
            return programId;
        }

        public void setProgramId(String programId) {
            this.programId = programId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<?> getSignUps() {
            return signUps;
        }

        public void setSignUps(List<?> signUps) {
            this.signUps = signUps;
        }

        public List<?> getDiscusses() {
            return discusses;
        }

        public void setDiscusses(List<?> discusses) {
            this.discusses = discusses;
        }

        public List<?> getLikes() {
            return likes;
        }

        public void setLikes(List<?> likes) {
            this.likes = likes;
        }

        public static class LocBean {
            /**
             * lng : 121.5207250807694
             * lat : 31.09107920514784
             */

            private double lng;
            private double lat;

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }
        }
    }
}
