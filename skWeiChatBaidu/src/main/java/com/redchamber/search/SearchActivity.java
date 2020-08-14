package com.redchamber.search;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.RedIndexUser;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.search.adapter.SearchListAdapter;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
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
 * 搜索
 */
public class SearchActivity extends BaseActivity {

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.iv_clear)
    ImageView mIvClear;
    @BindView(R.id.rv)
    RecyclerView mRv;

    private SearchListAdapter mAdapter;

    @Override
    protected int setLayout() {
        return R.layout.activity_search;
    }

    @Override
    protected void initView() {
        initSearchView();
        mAdapter = new SearchListAdapter(null);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(mAdapter);
    }

    @OnClick({R.id.iv_back, R.id.iv_clear})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_clear:
                mEtSearch.setText("");
                mIvClear.setVisibility(View.GONE);
                break;
        }
    }

    private void initSearchView() {
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_SEARCH == actionId) {
                    String nickName = mEtSearch.getText().toString().trim();
                    getSearchUser(nickName);
                }
                return false;
            }
        });
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strSearch = mEtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(strSearch)) {
                    mIvClear.setVisibility(View.GONE);
                } else {
                    mIvClear.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getSearchUser(String nickname) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("nickname", nickname);
        params.put("pageIndex", "1");
        params.put("pageSize", "20");

        HttpUtils.post().url(coreManager.getConfig().RED_INDEX_SEARCH)
                .params(params)
                .build()
                .execute(new ListCallback<RedIndexUser>(RedIndexUser.class) {
                    @Override
                    public void onResponse(ArrayResult<RedIndexUser> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            mAdapter.setNewData(result.getData());
                        } else {

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SearchActivity.this);
                    }
                });
    }

}
