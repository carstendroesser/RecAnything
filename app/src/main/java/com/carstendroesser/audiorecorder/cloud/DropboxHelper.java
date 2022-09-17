package com.carstendroesser.audiorecorder.cloud;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.carstendroesser.audiorecorder.utils.Preferences;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Created by carstendrosser on 10.01.18.
 */

public class DropboxHelper {

    private static DbxClientV2 sDbxClient;

    public static void init(Context pContext, String pAccessToken) {
        if (sDbxClient == null) {

            String appname = "RecAnything";
            String appversion = "-1";

            try {
                PackageInfo pInfo = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), 0);
                appversion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }

            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(appname + "/" + appversion)
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();

            sDbxClient = new DbxClientV2(requestConfig, pAccessToken);
        }
    }

    public static boolean isInitialized() {
        return sDbxClient != null;
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }

    public static void reset(final Context pContext, final OnResetDoneCallback pCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (sDbxClient != null) {
                    try {
                        sDbxClient.auth().tokenRevoke();
                        Preferences.setDropboxToken(pContext, null);
                        sDbxClient = null;
                        AuthActivity.result = null;

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                pCallback.onResetSuccess();
                            }
                        });

                    } catch (Exception pException) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                pCallback.onResetError();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public interface OnResetDoneCallback {
        void onResetSuccess();

        void onResetError();
    }


}
