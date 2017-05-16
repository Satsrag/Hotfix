package com.zuga.hotfix.Net;

import android.text.TextUtils;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 15/05/2017
 * @since 1.0
 **/

public class Params {
    private String params = "";

    public Params addParam(String key, String value) {
        if (TextUtils.isEmpty(params)) {
            params += "?" + key + "=" + value;
        } else {
            params += "&" + key + "=" + value;
        }
        return this;
    }

    public String getParams() {
        return params;
    }

    @Override
    public String toString() {
        return params;
    }
}
