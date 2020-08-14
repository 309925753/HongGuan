package com.sk.weichat.util.filter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.ArrayMap;

import androidx.core.content.ContextCompat;

import com.sk.weichat.R;
import com.sk.weichat.util.DisplayUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表情过滤器
 * TextView/EditText set this EmojiInputFilter, Support auto conversion Emoji
 */
public class EmojiInputFilter implements InputFilter {
    public static final int EMOJI_DRAWABLE_BOUND_SIZE_DP = 20;

    private static final int[] faceImages =
            {
                    R.drawable.e_01_smile, R.drawable.e_02_joy, R.drawable.e_03_heart_eyes,
                    R.drawable.e_04_sweat_smile, R.drawable.e_05_laughing, R.drawable.e_06_wink,
                    R.drawable.e_07_yum, R.drawable.e_24_blush, R.drawable.e_09_fearful,
                    R.drawable.e_10_ohyeah, R.drawable.e_11_cold_sweat, R.drawable.e_12_scream,
                    R.drawable.e_13_kissing_heart, R.drawable.e_14_smirk, R.drawable.e_15_angry,
                    R.drawable.e_16_sweat, R.drawable.e_17_stuck, R.drawable.e_18_rage,
                    R.drawable.e_19_etriumph, R.drawable.e_20_mask, R.drawable.e_27_flushed,
                    R.drawable.e_22_sunglasses, R.drawable.e_23_sob, R.drawable.e_del,

                    R.drawable.e_08_relieved, R.drawable.e_26_doubt, R.drawable.e_21_confounded,
                    R.drawable.e_28_sleepy,
                    R.drawable.e_29_sleeping, R.drawable.e_30_disappointed_relieved,
                    R.drawable.e_31_tire, R.drawable.e_32_astonished,
                    R.drawable.e_33_buttonnose, R.drawable.e_34_frowning, R.drawable.e_35_shutup,
                    R.drawable.e_36_expressionless, R.drawable.e_37_confused, R.drawable.e_38_tired_face,
                    R.drawable.e_39_grin, R.drawable.e_40_unamused, R.drawable.e_41_persevere,
                    R.drawable.e_42_relaxed, R.drawable.e_43_pensive, R.drawable.e_44_no_mouth,
                    R.drawable.e_45_worried, R.drawable.e_46_cry, R.drawable.e_47_pill,
                    R.drawable.e_del,

                    R.drawable.e_48_celebrate,
                    R.drawable.e_49_gift, R.drawable.e_50_birthday,
                    R.drawable.e_51_pray, R.drawable.e_52_ok_hand, R.drawable.e_53_first,
                    R.drawable.e_54_v, R.drawable.e_55_punch, R.drawable.e_56_thumbsup,
                    R.drawable.e_57_thumbsdown, R.drawable.e_58_muscle, R.drawable.e_59_maleficeent,
                    R.drawable.e_60_broken_heart, R.drawable.e_61_heart, R.drawable.e_62_taxi,
                    R.drawable.e_63_eyes, R.drawable.e_64_rose,
                    R.drawable.e_65_ghost, R.drawable.e_66_lip, R.drawable.e_67_fireworks,
                    R.drawable.e_68_balloon, R.drawable.e_69_clasphands, R.drawable.e_70_bye,
                    R.drawable.e_del,
            };
    private static final String[] faceScr =
            {
                    "[smile]", "[joy]", "[heart-eyes]"
                    , "[sweat_smile]", "[laughing]", "[wink]"
                    , "[yum]", "[blush]", "[fearful]"
                    , "[ohYeah]", "[cold-sweat]", "[scream]"
                    , "[kissing_heart]", "[smirk]", "[angry]"
                    , "[sweat]", "[stuck]", "[rage]"
                    , "[etriumph]", "[mask]", "[flushed]"
                    , "[sunglasses]", "[sob]", "[del]",

                    "[relieved]", "[doubt]",
                    "[confounded]", "[sleepy]",
                    "[sleeping]", "[disappointed_relieved]"
                    , "[tire]", "[astonished]",
                    "[buttonNose]", "[frowning]", "[shutUp]",
                    "[expressionless]", "[confused]", "[tired_face]",
                    "[grin]", "[unamused]", "[persevere]",
                    "[relaxed]", "[pensive]", "[no_mouth]",
                    "[worried]", "[cry]", "[pill]",
                    "[del]",

                    "[celebrate]", "[gift]", "[birthday]",
                    "[paray]", "[ok_hand]", "[first]",
                    "[v]", "[punch]", "[thumbsup]",
                    "[thumbsdown]", "[muscle]", "[maleficeent]",
                    "[broken_heart]", "[heart]", "[taxi]",
                    "[eyes]", "[rose]",
                    "[ghost]", "[lip]", "[fireworks]",
                    "[balloon]", "[clasphands]", "[bye]",
                    "[del]"
            };
    private final String regex;
    private final Pattern pattern;
    private Context context;
    private int emojiDpSize;
    private ArrayMap<String, Integer> faceBook;


    public EmojiInputFilter(Context context) {
        this.context = context;

        regex = buildRegex();
        pattern = Pattern.compile(regex);
        faceBook = buildEmojiToRes();

        float scale = Resources.getSystem().getDisplayMetrics().density;
        emojiDpSize = DisplayUtil.dip2px(context, EmojiInputFilter.EMOJI_DRAWABLE_BOUND_SIZE_DP);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (end > start) source = source.subSequence(start, end);

        // 新输入的字符串为空（删除剪切等）
        if (TextUtils.isEmpty(source)) return null;

        return source.toString().matches(".*" + regex + ".*") ? addEmojiSpans(source) : null;
    }

    private CharSequence addEmojiSpans(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int resId = faceBook.get(matcher.group());
            Drawable dr = ContextCompat.getDrawable(context, resId);
            dr.setBounds(0, 0, emojiDpSize, emojiDpSize);
            builder.setSpan(new ImageSpan(dr, ImageSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    private String buildRegex() {
        StringBuilder regex = new StringBuilder(faceScr.length * 3);
        regex.append('(');
        for (String s : faceScr) {
            regex.append(Pattern.quote(s)).append('|');
        }
        regex.replace(regex.length() - 1, regex.length(), ")");
        return regex.toString();
    }

    private ArrayMap<String, Integer> buildEmojiToRes() {
        if (faceImages.length != faceScr.length) {
            throw new IllegalStateException("Emoji resource ID/text mismatch");
        }
        ArrayMap<String, Integer> smileyToRes = new ArrayMap<>(faceScr.length);
        for (int i = 0; i < faceScr.length; i++) {
            int resId = faceImages[i];
            smileyToRes.put(faceScr[i], resId);
        }
        return smileyToRes;
    }
}
