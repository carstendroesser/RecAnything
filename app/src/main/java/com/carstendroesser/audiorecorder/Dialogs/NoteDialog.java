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
import android.widget.ImageView;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Note;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carsten on 09.05.17.
 */

public class NoteDialog extends AlertDialog {

    @Bind(R.id.editText)
    protected EditText mInputField;
    @Bind(R.id.buttonCancel)
    protected Button mCancelButton;
    @Bind(R.id.buttonSave)
    protected Button mSaveButton;
    @Bind(R.id.noteDialogTitle)
    protected TextView mTitleTextView;
    @Bind(R.id.noteDialogDeleteImageView)
    protected ImageView mDeleteImageView;

    /**
     * Creates a new dialog to create a note. Use the other constructor to edit
     * an existing note.
     *
     * @param pContext  we need that
     * @param pRecordId the id of the record the note is for
     * @param pListener notify this listener when the note was created
     */
    public NoteDialog(Context pContext, final int pRecordId, final OnNoteCreateListener pListener) {
        super(pContext);

        setup();

        // hide the deleteimageview because there is nothing to delete
        mDeleteImageView.setVisibility(View.GONE);

        // listen to the input and check if it is empty
        mInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence pCharSequence, int pStart, int pCount, int pAfter) {
                // empty
            }

            @Override
            public void onTextChanged(CharSequence pCharSequence, int pStart, int pBefore, int pCount) {
                if (mInputField.length() == 0) {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setTextColor(getContext().getResources().getColor(R.color.gray));
                } else {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setTextColor(getContext().getResources().getColor(R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable pEditable) {
                // empty
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                // check if empty
                if (mInputField.length() == 0) {
                    return;
                }

                // try to insert note
                if (DatabaseHelper.getInstance(getContext()).insertNote(pRecordId, mInputField.getText().toString())) {
                    pListener.onNoteCreate(true);
                } else {
                    pListener.onNoteCreate(false);
                }

                dismiss();
            }
        });
    }

    /**
     * Creates a new dialog to edit an existing note. Will load the
     * existing note into the inputfield.
     *
     * @param pContext  we need that
     * @param pRecord   the record the note is from
     * @param pListener gets notified when editing has finished
     */
    public NoteDialog(Context pContext, final Record pRecord, final OnNoteEditListener pListener) {
        super(pContext);

        setup();

        // make delete available
        mDeleteImageView.setVisibility(View.VISIBLE);
        mDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
                new ConfirmDialog(getContext(), R.string.confirm_delete_note_title, R.string.note_delete_message, R.string.confirm_delete_note_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        pDialog.dismiss();
                        if (DatabaseHelper.getInstance(getContext()).deleteNoteFor(pRecord.getId())) {
                            // success
                            pListener.onNoteDeleted(true);
                        } else {
                            // error
                            pListener.onNoteDeleted(false);
                        }
                    }
                }).show();
            }
        });

        mTitleTextView.setText(R.string.dialog_note_title_edit);

        // load the note
        mInputField.setText(pRecord.getNote().getText());
        mInputField.setSelection(pRecord.getNote().getText().length());

        // listen to input changes
        mInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence pCharSequence, int pStart, int pCount, int pAfter) {
                // empty
            }

            @Override
            public void onTextChanged(CharSequence pCharSequence, int pStart, int pBefore, int pCount) {
                if (mInputField.length() == 0 || pCharSequence.toString().equals(pRecord.getNote().getText())) {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setTextColor(getContext().getResources().getColor(R.color.gray));
                } else {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setTextColor(getContext().getResources().getColor(R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable pEditable) {
                // empty
            }
        });

        // save the edit note
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Note noteToSave = pRecord.getNote().clone();
                noteToSave.setText(mInputField.getText().toString());
                if (DatabaseHelper.getInstance(getContext()).updateNote(noteToSave)) {
                    pListener.onNoteEdit(true);
                } else {
                    pListener.onNoteEdit(false);
                }
                dismiss();
            }
        });
    }

    private void setup() {
        // setup the style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note, null);
        setView(content);

        ButterKnife.bind(this, content);

        // disable save button at first
        mSaveButton.setEnabled(false);
        mSaveButton.setTextColor(getContext().getResources().getColor(R.color.gray));

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
    }

    /**
     * Listener, used to notify when a note was about to be created.
     */
    public interface OnNoteCreateListener {
        /**
         * Called when a note was about to be created.
         *
         * @param pSuccess true, if created. Otherwise false
         */
        void onNoteCreate(boolean pSuccess);
    }

    /**
     * Listener, used to notify when a note was edited or deleted.
     */
    public interface OnNoteEditListener {
        /**
         * Called if a note was about to get edited within the database.
         *
         * @param pSuccess true if database changed successful. otherwise false
         */
        void onNoteEdit(boolean pSuccess);

        /**
         * Called if a note was about to get deleted.
         *
         * @param pSuccess true if note was deleted. otherwise false
         */
        void onNoteDeleted(boolean pSuccess);
    }

}
