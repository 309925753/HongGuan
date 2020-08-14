package fm.jiecao.jcvideoplayer_lib;

/**
 * Created by Administrator on 2017/3/17 0017.
 * 1. 视频播放控件 已准备，通知朋友圈页面停止播放录音，防止同时播放两种声音
 * 2. 我的同事 其他页面调用api成功，通知同事页面刷新UI
 * 3. 消息群发 收到回执后，通知群发页面，当群发页面收到所有人回执时，在隐藏等待符
 * 4. 视频播放完成，通知单聊界面，判断是否为阅后即焚视频
 */

public class MessageEvent {
    public final String message;

    public MessageEvent(String message) {
        this.message = message;
    }
}