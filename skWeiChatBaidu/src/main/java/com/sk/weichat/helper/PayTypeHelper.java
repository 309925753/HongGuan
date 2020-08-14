package com.sk.weichat.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sk.weichat.R;

public class PayTypeHelper {

    public static void selectPayType(Context ctx, SelectPayTypeCallback callback) {
        if (!YeepayHelper.ENABLE) {
            callback.payType(PayType.DEFAULT);
            return;
        }
        Dialog bottomDialog = new Dialog(ctx, R.style.BottomDialog);
        View contentView = LayoutInflater.from(ctx).inflate(R.layout.dialog_select_pay_type, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = ctx.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
        contentView.findViewById(R.id.tv3).setOnClickListener(v -> bottomDialog.dismiss());
        contentView.findViewById(R.id.tv1).setOnClickListener(v -> {
            bottomDialog.dismiss();
            callback.payType(PayType.DEFAULT);
        });
        contentView.findViewById(R.id.tv2).setOnClickListener(v -> {
            bottomDialog.dismiss();
            callback.payType(PayType.YEEPAY);
        });

    }

    public enum PayType {
        DEFAULT, YEEPAY
    }

    public interface SelectPayTypeCallback {
        void payType(PayType type);
    }
}
