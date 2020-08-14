package com.sk.weichat.ui.me.select;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.SelectFriendItem;
import com.sk.weichat.bean.company.Department;
import com.sk.weichat.bean.company.StructBean;
import com.sk.weichat.bean.company.StructBeanNetInfo;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.view.MarqueeTextView;
import com.sk.weichat.view.SelectCpyPopupWindow;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/**
 * 我的同事
 */
public class SelectColleagueActivity extends BaseActivity {
    private static Context mContext;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private Set<SelectFriendItem> mSelectedList = new HashSet<>();
    private List<StructBeanNetInfo> mStructData;// 服务器返回的完整数据
    private List<StructBean> mStructCloneData;
    private List<Department> mDepartments;
    private List<String> userList;
    private List<String> forCurrentSonDepart;
    private List<String> forCurrenttwoSonDepart;
    private List<String> forCurrentthrSonDepart;
    private SelectCpyPopupWindow mSelectCpyPopupWindow;
    private String mLoginUserId;
    private String mCompanyCreater;// 公司创建者
    private String mCompanyId;     // 公司id
    private String rootDepartment;
    private Map<String, StructBean> structBeanMap = new HashMap<>();

    public static void start(Activity ctx, int requestCode, List<SelectFriendItem> mSelectedList) {
        mContext = ctx;
        Intent intent = new Intent(ctx, SelectColleagueActivity.class);
        if (mSelectedList != null && mSelectedList.size() > 0) {
            intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(mSelectedList));
        }
        ctx.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_colleague);
        mLoginUserId = coreManager.getSelf().getUserId();
        String sSelectedList = getIntent().getStringExtra("SELECTED_ITEMS");
        if (!TextUtils.isEmpty(sSelectedList)) {
            mSelectedList.addAll(JSON.parseArray(sSelectedList, SelectFriendItem.class));
        }
        initActionBar();
        initView();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_colleague));
        TextView tv_title_right = findViewById(R.id.tv_title_right);
        tv_title_right.setText(getString(R.string.finish));
        tv_title_right.setOnClickListener(v -> {
            result();
        });
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.companyRecycle);
        mAdapter = new MyAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        EventBus.getDefault().register(this);
    }

    private void initData() {
        mStructData = new ArrayList<>();
        mStructCloneData = new ArrayList<>();

        mDepartments = new ArrayList<>();
        userList = new ArrayList<>();
        forCurrentSonDepart = new ArrayList<>();
        forCurrenttwoSonDepart = new ArrayList<>();
        forCurrentthrSonDepart = new ArrayList<>();
        loadData();
    }

    private void result() {
        Set<SelectFriendItem> items = new HashSet<>();
        for (StructBean bean : mStructCloneData) {
            if (bean.isEmployee() && bean.getSelected()) {
                items.add(new SelectFriendItem(bean.getUserId(), bean.getText(), 0));
            }
        }
        Intent intent = new Intent();
        intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(items));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        if (message.message.equals("Update")) {// 更新
            initData();
        }
    }

    private void loadData() {
        // 根据userId查询所属公司
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().AUTOMATIC_SEARCH_COMPANY)
                .params(params)
                .build()
                .execute(new ListCallback<StructBeanNetInfo>(StructBeanNetInfo.class) {
                    @Override
                    public void onResponse(ArrayResult<StructBeanNetInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            // 数据已正确返回
                            mStructData = result.getData();
                            if (mStructData == null || mStructData.size() == 0) {
                                // 数据为null
                                Toast.makeText(SelectColleagueActivity.this, R.string.tip_no_data, Toast.LENGTH_SHORT).show();
                            } else {
                                // 设置数据
                                setData(mStructData);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(SelectColleagueActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setData(List<StructBeanNetInfo> data) {
        StructBean structBean;
        for (int i = 0; i < data.size(); i++) {
            structBean = new StructBean();
            // 公司为最高级，pId统一以1做为标识
            structBean.setParent_id("1");
            // 公司id
            mCompanyId = data.get(i).getId();
            structBean.setId(mCompanyId);
            mCompanyCreater = String.valueOf(data.get(i).getCreateUserId());
            structBean.setCreateUserId(mCompanyCreater);
            structBean.setCompanyId(mCompanyId);
            // 测试发现好像是ios端将android端拉入公司，android端打开同事崩溃
            // 日志显示为getRootDpartId返回的是null
            // 看服务端返回的数据rootDepartmentId可以在department内获取到
            // 同时rootDepartmentId好像是通过companyId+1生成的，先兼容一下吧
            if (data.get(i).getRootDpartId() != null && data.get(i).getRootDpartId().size() > 0) {
                rootDepartment = data.get(i).getRootDpartId().get(0);
            } else {
                rootDepartment = data.get(i).getDepartments().get(1).getParentId();
            }
            // 公司名称
            structBean.setText(data.get(i).getCompanyName());
            // 公司公告
            if (TextUtils.isEmpty(data.get(i).getNoticeContent())) {
                structBean.setNotificationDes(getString(R.string.no_notice));
            } else {
                structBean.setNotificationDes(data.get(i).getNoticeContent());
            }
            /*
            数据展示
             */
            // 是否展开
            structBean.setExpand(false);
            // 层级
            structBean.setIndex(0);
            structBean.setCompany(true);
            structBean.setDepartment(false);
            structBean.setEmployee(false);
            //
            mStructCloneData.add(structBean);
            boolean companySelected = true;
            StructBean companyStructBean = structBean;
            /**
             * 该公司下所属部门数据
             */
            List<StructBeanNetInfo.DepartmentsBean> dps = data.get(i).getDepartments();
            for (int j = 0; j < dps.size(); j++) {
                // 部门数据
                Department department = new Department();
                department.setDepartmentId(dps.get(j).getId());
                department.setDepartmentName(dps.get(j).getDepartName());
                department.setBelongToCompany(dps.get(j).getCompanyId());
                mDepartments.add(department);

                structBean = new StructBean();
                // 员工级别标识
                int employeeIndex = 2;
                // 是否为四级部门之下的部门
                boolean otherSon = false;
                // 标识:先为所有部门的parentid设置为公司id,根部门不显示
                if (!dps.get(j).getId().equals(rootDepartment)) {
                    structBean.setParent_id(dps.get(j).getCompanyId());
                }
                // 默认部门parentid为根部门id,后创建一级部门parentid为公司id
                if (rootDepartment.equals(dps.get(j).getParentId()) || mCompanyId.equals(dps.get(j).getParentId())) {
                    // 一级部门
                    forCurrentSonDepart.add(dps.get(j).getId());
                    structBean.setIndex(1);
                    // 该部门下员工下标为2
                    employeeIndex = 2;
                    otherSon = true;
                }
                /*
                在此进行二、三、四级部门的判断
                 */
                for (int k = 0; k < forCurrentSonDepart.size(); k++) {
                    // 遍历部门集合,如果某个部门的parentId等于一级部门的某个部门Id，说明此部门为二级目录
                    if (forCurrentSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        // 为三级部门准备数据
                        forCurrenttwoSonDepart.add(dps.get(j).getId());
                        // 重新设置该部门的parent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // 设置下标
                        structBean.setIndex(2);
                        employeeIndex = 3;
                        otherSon = true;
                    }
                }
                for (int k = 0; k < forCurrenttwoSonDepart.size(); k++) {
                    // 遍历部门集合,如果某个部门的parentId等于二级部门的某个部门Id，说明此部门为三级目录
                    if (forCurrenttwoSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        forCurrentthrSonDepart.add(dps.get(j).getId());
                        // 重新设置该部门的parent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // 设置下标
                        structBean.setIndex(3);
                        employeeIndex = 4;
                        otherSon = true;
                    }
                }
                for (int k = 0; k < forCurrentthrSonDepart.size(); k++) {
                    // 遍历部门集合,如果某个部门的parentId等于三级部门的某个部门Id，说明此部门为四级目录
                    if (forCurrentthrSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        // 重新设置该部门的parent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // 设置下标
                        structBean.setIndex(4);
                        employeeIndex = 5;
                        otherSon = true;
                    }
                }
                if (!otherSon) {
                    // 以上部门内不存在该部门，至少为五级部门，下标设置为5吧
                    // 重新设置该部门的parent_id
                    structBean.setParent_id(dps.get(j).getParentId());
                    // 设置下标
                    structBean.setIndex(5);
                    employeeIndex = 6;
                }
                // 部门id
                structBean.setId(dps.get(j).getId());
                // 公司id
                structBean.setCompanyId(dps.get(j).getCompanyId());

                // 公司创建者id，判断是否拥有操作权限
                structBean.setCreateUserId(mCompanyCreater);
                // 部门名称
                structBean.setText(dps.get(j).getDepartName());
                // 部门下所有员工userId,为了避免添加员工时出现重复
                List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> empList = dps.get(j).getEmployees();

                for (StructBeanNetInfo.DepartmentsBean.EmployeesBean employeesBean : empList) {
                    int userId = employeesBean.getUserId();
                    userList.add(String.valueOf(userId));
                }
                /*
                数据显示
                 */
                structBean.setExpand(false);
                /*
                数据操作
                 */
                structBean.setCompany(false);
                structBean.setDepartment(true);
                structBean.setEmployee(false);
                mStructCloneData.add(structBean);
                StructBean departmentStructBean = structBean;
                /**
                 * 该部门下所属员工数据
                 */
                List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> eps = dps.get(j).getEmployees();
                boolean departmentSelected = true;
                for (int z = 0; z < eps.size(); z++) {
                    structBean = new StructBean();
                    // 标识:部门id
                    structBean.setParent_id(eps.get(z).getDepartmentId());
                    // 员工id
                    structBean.setId(eps.get(z).getId());
                    // 部门id
                    structBean.setDepartmentId(eps.get(z).getDepartmentId());
                    // 公司id
                    structBean.setCompanyId(eps.get(z).getCompanyId());

                    // 公司创建者
                    structBean.setCreateUserId(mCompanyCreater);
                    structBean.setEmployeeToCompanyId(eps.get(z).getCompanyId());
                    // employee name/id/role
                    structBean.setText(eps.get(z).getNickname());
                    structBean.setUserId(String.valueOf(eps.get(z).getUserId()));
                    structBean.setIdentity(eps.get(z).getPosition());
                    structBean.setRole(eps.get(z).getRole());
                    // 所属根部门
                    structBean.setRootDepartmentId(rootDepartment);
                    structBean.setExpand(false);
                    if (employeeIndex == 2) {
                        structBean.setIndex(2);
                    } else if (employeeIndex == 3) {
                        structBean.setIndex(3);
                    } else if (employeeIndex == 4) {
                        structBean.setIndex(4);
                    } else if (employeeIndex == 5) {
                        structBean.setIndex(5);
                    } else {
                        structBean.setIndex(6);
                    }
                    structBean.setCompany(false);
                    structBean.setDepartment(false);
                    structBean.setEmployee(true);
                    if (mSelectedList.contains(new SelectFriendItem(structBean.getUserId(), structBean.getText(), 0))) {
                        structBean.setSelected(true);
                    } else {
                        departmentSelected = false;
                        companySelected = false;
                    }
                    mStructCloneData.add(structBean);
                }
                departmentStructBean.setSelected(departmentSelected);
            }
            companyStructBean.setSelected(companySelected);
        }
        // 为了自动勾选已经选中所有成员的部门和公司，
        // 但是没法直接获取一个部门的父部门和子部门，于是只能遍历，
        Map<String, StructBean> existsMemberMap = new HashMap<>();
        for (StructBean s : mStructCloneData) {
            structBeanMap.put(s.getId(), s);
        }
        for (StructBean s : mStructCloneData) {
            StructBean parent = structBeanMap.get(s.getParent_id());
            if (parent != null) {
                existsMemberMap.put(parent.getId(), parent);
            }
            // 子部门没选中将导致父部门没选中，
            if (!s.getSelected()) {
                while (parent != null) {
                    parent.setSelected(false);
                    parent = structBeanMap.get(parent.getParent_id());
                }
            }
        }
        for (StructBean s : mStructCloneData) {
            if (!s.isEmployee() && !existsMemberMap.containsKey(s.getId())) {
                // 没有成员的部门不要自动选中，
                s.setSelected(false);
            }
        }
        mAdapter.setData(mStructCloneData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    interface ItemClickListener {
        void onItemClick(int layoutPosition);

        void onItemSelectChange(int layoutPosition, boolean selected);
    }

    class MyAdapter extends RecyclerView.Adapter<StructHolder> {
        // 已经设置好的完整数据
        List<StructBean> mData;
        // 真正展示的数据
        List<StructBean> currData;
        LayoutInflater mInflater;
        Context mContext;
        ItemClickListener mListener;

        public MyAdapter(Context context) {
            mData = new ArrayList<>();
            currData = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
            this.mContext = context;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            StructBean bean = currData.get(position);
            return bean.getId().hashCode();
        }

        public void setOnItemClickListener(ItemClickListener listener) {
            mListener = listener;
        }

        public void setData(List<StructBean> data) {
            mData = data;
            currData.clear();
            for (int i = 0; i < mData.size(); i++) {
                StructBean info = mData.get(i);
                if (info.getParent_id() != null) {
                    if (info.getParent_id().equals("1")) {
                        // 默认展开第一个公司下面的部门
                        currData.add(info);
                        if (i == 0) {
                            info.setExpand(true);
                            openItemData(info.getId(), 0, info.getIndex());
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }

        /**
         * 展示公司(部门)布局或者成员布局
         */
        private void showView(boolean group, StructHolder holder) {
            if (group) {
                holder.rlGroup.setVisibility(View.VISIBLE);
                holder.rlPersonal.setVisibility(View.GONE);
            } else {
                holder.rlGroup.setVisibility(View.GONE);
                holder.rlPersonal.setVisibility(View.VISIBLE);
            }
        }

        /**
         * 展开item
         */
        private void openItemData(String id, int position, int index) {
            for (int i = mData.size() - 1; i > -1; i--) {
                StructBean data = mData.get(i);
                // 根据parent_id为currData添加数据
                if (id.equals(data.getParent_id())) {
                    data.setExpand(false);// 默认收起
                    data.setIndex(index + 1);
                    currData.add(position + 1, data);
                }
            }
            notifyDataSetChanged();
        }

        /**
         * ```````
         */
        private void closeItemData(String id, int position) {
            StructBean structBean = currData.get(position);
            if (structBean.isCompany()) {
                for (int i = currData.size() - 1; i >= 0; i--) {
                    StructBean data = currData.get(i);
                    if (data.getId().equals(structBean.getId()) || data.getCompanyId().equals(structBean.getId())) { // 公司 || 公司下的部门&员工
                        if (data.isCompany()) { // 公司
                            data.setExpand(false);
                        } else if (data.isDepartment()) { // 部门
                            data.setExpand(false);
                            data.setIndex(data.getIndex() - 1);
                            currData.remove(i);
                        } else if (data.isEmployee()) { // 员工
                            data.setIndex(data.getIndex() - 1);
                            currData.remove(i);
                        }
                    }
                }
            } else if (structBean.isDepartment()) {
                Map<String, String> expandMap = new HashMap<>();
                expandMap.put(structBean.getId(), structBean.getId());
                List<StructBean> structBeans = new ArrayList<>();
                for (int i = 0; i < currData.size(); i++) {
                    StructBean data = currData.get(i);
                    if (expandMap.containsKey(data.getId())
                            || expandMap.containsKey(data.getParent_id())
                            || (data.isEmployee() && expandMap.containsKey(data.getDepartmentId()))) {
                        if (data.getId().equals(structBean.getId())) {
                            data.setExpand(false);// 当前点击的部门
                        } else {
                            if (data.isDepartment()) {// 子部门还有人，需要将子部门的id也记录下
                                expandMap.put(data.getId(), data.getId());
                            }
                            structBeans.add(data);
                        }
                    }
                }
                currData.removeAll(structBeans);
            }
            notifyDataSetChanged();
        }

        @Override
        public StructHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.manager_company_item_select, parent, false);
            final View add = view.findViewById(R.id.iv_group_add);
            final View add2 = view.findViewById(R.id.iv_group_add2);
            StructHolder holder = new StructHolder(view, new ItemClickListener() {
                // item click
                @Override
                public void onItemClick(int layoutPosition) {
                    StructBean bean = currData.get(layoutPosition);
                    // company/department click
                    if (bean.isExpand()) {
                        bean.setExpand(false);
                        closeItemData(bean.getId(), layoutPosition);
                    } else {
                        bean.setExpand(true);
                        openItemData(bean.getId(), layoutPosition, bean.getIndex());
                    }
                    // employee click
                    if (bean.isEmployee()) {
                        showEmployeeInfo(add2, layoutPosition);
                    }
                }

                @Override
                public void onItemSelectChange(int layoutPosition, boolean selected) {
                    StructBean bean = currData.get(layoutPosition);
                    if (bean.getSelected() == selected) {
                        return;
                    }
                    bean.setSelected(selected);
                    updateChild(bean);
                    updateParent(bean);

                    notifyDataSetChanged();
                }

                // 选中公司或者部门就全选，
                // 顺序可能不准，所以没其他方法获得部门成员，只能遍历，
                private void updateChild(StructBean bean) {
                    if (bean == null) {
                        return;
                    }
                    if (bean.isEmployee()) {
                        return;
                    }
                    for (StructBean s : mData) {
                        if (!TextUtils.equals(s.getParent_id(), bean.getId())) {
                            continue;
                        }
                        boolean selected = bean.getSelected();
                        if (s.getSelected() == selected) {
                            continue;
                        }
                        s.setSelected(selected);
                        updateChild(s);
                    }
                }

                private void updateParent(StructBean bean) {
                    boolean selected = bean.getSelected();
                    StructBean parent = structBeanMap.get(bean.getParent_id());
                    if (parent == null) {
                        return;
                    }
                    if (parent.getSelected() == selected) {
                        return;
                    }
                    if (parent.getSelected() && !selected) {
                        parent.setSelected(false);
                        updateParent(parent);
                        return;
                    }
                    // 以下是父部门未选中情况下选中子部门，需要判断是否跟着选中父部门，
                    // 数据结构设计的是没办法直接拿到父部门和子部门，因此只能一次次遍历，
                    for (StructBean s : mData) {
                        if (TextUtils.equals(s.getParent_id(), parent.getId())) {
                            if (!s.getSelected()) {
                                return;
                            }
                        }
                    }
                    // 到这表示父部门所有其他子部门都是选中了的，所以父部门跟着选中，
                    parent.setSelected(true);
                    updateParent(parent);
                }

                private int type(StructBean o) {
                    return o.getIndex();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(StructHolder holder, int position) {
            StructBean bean = currData.get(position);
            showView(bean.isCompany() || bean.isDepartment(), holder);
            if (bean.isCompany() || bean.isDepartment()) {
                if (bean.isExpand()) {
                    holder.ivGroup.setImageResource(R.mipmap.ex);
                    holder.ivGroupAdd.setVisibility(View.INVISIBLE);
                } else {
                    holder.ivGroup.setImageResource(R.mipmap.ec);
                    holder.ivGroupAdd.setVisibility(View.INVISIBLE);
                }
                ViewGroup.LayoutParams lp = holder.ivGroup.getLayoutParams();
                if (bean.getIndex() > 0) {
                    lp.width = DisplayUtil.dip2px(mContext, 10);
                    lp.height = DisplayUtil.dip2px(mContext, 10);
                } else {
                    lp.width = DisplayUtil.dip2px(mContext, 14);
                    lp.height = DisplayUtil.dip2px(mContext, 14);
                }
                holder.ivGroup.setLayoutParams(lp);
                if (bean.isCompany()) {
                    // 显示公告
                    holder.tvNotificationDes.setText(bean.getNotificationDes());
                    holder.rlNotification.setVisibility(View.VISIBLE);
                    // 设置背景颜色
                    // holder.rlGroup.setBackgroundColor(getResources().getAccentColor(R.color.department_item));
                } else if (bean.isDepartment()) {
                    // 隐藏公告
                    holder.rlNotification.setVisibility(View.GONE);
                    // holder.rlGroup.setBackgroundColor(getResources().getAccentColor(R.color.person_item));
                }
                holder.tvGroupText.setText(bean.getText());
                // 根据下标设置padding
                holder.rlGroup.setPadding(DisplayUtil.dip2px(mContext, 14 + 9 * bean.getIndex()), 0, 0, 0);
                holder.cbSelect.setChecked(bean.getSelected());
            } else {
                // 成员
                AvatarHelper.getInstance().displayAvatar(bean.getText(), bean.getUserId(), holder.ivInco, true);
                holder.tvTextName.setText(bean.getText());
                holder.tvIdentity.setText(bean.getIdentity());
                holder.rlPersonal.setPadding(DisplayUtil.dip2px(mContext, 14 + 9 * bean.getIndex() + 12), 0, 0, 0);
                holder.cbSelect2.setChecked(bean.getSelected());
            }
        }

        @Override
        public int getItemCount() {
            return currData.size();
        }

        @SuppressLint("ResourceAsColor")
        private void showEmployeeInfo(final View asView, final int layoutPosition) {
        }

    }

    class StructHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // 公司/部门名称
        TextView tvGroupText;
        // 员工名称
        TextView tvTextName;
        // 员工身份
        TextView tvIdentity;
        // 上下
        ImageView ivGroup;
        // 添加...
        ImageView ivGroupAdd;
        // 头像
        ImageView ivInco;
        // 公告内容
        MarqueeTextView tvNotificationDes;
        // 公司/部门
        LinearLayout rlGroup;
        // 公告
        LinearLayout rlNotification;
        // 个人
        LinearLayout rlPersonal;
        ItemClickListener mListener;
        CheckBox cbSelect = itemView.findViewById(R.id.cbSelect);
        CheckBox cbSelect2 = itemView.findViewById(R.id.cbSelect2);

        public StructHolder(View itemView, ItemClickListener listener) {
            super(itemView);
            mListener = listener;
            tvGroupText = (TextView) itemView.findViewById(R.id.tv_group_name);
            tvTextName = (TextView) itemView.findViewById(R.id.tv_text_name);
            tvIdentity = (TextView) itemView.findViewById(R.id.tv_text_role);
            tvIdentity.setTextColor(SkinUtils.getSkin(mContext).getAccentColor());
            tvNotificationDes = itemView.findViewById(R.id.notification_des);
            tvNotificationDes.setTextColor(SkinUtils.getSkin(mContext).getAccentColor());
            ivGroup = (ImageView) itemView.findViewById(R.id.iv_arrow);
            ImageViewCompat.setImageTintList(ivGroup, ColorStateList.valueOf(SkinUtils.getSkin(itemView.getContext()).getAccentColor()));
            ivGroupAdd = (ImageView) itemView.findViewById(R.id.iv_group_add);
            ImageViewCompat.setImageTintList(ivGroupAdd, ColorStateList.valueOf(SkinUtils.getSkin(itemView.getContext()).getAccentColor()));
            ivInco = (ImageView) itemView.findViewById(R.id.iv_inco);
            rlGroup = itemView.findViewById(R.id.ll_group);
            rlNotification = (LinearLayout) itemView.findViewById(R.id.notification_ll);
            rlPersonal = (LinearLayout) itemView.findViewById(R.id.rl_personal);
            /**
             * 设置点击事件
             */
            rlGroup.setOnClickListener(this);
            rlPersonal.setOnClickListener(this);
            ivGroupAdd.setOnClickListener(this);
            tvNotificationDes.setOnClickListener(this);
            tvIdentity.setOnClickListener(this);

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> mListener.onItemSelectChange(getLayoutPosition(), isChecked));
            cbSelect2.setOnCheckedChangeListener((buttonView, isChecked) -> mListener.onItemSelectChange(getLayoutPosition(), isChecked));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                default:
                    mListener.onItemClick(getLayoutPosition());
                    break;
            }
        }
    }
}
