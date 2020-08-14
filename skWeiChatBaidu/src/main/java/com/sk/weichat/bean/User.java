package com.sk.weichat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.MyApplication;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.StringUtils;

import java.util.List;

/**
 * 用户表
 */
@DatabaseTable(tableName = "user")
public class User implements Parcelable, Cloneable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private static final long serialVersionUID = 8216104856016715920L;
    @DatabaseField(id = true)
    private String userId;// 用户Id
    @DatabaseField
    private int userType;// 用户类型
    @DatabaseField(canBeNull = false)
    @JSONField(name = "nickname")
    private String nickName;// 昵称
    @DatabaseField(canBeNull = false)
    private String telephone; // 手机号码，带区号，
    @DatabaseField(canBeNull = false)
    private String password;
    @DatabaseField
    private String description;// 签名
    @DatabaseField
    private long birthday;// 公历生日
    @DatabaseField
    private long showLastLoginTime;// 上次登录时间，
    @DatabaseField(defaultValue = "0")
    private int sex;// 性别 0表示女，1表示男
    @DatabaseField
    private int countryId; // 国家编号
    @DatabaseField
    private int provinceId;// 省份编号
    @DatabaseField
    private int cityId;    // 城市编号
    @DatabaseField
    private int areaId;   // 地区编号
    @DatabaseField
    private int integral; // 积分
    @DatabaseField
    private int integralTotal;// 积分总数
    @DatabaseField
    private int level;  // 等级
    @DatabaseField
    private float money;// 金钱
    @DatabaseField
    private float moneyTotal;// 金钱总数
    @DatabaseField
    private int vip;// vip等级
    @DatabaseField
    private int friendsCount; // 朋友总数
    @DatabaseField
    private int fansCount;     // 粉丝总数
    @DatabaseField
    private int attCount;        // 关注总数
    @DatabaseField
    private int isAuth;// 是否认证
    @DatabaseField
    private int status;// 状态(未知)
    @DatabaseField
    private long offlineTime;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Company company;// 所在公司信息
    @DatabaseField
    private String msgBackGroundUrl;// 朋友圈背景URL
    @DatabaseField
    private String account;
    @DatabaseField
    private int setAccountCount;
    /* 请求用户与该用户的关系 */
    private AttentionUser friends;
    private Loc loc;
    private String myInviteCode;
    private double balance; // 余额
    // 1=游客（用于后台浏览数据）；2=公众号 ；3=机器账号，由系统自动生成；4=客服账号;5=管理员；6=超级管理员；7=财务；
    private List<Integer> role; // 身份，
    private int offlineNoPushMsg;
    private int onlinestate;// 好友是否在线
    private int createTime;// 注册日期，
    private int notSeeHim;//朋友圈访问权限，不看他
    private int notLetSeeHim;//朋友圈访问权限，不让他看
    // String类型以防万一服务器把用户支付密码密文给返回了，导致解析错误，
    private String payPassword; // 是否已经设置了支付密码，
    private int walletUserNo; // 是否已经开户了云钱包，
    // 隐私设置
    private PrivacySetting settings;
    // Secure
    private String dhMsgPublicKey;
    private String dhMsgPrivateKey;
    private String rsaMsgPublicKey;
    private String rsaMsgPrivateKey;

    // 无参构造函数
    public User() {
    }

    protected User(Parcel in) {
        userId = in.readString();
        userType = in.readInt();
        nickName = in.readString();
        telephone = in.readString();
        password = in.readString();
        description = in.readString();
        birthday = in.readLong();
        showLastLoginTime = in.readLong();
        sex = in.readInt();
        countryId = in.readInt();
        provinceId = in.readInt();
        cityId = in.readInt();
        areaId = in.readInt();
        integral = in.readInt();
        integralTotal = in.readInt();
        level = in.readInt();
        money = in.readFloat();
        moneyTotal = in.readFloat();
        vip = in.readInt();
        friendsCount = in.readInt();
        fansCount = in.readInt();
        attCount = in.readInt();
        isAuth = in.readInt();
        status = in.readInt();
        offlineTime = in.readLong();
        msgBackGroundUrl = in.readString();
        account = in.readString();
        setAccountCount = in.readInt();
        myInviteCode = in.readString();
        balance = in.readDouble();
        offlineNoPushMsg = in.readInt();
        onlinestate = in.readInt();
        createTime = in.readInt();
    }

    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public String getMyInviteCode() {
        return myInviteCode;
    }

    public void setMyInviteCode(String myInviteCode) {
        this.myInviteCode = myInviteCode;
    }

    public double getBalance() {
        return balance;
    }

    // TODO: 这个余额设置方法有大量调用只修改内存变量，被杀后重建无法恢复，
    // 要更新数据库，
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Integer> getRole() {
        return role;
    }

    public void setRole(List<Integer> role) {
        this.role = role;
    }

    public int getOfflineNoPushMsg() {
        return offlineNoPushMsg;
    }

    public void setOfflineNoPushMsg(int offlineNoPushMsg) {
        this.offlineNoPushMsg = offlineNoPushMsg;
    }

    public int getOnlinestate() {
        return onlinestate;
    }

    public void setOnlinestate(int onlinestate) {
        this.onlinestate = onlinestate;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getNotSeeHim() {
        return notSeeHim;
    }

    public void setNotSeeHim(int notSeeHim) {
        this.notSeeHim = notSeeHim;
    }

    public int getNotLetSeeHim() {
        return notLetSeeHim;
    }

    public void setNotLetSeeHim(int notLetSeeHim) {
        this.notLetSeeHim = notLetSeeHim;
    }

    public String getDhMsgPublicKey() {
        return dhMsgPublicKey;
    }

    public void setDhMsgPublicKey(String dhMsgPublicKey) {
        this.dhMsgPublicKey = dhMsgPublicKey;
    }

    public String getDhMsgPrivateKey() {
        return dhMsgPrivateKey;
    }

    public void setDhMsgPrivateKey(String dhMsgPrivateKey) {
        this.dhMsgPrivateKey = dhMsgPrivateKey;
    }

    public String getRsaMsgPublicKey() {
        return rsaMsgPublicKey;
    }

    public void setRsaMsgPublicKey(String rsaMsgPublicKey) {
        this.rsaMsgPublicKey = rsaMsgPublicKey;
    }

    public String getRsaMsgPrivateKey() {
        return rsaMsgPrivateKey;
    }

    public void setRsaMsgPrivateKey(String rsaMsgPrivateKey) {
        this.rsaMsgPrivateKey = rsaMsgPrivateKey;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        User user = (User) super.clone();
        // 不需要下面两个成员变量的克隆
        user.setCompany(null);
        user.setFriends(null);
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof User)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        User other = (User) o;
        boolean equals = true;
        equals &= StringUtils.strEquals(userId, other.userId);
        equals &= userType == other.userType;
        equals &= StringUtils.strEquals(nickName, other.nickName);
        equals &= StringUtils.strEquals(telephone, other.telephone);
        equals &= StringUtils.strEquals(password, other.password);
        equals &= StringUtils.strEquals(description, other.description);
        equals &= birthday == other.birthday;
        equals &= sex == other.sex;
        equals &= countryId == other.countryId;
        equals &= provinceId == other.provinceId;
        equals &= cityId == other.cityId;
        equals &= areaId == other.areaId;
        equals &= integral == other.integral;
        equals &= integralTotal == other.integralTotal;
        equals &= level == other.level;
        equals &= money == other.money;
        equals &= moneyTotal == other.moneyTotal;
        equals &= offlineTime == other.offlineTime;
        equals &= vip == other.vip;
        equals &= friendsCount == other.friendsCount;
        equals &= fansCount == other.fansCount;
        equals &= attCount == other.attCount;
        equals &= isAuth == other.isAuth;
        equals &= status == other.status;
        // 下面两个成员变量不比较
        // Company company;// 所在公司信息
        // AttentionUser friends;
        return equals;
    }


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getSetAccountCount() {
        return setAccountCount;
    }

    public void setSetAccountCount(int setAccountCount) {
        this.setAccountCount = setAccountCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * 获取一个没有areaCode的telephone，仅用于显示，不参与接口传参与非空判断等
     *
     * @return
     */
    public String getTelephoneNoAreaCode() {
        String areaCode = String.valueOf(PreferenceUtils.getInt(MyApplication.getContext(), Constants.AREA_CODE_KEY, 86));
        // 删除开头的区号，
        if (telephone.startsWith(areaCode)) {
            telephone = telephone.substring(areaCode.length());
        }
        return telephone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public int getIntegral() {
        return integral;
    }

    public void setIntegral(int integral) {
        this.integral = integral;
    }

    public int getIntegralTotal() {
        return integralTotal;
    }

    public void setIntegralTotal(int integralTotal) {
        this.integralTotal = integralTotal;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public float getMoneyTotal() {
        return moneyTotal;
    }

    public void setMoneyTotal(float moneyTotal) {
        this.moneyTotal = moneyTotal;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vipLevel) {
        this.vip = vipLevel;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public long getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(long offlineTime) {
        this.offlineTime = offlineTime;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public int getAttCount() {
        return attCount;
    }

    public void setAttCount(int attCount) {
        this.attCount = attCount;
    }

    public int getIsAuth() {
        return isAuth;
    }

    public void setIsAuth(int isAuth) {
        this.isAuth = isAuth;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public AttentionUser getFriends() {
        return friends;
    }

    public void setFriends(AttentionUser friends) {
        this.friends = friends;
    }

    /* 快捷方法 */
    public boolean isCompanyUser() {
        if (company != null && company.getId() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getCompanyId() {
        if (company != null && company.getId() > 0) {
            return company.getId();
        } else {
            return 0;
        }
    }

    /*  获取两者之间的好友关系 */
    public int getRelationshipStatus() {
        if (friends == null) {
            return Friend.STATUS_UNKNOW;
        } else {
            return friends.getStatus();
        }
    }

    /* 获取两者之间的黑名单关系 */
    public int getBlacklist() {
        if (friends == null) {
            return 0;
        } else {
            return friends.getBlacklist();
        }
    }

    /**
     * 是否是普通用户，会受限制的那种，
     *
     * @return 返回true表示是普通用户，
     */
    public boolean isOrdinaryUser() {
        return getRole() == null
                || getRole().isEmpty();
    }

    public String getMsgBackGroundUrl() {
        return msgBackGroundUrl;
    }

    public void setMsgBackGroundUrl(String msgBackGroundUrl) {
        this.msgBackGroundUrl = msgBackGroundUrl;
    }

    public boolean isSuperManager() {
        if (getRole() == null || getRole().isEmpty()) {
            return false;
        }
        for (int role : getRole()) {
            if (role == 5 || role == 6) {
                return true;
            }
        }
        return false;
    }

    public long getShowLastLoginTime() {
        return showLastLoginTime;
    }

    public void setShowLastLoginTime(long showLastLoginTime) {
        this.showLastLoginTime = showLastLoginTime;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public PrivacySetting getSettings() {
        return settings;
    }

    public void setSettings(PrivacySetting settings) {
        this.settings = settings;
    }

    public int getWalletUserNo() {
        return walletUserNo;
    }

    public void setWalletUserNo(int walletUserNo) {
        this.walletUserNo = walletUserNo;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userType=" + userType +
                ", nickName='" + nickName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", password='" + password + '\'' +
                ", description='" + description + '\'' +
                ", birthday=" + birthday +
                ", showLastLoginTime=" + showLastLoginTime +
                ", sex=" + sex +
                ", countryId=" + countryId +
                ", provinceId=" + provinceId +
                ", cityId=" + cityId +
                ", areaId=" + areaId +
                ", integral=" + integral +
                ", integralTotal=" + integralTotal +
                ", level=" + level +
                ", money=" + money +
                ", moneyTotal=" + moneyTotal +
                ", vip=" + vip +
                ", friendsCount=" + friendsCount +
                ", fansCount=" + fansCount +
                ", attCount=" + attCount +
                ", isAuth=" + isAuth +
                ", status=" + status +
                ", offlineTime=" + offlineTime +
                ", company=" + company +
                ", msgBackGroundUrl='" + msgBackGroundUrl + '\'' +
                ", account='" + account + '\'' +
                ", setAccountCount=" + setAccountCount +
                ", friends=" + friends +
                ", loc=" + loc +
                ", myInviteCode='" + myInviteCode + '\'' +
                ", balance=" + balance +
                ", role=" + role +
                ", offlineNoPushMsg=" + offlineNoPushMsg +
                ", onlinestate=" + onlinestate +
                ", createTime=" + createTime +
                ", notSeeHim=" + notSeeHim +
                ", notLetSeeHim=" + notLetSeeHim +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeInt(userType);
        dest.writeString(nickName);
        dest.writeString(telephone);
        dest.writeString(password);
        dest.writeString(description);
        dest.writeLong(birthday);
        dest.writeLong(showLastLoginTime);
        dest.writeInt(sex);
        dest.writeInt(countryId);
        dest.writeInt(provinceId);
        dest.writeInt(cityId);
        dest.writeInt(areaId);
        dest.writeInt(integral);
        dest.writeInt(integralTotal);
        dest.writeInt(level);
        dest.writeFloat(money);
        dest.writeFloat(moneyTotal);
        dest.writeInt(vip);
        dest.writeInt(friendsCount);
        dest.writeInt(fansCount);
        dest.writeInt(attCount);
        dest.writeInt(isAuth);
        dest.writeInt(status);
        dest.writeLong(offlineTime);
        dest.writeString(msgBackGroundUrl);
        dest.writeString(account);
        dest.writeInt(setAccountCount);
        dest.writeString(myInviteCode);
        dest.writeDouble(balance);
        dest.writeInt(offlineNoPushMsg);
        dest.writeInt(onlinestate);
        dest.writeInt(createTime);
    }

    public static class Loc {
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
}
