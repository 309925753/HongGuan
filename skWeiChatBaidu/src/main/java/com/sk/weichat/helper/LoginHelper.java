package com.sk.weichat.helper;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.LoginRegisterResult;
import com.sk.weichat.bean.User;
import com.sk.weichat.db.dao.UserDao;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

/**
 * 当前登陆用户的帮助类
 */
public class LoginHelper {
    public static final String ACTION_LOGIN = AppConfig.sPackageName + ".action.login";  // 登陆
    public static final String ACTION_LOGOUT = AppConfig.sPackageName + ".action.logout";// 用户手动注销登出
    public static final String ACTION_CONFLICT = AppConfig.sPackageName + ".action.conflict";          // 登陆冲突（另外一个设备登陆了）
    // 用户需要重新登陆，更新本地数据（可能是STATUS_USER_TOKEN_OVERDUE，STATUS_USER_NO_UPDATE，STATUS_USER_TOKEN_CHANGE三种状态之一）
    public static final String ACTION_NEED_UPDATE = AppConfig.sPackageName + ".action.need_update";
    public static final String ACTION_LOGIN_GIVE_UP = AppConfig.sPackageName + ".action.login_give_up";// 在下载资料的时候，没下载完就放弃登陆了
    /* 信息完整程度由低到高，从第2级别开始，MyApplication中的mLoginUser是有值得 */
    /* 没有用户，即游客（不需要进行其他操作） */
    public static final int STATUS_NO_USER = 0;
    /* 有用户，但是不完整，只有手机号，可能是之前注销过（不需要进行其他操作） */
    public static final int STATUS_USER_SIMPLE_TELPHONE = 1;
    /* 有用户，但是本地Token已经过期了（需要弹出对话框提示：本地Token已经过期，重新登陆） */
    public static final int STATUS_USER_TOKEN_OVERDUE = 2;
    /*
     * 有用户，本地Token未过期，但是可能信息不是最新的，即在上次登陆之后，没有更新完数据就退出了app （需要检测Token是否变更，变更即提示登陆，未变更则提示更新资料）
     */
    public static final int STATUS_USER_NO_UPDATE = 3;
    /*
     * 用户资料全部完整，但是Token已经变更了，提示重新登陆
     */
    public static final int STATUS_USER_TOKEN_CHANGE = 4;
    /*
     * 用户资料全部完整，但是还要检测Token是否变更 （需要检测Token是否变更，变更即提示登陆） 此状态比较特殊，因为有可能Token没有变更，不需要在进行登陆操作。<br/> 在检测Token接口调用失败的情况下，默认为一个不需要重新登陆的用户。
     * 在检测Token接口调用成功的情况下，此状态会立即过度到STATUS_USER_VALIDATION状态。
     */
    public static final int STATUS_USER_FULL = 5;
    /*
     * 用户资料全部完整，并且已经检测Token没有变更，或者新登录更新完成 （不需要进行其他操作）
     */
    public static final int STATUS_USER_VALIDATION = 6;
    private static final String TAG = "LoginHelper";

