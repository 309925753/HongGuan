package com.redchamber.lib.utils;

import android.widget.Toast;

public class ToastUtils {

    public static void showToast(String msg) {
        Toast.makeText(Utils.getApp(), msg, Toast.LENGTH_SHORT).show();
    }
}
