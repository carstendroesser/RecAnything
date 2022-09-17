package com.carstendroesser.audiorecorder.utils;

import com.carstendroesser.audiorecorder.views.AmplitudeView;

/**
 * Created by carstendrosser on 28.09.16.
 */

public class AmplitudeInterpolator {

    /**
     * Circle f(x) for a given amplitude. Uses AmplitudeView.MAX_AMPLITUDE as maximum.
     * M(MAX_AMPLITUDE/0), r = MAX_AMPLITUDE
     *
     * @param pAmplitude a amplitude to calculate f(x) for
     * @return f(pAmplitude)
     */
    public static float getPercentForAmplitude(int pAmplitude) {
        return (float) Math.sqrt(AmplitudeView.MAX_AMPLITUDE * AmplitudeView.MAX_AMPLITUDE
                - ((float) Math.pow((pAmplitude - AmplitudeView.MAX_AMPLITUDE), 2))) / AmplitudeView.MAX_AMPLITUDE;
    }

}
