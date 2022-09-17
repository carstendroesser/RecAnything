package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;

import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class InformationDialog extends AlertDialog {

    @Bind(R.id.buttonOk)
    protected Button mButtonOk;
    @Bind(R.id.messageTextView)
    protected TextView mMessageTextView;
    @Bind(R.id.versionTextView)
    protected TextView mVersionTextView;

    public InformationDialog(Context pContext) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_information, null);
        setView(content);

        ButterKnife.bind(this, content);

        load();

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        PackageInfo packageInfo = null;

        try {
            packageInfo = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException pException) {
            pException.printStackTrace();
        }

        String version = ", version: ?";

        if (packageInfo != null) {
            version = ", version: " + packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        }

        mVersionTextView.setText(getContext().getString(R.string.app_name) + version + "\nCarsten Drösser");
    }

    private void load() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tContents = "";

                try {
                    InputStream stream = getContext().getAssets().open("info.txt");

                    int size = stream.available();
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    stream.close();
                    tContents = new String(buffer);
                } catch (IOException e) {
                    // Handle exceptions here
                }

                final String finalTContents = tContents;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageTextView.setText(finalTContents);
                    }
                });
            }
        }).start();
    }

}
