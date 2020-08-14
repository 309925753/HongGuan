package com.sk.weichat.ui.company;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.company.Identity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;


public class ChangeEmployeeDepartment extends BaseActivity {
    private ListView mListView;
    private ListView mPullToRefreshListView;
    private EmpDepAdapter mAdapter;

    // 该公司下所有部门(除原部门)
    private List<String> mExistId;
    private List<String> mExistName;
    private List<Identity> identities;
    private String mLoginUserId;
    private String mCompanyId;
    private String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_employee_department);
        if (getIntent() != null) {
            mCompanyId = getIntent().getStringExtra("companyId");
            mId = getIntent().getStringExtra("userId");
            String DepartmentIdList = getIntent().getStringExtra("departmentIdList");
            String DepartmentNameList = getIntent().getStringExtra("departmentNameList");
            mExistId = JSON.parseArray(DepartmentIdList, String.class);
            mExistName = JSON.parseArray(DepartmentNameList, String.class);
        }
        identities = new ArrayList<>();
        for (int i = 0; i < mExistId.size(); i++) {
            Identity identity = new Identity();
            identity.setId(mExistId.get(i));
            identity.setName(mExistName.get(i));
            identities.add(identity);
        }
        mLoginUserId = coreManager.getSelf().getUserId();

        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.change_departement);
    }

    private void initView() {
        mPullToRefreshListView = (ListView) findViewById(R.id.pull_refresh_list);

        mAdapter = new EmpDepAdapter(identities, this);
        mPullToRefreshListView.setAdapter(mAdapter);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Identity identity = (Identity) mAdapter.getItem(i);
                changeEmployeeDepartment(mId, identity.getId());
            }
        });
    }

    private void changeEmployeeDepartment(String userId, String newDepartmentId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", userId);
        params.put("companyId", mCompanyId);
        params.put("newDepartmentId", newDepartmentId);

        HttpUtils.get().url(coreManager.getConfig().MODIFY_EMPLOYEE_DEPARTMENT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(ChangeEmployeeDepartment.this, R.string.change_departement_succ, Toast.LENGTH_SHORT).show();
                            // 更换部门成功,跳转至公司管理页面
                            EventBus.getDefault().post(new MessageEvent("Update"));// 数据有更新
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(ChangeEmployeeDepartment.this);
                    }
                });
    }

    private class EmpDepAdapter extends BaseAdapter {

        private List<Identity> mIdentity;
        private Context mContext;

        public EmpDepAdapter(List<Identity> identity, Context context) {
            mIdentity = identity;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mIdentity.size();
        }

        @Override
        public Object getItem(int i) {
            return mIdentity.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.a_item_for_change_department, viewGroup, false);
            }
            TextView departmentTv = ViewHolder.get(view, R.id.department_tv);
            departmentTv.setText(mIdentity.get(i).getName());
            return view;
        }
    }
}
