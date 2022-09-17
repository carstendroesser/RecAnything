package com.carstendroesser.audiorecorder.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by carstendrosser on 23.10.15.
 */
public class FormatUtils {

    /**
     * Turns milliseconds into a human-readable string in the format mm:ss.
     *
     * @param pMilliseconds the time to format
     * @return a human-readable time
     */
    public static String toReadableDuration(long pMilliseconds) {
        Date date = new Date(pMilliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(date);
    }

    /**
     * Turns a number of bytes into readable size.
     *
     * @param pBytes the number of bytes
     * @return readable size
     */
    public static String toReadableSize(long pBytes) {
        //let magic happen
        if (pBytes < 1024) return pBytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(pBytes)) / 10;
        return String.format("%.2f %sB", (double) pBytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    /**
     * Turns milliseconds into a readable date.
     *
     * @param pMilliseconds the milliseconds to transform
     * @return a date as string
     */
    public static String toReadableDate(long pMilliseconds) {
        Date date = new Date(pMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }


}
