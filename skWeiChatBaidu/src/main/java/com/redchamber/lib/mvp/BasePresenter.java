package com.redchamber.lib.mvp;


import com.redchamber.lib.net.rx.RxManager;

public abstract class BasePresenter<E, T> {
    public E mModel;
    public T mView;
    private RxManager rxManager;

    public void setMV(E model, T view) {
        this.mModel = model;
        this.mView = view;
    }

    public RxManager getRxManager() {
        if (rxManager == null) {
            synchronized (BasePresenter.class) {
                if (rxManager == null) {
                    rxManager = new RxManager();
                }
            }
        }
        return rxManager;
    }

    public void onDestroy() {
        getRxManager().clear();
        if (rxManager != null) {
            rxManager = null;
        }
    }
}
