package com.sk.weichat.util;

import android.util.Log;

import com.sk.weichat.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天消息漫游工具类  不要动随便改，灰飞烟灭
 * author：Administrator on 2016/12/26 10:32
 * description:文件说明
 * version:版本
 */
public class CharUtils {

    // 是否为中文
    public static boolean isChinese(String str) {
        String regEx = "[\\u4e00-\\u9fa5]+";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find())
            return true;
        else
            return false;
    }

    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result = 0;
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append(String.valueOf((char) result));
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append(String.valueOf((char) result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
            }
        }
        return sb.toString();
    }

    public static String formatBody(String input) {
        Log.e("xuan", "input  " + input);
        //        input = "{&quot;content&quot;:&quot;1222&quot;,&quot;fromUserName&quot;:&quot;qqq&quot;,&quot;isEncrypt&quot;:&quot;false&quot;,&quot;messageId&quot" +
        //                ";:&quot;d0c9d9dd80594ceeaf86ac23c5b90ef6&quot;,&quot;timeSend&quot;:1498632552,&quot;toUserType&quot;:1,&quot;type&quot;:1}";
        input = input.replaceAll("&quot;", "");
        input = input.replaceAll("\\{", "");
        input = input.replaceAll("\\}", "");

        StringBuilder sb = new StringBuilder();

        String[] str = input.split(",");
        for (String s : str) {
            String[] ss = s.split(":");
            if (ss.length > 1) {
                sb.append("\"");
                sb.append(ss[0]);
                sb.append("\"");
                sb.append(":");
                sb.append("\"");
                sb.append(ss[1]);
                sb.append("\"");
                sb.append(",");
            } else {
                Log.e("xuan", "else key " + ss[0]);
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.insert(0, "{");
        sb.append("}");
        Log.e("xuan", "else key " + sb.toString());
        return sb.toString();
    }

    public static String findMessageById(String input) {
        // input = "<message xmlns=\"jabber:client\" from=\"10009161@192.168.0.128/youjob\" to=\"10009162@192.168.0.128\" id=\"sOBB8-13\" type=\"chat\"><body>{&quot;fromUserId&quot;:&quot;10009161&quot;,&quot;fromUserName&quot;:&quot;ppp&quot;,&quot;timeSend&quot;:1498651534,&quot;toUserType&quot;:1,&quot;type&quot;:43}</body><thread>a4496fca-313c-46b0-aa06-7ccddc528d55</thread><request xmlns=\"urn:xmpp:receipts\"/></message>";
        String[] skk = input.split("from");

        String from = skk[1].split("@")[0].split("\"")[1];
        Log.e("xuan", "fromAtMessage  " + from);
        return from;
    }

    public static long getChatStartTimes(String fromUserId) {
        long time = PreferenceUtils.getLong(MyApplication.getContext(), "starttime" + fromUserId, System.currentTimeMillis());
        return time;
    }

    public static long findTimeById(String messageBody) {
        JSONObject json = null;
        try {
            json = new JSONObject(messageBody);
            double b = json.getDouble("timeSend");
            long time = (long) (b * 1000);
            return time;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
