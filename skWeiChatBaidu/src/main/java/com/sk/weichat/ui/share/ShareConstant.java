package com.sk.weichat.ui.share;

/**
 * 登录、分享sdk
 */
public class ShareConstant {
    // 与外部通信使用
    public static final String EXTRA_SHARE_CONTENT = "extra_share_content";
    public static final String EXTRA_AUTHORIZATION_RESULT = "extra_authorization_result";

    // 仅限本地使用
    public static boolean IS_SHARE_L_COME = false;
    public static boolean IS_SHARE_S_COME = false;

    public static String ShareContent;

    // 外部浏览器调起 授权、支付
    public static boolean IS_SHARE_QL_COME = false;
    public static boolean IS_SHARE_QP_COME = false;
}
