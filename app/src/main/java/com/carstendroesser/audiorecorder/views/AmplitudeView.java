package com.carstendroesser.audiorecorder.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.utils.AmplitudeInterpolator;
import com.carstendroesser.audiorecorder.utils.DensityConverter;

/**
 * Created by carstendrosser on 28.09.16.
 */

public class AmplitudeView extends View {

    public static final int MAX_AMPLITUDE = 15000;
    private static final int ANIMATION_DURATION = 3000;

    private Paint mAmplitudePaint;
    private Paint mSilenceGateAmplitudePaint;
    private boolean mSilenceGateAmplitudeEnabled = false;
    private int mSilenceGateAmplitude = 0;
    private int mAmplitude = 0;
    private ObjectAnimator mAmplitudeAnimator;
    private int mMinCircleWidth;

    public AmplitudeView(Context pContext) {
        this(pContext, null);
    }

    public AmplitudeView(Context pContext, AttributeSet pAttrs) {
        this(pContext, pAttrs, 0);
    }

    public AmplitudeView(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        super(pContext, pAttrs, pDefStyleAttr);
        setup(pAttrs);
    }

    /**
     * Setups paint, colors and reads the xml attributes.
     *
     * @param pDefStyleAttr the attributes to read
     */
    private void setup(AttributeSet pDefStyleAttr) {
        mAmplitudePaint = new Paint();
        mAmplitudePaint.setColor(getContext().getResources().getColor(R.color.white));
        mAmplitudePaint.setAntiAlias(true);
        mAmplitudePaint.setAlpha(80);

        mSilenceGateAmplitudePaint = new Paint();
        mSilenceGateAmplitudePaint.setColor(getContext().getResources().getColor(R.color.white));
        mSilenceGateAmplitudePaint.setAntiAlias(true);
        mSilenceGateAmplitudePaint.setStrokeCap(Paint.Cap.ROUND);
        mSilenceGateAmplitudePaint.setStyle(Paint.Style.STROKE);
        mSilenceGateAmplitudePaint.setAlpha(80);
        mSilenceGateAmplitudePaint.setStrokeWidth(DensityConverter.convertDpToPixels(getContext(), 2));

        TypedArray typedArray = getContext().obtainStyledAttributes(pDefStyleAttr, R.styleable.AmplitudeView, 0, 0);
        mMinCircleWidth = (int) typedArray.getDimension(R.styleable.AmplitudeView_minCircleWidth, 0.0f);
        typedArray.recycle();
    }

    /**
     * Sets an amplitude immediatly if its a higher one than the current one.
     * Starts an animator from this amplitude to 0.
     *
     * @param pAmplitude the amplitude to set
     */
    public void setAmplitudeAnimated(int pAmplitude) {

        // check if the amplitude is below the limit
        if (pAmplitude > MAX_AMPLITUDE) {
            pAmplitude = MAX_AMPLITUDE;
        }

        // set amplitude only if it's a higher one
        if (pAmplitude > mAmplitude) {
            setAmplitude(pAmplitude);
            if (mAmplitudeAnimator != null && mAmplitudeAnimator.isRunning()) {
                mAmplitudeAnimator.cancel();
            }
            mAmplitudeAnimator = ObjectAnimator.ofInt(this, "amplitude", 0);
            mAmplitudeAnimator.setInterpolator(new DecelerateInterpolator(8.0f));
            mAmplitudeAnimator.setDuration(ANIMATION_DURATION);
            mAmplitudeAnimator.start();
        }
    }

    /**
     * Sets an amplitude withput animating it back to 0.
     *
     * @param pAmplitude the amplitude to set
     */
    public void setAmplitude(int pAmplitude) {
        // check if amplitude is below the limit
        if (pAmplitude > MAX_AMPLITUDE) {
            pAmplitude = MAX_AMPLITUDE;
        }
        mAmplitude = pAmplitude;
        invalidate();
    }

    /**
     * Returns the current amplitude. Warning: can be below the value which has
     * been set with setAmplitude or setAmplitudeAnimated, because there is
     * a limit check.
     *
     * @return the current amplitude
     */
    public int getAmplitude() {
        return mAmplitude;
    }

    @Override
    protected void onDraw(Canvas pCanvas) {
        super.onDraw(pCanvas);
        // map amplitude to dimension
        float percent = AmplitudeInterpolator.getPercentForAmplitude(mAmplitude);
        int radiusSpace = pCanvas.getWidth() / 2 - mMinCircleWidth / 2;
        int radius = (int) (radiusSpace * percent + mMinCircleWidth / 2);

        int centerX = pCanvas.getWidth() / 2;
        int centerY = pCanvas.getHeight() / 2;

        mAmplitudePaint.setShader(new RadialGradient(centerX,
                centerX,
                radius == 0 ? 1 : radius,
                getContext().getResources().getColor(R.color.ultralightwhite),
                getContext().getResources().getColor(R.color.lightwhite),
                Shader.TileMode.MIRROR));

        pCanvas.drawCircle(centerX, centerY, radius, mAmplitudePaint);

        if (mSilenceGateAmplitudeEnabled) {
            float silenceGatePercent = AmplitudeInterpolator.getPercentForAmplitude(mSilenceGateAmplitude);
            int silenceGateRadius = (int) (radiusSpace * silenceGatePercent + mMinCircleWidth / 2);
            pCanvas.drawArc(new RectF(centerX - silenceGateRadius, centerY - silenceGateRadius, pCanvas.getWidth() - (centerX - silenceGateRadius), pCanvas.getHeight() - (centerY - silenceGateRadius)), 170, 20, false, mSilenceGateAmplitudePaint);
            pCanvas.drawArc(new RectF(centerX - silenceGateRadius, centerY - silenceGateRadius, pCanvas.getWidth() - (centerX - silenceGateRadius), pCanvas.getHeight() - (centerY - silenceGateRadius)), 350, 20, false, mSilenceGateAmplitudePaint);
        }
    }

    @Override
    protected void onMeasure(int pWidthMeasureSpec, int pHeightMeasureSpec) {
        super.onMeasure(pWidthMeasureSpec, pWidthMeasureSpec);
    }

    public void setSilenceGateAmplitudeEnabled(boolean pSilenceGateAmplitudeEnabled) {
        mSilenceGateAmplitudeEnabled = pSilenceGateAmplitudeEnabled;
        invalidate();
    }

    public void setSilenceGateAmplitude(int pSilenceGateAmplitude) {
        mSilenceGateAmplitude = pSilenceGateAmplitude;
        invalidate();
    }

}
