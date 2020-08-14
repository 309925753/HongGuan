package com.sk.weichat.ui.mucfile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.InstantMessageActivity;
import com.sk.weichat.ui.mucfile.bean.DownBean;
import com.sk.weichat.ui.mucfile.bean.MucFileBean;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.video.ChatVideoPreviewActivity;

import java.io.File;
import java.util.UUID;

import static com.sk.weichat.ui.mucfile.DownManager.STATE_DOWNLOADED;
import static com.sk.weichat.ui.mucfile.DownManager.STATE_DOWNLOADFAILED;
import static com.sk.weichat.ui.mucfile.DownManager.STATE_UNDOWNLOAD;
import static com.sk.weichat.ui.mucfile.DownManager.STATE_WAITINGDOWNLOAD;

/**
 * Created by Administrator on 2017/7/4.
 */
public class MucFileDetails extends BaseActivity implements DownManager.DownLoadObserver, View.OnClickListener {
    private ImageView ivInco;
    private TextView tvName;
    private Button btnStart;
    private TextView tvSize;
    private NumberProgressBar progressPar;
    private MucFileBean data;
    private RelativeLayout rlProgress;
    private TextView tvType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc_dateils);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());

        ImageView ivRight = findViewById(R.id.iv_title_right);
        ivRight.setImageResource(R.mipmap.share_icon);
        ivRight.setOnClickListener(v -> {
            shareFile();
        });

        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.detail));
        data = (MucFileBean) getIntent().getSerializableExtra("data");
        Log.e(TAG, "onCreate: " + data);
        if (TextUtils.isEmpty(data.getUrl())) {
            ToastUtil.showToast(mContext, R.string.data_exception);
            finish();
            return;
        }
        initView();
        initDatas();
    }

    /**
     * 发送给朋友
     */
    private void shareFile() {

        String mLoginUserId = coreManager.getSelf().getUserId();

        ChatMessage message = new ChatMessage();
        message.setType(fileType2XmppType(data.getType()));
        message.setContent(data.getUrl());
        message.setFileSize((int) data.getSize());
        message.setFilePath(data.getName());
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());

        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, AppConstant.NORMAL_INSTANT_ID, message)) {
            Intent intent = new Intent(MucFileDetails.this, InstantMessageActivity.class);
            intent.putExtra("fromUserId", AppConstant.NORMAL_INSTANT_ID);
            intent.putExtra("messageId", message.getPacketId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(mContext, getString(R.string.tip_message_wrap_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private int fileType2XmppType(int type) {
        if (type == 1) {
            return XmppMessage.TYPE_IMAGE;
        } else if (type == 3) {
            return XmppMessage.TYPE_VIDEO;
        }
        return XmppMessage.TYPE_FILE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownManager.instance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownManager.instance().deleteObserver(MucFileDetails.this);
    }

    public void initView() {
        Log.e(TAG, "initview: ");
        ivInco = (ImageView) findViewById(R.id.item_file_inco);
        tvName = (TextView) findViewById(R.id.item_file_name);
        tvType = (TextView) findViewById(R.id.item_file_type);
        tvSize = (TextView) findViewById(R.id.muc_dateils_size);

        btnStart = (Button) findViewById(R.id.btn_muc_down);
//        btnStart.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        ButtonColorChange.colorChange(this, btnStart);
        progressPar = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        rlProgress = (RelativeLayout) findViewById(R.id.muc_dateils_rl_pro);
        btnStart.setOnClickListener(this);
        findViewById(R.id.muc_dateils_stop).setOnClickListener(this);
        findViewById(R.id.muc_dateils_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        tvType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.getState() == DownManager.STATE_DOWNLOADING) {
                    DownManager.instance().pause(data);
                }

                if (data.getType() == 2) { // 音乐在线播放
                    Intent intent = new Intent(mContext, ChatVideoPreviewActivity.class);
                    intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_PATH, data.getUrl());
                    startActivity(intent);
                } else if (data.getType() == 3) {  // 视频在线播放
                    Intent intent = new Intent(mContext, ChatVideoPreviewActivity.class);
                    intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_PATH, data.getUrl());
                    startActivity(intent);
                } else if (data.getType() == 4 || data.getType() == 5 || data.getType() == 6 || data.getType() == 10) {
                    // 在线预览文档，需要解析，上面为微软的，下面为谷歌的(google无法访问)
/*
                    String url = "https://view.officeapps.live.com/op/view.aspx?src=" + data.getUrl();
                    //String url = "https://docs.google.com/viewer?url=" + data.getUrl();
                    Intent intent = new Intent(MucFileDetails.this, WebViewActivity.class);
                    intent.putExtra(EXTRA_URL, url);
                    startActivity(intent);
*/
                    ToastUtil.showToast(MucFileDetails.this, R.string.tip_preview_file_type_not_support);
                } else {
                    Intent intent = new Intent(MucFileDetails.this, MucFilePreviewActivity.class);
                    intent.putExtra("data", data);
                    startActivity(intent);
                }
            }
        });
    }

    protected void initDatas() {
        Log.e(TAG, "initDatas: ");
        if (data != null) {
            updateUI();
        }
    }

    private void updateUI() {
        Log.e(TAG, "updateUI: ");
        if (data.getType() == 1) {
            // 图片直接显示
            ImageLoadHelper.showImageCenterCropWithSize(
                    MucFileDetails.this,
                    data.getUrl(),
                    100, 100,
                    ivInco
            );
        } else {
            // 加载本地
            XfileUtils.setFileInco(data.getType(), ivInco);
        }
        tvName.setText(data.getName());

        if (data.getType() == 9) {
            tvType.setText(getString(R.string.not_support_preview));
        } else if (data.getType() == 4 || data.getType() == 5 || data.getType() == 6 || data.getType() == 10) {
            tvType.setText(getString(R.string.not_support_preview));
        } else {

            SpannableString type = XfileUtils.matcherSearchTitle(Color.parseColor("#6699FF"),
                    getString(R.string.preview_online), getString(R.string.look_online));
            tvType.setText(type);

        }
        tvName.setText(data.getName());
        DownBean info = DownManager.instance().getDownloadState(data);
        onDownLoadInfoChange(info);
    }

    @Override
    public void onClick(View v) {
        if (checkNet()) {
            // 检查网络
            switch (data.getState()) {
                case STATE_DOWNLOADED:
                    open();
                    break;
                case STATE_UNDOWNLOAD:
                    down();
                    break;
                case DownManager.STATE_DOWNLOADING:
                    stop();
                    break;
                case DownManager.STATE_PAUSEDOWNLOAD:
                    start();
                    break;
                case STATE_WAITINGDOWNLOAD:
                    cancelDown();
                    break;
                case STATE_DOWNLOADFAILED:
                    down();
                    break;
            }
        }
    }

    private void open() {
        File file = new File(DownManager.instance().getFileDir(), data.getName());
        FileOpenWays openWays = new FileOpenWays(mContext);
        openWays.openFiles(file.getAbsolutePath());
    }

    private void del() {
        DownManager.instance().detele(data);
    }

    private void cancelDown() {
        DownManager.instance().cancel(data);
    }

    private void stop() {
        DownManager.instance().pause(data);
    }

    private void down() {
        DownManager.instance().download(data);
    }

    private void start() {
        DownManager.instance().download(data);
    }

    private boolean checkNet() {
        return true;
    }

    @Override
    public void onDownLoadInfoChange(final DownBean info) {
        data.setState(info.state);

        int progress = (int) (info.cur / (float) info.max * 100);
        progressPar.setProgress(progress);

        rlProgress.setVisibility(View.VISIBLE);

        switch (info.state) {
            case STATE_DOWNLOADED:
                tvType.setText(getString(R.string.download_complete));
                btnStart.setText(getString(R.string.open));
                rlProgress.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                break;
            case STATE_UNDOWNLOAD:
                tvSize.setText(getString(R.string.not_downloaded));
                btnStart.setText(getString(R.string.download) + "(" + XfileUtils.fromatSize(info.max) + ")");
                rlProgress.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                break;
            case DownManager.STATE_DOWNLOADING:
                tvSize.setText(getString(R.string.downloading) + "…(" + XfileUtils.fromatSize(info.cur) + "/" + XfileUtils.fromatSize(info.max) + ")");
                btnStart.setVisibility(View.GONE);
                rlProgress.setVisibility(View.VISIBLE);
                break;
            case DownManager.STATE_PAUSEDOWNLOAD:
                rlProgress.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                tvSize.setText(getString(R.string.in_pause) + "…(" + XfileUtils.fromatSize(info.cur) + "/" + XfileUtils.fromatSize(info.max) + ")");
                btnStart.setText(getString(R.string.continue_downloading) + "(" + XfileUtils.fromatSize((info.max - info.cur)) + ")");
                break;
            case STATE_WAITINGDOWNLOAD:
                break;
            case STATE_DOWNLOADFAILED:
                tvType.setText(getString(R.string.download_error));
                rlProgress.setVisibility(View.GONE);
                tvSize.setText(getString(R.string.redownload));
                btnStart.setVisibility(View.VISIBLE);
                break;
        }
    }
}
