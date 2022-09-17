package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 18.10.17.
 */

public class PreferenceBlock extends Preference {

    private View.OnClickListener mClickListener;
    private Button mBuyButton;
    private ProgressBar mSpinner;

    public enum WhichView {
        SPINNER,
        BUTTON
    }

    public PreferenceBlock(Context pContext, AttributeSet pAttrs, int pDefStyleAttr, int pDefStyleRes) {
        super(pContext, pAttrs, pDefStyleAttr, pDefStyleRes);
        setup();
    }

    public PreferenceBlock(Context pContext, AttributeSet pAttrs, int pDefStyleAttr) {
        this(pContext, pAttrs, pDefStyleAttr, 0);
    }

    public PreferenceBlock(Context pContext, AttributeSet pAttrs) {
        this(pContext, pAttrs, 0);
    }

    protected void setup() {
        setLayoutResource(R.layout.preference_block);
    }

    @Override
    protected void onBindView(View pView) {
        super.onBindView(pView);

        mSpinner = pView.findViewById(R.id.spinner);
        mBuyButton = pView.findViewById(R.id.button);
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (mClickListener != null) {
                    mClickListener.onClick(pView);
                }
            }
        });
    }

    public void setView(WhichView pWhichView) {
        if(pWhichView == WhichView.SPINNER) {
            mSpinner.setVisibility(View.VISIBLE);
            mBuyButton.setVisibility(View.GONE);
        } else {
            mSpinner.setVisibility(View.GONE);
            mBuyButton.setVisibility(View.VISIBLE);
        }
    }

    public void setOnButtonClickListener(View.OnClickListener pClickListener) {
        mClickListener = pClickListener;
    }

}
