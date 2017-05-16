package com.zuga.hotfix;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.umeng.analytics.MobclickAgent;
import com.zuga.hotfix.hotfix.HotFix;
import com.zuga.hotfix.hotfix.packe_ng.PackerNg;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 10/05/2017
 * @since 1.0
 **/
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.zuga.hotfix.SampleApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL)
public class SampleApplicationLike extends DefaultApplicationLike {
    public SampleApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    /**
     * install multiDex before install tinker
     * so we don't need to put the tinker lib classes in the main dex
     *
     * @param base
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        HotFix.init(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final String market = PackerNg.getMarket(getApplication());
        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(getApplication(), "591588f9677baa34ff001eac", market));
    }
}


