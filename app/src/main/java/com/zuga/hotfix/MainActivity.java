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
