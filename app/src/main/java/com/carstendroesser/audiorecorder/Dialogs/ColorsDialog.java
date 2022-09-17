package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.carstendroesser.audiorecorder.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import uz.shift.colorpicker.LineColorPicker;

public class ColorsDialog extends AlertDialog {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonApply)
    protected Button mButtonApply;
    @Bind(R.id.picker)
    protected LineColorPicker mColorPicker;

    public ColorsDialog(final Context pContext, int pSelectedColor, final OnColorSelectListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_colors, null);
        setView(content);

        ButterKnife.bind(this, content);

        mColorPicker.setSelectedColor(pSelectedColor);

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
                pListener.onColorSelect(mColorPicker.getColor());
            }
        });
    }

    public interface OnColorSelectListener {
        void onColorSelect(int pColor);
    }

}
