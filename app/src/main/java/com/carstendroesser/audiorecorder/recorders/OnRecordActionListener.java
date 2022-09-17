package com.carstendroesser.audiorecorder.recorders;

/**
 * Created by carstendrosser on 06.01.16.
 */
public interface OnRecordActionListener {

    enum Action {
        START_INTENT,
        START_SUCCESSFUL,
        RECORD_FAIL,
        STOP_INTENT,
        STOP_SUCCESSFUL,
        PAUSE_INTENT,
        RESUME_INTENT,
        PAUSED,
        RESUMED,
        SILENT_START,
        SILENT_END,
        SEEK,
        SEEK_END,
        SEEK_LIMIT_REACHED,
        NEW_SIZE,
        ABORT_INTENT,
        ABORT_SUCCESS
    }

    /**
     * <i>startRecording()</i> has been called.
     */
    void onRecordStartIntent();

    /**
     * <i>Recorder.start()</i> did not throw any exception and succeeded.
     */
    void onRecordStartSuccessful();

    /**
     * Something went wrong while recording. See the exception to check what happened in detail.
     *
     * @param pException the thrown exception
     */
    void onRecordFail(Exception pException);

    /**
     * Called just before Recorder.stop() is fired.
     */
    void onRecordStopIntent();

    /**
     * The recording succeeded and is finished.
     *
     * @param pFilePath the filepath of the recording
     */
    void onRecordStopSuccessful(String pFilePath);

    /**
     * Called just before pause() is called.
     */
    void onRecordPauseIntent();

    /**
     * Called just before resume() is called.
     */
    void onRecordResumeIntent();

    /**
     * The recording was paused a moment ago.
     */
    void onRecordPaused();

    /**
     * The recording was resumed a moment ago.
     */
    void onRecordResumed();

    /**
     * A new amplitude has been calculated.
     *
     * @param pAmplitude the highest amplitude of the last few frames
     */
    void onNewAmplitude(int pAmplitude);

    /**
     * Silence has been detected.
     */
    void onSilenceStart();

    /**
     * A previous detected silent part has finished and noise is recognized again.
     */
    void onSilenceEnd();

    /**
     * The recorder is about to get seeked.
     */
    void onSeek();

    /**
     * The seeking has finished, the user lifted the button for seeking.
     */
    void onSeekEnd();

    /**
     * Called when about to get seeked, but no more seeking is possible.
     */
    void onSeekLimitReached();

    /**
     * Called when the size changed, either caused by seeking or recording.
     *
     * @param pSizeInByte the current recording size
     */
    void onNewSize(long pSizeInByte);

    /**
     * Called when the record is about to be aborted.
     */
    void onRecordAbortIntent();

    /**
     * Called when a record was aborted.
     */
    void onRecordAbortSuccess();

}
