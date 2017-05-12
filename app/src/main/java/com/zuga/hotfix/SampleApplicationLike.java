package com.zuga.hotfix;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.zuga.hotfix.hotfix.SampleResultService;

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
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
        PatchListener patchListener = new PatchListener() {
            @Override
            public int onPatchReceived(String path) {
                return 0;
            }
        };
        UpgradePatchRetry.getInstance(getApplication()).setRetryEnable(true);
        TinkerInstaller.install(this, new DefaultLoadReporter(getApplication()),
                new DefaultPatchReporter(getApplication()), patchListener,
                SampleResultService.class, null);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}


