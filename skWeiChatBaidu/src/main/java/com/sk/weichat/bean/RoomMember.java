package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.db.dao.RoomMemberDaoImpl;

/**
 * Created by zq on 2017/6/27 0027.
 */
@DatabaseTable(daoClass = RoomMemberDaoImpl.class)
public class RoomMember {
    public static final int ROLE_OWNER = 1;
    public static final int ROLE_MANAGER = 2;
    public static final int ROLE_MEMBER = 3;
    public static final int ROLE_INVISIBLE = 4;
    public static final int ROLE_GUARDIAN = 5;

    @DatabaseField(generatedId = true)
    private int _id;
    // 房间id
    @DatabaseField
    private String roomId;
    // 用户id
    @DatabaseField
    private String userId;
    // 用户昵称 A
    @DatabaseField
    private String userName;
    // 群主对该群内成员的备注名 仅群主可见
    @DatabaseField
    private String cardName;
    /**
     * 1创建者，2管理员，3成员,
     * 4, 隐身人，
     * 5，监控人，
     * 隐身人和监控人：即群主设置某成员为这2个角色，则群员数量减1,其他人完全看不到他；隐身人和监控人的区别是，前者不可以说话，后者能说话。
     */
    // 职位
    @DatabaseField
    private int role;
    // 加入时间
    @DatabaseField
    private int createTime;

    public RoomMember() {

    }

    public static boolean shouldSendRead(Integer role) {
        if (role == null) {
            return true;
        }
        return role != 4 && role != 5;
    }

    /**
     * 返回是群主或者管理员，
     * 用于显示管理员头像相框，
     */
    public boolean isGroupOwnerOrManager() {
        return getRole() == ROLE_OWNER || getRole() == ROLE_MANAGER;
    }

    /**
     * 全员禁言是否对此人生效，
     * {@link com.sk.weichat.bean.message.MucRoomMember#isAllBannedEffective}
     * {@link com.sk.weichat.bean.RoomMember#isAllBannedEffective}
     */
    public boolean isAllBannedEffective() {
        return getRole() == ROLE_MEMBER;
    }

    /**
     * 是否是隐身人，不能发言，
     */
    public boolean isInvisible() {
        return getRole() == ROLE_INVISIBLE;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }
}
