package com.xuan.xuanhttplibrary.okhttp.result;

import java.util.List;

/**
 * 可以直接使用ObjectResult<List<T>>，
 */
public class ArrayResult<T> extends Result {
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
