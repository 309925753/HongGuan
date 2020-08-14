package com.sk.weichat.ui.mucfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.log.FileUtils;
import com.sk.weichat.view.LoadingDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * TODO: 适配刘海屏，
 */
public class AddMucFileActivity extends BaseActivity {
    private static final int REQUEST_CODE_SELECT_FILE = 7;
    int index = 0;
    LoadingDialog loading;
    private String mRoomId;
    private List<MucSelectFileDialog.UpFileBean> beans;
    private MucSelectFileDialog dialog;
    // 复用toast, 上传成功通知可能快速更新，
    private Toast mToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        getSupportActionBar().hide();
        mRoomId = getIntent().getStringExtra("roomId");

        dialog = new MucSelectFileDialog(this, new MucSelectFileDialog.OptionFileBeanListener() {
            @Override
            public void option(List<MucSelectFileDialog.UpFileBean> beans) {
                AddMucFileActivity.this.beans = beans;
                loading = new LoadingDialog(AddMucFileActivity.this);
                if (beans.size() > 0) {
                    loading.show();
                    index = 0;
                    for (MucSelectFileDialog.UpFileBean bean : beans) {

                        UploadingHelper.upfile(
                                coreManager.getSelfStatus().accessToken,
                                coreManager.getSelf().getUserId(),
                                bean.file,
                                new UploadingHelper.OnUpFileListener() {

                                    @Override
                                    public void onSuccess(String url, String path) {
                                        addFile(bean, url);
                                    }

                                    @Override
                                    public void onFailure(String err, String path) {
                                        toast(err);
                                        // 不论成功失败，全走一遍就退出，
                                        ++index;
                                        ok();
                                    }
                                });
                    }
                }
            }

            @Override
            public void intent() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }
        });
        dialog.setOnDismissListener(dialog1 -> {
            finish();
        });
        dialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!dialog.isShowing()) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK
                || data == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SELECT_FILE: {
                String file_path = FileUtils.getPath(AddMucFileActivity.this, data.getData());
                Log.e("xuan", "conversionFile: " + file_path);
                if (file_path == null) {
                    ToastUtil.showToast(mContext, R.string.tip_file_not_supported);
                } else {
                    MucSelectFileDialog.UpFileBean upFileBean = new MucSelectFileDialog.UpFileBean();
                    upFileBean.file = new File(file_path);
                    // type没用，而且文件管理器选择的文件可能无法判断类型，
                    upFileBean.type = -1;
                    AddMucFileActivity.this.beans = Collections.singletonList(upFileBean);
                    loading = new LoadingDialog(AddMucFileActivity.this);
                    if (beans.size() > 0) {
                        loading.show();
                        index = 0;
                        for (MucSelectFileDialog.UpFileBean bean : beans) {

                            UploadingHelper.upfile(
                                    coreManager.getSelfStatus().accessToken,
                                    coreManager.getSelf().getUserId(),
                                    bean.file,
                                    new UploadingHelper.OnUpFileListener() {

                                        @Override
                                        public void onSuccess(String url, String path) {
                                            addFile(bean, url);
                                        }

                                        @Override
                                        public void onFailure(String err, String path) {
                                            toast(err);
                                            // 不论成功失败，全走一遍就退出，
                                            ++index;
                                            ok();
                                        }
                                    });
                        }
                    }
                }

                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addFile(final MucSelectFileDialog.UpFileBean bean, final String url) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("size", bean.file.length() + "");
        params.put("url", url);
        params.put("type", bean.type + "");
        params.put("name", bean.file.getName());

        HttpUtils.get().url(coreManager.getConfig().UPLOAD_MUC_FILE_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result == null) {
                            toast(getString(R.string.data_exception));
                            return;
                        }
                        toast(getString(R.string.number) + " " + (index + 1) + "/" + beans.size() + " " +
                                getString(R.string.individual) + getString(R.string.upload_successful));
                        // 不论成功失败，全走一遍就退出，
                        ++index;
                        ok();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        toast(getString(R.string.net_exception));
                        // 不论成功失败，全走一遍就退出，
                        ++index;
                        ok();
                    }
                });
    }

    public void ok() {
        if (index == beans.size()) {
            loading.dismiss();
            Intent intent = new Intent();
            intent.putExtra("code", 200);
            setResult(10010, intent);
            dialog.dismiss();
            finish();
        }
    }

    @SuppressLint("ShowToast")
    private void toast(String str) {
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }

        mToast.setText(str);
        mToast.show();
    }
}
