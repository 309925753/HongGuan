package com.sk.weichat.bean;

import java.util.Objects;

public class SelectFriendItem {
    private String userId;
    private String name;
    private int isRoom; // 01节约空间，

    @SuppressWarnings("unused")
    public SelectFriendItem() {
        // fastjson反序列化需要，
    }

    public SelectFriendItem(String userId, String name, int isRoom) {
        this.userId = userId;
        this.name = name;
        this.isRoom = isRoom;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsRoom() {
        return isRoom;
    }

    public void setIsRoom(int isRoom) {
        this.isRoom = isRoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectFriendItem that = (SelectFriendItem) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
