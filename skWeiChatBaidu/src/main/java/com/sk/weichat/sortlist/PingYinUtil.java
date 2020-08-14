package com.sk.weichat.sortlist;

import android.text.TextUtils;

import com.sk.weichat.Reporter;
import com.sk.weichat.util.LogUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public class PingYinUtil {
    private static final HanyuPinyinOutputFormat format;

    static {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private static boolean isChineseCharacter(char ch) {
        return '\u4E00' <= ch && ch <= '\u9FA5';
    }

    /**
     * 将字符串中的中文转换为全拼<br/>
     * 1、如果字符串为空，返回#<br/>
     * 2、如为中文，张三输出ZHANGSAN<br/>
     * 3、如果为字符 abc输出ABC<br/>
     * 4、如果为其他字符，输入#<br/>
     */
    public static String getPingYin(String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return "#";
        }
        char[] input = inputString.toCharArray();
        StringBuilder output = new StringBuilder();
        try {
            for (char ch : input) {
                if (isChineseCharacter(ch)) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (temp == null || temp.length == 0) {
                        // unicode_to_hanyu_pinyin.txt里写死了部分汉字没有拼音，比如\u8985, 覅,
                        // 也就是正常情况这个temp可能为空，
                        LogUtils.log("获取拼音失败：" + inputString + " -> " + ch);
                        output.append("#");
                    } else {
                        output.append(temp[0]);
                    }
                } else if (Character.isAlphabetic(ch)) {
                    output.append(Character.toUpperCase(ch));
                } else {
                    output.append("#");
                }
            }
        } catch (Exception e) {
            Reporter.post("获取拼音失败：" + inputString, e);
        }
        return output.toString();
    }

}
