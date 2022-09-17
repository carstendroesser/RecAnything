package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquaredImageView extends ImageView {

    public SquaredImageView(Context pContext) {
        this(pContext, null);
    }

    public SquaredImageView(Context pContext, AttributeSet pAttrs) {
        this(pContext, pAttrs, 0);
    }

    public SquaredImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int pWidthMeasureSpec, int pHeightMeasureSpec) {
        super.onMeasure(pWidthMeasureSpec, pWidthMeasureSpec);
    }

}
