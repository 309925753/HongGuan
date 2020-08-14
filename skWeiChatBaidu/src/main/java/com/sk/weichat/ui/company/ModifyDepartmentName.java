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
 * 修改部门名称
 */
public class ModifyDepartmentName extends BaseActivity implements View.OnClickListener {
    private EditText mCpyNemEdit;
    private String mDptNewName;
    private String mDepartmentId;
    private String mDepartmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motify_dptname);
        Intent intent = getIntent();
        if (intent != null) {
            mDepartmentId = intent.getStringExtra("departmentId");
            mDepartmentName = intent.getStringExtra("departmentName");
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
        tvTitle.setText(R.string.modify_department_name);
    }

    private void initView() {
        mCpyNemEdit = (EditText) findViewById(R.id.department_edit);
        mCpyNemEdit.setText(mDepartmentName);
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
                mDptNewName = mCpyNemEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mDptNewName)) {
                    // 部门名不能为空
                    Toast.makeText(this, R.string.department_name_connot_null, Toast.LENGTH_SHORT).show();
                } else if (mDptNewName.equals(mDepartmentName)) {
                    Toast.makeText(this, R.string.department_name_connot_same, Toast.LENGTH_SHORT).show();
                } else {
                    createDepartment(mDptNewName, mDepartmentId);
                }
                break;
        }
    }

    private void createDepartment(String departmentName, String departmentId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("dpartmentName", departmentName);
        params.put("departmentId", departmentId);

        HttpUtils.get().url(coreManager.getConfig().MODIFY_DEPARTMENT_NAME)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(ModifyDepartmentName.this, R.string.modify_succ, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ModifyDepartmentName.this);
                    }
                });
    }
}
