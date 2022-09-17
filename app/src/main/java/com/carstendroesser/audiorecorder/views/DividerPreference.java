package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.utils.DensityConverter;

/**
 * Created by carstendrosser on 18.10.17.
 */

public class DividerPreference extends Preference {

    public DividerPreference(Context pContext, AttributeSet pAttrs, int pDefStyleAttrs, int pDefStyleRes) {
        super(pContext, pAttrs, pDefStyleAttrs, pDefStyleRes);
    }

    public DividerPreference(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        super(pContext, pAttrs, pDefStyleAttr);
    }

    public DividerPreference(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
    }

    @Override
    protected View onCreateView(ViewGroup pParent) {
        View view = super.onCreateView(pParent);
        view.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                DensityConverter.convertDpToPixels(getContext(), 1)));
        view.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
        return view;
    }

}
