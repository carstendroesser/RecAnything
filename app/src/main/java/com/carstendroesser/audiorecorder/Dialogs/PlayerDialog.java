package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.Settings;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by carsten on 21.04.17.
 */

public class PlayerDialog extends AlertDialog implements DialogInterface.OnDismissListener, DialogInterface.OnCancelListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    @Bind(R.id.titleTextView)
    TextView mTitleTextView;
    @Bind(R.id.playerControls)
    View mPlayerControls;
    @Bind(R.id.playerErrorTextView)
    View mErrorView;
    @Bind(R.id.playerLoadingIndicator)
    View mLoadingIndicator;
    @Bind(R.id.playPauseButton)
    FloatingActionButton mPlayPauseButton;
    @Bind(R.id.leftChronometerTextView)
    TextView mLeftChronometerTextView;
    @Bind(R.id.rightChronometerTextView)
    TextView mRightChronometerTextView;
    @Bind(R.id.playbackSeekBar)
    SeekBar mSeekBar;
    @Bind(R.id.rewindTextView)
    TextView mRewindTextView;
    @Bind(R.id.forwardTextView)
    TextView mForwardTextView;
    @Bind(R.id.playbackSpeedContainer)
    View mPlaybackSpeedContainer;
    @Bind(R.id.playbackSpeedSeekbar)
    SeekBar mPlayBackSpeedSeekBar;
    @Bind(R.id.playbackSpeedTextView)
    TextView mPlaybackSpeedTextView;

    private enum State {
        LOADING,
        ERROR,
        READY
    }

    private Record mRecord;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private int mRewindSeconds;

    // used to update the seekbar according to the mediaplayer
    private Runnable mUIUpdater = new Runnable() {
        @Override
        public void run() {
            // update the seekbar etc.
            updateControls(mMediaPlayer.getCurrentPosition());
            // recursive!
            mHandler.post(mUIUpdater);
        }
    };

    /**
     * Creats a new dialog to play the given record.
     *
     * @param pContext we need that
     * @param pRecord  the record to play
     */
    public PlayerDialog(Context pContext, Record pRecord) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_player, null);
        setView(content);

        ButterKnife.bind(this, content);

        mTitleTextView.setText(pRecord.getName());

        mRewindSeconds = Settings.getRewindSeconds(getContext());

        mRewindTextView.setText("-" + mRewindSeconds + "s");
        mForwardTextView.setText("+" + mRewindSeconds + "s");

        mRecord = pRecord;
        mSeekBar.setOnSeekBarChangeListener(this);

        mPlayBackSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar pSeekBar, int pProgress, boolean pFromUser) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (pFromUser) {
                        float speed = (float) (0.5 + ((float) pProgress) / 10f);
                        boolean supported = false;
                        if (mMediaPlayer.isPlaying()) {
                            supported = setSpeed(speed);
                        } else {
                            supported = setSpeed(speed);
                            mMediaPlayer.pause();
                        }

                        if(!supported) {
                            mPlaybackSpeedTextView.setText(getContext().getString(R.string.settings_title_playback_speed) + " (x" + speed + ") " + getContext().getString(R.string.playbackspeed_not_supported));
                            setSpeed(1.0f);
                        } else {
                            mPlaybackSpeedTextView.setText(getContext().getString(R.string.settings_title_playback_speed) + " (x" + speed + ")");
                        }

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar pSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar pSeekBar) {

            }
        });

        setup();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean setSpeed(float pSpeed) {
        mMediaPlayer.getPlaybackParams().setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_FAIL);
        try {
            mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(pSpeed));
            return true;
        } catch (IllegalArgumentException pException) {
            return false;
        }
    }

    private void setup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlaybackSpeedContainer.setVisibility(View.VISIBLE);
            mPlaybackSpeedTextView.setText(getContext().getString(R.string.settings_title_playback_speed) + " (x1.0)");
            mPlayBackSpeedSeekBar.setProgress(5);
        }

        // listen to close-events to stop playback properly
        setOnDismissListener(this);
        setOnCancelListener(this);

        // prepare the playback
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        setState(State.LOADING);

        try {
            mMediaPlayer.setDataSource(mRecord.getPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            setState(State.ERROR);
            Log.d("AHA", e.getMessage());
        }
    }

    /**
     * Sets the correct seekbar position and chronometer times.
     *
     * @param pCurrentPosition the current progress
     */
    private void updateControls(int pCurrentPosition) {
        // update the max
        mSeekBar.setMax(mMediaPlayer.getDuration());
        // set current position to seekbar
        mSeekBar.setProgress(pCurrentPosition);
        // set played and remaining playback-time
        mLeftChronometerTextView.setText(FormatUtils.toReadableDuration(pCurrentPosition));
        mRightChronometerTextView.setText(FormatUtils.toReadableDuration(mMediaPlayer.getDuration() - pCurrentPosition));
    }

    /**
     * Sets the correct viewstate
     *
     * @param pState loading, error or ready
     */
    private void setState(State pState) {
        mErrorView.setVisibility(pState == State.ERROR ? View.VISIBLE : GONE);
        mLoadingIndicator.setVisibility(pState == State.LOADING ? VISIBLE : GONE);
        mPlayerControls.setVisibility(pState == State.READY ? VISIBLE : GONE);
    }

    /**
     * Starts updating the controls.
     */
    private void startUIUpdater() {
        mHandler.post(mUIUpdater);
    }

    /**
     * Stops updating the controls.
     */
    private void stopUIUpdater() {
        mHandler.removeCallbacks(mUIUpdater);
    }

    @Override
    public void onDismiss(DialogInterface pDialogInterface) {
        stopUIUpdater();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onCancel(DialogInterface pDialogInterface) {
        stopUIUpdater();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onProgressChanged(SeekBar pSeekBar, int pProgress, boolean pFromUser) {
        // if the user is the source of changes and we have a mediaplayer..
        if (pFromUser && mMediaPlayer != null) {
            // set the controls accordingly
            updateControls(pProgress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar pSeekBar) {
        // to prevent jumping of the seekbar, disable UIUpdater
        // while dragging the seekbar
        stopUIUpdater();
    }

    @Override
    public void onStopTrackingTouch(SeekBar pSeekBar) {
        if (mMediaPlayer == null) {
            return;
        }

        // finally, seek the mediaplayer
        mMediaPlayer.seekTo(pSeekBar.getProgress());

        // if playing, restart the UIUpdater
        if (mMediaPlayer.isPlaying()) {
            startUIUpdater();
        }
    }

    @Override
    public void onPrepared(MediaPlayer pMediaPlayer) {
        // we got everything we need to update the controls
        updateControls(pMediaPlayer.getCurrentPosition());
        // set controls visible
        setState(State.READY);
        mPlayPauseButton.setImageResource(R.drawable.playback_play_white);

        if (Settings.isAutoplayWanted(getContext())) {
            mPlayPauseButton.performClick();
        }

        AudioManager audio = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (currentVolume == 0) {
            Toast.makeText(getContext(), R.string.volume_low, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onError(MediaPlayer pMediaPlayer, int i, int i1) {
        setState(State.ERROR);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer pMediaPlayer) {
        // playback finished, don't update controls anymore
        stopUIUpdater();
        mSeekBar.setProgress(mSeekBar.getMax());
        // update icon on play/pause button
        mPlayPauseButton.setImageResource(R.drawable.playback_play_white);
    }

    @OnClick(R.id.playPauseButton)
    protected void onPlayPauseClicked() {
        if (mMediaPlayer.isPlaying()) {
            // if we playback, pause and stop updater
            mMediaPlayer.pause();
            mPlayPauseButton.setImageResource(R.drawable.playback_play_white);
            stopUIUpdater();
        } else {
            // otherwise, start and begin updating
            mMediaPlayer.start();
            mPlayPauseButton.setImageResource(R.drawable.playback_pause_white);
            startUIUpdater();
        }
    }

    @OnClick(R.id.playbackRewindButton)
    protected void onRewindButtonClicked() {
        int newPosition = mMediaPlayer.getCurrentPosition() - (mRewindSeconds * 1000);
        if (newPosition < 0) {
            newPosition = 0;
        }
        mMediaPlayer.seekTo(newPosition);
        updateControls(mMediaPlayer.getCurrentPosition());
    }

    @OnClick(R.id.playbackForwardButton)
    protected void onForwardButtonClicked() {
        int newPosition = mMediaPlayer.getCurrentPosition() + (mRewindSeconds * 1000);
        if (newPosition > mMediaPlayer.getDuration()) {
            newPosition = mMediaPlayer.getDuration();
        }
        mMediaPlayer.seekTo(newPosition);
        updateControls(mMediaPlayer.getCurrentPosition());
    }

    @OnClick(R.id.closeButton)
    protected void onCloseButtonClicked() {
        dismiss();
    }

}
