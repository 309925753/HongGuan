package com.sk.weichat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.sk.weichat.BuildConfig;
import com.sk.weichat.Reporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * 获取系统设备信息的工具类
 *
 * @author dty
 */
public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";

    /* 获取手机唯一序列号 */
    public static String getDeviceId(Context context) {
        String deviceId = readDeviceId(context);
        if (!TextUtils.isEmpty(deviceId)) {
            Log.i(TAG, "getDeviceId() returned: " + deviceId);
            return deviceId;
        }
        deviceId = makeDeviceId(context);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        saveDeviceId(context, deviceId);
        Log.i(TAG, "getDeviceId() put and returned: " + deviceId);
        return deviceId;
    }

    @Nullable
    @SuppressLint("HardwareIds")
    private static String makeDeviceId(Context context) {
        String deviceId = null;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();// 手机设备ID，这个ID会被用为用户访问统计
            }
        } catch (Exception ignored) {
            // deviceId越来越严格了，以防万一，
        }
        Log.d(TAG, "makeDeviceId() returned: " + deviceId);
        return deviceId;
    }

    private static void saveDeviceId(Context context, @NonNull String deviceId) {
        Log.d(TAG, "saveDeviceId() called with: context = [" + context + "], deviceId = [" + deviceId + "]");
        internalSaveDeviceId(context, deviceId);
        externalSaveDeviceId(context, deviceId);
    }

    @Nullable
    private static String readDeviceId(Context context) {
        // 内存外存互相同步，删除一个会用剩下一个复制过来，
        String internalReadDeviceId = internalReadDeviceId(context);
        String externalReadDeviceId = externalReadDeviceId(context);
        String result = null;
        // 都有时优先使用内存的，
        if (!TextUtils.isEmpty(internalReadDeviceId)) {
            if (!TextUtils.equals(internalReadDeviceId, externalReadDeviceId)) {
                // 内存有，外存不同，就刷新外存，
                externalSaveDeviceId(context, internalReadDeviceId);
            }
            result = internalReadDeviceId;
        } else if (!TextUtils.isEmpty(externalReadDeviceId)) {
            // 内存没有，外存有，就更新内存，
            internalSaveDeviceId(context, externalReadDeviceId);
            result = externalReadDeviceId;
        }
        // 内存外存都没有，返回null,
        Log.d(TAG, "readDeviceId() returned: " + result);
        return result;
    }

    @Nullable
    private static String internalReadDeviceId(Context context) {
        String result = PreferenceUtils.getString(context, Constants.KEY_DEVICE_ID);
        Log.d(TAG, "internalReadDeviceId() returned: " + result);
        return result;
    }

    private static void internalSaveDeviceId(Context context, @NonNull String deviceId) {
        Log.d(TAG, "internalSaveDeviceId() called with: context = [" + context + "], deviceId = [" + deviceId + "]");
        PreferenceUtils.putString(context, Constants.KEY_DEVICE_ID, deviceId);
    }

    private static File getExternalDeviceIdFile(Context ctx) {
        File rootFile = Environment.getExternalStoragePublicDirectory("");
        // 包名参与，以防万一多个app冲突，
        File folder = new File(rootFile, "." + BuildConfig.APPLICATION_ID);
        File file = new File(folder, ".deviceId");
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        Log.d(TAG, "getExternalDeviceIdFile() returned: " + file);
        return file;
    }

    private static void externalSaveDeviceId(Context context, @NonNull String deviceId) {
        Log.d(TAG, "externalSaveDeviceId() called with: context = [" + context + "], deviceId = [" + deviceId + "]");
        try {
            File file = getExternalDeviceIdFile(context);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(deviceId);
                fileWriter.flush();
            }
        } catch (Exception e) {
            // 无论如何不能崩溃，比如没权限什么的，
            Reporter.unreachable(e);
        }
    }

    @Nullable
    private static String externalReadDeviceId(Context context) {
        String result = null;
        try {
            File file = getExternalDeviceIdFile(context);
            if (file.exists()) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    result = fileReader.readLine();
                }
            }
        } catch (Exception e) {
            // 无论如何不能崩溃，比如没权限什么的，
            Reporter.unreachable(e);
        }
        Log.d(TAG, "externalReadDeviceId() returned: " + result);
        return result;
    }

    /* 获取操作系统版本号 */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 获取手机型号 */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /* 获取手机厂商 */
    public static String getManufacturers() {
        return android.os.Build.MANUFACTURER;
    }

    /* 获取app的版本信息 */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;// 系统版本号
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;// 系统版本名
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断手机Rom
     *
     * @param propName
     * @return
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断 miui emui meizu oppo vivo Rom
     *
     * @return
     */
    public static boolean isMiuiRom() {
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isEmuiRom() {
        String property = getSystemProperty("ro.build.version.emui");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isMeizuRom() {
        String property = getSystemProperty("ro.build.display.id");
        return property != null && property.toLowerCase().contains("flyme");
    }

    public static boolean isOppoRom() {
        String property = getSystemProperty("ro.build.version.opporom");
        return !TextUtils.isEmpty(property);
    }

    public static boolean isVivoRom() {
        String property = getSystemProperty("ro.vivo.os.version");
        return !TextUtils.isEmpty(property);
    }

    public static boolean is360Rom() {
        return Build.MANUFACTURER != null
                && (Build.MANUFACTURER.toLowerCase().contains("qiku")
                || Build.MANUFACTURER.contains("360"));
    }
}
