package com.redchamber.bean;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class RegisterInfoBean implements Serializable, Parcelable {
    public RegisterInfoBean() {
    }

    public String telephone;
    public String smsCode;
    public String password;
    public String nickname;
    public String sex;
    public String birthDay;
    public String residentCity;
    public String position;
    public String program;
    public String expectFriend;
    public String height;
    public String weight;
    public String selfDesc;
    public String inviteCode;
    public Uri headImage;
    public String userType;
    public String areaCode;
    public String isSmsRegister;
    public String realPassword;
    public String thirdToken;
    public String thirdTokenType;
    public String areaName;

    public RegisterInfoBean(Parcel in) {
        telephone = in.readString();
        smsCode = in.readString();
        password = in.readString();
        nickname = in.readString();
        sex = in.readString();
        birthDay = in.readString();
        residentCity = in.readString();
        position = in.readString();
        program = in.readString();
        expectFriend = in.readString();
        height = in.readString();
        weight = in.readString();
        selfDesc = in.readString();
        inviteCode = in.readString();
        headImage = in.readParcelable(Uri.class.getClassLoader());
        userType = in.readString();
        areaCode = in.readString();
        isSmsRegister = in.readString();
        realPassword = in.readString();
        thirdToken = in.readString();
        thirdTokenType = in.readString();
        areaName = in.readString();
    }

    public static final Creator<RegisterInfoBean> CREATOR = new Creator<RegisterInfoBean>() {
        @Override
        public RegisterInfoBean createFromParcel(Parcel in) {
            return new RegisterInfoBean(in);
        }

        @Override
        public RegisterInfoBean[] newArray(int size) {
            return new RegisterInfoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(telephone);
        parcel.writeString(smsCode);
        parcel.writeString(password);
        parcel.writeString(nickname);
        parcel.writeString(sex);
        parcel.writeString(birthDay);
        parcel.writeString(residentCity);
        parcel.writeString(position);
        parcel.writeString(program);
        parcel.writeString(expectFriend);
        parcel.writeString(height);
        parcel.writeString(weight);
        parcel.writeString(selfDesc);
        parcel.writeString(inviteCode);
        parcel.writeParcelable(headImage, i);
        parcel.writeString(userType);
        parcel.writeString(areaCode);
        parcel.writeString(isSmsRegister);
        parcel.writeString(realPassword);
        parcel.writeString(thirdToken);
        parcel.writeString(thirdTokenType);
        parcel.writeString(areaName);
    }
}
