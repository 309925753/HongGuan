package com.redchamber.photo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.photo.adapter.RedAlbumAdapter;
import com.redchamber.view.SetPhotoCoinDialog;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 红包照片
 */
public class RedAlbumActivity extends BaseActivity {

    @BindView(R.id.rv)
    RecyclerView mRvAlbum;

    private RedAlbumAdapter mAdapter;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_red_album;
    }

    @Override
    protected void initView() {
        mAdapter = new RedAlbumAdapter(this, null);
        mRvAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        mRvAlbum.setAdapter(mAdapter);

        queryMyAlbum();
    }

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, RedAlbumActivity.class));
    }

    @OnClick({R.id.iv_back, R.id.tv_confirm})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_confirm:
                String urlList = "";
                for (PhotoBean photo : mAdapter.getData()) {
                    if (photo.isSelect) {
                        urlList += photo.photoId + ";";
                    }
                }
                if (TextUtils.isEmpty(urlList)) {
                    ToastUtils.showToast("请选择照片");
                    return;
                }
                urlList = urlList.substring(0, urlList.length() - 1);
                SetPhotoCoinDialog setPhotoCoinDialog = new SetPhotoCoinDialog(this, urlList);
                setPhotoCoinDialog.show();
                setPhotoCoinDialog.setSetPhotoListener(new SetPhotoCoinDialog.setPhotoListener() {
                    @Override
                    public void onSuccess() {
                        finish();
                    }
                });
                break;
        }
    }

    private void queryMyAlbum() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("pageNo", "1");
        params.put("pageSize", "50");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_QUERY_MY_ALBUM)
                .params(params)
                .build()
                .execute(new ListCallback<PhotoBean>(PhotoBean.class) {

                    @Override
                    public void onResponse(ArrayResult<PhotoBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mAdapter.setNewData(result.getData());
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(RedAlbumActivity.this);
                    }
                });
    }


}
