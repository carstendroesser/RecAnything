package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.adapters.DirectoryAdapter;
import com.carstendroesser.audiorecorder.utils.Settings;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DirectoryPickerDialog extends AlertDialog implements DirectoryAdapter.OnDirectoryClickListener, DialogInterface.OnShowListener {

    @Bind(R.id.recyclerView)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.buttonSelect)
    protected Button mButtonSelect;
    @Bind(R.id.selectedDirectory)
    protected TextView mSelectedDirectoryTextView;
    @Bind(R.id.selectedDirectoryHint)
    protected TextView mSelectedDirectoryHintTextView;

    private DirectoryAdapter mDirectoryAdapter;
    private File mCurrentDirectory;

    private Toast mToast;

    public DirectoryPickerDialog(Context pContext, final OnDirectorySelectedListener pListener) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_directorypicker, null);
        setView(content);

        ButterKnife.bind(this, content);

        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);

        setOnShowListener(this);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mDirectoryAdapter = new DirectoryAdapter(this);
        mRecyclerView.setAdapter(mDirectoryAdapter);
        mDirectoryAdapter.updateList(new ArrayList<File>());

        mButtonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                pListener.onDirectorySelected(mCurrentDirectory);
                dismiss();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });
    }

    @Override
    public void onShow(DialogInterface pDialog) {
        File file = new File(Settings.getRecordingPath(getContext()));

        if (file.exists()) {
            fill(Settings.getRecordingPath(getContext()));
        } else {
            if (file.mkdirs()) {
                fill(Settings.getRecordingPath(getContext()));
            } else {
                // check external storage
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    fill(Environment.getExternalStorageDirectory().getPath());
                } else {
                    Toast.makeText(getContext(), R.string.storage_busy, Toast.LENGTH_SHORT).show();
                }
            }
        }


        if (file.exists()) {
            fill(Settings.getRecordingPath(getContext()));
        } else {
            fill(Environment.getExternalStorageDirectory().getPath());
            Toast.makeText(getContext(), R.string.storage_busy, Toast.LENGTH_SHORT).show();
        }

    }

    private void fill(String pPath) {
        mCurrentDirectory = new File(pPath);

        if (mCurrentDirectory.canWrite()) {
            mButtonSelect.setEnabled(true);
            mButtonSelect.setTextColor(getContext().getResources().getColor(R.color.black));
            mSelectedDirectoryHintTextView.setText(R.string.select_directory);
            mSelectedDirectoryTextView.setTextColor(getContext().getResources().getColor(R.color.black));
        } else {
            mButtonSelect.setEnabled(false);
            mButtonSelect.setTextColor(getContext().getResources().getColor(R.color.lightgray));
            mSelectedDirectoryHintTextView.setText(R.string.select_directory_error);
            mSelectedDirectoryTextView.setTextColor(getContext().getResources().getColor(R.color.red));
        }

        File[] array = mCurrentDirectory.listFiles();

        if (array == null) {
            mToast.setText(mCurrentDirectory.getName() + " is currently not available");
            mToast.show();
            return;
        }

        mSelectedDirectoryTextView.setText(mCurrentDirectory.getPath());

        ArrayList<File> allFiles = new ArrayList<File>(Arrays.asList(array));

        Collections.sort(allFiles, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
            }
        });

        ArrayList<File> folders = new ArrayList<File>();
        for (int i = 0; i < allFiles.size(); i++) {
            if (allFiles.get(i).isDirectory()) {
                folders.add(allFiles.get(i));
            }
        }

        if (mCurrentDirectory.getParentFile() != null && mCurrentDirectory.getParentFile().canRead()) {
            folders.add(0, null);
        }

        mDirectoryAdapter.updateList(folders);
    }

    @Override
    public void onNavigationUpClicked() {
        fill(mCurrentDirectory.getParentFile().getPath());
    }

    @Override
    public void onDirectoryClicked(File pDirectory) {
        fill(pDirectory.getPath());
    }

    public interface OnDirectorySelectedListener {
        void onDirectorySelected(File pDirectory);
    }

}
