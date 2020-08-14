package com.sk.weichat.xmpp.helloDemon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 神隐模式
 * 不清理 && 自启动
 */
public class IntentWrapper {
    //Android 7.0+ Doze 模式
    protected static final int DOZE = 98;
    //华为 自启管理
    protected static final int HUAWEI = 99;
    //华为 锁屏清理
    protected static final int HUAWEI_GOD = 100;
    //小米 自启动管理
    protected static final int XIAOMI = 101;
    //小米 神隐模式
    protected static final int XIAOMI_GOD = 102;
    //三星 5.0/5.1 自启动应用程序管理
    protected static final int SAMSUNG_L = 103;
    //魅族 自启动管理
    protected static final int MEIZU = 104;
    //魅族 待机耗电管理
    protected static final int MEIZU_GOD = 105;
    //Oppo 自启动管理
    protected static final int OPPO = 106;
    //三星 6.0+ 未监视的应用程序管理
    protected static final int SAMSUNG_M = 107;
    //Oppo 自启动管理(旧版本系统)
    protected static final int OPPO_OLD = 108;
    //Vivo 后台高耗电
    protected static final int VIVO_GOD = 109;
    //金立 应用自启
    protected static final int GIONEE = 110;
    //乐视 自启动管理
    protected static final int LETV = 111;
    //乐视 应用保护
    protected static final int LETV_GOD = 112;
    //酷派 自启动管理
    protected static final int COOLPAD = 113;
    //联想 后台管理
    protected static final int LENOVO = 114;
    //联想 后台耗电优化
    protected static final int LENOVO_GOD = 115;
    //中兴 自启管理
    protected static final int ZTE = 116;
    //中兴 锁屏加速受保护应用
    protected static final int ZTE_GOD = 117;

    protected static List<IntentWrapper> sIntentWrapperList;
    private static String mAppName = MyApplication.getContext().getString(R.string.app_name);
    protected Intent intent;
    protected int type;

    protected IntentWrapper(Intent intent, int type) {
        this.intent = intent;
        this.type = type;
    }

