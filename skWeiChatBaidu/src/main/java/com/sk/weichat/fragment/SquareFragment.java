package com.sk.weichat.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.ConfigBean;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.event.MessageEventHongdian;
import com.sk.weichat.call.CallConstants;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.MyZanDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.groupchat.SelectContactsActivity;
import com.sk.weichat.ui.life.LifeCircleActivity;
import com.sk.weichat.ui.live.LiveActivity;
import com.sk.weichat.ui.me.NearPersonActivity;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.ui.trill.TrillActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.UiUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class SquareFragment extends EasyFragment {
    private SquareActionAdapter adapter;
    private List<Item> data;
    private PublicAdapter publicAdapter;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_square;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        ((TextView) (findViewById(R.id.tv_title_center))).setText(getString(R.string.find));
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        ImageView ivRight = findViewById(R.id.iv_title_right);
        ivRight.setImageResource(R.drawable.messaeg_scnning);
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setOnClickListener(v -> {
            MainActivity.requestQrCodeScan(getActivity());
        });
        RecyclerView rvAction = findViewById(R.id.rvAction);
        rvAction.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        data = getData();
        adapter = new SquareActionAdapter();
        rvAction.setAdapter(adapter);
        // 避免刷新时的闪烁，
        rvAction.setItemAnimator(null);
        EventBus.getDefault().register(this);

        ImageView head = findViewById(R.id.head);
        if (!TextUtils.isEmpty(coreManager.getConfig().headBackgroundImg)) {
            ImageLoadHelper.showImage(requireContext(), coreManager.getConfig().headBackgroundImg, head);
        } else {
            head.setVisibility(View.GONE);
        }

        AsyncUtils.doAsync(this, throwable -> {
            Reporter.post("获取生活圈新消息数量失败，", throwable);
            Activity ctx = getActivity();
            if (ctx != null) {
                ctx.runOnUiThread(() -> ToastUtil.showToast(requireContext(), R.string.tip_get_life_circle_number_failed));
            }
        }, squareFragmentAsyncContext -> {
            final int lifeCircleNumber = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
            squareFragmentAsyncContext.uiThread(squareFragment -> squareFragment.updateLifeCircleNumber(lifeCircleNumber));
        });

        if (!coreManager.getConfig().enableMpModule) {
            findViewById(R.id.llHotNumber).setVisibility(View.GONE);
        } else {
            RecyclerView rvPublicNumber = findViewById(R.id.rvPublicNumber);
            rvPublicNumber.setHasFixedSize(true);
            rvPublicNumber.setNestedScrollingEnabled(false);
            LinearLayoutManager lm = new LinearLayoutManager(requireContext());
            rvPublicNumber.setLayoutManager(lm);
            publicAdapter = new PublicAdapter();
            rvPublicNumber.setAdapter(publicAdapter);
            requestServiceNumber();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // 更新发现模块新消息数量
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventHongdian message) {
        if (message.number == -1) {
            // 好友更新了动态
            int size = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
            if (size != 0) {
                // 本地社交圈有未读数量，不更新红点
                return;
            }
        }
        updateLifeCircleNumber(message.number);
    }

    private void updateLifeCircleNumber(int number) {
        for (int i = 0; i < data.size(); i++) {
            Item item = data.get(i);
            if (item.textRes == R.string.life_circle) {
                item.number = number;
                adapter.notifyItemChanged(i);
                return;
            }
        }
    }

    private List<Item> getData() {
        List<Item> ret = new LinkedList<>();
        // lifeCircle  生活圈，  videoMeeting 视频会议，  liveVideo 视频直播，  shortVideo 短视频， peopleNearby 附近的人
        ConfigBean.PopularApp popularAPP = coreManager.getConfig().popularAPP;
        if (popularAPP.lifeCircle > 0) {
            ret.add(new Item(R.string.life_circle, R.mipmap.square_item_life, toStartActivity(LifeCircleActivity.class)));
        }
        if (popularAPP.videoMeeting > 0) {
            ret.add(new Item(R.string.chat_video_conference, R.mipmap.square_item_video_meeting, () -> SelectContactsActivity.startQuicklyInitiateMeeting(requireContext(), CallConstants.Video_Meet)));
        }
        if (popularAPP.videoMeeting > 0) {
            ret.add(new Item(R.string.meeting, R.mipmap.square_item_voice_meeting, () -> SelectContactsActivity.startQuicklyInitiateMeeting(requireContext(), CallConstants.Audio_Meet)));
        }
        if (popularAPP.liveVideo > 0) {
            ret.add(new Item(R.string.live_chat, R.mipmap.square_item_live_chat, toStartActivity(LiveActivity.class)));
        }
        if (popularAPP.shortVideo > 0) {
            ret.add(new Item(R.string.douyin, R.mipmap.square_item_douyin, toStartActivity(TrillActivity.class)));
        }
        if (popularAPP.peopleNearby > 0) {
            ret.add(new Item(R.string.near_person, R.mipmap.square_item_nearby, toStartActivity(NearPersonActivity.class)));
        }
        return new ArrayList<>(ret);
    }

    private Runnable toStartActivity(final Class<? extends Activity> clazz) {
        return () -> {
            Intent intent = new Intent(requireContext(), clazz);
            startActivity(intent);
        };
    }

    @SuppressWarnings("unused")
    private Runnable toToast() {
        return () -> ToastUtil.showToast(requireContext(), R.string.tip_coming_soon);
    }

    private void requestServiceNumber() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        DialogHelper.showDefaulteMessageProgressDialogAddCancel(requireActivity(), null);

        HttpUtils.get().url(coreManager.getConfig().PUBLIC_SEARCH)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getContext(), result)) {
                            List<User> list = result.getData();
                            publicAdapter.setData(list);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(getContext());
                    }
                });
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final View llRoot;
        private final TextView tvActionName;
        private final ImageView ivActionImage;
        private final TextView tvNumber;

        public ViewHolder(View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            tvActionName = itemView.findViewById(R.id.tvActionName);
            ivActionImage = itemView.findViewById(R.id.ivActionImage);
            tvNumber = itemView.findViewById(R.id.tvNumber);

            ImageViewCompat.setImageTintMode(ivActionImage, PorterDuff.Mode.MULTIPLY);
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_pressed},
                    new int[]{},
            };

            int[] colors = new int[]{
                    0xffe5e5e5,
                    0xffffffff,
            };

            ColorStateList colorStateList = new ColorStateList(states, colors);
            ImageViewCompat.setImageTintList(ivActionImage, colorStateList);
        }
    }

    private static class Item {
        private final int textRes;
        private final Runnable onClickCallback;
        private int imageRes;
        // 小红点提示的数量，
        // 0就不显示小红点，
        private int number = 0;

        Item(@StringRes int textRes, @DrawableRes int imageRes, Runnable onClickCallback) {
            this(textRes, imageRes, onClickCallback, 0);
        }

        Item(@StringRes int textRes, @DrawableRes int imageRes, Runnable onClickCallback, int number) {
            this.textRes = textRes;
            this.imageRes = imageRes;
            this.onClickCallback = onClickCallback;
            this.number = number;
        }
    }

    private static class PublicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHead = itemView.findViewById(R.id.notice_iv);
        TextView tvName = itemView.findViewById(R.id.notice_tv);

        PublicViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class SquareActionAdapter extends RecyclerView.Adapter<ViewHolder> {
        /**
         * 动态改变itemView的高度，
         * 最小是itemView中自动布局llRoot的高度加30dp,
         * 尽量一页三行，也就是parent高度三分之一，
         */
        private void resetLayoutSize(
                View itemView,
                View parent,
                View llRoot
        ) {
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = Math.max(
                    llRoot.getWidth() + DisplayUtil.dip2px(requireContext(), 8),
                    parent.getWidth() / 5
            );
            itemView.setLayoutParams(layoutParams);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.item_square_action, parent, false);
            final ViewHolder vh = new ViewHolder(itemView);
            int width = ScreenUtil.getScreenWidth(parent.getContext());
            ViewGroup.LayoutParams layoutParams = vh.itemView.getLayoutParams();
            layoutParams.width = width / 5;
            vh.itemView.setLayoutParams(layoutParams);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Item item = data.get(position);
            holder.itemView.setOnClickListener(v -> {
                if (UiUtils.isNormalClick(v)) {
                    item.onClickCallback.run();
                }
            });
            holder.ivActionImage.setImageResource(item.imageRes);
            holder.tvActionName.setText(item.textRes);
            UiUtils.updateNum(holder.tvNumber, item.number);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class PublicAdapter extends RecyclerView.Adapter<PublicViewHolder> {
        private List<User> data = Collections.emptyList();

        public void setData(List<User> data) {
            this.data = new ArrayList<>(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PublicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new PublicViewHolder(getLayoutInflater().inflate(R.layout.item_square_public_number, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PublicViewHolder vh, int i) {
            User item = data.get(i);
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), item.getUserId());
            if (friend != null) {
                vh.tvName.setText(TextUtils.isEmpty(friend.getRemarkName()) ? item.getNickName() : friend.getRemarkName());
            } else {
                vh.tvName.setText(item.getNickName());
            }
            AvatarHelper.getInstance().displayAvatar(item.getNickName(), item.getUserId(), vh.ivHead, true);

            vh.itemView.setOnClickListener(v -> {
                Friend friend2 = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), item.getUserId());
                if (friend2 != null && (friend2.getStatus() == Friend.STATUS_FRIEND || friend2.getStatus() == Friend.STATUS_SYSTEM)) {
                    ChatActivity.start(requireContext(), friend2);
                } else {
                    BasicInfoActivity.start(requireContext(), item.getUserId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
