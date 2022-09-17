package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.WavFile;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.apptik.widget.MultiSlider;

import static io.apptik.widget.MultiSlider.OnThumbValueChangeListener;

/**
 * Created by carstendrosser on 20.11.15.
 */
public class TrimDialog extends AlertDialog implements OnThumbValueChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, DialogInterface.OnDismissListener {

    private WavFile mWavFile;
    private int mRecordId;
    private OnTrimActionListener mOnTrimActionListener;
    private MediaPlayer mMediaPlayer;
    private long mStartMs;
    private long mEndMs;
    private Handler mHandler;
    private Runnable mAutoEndRunnable;

    @Bind(R.id.loadingContainer)
    protected View mLoadingContainer;
    @Bind(R.id.loadingProgressBarHorizontal)
    protected ProgressBar mLoadingProgressBar;
    @Bind(R.id.startTextView)
    protected TextView mStartTextView;
    @Bind(R.id.endTextView)
    protected TextView mEndTextView;
    @Bind(R.id.multiSlider)
    protected MultiSlider mMultiSlider;
    @Bind(R.id.trimContainer)
    protected View mTrimContainer;
    @Bind(R.id.errorTextView)
    protected TextView mErrorTextView;
    @Bind(R.id.errorCancelButton)
    protected Button mErrorCancelButton;
    @Bind(R.id.trimToButton)
    protected Button mTrimToButton;
    @Bind(R.id.trimCancelButton)
    protected Button mTrimCancelButton;
    @Bind(R.id.playPauseButton)
    protected ImageView mPlayPauseButton;
    @Bind(R.id.infoTextView)
    protected TextView mInfoTextView;

    private enum PlaceHolder {
        LOADING,
        ERROR,
        TRIM;
    }

