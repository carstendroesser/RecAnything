package com.carstendroesser.audiorecorder.utils;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by carstendrosser on 01.12.17.
 */

public class AnimationFactory {

    public static Animation getFlashingAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.3f);
        alphaAnimation.setDuration(400);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        return alphaAnimation;
    }

}