    // 获取登陆和登出的action filter
    public static IntentFilter getLogInOutActionFilter() {
        Log.d(TAG, "getLogInOutActionFilter() called");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOGIN);
        intentFilter.addAction(ACTION_LOGOUT);
        intentFilter.addAction(ACTION_CONFLICT);
        intentFilter.addAction(ACTION_NEED_UPDATE);
        intentFilter.addAction(ACTION_LOGIN_GIVE_UP);
        return intentFilter;
    }

    // 登陆广播,且登陆的用户为MyApplication.getInstance().mLoginUser
    public static void broadcastLogin(Context context) {
        Log.d(TAG, "broadcastLogin() called with: context = [" + context + "]");
        if (context == null) {// 防止传入Context对象的界面被销毁导致context为空
            context = MyApplication.getContext();
        }
        Intent intent = new Intent(ACTION_LOGIN);
        context.sendBroadcast(intent);
    }

    // 登出广播
    public static void broadcastLogout(Context context) {
        Log.d(TAG, "broadcastLogout() called with: context = [" + context + "]");
        if (context == null) {// 防止传入Context对象的界面被销毁导致context为空
            context = MyApplication.getContext();
        }
        Intent intent = new Intent(ACTION_LOGOUT);
        context.sendBroadcast(intent);
    }

    // 放弃登陆
    public static void broadcastLoginGiveUp(Context context) {
        Log.d(TAG, "broadcastLoginGiveUp() called with: context = [" + context + "]");
        if (context == null) {// 防止传入Context对象的界面被销毁导致context为空
            context = MyApplication.getContext();
        }
        Intent intent = new Intent(ACTION_LOGIN_GIVE_UP);
        context.sendBroadcast(intent);
    }

    // 登陆冲突（另外一个设备登陆了）
    public static void broadcastConflict(Context context) {
        Log.d(TAG, "broadcastConflict() called with: context = [" + context + "]");
        if (context == null) {// 防止传入Context对象的界面被销毁导致context为空
            context = MyApplication.getContext();
        }
        Intent intent = new Intent(ACTION_CONFLICT);
        context.sendBroadcast(intent);
    }

    /* 进入MainActivity，判断当前是游客，还是之前已经登陆过的用户 */
    public static int prepareUser(Context context, CoreManager coreManager) {
        Log.d(TAG, "prepareUser() called with: context = [" + context + "]");
        boolean idIsEmpty = TextUtils.isEmpty(UserSp.getInstance(context).getUserId(""));

        int userStatus;
        if (!idIsEmpty && UserSp.getInstance(context).isLogged()) {// 用户标识都不为空，那么就能代表一个完整的用户
            // 进入之前，加载本地已经存在的数据
            User user = coreManager.getSelf();
            if (user == null) {
                String userId = UserSp.getInstance(context).getUserId("");
                user = UserDao.getInstance().getUserByUserId(userId);
            }

            if (!LoginHelper.isUserValidation(user)) {// 用户数据错误,那么就认为是一个游客
                userStatus = STATUS_NO_USER;
            } else {
                coreManager.setSelf(user);

                if (LoginHelper.isTokenValidation()) {// Token未过期
                    boolean isUpdate = UserSp.getInstance(context).isUpdate(true);
                    if (isUpdate) {
                        userStatus = STATUS_USER_FULL;
                    } else {
                        userStatus = STATUS_USER_NO_UPDATE;
                    }
                } else {// Token过期
                    userStatus = STATUS_USER_TOKEN_OVERDUE;
                }
            }
        } else if (!idIsEmpty) {// （适用于切换账号之后的操作）手机号不为空
            userStatus = STATUS_USER_SIMPLE_TELPHONE;
        } else {
            userStatus = STATUS_NO_USER;
        }
        MyApplication.getInstance().mUserStatus = userStatus;
        Log.d(TAG, "prepareUser() returned: " + userStatus);
        return userStatus;
    }

    // User数据是否能代表一个有效的用户
    public static boolean isUserValidation(User user) {
        if (user == null) {
            return false;
        }
        if (TextUtils.isEmpty(user.getUserId())) {
            return false;
        }
        if (TextUtils.isEmpty(user.getTelephone())) {
            return false;
        }
        if (TextUtils.isEmpty(user.getPassword())) {
            return false;
        }
        if (TextUtils.isEmpty(user.getNickName())) {
            return false;
        }
        return true;
    }

    /**
     * AccessToken 是否是有效的
     *
     * @return
     */
    public static boolean isTokenValidation() {
        Log.d(TAG, "isTokenValidation() called");
        return !TextUtils.isEmpty(UserSp.getInstance(MyApplication.getContext()).getAccessToken());
    }

    public static boolean setLoginUser(Context context, CoreManager coreManager, String telephone, String password, com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
        Log.d(TAG, "setLoginUser() called with: context = [" + context + "], telephone = [" + telephone + "], password = [" + password + "], result = [" + result + "]");
        if (result == null) {
            return false;
        }
        if (result.getResultCode() != Result.CODE_SUCCESS) {
            return false;
        }
        if (result.getData() == null) {
            return false;
        }
        String sAreaCode = result.getData().getAreaCode();
        try {
            int areaCode;
            areaCode = Integer.valueOf(result.getData().getAreaCode());
            PreferenceUtils.putInt(context, Constants.AREA_CODE_KEY, areaCode);
        } catch (Exception e) {
            LogUtils.log("没有areaCode");
        }
        try {
            String rPassword = result.getData().getPassword();
            if (!TextUtils.isEmpty(rPassword)) {
                password = rPassword;
            }
        } catch (Exception e) {
            LogUtils.log("没有password");
        }
        try {
            String rTelephone = result.getData().getTelephone();
            if (!TextUtils.isEmpty(rTelephone)) {
                telephone = rTelephone;
                if (!TextUtils.isEmpty(sAreaCode) && telephone.startsWith(sAreaCode)) {
                    telephone = telephone.substring(sAreaCode.length());
                }
            }
        } catch (Exception e) {
            LogUtils.log("没有telephone");
        }

        // 保存当前登陆的用户信息和Token信息作为全局变量，方便调用
        User user = coreManager.getSelf();
        if (user == null) {
            user = new User();
        }
        user.setTelephone(telephone);
        user.setPassword(password);
        user.setUserId(result.getData().getUserId());
        user.setNickName(result.getData().getNickName());
        user.setRole(result.getData().getRole());
        user.setAccount(result.getData().getAccount());
        user.setSetAccountCount(result.getData().getSetAccountCount());
        user.setMyInviteCode(result.getData().getMyInviteCode());
        if (!LoginHelper.isUserValidation(user)) {
            // 请求下来的用户数据不完整有错误
            return false;
        }
        if (result.getData().getLogin() != null) {
            // 保存该用户上次离线的时间，通过当前时间与离线时间的时间差，获取该时间段内群组内的离线消息
            Constants.OFFLINE_TIME_IS_FROM_SERVICE = true;
            CoreManager.saveOfflineTime(context, result.getData().getUserId(), result.getData().getLogin().getOfflineTime());
        }
        // 保存基本信息到数据库
        boolean saveAble = UserDao.getInstance().saveUserLogin(user);
        if (!saveAble) {
            return false;
        }
        // 保存最后一次登录的用户信息到Sp，用于免登陆
        UserSp userSp = UserSp.getInstance(context);
        userSp.setUserId(result.getData().getUserId());
        if (1 == result.getData().getUserStatus()) {
            userSp.setLogged(true);
        } else {
            userSp.setLogged(false);
        }
        userSp.saveLoginResult(result.getData());
        MyApplication.getInstance().mUserStatusChecked = true;
        MyApplication.getInstance().mUserStatus = STATUS_USER_VALIDATION;
        coreManager.setSelf(user);
        LoginSecureHelper.setLogged();
        return true;
    }

    /**
     * Secure chat third/sms login verify password interface used
     *
     * @param context
     * @param coreManager
     * @param telephone
     * @param password
     * @param result
     * @return
     */
    public static boolean saveUserForThirdSmsVerifyPassword(Context context, CoreManager coreManager, String telephone, String password, com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
        // 保存当前登陆的用户信息和Token信息作为全局变量，方便调用
        User user = coreManager.getSelf();
        if (user == null) {
            user = new User();
        }
        user.setTelephone(telephone);
        user.setPassword(password);
        user.setUserId(result.getData().getUserId());
        user.setNickName(result.getData().getNickName());
        user.setRole(result.getData().getRole());
        user.setAccount(result.getData().getAccount());
        user.setSetAccountCount(result.getData().getSetAccountCount());
        user.setMyInviteCode(result.getData().getMyInviteCode());
        // 保存最后一次登录的用户信息到Sp，用于免登陆
        UserSp userSp = UserSp.getInstance(context);
        userSp.saveLoginResult(result.getData());
        coreManager.setSelf(user);
        return true;
    }
}
