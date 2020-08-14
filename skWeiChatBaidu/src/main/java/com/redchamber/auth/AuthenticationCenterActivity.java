package com.redchamber.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.redchamber.bean.CertificateBean;
import com.redchamber.bean.MyHomepageBean;
import com.redchamber.event.UpdateAuthentionEvent;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.PersonalInfoRequest;
import com.redchamber.util.UserLevelUtils;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 认证中心
 */
public class AuthenticationCenterActivity extends BaseActivity {

    @BindView(R.id.ll_privilege_a)
    LinearLayout mLlPrivilegeA;
    @BindView(R.id.ll_privilege_b)
    LinearLayout mLlPrivilegeB;
    @BindView(R.id.ll_privilege_c)
    LinearLayout mLlPrivilegeC;
    @BindView(R.id.ll_privilege_d)
    LinearLayout mLlPrivilegeD;
    @BindView(R.id.ll_privilege_e)
    LinearLayout mLlPrivilegeE;
    @BindView(R.id.ll_privilege_f)
    LinearLayout mLlPrivilegeF;
    @BindView(R.id.iv_lock_a)
    ImageView mIvLockA;
    @BindView(R.id.iv_lock_b)
    ImageView mIvLockB;
    @BindView(R.id.iv_lock_c)
    ImageView mIvLockC;
    @BindView(R.id.iv_lock_d)
    ImageView mIvLockD;
    @BindView(R.id.iv_lock_e)
    ImageView mIvLockE;
    @BindView(R.id.iv_lock_f)
    ImageView mIvLockF;
    @BindView(R.id.tv_true_man)
    TextView tvTrueMan;
    @BindView(R.id.tv_true_gir)
    TextView tvTrueGir;
    @BindView(R.id.ll_girl)
    LinearLayout llGirl;
    @BindView(R.id.ll_tequan)
    LinearLayout llTequan;
    @BindView(R.id.tv_comm_a)
    TextView tvCommA;
    @BindView(R.id.tv_comm_b)
    TextView tvCommB;
    @BindView(R.id.tv_comm_c)
    TextView tvCommC;

    private CertificateBean.DataBean certificateBean = new CertificateBean.DataBean();

    @Override
    protected int setLayout() {
        return R.layout.red_activity_authentication_center;
    }

    @Override
    protected void initView() {
        postCerifcalion();
        EventBusHelper.register(this);
    }

