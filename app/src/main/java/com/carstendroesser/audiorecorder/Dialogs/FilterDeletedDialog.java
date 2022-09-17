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
public class FilterDeletedDialog extends AlertDialog {

    @Bind(R.id.buttonOk)
    protected Button mButtonOk;

    public FilterDeletedDialog(Context pContext) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_filters_deleted, null);
        setView(content);

        ButterKnife.bind(this, content);

        // pass through the click event
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
    }

}
