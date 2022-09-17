package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.utils.Preferences;

import java.io.File;
import java.io.IOException;

/**
 * Created by carstendrosser on 26.11.17.
 */

public abstract class MediaRecorderBasedRecorder extends Recorder {

    protected String mFileSuffix;
    protected Handler mRecordSizeHandler;
    protected Runnable mRecordSizeTicker;
    protected MediaRecorder mMediaRecorder;
    protected File mFile;
    protected boolean mIsRecording;
    protected boolean mIsPaused;
    protected long mStartTime;
    protected long mPauseTime;

    public MediaRecorderBasedRecorder(Context pContext, String pFileSuffix) {
        super(pContext);

        mFileSuffix = pFileSuffix;

        mMediaRecorder = new MediaRecorder();
        mRecordSizeHandler = new Handler(Looper.getMainLooper());
        mRecordSizeTicker = new Runnable() {
            @Override
            public void run() {
                mRecordSizeHandler.post(mRecordSizeTicker);
                notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
                boolean isProVersion = Preferences.getIsProVersion(mContext);
                if (getTotalTime() >= MAX_FREE_DURATION_MS && !isProVersion && isRecording()) {
                    stop();
                }
            }
        };
    }

    @Override
    public void stop() {
        notifyListenersInMainThread(OnRecordActionListener.Action.STOP_INTENT);
        mIsRecording = false;
        mIsPaused = false;
        stopAmplitudeFeed();
        mRecordSizeHandler.removeCallbacks(mRecordSizeTicker);
        mMediaRecorder.stop();
        notifyListenersInMainThread(OnRecordActionListener.Action.STOP_SUCCESSFUL);
    }

    @Override
    public void pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mIsRecording && !mIsPaused) {
            notifyListenersInMainThread(OnRecordActionListener.Action.PAUSE_INTENT);
            mIsPaused = true;
            mPauseTime = SystemClock.elapsedRealtime();
            mRecordSizeHandler.removeCallbacks(mRecordSizeTicker);
            mMediaRecorder.pause();
            stopAmplitudeFeed();
            notifyListenersInMainThread(OnRecordActionListener.Action.PAUSED);
        }
    }

    @Override
    public void resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mIsRecording && mIsPaused) {
            notifyListenersInMainThread(OnRecordActionListener.Action.RESUME_INTENT);
            mIsPaused = false;
            mMediaRecorder.resume();
            mStartTime = mStartTime + (SystemClock.elapsedRealtime() - mPauseTime);
            mPauseTime = 0;
            mRecordSizeHandler.removeCallbacks(mRecordSizeTicker);
            mRecordSizeHandler.post(mRecordSizeTicker);
            startAmplitudeFeed();
            notifyListenersInMainThread(OnRecordActionListener.Action.RESUMED);
        }
    }

    @Override
    public boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public void start() {
        notifyListenersInMainThread(OnRecordActionListener.Action.START_INTENT);
        try {
            setup();
            mStartTime = SystemClock.elapsedRealtime();
            mPauseTime = 0;
            mIsRecording = true;
            mFile = new File(FileFactory.createRecordFilePath(mContext, mFileSuffix));
            mFilePath = mFile.getPath();
            mMediaRecorder.setOutputFile(mFilePath);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mRecordSizeHandler.post(mRecordSizeTicker);
            startAmplitudeFeed();
            notifyListenersInMainThread(OnRecordActionListener.Action.START_SUCCESSFUL);
        } catch (IOException pException) {
            notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
            pException.printStackTrace();
        }
    }

    @Override
    public int getTotalTime() {
        if (mIsPaused) {
            return (int) (mPauseTime - mStartTime);
        } else {
            return (int) (SystemClock.elapsedRealtime() - mStartTime);
        }
    }

    @Override
    public long getCurrentSizeInByte() {
        return mFile.length();
    }

    @Override
    public int getMaxAmplitude() {
        return mMediaRecorder.getMaxAmplitude();
    }

    @Override
    public boolean isInSilence() {
        return false;
    }

    @Override
    public boolean isPausable() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    public boolean canDetectSilence() {
        return false;
    }

    @Override
    public void abort() {
        mRecordSizeHandler.removeCallbacks(mRecordSizeTicker);
        stopAmplitudeFeed();
        mPauseTime = 0;
        mIsRecording = false;
        mIsRecording = false;
        notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_INTENT);
        mMediaRecorder.reset();
        mFile.delete();
        notifyListenersInMainThread(OnRecordActionListener.Action.ABORT_SUCCESS);
    }

    @Override
    public void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        if (mFile != null) {
            mFile = null;
        }
    }

    protected abstract void setup();

}
