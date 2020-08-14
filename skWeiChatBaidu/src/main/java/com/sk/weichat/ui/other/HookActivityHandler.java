package com.sk.weichat.ui.other;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class HookActivityHandler implements InvocationHandler {
    Object object;

    public HookActivityHandler(Object field) {
        this.object = field;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d("HookActivityHandler", "invoke: " + method.getName());
        return method.invoke(object, args);
    }
}
