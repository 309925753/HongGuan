package com.redchamber.radio;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.bar.AppointmentComActivity;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.home.adapter.MyFragmentPagerAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.radio.fragment.MyMomentFragment;
import com.redchamber.radio.fragment.MyProgramFragment;
import com.redchamber.release.ReleaseMomentActivity;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.view.ReleaseCodeDialog;
import com.redchamber.view.ReleaseRadioSelectDialog;
import com.redchamber.view.SelectProgramThemeDialog;
import com.redchamber.vip.VipCenterActivity;
import com.sk.weichat.R;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 个人中心 我的广播
 */
public class MyRadioActivity extends BaseActivity {

    @BindView(R.id.rg_radio)
    RadioGroup mRgRadio;
    @BindView(R.id.rb_moment)
    RadioButton mRbMoment;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private int position;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_my_radio;
    }

    @Override
    protected void initView() {
        setListener();
        initFragment();

    }

    @OnClick({R.id.iv_back, R.id.tv_release})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_release:
                pubCheck();
                break;
        }
    }

    /**
     * 发布前校验数据
     */
    private void pubCheck() {
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.post().url(coreManager.getConfig().RED_PUB_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            String  programData=result.getData();
                            JSONObject jsonObject= null;
                            try {
                                jsonObject = new JSONObject(programData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String   programId= jsonObject.optString("programId");
                            showRelease(programId);

                        } else {
                            showBarCom(result.getResultMsg(), result.getResultCode());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyRadioActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /**
     * 显示发布
     */
    private void showRelease(String programId) {
        ReleaseRadioSelectDialog radioSelectDialog = new ReleaseRadioSelectDialog(MyRadioActivity.this);
        radioSelectDialog.show();
        radioSelectDialog.setOnConfirmListener(new ReleaseRadioSelectDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if (0 == type) {
                    if(!TextUtils.isEmpty(programId)){
                        ToastUtil.showLongToast(MyRadioActivity.this,"当前已有进行中的节目");
                    }else {
                        SelectProgramThemeDialog selectProgramThemeDialog = new SelectProgramThemeDialog(MyRadioActivity.this);
                        selectProgramThemeDialog.show();
                    }
                } else {
                    ReleaseMomentActivity.startReleaseMomentActivity(MyRadioActivity.this);
                }
            }
        });
    }

    private int REAL_PEOPLE_VERIFY_FIRST = 100439;
    private int HAVENOLEGAL_POWER = 100440;
    private int TIMES_HAVE_USED = 100441;

    private void showBarCom(String title, int resultCode) {
        String confirm = null;

        if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
            confirm = "真人认证后才可以发布";

        } else if (resultCode == HAVENOLEGAL_POWER) {
            confirm = "会员才可以评论哦";
            title="成为会员，免费评论";

        } else if (resultCode == TIMES_HAVE_USED) {
            confirm = "次数已用完";
            queryFreeAuthTimes(resultCode);
            return;
        }else if(resultCode==100445)  {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        }else {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        }

        if(resultCode==100445){
            // 没有配置次数，提示升级到VIP或则余额购买 100445
            queryFreeAuthTimes(resultCode);
            return;
        }
        CommCodeDialog commCodeDialog = new CommCodeDialog(MyRadioActivity.this, title, confirm);
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
                    AuthenticationCenterActivity.startAuthenticationCenterActivity(MyRadioActivity.this);
                } else if (resultCode == HAVENOLEGAL_POWER) {
                    VipCenterActivity.startVipCenterActivity(MyRadioActivity.this);
                } else if (resultCode == TIMES_HAVE_USED) {
                    queryFreeAuthTimes(resultCode);
                }else  if(resultCode==100445){
                    //    VipCenterActivity.startVipCenterActivity(getContext());
                    //   queryFreeAuthTimes(resultCode);
                }

            }
        });

    }

    /**
     * 获取免费权限的次数
     */
    private void  queryFreeAuthTimes(int resultCode){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type","001");
        HttpUtils.post().url(coreManager.getConfig().RED_MY_QUERY_FREEAUTH_TIMES)
                .params(params)
                .build()
                .execute(new BaseCallback<QueryFreeAuthBean>(QueryFreeAuthBean.class) {

                    @Override
                    public void onResponse(ObjectResult<QueryFreeAuthBean> result) {
                        if (result.getResultCode() == 1&&result.getData()!=null) {
                            if(resultCode==TIMES_HAVE_USED){
                                payRed(result.getData(),TIMES_HAVE_USED);
                            }else if(resultCode==100445){
                                // 没有配置次数，提示升级到VIP或则余额购买 100445
                                VipOrPay(result.getData(),resultCode);
                            }

                        }else{
                            LogUtil.e(result.getResultMsg());
                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyRadioActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void  VipOrPay(QueryFreeAuthBean queryFreeAuthBean, int resultCode){

        ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(MyRadioActivity.this, "发布动态或节目", "支付"+queryFreeAuthBean.getPrice()+"红豆","会员免费发布"+queryFreeAuthBean.getVipSumTimes()+"次");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if(type==1){
                    payPublish(queryFreeAuthBean.getPrice());
                }else if(type==2){
                    VipCenterActivity.startVipCenterActivity(MyRadioActivity.this);
                }
            }
        });
    }
    /**
     *红豆支付
     */
    private void payRed(QueryFreeAuthBean queryFreeAuthBean, int resultCode){
        CommCodeDialog commCodeDialog = new CommCodeDialog(MyRadioActivity.this, "次数已用完", "支付"+queryFreeAuthBean.getPrice()+"红豆");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                payPublish(queryFreeAuthBean.getPrice());
            }
        });
    }

    /**
     * 付费发布
     */
    private void  payPublish(int payPublish){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("gold",String.valueOf(payPublish));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PAY_FORPUB)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            showRelease(null);
                        }else{
                            ToastUtil.showLongToast(MyRadioActivity.this,result.getResultMsg());
                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MyRadioActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, MyRadioActivity.class));
    }

    private void initFragment() {
        List<BaseFragment> alFragment = new ArrayList<>(2);
        alFragment.add(new MyMomentFragment());
        alFragment.add(new MyProgramFragment());
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), alFragment));
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mRgRadio.check(R.id.rb_moment);
                        break;
                    case 1:
                        mRgRadio.check(R.id.rb_program);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setListener() {
        mRgRadio.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        mRbMoment.performClick();
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_moment://我的动态
                    position = 0;
                    break;
                case R.id.rb_program://我的节目
                    position = 1;
                    break;
            }
            mViewPager.setCurrentItem(position, true);
        }
    }

}
