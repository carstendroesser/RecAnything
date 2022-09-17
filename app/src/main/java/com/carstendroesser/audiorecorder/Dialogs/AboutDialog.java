package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.WavFile;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * Created by carstendrosser on 20.11.15.
 */
public class AboutDialog extends AlertDialog {

    @Bind(R.id.titleTextView)
    protected TextView mTitleTextView;
    @Bind(R.id.buttonOk)
    protected Button mButtonOk;
    @Bind(R.id.pathTextView)
    protected TextView mPathTextView;
    @Bind(R.id.categoryTextView)
    protected TextView mCategoryTextView;
    @Bind(R.id.sizeTextView)
    protected TextView mSizeTextView;
    @Bind(R.id.durationTextView)
    protected TextView mDurationTextView;
    @Bind(R.id.mediaTextView)
    protected TextView mMediaTextView;
    @Bind(R.id.noteTextView)
    protected TextView mNoteTextView;
    @Bind(R.id.lastModifiedTextView)
    protected TextView mLastModifiedTextView;
    @Bind(R.id.qualityTextView)
    protected TextView mQualityTextView;
    @Bind(R.id.errorTextView)
    protected TextView mErrorTextView;
    @Bind(R.id.qualityContainer)
    protected View mQualityContainer;

    public AboutDialog(Context pContext, final Record pRecord) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_about, null);
        setView(content);

        ButterKnife.bind(this, content);

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        mTitleTextView.setText(pRecord.getName());

        mPathTextView.setText(pRecord.getPath());
        mCategoryTextView.setText(pRecord.getCategory().getName());
        mSizeTextView.setText(FormatUtils.toReadableSize(pRecord.getFileSize()));

        final Handler handler = new Handler();

        // get the duration for this recording
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final MediaPlayer player = new MediaPlayer();
                    player.setDataSource(pRecord.getPath());
                    player.prepare();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDurationTextView.setText(FormatUtils.toReadableDuration(player.getDuration()));
                            player.release();
                        }
                    });
                } catch (final IOException pException) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDurationTextView.setText("?");
                            mErrorTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();

        mLastModifiedTextView.setText(FormatUtils.toReadableDate(pRecord.getLastModified()));

        if (pRecord.getMediaList().size() > 0) {
            mMediaTextView.setText(R.string.yes);
        } else {
            mMediaTextView.setText(R.string.no);
        }

        if (pRecord.getNote() != null) {
            mNoteTextView.setText(R.string.yes);
        } else {
            mNoteTextView.setText(R.string.no);
        }

        if (pRecord.isWavRecording()) {
            try {
                WavFile wavFile = new WavFile(pRecord.getPath(), "rw");
                String qualityString =
                        pContext.getResources().getString(R.string.samplerate) + " " + wavFile.getSampleRate() + "\n"
                                + pContext.getResources().getString(R.string.bitspersample) + " " + wavFile.getBitsPerSample() + "\n"
                                + pContext.getResources().getString(R.string.channels) + " " + wavFile.getChannels();
                mQualityTextView.setText(qualityString);
            } catch (Exception pException) {
                mQualityTextView.setText("?");
                mErrorTextView.setVisibility(View.VISIBLE);
            }
        } else {
            mQualityContainer.setVisibility(GONE);
        }
    }

}
