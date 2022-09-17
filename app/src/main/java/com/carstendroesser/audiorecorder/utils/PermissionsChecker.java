package com.carstendroesser.audiorecorder.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by carstendrosser on 23.05.17.
 */

public class PermissionsChecker {

    public static boolean hasSystemSettingsWritePermission(Context pContext) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(pContext);
        }
        return retVal;
    }

    public static boolean hasPermission(Context pContext, String pPermission) {
        int result = ContextCompat.checkSelfPermission(pContext, pPermission);
        if (result == PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasDoNotDisturbPermission(Context pContext) {
        NotificationManager notificationManager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            return false;
        } else {
            return true;
        }
    }

}
