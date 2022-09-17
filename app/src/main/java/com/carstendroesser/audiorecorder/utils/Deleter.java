package com.carstendroesser.audiorecorder.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.Dialogs.ProgressDialog;
import com.carstendroesser.audiorecorder.R;

/**
 * Created by carstendrosser on 29.02.16.
 */
public class Deleter {

    /**
     * Initiates a delete-process, beginning with a confirmation-dialog.
     * It then shows a ProgressDialog and calls the specific listener as
     * soon as deleting has finished.
     *
     * @param pContext        we need that
     * @param pRecordId       the record's id to delete everything for
     * @param pDeleteListener used to get notified when deleting has finished
     */
    public static void deleteRecord(final Context pContext, final int pRecordId, final OnDeleteListener pDeleteListener) {
        // show a confirmation-dialog
        new ConfirmDialog(pContext, R.string.confirm_delete_record_title, R.string.delete_record_dialog_message, R.string.confirm_delete_record_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface pDialog, int pWhich) {
                // as we confirm...

                // ...dismiss the confirmationdialog
                pDialog.dismiss();

                // and show a ProgressDialog instead

                final ProgressDialog progressDialog = new ProgressDialog(pContext, pContext.getString(R.string.progressdialog_delete_in_progress));
                progressDialog.show();

                // delete async
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // delete recordfile, notes & media
                        DatabaseHelper.getInstance(pContext).deleteRecordById(pRecordId);
                        DatabaseHelper.getInstance(pContext).deleteNoteFor(pRecordId);
                        DatabaseHelper.getInstance(pContext).deleteAllMediaFor(pRecordId);

                        // sleep the thread to not have a blink-effect
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException pException) {
                            pException.printStackTrace();
                        }

                        progressDialog.dismiss();

                        // notify that deleting has finished
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                pDeleteListener.onRecordDeleted();
                            }
                        });

                    }
                }).start();

            }
        }).show();
    }

    /**
     * Used to get notified as soons as deleting has finished.
     */
    public interface OnDeleteListener {
        /**
         * Called when the deleting has been finished.
         */
        void onRecordDeleted();

        /**
         * Called when the deleting of attachments has finished.
         */
        void onAttachmentsOnlyDeleted();
    }

}
