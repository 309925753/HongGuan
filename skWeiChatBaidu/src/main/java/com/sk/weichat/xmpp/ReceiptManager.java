package com.sk.weichat.xmpp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.event.MessageSendChat;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.NewFriendMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.login.MachineDao;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.me.sendgroupmessage.ChatActivityForSendGroup;
import com.sk.weichat.util.log.LogUtils;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.sk.weichat.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.Jid;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.sk.weichat.xmpp
 * @作者:王阳
 * @创建时间: 2015年10月15日 下午5:04:34
 * @描述: 消息回执的处理
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: TODO
 */
public class ReceiptManager {
    public static final int MESSAGE_DELAY = 20 * 1000; // 等待消息回执， 超时时间
    private static final int RECEIPT_NO = 0x1; // 没有收到回执
    private static final int RECEIPT_YES = 0x2;// 收到回执
    /**
     * 处理消息回执
     */
    public static Map<String, ReceiptObj> mReceiptMap = new HashMap<String, ReceiptObj>();
    private CoreService mService;
    private XMPPTCPConnection mConnection;
    private String mLoginUserId;// 用于切换用户后，判断是否清除回执内容
    /**
     * 重发次数表
     */
    private Map<String, Integer> mReSendMap = new HashMap<String, Integer>();
    @SuppressLint("HandlerLeak")
    private Handler mReceiptMapHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            String packetId = (String) msg.obj;
            if (TextUtils.isEmpty(packetId)) {
                return;
            }

            ReceiptObj obj = mReceiptMap.get(packetId); // 接收到message先从map中去除对应键值的消息
            if (obj == null || obj.msg == null || obj.toUserId == null) {// 没有取出表示没有该消息
                return;
            }

