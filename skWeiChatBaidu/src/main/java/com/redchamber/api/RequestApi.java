package com.redchamber.api;

import com.redchamber.bean.IndustryJobBean;
import com.redchamber.bean.SmsCodeBean;
import com.redchamber.lib.base.response.BaseResponse;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface RequestApi {

    @GET("basic/randcode/sendSms")
    Observable<BaseResponse<SmsCodeBean>> sendSmsCode(@QueryMap Map<String, String> map);

    @POST("user/register/v1")
    Observable<BaseResponse> userRegister(@QueryMap Map<String, String> map);

    @POST("userInfo/setSex")
    Observable<BaseResponse> setSex(@Field("sex") int sex);

    Observable<BaseResponse<List<IndustryJobBean>>> getJobList();

}
