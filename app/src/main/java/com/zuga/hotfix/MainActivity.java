package com.zuga.hotfix;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadPatch(View view) {
        TinkerInstaller.onReceiveUpgradePatch(getApplication(), Environment.getExternalStorageDirectory() + "patch.apk");
    }
}
