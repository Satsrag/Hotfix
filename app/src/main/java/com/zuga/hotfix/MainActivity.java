package com.zuga.hotfix;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "hot fix end", Toast.LENGTH_SHORT).show();
    }

    public void loadPatch(View view) {
        TinkerInstaller.onReceiveUpgradePatch(getApplication(), Environment.getExternalStorageDirectory() + "patch.apk");
    }
}