    @OnClick({R.id.iv_back, R.id.ll_true, R.id.ll_girl})
    void onClick(View view) {


        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_true:
                if (cert == 0 ) {
                        AuthenticationRealActivity.startAuthenticationRealActivity(this);
                }
                break;
            case R.id.ll_girl:
                if (certGir == 0&& MyApplication.mMyHomepageBean != null) {
                        char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
                        if(arr[3]=='0'){
                            ToastUtil.showToast(this,"必须先真人认证之后才能进行女神认证");
                        }else {
                            AuthenticationGirlActivity.startAuthenticationGirlActivity(this);
                        }

                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(UpdateAuthentionEvent message) {
        postCerifcalion();
        getMyHomepage();
    }

    public static void startAuthenticationCenterActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, AuthenticationCenterActivity.class));
    }

    private void setStatus(int status) {
        if (0 == status) {
            mLlPrivilegeA.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mLlPrivilegeB.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mLlPrivilegeC.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mLlPrivilegeD.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mLlPrivilegeE.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mLlPrivilegeF.setBackgroundResource(R.drawable.red_shape_ededed_6);
            mIvLockA.setVisibility(View.VISIBLE);
            mIvLockB.setVisibility(View.VISIBLE);
            mIvLockC.setVisibility(View.VISIBLE);
            mIvLockD.setVisibility(View.VISIBLE);
            mIvLockE.setVisibility(View.VISIBLE);
            mIvLockF.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     */
    private void postCerifcalion() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.post().url(coreManager.getConfig().RED_MY_QUERY_CERTIFICATE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        // 人脸在线活体检测
                        if (result.getResultCode() == 1) {
                            List<CertificateBean.DataBean> certificateBean = JSON.parseArray(result.getData(), CertificateBean.DataBean.class);
                            if (certificateBean != null) {
                                setUiData(certificateBean);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(AuthenticationCenterActivity.this);
                    }
                });
    }

    private int cert = 0;
    private int certGir = 0;


    private void setUiData(List<CertificateBean.DataBean> certificateBean) {
        // tvTrueMan     //tvTrueGir
        //
        // 状态 0审核中 1已通过 flage 真人认证通过          女神认证通过  真人认证(审核中)
        //认证类型 0真人认证 1女神/男神 认证 type

        for (int i = 0; i < certificateBean.size(); i++) {
            if (certificateBean.get(i).getType() == 0) {
                if (certificateBean.get(i).getFlag() == 0) {
                    tvTrueMan.setText("真人认证(审核中)");
                    cert = 1;
                } else if (certificateBean.get(i).getFlag() == 1) {
                    tvTrueMan.setText("真人认证已通过");
                    mIvLockA.setVisibility(View.INVISIBLE);
                    mIvLockB.setVisibility(View.INVISIBLE);
                    mIvLockC.setVisibility(View.INVISIBLE);
                    mIvLockD.setVisibility(View.INVISIBLE);
                    cert = 1;
                }
               /* if(certificateBean.get(i).getFlag()==0){
                    tvTrueMan.setText("真人认证(审核中)");
                }else if(certificateBean.get(i).getFlag()==1){
                    tvTrueMan.setText("真人认证已通过");
                }*/
            } else if (certificateBean.get(i).getType() == 1) {
                if (certificateBean.get(i).getFlag() == 0) {
                    tvTrueGir.setText("女神认证(审核中)");
                    certGir = 1;
                } else if (certificateBean.get(i).getFlag() == 1) {
                    tvTrueGir.setText("女神认证已通过");
                    mIvLockA.setVisibility(View.INVISIBLE);
                    mIvLockB.setVisibility(View.INVISIBLE);
                    mIvLockC.setVisibility(View.INVISIBLE);
                    mIvLockD.setVisibility(View.INVISIBLE);
                    mIvLockE.setVisibility(View.INVISIBLE);
                    mIvLockF.setVisibility(View.INVISIBLE);
                    certGir = 1;
                }
              /*  if(certificateBean.get(i).getFlag()==1){
                    tvTrueMan.setText("真人认证(审核中)");
                }else if(certificateBean.get(i).getFlag()==1){
                    tvTrueMan.setText("真人认证已通过");
                }*/
            }

        }
        /*if(certificateBean.getType()==1){
            //状态 0审核中 1已通过"
            if(certificateBean.getFlag()==0){
                tvTrueMan.setText("女神认证(审核中)");
            }else if(certificateBean.getFlag()==1){
                tvTrueMan.setText("女神认证通过");

            }
            if(certificateBean.getCertificateInfo()!=null){
                tvTrueMan.setText("真人认证");
            }
        }

        if(certificateBean.getType()==0){
            //状态 0审核中 1已通过"
            if(certificateBean.getFlag()==0){
                tvTrueMan.setText("真人认证(审核中)");
            }else if(certificateBean.getFlag()==1){
                tvTrueMan.setText("真人认证通过");

            }
           *//* if(certificateBean.getCertificateInfo()!=null){
                tvTrueMan.setText("女神认证");
            }*//*
        }*/
        if (MyApplication.mMyHomepageBean != null) {
            boolean[] userLevels = UserLevelUtils.getLevels(MyApplication.mMyHomepageBean.userLevel);
            if (!userLevels[0]) {//男性
                llGirl.setVisibility(View.INVISIBLE);
                tvCommA.setText("评论女士广播");
                tvCommB.setText("报名女士广播");
                tvCommC.setText("私聊女士");
                mLlPrivilegeE.setVisibility(View.INVISIBLE);
                mLlPrivilegeF.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void getMyHomepage() {
        PersonalInfoRequest.getInstance().getMyHomepage(AuthenticationCenterActivity.this, new PersonalInfoRequest.PersonalInfoCallBack() {
            @Override
            public void onSuccess(MyHomepageBean myHomepageBean) {
                MyApplication.mMyHomepageBean = myHomepageBean;

            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }


}
