package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.os.StatFs;

import java.io.File;

/**
 * Created by carstendrosser on 23.05.17.
 */

public class StorageUtils {

    /**
     * Calculates the free space on the rootpath.
     *
     * @return free byte in storage
     */
    public static long getFreeStorage(Context pContext) {
        try {
            File file = new File(Settings.getRecordingPath(pContext));
            return file.getUsableSpace();
        } catch (SecurityException pSecurityException) {
            try {
                String storage = Settings.getRecordingPath(pContext);
                StatFs stat = new StatFs(storage);
                return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
            } catch (IllegalArgumentException pIllegalArgumentException) {
                return 0;
            }
        }
    }

}
