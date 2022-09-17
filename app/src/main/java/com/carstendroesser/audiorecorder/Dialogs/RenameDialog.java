package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RenameDialog extends AlertDialog {

    @Bind(R.id.editText)
    protected EditText mEditText;
    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonRename)
    protected Button mButtonRename;

    public RenameDialog(Context pContext, final String pTitle, final OnRenameListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_rename, null);
        setView(content);

        ButterKnife.bind(this, content);

        // show existing title
        mEditText.setText(pTitle);
        mEditText.setSelection(pTitle.length());

        // listen to input changes
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence pCharSequence, int pStart, int pCount, int pAfter) {
                // empty
            }

            @Override
            public void onTextChanged(CharSequence pCharSequence, int pStart, int pBefore, int pCount) {
                if (pCharSequence.toString().equals(pTitle) || mEditText.length() == 0) {
                    mButtonRename.setEnabled(false);
                    mButtonRename.setTextColor(getContext().getResources().getColor(R.color.gray));
                } else {
                    mButtonRename.setEnabled(true);
                    mButtonRename.setTextColor(getContext().getResources().getColor(R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable pEditable) {
                // empty
            }
        });

        // disable renamebutton at first
        mButtonRename.setEnabled(false);
        mButtonRename.setTextColor(getContext().getResources().getColor(R.color.gray));

        // pass-trough clickevent
        mButtonRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
                pListener.onRenameClick(mEditText.getText().toString());
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
    }

    /**
     * Used to get informed about renaming
     */
    public interface OnRenameListener {
        /**
         * Called when rename was clicked
         *
         * @param pNewTitle the new given record title
         */
        void onRenameClick(String pNewTitle);
    }

}
