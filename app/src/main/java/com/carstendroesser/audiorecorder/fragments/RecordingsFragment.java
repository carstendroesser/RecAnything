package com.carstendroesser.audiorecorder.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.carstendroesser.audiorecorder.Dialogs.AboutDialog;
import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.Dialogs.ContextMenuDialog;
import com.carstendroesser.audiorecorder.Dialogs.NoteDialog;
import com.carstendroesser.audiorecorder.Dialogs.PhotosDialog;
import com.carstendroesser.audiorecorder.Dialogs.PlayerDialog;
import com.carstendroesser.audiorecorder.Dialogs.RecategoryDialog;
import com.carstendroesser.audiorecorder.Dialogs.RenameDialog;
import com.carstendroesser.audiorecorder.Dialogs.TrimDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.activities.PaintingActivity;
import com.carstendroesser.audiorecorder.adapters.CategoriesListAdapter;
import com.carstendroesser.audiorecorder.adapters.RecordsAdapter;
import com.carstendroesser.audiorecorder.models.Category;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.AudioUtils;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.Deleter;
import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.utils.IntentFactory;
import com.carstendroesser.audiorecorder.utils.PermissionsChecker;
import com.carstendroesser.audiorecorder.utils.Preferences;
import com.carstendroesser.audiorecorder.utils.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

/**
 * Created by carstendrosser on 21.12.15.
 */
public class RecordingsFragment extends Fragment implements RecordsAdapter.OnListItemClickListener, Deleter.OnDeleteListener, TrimDialog.OnTrimActionListener, RenameDialog.OnRenameListener, CategoriesListAdapter.OnCategorySelectedListener, PhotosDialog.OnMediaDeleteListener, RecordsAdapter.OnFilterResetButtonClickListener {

    private final int REQUESTCODE_PAINTING = 101;
    private final int REQUESTCODE_TAKE_PHOTO = 102;
    private final int REQUESTCODE_PICK_PHOTO = 103;

    @Bind(R.id.recordingsEmptyViewContainer)
    LinearLayout mRecordingsEmptyViewContainer;
    @Bind(R.id.recordingsEmptyViewTextView)
    TextView mRecordingsEmptyViewTextView;
    @Bind(R.id.recordingsRecyclerView)
    RecyclerView mRecordingsRecyclerView;
    @Bind(R.id.recordingsLoadingIndicator)
    ProgressBar mRecordingsLoadingIndicator;
    @Bind(R.id.noResultsBar)
    LinearLayout mNoResultsBar;
    @Bind(R.id.noResultsTextView)
    TextView mNoResultsTextView;
    @Bind(R.id.noResultsResetButton)
    Button mNoResultsResetButton;

