package com.sk.weichat.ui.contacts.label;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseLabelGridFragment;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.UiUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * A simple {@link Fragment} subclass.
 */
public class LabelFragment extends BaseLabelGridFragment<LabelFragment.LabelHolder> {
    private List<Label> mLabelList;
    private Map<String, String> map = new HashMap<>();

    @Override
    public void initDatas(int pager) {
        refreshLabelListFromService();
    }

    @Override
    public LabelHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_label_grid, parent, false);
        return new LabelHolder(v);
    }

    @Override
    public LabelHolder initOtherHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_other_label_grid, parent, false);
        return new LabelHolder(v);
    }

    @Override
    public void fillData(LabelHolder holder, int position) {
        final Label label = mLabelList.get(position);
        if (label != null) {
            List<String> userIds = JSON.parseArray(label.getUserIdList(), String.class);
            if (userIds != null) {
                holder.tv_label.setText(label.getGroupName() + "(" + userIds.size() + ")");
            } else {
                holder.tv_label.setText(label.getGroupName() + "(0)");
            }
        }
        ViewCompat.setBackgroundTintList(holder.tv_label, ColorStateList.valueOf(SkinUtils.getSkin(requireActivity()).getAccentColor()));
        if (label == null) {
            ViewCompat.setBackgroundTintList(holder.iv_label, ColorStateList.valueOf(SkinUtils.getSkin(requireActivity()).getAccentColor()));
            holder.iv_label.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), CreateLabelActivity.class);
                intent.putExtra("isEditLabel", false);
                getActivity().startActivityForResult(intent, 0x01);
            });
        } else {
            holder.iv_delete_label.setVisibility(map.containsKey(label.getGroupId()) ? View.VISIBLE : View.GONE);
            holder.tv_label.setOnClickListener(v -> {
                onItemClick(v, position);
                holder.iv_delete_label.setVisibility(holder.iv_delete_label.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
            });
            holder.tv_label.setOnLongClickListener(v -> {
                if (map.containsKey(label.getGroupId())) {
                    map.remove(label.getGroupId());
                } else {
                    map.put(label.getGroupId(), label.getGroupId());
                }
                update();
                return true;
            });
            holder.iv_delete_label.setOnClickListener(v -> {
                onIvClick(position);
                holder.iv_delete_label.setVisibility(holder.iv_delete_label.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
            });
        }
    }

    @Override
    public void fillOtherData(LabelHolder holder, int position) {
        ViewCompat.setBackgroundTintList(holder.iv_label, ColorStateList.valueOf(SkinUtils.getSkin(requireActivity()).getAccentColor()));
        holder.iv_label.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CreateLabelActivity.class);
            intent.putExtra("isEditLabel", false);
            startActivityForResult(intent, 0x01);
        });
    }

    public void onItemClick(View view, int position) {
        if (UiUtils.isNormalClick(view)) {// 防止过快点击
            Label label = mLabelList.get(position);
            if (label != null) {
                Intent intent = new Intent(requireActivity(), CreateLabelActivity.class);
                intent.putExtra("isEditLabel", true);
                intent.putExtra("labelId", label.getGroupId());
                startActivityForResult(intent, 0x01);
            }
        }
    }

    public void onIvClick(int position) {
        final Label label = mLabelList.get(position);
        deleteLabel(label);
    }

    private void deleteLabel(final Label label) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        DialogHelper.showDefaulteMessageProgressDialog(requireActivity());

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().deleteLabel(coreManager.getSelf().getUserId(), label.getGroupId());
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    // 从服务端下载标签
    private void refreshLabelListFromService() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ArrayResult<Label> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Label> labelList = result.getData();
                            LabelDao.getInstance().refreshLabel(coreManager.getSelf().getUserId(), labelList);
                            update(labelList);
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    // 从数据库加载标签
    private void loadData() {
        if (mLabelList != null) {
            mLabelList.clear();
        }
        mLabelList = LabelDao.getInstance().getAllLabels(coreManager.getSelf().getUserId());
        update(mLabelList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01) {
            loadData();
        }
    }

    class LabelHolder extends RecyclerView.ViewHolder {
        TextView tv_label;
        ImageView iv_label;
        ImageView iv_delete_label;

        LabelHolder(@NonNull View itemView) {
            super(itemView);
            iv_delete_label = itemView.findViewById(R.id.iv_delete_label);
            tv_label = itemView.findViewById(R.id.tv_label);
            iv_label = itemView.findViewById(R.id.iv_label);
        }
    }
}
