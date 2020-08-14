package com.sk.weichat.ui.message;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.EventSecureNotify;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.PublicKey;
import com.sk.weichat.bean.PublicKeyServer;
import com.sk.weichat.bean.event.MessageSendChat;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.MucRoom;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.PublicKeyDao;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.DH;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.xmpp.ListenerManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class HandleSecureChatMessage {
    private static Map<String, List<ChatMessage>> verifySignatureFailedMsgMap = new HashMap<>();
    private static Map<String, Boolean> getUserPublicKeyListFromServerFlagMap = new HashMap<>();

    /**
     * @param chatMessage
     */
    public static void distributionChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == XmppMessage.TYPE_SECURE_REFRESH_KEY) {
            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getFromUserId());
            String[] split = chatMessage.getContent().split(",");
            if (friend != null && split.length >= 2) {
                Log.e("msg", "distributionChatMessage success");
                FriendDao.getInstance().updatePublicKeyDH(friend.getUserId(), split[0]);
                FriendDao.getInstance().updatePublicKeyRSARoom(friend.getUserId(), split[1]);
            }
        } else if (chatMessage.getType() == XmppMessage.TYPE_SECURE_SEND_KEY) {
            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getObjectId());
            if (friend != null) {
                updateSelfChatKeyGroup(friend.getRoomId(), chatMessage.getContent());
            } else {
                Log.e("msg", "distributionChatMessage failed");
            }
        } else if (chatMessage.getType() == XmppMessage.TYPE_SECURE_NOTIFY_REFRESH_KEY) {// 群主重置chatKey
            Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getObjectId());
            // 1.缓冲区内改群组的消息全部清空，数据库内该群的消息全部删除，通知聊天界面刷新
            verifySignatureFailedMsgMap.remove(friend.getUserId());
            ChatMessageDao.getInstance().deleteMessageTable(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), friend.getUserId());
            EventBus.getDefault().post(new EventSecureNotify(EventSecureNotify.MULTI_SNED_RESET_KEY_MSG, chatMessage));
            // 2.通知到群组内
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(MyApplication.getContext().getString(R.string.group_owner_reset_chat_key));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getObjectId(), chatMessage, true);
            }
            // 3.调用接口获取新的通信密钥
            getUserPublicKeyListFromServerFlagMap.put(friend.getUserId(), true);
            getFriendChatKeyFromServe(friend.getUserId(), friend.getRoomId());
        }
    }

    /**
     * 处理单聊验签失败的消息
     *
     * @param chatMessage
     */
    public static void handleVerifySignatureFailedMsg(ChatMessage chatMessage) {
        String userId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        Friend friend;
        if (TextUtils.equals(userId, chatMessage.getFromUserId())) {
            friend = FriendDao.getInstance().getFriend(userId, chatMessage.getToUserId());
        } else {
            friend = FriendDao.getInstance().getFriend(userId, chatMessage.getFromUserId());
        }
        if (friend != null) {
            boolean isVerifySignatureSuccess = false;
            List<PublicKey> keys = PublicKeyDao.getInstance().getAllPublicKeys(userId, friend.getUserId());
            for (int i = 0; i < keys.size(); i++) {
                String key = DH.getCommonSecretKeyBase64(SecureChatUtil.getDHPrivateKey(userId), keys.get(i).getPublicKey());
                String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                // 本地根据签名规则生成签名
                String signature = SecureChatUtil.getSignatureSingle(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                        chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                        chatMessage.getContent());
                if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                    Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt success");
                    //  验签成功，解密重新赋值
                    isVerifySignatureSuccess = true;
                    chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey)));
                    chatMessage.setIsEncrypt(0);
                    break;
                }
            }
            if (!isVerifySignatureSuccess) {
                Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt failed, wait...");
                // 本地好友dh公钥表内公钥参与运算后均无法验签成功，将消息放入map集合内，从服务器获取好友dh公钥
                List<ChatMessage> chatMessages = getChatMessages(friend.getUserId());
                chatMessages.add(chatMessage);
                verifySignatureFailedMsgMap.put(friend.getUserId(), chatMessages);
                if (!getUserPublicKeyListFromServerFlagMap.containsKey(friend.getUserId())
                        || !getUserPublicKeyListFromServerFlagMap.get(friend.getUserId())) {// 当前未在调用获取该好友的dh公钥列表接口，可以调用，否则等待接口调用完成
                    Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt failed, start loading");
                    getUserPublicKeyListFromServerFlagMap.put(friend.getUserId(), true);
                    getFriendPublicKeyListFromServe(friend.getUserId());
                } else {
                    Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt failed, loading...");
                }
            }
        } else {
            // todo 好友不存在，理论上不太可能，因为在外面就判断了
            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt, but friend ==null");
        }

    }

    private static void getFriendPublicKeyListFromServe(String friendId) {
        String ownerId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", friendId);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getContext()).AUTHKEYS_GET_DHMSG_KEY_LIST)
                .params(params)
                .build()
                .execute(new BaseCallback<PublicKeyServer>(PublicKeyServer.class) {
                    @Override
                    public void onResponse(ObjectResult<PublicKeyServer> result) {
                        if (result.getResultCode() == 1) {
                            List<PublicKeyServer.PublicKeyList> publicKeyServers = result.getData().getPublicKeyList();
                            List<PublicKey> publicKeys = new ArrayList<>();
                            for (int i = 0; i < publicKeyServers.size(); i++) {
                                PublicKey publicKey = new PublicKey();
                                publicKey.setOwnerId(ownerId);
                                publicKey.setUserId(result.getData().getUserId());
                                publicKey.setPublicKey(publicKeyServers.get(i).getKey());
                                publicKey.setKeyCreateTime(publicKeyServers.get(i).getTime());
                                publicKeys.add(publicKey);
                            }
                            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe success, reprocessing");
                            // 更新PublicKey
                            PublicKeyDao.getInstance().refreshPublicKeys(ownerId, result.getData().getUserId(), publicKeys);
                            // 如下载成功，待本地更新在移除该flag
                            getUserPublicKeyListFromServerFlagMap.remove(result.getData().getUserId());
                            // 取出map内当前用户未验签成功的消息，移除当前用户的chatMessages
                            List<ChatMessage> chatMessages = getChatMessages(result.getData().getUserId());
                            verifySignatureFailedMsgMap.remove(result.getData().getUserId());
                            // 再次取出该用户公钥表，遍历
                            List<PublicKey> keys = PublicKeyDao.getInstance().getAllPublicKeys(ownerId, result.getData().getUserId());
                            boolean isVerifySignatureSuccessAgain = false;
                            for (int i = 0; i < chatMessages.size(); i++) {
                                ChatMessage chatMessage = chatMessages.get(i);
                                for (int i1 = 0; i1 < keys.size(); i1++) {
                                    String key = DH.getCommonSecretKeyBase64(SecureChatUtil.getDHPrivateKey(ownerId), keys.get(i1).getPublicKey());
                                    String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                                    // 本地根据签名规则生成签名
                                    String signature = SecureChatUtil.getSignatureSingle(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                                            chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                                            chatMessage.getContent());
                                    if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                                        Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：reprocessing success");
                                        isVerifySignatureSuccessAgain = true;
                                        // 解密得到明文内容，在直接加密放入content字段内
                                        String content = AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey));
                                        String saveContent = AES.encryptBase64(content, Base64.decode(SecureChatUtil.getSymmetricKey(chatMessage.getPacketId())));
                                        chatMessage.setContent(saveContent);
                                        chatMessage.setIsEncrypt(0);
                                        // 异步进行，已经存到数据库内了，需要更新数据库
                                        ChatMessageDao.getInstance().updateVerifySignatureFailedMsg(ownerId, result.getData().getUserId(),
                                                chatMessage.getPacketId(), saveContent);
                                        break;
                                    } else {
                                        // todo 消息还是验签失败，可能遭遇第三方恶意篡改重要字段，基本已经解不开了，可考虑直接从数据库内删除掉该条消息[服务端上的记录也需要删除]
                                    }
                                }
                            }
                            if (isVerifySignatureSuccessAgain) {
                                // 因为从服务器获取好友公钥列表为异步，需要发送通知更新消息界面与聊天界面
                                Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：isVerifySignatureSuccessAgain,notify");
                                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getContext());
                            }
                        } else {
                            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：" + result.getResultMsg());
                            // getUserPublicKeyListFromServerFlagMap.remove(result.getData().getUserId());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：onError");
                        getUserPublicKeyListFromServerFlagMap.put(friendId, false);
                    }
                });
    }

    /**
     * 处理群组验签失败的消息
     *
     * @param chatMessage
     */
    public static void handleVerifySignatureFailedMsgGroup(ChatMessage chatMessage) {
        String userId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        Friend friend = FriendDao.getInstance().getFriend(userId, chatMessage.getToUserId());
        if (friend != null) {
            // 群组只有一个chatKey，解不了直接将消息放入缓冲队列内
            List<ChatMessage> chatMessages = getChatMessages(friend.getUserId());
            chatMessages.add(chatMessage);
            verifySignatureFailedMsgMap.put(friend.getUserId(), chatMessages);
            if (!getUserPublicKeyListFromServerFlagMap.containsKey(friend.getUserId())
                    || !getUserPublicKeyListFromServerFlagMap.get(friend.getUserId())) {// 当前未在调用获取该好友的dh公钥列表接口，可以调用，否则等待接口调用完成
                Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt failed, start loading");
                getUserPublicKeyListFromServerFlagMap.put(friend.getUserId(), true);
                getFriendChatKeyFromServe(friend.getUserId(), friend.getRoomId());
            } else {
                Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt failed, loading...");
            }
        } else {
            // todo 好友不存在，理论上不太可能，因为在外面就判断了
            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt, but friend ==null");
        }
    }

    /**
     * 获取当前群组最新的消息密钥
     *
     * @param roomId
     */
    private static void getFriendChatKeyFromServe(String friendId, String roomId) {
        String ownerId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        HashMap<String, String> params = new HashMap<>();
        params.put("roomId", roomId);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getContext()).ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {
                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        if (result.getResultCode() == 1) {
                            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendChatKeyFromServe success, reprocessing");
                            MucRoom mucRoom = result.getData();
                            boolean isUpdate = false;
                            if (mucRoom.getIsSecretGroup() == 1 && mucRoom.getMember() != null) {
                                try {
                                    String chatKey = new String(RSA.decryptFromBase64(mucRoom.getMember().getChatKeyGroup(), Base64.decode(SecureChatUtil.getRSAPrivateKey(ownerId))));
                                    FriendDao.getInstance().updateChatKeyGroup(mucRoom.getJid(), SecureChatUtil.encryptChatKey(mucRoom.getJid(), chatKey));
                                    isUpdate = true;
                                    Log.e("msg", "设置chatKey成功-->" + chatKey);
                                } catch (Exception e) {
                                    Log.e("msg", "设置chatKey失败");
                                    FriendDao.getInstance().updateIsLostChatKeyGroup(mucRoom.getJid(), 1);
                                }
                            }
                            // 如下载成功，待本地更新在移除该flag
                            getUserPublicKeyListFromServerFlagMap.remove(friendId);
                            if (isUpdate) {
                                // 取出map内当前用户未验签成功的消息，移除当前用户的chatMessages
                                List<ChatMessage> chatMessages = getChatMessages(friendId);
                                verifySignatureFailedMsgMap.remove(friendId);
                                Friend friend = FriendDao.getInstance().getFriend(ownerId, friendId);
                                boolean isVerifySignatureSuccessAgain = false;
                                for (int i = 0; i < chatMessages.size(); i++) {
                                    ChatMessage chatMessage = chatMessages.get(i);
                                    String key = SecureChatUtil.decryptChatKey(chatMessage.getToUserId(), friend.getChatKeyGroup());
                                    String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                                    // 本地根据签名规则生成签名
                                    String signature = SecureChatUtil.getSignatureMulti(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                                            chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                                            chatMessage.getContent());
                                    // 对比消息签名
                                    if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                                        Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：reprocessing success");
                                        isVerifySignatureSuccessAgain = true;
                                        // 解密得到明文内容，在直接加密放入content字段内
                                        String content = AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey));
                                        String saveContent = AES.encryptBase64(content, Base64.decode(SecureChatUtil.getSymmetricKey(chatMessage.getPacketId())));
                                        chatMessage.setContent(saveContent);
                                        chatMessage.setIsEncrypt(0);
                                        // 异步进行，已经存到数据库内了，需要更新数据库
                                        ChatMessageDao.getInstance().updateVerifySignatureFailedMsg(ownerId, friendId,
                                                chatMessage.getPacketId(), saveContent);
                                    } else {
                                        Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：reprocessing fail");
                                        // todo 消息还是验签失败，可能遭遇第三方恶意篡改重要字段，基本已经解不开了，可考虑直接从数据库内删除掉该条消息[服务端上的记录也需要删除]
                                    }
                                }
                                if (isVerifySignatureSuccessAgain) {
                                    // 因为从服务器获取好友公钥列表为异步，需要发送通知更新消息界面与聊天界面
                                    Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendPublicKeyListFromServe：isVerifySignatureSuccessAgain,notify");
                                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getContext());
                                }
                            }
                        } else {
                            Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendChatKeyFromServe：" + result.getResultMsg());
                            getUserPublicKeyListFromServerFlagMap.remove(friendId);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("msg", "msg dao decrypt isEncrypt==3 local keys decrypt--->getFriendChatKeyFromServe：onError");
                        getUserPublicKeyListFromServerFlagMap.put(friendId, false);
                    }
                });
    }

    private static List<ChatMessage> getChatMessages(String friendId) {
        List<ChatMessage> chatMessages = verifySignatureFailedMsgMap.get(friendId);
        if (chatMessages == null) {
            chatMessages = new ArrayList<>();
        }
        return chatMessages;
    }

    /**
     * 将群组置为isLostChatKeyGroup状态，发送804消息请求chatKeyGroup
     *
     * @param isDelay: 当前正在创建群组，给一个delay缓冲下
     * @param roomJid
     */
    public static void sendRequestChatKeyGroupMessage(boolean isDelay, String roomJid) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_SECURE_LOST_KEY);
        chatMessage.setFromUserId(CoreManager.requireSelf(MyApplication.getContext()).getUserId());
        chatMessage.setFromUserName(CoreManager.requireSelf(MyApplication.getContext()).getNickName());
        chatMessage.setToUserId(roomJid);
        String signature = RSA.signBase64(roomJid,
                Base64.decode(SecureChatUtil.getRSAPrivateKey(CoreManager.requireSelf(MyApplication.getContext()).getUserId())));
        chatMessage.setContent(signature);
        chatMessage.setObjectId(roomJid);
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        if (isDelay) {
            Handler handler = new Handler(Looper.getMainLooper());// 子线程中创建handler时，需要先prepare Looper || 获取到主线程的Looper
            handler.postDelayed(() -> EventBus.getDefault().post(new MessageSendChat(true, roomJid, chatMessage)), 1000);
        } else {
            EventBus.getDefault().post(new MessageSendChat(true, roomJid, chatMessage));
        }
    }

    /**
     * click 804 msg, send 805 msg for requested member
     *
     * @param chatMessage
     */
    public static void sendChatKeyForRequestedMember(ChatMessage chatMessage) {
        // 先从服务器内得到请求key用户的rsa公钥
        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getToUserId());
        HashMap<String, String> params = new HashMap<>();
        params.put("roomId", friend.getRoomId());
        params.put("userId", chatMessage.getFromUserId());

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getContext()).ROOM_GET_MEMBER_RSA_PUBLIC_KEY)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (Result.checkSuccess(MyApplication.getContext(), result)) {
                            // 验签，判断是否为群成员请求
                            try {
                                JSONObject jsonObject = new JSONObject(result.getData());
                                String publicKey = jsonObject.getString("rsaPublicKey");
                                if (RSA.verifyFromBase64(chatMessage.getObjectId(), Base64.decode(publicKey), chatMessage.getContent())) {
                                    // 验签通过，发送一条805的消息给请求方，在发送一条805的消息到群组内，将请求标记为已处理
                                    EventBus.getDefault().post(new MessageSendChat(false, chatMessage.getFromUserId(),
                                            createMessage(chatMessage, friend.getChatKeyGroup(), publicKey, false)));
                                    EventBus.getDefault().post(new MessageSendChat(true, chatMessage.getToUserId(),
                                            createMessage(chatMessage, friend.getChatKeyGroup(), publicKey, true)));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private static ChatMessage createMessage(ChatMessage message, String chatKeyGroup, String publicKey,
                                             boolean isGroup) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_SECURE_SEND_KEY);
        chatMessage.setFromUserId(CoreManager.requireSelf(MyApplication.getContext()).getUserId());
        chatMessage.setFromUserName(CoreManager.requireSelf(MyApplication.getContext()).getNickName());
        chatMessage.setToUserId(isGroup ? message.getToUserId() : message.getFromUserId());
        if (isGroup) {
            // 上一条消息的msgId
            chatMessage.setContent(message.getPacketId());
        } else {
            // 请求放公钥加密的chaKey
            String chatKey = SecureChatUtil.decryptChatKey(message.getToUserId(), chatKeyGroup);
            chatMessage.setContent(RSA.encryptBase64(chatKey.getBytes(), Base64.decode(publicKey)));
        }
        chatMessage.setGroup(isGroup);
        chatMessage.setObjectId(message.getToUserId());
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        return chatMessage;
    }

    /**
     * 更新自己的chatKey
     *
     * @param roomId
     */
    private static void updateSelfChatKeyGroup(String roomId, String key) {
        HashMap<String, String> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("key", key);

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getContext()).UPDETE_GROUP_CHAT_KEY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }
}
