package com.redchamber.setting;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.AppSettingBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.AppSettingRequest;
import com.redchamber.web.WebViewActivity;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.ui.lock.DeviceLockHelper;
import com.sk.weichat.util.GetFileSizeUtil;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.view.window.WindowShowService;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 设置
 */
public class SettingActivity extends BaseActivity {

    @BindView(R.id.cb_call)
    CheckBox mCbCall;
    @BindView(R.id.cb_notify)
    CheckBox mCbNotify;
    @BindView(R.id.tv_phone)
    TextView mTvPhone;
    @BindView(R.id.tv_cache)
    TextView mTvCache;

    private AppSettingBean mAppSettingBean;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_setting;
    }

    @Override
    protected void initView() {
        long cacheSize = GetFileSizeUtil.getFileSize(new File(MyApplication.getInstance().mAppDir));
        mTvCache.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        addCheckboxListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryAppSetting();
    }

    @OnClick({R.id.iv_back, R.id.rl_set_privacy, R.id.rl_phone, R.id.rl_password, R.id.rl_cache,
            R.id.rl_agreement_platform, R.id.rl_agreement_user, R.id.rl_privacy_user, R.id.tv_logout})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_set_privacy:
                PrivacySettingActivity.startPrivacySettingActivity(this);
                break;
            case R.id.rl_phone:
                BindPhoneActivity.startActivity(this);
                break;
            case R.id.rl_password:
                ChangePasswordActivity.startActivity(this);
                break;
            case R.id.rl_cache:
                clearCache();
                break;
            case R.id.rl_agreement_platform:
                WebViewActivity.startWebActivity(this, GlobalConstants.URL_AGREEMENT_PLATFORM, "平台使用规范");
                break;
            case R.id.rl_agreement_user:
                WebViewActivity.startWebActivity(this, GlobalConstants.URL_AGREEMENT_USER, "用户使用协议");
                break;
            case R.id.rl_privacy_user:
                WebViewActivity.startWebActivity(this, GlobalConstants.URL_AGREEMENT_USER_PRIVACY, "用户隐私政策");
                break;
            case R.id.tv_logout:
                showExitDialog();
                break;
        }
    }

    public static void startSettingActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    private void showExitDialog() {
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething(null, getString(R.string.sure_exit_account), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                stopService(new Intent(mContext, WindowShowService.class));
                logout();
                // 退出时清除设备锁密码，
                DeviceLockHelper.clearPassword();
                UserSp.getInstance(mContext).clearUserInfo();
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                coreManager.logout();
                LoginHelper.broadcastLogout(mContext);
                startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                finish();
            }
        });
        mSF.show();
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<String, String>();
        // 得到电话
        String phoneNumber = coreManager.getSelf().getTelephone();
        String digestTelephone = Md5Util.toMD5(phoneNumber);
        params.put("telephone", digestTelephone);
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 默认为86
        params.put("areaCode", String.valueOf(86));
        params.put("deviceKey", "android");

        HttpUtils.get().url(coreManager.getConfig().USER_LOGOUT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void queryAppSetting() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.post().url(coreManager.getConfig().RED_QUERY_APP_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<AppSettingBean>(AppSettingBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AppSettingBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (1 == result.getResultCode() && result.getData() != null) {
                            mAppSettingBean = result.getData();
                            mCbCall.setChecked(1 == mAppSettingBean.lianMaiFlag);
                            mCbNotify.setChecked(1 == mAppSettingBean.newsNotify);
                            mTvPhone.setText(mAppSettingBean.mobileNo + "(仅自己可看)");
                            PreferenceUtils.saveMobilePhone(mAppSettingBean.mobileNo);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void clearCache() {
        String filePath = MyApplication.getInstance().mAppDir;
        new ClearCacheAsyncTask(filePath).execute(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class ClearCacheAsyncTask extends AsyncTask<Boolean, String, Integer> {

        private File rootFile;
        private ProgressDialog progressDialog;

        private int filesNumber = 0;
        private boolean canceled = false;
        private long notifyTime = 0;

        public ClearCacheAsyncTask(String filePath) {
            this.rootFile = new File(filePath);
        }

        @Override
        protected void onPreExecute() {
            filesNumber = GetFileSizeUtil.getFolderSubFilesNumber(rootFile);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.deleteing));
            progressDialog.setMax(filesNumber);
            progressDialog.setProgress(0);
            // 设置取消按钮
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    canceled = true;
                }
            });
            progressDialog.show();
        }

        /**
         * 返回true代表删除完成，false表示取消了删除
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            if (filesNumber == 0) {
                return 0;
            }
            // 是否删除已清空的子文件夹
            boolean deleteSubFolder = params[0];
            return deleteFolder(rootFile, true, deleteSubFolder, 0);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // String filePath = values[0];
            int progress = Integer.parseInt(values[1]);
            // progressDialog.setMessage(filePath);
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (!canceled && result == filesNumber) {
                ToastUtil.showToast(mContext, R.string.clear_completed);
            }
            long cacheSize = GetFileSizeUtil.getFileSize(rootFile);
            mTvCache.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        }

        /**
         * 是否删除完毕
         *
         * @param file
         * @param deleteSubFolder
         * @return
         */
        private int deleteFolder(File file, boolean rootFolder, boolean deleteSubFolder, int progress) {
            if (file == null || !file.exists() || !file.isDirectory()) {
                return 0;
            }
            File flist[] = file.listFiles();
            for (File subFile : flist) {
                if (canceled) {
                    return progress;
                }
                if (subFile.isFile()) {
                    subFile.delete();
                    progress++;
                    long current = System.currentTimeMillis();
                    if (current - notifyTime > 200) {// 200毫秒更新一次界面
                        notifyTime = current;
                        publishProgress(subFile.getAbsolutePath(), String.valueOf(progress));
                    }
                } else {
                    progress = deleteFolder(subFile, false, deleteSubFolder, progress);
                    if (deleteSubFolder) {
                        subFile.delete();
                    }
                }
            }
            return progress;
        }
    }

    private void addCheckboxListener() {
        mCbCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAppSettingBean == null) {
                    return;
                }
                int openFlag = mAppSettingBean.lianMaiFlag == 1 ? 0 : 1;
                AppSettingRequest.getInstance().set(SettingActivity.this, "1", openFlag,
                        new AppSettingRequest.AppSettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mAppSettingBean.lianMaiFlag = openFlag;
                                mCbCall.setChecked(openFlag == 1);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });

        mCbNotify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mAppSettingBean == null) {
                    return;
                }
                int openFlag = mAppSettingBean.newsNotify == 1 ? 0 : 1;
                AppSettingRequest.getInstance().set(SettingActivity.this, "2", openFlag,
                        new AppSettingRequest.AppSettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mAppSettingBean.newsNotify = openFlag;
                                mCbNotify.setChecked(openFlag == 1);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });
    }

}
