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
def bakPath = file("${buildDir}/bakApk/")
def bakDir = "base"
def patchVersion = "1" // package new base version this value must be set 0;
def baseVersionCode = 1
def baseVersionName = "1.0.0"

android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    defaultConfig {
        applicationId "com.zuga.hotfix"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode baseVersionCode
        versionName "${baseVersionName}"
        multiDexEnabled true
        buildConfigField "String", "MESSAGE", "\"I am the base apk\""
        buildConfigField "String", "TINKER_ID", "\"${patchVersion}\""
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
```

- MainActivity

```java
package com.zuga.hotfix;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zuga.hotfix.hotfix.HotFixHandle;
import com.zuga.hotfix.hotfix.util.Utils;

public class MainActivity extends PermissionActivity {
    private final static String TAG = "MainActivity";
    private HotFixHandle hotFixHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zuga.hotfix.R.layout.activity_main);
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
        getHotFixHandle().loadPatch();
    }

    public void showInfo(View view) {
        Toast.makeText(this, getHotFixHandle().getInfo(), Toast.LENGTH_LONG).show();
    }

    private HotFixHandle getHotFixHandle() {
        if (hotFixHandle == null) {
            hotFixHandle = new HotFixHandle();
        }
        return hotFixHandle;
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
    def patchVersion = "0" // package new base version this value must be set 0;
    def baseVersionCode = 1
    def baseVersionName = "1.0.0"
```

```java
   ./gradlew resguardRelease
```

- package multi channel

```java
    java -jar app/build/bakApk/base-1.0.0/app-release.apk markets.txt apks
```

- change code and perform patch version

- rename app/build/bakApk/base-1.0.0 to app/build/bakApk/base

- change the version name in build.gradle

```java
    def bakDir = "base"
    def patchVersion = "1" // package new base version this value must be set 0;
    def baseVersionCode = 1
    def baseVersionName = "1.0.0"
```

- package patch version

```java
    ./gradlew resguardRelease tinkerpatchRelease
```

- copy patch package to service

