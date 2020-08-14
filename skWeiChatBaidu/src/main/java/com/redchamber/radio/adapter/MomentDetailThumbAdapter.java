package com.redchamber.radio.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PageDataBean;
import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 个人中心 动态详情-点赞
 */
public class MomentDetailThumbAdapter extends BaseQuickAdapter<PageDataBean.LikesBean, MomentDetailThumbAdapter.ThumbViewHolder> {

    private int mWidth;
    private BtnOnClick btnOnClick;



    public interface BtnOnClick {
        void btnOnClick();
    }
    public void setBtnOnClice(BtnOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }


    public MomentDetailThumbAdapter(Context context, @Nullable  List<PageDataBean.LikesBean> data) {
        super(R.layout.red_item_rv_thumb_list, data);
        this.mWidth = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 108)) / 10;
    }

    @Override
    public int getItemCount() {
        if (getData().size() > 10) {
            return 10;
        }
        return super.getItemCount();
    }

    @Override
    protected void convert(@NonNull ThumbViewHolder helper, PageDataBean.LikesBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();

        AvatarHelper.getInstance().displayAvatar(String.valueOf(item.getUserId()), helper.mIvAvatar);
        lp.width = lp.height = mWidth;
        if (helper.getAdapterPosition() == 9) {
            helper.mTvRest.setVisibility(View.VISIBLE);
            helper.mRlRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnOnClick.btnOnClick();
                }
            });
        }
    }

    public static class ThumbViewHolder extends BaseViewHolder {

        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;
        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_rest)
        TextView mTvRest;

        public ThumbViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
