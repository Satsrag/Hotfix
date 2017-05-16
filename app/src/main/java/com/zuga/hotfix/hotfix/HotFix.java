package com.zuga.hotfix.hotfix;

import android.support.multidex.MultiDex;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.zuga.hotfix.hotfix.Log.MyLogImp;
import com.zuga.hotfix.hotfix.util.SampleApplicationContext;
import com.zuga.hotfix.hotfix.util.TinkerManager;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 16/05/2017
 * @since 1.0
 **/

public class HotFix {
    public static void init(ApplicationLike app) {
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(app.getApplication());

        SampleApplicationContext.application = app.getApplication();
        SampleApplicationContext.context = app.getApplication();
        TinkerManager.setTinkerApplicationLike(app);

        TinkerManager.initFastCrashProtect();
        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);

        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(new MyLogImp());

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(app);
        Tinker tinker = Tinker.with(app.getApplication());
    }
}
