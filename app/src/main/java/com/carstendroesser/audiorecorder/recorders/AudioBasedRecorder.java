package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.AudioRecord;

import com.carstendroesser.audiorecorder.utils.Preferences;

import java.io.IOException;

/**
 * Created by carstendrosser on 13.12.17.
 */

public abstract class AudioBasedRecorder extends Recorder {

    volatile boolean mIsRecording;
    volatile boolean mIsPaused;
    volatile boolean mIsInSilence;
    volatile boolean mIsAborted;

    AudioRecord mAudioRecord;

    short mChannels;
    short mSamples;

    int mSampleRate;
    int mAmplitude;
    int mBufferSize;
    int mFramePeriod;
    volatile int mPayloadSize;

    volatile int mBytesInSilence;

    AudioBasedRecorder(Context pContext) {
        super(pContext);
    }

    @Override
    public void start() {
        if (mIsRecording) {
            mException = new Exception("Called start when already in recording-state");
            notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
            return;
        }

        notifyListenersInMainThread(OnRecordActionListener.Action.START_INTENT);

        Thread recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    onRecordStart();
                    notifyListenersInMainThread(OnRecordActionListener.Action.START_SUCCESSFUL);

                    while (mIsRecording && !mIsAborted) {
                        onRecordingWhileLoop();

                        if (mIsPaused) {
                            notifyListenersInMainThread(OnRecordActionListener.Action.PAUSE_INTENT);
                            onRecordPause();
                            notifyListenersInMainThread(OnRecordActionListener.Action.PAUSED);

                            while (mIsPaused) {
                                if (mIsAborted) {
                                    notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_INTENT);
                                    onRecordAbort();
                                    notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_SUCCESS);
                                    return;
                                }
                            }

                            notifyListenersInMainThread(OnRecordActionListener.Action.RESUME_INTENT);

                            onRecordResume();

                            notifyListenersInMainThread(OnRecordActionListener.Action.RESUMED);
                        }

                        boolean isProVersion = Preferences.getIsProVersion(mContext);
                        if (getTotalTime() >= MAX_FREE_DURATION_MS && !isProVersion && isRecording()) {
                            stop();
                        }
                    }

                    if (mIsAborted) {
                        notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_INTENT);
                        onRecordAbort();
                        notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_SUCCESS);
                    } else {
                        notifyListenersInMainThread(OnRecordActionListener.Action.STOP_INTENT);
                        onRecordStop();
                        notifyListenersInMainThread(OnRecordActionListener.Action.STOP_SUCCESSFUL);
                    }

                } catch (Exception pException) {
                    pException.printStackTrace();
                    mException = pException;
                    notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
                } finally {
                    onRecordingThreadFinally();
                }
            }
        });

        recordingThread.start();
    }

    @Override
    public void stop() {
        mIsRecording = false;
        mIsPaused = false;
        mIsInSilence = false;
        mIsAborted = false;
    }

    @Override
    public final void resume() {
        mIsPaused = false;
    }

    @Override
    public final boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public final boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public final boolean isInSilence() {
        return mIsInSilence;
    }

    @Override
    public final int getMaxAmplitude() {
        int result = mAmplitude;
        mAmplitude = 0;
        return result;
    }

    @Override
    public final boolean isPausable() {
        return true;
    }

    @Override
    public final synchronized int getTotalTime() {
        if (mAudioRecord == null) {
            return 0;
        }
        // total datasize in bytes * bits / e.g. 44100 * bitsPerSaple * monoOrStereo / ms
        return (mPayloadSize * 8) / (mSampleRate * mSamples * mChannels / 1000);
    }

    @Override
    public final boolean canDetectSilence() {
        return true;
    }

    @Override
    public void release() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
        mAudioRecord = null;
    }

    @Override
    public final void abort() {
        mIsAborted = true;
    }

    private void onRecordPause() {
        stopAmplitudeFeed();
        mAudioRecord.stop();
    }

    private void onRecordResume() {
        mAudioRecord.startRecording();
        startAmplitudeFeed();
    }

    protected void onRecordStop() throws IOException {
        stopAmplitudeFeed();
        mAudioRecord.stop();
    }

    protected void onRecordAbort() throws IOException {
        stopAmplitudeFeed();
        mAudioRecord.stop();
    }

    protected void onRecordingThreadFinally() {
        mIsRecording = false;
        mIsPaused = false;
        mIsInSilence = false;
        mIsAborted = false;
    }

    protected abstract void onRecordStart() throws Exception;

    protected abstract void onRecordingWhileLoop();

}
