package com.sk.weichat.ui.message;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.view.HeadView;
import com.sk.weichat.view.HorizontalListView;

import java.util.List;

/**
 * 转发多选
 */
public class InstantMessageConfirmNew extends PopupWindow {
    private Context mContext;
    private View mMenuView;
    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;
    private TextView mSend, mCancle;

    private List<Friend> friendList;

    public InstantMessageConfirmNew(Activity context, View.OnClickListener itemsOnClick, List<Friend> friend) {
        super(context);
        this.mContext = context;
        this.friendList = friend;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.message_instantconfirm_new, null);
        mHorAdapter = new HorListViewAdapter();

        mHorizontalListView = (HorizontalListView) mMenuView.findViewById(R.id.horizontal_list_view);
        mHorizontalListView.setAdapter(mHorAdapter);

        mSend = (TextView) mMenuView.findViewById(R.id.btn_send);
        mCancle = (TextView) mMenuView.findViewById(R.id.btn_cancle);
        mSend.setOnClickListener(itemsOnClick);
        mCancle.setOnClickListener(itemsOnClick);
        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int bottom = mMenuView.findViewById(R.id.pop_layout).getBottom();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    } else if (y > bottom) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    private class HorListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friendList.size();
        }

        @Override
        public Object getItem(int position) {
            return friendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new HeadView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            HeadView imageView = (HeadView) convertView;
            Friend selectPosition = friendList.get(position);
            AvatarHelper.getInstance().displayAvatar(CoreManager.requireSelf(mContext).getUserId(), selectPosition, imageView);
            return convertView;
        }
    }
}
