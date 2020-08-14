package com.sk.weichat.sortlist;

import java.util.Comparator;

/**
 * 根据首字母排序 #排序到最后面
 */
public class BaseComparator<T> implements Comparator<BaseSortModel<T>> {

    public int compare(BaseSortModel<T> o1, BaseSortModel<T> o2) {
        if (o1.getFirstLetter().equals("#")) {
            if (o2.getFirstLetter().equals("#")) {
                return o1.getWholeSpell().compareTo(o2.getWholeSpell());
            } else {
                return 1;
            }
        } else {
            if (o2.getFirstLetter().equals("#")) {
                return -1;
            } else {
                return o1.getWholeSpell().compareTo(o2.getWholeSpell());
            }
        }

    }
}
