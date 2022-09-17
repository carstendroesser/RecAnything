package com.carstendroesser.audiorecorder.utils;

import android.content.Context;

import com.carstendroesser.audiorecorder.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by carstendrosser on 11.10.15.
 */
public class FileFactory {

    private static final int FILENAME_COUNT = 0;
    private static final int FILENAME_TIMESTAMP = 1;
    private static final int FILENAME_COUNT_TIMESTAMP = 2;
    private static final int FILENAME_TIMESTAMP_ONLY = 3;

    //the fileprefix for all temporary files
    private static final String mDefaultTemporaryName = "temporary_";

    //the fileprefix, same for all recordings
    private static final String mDefaultRecordName = "recording_";

    //the mediaprefix, same for all media
    private static final String mDefaultMediaName = "media_";

    //the paintingprefix, same for all paintings
    private static final String mDefaultPaintingName = "painting_";

    /**
     * Creates a unique filename. Will also create all directories
     * if they do not exist yet.
     *
     * @return a unique path for a new recording
     * @throws IOException thrown if something went wrong (:
     */
    public static String createRecordFilePath(Context pContext, String pSuffix) throws IOException {
        long timeInMs = new Date().getTime();
        File directory = new File(getPath(pContext));
        if (!directory.exists()) {
            directory.mkdir();
        }

        int defaultFileNameValue = Settings.getDefaultFilenameValue(pContext);

        File file;
        long count = DatabaseHelper.getInstance(pContext).getRecordsCount() + 1;

        switch (defaultFileNameValue) {
            case FILENAME_COUNT:
                file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + pSuffix);
                int duplicate = 1;
                while (file.exists()) {
                    duplicate += 1;
                    file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + "-" + duplicate + pSuffix);
                }
                break;
            case FILENAME_TIMESTAMP:
                file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + timeInMs + pSuffix);
                break;
            case FILENAME_COUNT_TIMESTAMP:
                file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + "-" + timeInMs + pSuffix);
                int duplicate2 = 1;
                while (file.exists()) {
                    duplicate2 += 1;
                    file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + "-" + duplicate2 + "-" + timeInMs + pSuffix);
                }
                break;
            case FILENAME_TIMESTAMP_ONLY:
                file = new File(directory, "" + timeInMs + pSuffix);
                break;
            default:
                file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + pSuffix);
                int duplicate3 = 1;
                while (file.exists()) {
                    duplicate3 += 1;
                    file = new File(directory, pContext.getResources().getString(R.string.recording) + "-" + count + "-" + duplicate3 + pSuffix);
                }
                break;
        }

        return file.getAbsolutePath();
    }

    /**
     * Creates a unique filename like <i>media_timeInMs</i>. Will also create all directories
     * if they do not exist yet.
     *
     * @return a unique path for a new media
     */
    public static String createMediaFilePath(Context pContext) {
        long timeInMs = new Date().getTime();
        File directory = new File(getPath(pContext));
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, mDefaultMediaName + timeInMs);
        return file.getAbsolutePath();
    }

    /**
     * Creates a unique filename like <i>painting_timeInMs</i>. Will also create all directories
     * if they do not exist yet.
     *
     * @return a unique path for a new painting
     */
    public static String createPaintingFilePath(Context pContext) {
        long timeInMs = new Date().getTime();
        File directory = new File(getPath(pContext));
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, mDefaultPaintingName + timeInMs + ".jpg");
        return file.getAbsolutePath();
    }

    /**
     * Creates a temporary file in the same folder as all records are. Mostly used
     * to trim a recording.
     *
     * @return the newly created temporary file's path
     */
    public static String createTemporaryFilePath(Context pContext, String pSuffix) {
        long timeInMs = new Date().getTime();
        File directory = new File(getPath(pContext));
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, mDefaultTemporaryName + timeInMs + pSuffix);
        return file.getAbsolutePath();
    }

    private static String getPath(Context pContext) {
        return Settings.getRecordingPath(pContext);
    }

}
