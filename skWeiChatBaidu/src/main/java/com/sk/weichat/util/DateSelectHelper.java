package com.sk.weichat.util;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;

import com.sk.weichat.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/17.
 */

public class DateSelectHelper {

    private DateSelectHelper() {
    }

    private static DateSelectHelper dateSelectHelper = new DateSelectHelper();

    private DatePickerDialog dialog;

    @SuppressWarnings("ResourceType")
    public static DateSelectHelper getInstance(Context context) { //AlertDialog.THEME_HOLO_LIGHT
        Calendar c = Calendar.getInstance();

        dateSelectHelper.dialog = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, null,
                // 绑定监听器
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        String str = context.getString(R.string.sure);
        String cancel = context.getString(R.string.cancel);

        // 手动设置按钮
        dateSelectHelper.dialog.setButton(DialogInterface.BUTTON_POSITIVE, str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker datePicker = dateSelectHelper.dialog.getDatePicker();
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();

                if (dateSelectHelper.mListener != null) {
                    String str = year + "-" + (month + 1) + "-" + day;
                    dateSelectHelper.mListener.onDateSet(dateSelectHelper.dateFromat(year, month, day), str);
                }
            }
        });

        dateSelectHelper.dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return dateSelectHelper;
    }

    private OnDateResultListener mListener;

    public void show() {
        dateSelectHelper.dialog.show();
    }

    public void setDateMax(long dateMax) {
        dateSelectHelper.dialog.getDatePicker().setMaxDate(dateMax);
    }

    /**
     * 格式必须是 yyyy-mm-day
     * "2017-11-02"
     */
    public void setDateMax(String dateMax) {
        if (TextUtils.isEmpty(dateMax) || dateMax.length() < 7) {
            return;
        }

        String[] cc = dateMax.split("-");
        long date = dateFromat(Integer.parseInt(cc[0]), (Integer.parseInt(cc[1]) - 1), Integer.parseInt(cc[2]));
        setDateMax(date);
    }

    /**
     * 格式必须是 yyyy-mm-day
     * "2017-11-02"
     */
    public void setDateMin(String dateMax) {
        if (TextUtils.isEmpty(dateMax) || dateMax.length() < 7) {
            return;
        }

        String[] cc = dateMax.split("-");
        long date = dateFromat(Integer.parseInt(cc[0]), (Integer.parseInt(cc[1]) - 1), Integer.parseInt(cc[2]));

        setDateMin(date);
    }

    public void setDateMin(long dateMin) {
        dateSelectHelper.dialog.getDatePicker().setMinDate(dateMin);
    }

    public void setOnDateSetListener(OnDateResultListener listener) {
        mListener = listener;
    }

    public interface OnDateResultListener {

        void onDateSet(long time, String dateFromat);
    }

    private DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Log.e("xuan", "onDateSet: ");
            if (mListener != null) {
                String str = year + "-" + (month + 1) + "-" + dayOfMonth;
                mListener.onDateSet(dateFromat(year, month, dayOfMonth), str);
            }
        }
    };

    private long dateFromat(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String str = year + "-" + (month + 1) + "-" + dayOfMonth;
        return c.getTimeInMillis();
    }

    public void setCurrentDate(long currentDate) {
        Date date = new Date();
        date.setTime(currentDate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        dialog.getDatePicker().init(year, month, dayOfMonth, null);
    }
}
