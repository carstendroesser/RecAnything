package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by carstendrosser on 02.11.15.
 */
public class DensityConverter {

    /**
     * Converts dp to px.
     *
     * @param pContext
     * @param pDensityPoints
     * @return the given dp in px
     */
    public static int convertDpToPixels(Context pContext, int pDensityPoints) {
        Resources resources = pContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pDensityPoints, resources.getDisplayMetrics());
    }

}
