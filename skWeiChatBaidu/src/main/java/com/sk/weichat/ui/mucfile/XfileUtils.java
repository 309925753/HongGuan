package com.sk.weichat.ui.mucfile;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sk.weichat.R;
import com.sk.weichat.bean.message.XmppMessage;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Administrator on 2017/7/7.
 */

public class XfileUtils {

    public static String fromatTime(long time) {
        return fromatTime(time, "MM-dd");
    }

    /**
     * "MM-dd HH:mm"
     */
    public static String fromatTime(long time, String s) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(s);
        Date d = new Date(time);
        return dateFormat.format(d);
    }

    public static String fromatFloat(double time) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(time);
    }

    public static String fromatSize(long size) {
        String dw = " B";
        float f = (float) size;
        if (f > 1024) {
            f = f / 1024;
            dw = " KB";
        }

        if (f > 1024) {
            f = f / 1024;
            dw = " MB";
        }

        DecimalFormat df = new DecimalFormat("#0.00");

        return df.format(f) + dw;
    }

    public static int getProgress(long cur, long max) {
        int progress = (int) (cur / (float) max * 100);
        return progress;
    }

    public static void setFileInco(int type, ImageView ivInco) {
        switch (type) {
           /* case 1:  // 图片
                Bitmap b = decodeBitmapFromFile(f.getAbsolutePath(), 120, 120);//BitmapFactory.decodeFile(f.getAbsolutePath());
                ivInco.setImageBitmap(b);
                break;*/
            case 2: // music
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_y);
                break;
            case 3: // 视屏
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_v);
                break;
            case 5: // xls
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_x);
                break;
            case 6: // doc
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_w);
                break;
            case 4: // ppt
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_p);
                break;
            case 10: // pdf
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_f);
                break;
            case 11: // apk
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_a);
                break;
            case 8: // txt
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_t);
                break;
            case 7: // rar of zip
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_z);
                break;
            case 9: // 其他
            default:
                ivInco.setImageResource(R.drawable.ic_muc_flie_type_what);
                break;
        }
    }

    public static int getFileType(String suffix) {
        if (suffix == null || "".equals(suffix)) {
            return 9;
        }

        int type = 9;
        if (suffix.equals("png") || suffix.equals("jpg") || suffix.equals("gif")) {
            type = 1;
        } else if (suffix.equals("mp3")) {
            type = 2;
        } else if (suffix.equals("mp4") || suffix.equals("avi")) {
            type = 3;
        } else if (suffix.equals("xls")) {
            type = (5);
        } else if (suffix.equals("doc")) {
            type = (6);
        } else if (suffix.equals("ppt")) {
            type = (4);
        } else if (suffix.equals("pdf")) {
            type = (10);
        } else if (suffix.equals("apk")) {
            type = (11);
        } else if (suffix.equals("txt")) {
            type = (8);
        } else if (suffix.equals("rar") || suffix.equals("zip")) {
            type = (7);
        } else {
            type = (9);
        }

        return type;
    }


    /**
     * 文字变色
     */
    public static SpannableString matcherSearchTitle(int color, String text, String keyword) {
        String string = text.toLowerCase();
        String key = "";
        if (keyword != null) {
            key = keyword.toLowerCase();
        }
        Pattern pattern = Pattern.compile(key);
        Matcher matcher = pattern.matcher(string);
        SpannableString ss = new SpannableString(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ss.setSpan(new ForegroundColorSpan(color), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /**
     * 文字变色
     */
    public static SpannableString matcherSearchTitle(int color, CharSequence text, String keyword) {
        String key = "";
        if (keyword != null) {
            key = keyword.toLowerCase();
        }
        Pattern pattern = Pattern.compile(key);
        Matcher matcher = pattern.matcher(text);
        SpannableString ss = new SpannableString(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ss.setSpan(new ForegroundColorSpan(color), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /**
     * 获得控件的高度
     */
    public static int measureViewHeight(View v) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        int height = v.getMeasuredHeight();
        //        int width = v.getMeasuredWidth();

        Log.e("xuan", "measureViewHeight: " + height);
        return height;
    }

    public static boolean isNotEmpty(List<?> data) {
        if (data != null && data.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static String strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return String.valueOf(strtodate);
    }


    /**
     * 如果是一条不显示的消息就不要保存了
     *
     * @param type 消息类型
     * @return true 不显示  false显示
     */

    public static boolean isNotChatVisibility(int type) {

        boolean b = false;

        if (type == XmppMessage.TYPE_READ) { // 已读消息还是要保存的， 不让它查询出来就行了
            b = false;
        } else if (type > 100) {
            b = true;
            if (type == XmppMessage.TYPE_END_CONNECT_VOICE) {
                b = false;
            } else if (type == XmppMessage.TYPE_NO_CONNECT_VIDEO) {
                b = false;
            } else if (type == XmppMessage.TYPE_END_CONNECT_VIDEO) {
                b = false;
            } else if (type == XmppMessage.TYPE_NO_CONNECT_SCREEN) {
                b = false;
            } else if (type == XmppMessage.TYPE_END_CONNECT_SCREEN) {
                b = false;
            } else if (type == XmppMessage.TYPE_NO_CONNECT_VOICE) {
                b = false;
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_VOICE) {
                b = false;
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_SCREEN) {
                b = false;
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_VIDEO) {
                b = false;
            } else if (type == XmppMessage.TYPE_IS_MU_CONNECT_TALK) {
                b = false;
            } else if (type == XmppMessage.TYPE_IS_BUSY) {
                b = false;
            } else if (type == XmppMessage.TYPE_SAYHELLO) {
                b = false;
            } else if (type == XmppMessage.TYPE_FEEDBACK) {
                b = false;
            } else if (type == XmppMessage.TYPE_SECURE_LOST_KEY) {
                b = false;
            }
        }
        return b;
    }
}
