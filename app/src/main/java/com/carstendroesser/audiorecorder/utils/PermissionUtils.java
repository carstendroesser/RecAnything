package com.carstendroesser.audiorecorder.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by carstendrosser on 12.08.17.
 */

public class PermissionUtils {

    public static final int REQUEST_CODE = 1;

    public static boolean requestPermissions(Activity pActivity, String[] pPermissions, int pRequestCode) {
        ArrayList<String> notGrantedPermissions = new ArrayList<String>();

        for (String permission : pPermissions) {
            if (ContextCompat.checkSelfPermission(pActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermissions.add(permission);
            }
        }

        String[] permissionsNeededArray = new String[notGrantedPermissions.size()];
        permissionsNeededArray = notGrantedPermissions.toArray(permissionsNeededArray);

        if (permissionsNeededArray.length != 0) {
            ActivityCompat.requestPermissions(
                    pActivity,
                    permissionsNeededArray,
                    pRequestCode);
            return true;
        }

        return false;
    }

}
