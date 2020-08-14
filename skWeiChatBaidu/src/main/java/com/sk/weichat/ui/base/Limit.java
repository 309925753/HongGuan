package com.sk.weichat.ui.base;

public class Limit {
    private CoreManager core;

    Limit(CoreManager coreManager) {
        core = coreManager;
    }

    public boolean cannotCreateGroup() {
        return core.getConfig().ordinaryUserCannotCreateGroup
                && core.getSelf().isOrdinaryUser();
    }

    public boolean cannotSearchFriend() {
        return core.getConfig().isHideSearchFriend
                || (core.getConfig().ordinaryUserCannotSearchFriend
                && core.getSelf().isOrdinaryUser());
    }
}
