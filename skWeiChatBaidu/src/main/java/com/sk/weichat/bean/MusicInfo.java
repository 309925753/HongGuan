package com.sk.weichat.bean;

import com.alibaba.fastjson.JSONObject;

public class MusicInfo {

    public String cover; // 封面图地址
    public long length; // 音乐长度
    public String name; // 音乐名称
    public String nikeName; // 创作人
    public String path; // 音乐地址
    public int state; // 状态 0未下载 1,播放中，2,下载中，3暂停中
    public int useCount;
    public String id;


    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void parseData(JSONObject data) {
        this.cover = data.getString("cover");
        this.path = data.getString("path");
        this.name = data.getString("name");
        this.nikeName = data.getString("nikeName");
        this.length = data.getInteger("length");
    }

    public void appendDown(String mDown) {
        this.path = mDown + this.path;
    }
}
