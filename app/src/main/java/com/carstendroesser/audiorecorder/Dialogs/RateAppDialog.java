package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.carstendroesser.audiorecorder.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class RateAppDialog extends AlertDialog {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonConfirm)
    protected Button mButtonConfirm;

    public RateAppDialog(Context pContext, final OnClickListener pConfirmClickListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_rate, null);
        setView(content);

        ButterKnife.bind(this, content);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        // pass through the click event
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
                pConfirmClickListener.onClick(RateAppDialog.this, 0);
            }
        });

    }

}