    public static List<IntentWrapper> getIntentWrapperList() {
        if (sIntentWrapperList == null) {
            // if (!DaemonEnv.sInitialized) return new ArrayList<>();
            sIntentWrapperList = new ArrayList<>();
            //Android 7.0+ Doze 模式
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PowerManager pm = (PowerManager) DaemonEnv.sApp.getSystemService(Context.POWER_SERVICE);
                boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(DaemonEnv.sApp.getPackageName());
                if (!ignoringBatteryOptimizations) {
                    Intent dozeIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    dozeIntent.setData(Uri.parse("package:" + DaemonEnv.sApp.getPackageName()));
                    sIntentWrapperList.add(new IntentWrapper(dozeIntent, DOZE));
                }
            }*/

            //华为 自启管理
            Intent huaweiIntent = new Intent();
            huaweiIntent.setAction("huawei.intent.action.HSM_BOOTAPP_MANAGER");
            sIntentWrapperList.add(new IntentWrapper(huaweiIntent, HUAWEI));

            //华为 锁屏清理
            Intent huaweiGodIntent = new Intent();
            huaweiGodIntent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            sIntentWrapperList.add(new IntentWrapper(huaweiGodIntent, HUAWEI_GOD));

            //小米 自启动管理
            Intent xiaomiIntent = new Intent();
            xiaomiIntent.setAction("miui.intent.action.OP_AUTO_START");
            xiaomiIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sIntentWrapperList.add(new IntentWrapper(xiaomiIntent, XIAOMI));

            //小米 神隐模式
            Intent xiaomiGodIntent = new Intent();
            xiaomiGodIntent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
            xiaomiGodIntent.putExtra("package_name", MyApplication.getInstance().getPackageName());
            xiaomiGodIntent.putExtra("package_label", getApplicationName());
            sIntentWrapperList.add(new IntentWrapper(xiaomiGodIntent, XIAOMI_GOD));

            //三星 5.0/5.1 自启动应用程序管理DaemonEnv.sApp.getPackageManager()
            Intent samsungLIntent = MyApplication.getInstance().getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm");
            if (samsungLIntent != null)
                sIntentWrapperList.add(new IntentWrapper(samsungLIntent, SAMSUNG_L));

            //三星 6.0+ 未监视的应用程序管理
            Intent samsungMIntent = new Intent();
            samsungMIntent.setComponent(new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.battery.BatteryActivity"));
            sIntentWrapperList.add(new IntentWrapper(samsungMIntent, SAMSUNG_M));

            //魅族 自启动管理
            Intent meizuIntent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            meizuIntent.addCategory(Intent.CATEGORY_DEFAULT);
            meizuIntent.putExtra("packageName", MyApplication.getInstance().getPackageName());
            sIntentWrapperList.add(new IntentWrapper(meizuIntent, MEIZU));

            //魅族 待机耗电管理
            Intent meizuGodIntent = new Intent();
            meizuGodIntent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.powerui.PowerAppPermissionActivity"));
            sIntentWrapperList.add(new IntentWrapper(meizuGodIntent, MEIZU_GOD));

            //Oppo 自启动管理
            Intent oppoIntent = new Intent();
            oppoIntent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            sIntentWrapperList.add(new IntentWrapper(oppoIntent, OPPO));

            //Oppo 自启动管理(旧版本系统)
            Intent oppoOldIntent = new Intent();
            oppoOldIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity"));
            sIntentWrapperList.add(new IntentWrapper(oppoOldIntent, OPPO_OLD));

            //Vivo 后台高耗电
            Intent vivoGodIntent = new Intent();
            vivoGodIntent.setComponent(new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
            sIntentWrapperList.add(new IntentWrapper(vivoGodIntent, VIVO_GOD));

            //金立 应用自启
            Intent gioneeIntent = new Intent();
            gioneeIntent.setComponent(new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.MainActivity"));
            sIntentWrapperList.add(new IntentWrapper(gioneeIntent, GIONEE));

            //乐视 自启动管理
            Intent letvIntent = new Intent();
            letvIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            sIntentWrapperList.add(new IntentWrapper(letvIntent, LETV));

            //乐视 应用保护
            Intent letvGodIntent = new Intent();
            letvGodIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity"));
            sIntentWrapperList.add(new IntentWrapper(letvGodIntent, LETV_GOD));

            //酷派 自启动管理
            Intent coolpadIntent = new Intent();
            coolpadIntent.setComponent(new ComponentName("com.yulong.android.security", "com.yulong.android.seccenter.tabbarmain"));
            sIntentWrapperList.add(new IntentWrapper(coolpadIntent, COOLPAD));

            //联想 后台管理
            Intent lenovoIntent = new Intent();
            lenovoIntent.setComponent(new ComponentName("com.lenovo.security", "com.lenovo.security.purebackground.PureBackgroundActivity"));
            sIntentWrapperList.add(new IntentWrapper(lenovoIntent, LENOVO));

            //联想 后台耗电优化
            Intent lenovoGodIntent = new Intent();
            lenovoGodIntent.setComponent(new ComponentName("com.lenovo.powersetting", "com.lenovo.powersetting.ui.Settings$HighPowerApplicationsActivity"));
            sIntentWrapperList.add(new IntentWrapper(lenovoGodIntent, LENOVO_GOD));

            //中兴 自启管理
            Intent zteIntent = new Intent();
            zteIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager"));
            sIntentWrapperList.add(new IntentWrapper(zteIntent, ZTE));

            //中兴 锁屏加速受保护应用
            Intent zteGodIntent = new Intent();
            zteGodIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.setting.ClearAppSettingsActivity"));
            sIntentWrapperList.add(new IntentWrapper(zteGodIntent, ZTE_GOD));
        }
        return sIntentWrapperList;
    }

    public static String getApplicationName() {
       /* if (sApplicationName == null) {
            if (!DaemonEnv.sInitialized)
                return "";
            PackageManager pm;
            ApplicationInfo ai;
            try {
                pm = DaemonEnv.sApp.getPackageManager();
                ai = pm.getApplicationInfo(DaemonEnv.sApp.getPackageName(), 0);
                sApplicationName = pm.getApplicationLabel(ai).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                sApplicationName = DaemonEnv.sApp.getPackageName();
            }
        }*/
        return MyApplication.getInstance().getPackageName();
    }

    /**
     * 处理白名单.
     *
     * @return 弹过框的 IntentWrapper.
     */
    @NonNull
    public static List<IntentWrapper> whiteListMatters(final Activity a, String reason) {
        List<IntentWrapper> showed = new ArrayList<>();
        if (reason == null)
            reason = MyApplication.getContext().getString(R.string.intent_wrapper_reason);
        List<IntentWrapper> intentWrapperList = getIntentWrapperList();
        for (final IntentWrapper iw : intentWrapperList) {
            //如果本机上没有能处理这个Intent的Activity，说明不是对应的机型，直接忽略进入下一次循环。
            if (!iw.doesActivityExists())
                continue;
            switch (iw.type) {
                case HUAWEI:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_allow) + mAppName + a.getString(R.string.intent_wrapper_1) +
                                    a.getString(R.string.intent_wrapper_2) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case ZTE_GOD:
                case HUAWEI_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // setIsTitle(getApplicationName() + " 需要加入锁屏清理白名单")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_3) +
                                    a.getString(R.string.intent_wrapper_4) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case XIAOMI_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_close) + mAppName + a.getString(R.string.intent_wrapper_5) +
                                    a.getString(R.string.intent_wrapper_6) + getApplicationName() + a.getString(R.string.intent_wrapper_7))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case SAMSUNG_L:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // .setIsTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_8) +
                                    a.getString(R.string.intent_wrapper_9) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case SAMSUNG_M:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_10) +
                                    a.getString(R.string.intent_wrapper_11) + getApplicationName() + a.getString(R.string.intent_wrapper_12))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case MEIZU:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_allow) + mAppName + a.getString(R.string.intent_wrapper_13) +
                                    a.getString(R.string.intent_wrapper_14))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case MEIZU_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // .setIsTitle(getApplicationName() + " 需要在待机时保持运行")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_15) +
                                    a.getString(R.string.intent_wrapper_16) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case ZTE:
                case LETV:
                case XIAOMI:
                case OPPO:
                case OPPO_OLD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_17) +
                                    a.getString(R.string.intent_wrapper_18) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case COOLPAD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need) + mAppName + a.getString(R.string.intent_wrapper_19) +
                                    a.getString(R.string.intent_wrapper_20) + getApplicationName() + a.getString(R.string.intent_wrapper_31))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case VIVO_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // .setIsTitle("需要允许 " + getApplicationName() + " 的后台运行")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_allow) + mAppName + a.getString(R.string.intent_wrapper_21) +
                                    a.getString(R.string.intent_wrapper_22) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case GIONEE:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_allow) + mAppName + a.getString(R.string.intent_wrapper_23) +
                                    a.getString(R.string.intent_wrapper_24) + getApplicationName() + a.getString(R.string.intent_wrapper_32))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LETV_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // .setIsTitle("需要禁止 " + getApplicationName() + " 被自动清理")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_prohibited) + mAppName + a.getString(R.string.intent_wrapper_25) +
                                    a.getString(R.string.intent_wrapper_26) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_off))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LENOVO:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_allow) + mAppName + a.getString(R.string.intent_wrapper_27) +
                                    a.getString(R.string.intent_wrapper_28) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_on))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LENOVO_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(true)
                            // .setIsTitle("需要关闭 " + getApplicationName() + " 的后台耗电优化")
                            .setMessage(reason + MyApplication.getContext().getString(R.string.intent_wrapper_need_close) + mAppName + a.getString(R.string.intent_wrapper_29) +
                                    a.getString(R.string.intent_wrapper_30) + getApplicationName() + MyApplication.getContext().getString(R.string.intent_wrapper_switch_turn_off))
                            .setPositiveButton(a.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
            }
        }
        if (showed.size() == 0) {// 说明没有可执行的intent 通知到设置界面
            MyApplication.getContext().sendBroadcast(new Intent(com.sk.weichat.broadcast.OtherBroadcast.NO_EXECUTABLE_INTENT));
        }
        return showed;
    }

    /**
     * 获取支持跳转至自动启动管理的机型，来控制 '设置-允许退出APP后可以收到消息" item的显示与隐藏
     */
    @NonNull
    public static List<IntentWrapper> getWhiteListMatters(final Activity a, String reason) {
        List<IntentWrapper> showed = new ArrayList<>();
        if (reason == null)
            reason = MyApplication.getContext().getString(R.string.intent_wrapper_reason);
        List<IntentWrapper> intentWrapperList = getIntentWrapperList();
        for (final IntentWrapper iw : intentWrapperList) {
            //如果本机上没有能处理这个Intent的Activity，说明不是对应的机型，直接忽略进入下一次循环。
            if (!iw.doesActivityExists())
                continue;
            switch (iw.type) {
                case HUAWEI:
                    showed.add(iw);
                    break;
                case ZTE_GOD:
                case HUAWEI_GOD:
                    showed.add(iw);
                    break;
                case XIAOMI_GOD:
                    showed.add(iw);
                    break;
                case SAMSUNG_L:
                    showed.add(iw);
                    break;
                case SAMSUNG_M:
                    showed.add(iw);
                    break;
                case MEIZU:
                    showed.add(iw);
                    break;
                case MEIZU_GOD:
                    showed.add(iw);
                    break;
                case ZTE:
                case LETV:
                case XIAOMI:
                case OPPO:
                case OPPO_OLD:
                    showed.add(iw);
                    break;
                case COOLPAD:
                    showed.add(iw);
                    break;
                case VIVO_GOD:
                    showed.add(iw);
                    break;
                case GIONEE:
                    showed.add(iw);
                    break;
                case LETV_GOD:
                    showed.add(iw);
                    break;
                case LENOVO:
                    showed.add(iw);
                    break;
                case LENOVO_GOD:
                    showed.add(iw);
                    break;
            }
        }
        return showed;
    }

    /**
     * 防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
     */
    public static void onBackPressed(Activity a) {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        a.startActivity(launcherIntent);
    }

    /**
     * 判断本机上是否有能处理当前Intent的Activity
     */
    protected boolean doesActivityExists() {
        // if (!DaemonEnv.sInitialized)
        // return false;
        PackageManager pm = MyApplication.getInstance().getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    /**
     * 安全地启动一个Activity
     */
    protected void startActivitySafely(Activity activityContext) {
        try {
            activityContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
