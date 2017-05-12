package com.zuga.hotfix;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ${User}
 * @version 1.0
 * @see null
 * ${Time}
 * @since 1.0
 */

public class PermissionActivity extends AppCompatActivity {
    private PermissionCallback callback;
    private int resDeniedString = 0;


    public interface PermissionCallback {
        void isAllAccess(boolean isAllAccess);
    }

    protected void askPermission(int resDeniedString, PermissionCallback callback, String[] permissions) {
        this.resDeniedString = resDeniedString;
        this.callback = callback;
        List<String> needPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                needPermissions.add(permission);
            }
        }
        if (!needPermissions.isEmpty()) {
            String[] permissionArray = new String[needPermissions.size()];
            needPermissions.toArray(permissionArray);
            ActivityCompat.requestPermissions(this, permissionArray, 7838);
            return;
        }
        if (this.callback != null) callback.isAllAccess(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 7838) {
            boolean isAllAccess = true;
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    isAllAccess = false;
                    break;
                }
            }
            if (isAllAccess) {
                if (callback != null) callback.isAllAccess(true);
            } else {
                if (resDeniedString == 0) {
                    if (callback != null) callback.isAllAccess(false);
                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
