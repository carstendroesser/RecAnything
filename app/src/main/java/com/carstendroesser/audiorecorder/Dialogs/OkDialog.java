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
public class OkDialog extends AlertDialog {

    @Bind(R.id.buttonOk)
    protected Button mButtonOk;
    @Bind(R.id.messageTextView)
    protected TextView mMessageTextView;
    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;

    public OkDialog(Context pContext, int pTitle, int pMessage, final OnClickListener pConfirmClickListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_ok, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleTextView.setText(pTitle);
        mMessageTextView.setText(pMessage);

        // pass through the click event
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                pConfirmClickListener.onClick(OkDialog.this, 0);
            }
        });

    }

}