            if (msg.what == RECEIPT_NO) { // 认为这条消息未发送成功,已超时
                if (obj.sendType == SendType.NORMAL) {
                    ChatMessage chat = (ChatMessage) obj.msg;
                    int index = 0;
                    if (chat.getType() == 26) {
                        LogUtils.e("TAG", "已读消息发送失败:" + packetId);
                        if (mReSendMap.containsKey(packetId)) {
                            index = mReSendMap.get(packetId);
                        } else {
                            index = chat.getReSendCount();
                        }
                    } else {
                        LogUtils.e("TAG", "普通消息发送失败:" + packetId);
                        if (mReSendMap.containsKey(packetId)) {
                            index = mReSendMap.get(packetId);
                        } else {
                            index = chat.getReSendCount(); // 3
                        }
                    }

                    LogUtils.e("TAG", "消息自动重发剩余次数:" + index);
                    if (index > 0) {// 在这里把发送失败的消息在发送一次
                        mReSendMap.put(packetId, index - 1);
                        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getInstance()).getUserId(), obj.toUserId);
                        if (friend != null && friend.getRoomFlag() != 0) {
                            EventBus.getDefault().post(new MessageSendChat(true, obj.toUserId, chat));  // @see MainActivity
                        } else {
                            EventBus.getDefault().post(new MessageSendChat(false, obj.toUserId, chat)); // @see MainActivity
                        }
                        return;
                    } else {// 重发结束，发送失败
                        ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, obj.toUserId, chat.getPacketId(),
                                ChatMessageListener.MESSAGE_SEND_FAILED);
                    }
                    mReceiptMap.remove(packetId);
                } else {
                    ListenerManager.getInstance().notifyNewFriendSendStateChange(obj.toUserId, ((NewFriendMessage) obj.msg),
                            ChatMessageListener.MESSAGE_SEND_FAILED);
                    mReceiptMap.remove(packetId);
                }
            } else if (msg.what == RECEIPT_YES) {// 认为发送成功
                if (obj.Read == 1) { // 已读消息 Type==26
                    LogUtils.e("TAG", "已读消息发送成功: " + obj.Read_msg_pid + " to " + obj.toUserId + "修改本地");
                    if (mLoginUserId.equals(obj.toUserId)) {
                        for (String s : MyApplication.machine) {
                            ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, s, obj.Read_msg_pid, true);  // 传入的 packetId是被回执的消息的packetId
                        }
                    } else {
                        ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, obj.toUserId, obj.Read_msg_pid, true); // 传入的 packetId是被回执的消息的packetId
                    }
                } else {
                    LogUtils.e("TAG", "普通消息发送成功: " + packetId);
                    if (obj.sendType == SendType.NORMAL) {
                        ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, obj.toUserId, ((ChatMessage) obj.msg).getPacketId(),
                                ChatMessageListener.MESSAGE_SEND_SUCCESS);

                        if (ChatActivityForSendGroup.isAlive) {
                            // 收到消息回执，通知消息群发页面
                            EventBus.getDefault().post(new MessageEvent(obj.toUserId));
                        }
                    } else {
                        ListenerManager.getInstance().notifyNewFriendSendStateChange(obj.toUserId, ((NewFriendMessage) obj.msg),
                                ChatMessageListener.MESSAGE_SEND_SUCCESS);
                    }
                }
            }
            mReceiptMap.remove(packetId);
        }
    };

    ReceiptReceivedListener mReceiptReceivedListener = new ReceiptReceivedListener() {
        @Override
        public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
            mReceiptMapHandler.removeMessages(RECEIPT_NO, receiptId);
            android.os.Message handlerMsg = mReceiptMapHandler.obtainMessage(RECEIPT_YES);
            handlerMsg.obj = receiptId;
            mReceiptMapHandler.sendMessage(handlerMsg);

            Log.e("msg", "收到消息回执:fromJid=" + fromJid + "-----toJid=" + toJid + "-----receiptId=" + receiptId);
            if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {
                // 1.因为现在都是服务端代发回执，且回执的from不带resource，所以基本都会走到Exception内
                // 2.type==200的检测消息除了服务端发送回执之后，客户端收到之后也会发送回执，且客户端发送的from的带有resource
                // 3.所以这里基本是处理type==200消息的回执的地方
                try {
                    String from = fromJid.toString().substring(0, fromJid.toString().indexOf("/"));
                    if (toJid.toString().contains(from)) {
                        String resource = fromJid.toString().substring(fromJid.toString().indexOf("/") + 1, fromJid.length());
                        MachineDao.getInstance().updateMachineOnLineStatus(resource, true);
                    }
                } catch (Exception e) {
                    Log.e("msg", "updateMachineOnLineStatus Failed");
                }
            }
        }
    };

    public ReceiptManager(CoreService service, XMPPTCPConnection connection) {
        mService = service;
        mConnection = connection;
        mLoginUserId = XmppStringUtil.parseName(mConnection.getUser().toString());

        DeliveryReceiptManager mDeliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(mConnection);
        // 自动发送消息回执
        mDeliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.disabled);// 现在都是由服务端代发回执，客户端就不发回执了
        // 添加消息回执监听
        mDeliveryReceiptManager.addReceiptReceivedListener(mReceiptReceivedListener);//mReceiptReceivedListener);//);
    }

    /* 添加一个即将发送的消息 */
    public void addWillSendMessage(final String toUserId
            , final XmppMessage xmppMessage, SendType sendType, String content) {

        // 将之前可能存在的回执缓存清除掉
        if (mReceiptMap.containsKey(xmppMessage.getPacketId())) {
            ReceiptObj oldObj = mReceiptMap.get(xmppMessage.getPacketId());
            mReceiptMapHandler.removeMessages(RECEIPT_NO, oldObj);
            mReceiptMap.remove(xmppMessage.getPacketId());
        }

        int type = xmppMessage.getType(); // 消息类型

        // 将这个回执对象缓存起来，这样在接到回执的时候容易定位到是发给谁的哪条消息
        ReceiptObj obj = new ReceiptObj();
        obj.toUserId = toUserId;
        obj.msg = xmppMessage;
        obj.sendType = sendType;
        obj.Read = (type == XmppMessage.TYPE_READ) ? 1 : 0; // 判断类型
        obj.Read_msg_pid = content;
        mReceiptMap.put(xmppMessage.getPacketId(), obj);// 记录一条新发送出去的消息(还没有接收到回执)

        android.os.Message handlerMsg = mReceiptMapHandler.obtainMessage(RECEIPT_NO); // 默认先标记没有接收到回执
        handlerMsg.obj = xmppMessage.getPacketId();

        mReceiptMapHandler.sendMessageDelayed(handlerMsg, MESSAGE_DELAY);// 延迟二十秒发送

        if (xmppMessage instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) xmppMessage;
            Log.e("msg", "产生一条消息，等待回执..." + "xmppMessage.getPacketId()--->" + xmppMessage.getPacketId()
                    + " ，chatMessage.getPacketId()--->" + chatMessage.getPacketId() + " ，type--->" + chatMessage.getType()
                    + " ，content--->" + chatMessage.getContent());
        }

    }

    public void reset() {
        String userId = XmppStringUtil.parseName((mConnection.getUser().toString()));
        if (!mLoginUserId.equals(userId)) {
            mLoginUserId = userId;
            mReceiptMapHandler.removeCallbacksAndMessages(null);
            mReceiptMap.clear();
        }
    }

    /**
     * 发送的消息类型，用于类型判断和消息回执的分发
     */
    public enum SendType {
        NORMAL, PUSH_NEW_FRIEND
    }

    class ReceiptObj {
        String toUserId;// 普通消息和新朋友消息公用
        XmppMessage msg;// 用于普通消息和新朋友消息公用
        SendType sendType;// 用于分发普通消息和新朋友消息的回执
        int Read;// 用于标记此消息是否为已读回执消息
        String Read_msg_pid;// 被回执消息的packetId
    }
}
