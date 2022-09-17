package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;

import butterknife.ButterKnife;

public class ProgressDialog extends AlertDialog {

    public ProgressDialog(Context pContext, String pMessage) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_progress, null);
        setView(content);

        ((TextView) content.findViewById(R.id.textview)).setText(pMessage);

        ButterKnife.bind(this, content);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

}
