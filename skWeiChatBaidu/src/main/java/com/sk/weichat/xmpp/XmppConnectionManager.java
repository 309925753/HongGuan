package com.sk.weichat.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.event.MessageEventBG;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.HttpUtil;
import com.sk.weichat.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.Callable;

import de.greenrobot.event.EventBus;

/**
 * XMPP连接类
 */
public class XmppConnectionManager {
    private static final String TAG = "zq";

    /* Handler */ // 这些值在监听状态的messageFragment还有用到，
    private static final int MSG_CONNECTING = 0;// 连接中...
    private static final int MSG_CONNECTED = 1;// 已连接
    private static final int MSG_AUTHENTICATED = 2;// 已认证
    private static final int MSG_CONNECTION_CLOSED = 3;// 连接关闭
    private static final int MSG_CONNECTION_CLOSED_ON_ERROR = 4;// 连接错误
    public static int mXMPPCurrentState;
    private Context mContext;
    private NotifyConnectionListener mNotifyConnectionListener;
    private XMPPTCPConnection mConnection;
    private XReconnectionManager mReconnectionManager;
    private XServerReceivedListener XServerReceivedListener;
    private boolean mIsNetWorkActive;// 当前网络是否连接上
    private boolean doLogining = false;
    private String mLoginUserId;  // 仅用于登陆失败，重新登陆用
    private LoginThread mLoginThread;
    private boolean isReturned;
    private AbstractConnectionListener mAbstractConnectionListener = new AbstractConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.e(TAG, "connected：已连接");
            mXMPPCurrentState = MSG_CONNECTED;
            if (mNotifyConnectionListener != null) {
                mNotifyConnectionListener.notifyConnected(connection);
            }
        }

        @Override
        public void authenticated(final XMPPConnection connection, boolean resumed) {
            Log.e(TAG, "authenticated：认证成功");
            Log.e(TAG, "resumed-->" + resumed);

            mXMPPCurrentState = MSG_AUTHENTICATED;
            if (mNotifyConnectionListener != null) {
                mNotifyConnectionListener.notifyAuthenticated(connection);
            }

            if (mConnection.isSmResumptionPossible()) {
                Log.e(TAG, "服务端开启了流");
            } else {
                Log.e(TAG, "服务端关闭了流");
                MyApplication.IS_OPEN_RECEIPT = true;// 检查服务器是否启用了流管理，如关闭本地请求回执标志位一定为true
            }
        }

        @Override
        public void connectionClosed() {
            Log.e(TAG, "connectionClosed：连接关闭");
            mXMPPCurrentState = MSG_CONNECTION_CLOSED;
            if (mNotifyConnectionListener != null) {
                mNotifyConnectionListener.notifyConnectionClosed();
            }

            EventBus.getDefault().post(new MessageEventBG(false, true));
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.e(TAG, "connectionClosedOnError：连接异常");
            Log.e(TAG, "connectionClosedOnError：" + e.getMessage());
            Reporter.post("xmpp connectionClosedOnError,", e);
            mXMPPCurrentState = MSG_CONNECTION_CLOSED_ON_ERROR;
            if (mNotifyConnectionListener != null) {
                mNotifyConnectionListener.notifyConnectionClosedOnError(e);
            }

            EventBus.getDefault().post(new MessageEventBG(false, true));

            if (TextUtils.equals(e.getMessage(), "Parser got END_DOCUMENT event. This could happen e.g. if the server closed the connection without sending a closing stream element")
                    || TextUtils.equals(e.getMessage(), "Broken pipe")) {
                // 开启流管理的情况下偶现该问题
                // 当message为END_DOCUMENT或Broken pipe时，正常的login以及reconnect都连接不上XMPP了，必须退出当前账号||退出程序 重进才可以，这里我们发送一个广播进行特殊的重连
                MyApplication.getInstance().sendBroadcast(new Intent(Constants.CLOSED_ON_ERROR_END_DOCUMENT));
            } else {
                // 切换网络之后，应该会立即回调到该方法内，网络改变监听才监听到网络改变，
                // 但偶现网络监听监听到网络改变，XMPP还是认证的情况(即监听先于该方法调用)，导致之后回调到该方法内没有去重新登录XMPP了
                // 所以当回调到这里的时候我们模拟发送一个网络改变的广播
                // 调试发现如果每次回调该方法之后都发送一个广播出去，重连速度会变得比较慢，所以我们只针对isReturned的情况发送
                if (isReturned) {
                    isReturned = false;
                    MyApplication.getInstance().sendBroadcast(new Intent(Constants.CLOSED_ON_ERROR_NORMAL));
                }
            }
        }
    };
    private BroadcastReceiver mNetWorkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            Log.e(TAG, "监测到网络改变");
            mIsNetWorkActive = isGprsOrWifiConnected();
            if (isAuthenticated()) {
                Log.e(TAG, "XMPP已认证，Return");
                isReturned = true;
                return;
            }
            if (mIsNetWorkActive) {// 有网
                if (isLoginAllowed()) {
                    Log.e(TAG, "有网，开始登录");

                    login(mLoginUserId);
                }
            } else {// 无网
                Log.e(TAG, "无网");
                if (mLoginThread != null && mLoginThread.isAlive()) {
                    Log.e(TAG, "无网且登录线程isAlive,打断该线程");
                    mLoginThread.interrupt();
                }
            }
        }
    };

    public XmppConnectionManager(Context context, NotifyConnectionListener listener) {
        mContext = context;
        mNotifyConnectionListener = listener;

        mConnection = new XMPPTCPConnection(getConnectionConfiguration());
        mConnection.addConnectionListener(mAbstractConnectionListener);

        initNetWorkStatusReceiver();
        mReconnectionManager = new XReconnectionManager(mContext, mConnection);
        // 流管理启用生效
        XServerReceivedListener = new XServerReceivedListener();
        mConnection.addStanzaAcknowledgedListener(XServerReceivedListener);
    }

    private XMPPTCPConnectionConfiguration getConnectionConfiguration() {
        final String mXmppHost = CoreManager.requireConfig(MyApplication.getInstance()).XMPPHost;
        int mXmppPort = CoreManager.requireConfig(MyApplication.getInstance()).mXMPPPort;
        String mXmppDomain = CoreManager.requireConfig(MyApplication.getInstance()).XMPPDomain;

        DomainBareJid mDomainBareJid = null;
        try {
            mDomainBareJid = JidCreate.domainBareFrom(mXmppDomain);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        InetAddress address = AsyncUtils.forceAsync(new Callable<InetAddress>() {
            @Override
            public InetAddress call() throws Exception {
                try {
                    return InetAddress.getByName(mXmppHost);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
                .setHostAddress(address) // 服务器地址
                .setPort(mXmppPort) // 服务器端口
                .setXmppDomain(mDomainBareJid)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.ifpossible) // 是否开启安全模式
                .setCompressionEnabled(true)
                .setSendPresence(false);
        if (Log.isLoggable("SMACK", Log.DEBUG)) {
            // 为方便测试，留个启用方法，命令运行"adb shell setprop log.tag.SMACK D"启用，
            builder.enableDefaultDebugger();
        }
        // 如果本地有用户信息，取出来放进config里用于避免自动重连时崩溃，
        // 自动重连时connection中如果没有username就会从config中拿，还是优先connection中的参数，
        User self = CoreManager.getSelf(mContext);
        if (self != null) {
            builder.setUsernameAndPassword(self.getUserId(), UserSp.getInstance(mContext).getAccessToken());
        }
        Resourcepart mResourcepart;
        if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
            mResourcepart = Resourcepart.fromOrThrowUnchecked("android");
        } else {
            mResourcepart = Resourcepart.fromOrThrowUnchecked("youjob");
        }
        builder.setResource(mResourcepart);
        return builder.build();
    }

    public XMPPTCPConnection getConnection() {
        return mConnection;
    }

    public boolean isAuthenticated() {
        return mConnection != null && mConnection.isConnected() && mConnection.isAuthenticated();
    }

    private boolean isLoginAllowed() {
        return doLogining && mIsNetWorkActive && (!mConnection.isConnected() || !mConnection.isAuthenticated());
    }

    private boolean isGprsOrWifiConnected() {
        if (!HttpUtil.isGprsOrWifiConnected(mContext)) {
            logout();
        } else {
            if (!TextUtils.isEmpty(CoreManager.requireSelf(mContext).getUserId())
                    && !TextUtils.isEmpty(CoreManager.requireSelf(mContext).getPassword()))
                login(CoreManager.requireSelf(mContext).getUserId());
        }
        return true;
    }

    /*********************
     * 网络连接状态
     ***************/
    private void initNetWorkStatusReceiver() {
        // 获取程序启动时的网络状态
        mIsNetWorkActive = isGprsOrWifiConnected();
        // 注册网络监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.CLOSED_ON_ERROR_NORMAL);
        mContext.registerReceiver(mNetWorkChangeReceiver, intentFilter);
    }

    public synchronized void login(final String userId) {
        if (mConnection.isAuthenticated()) {
            /*// 如果已经登陆
            if (StringUtils.parseName(mConnection.getUser()).equals(userId)) {
                // 如果登陆的用户和需要在登陆的是同一个用户，赋予可能改变的用户名和密码，返回
                return;
            } else {
                mConnection.disconnect();
            }*/
            return;
        }

        String password = UserSp.getInstance(mContext).getAccessToken();
        if (TextUtils.isEmpty(password)) {
            return;
        }
        if (mLoginThread != null && mLoginThread.isAlive()) {
            // 正在进行上一个用户的登陆中，或者用户密码变更，但是还在登陆中
            if (mLoginThread.isSameUser(userId, password)) {
                if (mLoginThread.getAttempts() > 13) {
                    Log.e(TAG, "Currently logged in xmpp, but the attempts is too big.End the current thread,start a new LoginThread");
                    // 当尝试次数大于13的时候，尝试的时间变得太长，果断结束点，开始一次新的尝试
                    mLoginThread.interrupt();
                    doLogining = false;
                } else {
                    Log.e(TAG, "Currently logged in xmpp,Repeated call login method,return." + "attempts--->" + mLoginThread.getAttempts());
                    return;
                }
            } else {
                // 和之前在尝试登陆的用户属性一致，结束这个登陆的线程
                mLoginThread.interrupt();
                doLogining = false;
                return;
            }
        }
        // 等待上一个登陆线程的结束，才开始下一个
        long time = System.currentTimeMillis();
        while (mLoginThread != null && mLoginThread.isAlive()) {
            if (System.currentTimeMillis() - time > 3000) {
                // 防止结束线程时异常了，卡住主线程
                break;
            }
        }
        doLogining = true;
        mLoginUserId = userId;

        mLoginThread = new LoginThread(userId, password);
        mLoginThread.start();
    }

    void logout() {
        doLogining = false;
        if (mLoginThread != null && mLoginThread.isAlive()) {
            mLoginThread.interrupt();
        }
        if (mReconnectionManager != null) {
            mReconnectionManager.release();
        }
        if (mConnection == null) {
            return;
        }

        presenceOffline();

        if (mConnection.isConnected()) {
            Log.e("zq", "断开连接" + 3);
            mConnection.disconnect();
        }
    }

    void release() {
        mContext.unregisterReceiver(mNetWorkChangeReceiver);
        doLogining = false;
        if (mLoginThread != null && mLoginThread.isAlive()) {
            mLoginThread.interrupt();
        }
        mReconnectionManager.release();

        presenceOffline();

        if (mConnection != null && mConnection.isConnected()) {
            Log.e("zq", "断开连接" + 4);
            mConnection.disconnect();
        }
    }

    void sendOnLineMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                presenceOnline();
            }
        }).start();
    }

    private void presenceOnline() {
        Presence presence = new Presence(Presence.Type.available);
        try {
            try {
                mConnection.sendStanza(presence);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void presenceOffline() {
        Presence presence = new Presence(Presence.Type.unavailable);
        try {
            try {
                mConnection.sendStanza(presence);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录 xmpp 线程
     */
    private class LoginThread extends Thread {
        private String loginUserId;
        private String loginPassword;
        private int attempts;
        private int randomBase = new Random().nextInt(11) + 5; // between 5 and 15seconds
        private int connectionTimeInterval = 9;

        LoginThread(String loginUserId, String loginPassword) {
            this.loginUserId = loginUserId;
            this.loginPassword = loginPassword;
            this.setName("Xmpp Login Thread" + loginUserId);
        }

        public boolean isSameUser(String userId, String password) {
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password)) {
                return false;
            }
            return loginUserId.equals(userId) && loginPassword.equals(password);
        }

        public int getAttempts() {
            return attempts;
        }

        /**
         * Returns the number of seconds until the next reconnection attempt.
         *
         * @return the number of seconds until the next reconnection attempt.
         */
        private int timeDelay() {
            attempts++;
            if (attempts > 13) {
                return randomBase * 6 * 5; // between 2.5 and 7.5 minutes
            }
            if (attempts > 7) {
                return randomBase * 6; // between 30 and 90 seconds (~1 minutes)
            }
            return randomBase; // 10 seconds
        }

        /**
         * timeDelay : xmpp connection failed,
         * if attempts <= 7, connection interval is 10 seconds,
         * attempts > 7,interval is 60 seconds,
         * attempts > 13,interval is 5 minutes
         * <p>
         * <p>
         * <p>
         * timeDelay2: xmpp connection failed,
         * if attempts > 21,interval is always 30 seconds,
         * else interval is 9 seconds + attempts
         *
         * @return the number of seconds until the next reconnection attempt.
         */
        private int timeDelay2() {
            attempts++;
            if (connectionTimeInterval >= 30) {
                return connectionTimeInterval;
            }
            return connectionTimeInterval + attempts;
        }

        public void run() {
            while (isLoginAllowed()) {
                mXMPPCurrentState = MSG_CONNECTING;
                if (mNotifyConnectionListener != null) {
                    mNotifyConnectionListener.notifyConnecting();
                }
                try {
                    if (!mConnection.isConnected()) {
                        try {
                            mConnection.connect();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                       /* TrafficStats.setThreadStatsTag(0x01);
                        try {
                            ((XMPPTCPConnection) mConnection).connectWithoutLogin();
                        } catch (Exception e) {
                            // 捕获到异常
                        } finally {
                            TrafficStats.clearThreadStatsTag();
                        }*/
                    }

                    // 登录XMPP
                    // resource 改为全局变量
                    Resourcepart mResourcepart;
                    if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
                        mResourcepart = Resourcepart.fromOrThrowUnchecked(MyApplication.MULTI_RESOURCE);
                    } else {
                        mResourcepart = Resourcepart.fromOrThrowUnchecked("youjob");
                    }

                    try {
                        mConnection.login(loginUserId, loginPassword, mResourcepart);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mConnection.isAuthenticated()) {// 登录成功 已验证
                        PingManager.getInstanceFor(mConnection).setPingInterval(CoreManager.requireConfig(MyApplication.getInstance()).xmppPingTime);
                        PingManager.getInstanceFor(mConnection).registerPingFailedListener(new PingFailedListener() {// 注册PING机制失败回调
                            @Override
                            public void pingFailed() {
                                Log.e(TAG, "ping 失败了");
                                // ping失败之后，我端在服务端的状态变为离线，且无法收发消息，同时因为ping失败之后不会回调到任何一个xmpp的监听，消息界面还显示在线，所以会造成一些bug
                                // 1.本地调用断开连接的方法
                                mAbstractConnectionListener.connectionClosed();
                                // 2.发送广播到主界面，进行重连
                                MyApplication.getInstance().sendBroadcast(new Intent(Constants.PING_FAILED));
                            }
                        });
                    } else {
                        Log.e("zq", "断开连接" + 1);
                        mConnection.disconnect();
                    }
                } catch (SmackException | IOException e) {
                    // Todo if SASL Authentication failed. No know authentication mechanisims. Need import Smack-sasl-provided.jar
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                    if (!TextUtils.isEmpty(e.getMessage())
                            && e.getMessage().contains("not-authorized")) { // org.jivesoftware.smack.sasl.SASLErrorException: SASLError using PLAIN: not-authorized
                        MyApplication.getInstance().sendBroadcast(new Intent(Constants.NOT_AUTHORIZED));
                    }
                    return;
                }
                if (mConnection.isAuthenticated()) {
                    if (!XmppStringUtil.parseName(mConnection.getUser().toString()).equals(loginUserId)) {
                        Log.e("zq", "断开连接" + 2);
                        mConnection.disconnect();
                    } else {
                        doLogining = false;
                        // mAbstractConnectionListener.authenticated(mConnection);
                    }
                } else {
                    // Find how much time we should wait until the next try
                    int remainingSeconds = timeDelay2();
                    Log.e(TAG, "login try delay：remainingSeconds：" + remainingSeconds);
                    while (isLoginAllowed() && remainingSeconds > 0) {
                        Log.e(TAG, "login try delay");
                        try {
                            Thread.sleep(1000);
                            remainingSeconds--;
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
