package com.sk.weichat.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.adapter.PublicMessageRecyclerAdapter;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.collection.CollectionEvery;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardforchat;
import okhttp3.Call;

/**
 * Created by Administrator on 2017/10/20 0020.
 * 我的收藏
 */
public class MyCollection extends BaseActivity {
    private boolean isSendCollection;

    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mPullToRefreshListView;
    private PublicMessageRecyclerAdapter mPublicMessageAdapter;
    private List<PublicMessage> mPublicMessage;
    private List<CollectionEvery> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_my_collection);
        if (getIntent() != null) {
            isSendCollection = getIntent().getBooleanExtra("IS_SEND_COLLECTION", false);
        }
        mPublicMessage = new ArrayList<>();
        initActionBar();
        initView();
        getMyCollectionList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VoicePlayer.instance().stop();
    }

    @Override
    public void onBackPressed() {
        if (!JVCideoPlayerStandardforchat.handlerBack()) {
            finish();
        }
    }

    @Override
    public void finish() {
        JCVideoPlayer.releaseAllVideos();
        super.finish();
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
        //  tvTitle.setText(InternationalizationHelper.getString("JX_MyCollection"));
        tvTitle.setText(R.string.my_collection);
    }

    private void initView() {
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mPullToRefreshListView = findViewById(R.id.recyclerView);
        mPullToRefreshListView.setLayoutManager(new LinearLayoutManager(this));
        mPublicMessageAdapter = new PublicMessageRecyclerAdapter(MyCollection.this, coreManager, mPublicMessage);
        if (isSendCollection) {
            mPublicMessageAdapter.setCollectionType(2);
        } else {
            mPublicMessageAdapter.setCollectionType(1);
        }

        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            getMyCollectionList();
        });

        mPullToRefreshListView.setAdapter(mPublicMessageAdapter);
        mPublicMessageAdapter.setOnItemClickListener(vh -> {
            if (isSendCollection) {
                int position = vh.getAdapterPosition();
                CollectionEvery collection = data.get(position);
                if (collection != null) {
                    SelectionFrame dialog = new SelectionFrame(MyCollection.this);
                    dialog.setSomething(null, getString(R.string.tip_confirm_send), new SelectionFrame.OnSelectionFrameClickListener() {
                        @Override
                        public void cancelClick() {
                        }

                        @Override
                        public void confirmClick() {
                            Intent intent = new Intent();
                            try {
                                intent.putExtra("data", JSON.toJSONString(collection));
                                setResult(RESULT_OK, intent);
                                finish();
                            } catch (JSONException e) {
                                ToastUtil.showToast(mContext, getString(R.string.parse_exception));
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();
                } else {
                    Reporter.unreachable();
                    ToastUtil.showToast(mContext, R.string.tip_server_error);
                }
            }
        });
    }

    /**
     * 将url传给服务端，服务端
     */

    /**
     * 1.获取收藏列表
     * 2.将msg字段解析，变成List<ChatMessage>
     * 3.将List<ChatMessage>转换为List<PublishMessage>
     */
    public void getMyCollectionList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().Collection_LIST_OTHER)
                .params(params)
                .build()
                .execute(new ListCallback<CollectionEvery>(CollectionEvery.class) {
                    @Override
                    public void onResponse(ArrayResult<CollectionEvery> result) {
                        DialogHelper.dismissProgressDialog();
                        mPublicMessage.clear();
                        mRefreshLayout.finishRefresh();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            afterGetData(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 坑太大，不应该和朋友圈共用adapter, 共用实体类，
     */
    public void afterGetData(List<CollectionEvery> data) {
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            CollectionEvery collection = data.get(i);
            PublicMessage publicMessage = new PublicMessage();
            publicMessage.setUserId(coreManager.getSelf().getUserId());
            publicMessage.setNickName(coreManager.getSelf().getNickName());
            // 显示的时间应该为收藏这条消息的时间，而不是发送这条消息的时间
            publicMessage.setTime(data.get(i).getCreateTime());
            // 播放语音需要用到messageId,否则不需要设置
            publicMessage.setMessageId(collection.getEmojiId());
            // 文件名，
            publicMessage.setFileName(collection.getFileName());
            String name = collection.getFileName();
            try {
                // 服务器给的文件名可能包含路径，
                // TODO: PC端可能给反斜杠\, 没测，
                int lastIndex = name.lastIndexOf('/');
                publicMessage.setFileName(name.substring(lastIndex + 1));
            } catch (Exception e) {
                publicMessage.setFileName(name);
            }
            // 我的收藏专属id
            publicMessage.setEmojiId(data.get(i).getEmojiId());

            PublicMessage.Body body = new PublicMessage.Body();
            // 朋友圈收藏来的所有消息类型都有collectContent文本内容字段，
            body.setText(collection.getCollectContent());
            if (collection.getType() == CollectionEvery.TYPE_TEXT) {
                // 文本
                body.setType(PublicMessage.TYPE_TEXT);
                // 聊天收藏来的文字消息没有collectCntent字段，
                // 聊天和朋友圈收藏来的文字消息都有msg字段，
                collection.setCollectContent(collection.getMsg());
                body.setText(collection.getCollectContent());
            } else if (collection.getType() == CollectionEvery.TYPE_IMAGE) {
                // 图片
                body.setType(PublicMessage.TYPE_IMG);
                List<PublicMessage.Resource> images = new ArrayList<>();
                String allUrl = collection.getUrl();
                if (!TextUtils.isEmpty(allUrl)) {
                    for (String url : allUrl.split(",")) {
                        PublicMessage.Resource resource = new PublicMessage.Resource();
                        resource.setOriginalUrl(url);
                        images.add(resource);
                    }
                }
                body.setImages(images);
            } else if (collection.getType() == CollectionEvery.TYPE_VOICE) {
                // 语音
                body.setType(PublicMessage.TYPE_VOICE);
                List<PublicMessage.Resource> audios = new ArrayList<>();
                PublicMessage.Resource resource = new PublicMessage.Resource();
                resource.setLength(collection.getFileLength());
                resource.setSize(collection.getFileSize());
                resource.setOriginalUrl(collection.getUrl());
                audios.add(resource);
                body.setAudios(audios);
            } else if (collection.getType() == CollectionEvery.TYPE_VIDEO) {
                // 视频
                body.setType(PublicMessage.TYPE_VIDEO);
                // 视频封面
                /*List<PublicMessage.Resource> images = new ArrayList<>();
                PublicMessage.Resource resource1 = new PublicMessage.Resource();
                resource1.setOriginalUrl(message.getContent());
                images.add(resource1);
                body.setImages(images);*/
                // 视频源
                List<PublicMessage.Resource> videos = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                videos.add(resource2);
                body.setVideos(videos);
            } else if (collection.getType() == CollectionEvery.TYPE_FILE) {
                // 文件
                body.setType(PublicMessage.TYPE_FILE);
                List<PublicMessage.Resource> files = new ArrayList<>();
                PublicMessage.Resource resource2 = new PublicMessage.Resource();
                resource2.setOriginalUrl(collection.getUrl());
                resource2.setLength(collection.getFileLength());
                resource2.setSize(collection.getFileSize());
                files.add(resource2);
                body.setFiles(files);
            }
            publicMessage.setBody(body);
            mPublicMessage.add(publicMessage);
        }
        mPublicMessageAdapter.setData(mPublicMessage);
    }
}
