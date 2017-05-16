package com.zuga.hotfix.Net;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 15/05/2017
 * @since 1.0
 **/

public class Http {
    private static final Http INSTANCE = new Http();

    private Http() {
    }

    public void get(String url, Params params, Callback callback) {
        new HttpHandle(callback).get(url, params);
    }

    public void down(String url, String savePath, Callback callback) {
        new HttpHandle(callback).down(url, savePath);
    }

    public static Http getInstance() {
        return INSTANCE;
    }
}
