package com.carstendroesser.audiorecorder.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.activities.MainActivity;

/**
 * Created by carstendrosser on 10.10.15.
 */
public class NotificationFactory {

    public static final int NOTIFICATION_ID = 2707;

    //we do not want to have object of this class
    private NotificationFactory() {
    }

    public static Notification getNotificationWithText(Context pContext, String pText) {
        Intent intent = new Intent(pContext, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(pContext);
        mNotificationBuilder.setSmallIcon(R.drawable.recmic)
                .setContentTitle(pContext.getString(R.string.app_name))
                .setContentText(pText)
                .setContentIntent(PendingIntent.getActivity(pContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setOnlyAlertOnce(true)
                .setOngoing(true);
        return mNotificationBuilder.build();
    }

}
