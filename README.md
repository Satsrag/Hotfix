# Hotfix

- Refer to tinker, AndResGuard and Packer-ng

    - [tinker](https://github.com/Tencent/tinker): the tencent hotfix dependency.

    - [AndResGuard](https://github.com/shwenzhang/AndResGuard): the tencent proguard resources dependency.

    - [Packer-ng](https://github.com/mcxiaoke/packer-ng-plugin): dependency of packaging multi channel.

    ## Usage

- project build.gradle

```java
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.1'
        classpath('com.tencent.tinker:tinker-patch-gradle-plugin:1.7.9')
        classpath 'com.tencent.mm:AndResGuard-gradle-plugin:1.2.3'
    }
}
```

- module build.gradle

```java
apply plugin: 'com.android.application'

def javaVersion = JavaVersion.VERSION_1_7

android {
    compileSdkVersion 25
    buildToolsVersion "25.0.2"
    defaultConfig {
        applicationId "com.zuga.hotfix"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0.11"
        multiDexEnabled true
        buildConfigField "String", "MESSAGE", "\"I am the base apk\""
        buildConfigField "String", "TINKER_ID", "\"patch-${versionName}\""
        buildConfigField "String", "PLATFORM", "\"all\""
    }
    compileOptions {
        sourceCompatibility javaVersion
        targetCompatibility javaVersion
    }
    dexOptions {
        jumboMode = true
    }
    signingConfigs {
        release {
            try {
                storeFile file("./hotfix.jks")
                storePassword "12345678"
                keyAlias "hotfix"
                keyPassword "12345678"
                v2SigningEnabled false
            } catch (ex) {
                throw new InvalidUserDataException(ex.toString())
            }
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.tencent.tinker:tinker-android-lib:1.7.9'
    provided 'com.tencent.tinker:tinker-android-anno:1.7.9'
    compile 'com.android.support:multidex:1.0.1'
    compile 'com.umeng.analytics:analytics:latest.integration'
}

//Tinker
def bakPath = file("${buildDir}/bakApk/")
def bakDir = "base-1.0.9"
def tinkerVersion = "base-${android.defaultConfig.versionName}"
def tinkerEnable = true

if (tinkerEnable) {
    apply plugin: 'com.tencent.tinker.patch'
    tinkerPatch {
        oldApk = "${bakPath}/${bakDir}/app-release.apk"
        ignoreWarning = false
        useSign = true
        tinkerEnable = true
        buildConfig {
            applyMapping = "${bakPath}/${bakDir}/app-release-mapping.txt"
            applyResourceMapping = "${bakPath}/${bakDir}/app-release-R.txt"
            tinkerId = "${tinkerVersion}"
            keepDexApply = false
            isProtectedApp = false
        }
        dex {
            dexMode = "jar"
            pattern = ["classes*.dex", "assets/secondary-dex-?.jar"]
            loader = ["tinker.sample.android.app.BaseBuildInfo"]
        }
        lib {
            pattern = ["lib/*/*.so"]
        }
        res {
            pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
            ignoreChange = ["assets/sample_meta.txt"]
            largeModSize = 100
        }
        packageConfig {
            configField("patchMessage", "tinker is sample to use")
            configField("platform", "all")
            configField("patchVersion", "1.0")
        }
        sevenZip {
            zipArtifact = "com.tencent.mm:SevenZip:1.2.3"
        }
    }
    apply plugin: 'AndResGuard'
    andResGuard {
        def mf = file("${bakPath}/${bakDir}/app-release-resource_mapping.txt")
        if (!mf.exists()) {
            mappingFile = null
        } else {
            mappingFile = mf
        }
        use7zip = true
        useSign = true
        keepRoot = false
        whiteList = [
                // your icon
                "R.drawable.icon",
                // for fabric
                "R.string.com.crashlytics.*",
                // for umeng update
                "R.string.umeng*",
                "R.string.UM*",
                "R.string.tb_*",
                "R.layout.umeng*",
                "R.layout.tb_*",
                "R.drawable.umeng*",
                "R.drawable.tb_*",
                "R.anim.umeng*",
                "R.color.umeng*",
                "R.color.tb_*",
                "R.style.*UM*",
                "R.style.umeng*",
                "R.id.umeng*",
                // umeng share for sina
                "R.drawable.sina*",
                // for google-services.json
                "R.string.google_app_id",
                "R.string.gcm_defaultSenderId",
                "R.string.default_web_client_id",
                "R.string.ga_trackingId",
                "R.string.firebase_database_url",
                "R.string.google_api_key",
                "R.string.google_crash_reporting_api_key"
        ]
        compressFilePattern = [
                "*.png",
                "*.jpg",
                "*.jpeg",
                "*.gif",
                "resources.arsc"
        ]
        sevenzip {
            artifact = 'com.tencent.mm:SevenZip:1.2.3'
        }
    }

    android.applicationVariants.all { variant ->
        def taskName = variant.name
        tasks.all {
            if ("tinkerPatch${taskName.capitalize()}".equalsIgnoreCase(it.name)) {
                it.doFirst({
                    it.buildApkPath = "${buildDir}/outputs/apk/AndResGuard_${project.getName()}-${taskName}/${project.getName()}-${taskName}_signed_7zip_aligned.apk"
                })
            }
            if (it.name.startsWith("resguard") && taskName.equalsIgnoreCase("release")) {
                it.doLast() {
                    copy {
                        def copyPath = "${bakPath}/${tinkerVersion}"
                        println("resguard release copy task start>>>>>>>>>>")
                        from "${buildDir}/outputs/apk/AndResGuard_${project.getName()}-${taskName}/${project.getName()}-${taskName}_signed_7zip_aligned.apk"
                        into file(copyPath)
                        rename { String fileName ->
                            fileName.replace("${project.getName()}-${taskName}_signed_7zip_aligned.apk", "${project.getName()}-${taskName}.apk")
                        }

                        from "${buildDir}/outputs/mapping/${taskName}/mapping.txt"
                        into file(copyPath)
                        rename { String fileName ->
                            fileName.replace("mapping.txt", "${project.getName()}-${taskName}-mapping.txt")
                        }

                        from "${buildDir}/intermediates/symbols/${taskName}/R.txt"
                        into file(copyPath)
                        rename { String fileName ->
                            fileName.replace("R.txt", "${project.getName()}-${taskName}-R.txt")
                        }
                        from "${buildDir}/outputs/apk/AndResGuard_${project.getName()}-${taskName}/resource_mapping_${project.getName()}-release.txt"
                        into file(copyPath)
                        rename { String fileName ->
                            fileName.replace("resource_mapping_${project.getName()}-release.txt", "${project.getName()}-${taskName}-resource_mapping.txt")
                        }
                        println("copy path : ${copyPath}")
                        println("<<<<<<<<<resgurd release copy task end")
                    }
                }
            }
        }
    }
}
```

- change Application to ApplicationLike

```java
package com.zuga.hotfix;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.umeng.analytics.MobclickAgent;
import com.zuga.hotfix.hotfix.Log.MyLogImp;
import com.zuga.hotfix.hotfix.packe_ng.PackerNg;
import com.zuga.hotfix.hotfix.util.SampleApplicationContext;
import com.zuga.hotfix.hotfix.util.TinkerManager;

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

        SampleApplicationContext.application = getApplication();
        SampleApplicationContext.context = getApplication();
        TinkerManager.setTinkerApplicationLike(this);

        TinkerManager.initFastCrashProtect();
        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);

        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(new MyLogImp());

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this);
        Tinker tinker = Tinker.with(getApplication());
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
```

- MainActivity

```java
package com.zuga.hotfix;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.zuga.hotfix.Net.Callback;
import com.zuga.hotfix.Net.Http;
import com.zuga.hotfix.Net.Params;
import com.zuga.hotfix.hotfix.BuildInfo;
import com.zuga.hotfix.hotfix.util.MD5;
import com.zuga.hotfix.hotfix.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MainActivity extends PermissionActivity {
    private final static String TAG = "MainActivity";
    private String mPatchPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPatchPath = getFilesDir() + "/patch";
        File file = new File(mPatchPath);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "hotfix end", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setBackground(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }

    public void loadPatch(View view) {
        checkPatch();
    }

    public void showInfo(View view) {
        final TextView v = new TextView(this);
        v.setText(String.format("baseId: %s \npatchId %s\nisLoaded: %s\nisEnable: %s"
                , getBaseId(), getPatchId(), isLoaded(), isEnabled()));
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setTextColor(0xFF000000);
        v.setTypeface(Typeface.MONOSPACE);
        final int padding = 16;
        v.setPadding(padding, padding, padding, padding);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private String getBaseId() {
        final Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            return tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID);
        } else {
            return ShareTinkerInternals.getManifestTinkerID(getApplicationContext());
        }
    }

    private String getPatchId() {
        return BuildInfo.TINKER_ID;
    }

    private boolean isLoaded() {
        return Tinker.with(getApplicationContext()).isTinkerLoaded();
    }

    private boolean isEnabled() {
        return Tinker.with(getApplicationContext()).isTinkerEnabled();
    }

    private void checkPatch() {
        String path = "http://192.168.1.113/thinkphp";
        String baseId = getBaseId();
        baseId = baseId.replaceAll("tinker_id_base-", "");
        String patchId = getPatchId();
        patchId = patchId.replaceAll("patch-", "");
        Params params = new Params()
                .addParam("base_version", baseId)
                .addParam("patch_version", patchId);
        Http.getInstance().get(path, params, new Callback() {
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "onSuccess: CheckPatch Net Interface: " + result);
                int et = 0;
                String url = "";
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("et")) {
                        et = jsonObject.getInt("et");
                    }
                    if (jsonObject.has("url")) {
                        url = jsonObject.getString("url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (et != 0) {
                    System.err.println("checkPatch net interface error, et = " + et);
                    clearPatch();
                    return;
                }
                final String savePath = mPatchPath + "/" + MD5.stringToMD5(url);
                if (new File(savePath).exists()) {
                    installPatch(savePath);
                } else {
                    downPatch(url);
                }
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void clearPatch() {
        final File file = new File(mPatchPath);
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            for (File child : files) {
                if (child.isFile()) {
                    child.delete();
                }
            }
        }
    }

    private void downPatch(final String patchPath) {
        final String savePath = mPatchPath + "/" + MD5.stringToMD5(patchPath);
        Http.getInstance().down(patchPath, savePath, new Callback() {
            @Override
            public void onSuccess(String result) {
                installPatch(result);
            }
        });
    }

    private void installPatch(final String patchPath) {
        TinkerInstaller.onReceiveUpgradePatch(getApplication(), patchPath);
    }
}
```

- Download PackerNg-xxx.jar and copy to root project

- create markets.txt in root project and define channel

```java
Google_Market#Google电子市场
anroid#安卓市场
91_market#91商城
sony_market     #sony商城
UC  #UC浏览器
360SearchApp#360SearchApp
HelloMarket
OkMarket
ZugaTech
```


## perform


- clean project

```java
    ./gradlew clean
```

- package base version

```java
   ./gradlew resguardRelease
```

- package multi channel

```java
    java -jar app/build/bakApk/base-1.0.9/app-release.apk markets.txt apks
```

- change code and perform patch version

- change the version name

```java
    versionName "1.0.11"
```

- change the bakDir in module build.gradle

```java
    def bakDir = "base-1.0.9"
```

- package patch version

```java
    ./gradlew resguardRelease tinkerpatchRelease
```

- copy patch package to service

