package com.sk.weichat.util.filter;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;

import java.util.HashSet;
import java.util.Set;

/**
 * 限定包含的字符，
 * 类似{@link DigitsKeyListener}的效果，但不会限定弹出数字键盘，
 */
@SuppressWarnings("ALL")
public class CharacterInputFilter implements InputFilter {
    private Set<Character> allCharSet;

    public CharacterInputFilter(String allChar) {
        allCharSet = new HashSet<>(allChar.length());
        for (char ch : allChar.toCharArray()) {
            allCharSet.add(ch);
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        CharSequence in = source.subSequence(start, end);
        for (int i = 0; i < in.length(); i++) {
            char ch = in.charAt(i);
            if (!allCharSet.contains(ch)) {
                return "";
            }
        }
        return null;
    }
}
