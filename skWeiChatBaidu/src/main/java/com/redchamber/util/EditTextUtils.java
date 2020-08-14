package com.redchamber.util;

import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Spanned;
import android.widget.EditText;

public class EditTextUtils {

    /**
     * 禁止EditText输入空格
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpace(EditText editText, int maxLength) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if (charSequence.equals(" ")) {
                    return "";
                }
                return charSequence;
            }
        };
        editText.setFilters(new InputFilter[]{filter, new LengthFilter(maxLength)});
    }

}
