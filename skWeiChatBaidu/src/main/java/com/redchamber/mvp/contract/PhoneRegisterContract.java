package com.redchamber.mvp.contract;



import com.redchamber.bean.SmsCodeBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.mvp.BaseView;

import java.util.Map;

import io.reactivex.Observable;

/**
 * 手机号码注册
 */
public interface PhoneRegisterContract {

    interface PhoneRegisterM extends BaseModel {

        Observable<BaseResponse<SmsCodeBean>> sendSmsCode(Map<String, String> map);

        Observable<BaseResponse> userRegister(Map<String, String> map);

    }

    interface PhoneRegisterV extends BaseView {

        void sendSmsCodeResult(int resultCode, String resultMsg);

        void userRegisterResult(int resultCode, String resultMsg);

    }

    abstract class PhoneRegisterP extends BasePresenter<PhoneRegisterM, PhoneRegisterV> {

        public abstract void sendSmsCode(Map<String, String> map);

        public abstract void userRegister(Map<String, String> map);

    }


}
