package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.FileProvider;

import com.carstendroesser.audiorecorder.BuildConfig;
import com.carstendroesser.audiorecorder.models.Record;

import java.io.File;

/**
 * Created by carstendrosser on 01.03.16.
 */
public class IntentFactory {

    /**
     * Generates an intent used to open other apps, handling the playback
     * of the given recording.
     *
     * @param pRecord the record to play
     * @return an intent to open the specific applications
     */
    public static Intent getExternalPlayerIntentFor(Context pContext, Record pRecord) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(pContext, "com.carstendroesser.audiorecorder.utils.AudioFileProvider", new File(pRecord.getPath()));
        //Uri uri = FileProvider.getUriForFile(pContext, BuildConfig.APPLICATION_ID + ".fileprovider", new File(pRecord.getPath()));
        intent.setDataAndType(uri, "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    public static Intent getMarketIntent(Context pContext) {
        final String appPackageName = pContext.getPackageName();
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        } catch (android.content.ActivityNotFoundException pException) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
        return intent;
    }

    /**
     * Generates an intent used to share the audiofile only to another
     * app.
     *
     * @param pRecord the record which's audiofile shall be sent
     * @return a shareintent
     */
    public static Intent getShareIntentFor(Record pRecord) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(MimeTypeUtils.getMimeType(pRecord.getPath()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(pRecord.getPath())));
        return shareIntent;
    }

    public static Intent getSystemSettingsPermissionIntent(Context pContext) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + pContext.getPackageName()));
        }
        return intent;
    }

    public static Intent getContactIntent(Context pContext) {
        Intent mailto = new Intent(Intent.ACTION_SEND);
        mailto.setType("message/rfc822");
        mailto.putExtra(Intent.EXTRA_EMAIL, new String[]{"cadr.market@gmail.com"});
        mailto.putExtra(Intent.EXTRA_SUBJECT, "[Audiorecorder] Contact");
        mailto.putExtra(Intent.EXTRA_TEXT, ContactUtils.getContactInfo(pContext));
        return mailto;
    }

    public static Intent getDoNotDisturbPermissionIntent() {
        return new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
    }

}
