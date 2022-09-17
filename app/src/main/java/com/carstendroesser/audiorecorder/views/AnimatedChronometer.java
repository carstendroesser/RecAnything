package com.carstendroesser.audiorecorder.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Chronometer;

import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 01.12.17.
 */

public class AnimatedChronometer extends Chronometer {

    private SpannableStringBuilder mSpannableStringBuilder;
    private Integer mStartColor;
    private Integer mEndColor;
    private ValueAnimator mColorAnimator;
    private ForegroundColorSpan mForegroundColorSpan;
    private boolean mAnimate = false;

    public AnimatedChronometer(Context context) {
        this(context, null);
    }

    public AnimatedChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setBase(long pBase) {
        mAnimate = false;
        super.setBase(pBase);
        mAnimate = true;
    }

    @Override
    public void start() {
        mAnimate = false;
        super.start();
        mAnimate = true;
    }

    @Override
    public void setText(final CharSequence pTextToSet, final BufferType pType) {

        if (!mAnimate) {
            super.setText(pTextToSet, pType);
            return;
        }

        if (mColorAnimator == null) {
            mStartColor = getResources().getColor(R.color.transparent);
            mEndColor = getResources().getColor(R.color.white);
            mColorAnimator = ValueAnimator.ofArgb(mStartColor, mEndColor);
            mColorAnimator.setInterpolator(new DecelerateInterpolator());
            mColorAnimator.setDuration(500);

            mSpannableStringBuilder = new SpannableStringBuilder();
        }


        if (pTextToSet.length() == 5) {
            int start = 0;
            final int end = pTextToSet.length();

            if (pTextToSet.length() == getText().length()) {
                if (pTextToSet.charAt(0) == getText().charAt(0)) {
                    start = 1;
                    if (pTextToSet.charAt(1) == getText().charAt(1)) {
                        start = 3;
                        if (pTextToSet.charAt(3) == getText().charAt(3)) {
                            start = 4;
                        }
                    }
                }
            }

            mColorAnimator.cancel();
            mColorAnimator.removeAllUpdateListeners();
            final int finalStart = start;
            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator pAnimation) {
                    mForegroundColorSpan = new ForegroundColorSpan((Integer) pAnimation.getAnimatedValue());
                    mSpannableStringBuilder = new SpannableStringBuilder(pTextToSet);
                    mSpannableStringBuilder.setSpan(mForegroundColorSpan, finalStart, end, 0);
                    AnimatedChronometer.super.setText(mSpannableStringBuilder, pType);
                }
            });

            mColorAnimator.start();
        }


    }
}
