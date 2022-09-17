package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 18.10.17.
 */

public class PreferenceHeader extends PreferenceCategory {

    public PreferenceHeader(Context pContext, AttributeSet pAttrs, int pDefStyleAttr, int pDefStyleRes) {
        super(pContext, pAttrs, pDefStyleAttr, pDefStyleRes);
    }

    public PreferenceHeader(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        super(pContext, pAttrs, pDefStyleAttr);
    }

    public PreferenceHeader(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
    }

    @Override
    protected View onCreateView(ViewGroup pParent) {
        TextView categoryTitle = (TextView) super.onCreateView(pParent);
        categoryTitle.setTextColor(pParent.getResources().getColor(R.color.black));

        return categoryTitle;
    }

}
