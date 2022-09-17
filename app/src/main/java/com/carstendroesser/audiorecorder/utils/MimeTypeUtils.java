package com.carstendroesser.audiorecorder.utils;

import android.webkit.MimeTypeMap;

/**
 * Created by carstendrosser on 24.02.19.
 */

public class MimeTypeUtils {

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
