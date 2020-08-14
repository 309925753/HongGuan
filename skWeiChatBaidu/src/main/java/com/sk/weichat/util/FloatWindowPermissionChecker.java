package com.sk.weichat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.sk.weichat.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by feifan on 2017/8/21.
 * Contacts me:404619986@qq.com
 */

public class FloatWindowPermissionChecker {
    private static final String TAG = "FloatWindowPermissionChecker";
    //无法跳转的提示，应当根据不同的Rom给予不同的提示，比如Oppo应该提示去手机管家里开启，这里偷懒懒得写了

    /**
     * 悬浮窗权限判断
     *
     * @param context 上下文
     * @return [ true, 有权限 ][ false, 无权限 ]
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkAlertWindowsPermission(Context context) {
        boolean hasPermission;
        if (Build.VERSION.SDK_INT < 19) {
            hasPermission = true;
        } else if (Build.VERSION.SDK_INT < 23) {
            if (DeviceInfoUtil.isMiuiRom() || DeviceInfoUtil.isMeizuRom() || DeviceInfoUtil.isEmuiRom() || DeviceInfoUtil.is360Rom()) {// 特殊机型
                hasPermission = opPermissionCheck(context, 24);
            } else {// 其他机型
                hasPermission = true;
            }
        } else {// 6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
            hasPermission = highVersionPermissionCheck(context);
        }
        return hasPermission;
    }

    /**
     * [19-23]之间版本通过[AppOpsManager]的权限判断
     *
     * @param context 上下文
     * @param op
     * @return [ true, 有权限 ][ false, 无权限 ]
     */
    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess", "SameParameterValue"})
    private static boolean opPermissionCheck(Context context, int op) {
        try {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            Class clazz = AppOpsManager.class;
            Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
            return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Android 6.0 版本及之后的权限判断
     *
     * @param context 上下文
     * @return [ true, 有权限 ][ false, 无权限 ]
     */
    @SuppressWarnings("unchecked")
    private static boolean highVersionPermissionCheck(Context context) {
        if (DeviceInfoUtil.isMeizuRom()) {// 魅族6.0的系统单独适配
            return opPermissionCheck(context, 24);
        }
        try {
            Class clazz = Settings.class;
            Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
            return (Boolean) canDrawOverlays.invoke(null, context);
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void tryJumpToPermissionPage(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (DeviceInfoUtil.isMiuiRom()) {
                applyMiuiPermission(context);
            } else if (DeviceInfoUtil.isEmuiRom()) {
                applyHuaweiPermission(context);
            } else if (DeviceInfoUtil.isVivoRom()) {
                applyVivoPermission(context);
            } else if (DeviceInfoUtil.isOppoRom()) {
                applyOppoPermission(context);
            } else if (DeviceInfoUtil.is360Rom()) {
                apply360Permission(context);
            } else if (DeviceInfoUtil.isVivoRom()) {
                applyVivoPermission(context);
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        } else {
            if (DeviceInfoUtil.isMeizuRom()) {
                applyMeizuPermission(context);
            } else {
                applyCommonPermission(context);
            }
        }
    }

    private static boolean startActivitySafely(Intent intent, Context context) {
        if (isIntentAvailable(intent, context)) {
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                applyCommonPermission(context);
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIntentAvailable(Intent intent, Context context) {
        return intent != null && context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    private static void showAlertToast(Context context) {
        Toast.makeText(context, context.getString(R.string.tip_permission_settings_page_not_found), Toast.LENGTH_LONG).show();
    }

    private static void applyCommonPermission(Context context) {
        try {
            Class clazz = Settings.class;
            Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
            Intent intent = new Intent(field.get(null).toString());
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            startActivitySafely(intent, context);
        } catch (Exception e) {
            showAlertToast(context);
        }
    }

    private static void applyCoolpadPermission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.yulong.android.seccenter", "com.yulong.android.seccenter.dataprotection.ui.AppListActivity");
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyLenovoPermission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.lenovo.safecenter", "com.lenovo.safecenter.MainTab.LeSafeMainActivity");
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyZTEPermission(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER");
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyLetvPermission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AppActivity");
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyVivoPermission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager");
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyOppoPermission(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packageName", context.getPackageName());
        intent.setAction("com.oppo.safe");
        intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity");
        if (!startActivitySafely(intent, context)) {
            intent.setAction("com.color.safecenter");
            intent.setClassName("com.color.safecenter", "com.color.safecenter.permission.floatwindow.FloatWindowListActivity");
            if (!startActivitySafely(intent, context)) {
                intent.setAction("com.coloros.safecenter");
                intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
                if (!startActivitySafely(intent, context)) {
                    showAlertToast(context);
                }
            }
        }
    }

    private static void apply360Permission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
        if (!startActivitySafely(intent, context)) {
            intent.setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
            if (!startActivitySafely(intent, context)) {
                showAlertToast(context);
            }
        }
    }

    private static void applyMiuiPermission(Context context) {
        Intent intent = new Intent();
        intent.setAction("miui.intent.action.APP_PERM_EDITOR");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("extra_pkgname", context.getPackageName());
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyMeizuPermission(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        intent.putExtra("packageName", context.getPackageName());
        if (!startActivitySafely(intent, context)) {
            showAlertToast(context);
        }
    }

    private static void applyHuaweiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
            intent.setComponent(comp);
            if (!startActivitySafely(intent, context)) {
                comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");
                intent.setComponent(comp);
                context.startActivity(intent);
            }
        } catch (SecurityException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings", "com.android.settings.permission.TabItem");
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            showAlertToast(context);
        }
    }

    private static void applySmartisanPermission(Context context) {
        Intent intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS_NEW");
        intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
        intent.putExtra("index", 17); //有版本差异,不一定定位正确
        if (!startActivitySafely(intent, context)) {
            intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS");
            intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
            intent.putExtra("permission", new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW});
            if (!startActivitySafely(intent, context)) {
                showAlertToast(context);
            }
        }
    }
}
