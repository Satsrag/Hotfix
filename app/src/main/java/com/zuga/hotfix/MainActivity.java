package com.zuga.hotfix;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.loader.shareutil.ShareConstants;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.iv_panda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hot fix end", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadPatch(View view) {
        final String path = Environment.getExternalStorageDirectory() + "/patch.apk";
        Log.e(TAG, "loadPatch: " + path);
        TinkerInstaller.onReceiveUpgradePatch(getApplication(), path);
        TinkerLoadResult result = Tinker.with(this).getTinkerLoadResultIfPresent();
        boolean tinkerLoaded = Tinker.with(this).isTinkerLoaded();
        if (tinkerLoaded) {
            String TinkerId = result.getPackageConfigByName(ShareConstants.TINKER_ID);
            String newTinkerId = result.getPackageConfigByName(ShareConstants.NEW_TINKER_ID);
            Log.e(TAG, "loadPatch: TinkerId: " + TinkerId);
            Log.e(TAG, "loadPatch: NewTinkerId: " + newTinkerId);
        }
    }
}
