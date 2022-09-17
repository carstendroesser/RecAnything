package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class MessageDialog extends AlertDialog {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonConfirm)
    protected Button mButtonConfirm;
    @Bind(R.id.messageTextView)
    protected TextView mMessageTextView;
    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;

    /**
     * Creates a new dialog to be shown to message something.
     *
     * @param pContext              we need that
     * @param pCancelClickListener  handle the action here
     * @param pConfirmClickListener handle the action here
     */
    public MessageDialog(Context pContext, int pTitle, int pMessage, int pCancelButtonText, int pConfirmButtonText, final OnClickListener pCancelClickListener, final OnClickListener pConfirmClickListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_message, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleTextView.setText(pTitle);
        mMessageTextView.setText(pMessage);
        mButtonConfirm.setText(pConfirmButtonText);
        mButtonCancel.setText(pCancelButtonText);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                pCancelClickListener.onClick(MessageDialog.this, 0);
            }
        });

        // pass through the click event
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                pConfirmClickListener.onClick(MessageDialog.this, 0);
            }
        });

        setCancelable(false);
    }

}
