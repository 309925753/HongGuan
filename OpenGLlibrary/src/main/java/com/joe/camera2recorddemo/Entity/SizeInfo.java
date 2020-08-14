package com.joe.camera2recorddemo.Entity;

public class SizeInfo {

    private int mWidth;
    private int mHeight;

    public SizeInfo(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }


    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }


    @Override
    public String toString() {
        return "SizeInfo{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }
}
