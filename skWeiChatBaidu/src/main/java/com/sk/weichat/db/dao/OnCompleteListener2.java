package com.sk.weichat.db.dao;

import androidx.annotation.WorkerThread;

public interface OnCompleteListener2 {// User to FriendDao addAttentionUsers addRooms

    @WorkerThread
    void onLoading(int progressRate, int sum);

    @WorkerThread
    void onCompleted();
}
