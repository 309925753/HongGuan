package com.sk.weichat.util.filter;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://onebigfunction.com/android/2015/09/01/regex-input-filter/
 */
@SuppressWarnings("ALL")
public class RegexInputFilter implements InputFilter {

    private static final String CLASS_NAME = RegexInputFilter.class.getSimpleName();
    private Pattern mPattern;

    /**
     * Convenience constructor, builds Pattern object from a String
     *
     * @param pattern Regex string to build pattern from.
     */
    public RegexInputFilter(String pattern) {
        this(Pattern.compile(pattern));
    }

    public RegexInputFilter(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException(CLASS_NAME + " requires a regex.");
        }

        mPattern = pattern;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        Matcher matcher = mPattern.matcher(source);
        if (!matcher.matches()) {
            return "";
        }

        return null;
    }
}
