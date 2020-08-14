package com.sk.weichat.util;

/**
 * 回调
 *
 * @version 1.0
 * <p>
 * Created by LeoTesla on 2017/2/25.
 */

public interface QrCodeCallback<T> {

    /**
     * 完成
     *
     * @param success 状态
     * @param ret     结果
     */
    void onComplete(boolean success, T ret);

}
