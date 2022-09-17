package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.carstendroesser.audiorecorder.R;

import java.util.Locale;

/**
 * Created by carstendrosser on 25.11.17.
 */

public class ContactUtils {

    public static String getContactInfo(Context pContext) {
        String model = Build.MODEL;
        int androidVersion = Build.VERSION.SDK_INT;
        String brand = Build.BRAND;
        PackageInfo packageInfo = null;

        try {
            packageInfo = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException pException) {
            pException.printStackTrace();
        }

        String version = "?";
        String installed = "?";
        String updated = "?";

        if (packageInfo != null) {
            version = packageInfo.versionName + " (" + packageInfo.versionCode + ")";
            installed = FormatUtils.toReadableDate(packageInfo.firstInstallTime);
            updated = FormatUtils.toReadableDate(packageInfo.lastUpdateTime);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("Device: " + Build.MANUFACTURER + " " + brand + " " + model + "\n");
        builder.append("Android: " + Build.VERSION.RELEASE + " (" + androidVersion + ")\n");
        builder.append("App: " + version + "\n");
        builder.append("Installed: " + installed + "\n");
        builder.append("Updated: " + updated + "\n");
        builder.append("Location: " + Locale.getDefault().getCountry());
        builder.append("\n\n");
        builder.append(pContext.getString(R.string.contact_email_introduction));
        builder.append("\n\n\n");

        return builder.toString();
    }

}
