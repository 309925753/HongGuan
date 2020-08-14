package com.sk.weichat.ui.me.sendgroupmessage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sk.weichat.R;

/**
 * Created by Administrator on 2016/9/8.
 */
public class ChatToolsForSendGroup extends RelativeLayout {

    TextView photoTv, cameraTv, talkTv, positiTv, cardTv, fileTv;

    public ChatToolsForSendGroup(Context context) {
        super(context);
        init(context);
    }

    public ChatToolsForSendGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatToolsForSendGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context mContext) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(R.layout.chat_tools_view_for_sg, this);

        photoTv = (TextView) findViewById(R.id.im_photo_tv);
        cameraTv = (TextView) findViewById(R.id.im_camera_tv);
        talkTv = (TextView) findViewById(R.id.im_video_tv);
        positiTv = (TextView) findViewById(R.id.im_loc_tv);
        cardTv = (TextView) findViewById(R.id.im_card_tv);
        fileTv = (TextView) findViewById(R.id.im_file_tv);

        photoTv.setText(getContext().getString(R.string.chat_poto));
        cameraTv.setText(getContext().getString(R.string.photograph));
        talkTv.setText(getContext().getString(R.string.chat_send_video));
        positiTv.setText(getContext().getString(R.string.chat_loc));
        cardTv.setText(getContext().getString(R.string.chat_card));
        fileTv.setText(getContext().getString(R.string.chat_file));
    }

    public void setOnToolsClickListener(OnClickListener listener) {
        photoTv.setOnClickListener(listener);
        cameraTv.setOnClickListener(listener);
        talkTv.setOnClickListener(listener);
        positiTv.setOnClickListener(listener);
        cardTv.setOnClickListener(listener);
        fileTv.setOnClickListener(listener);
    }
}
