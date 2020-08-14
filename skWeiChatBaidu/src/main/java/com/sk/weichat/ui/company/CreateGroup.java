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
 * 创建子部门
 */

public class CreateGroup extends BaseActivity implements View.OnClickListener {

    private EditText mDepartmentEdit;
    private String mDepartmentName;
    private String mCompanyId;
    private String mParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Intent intent = getIntent();
        if (intent != null) {
            mCompanyId = intent.getStringExtra("companyId");
            mParentId = intent.getStringExtra("parentId");
        } else {
            finish();
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.create_son_department);
    }

    private void initView() {
        mDepartmentEdit = (EditText) findViewById(R.id.department_edit);
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
                mDepartmentName = mDepartmentEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mDepartmentName)) {
                    // 部门名不能为空
                    Toast.makeText(this, R.string.name_connot_null, Toast.LENGTH_SHORT).show();
                } else {
                    createDepartment(mDepartmentName, mCompanyId, mParentId);
                }
                break;
        }
    }

    private void createDepartment(String departmentName, String companyId, String parentId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("departName", departmentName);
        params.put("createUserId", coreManager.getSelf().getUserId());
        params.put("parentId", parentId);

        HttpUtils.get().url(coreManager.getConfig().CREATE_DEPARTMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(CreateGroup.this, R.string.create_son_department_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(CreateGroup.this);
                    }
                });
    }
}
