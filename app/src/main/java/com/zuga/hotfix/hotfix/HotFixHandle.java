package com.zuga.hotfix.hotfix;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.zuga.hotfix.hotfix.Net.Callback;
import com.zuga.hotfix.hotfix.Net.Http;
import com.zuga.hotfix.hotfix.Net.Params;
import com.zuga.hotfix.hotfix.util.MD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.ContentValues.TAG;
import static com.zuga.hotfix.hotfix.util.SampleApplicationContext.context;

/**
 * @author saqrag
 * @version 1.0
 * @see null
 * 16/05/2017
 * @since 1.0
 **/

public class HotFixHandle {
    private String mPatchPath;

    public HotFixHandle() {
        mPatchPath = context.getFilesDir() + "/patch";
        File file = new File(mPatchPath);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
    }

    public final void loadPatch() {
        checkPatch();
    }

    public String getInfo() {
        return String.format("baseId: %s\npatchId: %s", getVersionName(), getPatchId());
    }

    private String getBaseId() {
        final Tinker tinker = Tinker.with(context);
        if (tinker.isTinkerLoaded()) {
            return tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID);
        } else {
            return ShareTinkerInternals.getManifestTinkerID(context);
        }
    }

    private String getPatchId() {
        return BuildInfo.TINKER_ID;
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
        TinkerInstaller.onReceiveUpgradePatch(context, patchPath);
    }

    private String getVersionName() {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
