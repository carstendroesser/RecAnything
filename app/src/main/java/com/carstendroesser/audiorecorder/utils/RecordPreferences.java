package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by carstendrosser on 17.10.15.
 */
public class RecordPreferences {

    private static String DEFAULT_PREFERENCES = "record_preferences";
    private static String LAST_RECORD_ID = "last_record_id";

    /**
     * Saves the last record's id.
     *
     * @param pContext we need that
     * @param pId      the record's id
     */
    public static void putLastRecordId(Context pContext, int pId) {
        SharedPreferences preferences = pContext.getSharedPreferences(DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putInt(LAST_RECORD_ID, pId).commit();
    }

    /**
     * Gets the last record's id. Used to load the last record into the toplayout.
     *
     * @param pContext we need that
     * @return the id of the last record
     */
    public static int getLastRecordId(Context pContext) {
        SharedPreferences preferences = pContext.getSharedPreferences(DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getInt(LAST_RECORD_ID, -1);
    }

}
