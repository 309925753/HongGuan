package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Contact;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class InviteDialog extends Dialog {

    private OnInviteListItemClickListener mOnInviteListItemClickListener;
    private Context mContext;
    private List<Contact> mContactList = new ArrayList<>();

    private ListView mInviteListView;
    private InviteAdapter mInviteAdapter;

    public InviteDialog(Context context, List<Contact> contactList, OnInviteListItemClickListener onInviteListItemClickListener) {
        super(context, R.style.BottomDialog);
        this.mContext = context;
        this.mContactList = contactList;
        this.mOnInviteListItemClickListener = onInviteListItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mInviteListView = (ListView) findViewById(R.id.invite_list);
        mInviteAdapter = new InviteAdapter(mContext, mContactList);
        mInviteListView.setAdapter(mInviteAdapter);

        mInviteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = mContactList.get(position);
                if (mOnInviteListItemClickListener != null && contact != null) {
                    dismiss();
                    mOnInviteListItemClickListener.onInviteItemClick(contact);
                }
            }
        });

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.7);
        lp.height = (int) (ScreenUtil.getScreenHeight(getContext()) * 0.7);
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.CENTER);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    public interface OnInviteListItemClickListener {
        void onInviteItemClick(Contact contact);
    }

    class InviteAdapter extends CommonAdapter<Contact> {

        public InviteAdapter(Context context, List<Contact> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_contacts, position);
            TextView mTitleTv = viewHolder.getView(R.id.catagory_title);
            CheckBox mCheckBox = viewHolder.getView(R.id.check_box);
            TextView mNameTv = viewHolder.getView(R.id.user_name_tv);
            mTitleTv.setVisibility(View.GONE);
            Contact contact = data.get(position);
            if (contact != null) {
                mNameTv.setText(contact.getToUserName());
            }
            return viewHolder.getConvertView();
        }
    }
}
