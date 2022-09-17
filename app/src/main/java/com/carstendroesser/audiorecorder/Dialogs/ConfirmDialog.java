package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class ConfirmDialog extends AlertDialog {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonConfirm)
    protected Button mButtonConfirm;
    @Bind(R.id.messageTextView)
    protected TextView mMessageTextView;
    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;

    /**
     * Creates a new dialog to be shown to confirm something.
     *
     * @param pContext              we need that
     * @param pConfirmClickListener handle the action here
     */
    public ConfirmDialog(Context pContext, int pTitle, int pMessage, int pConfirmButtonText, final OnClickListener pConfirmClickListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_confirm, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleTextView.setText(pTitle);
        mMessageTextView.setText(pMessage);
        mButtonConfirm.setText(pConfirmButtonText);

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
                pConfirmClickListener.onClick(ConfirmDialog.this, 0);
            }
        });

    }

}