    private RecordsAdapter mRecordsAdapter;
    private String mTakePhotoPath;
    private Record mRecord;
    private ContextMenuDialog mContextMenuDialog;
    private OnRecordingsChangedListener mOnRecordingsChangedListener;
    private OnNoResultsResetButtonClickListener mOnNoResultsResetButtonClickListener;

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pBundle) {
        View view = pInflater.inflate(R.layout.fragment_recordings, pContainer, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecordingsRecyclerView.setLayoutManager(linearLayoutManager);

        //create new RecordsAdapter without any content
        mRecordsAdapter = new RecordsAdapter(getContext(), new ArrayList<Record>());
        mRecordsAdapter.setOnItemClickListener(this);
        mRecordsAdapter.setOnFilterResetButtonClickListener(this);
        mRecordingsRecyclerView.setAdapter(mRecordsAdapter);

        return view;
    }

    /**
     * Loads the records and shows them in a RecyclerView.
     */
    public void loadRecordings(final String pFilterString) {
        mRecordingsRecyclerView.setVisibility(GONE);
        mRecordingsEmptyViewContainer.setVisibility(GONE);
        mRecordingsLoadingIndicator.setVisibility(View.VISIBLE);
        mRecordingsLoadingIndicator.startAnimation(AnimationUtils.loadAnimation(mRecordingsLoadingIndicator.getContext(), R.anim.fade_in));

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Record> allRecords;
                if (pFilterString == null) {
                    allRecords = DatabaseHelper.getInstance(getContext()).getRecords(Preferences.getRecordingsSorting(getContext()));
                } else {
                    allRecords = DatabaseHelper.getInstance(getContext()).getRecordsBeginningWith(pFilterString, Preferences.getRecordingsSorting(getContext()));
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //will call notifyDataSetChanged
                        mRecordsAdapter.updateList(allRecords);

                        // scroll to top
                        mRecordingsRecyclerView.scrollToPosition(0);

                        //check if emptyview needs to be shown
                        updateEmptyViewVisibility();

                        //show RecyclerView and dismiss ProgressBar
                        mRecordingsRecyclerView.setVisibility(View.VISIBLE);
                        mRecordingsRecyclerView.startAnimation(AnimationUtils.loadAnimation(mRecordingsRecyclerView.getContext(), R.anim.fade_in));
                        mRecordingsLoadingIndicator.setVisibility(GONE);

                        if (pFilterString == null) {
                            // seems to be a loading caused by renaming etc.
                            mOnRecordingsChangedListener.onRecordingsChanged(false);
                            hideNoResultsBar();
                        } else {
                            // seems to be a filtering
                            mOnRecordingsChangedListener.onRecordingsChanged(true);

                            if (allRecords.isEmpty() && pFilterString.length() != 0) {
                                showNoResultsBar(pFilterString);
                            } else {
                                hideNoResultsBar();
                            }
                        }

                    }
                });
            }
        }).start();
    }

    /**
     * Shows the notificationbar, indicating that there are no results
     * for the given filteringtext.
     *
     * @param pText the filtertext for which no results have been found
     */
    private void showNoResultsBar(String pText) {
        mNoResultsTextView.setText(getResources().getString(R.string.filter_no_results) + pText + "'");

        // if it is visible already, don't animate it again
        if (mNoResultsBar.getVisibility() == View.VISIBLE) {
            return;
        }

        mNoResultsBar.setVisibility(View.VISIBLE);
        mNoResultsBar.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fall_down));
    }

    /**
     * Hides the notificationbar for indicating that there are no results.
     */
    private void hideNoResultsBar() {
        mNoResultsTextView.setText("");

        // if it is gone already, don't animate it again
        if (mNoResultsBar.getVisibility() == GONE) {
            return;
        }

        mNoResultsBar.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fall_up));
        mNoResultsBar.setVisibility(View.GONE);
    }

    /**
     * Checks if the adapter is empty and sets the visibility of the
     * emptyView accordingly.
     */
    public void updateEmptyViewVisibility() {
        if (mRecordsAdapter.getItemCount() == 0) {
            mRecordingsEmptyViewContainer.setVisibility(View.VISIBLE);
            mRecordingsEmptyViewContainer.startAnimation(AnimationUtils.loadAnimation(mRecordingsEmptyViewContainer.getContext(), R.anim.fade_in));
        } else {
            mRecordingsEmptyViewContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onListItemClick(View pView, Record pRecord) {
        // show the playerdialog
        if (Settings.isInternalPlayerWanted(getContext())) {
            new PlayerDialog(getContext(), pRecord).show();
        } else {
            startActivity(IntentFactory.getExternalPlayerIntentFor(pView.getContext(), pRecord));
        }
    }

    @Override
    public void onListItemLongClick(View pView, Record pRecord) {
        onListItemMenuClick(pView, pRecord);
    }

    @Override
    public void onListItemMediaItemClick(View pView, Record pRecord, int pMediaItem) {
        // show photosdialog
        new PhotosDialog(getContext(), pRecord, pMediaItem, this).show();
    }

    @Override
    public void onListItemMenuClick(View pView, final Record pRecord) {

        mRecord = pRecord;

        // dismiss existing contextmenudialog if showing
        if (mContextMenuDialog != null && mContextMenuDialog.isShowing()) {
            mContextMenuDialog.dismiss();
        }

        // create new contextmenudialog
        mContextMenuDialog = new ContextMenuDialog(getContext(), pRecord.isWavRecording(), pRecord.getNote() == null, new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                mContextMenuDialog.dismiss();


                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                switch (pView.getId()) {
                    case R.id.contextMenuItemRename:
                        new RenameDialog(getContext(), pRecord.getName(), RecordingsFragment.this).show();
                        break;
                    case R.id.contextMenuItemRecategory:
                        new RecategoryDialog(getContext(), RecordingsFragment.this).show();
                        break;
                    case R.id.contextMenuItemTrim:
                        new TrimDialog(getContext(), pRecord.getId(), RecordingsFragment.this).show();
                        break;
                    case R.id.contextMenuItemDelete:
                        Deleter.deleteRecord(getContext(), pRecord.getId(), RecordingsFragment.this);
                        break;
                    case R.id.contextMenuItemShare:
                        startActivity(IntentFactory.getShareIntentFor(pRecord));
                        break;
                    case R.id.contextMenuItemExternalPlayer:
                        startActivity(IntentFactory.getExternalPlayerIntentFor(getContext(), pRecord));
                        break;
                    case R.id.contextMenuItemNote:
                        newNote();
                        break;
                    case R.id.contextMenuItemTakePhoto:
                        attachPhoto();
                        break;
                    case R.id.contextMenuItemPickPhoto:
                        pickPhoto();
                        break;
                    case R.id.contextMenuItemPainting:
                        startActivityForResult(new Intent(getContext(), PaintingActivity.class), REQUESTCODE_PAINTING);
                        break;
                    case R.id.contextMenuItemAbout:
                        new AboutDialog(getContext(), pRecord).show();
                        break;
                    case R.id.contextMenuItemSetRingtone:
                        if (PermissionsChecker.hasSystemSettingsWritePermission(getContext())) {
                            AudioUtils.setAsRingtone(getContext(), pRecord);
                            Toast.makeText(getContext(), R.string.ringtone_set, Toast.LENGTH_SHORT).show();
                        } else {
                            new ConfirmDialog(getContext(),
                                    R.string.no_permission,
                                    R.string.system_settings_write_permission_grant,
                                    R.string.system_settings_write_premission_button_text,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface pDialog, int pWhich) {
                                            startActivity(IntentFactory.getSystemSettingsPermissionIntent(getContext()));
                                            pDialog.dismiss();
                                        }
                                    }).show();
                        }

                        break;
                }
            }
        });

        mContextMenuDialog.show();
    }

    @Override
    public void onListItemNoteClick(View pView, Record pRecord) {
        // show a new notedialog to edit the clicked note
        new NoteDialog(getContext(), pRecord, new NoteDialog.OnNoteEditListener() {
            @Override
            public void onNoteEdit(boolean pSuccess) {
                if (pSuccess) {
                    loadRecordings(null);
                } else {
                    Toast.makeText(getContext(), R.string.note_edit_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNoteDeleted(boolean pSuccess) {
                if (pSuccess) {
                    loadRecordings(null);
                } else {
                    Toast.makeText(getContext(), R.string.note_delete_error, Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    @Override
    public void onRecordDeleted() {
        loadRecordings(null);
    }

    @Override
    public void onAttachmentsOnlyDeleted() {
        loadRecordings(null);
        Toast.makeText(getContext(), R.string.delete_attachments_only, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrimSucceeded() {
        loadRecordings(null);
    }

    @Override
    public void onTrimError() {
        Toast.makeText(getContext(), R.string.cut_and_trim_error_message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a dialog to create new note for the currently clicked record.
     */
    private void newNote() {
        new NoteDialog(getContext(), mRecord.getId(), new NoteDialog.OnNoteCreateListener() {
            @Override
            public void onNoteCreate(boolean pSuccess) {
                if (pSuccess) {
                    // if succeeded, update the list
                    loadRecordings(null);
                } else {
                    Toast.makeText(getContext(), R.string.note_error, Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    /**
     * Opens the camera to take a photo for the currently clicked record.
     */
    private void attachPhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // remember photopath to insert it in onActivityResult
        mTakePhotoPath = FileFactory.createMediaFilePath(getContext());
        File outputFile = new File(mTakePhotoPath);

        // set the path to save the photo to
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));

        // check if we have any camera-apps
        if (takePhotoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUESTCODE_TAKE_PHOTO);
        } else {
            Toast.makeText(getContext(), R.string.no_camera_found, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Opens the gallery to pick a photo as attachment for the currently
     * clicked record.
     */
    private void pickPhoto() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, null);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUESTCODE_PICK_PHOTO);
    }


    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
        super.onActivityResult(pRequestCode, pResultCode, pData);

        // check if everything is ok
        if (pResultCode != RESULT_OK) {
            return;
        }

        if (pRequestCode == REQUESTCODE_PICK_PHOTO) {

            // inputstream from data
            InputStream inputStream = null;

            // outputstream to write to file
            OutputStream outputStream = null;

            // mediafilename
            String mediaFileName = FileFactory.createMediaFilePath(getContext());

            try {
                inputStream = getContext().getContentResolver().openInputStream(pData.getData());
                outputStream = new FileOutputStream(new File(mediaFileName));

                int read = 0;
                byte[] bytes = new byte[1024];

                // write picked photo to new file
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                // insert media to database
                DatabaseHelper.getInstance(getContext()).insertMedia(mRecord.getId(), 1, mediaFileName);

                //refresh medias
                loadRecordings(null);

            } catch (Exception pException) {
                // TODO
                Toast.makeText(getContext(), R.string.pickphoto_error, Toast.LENGTH_SHORT).show();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException pException) {
                    Toast.makeText(getContext(), R.string.pickphoto_error, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (pRequestCode == REQUESTCODE_PAINTING) {
            // get saved painting
            Bundle extras = pData.getExtras();
            String paintingFilepath = extras.getString("paintingFilepath");
            // insert that painting
            if (DatabaseHelper.getInstance(getContext()).insertMedia(mRecord.getId(), 2, paintingFilepath)) {
                Toast.makeText(getContext(), R.string.painting_added, Toast.LENGTH_SHORT).show();
                loadRecordings(null);
            } else {
                Toast.makeText(getContext(), R.string.painting_add_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (pRequestCode == REQUESTCODE_TAKE_PHOTO) {
            // insert taken photo into database
            if (DatabaseHelper.getInstance(getContext()).insertMedia(mRecord.getId(), 1, mTakePhotoPath)) {
                Toast.makeText(getContext(), R.string.photo_added, Toast.LENGTH_SHORT).show();
                loadRecordings(null);
            } else {
                Toast.makeText(getContext(), R.string.photo_add_failed, Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onRenameClick(String pNewTitle) {
        mRecord.setName(pNewTitle);
        DatabaseHelper.getInstance(getContext()).updateRecord(mRecord);
        loadRecordings(null);
    }

    @Override
    public void onCategorySelected(Category pCategory) {
        mRecord.setCategory(pCategory);
        mRecord.setCategoryId(pCategory.getId());
        DatabaseHelper.getInstance(getContext()).updateRecord(mRecord);
        loadRecordings(null);
    }

    @Override
    public void onMediaDeleted(boolean pDeleted) {
        if (pDeleted) {
            loadRecordings(null);
        } else {
            Toast.makeText(getContext(), R.string.media_delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.noResultsResetButton)
    protected void onNoResultsResetButtonClick() {
        if (mOnNoResultsResetButtonClickListener != null) {
            mOnNoResultsResetButtonClickListener.onNoResultsResetButtonClick();
        }
    }

    public void filter(String pFilterText) {
        loadRecordings(pFilterText);
    }

    /**
     * Sets a listener to get notified about the loading-done and dataset-changed
     * events.
     *
     * @param pListener the listener to notify
     */
    public void setOnRecordingsChangedListener(OnRecordingsChangedListener pListener) {
        mOnRecordingsChangedListener = pListener;
    }

    /**
     * Sets a listener to get notified about the clickevent of the reset button.
     *
     * @param pListener the listener to notify
     */
    public void setOnNoResultsResetButtonClickListener(OnNoResultsResetButtonClickListener pListener) {
        mOnNoResultsResetButtonClickListener = pListener;
    }

    @Override
    public void onFilterResetButtonClick() {
        DatabaseHelper.getInstance(getContext()).clearUnselectedCategories();
        loadRecordings(null);
    }

    /**
     * Listener used to listen to dataset-changes/loading-done events.
     */
    public interface OnRecordingsChangedListener {
        /**
         * Notifies about a loading-done event. This can either be
         * caused by a filtering or usual loading event.
         *
         * @param pFiltered if loading was caused by filtering
         */
        void onRecordingsChanged(boolean pFiltered);
    }

    /**
     * Listener used to listen to onclick events of the reset button.
     */
    public interface OnNoResultsResetButtonClickListener {
        /**
         * The reset button was clicked.
         */
        void onNoResultsResetButtonClick();
    }

}
