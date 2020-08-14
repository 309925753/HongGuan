package com.sk.weichat.ui.live;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.sk.weichat.R;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.live.adapter.LiveFragmentPageAdapter;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 主播activity: 主要用来推流,并将参数传入操作界面(AnchorChatFragment)
 * javacv:
 * 可以直接接收取摄像头的帧数据(1s=1frame,1=12pics),通过ffmpeg来进行编码和推流
 * 打开摄像头，写一个SurfaceView进行预览，然后实现PreviewCallback将摄像头每一帧的数据交给javacv即可
 * yasea:
 * 使用android自带的编码工具，可实现硬编码
 * 一、软编码和硬编码如何区分
 * 软编码：使用CPU进行编码
 * 硬编码：使用非CPU进行编码，如显卡GPU、专用的DSP、FPGA、ASIC芯片等
 * 二、软编码和硬编码比较
 * 软编码：实现直接、简单，参数调整方便，升级易，但CPU负载重，性能较硬编码低，低码率下质量通常比硬编码要好一点。
 * 硬编码：性能高，低码率下通常质量低于软编码器，但部分产品在GPU硬件平台移植了优秀的软编码算法（如X264）的，质量基本等同于软编码。
 * 一：使用yasea进行摄像头采集、编码然后向srs服务器rtmp推流
 * 二：部署srs流媒体服务器
 */
public class PushFlowActivity extends BaseActivity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {
    private static final String TAG = "Yasea";
    private static final boolean DEBUG_NET = false;
    private SrsPublisher mPublisher;
    private ViewPager vp;
    private ArrayList<Fragment> fragments;
    // 直播界面
    private AnchorChatFragment anchorChatFragment;
    private CleanFragment cleanFragment;
    private String mAccessToken;
    // 推流地址/直播间id/房间jid/房间名/主播id
    private String mRtmpURL;
    private String mRoomid;
    private String mRoomJid;
    private String mRoomName;
    private String mRoomUserId;

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        if (!TextUtils.isEmpty(mRoomJid)) {
            coreManager.joinMucChat(mRoomJid, 0);
            return;
        }
        // 在服务绑定成功后mRoomJid可能还未赋值，坐下兼容
        if (getIntent() != null) {
            mRoomJid = getIntent().getStringExtra(LiveConstants.LIVE_CHAT_ROOM_ID);
        }
        coreManager.joinMucChat(mRoomJid, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_push_flow);
        mAccessToken = coreManager.getSelfStatus().accessToken;
        initView();
    }

    private void initView() {
        if (getIntent() != null) {
            mRtmpURL = getIntent().getStringExtra(LiveConstants.LIVE_PUSH_FLOW_URL);
            mRoomid = getIntent().getStringExtra(LiveConstants.LIVE_ROOM_ID);
            mRoomJid = getIntent().getStringExtra(LiveConstants.LIVE_CHAT_ROOM_ID);
            mRoomName = getIntent().getStringExtra(LiveConstants.LIVE_ROOM_NAME);
            mRoomUserId = getIntent().getStringExtra(LiveConstants.LIVE_ROOM_PERSON_ID);
        }
        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));

        // RTMP推流状态回调
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        // 编码状态回调
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        // 记录消息状态回调
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));

        // 设置展示界面大小(预览分辨率)
        mPublisher.setPreviewResolution(1280, 720);
        // 设置竖屏推流 1为竖屏 2为横屏
        mPublisher.setScreenOrientation(1);
        // 设置输出界面大小(推流分辨率)
        mPublisher.setOutputResolution(720, 1280);
        // 设置视频高清模式(传输率)
        mPublisher.setVideoHDMode();
        // 打开摄像头，开始预览(未推流)
        mPublisher.startCamera();
        // 设为硬编码
        // mPublisher.switchToHardEncoder();
        // 设为软编码
        mPublisher.switchToSoftEncoder();
        // 开始推流
        mPublisher.startPublish(mRtmpURL);
        /**
         * 操作区
         */
        vp = (ViewPager) findViewById(R.id.view_pager);
        // 针对系统销毁当前activity之后重建的容错处理
        removeFragments();
        fragments = new ArrayList<>();
        anchorChatFragment = new AnchorChatFragment(mPublisher, mRoomid, mRoomJid, mRoomName, mRoomUserId);
        cleanFragment = new CleanFragment();
        fragments.add(anchorChatFragment);
        fragments.add(cleanFragment);
        LiveFragmentPageAdapter mLiveFragmentPageAdapter = new LiveFragmentPageAdapter(getSupportFragmentManager(), fragments);
        vp.setAdapter(mLiveFragmentPageAdapter);
        vp.setCurrentItem(0);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void removeFragments() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> lf = fm.getFragments();
        for (Fragment f : lf) {
            fm.beginTransaction().remove(f).commitNowAllowingStateLoss();
        }
    }

    public void iAmLiving() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("status", "1");
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_STATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Log.e("zq", "已通知服务器，我正在直播，更新正在直播列表");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    public void iAmFinishLiving() {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("status", "0");
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_STATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            Log.e("zq", "已通知服务器，我结束直播,更新正在直播列表");
                            MucgroupUpdateUtil.broadcastUpdateUi(PushFlowActivity.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPublisher.resumeRecord();
        // 告诉服务器，我正在进行直播
        iAmLiving();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
        // 告诉服务器我已结束直播
        iAmFinishLiving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
    }

    /**
     * 异常处理
     */
    private void handleException(Exception exception) {
        log("1");
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Implementation of SrsRtmpListener.

    /**
     * RtmpHandler
     *
     * @param msg
     */
    @Override
    public void onRtmpConnecting(String msg) {
        log("2");
        Toast.makeText(this, getString(R.string.registration_inprogress), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        log("3");
        Toast.makeText(this, getString(R.string.registration_ok), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpStopped() {
        log("4");
    }

    @Override
    public void onRtmpDisconnected() {
        log("5");
        // Toast.makeText(this, getString(R.string.break_off), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
        log("6");
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        log("7");
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        log("8");
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioStreaming() {
        log("9");
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        log("10");
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {// 切换网络会回调到这里，之后好像就不会继续推流了
        log("11");
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        log("12");
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        log("13");
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        log("14");
        handleException(e);
    }

    // Implementation of SrsRecordHandler.

    /**
     * SrsRecordHandler
     */
    @Override
    public void onRecordStarted(String msg) {
        log("15");
    }

    @Override
    public void onRecordResume() {
        log("16");
    }

    @Override
    public void onRecordPause() {
        log("17");
    }

    @Override
    public void onRecordFinished(String msg) {
        log("18");
    }

    @Override
    public void onRecordIOException(IOException e) {
        log("19");
        handleException(e);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        log("20");
        handleException(e);
    }

    // Implementation of SrsEncodeHandler.

    /**
     * SrsEncodeHandler
     */

    @Override
    public void onNetworkWeak() {
        log("21");
    }

    @Override
    public void onNetworkResume() {
        log("22");
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        log("23");
        handleException(e);
    }

    private void log(String msg) {
        if (DEBUG_NET) {
            Log.e("live", msg);
        }
    }
}
