package com.zuga.hotfix.hotfix.Net;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 15/05/2017
 * @since 1.0
 **/

public abstract class Callback {
    public abstract void onSuccess(String result);

    public void onError(int errorType) {
    }

    public void onProgressUpdate(int progress){}

    public void onCancelled(){}
}
