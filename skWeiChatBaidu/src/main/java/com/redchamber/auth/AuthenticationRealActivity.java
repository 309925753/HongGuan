package com.redchamber.auth;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.redchamber.bean.BaiduFaceDetectBean;
import com.redchamber.event.UpdateAuthentionEvent;
import com.redchamber.face.FaceLivenessExpActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.GsonUtils;
import com.redchamber.util.HttpUtil;
import com.redchamber.util.UserLevelUtils;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

/**
 * 真人认证
 */
public class AuthenticationRealActivity extends BaseActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected int setLayout() {
        return R.layout.red_activity_authentication_real;
    }

    @Override
    protected void initView() {

    }

    @OnClick({R.id.iv_back, R.id.ll_start})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_start:
            /*    CommonHintSingleDialog commonHintSingleDialog = new CommonHintSingleDialog(this,
                        "【我的相册】中至少要有一张本人露脸照片才可以认证哦", "确定");
                commonHintSingleDialog.show();
                commonHintSingleDialog.setOnConfirmListener(new CommonHintSingleDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastUtils.showToast("开始认证");
                    }
                });*/
                Intent intent = new Intent(this, FaceLivenessExpActivity.class);
                startActivityForResult(intent, 100);
                break;
        }
    }

    public static void startAuthenticationRealActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, AuthenticationRealActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                if (data != null) {
                    final String image = data.getStringExtra("image");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.e("data =" + image);
                            //  ToastUtil.showLongToast(AuthenticationRealActivity.this,"image data = " +image);
//                            if(!TextUtils.isEmpty(image)){
//                                postCerifcalion(image);
//                            }

                            faceMatch(image);

                        }
                    }).start();
                }
                break;
        }
    }

    /**
     * 百度返回图片数据并请求后台
     *
     * @param base64
     */
    private void postCerifcalion(String base64) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("certificateInfo", base64);
        params.put("type", String.valueOf(0));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_COMMIT_CERTIFICATE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        // 人脸在线活体检测
                        if (Result.checkSuccess(AuthenticationRealActivity.this, result)) {
                            EventBus.getDefault().post(new UpdateAuthentionEvent(""));
                            ToastUtil.showLongToast(AuthenticationRealActivity.this, "认证成功");
                            finish();
                        } else {
                            ToastUtil.showLongToast(AuthenticationRealActivity.this, "认证失败请重新认证");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(AuthenticationRealActivity.this);
                    }
                });
    }

    private void faceMatch(String image) {
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/detect";

        final Observable<BaiduFaceDetectBean> observable = Observable.create(new ObservableOnSubscribe<BaiduFaceDetectBean>() {

            @Override
            public void subscribe(ObservableEmitter<BaiduFaceDetectBean> e) throws Exception {
                Map<String, Object> map = new HashMap<>();
                map.put("image", image);
                map.put("face_field", "gender");
                map.put("image_type", "BASE64");

                String param = GsonUtils.toJson(map);

                String accessToken = CoreManager.getInstance(AuthenticationRealActivity.this).getConfig().baiduAiToken;

                String result = HttpUtil.post(url, accessToken, "application/json", param);
                BaiduFaceDetectBean entity = new Gson().fromJson(result, BaiduFaceDetectBean.class);
                e.onNext(entity);
                e.onComplete();
            }

        });
        DisposableObserver<BaiduFaceDetectBean> disposableObserver = new DisposableObserver<BaiduFaceDetectBean>() {

            @Override
            public void onNext(BaiduFaceDetectBean value) {
                if (value != null && value.result != null && value.result.face_list != null && value.result.face_list.size() > 0) {
                    BaiduFaceDetectBean.Result.Face.Gender gender = value.result.face_list.get(0).gender;
                    if (TextUtils.equals(UserLevelUtils.getSex(MyApplication.mMyHomepageBean.userLevel), gender.type)) { //性别相同
                        postCerifcalion(image);
                    } else {
                        fenghao();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showToast(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
        mCompositeDisposable.add(disposableObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void fenghao() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.post().url(coreManager.getConfig().RED_FENG_HAO)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (1 == result.getResultCode()) {
                            ToastUtils.showToast("您上传性别与本人不一致，账号冻结，联系客服微信公众号：红馆E");
                            Intent intent = new Intent(AuthenticationRealActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(AuthenticationRealActivity.this);
                    }
                });
    }

}
