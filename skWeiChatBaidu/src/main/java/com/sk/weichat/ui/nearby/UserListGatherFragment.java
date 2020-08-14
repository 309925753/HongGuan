package com.sk.weichat.ui.nearby;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.User;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseGridFragment;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.view.CircleImageView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 搜索...
 */
public class UserListGatherFragment extends BaseGridFragment<UserListGatherFragment.UserListGatherHolder> {
    double latitude;
    double longitude;
    private List<User> mUsers = new ArrayList<>();

    private boolean isPullDwonToRefersh;
    private int mPageIndex = 0;

    private String mKeyWord;// 关键字(keyword)
    private int mSex;                 // 城市Id(cityId)
    private int mMinAge;         // 行业Id(industryId)
    private int mMaxAge;         // 职能Id(fnId)
    private int mShowTime;     // 日期(days)

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            isPullDwonToRefersh = true;
        } else {
            isPullDwonToRefersh = false;
        }

        latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();

        mKeyWord = getActivity().getIntent().getStringExtra("key_word");
        mSex = getActivity().getIntent().getIntExtra("sex", 0);
        mMinAge = getActivity().getIntent().getIntExtra("min_age", 0);
        mMaxAge = getActivity().getIntent().getIntExtra("max_age", 200);
        mShowTime = getActivity().getIntent().getIntExtra("show_time", 0);
        requestData(isPullDwonToRefersh);
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(mPageIndex));
        // params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("pageSize", "20");
        if (!TextUtils.isEmpty(mKeyWord)) {
            params.put("nickname", mKeyWord);
        }
        if (mSex != 0) {
            params.put("sex", String.valueOf(mSex));
        }

        if (mMinAge != 0) {
            params.put("minAge", String.valueOf(mMinAge));
        }

        if (mMaxAge != 0) {
            params.put("maxAge", String.valueOf(mMaxAge));
        }

        params.put("active", String.valueOf(mShowTime));

        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        HttpUtils.get().url(coreManager.getConfig().USER_NEAR)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {
                        DialogHelper.dismissProgressDialog();
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mUsers.clear();

                        }
                        List<User> datas = result.getData();
                        if (datas != null && datas.size() > 0) {
                            mUsers.addAll(datas);

                        }
                        update(mUsers);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public UserListGatherHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_nearby_grid, parent, false);
        return new UserListGatherHolder(v);
    }

    @Override
    public void fillData(UserListGatherHolder holder, int position) {
        if (mUsers != null && mUsers.size() > 0) {
            User user = mUsers.get(position);
            String name;
            if (user.getFriends() != null && !TextUtils.isEmpty(user.getFriends().getRemarkName())) {
                // 此接口服务端不返回friends，判断还是先放这在这里吧
                name = user.getFriends().getRemarkName();
            } else {
                name = user.getNickName();
            }
            AvatarHelper.getInstance().displayRoundAvatar(name, user.getUserId(), holder.ivBgImg, false);
            AvatarHelper.getInstance().displayAvatar(name, user.getUserId(), holder.ivHead, true);
            holder.tvName.setText(name);
            String distance = DisplayUtil.getDistance(latitude, longitude, user);
            holder.tvDistance.setText(distance);
            holder.tvTime.setText(TimeUtils.skNearbyTimeString(user.getCreateTime()));
        }
    }

    public void onItemClick(int position) {
        User user = mUsers.get(position);
        String userId = user.getUserId();
        int fromAddType;
        if (user.getNickName().contains(mKeyWord)) {
            fromAddType = BasicInfoActivity.FROM_ADD_TYPE_NAME;
        } else {
            // 昵称不包含关键字的话就是通过手机号搜索出来的，
            fromAddType = BasicInfoActivity.FROM_ADD_TYPE_PHONE;
        }
        BasicInfoActivity.start(requireActivity(), userId, fromAddType);
    }

    class UserListGatherHolder extends RecyclerView.ViewHolder {
        LinearLayout rootView;
        RoundedImageView ivBgImg;
        TextView tvName;
        CircleImageView ivHead;
        TextView tvDistance;
        TextView tvTime;

        UserListGatherHolder(View itemView) {
            super(itemView);
            rootView = (LinearLayout) itemView.findViewById(R.id.ll_nearby_grid_root);
            ivBgImg = (RoundedImageView) itemView.findViewById(R.id.iv_nearby_img);
            ivBgImg.setCornerRadius(ScreenUtil.dip2px(requireContext(), 7), ScreenUtil.dip2px(requireContext(), 7), 0, 0);
            tvName = (TextView) itemView.findViewById(R.id.tv_nearby_name);
            ivHead = (CircleImageView) itemView.findViewById(R.id.iv_nearby_head);
            tvDistance = (TextView) itemView.findViewById(R.id.tv_nearby_distance);
            tvTime = (TextView) itemView.findViewById(R.id.tv_nearby_time);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(getLayoutPosition());
                }
            });
        }
    }
}
