package com.sk.weichat.bean;

import java.util.Comparator;

public class EmojiComp implements Comparator<Emoji> {
    @Override
    public int compare(Emoji o1, Emoji o2) {
        return -(o1.getCount() - o2.getCount() > 0 ? 1 : (o1.getCount() - o2.getCount() == 0 ? 0 : -1));
    }
}
