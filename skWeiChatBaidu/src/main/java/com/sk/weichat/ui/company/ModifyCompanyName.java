package com.sk.weichat.ui.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/**
 * 修改公司名称
 */
public class ModifyCompanyName extends BaseActivity implements View.OnClickListener {
    private EditText mCpyNemEdit;
    private String mCpyNemName;
    private String mCompanyId;
    private String mCompanyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motify_cpyname);
        Intent intent = getIntent();
        if (intent != null) {
            mCompanyId = intent.getStringExtra("companyId");
            mCompanyName = intent.getStringExtra("companyName");
        } else {
            // ...
            finish();
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.modify_company_name);
    }

    private void initView() {
        mCpyNemEdit = (EditText) findViewById(R.id.department_edit);
        mCpyNemEdit.setText(mCompanyName);
        findViewById(R.id.create_department_btn).setOnClickListener(this);
        ButtonColorChange.colorChange(this, findViewById(R.id.create_department_btn));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.create_department_btn:
                mCpyNemName = mCpyNemEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mCpyNemName)) {
                    // 公司名不能为空
                    Toast.makeText(this, R.string.company_name_connot_null, Toast.LENGTH_SHORT).show();
                } else if (mCpyNemName.equals(mCompanyName)) {
                    Toast.makeText(this, R.string.company_name_connot_same, Toast.LENGTH_SHORT).show();
                } else {
                    createDepartment(mCpyNemName, mCompanyId);
                }
                break;
        }
    }

    private void createDepartment(String cpyNewName, String companyId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("companyName", cpyNewName);

        HttpUtils.get().url(coreManager.getConfig().MODIFY_COMPANY_NAME)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(ModifyCompanyName.this, R.string.modify_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ModifyCompanyName.this);
                    }
                });
    }
}
