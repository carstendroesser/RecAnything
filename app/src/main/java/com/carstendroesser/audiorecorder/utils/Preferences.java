package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by carstendrosser on 21.11.17.
 */

public class Preferences {

    public static DatabaseHelper.Sorting getRecordingsSorting(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return DatabaseHelper.Sorting.getSortingById(prefs.getInt("recordingssorting", 2));
    }

    public static void setRecordingsSorting(Context pContext, DatabaseHelper.Sorting pSorting) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putInt("recordingssorting", pSorting.mId).commit();
    }

    public static void setLastRingerMode(Context pContext, int pRingerMode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putInt("lastringermode", pRingerMode).commit();
    }

    public static int getLastRingerMode(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getInt("lastringermode", -1);
    }

    public static int getAppStartingCounts(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getInt("starts", -1);
    }

    public static void setAppStartingCounts(Context pContext, int pStarts) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putInt("starts", pStarts).commit();
    }

    public static void setRateAppDialogWasShown(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putBoolean("rateappdialogwasshown", true).commit();
    }

    public static boolean getRateAppDialogWasShown(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("rateappdialogwasshown", false);
    }

    public static void setIsFreeVersionHintWanted(Context pContext, boolean pIsFreeVersionWanted) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putBoolean("isfreeversionwanted", pIsFreeVersionWanted).commit();
    }

    public static boolean getIsFreeVersionHintWanted(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("isfreeversionwanted", true);
    }

    public static void setIsProVersion(Context pContext, boolean pIsProVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        prefs.edit().putBoolean("pro", pIsProVersion).commit();
    }

    public static boolean getIsProVersion(Context pContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
        return prefs.getBoolean("pro", false);
    }

    public static String getDropboxToken(Context pContext) {
        SharedPreferences prefs = pContext.getSharedPreferences("keys", MODE_PRIVATE);
        return prefs.getString("access-token-dropbox", null);
    }

    public static void setDropboxToken(Context pContext, String pToken) {
        SharedPreferences prefs = pContext.getSharedPreferences("keys", MODE_PRIVATE);
        prefs.edit().putString("access-token-dropbox", pToken).commit();
    }

}
