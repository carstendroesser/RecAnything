package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.carstendroesser.audiorecorder.utils.DensityConverter;

/**
 * Created by carstendrosser on 18.10.17.
 */

public class IconPreference extends Preference {

    public IconPreference(Context pContext, AttributeSet pAttrs, int pDefStyleAttr, int pDefStyleRes) {
        super(pContext, pAttrs, pDefStyleAttr, pDefStyleRes);
    }

    public IconPreference(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        super(pContext, pAttrs, pDefStyleAttr);
    }

    public IconPreference(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
    }

    @Override
    public void onBindView(View pView) {
        super.onBindView(pView);
        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);

        ImageView imageView = (ImageView) pView.findViewById(android.R.id.icon);
        if (imageView != null) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = DensityConverter.convertDpToPixels(getContext(), 48);
            params.width = DensityConverter.convertDpToPixels(getContext(), 48);
            int padding = DensityConverter.convertDpToPixels(getContext(), 12);
            imageView.setLayoutParams(params);
            imageView.setPadding(padding, padding, padding, padding);
        }
    }

}
