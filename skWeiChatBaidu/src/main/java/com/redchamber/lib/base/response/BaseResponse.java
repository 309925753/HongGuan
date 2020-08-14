package com.redchamber.lib.base.response;

public class BaseResponse<T> {

    public String code;

    public int resultCode;

    public long currentTime;

    public String resultMsg;

    public T data;

}