    /**
     * Creates a new dialog loading the given record. Shows options to trim the recording
     * and will handle the trimming.
     *
     * @param pContext              we need that
     * @param pRecordId             the id of the record to trim
     * @param pOnTrimActionListener to receive callbacks (success/error)
     */
    public TrimDialog(Context pContext, int pRecordId, OnTrimActionListener
            pOnTrimActionListener) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate the view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_trim_record, null);
        setView(content);

        ButterKnife.bind(this, content);

        mRecordId = pRecordId;

        mOnTrimActionListener = pOnTrimActionListener;

        setup();
    }

    /**
     * Setups everything.
     */
    private void setup() {

        // playback end-handler
        mHandler = new Handler();
        mAutoEndRunnable = new Runnable() {
            @Override
            public void run() {
                pauseMediaPlayer();
            }
        };

        // make the dialog not cancelable
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        // listen to dismiss
        setOnDismissListener(this);

        // load the recording
        show(PlaceHolder.LOADING);
        mLoadingProgressBar.setVisibility(View.GONE);

        // setup the multislider's drawables
        mMultiSlider.getThumb(0).setThumb(getContext().getResources().getDrawable(R.drawable.multislider_thumb));
        mMultiSlider.getThumb(1).setThumb(getContext().getResources().getDrawable(R.drawable.multislider_thumb));

        // fetch the record
        Record record = DatabaseHelper.getInstance(getContext()).getRecordById(mRecordId);

        boolean loadedSuccessfully;

        // play/pause prelistening
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if (mMediaPlayer.isPlaying()) {
                    pauseMediaPlayer();
                } else {
                    resumeMediaPlayer();
                }
            }
        });

        // setup multislider by the given wavfile
        if (record != null) {
            try {
                mWavFile = new WavFile(record.getPath(), "rw");
                mMultiSlider.setMax(mWavFile.getFrameCount());
                mMultiSlider.setDrawThumbsApart(false);
                mMultiSlider.setOnThumbValueChangeListener(this);
                mMultiSlider.getThumb(0).setValue(0);
                mMultiSlider.getThumb(1).setValue(mWavFile.getFrameCount());
                mMultiSlider.getThumb(0).setMax(mMultiSlider.getThumb(1).getValue() - mWavFile.getFramesPerSecond());
                mMultiSlider.getThumb(1).setMin(mMultiSlider.getThumb(0).getValue() + mWavFile.getFramesPerSecond());
                loadedSuccessfully = true;
            } catch (IOException pE) {
                pE.printStackTrace();
                loadedSuccessfully = false;
            }
        } else {
            loadedSuccessfully = false;
        }

        // setup mediaplayer by the given wavfile
        if (loadedSuccessfully) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setDataSource(mWavFile.getPath());
                mMediaPlayer.prepare();
            } catch (IOException pE) {
                show(PlaceHolder.ERROR);
            }
        } else {
            show(PlaceHolder.ERROR);
        }

    }

    /**
     * Shows only one placeholder to have no conflict of visibilities.
     *
     * @param pPlaceHolder the content to show
     */
    private void show(PlaceHolder pPlaceHolder) {

        // disable all buttons
        mErrorCancelButton.setVisibility(View.GONE);
        mTrimToButton.setVisibility(View.GONE);
        mTrimCancelButton.setVisibility(View.GONE);

        // show the corrent container
        mTrimContainer.setVisibility(pPlaceHolder == PlaceHolder.TRIM ? View.VISIBLE : View.GONE);
        mLoadingContainer.setVisibility(pPlaceHolder == PlaceHolder.LOADING ? View.VISIBLE : View.GONE);
        mErrorTextView.setVisibility(pPlaceHolder == PlaceHolder.ERROR ? View.VISIBLE : View.GONE);

        // show the correct buttons
        switch (pPlaceHolder) {
            case LOADING:
                // no buttons shown
                break;
            case ERROR:
                mErrorCancelButton.setVisibility(View.VISIBLE);
                break;
            case TRIM:
                mTrimToButton.setVisibility(View.VISIBLE);
                mTrimCancelButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onValueChanged(MultiSlider pMultiSlider, MultiSlider.Thumb thumb, int pThumbIndex, int pValue) {

        // the selection changed, pause the playback
        pauseMediaPlayer();

        // calculate the selection & update views
        try {
            int otherThumbIndex = pThumbIndex == 0 ? 1 : 0;

            MultiSlider.Thumb otherThumb = mMultiSlider.getThumb(otherThumbIndex);

            // always have 1 second delta
            if (thumb.getValue() > otherThumb.getValue()) {
                otherThumb.setMax(thumb.getValue() - mWavFile.getFramesPerSecond());
            } else {
                otherThumb.setMin(thumb.getValue() + mWavFile.getFramesPerSecond());
            }

            // get start & end of selection
            mStartMs = mMultiSlider.getThumb(0).getValue() / mWavFile.getFramesPerSecond() * 1000;
            mEndMs = mMultiSlider.getThumb(1).getValue() / mWavFile.getFramesPerSecond() * 1000;
            mStartTextView.setText(FormatUtils.toReadableDuration(mStartMs));
            mEndTextView.setText(FormatUtils.toReadableDuration(mEndMs));
            updateInfoTextView();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

    /**
     * Pauses the playback of the selection. Will also end the delayed posted runnable to
     * stop the playback.
     */
    private void pauseMediaPlayer() {
        mHandler.removeCallbacks(mAutoEndRunnable);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayPauseButton.setImageResource(R.drawable.play);
        }
    }

    /**
     * Starts playing the selection from the beginning and posts a delayed
     * runnable to stop the playback at the selected end.
     */
    private void resumeMediaPlayer() {
        mHandler.removeCallbacks(mAutoEndRunnable);
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo((int) mStartMs);
            mMediaPlayer.start();

            mHandler.postDelayed(mAutoEndRunnable, mEndMs - mStartMs);
            mPlayPauseButton.setImageResource(R.drawable.pause);
        }
    }

    /**
     * Updates the info textview with length & size of the selection.
     */
    private void updateInfoTextView() {
        // get the count of frames of the selection
        long frames = mMultiSlider.getThumb(1).getValue() - mMultiSlider.getThumb(0).getValue();
        try {
            long bytes = mWavFile.getFrameSize() * frames;
            mInfoTextView.setText(FormatUtils.toReadableDuration(mEndMs - mStartMs) + " / " + FormatUtils.toReadableSize(bytes));
        } catch (IOException pE) {
            pE.printStackTrace();
        }
    }

    @OnClick(R.id.trimCancelButton)
    protected void onTrimCancelButtonClick() {
        dismiss();
    }

    @OnClick(R.id.trimToButton)
    protected void onTrimToButtonClick() {
        // we are doing something, show loading state
        show(PlaceHolder.LOADING);
        setTitle(getContext().getString(R.string.cut_and_trim_trimming));

        // stop the playback
        pauseMediaPlayer();

        // do in background
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // get end & startframe & total count of bytes
                    int beginFrame = mMultiSlider.getThumb(0).getValue();
                    int endFrame = mMultiSlider.getThumb(1).getValue();
                    int byteCount = (endFrame - beginFrame) * mWavFile.getFrameSize();

                    byte[] buffer = new byte[1024];
                    WavFile temporaryFile = new WavFile(FileFactory.createTemporaryFilePath(getContext(), ".wav"), "rw");
                    temporaryFile.writeHeaderData();
                    temporaryFile.copyHeaderDataFrom(mWavFile);
                    temporaryFile.seek(44);

                    int bytesCopied = 0;

                    mWavFile.seekToFrame(beginFrame);

                    // copy the selection to a new file
                    while (mWavFile.getFilePointer() < 44 + endFrame * mWavFile.getFrameSize()) {
                        mWavFile.read(buffer);
                        temporaryFile.write(buffer);
                        bytesCopied += buffer.length;

                        // calculate the progress
                        final float percentCopied = (float) bytesCopied / (float) byteCount;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingProgressBar.setProgress((int) (100 * percentCopied));
                            }
                        });
                    }

                    // write headerdata
                    temporaryFile.writeDataSize(bytesCopied);
                    temporaryFile.writeFilesize(bytesCopied + 44 - 8);

                    // close both files
                    mWavFile.close();
                    temporaryFile.close();

                    // swap files
                    String originalPath = mWavFile.getPath();

                    File deleteFile = new File(originalPath);
                    deleteFile.delete();

                    File trimmedFile = new File(temporaryFile.getPath());
                    trimmedFile.renameTo(new File(originalPath));

                } catch (IOException pE) {
                    pE.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            show(PlaceHolder.ERROR);
                        }
                    });
                    return;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mOnTrimActionListener.onTrimSucceeded();
                        dismiss();
                    }
                });
            }
        }).start();
    }

    @OnClick(R.id.errorCancelButton)
    protected void onErrorCancelButtonClick() {
        dismiss();
        mOnTrimActionListener.onTrimError();
    }

    @Override
    public void onPrepared(MediaPlayer pMediaPlayer) {
        show(PlaceHolder.TRIM);
        mPlayPauseButton.setImageResource(R.drawable.play);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer pMediaPlayer, int pWhat, int pExtra) {
        show(PlaceHolder.ERROR);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer pMediaPlayer) {
        mPlayPauseButton.setImageResource(R.drawable.play);
    }

    @Override
    public void onDismiss(DialogInterface pDialog) {
        pauseMediaPlayer();
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }

    /**
     * Used to receive callback about success and error when trimming.
     */
    public interface OnTrimActionListener {
        /**
         * The trimming succeeded.
         */
        void onTrimSucceeded();

        /**
         * The trimming didn't finish correctly, something went wrong.
         */
        void onTrimError();
    }

}
