package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.utils.DensityConverter;

/**
 * Created by carsten on 15.05.17.
 */

public class SquaredViewPager extends ViewPager {

    public SquaredViewPager(Context pContext) {
        super(pContext);
    }

    public SquaredViewPager(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
    }

    protected void onMeasure(int pWidthMeasureSpec, int pHeightMeasureSpec) {
        int padding = DensityConverter.convertDpToPixels(getContext(), (int) getResources().getDimension(R.dimen.padding_normal));
        super.onMeasure(pWidthMeasureSpec, pWidthMeasureSpec - padding);
    }

}
