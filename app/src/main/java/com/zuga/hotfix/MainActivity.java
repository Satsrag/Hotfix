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
