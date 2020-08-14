package com.redchamber.mvp.model;

import com.redchamber.api.AppConfig;
import com.redchamber.api.RequestApi;
import com.redchamber.bean.SmsCodeBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.RetrofitManager;
import com.redchamber.lib.utils.MAC;
import com.redchamber.lib.utils.MD5;
import com.redchamber.lib.utils.Parameter;
import com.redchamber.mvp.contract.PhoneRegisterContract;

import java.util.Map;

import io.reactivex.Observable;

public class PhoneRegisterModel implements PhoneRegisterContract.PhoneRegisterM {

    @Override
    public Observable<BaseResponse<SmsCodeBean>> sendSmsCode(Map<String, String> mapCode) {
        String salt = String.valueOf(System.currentTimeMillis());
        String macContent = AppConfig.apiKey + Parameter.joinValues(mapCode) + salt;
        byte[] key = MD5.encrypt(AppConfig.apiKey);
        String mac = MAC.encodeBase64(macContent.getBytes(), key);
        mapCode.put("salt", salt);
        mapCode.put("secret", mac);
        return RetrofitManager.getApiService(RequestApi.class).sendSmsCode(mapCode);
    }

    @Override
    public Observable<BaseResponse> userRegister(Map<String, String> map) {
        long time = System.currentTimeMillis() ;

        String macContent = AppConfig.apiKey + Parameter.joinValues(map) + time;
        byte[] keyData = MD5.encrypt(AppConfig.apiKey);
        String data = MAC.encodeBase64(macContent.getBytes(), keyData);
        map.put("data", data);

        String secretContent = AppConfig.apiKey + Parameter.joinValues(map) + time;
        byte[] keySecret = MD5.encrypt(AppConfig.apiKey);
        String secret = MAC.encodeBase64(secretContent.getBytes(), keySecret);
        map.put("secret", secret);

        map.put("time", String.valueOf(time));
        map.put("salt", String.valueOf(time));

        return RetrofitManager.getApiService(RequestApi.class).userRegister(map);
    }

}
