package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.carstendroesser.audiorecorder.recorders.OnRecordActionListener.Action;

/**
 * Created by carstendrosser on 20.01.16.
 */
public abstract class Recorder {

    private Handler mHandler;
    private Runnable mTicker;

    protected static final long MAX_FREE_DURATION_MS = 600000; // 10 minutes

    private OnRecordActionListener mListenerInService;
    private OnRecordActionListener mListenerInActivity;

    protected String mFilePath;
    protected Exception mException;
    protected Context mContext;

    public Recorder(Context pContext) {
        mContext = pContext;
        mHandler = new Handler(Looper.getMainLooper());
        //endless loop to post amplitude
        mTicker = new Runnable() {
            @Override
            public void run() {
                mHandler.post(mTicker);
                int amplitude = getMaxAmplitude();
                if (mListenerInActivity != null) {
                    mListenerInActivity.onNewAmplitude(amplitude);
                }
                if (mListenerInService != null) {
                    mListenerInService.onNewAmplitude(amplitude);
                }
            }
        };
    }

    protected final void startAmplitudeFeed() {
        mHandler.removeCallbacks(mTicker);
        mHandler.post(mTicker);
    }

    protected final void stopAmplitudeFeed() {
        mHandler.removeCallbacks(mTicker);
    }

    public void setServiceOnRecordActionListener(OnRecordActionListener pListener) {
        mListenerInService = pListener;
    }

    public void setActivityOnRecordActionListener(OnRecordActionListener pListener) {
        mListenerInActivity = pListener;
    }

    public OnRecordActionListener getActivityOnRecordActionListener() {
        return mListenerInActivity;
    }

    protected void notifyListenersInMainThread(final Action pAction) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (pAction) {
                    case START_INTENT:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordStartIntent();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordStartIntent();
                        }
                        break;
                    case START_SUCCESSFUL:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordStartSuccessful();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordStartSuccessful();
                        }
                        break;
                    case STOP_INTENT:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordStopIntent();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordStopIntent();
                        }
                        break;
                    case STOP_SUCCESSFUL:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordStopSuccessful(mFilePath);
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordStopSuccessful(mFilePath);
                        }
                        break;
                    case PAUSE_INTENT:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordPauseIntent();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordPauseIntent();
                        }
                        break;
                    case PAUSED:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordPaused();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordPaused();
                        }
                        break;
                    case RESUME_INTENT:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordResumeIntent();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordResumeIntent();
                        }
                        break;
                    case RESUMED:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordResumed();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordResumed();
                        }
                        break;
                    case RECORD_FAIL:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordFail(mException);
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordFail(mException);
                        }
                        break;
                    case SILENT_START:
                        if (mListenerInService != null) {
                            mListenerInService.onSilenceStart();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onSilenceStart();
                        }
                        break;
                    case SILENT_END:
                        if (mListenerInService != null) {
                            mListenerInService.onSilenceEnd();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onSilenceEnd();
                        }
                        break;
                    case SEEK:
                        if (mListenerInService != null) {
                            mListenerInService.onSeek();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onSeek();
                        }
                        break;
                    case SEEK_END:
                        if (mListenerInService != null) {
                            mListenerInService.onSeekEnd();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onSeekEnd();
                        }
                        break;
                    case SEEK_LIMIT_REACHED:
                        if (mListenerInService != null) {
                            mListenerInService.onSeekLimitReached();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onSeekLimitReached();
                        }
                        break;
                    case NEW_SIZE:
                        if (mListenerInService != null) {
                            mListenerInService.onNewSize(getCurrentSizeInByte());
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onNewSize(getCurrentSizeInByte());
                        }
                        break;
                    case ABORT_INTENT:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordAbortIntent();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordAbortIntent();
                        }
                        break;
                    case ABORT_SUCCESS:
                        if (mListenerInService != null) {
                            mListenerInService.onRecordAbortSuccess();
                        }
                        if (mListenerInActivity != null) {
                            mListenerInActivity.onRecordAbortSuccess();
                        }
                        break;
                }
            }
        });

    }

    /**
     * Does nothing.
     */
    public void seekBack() {
        // empty
    }

    /**
     * Does nothing.
     */
    public void onSeekEnd() {
        // empty
    }

    public abstract void stop();

    public abstract void pause();

    public abstract void resume();

    public abstract boolean isPaused();

    public abstract boolean isRecording();

    public abstract void start();

    public abstract int getTotalTime();

    public abstract long getCurrentSizeInByte();

    public abstract int getMaxAmplitude();

    public abstract boolean isInSilence();

    public abstract boolean isPausable();

    public abstract boolean isSeekable();

    public abstract boolean canDetectSilence();

    public abstract void abort();

    public abstract void release();

}
