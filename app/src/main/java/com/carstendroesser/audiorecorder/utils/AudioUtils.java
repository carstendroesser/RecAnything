package com.carstendroesser.audiorecorder.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;

import java.io.File;

/**
 * Created by carstendrosser on 22.05.17.
 */

public class AudioUtils {

    public static void setAsRingtone(Context pContext, Record pRecord) {
        File file = new File(pRecord.getPath());

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, pRecord.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.ARTIST, pContext.getString(R.string.app_name));
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        pContext.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
        Uri newUri = pContext.getContentResolver().insert(uri, values);

        RingtoneManager.setActualDefaultRingtoneUri(pContext, RingtoneManager.TYPE_RINGTONE, newUri);
    }

    public static void turnToSilentRingerMode(Context pContext) {
        AudioManager audioManager = (AudioManager) pContext.getSystemService(Context.AUDIO_SERVICE);
        Preferences.setLastRingerMode(pContext, audioManager.getRingerMode());
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public static void restoreSavedRingerMode(Context pContext) {
        AudioManager audioManager = (AudioManager) pContext.getSystemService(Context.AUDIO_SERVICE);
        int modeToRestore = Preferences.getLastRingerMode(pContext);
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT && modeToRestore != -1) {
            audioManager.setRingerMode(modeToRestore);
        }
    }

    public static void addToMediaStore(Context pContext, String pName, String pPath) {
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, pName);
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.ARTIST, pContext.getString(R.string.app_name));
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        values.put(MediaStore.Audio.Media.DATA, pPath);
        ContentResolver contentResolver = pContext.getContentResolver();

        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        pContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
    }

}
