package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Created by carstendrosser on 07.11.17.
 */

public class Settings {

    public static boolean isEchoCancelingWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("echocanceling", false);
    }

    public static boolean isNoiseCancelingWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("noisecanceling", false);
    }

    public static boolean isAutogainWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("autogain", false);
    }

    public static int getSamplerate(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("samplerate", "44100"));
    }

    public static int getRecordingSource(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        int source = Integer.parseInt(prefs.getString("source", "1"));
        switch (source) {
            case 1:
                return MediaRecorder.AudioSource.DEFAULT;
            case 2:
                return MediaRecorder.AudioSource.CAMCORDER;
            case 3:
                return MediaRecorder.AudioSource.MIC;
            default:
                return MediaRecorder.AudioSource.DEFAULT;
        }
    }

    public static float getGainValue(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Float.parseFloat(prefs.getString("gainvalue", "1"));
    }

    public static void setGainValue(Context pContext, float pGainValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putString("gainvalue", "" + pGainValue).commit();
    }

    public static int getSeekValue(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("seekvalue", "1"));
    }

    public static int getDetectSilenceDelayValue(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("detectsilencevalue", "2"));
    }

    public static boolean isDetectSilenceWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("detectsilence", true);
    }

    public static void setDetectSilenceWanted(Context pContext, boolean pWanted) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putBoolean("detectsilence", pWanted).commit();
    }

    public static boolean isSeekingWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("seeking", false);
    }

    public static int getDetectSilenceGate(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("detectsilencegate", "1000"));
    }

    public static boolean isSilentRingerModeWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("silentringermode", false);
    }

    public static void setRecordingPath(Context pContext, String pPath) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putString("path", pPath).commit();
    }

    public static String getRecordingPath(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getString("path", Environment.getExternalStorageDirectory().getPath() + "/audiorecorder");
    }

    public static int getDefaultFilenameValue(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("defaultfilename", "0"));
    }

    public static int getChoosenRecorder(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("recorder", "0"));
    }

    public static void setChoosenRecorder(Context pContext, int pVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putString("recorder", "" + pVal).commit();
    }

    public static boolean isAutoplayWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("autoplay", false);
    }

    public static int getRewindSeconds(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return Integer.parseInt(prefs.getString("rewindseconds", "5"));
    }

    public static boolean isQuickRenamingWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("quickrenaming", false);
    }

    public static boolean isConfirmAbortWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("confirmabort", true);
    }

    public static boolean isPauseOnLowBatteryWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("pauseonlowbattery", false);
    }

    public static boolean isAutouploadWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("cloudautomaticupload", false);
    }

    public static boolean setAutouploadWanted(Context pContext, boolean pValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.edit().putBoolean("cloudautomaticupload", pValue).commit();
    }

    public static void setSilentRingerModeWanted(Context pContext, boolean pValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putBoolean("silentringermode", pValue).commit();
    }

    public static boolean isInternalPlayerWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("useinternalplayer", true);
    }

    public static boolean isKeepScreenOnWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("keepscreenon", true);
    }

    public static boolean isMediaStoreWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("mediastore", false);
    }

}
