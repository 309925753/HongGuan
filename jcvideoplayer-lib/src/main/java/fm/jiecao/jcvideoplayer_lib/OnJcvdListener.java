package fm.jiecao.jcvideoplayer_lib;

/**
 * Created by xuan  改进jcv的通知回调方式
 * On 2016/04/04 22:13
 */
public interface OnJcvdListener {

    void onPrepared(); // 加载完成的回调

    void onCompletion(); // 播放完成的回调

    void onError(); // 播放出错

    void onPause(); // 播放暂停

    void onReset(); // 重置播放器
}
