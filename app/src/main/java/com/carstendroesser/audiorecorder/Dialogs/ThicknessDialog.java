package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.views.PaintView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ThicknessDialog extends AlertDialog {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonApply)
    protected Button mButtonApply;
    @Bind(R.id.seekbar)
    protected SeekBar mSeekbar;

    public ThicknessDialog(final Context pContext, int pThickness, final OnThicknessSelectListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_thickness, null);
        setView(content);

        ButterKnife.bind(this, content);

        mSeekbar.setProgress(pThickness - PaintView.MIN_STROKEWIDTH);
        mSeekbar.setMax(PaintView.MAX_STROKEWIDTH);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
        mButtonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                pListener.onThicknessSelect(mSeekbar.getProgress() + PaintView.MIN_STROKEWIDTH);
            }
        });
    }

    public interface OnThicknessSelectListener {
        void onThicknessSelect(int pThickness);
    }

}
