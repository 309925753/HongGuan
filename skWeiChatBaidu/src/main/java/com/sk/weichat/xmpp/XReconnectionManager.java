package com.sk.weichat.xmpp;

import android.content.Context;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.UserCheckedActivity;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPException.StreamErrorException;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * 当网络发生改变<由无网络变成有网络>的时候，会调用该类的重连线程 {@link #setNetWorkState(boolean)}-->重连{@link #reconnect()} 或 停止重连{@link #mReconnectionThread}{@link Thread#interrupt()};
 */
public class XReconnectionManager extends AbstractConnectionListener {
    private static final String TAG = "XReconnectionManager";

    private Context mContext;
    private XMPPTCPConnection mConnection;
    private ReconnectionManager mReconnectionManager;

    public XReconnectionManager(Context context, XMPPTCPConnection connection) {
        mContext = context;
        mConnection = connection;
        mConnection.addConnectionListener(this);

        // 不自己重连，改为smack内部的自动重连
        mReconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        /**
         * XmppConnectionManager 的 mNetWorkChangeReceiver 有自动重连处理(xmpp回调到连接断开||异常——>都会回调到mNetWorkChangeReceiver内)
         * enableAutomaticReconnection还是先禁用smack内部的自动重连吧
         */
        mReconnectionManager.disableAutomaticReconnection();
    }

    private void conflict() {
        ((CoreService) mContext).logout();
        MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
        // 弹出对话框
        UserCheckedActivity.start(mContext);
        // LoginHelper.broadcastConflict(mContext);
    }

    /**
     * @param e
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        if (e instanceof StreamErrorException) {// 重复登录
            StreamErrorException streamErrorException = (StreamErrorException) e;
            StreamError streamError = streamErrorException.getStreamError();

            if (streamError.getCondition().equals(StreamError.Condition.conflict)) {// 下线通知
                if (CoreService.DEBUG)
                    Log.d(CoreService.TAG, "异常断开，有另外设备登陆啦");
                conflict();
            }
        }
    }

    void release() {
        if (mReconnectionManager != null) {// 停止重连
            mReconnectionManager.disableAutomaticReconnection();
        }
    }
}
