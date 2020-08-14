package com.sk.weichat.ui.circle.range;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.MyZan;
import com.sk.weichat.db.dao.MyZanDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.circle.BusinessCircleActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.view.SelectionFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/6/28 0028.
 * 最新评论 && 赞
 * 整理于2018/4/19 by zq: 之前逻辑太混乱
 */
public class NewZanActivity extends BaseActivity {
    List<MyZan> zanList = new ArrayList<>();
    List<MyZan> zanListIsRead = new ArrayList<>();
    private ListView lv;
    private MyAdapter adapter;
    private View bottomView;
    private LinearLayout loadMore;
    // 全部展开 || 显示几条
    private boolean openAll = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_zan);
        if (getIntent() != null) {
            openAll = getIntent().getExtras().getBoolean("OpenALL", false);
        }
        initActionBar();
        initView();
        initEvent();
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
        tvTitle.setText(getString(R.string.new_comment_and_praise));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.empty));
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(null, getString(R.string.tip_sure_delete_all_data), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        if (zanList == null || zanList.size() == 0) {
                            return;
                        }
                        for (int i = 0; i < zanList.size(); i++) {
                            MyZan zan = zanList.get(i);
/*
                            zan.setZanbooleanyidu(1);
                            MyZanDao.getInstance().UpdataZan(zan);
*/
                            MyZanDao.getInstance().deleteZan(zan);
                        }
                        zanList.clear();
                        adapter.notifyDataSetChanged();
                        lv.removeFooterView(bottomView);
                    }
                });
                selectionFrame.show();
            }
        });
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        adapter = new MyAdapter(this);
        bottomView = getLayoutInflater().inflate(R.layout.dongtai_loadmore, null);
        loadMore = (LinearLayout) bottomView.findViewById(R.id.load);
        TextView textView = (TextView) bottomView.findViewById(R.id.look_for_eary);
        textView.setText(getString(R.string.tip_look_early_message));
        lv.addFooterView(bottomView);
        // 获取到所有消息
        zanList = MyZanDao.getInstance().queryZan(coreManager.getSelf().getUserId());
        if (zanList == null || zanList.size() == 0) {
            findViewById(R.id.fl_empty).setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < zanList.size(); i++) {
            if ((zanList.get(i).getZanbooleanyidu() == 1)) {// 将已读的数据放入另一个集合
                zanListIsRead.add(zanList.get(i));
            }
        }
        zanList.removeAll(zanListIsRead);// 移除已读的，剩下最新的
        Collections.reverse(zanList);
        lv.setAdapter(adapter);
        for (int i = 0; i < zanList.size(); i++) {// 将最新的也标记为已读，更新数据库
            MyZan zan = zanList.get(i);
            zan.setZanbooleanyidu(1);
            MyZanDao.getInstance().UpdataZan(zan);
        }

        if (openAll) {
            lv.removeFooterView(bottomView);
            int size = zanList.size();
            zanList = MyZanDao.getInstance().queryZan(coreManager.getSelf().getUserId());// 展开全部
            if (zanList.size() != size) {
                Collections.reverse(zanList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void initEvent() {
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lv.removeFooterView(bottomView);
                int size = zanList.size();
                zanList = MyZanDao.getInstance().queryZan(coreManager.getSelf().getUserId());// 展开全部
                if (zanList.size() != size) {
                    Collections.reverse(zanList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), BusinessCircleActivity.class);
                intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                intent.putExtra(AppConstant.EXTRA_USER_ID, zanList.get(position).getFromUserId());
                intent.putExtra(AppConstant.EXTRA_NICK_NAME, zanList.get(position).getFromUsername());
                intent.putExtra("pinglun", zanList.get(position).getHuifu());
                intent.putExtra("dianzan", zanList.get(position).getFromUsername());
                intent.putExtra("isdongtai", true);
                intent.putExtra("messageid", zanList.get(position).getCricleuserid());
                startActivity(intent);
            }
        });
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return zanList.size();
        }

        @Override
        public Object getItem(int position) {
            return zanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.new_zan_item, parent, false);
                holder.fromimage = (ImageView) convertView.findViewById(R.id.fromimage);
                holder.name = (TextView) convertView.findViewById(R.id.fromname);
                holder.imagedianzan = (ImageView) convertView.findViewById(R.id.image_dianzhan);
                holder.pinglun = (TextView) convertView.findViewById(R.id.pinglun);
                holder.huifude = (LinearLayout) convertView.findViewById(R.id.huifude);
                holder.tousername = (TextView) convertView.findViewById(R.id.tousername);
                holder.huifuneirong = (TextView) convertView.findViewById(R.id.huifuneirong);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.getPinglun = (TextView) convertView.findViewById(R.id.text_pinglun);
                holder.toimage = (ImageView) convertView.findViewById(R.id.toimage);
                holder.voicepal = (ImageView) convertView.findViewById(R.id.voice_bg);
                holder.voiceplay = (ImageView) convertView.findViewById(R.id.voiceplay);
                holder.videopal = (ImageView) convertView.findViewById(R.id.videotheum);
                holder.videoplay = (ImageView) convertView.findViewById(R.id.videoplay);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String huifuid = zanList.get(position).getHuifu();
            holder.name.setText(zanList.get(position).getFromUsername());
            if (huifuid == null || huifuid.equals("101")) {// 为赞，隐藏评论与回复
                holder.imagedianzan.setVisibility(View.VISIBLE);
                holder.pinglun.setVisibility(View.GONE);
                holder.huifude.setVisibility(View.GONE);
            } else if (huifuid.equals("102")) {
                holder.imagedianzan.setVisibility(View.GONE);
                holder.pinglun.setVisibility(View.VISIBLE);
                holder.huifude.setVisibility(View.GONE);
                holder.pinglun.setText(R.string.tip_mention_you);
            } else {
                String content = zanList.get(position).getTousername();
                if (TextUtils.isEmpty(content)) {// ToUserName为空，为评论，隐藏赞与回复
                    holder.imagedianzan.setVisibility(View.GONE);
                    holder.pinglun.setVisibility(View.VISIBLE);
                    holder.huifude.setVisibility(View.GONE);
                    holder.pinglun.setText(zanList.get(position).getHuifu());
                } else {//  ToUserName不为空，为回复，隐藏赞与评论
                    holder.imagedianzan.setVisibility(View.GONE);
                    holder.pinglun.setVisibility(View.GONE);
                    holder.huifude.setVisibility(View.VISIBLE);
                    holder.tousername.setText(zanList.get(position).getTousername() + ":");
                    holder.huifuneirong.setText(zanList.get(position).getHuifu());
                }
            }
            holder.time.setText(TimeUtils.getFriendlyTimeDesc(mContext, Long.parseLong(zanList.get(position).getSendtime())));
            AvatarHelper.getInstance().displayAvatar(zanList.get(position).getFromUsername(), zanList.get(position).getFromUserId(), holder.fromimage, false);
            // type：1=文字；2=图片；3=语音；4=视频
            if (zanList.get(position).getType() == 1) {
                holder.getPinglun.setVisibility(View.VISIBLE);
                holder.toimage.setVisibility(View.GONE);
                holder.voicepal.setVisibility(View.GONE);
                holder.voiceplay.setVisibility(View.GONE);
                holder.videopal.setVisibility(View.GONE);
                holder.videoplay.setVisibility(View.GONE);
                holder.getPinglun.setText(zanList.get(position).getContent());
            } else if (zanList.get(position).getType() == 2) {
                holder.getPinglun.setVisibility(View.GONE);
                holder.toimage.setVisibility(View.VISIBLE);
                holder.voicepal.setVisibility(View.GONE);
                holder.voiceplay.setVisibility(View.GONE);
                holder.videoplay.setVisibility(View.GONE);
                holder.videopal.setVisibility(View.GONE);
                AvatarHelper.getInstance().displayUrl(zanList.get(position).getContenturl(), holder.toimage);
            } else if (zanList.get(position).getType() == 3) {
                holder.getPinglun.setVisibility(View.GONE);
                holder.toimage.setVisibility(View.GONE);
                holder.voicepal.setVisibility(View.VISIBLE);
                holder.voiceplay.setVisibility(View.VISIBLE);
                holder.videopal.setVisibility(View.GONE);
                holder.videoplay.setVisibility(View.GONE);
                AvatarHelper.getInstance().displayUrl(zanList.get(position).getContenturl(), holder.voicepal);
            } else if (zanList.get(position).getType() == 4) {
                holder.getPinglun.setVisibility(View.GONE);
                holder.toimage.setVisibility(View.GONE);
                holder.voicepal.setVisibility(View.GONE);
                holder.voiceplay.setVisibility(View.GONE);
                holder.videopal.setVisibility(View.VISIBLE);
                holder.videoplay.setVisibility(View.VISIBLE);
                AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(zanList.get(position).getContenturl(), holder.videopal);
            }
            return convertView;
        }
    }

    class ViewHolder {
        public ImageView fromimage;
        public TextView name;
        public ImageView imagedianzan;
        public TextView pinglun;
        public LinearLayout huifude;
        public TextView tousername;
        public TextView huifuneirong;
        public TextView time;
        public TextView getPinglun;
        public ImageView toimage;
        public ImageView voicepal;
        public ImageView voiceplay;
        public ImageView videopal;
        public ImageView videoplay;
    }
}
