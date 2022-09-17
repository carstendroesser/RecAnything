package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class ContinueDialog extends AlertDialog {

    @Bind(R.id.buttonContinue)
    protected Button mButtonContinue;
    @Bind(R.id.messageTextView)
    protected TextView mMessageTextView;
    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;
    @Bind(R.id.checkbox)
    protected CheckBox mCheckBox;

    public ContinueDialog(Context pContext, int pTitle, int pMessage, final OnContinueListener pListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_continue, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleTextView.setText(pTitle);
        mMessageTextView.setText(pMessage);

        mButtonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();

                if (pListener != null) {
                    pListener.onContinued(mCheckBox.isChecked());
                }
            }
        });
    }

    public interface OnContinueListener {
        void onContinued(boolean pDoNotShowAgainChecked);
    }

}
